package icu.nothingless.controller.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.controller.config.ChatConfigurator;
import icu.nothingless.pojo.bean.MessageBean;
import icu.nothingless.pojo.dto.Message;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.service.interfaces.IMessageService;
import icu.nothingless.service.interfaces.IUserService;
import icu.nothingless.tools.ChatJedisUtil;
import icu.nothingless.tools.ChatRedisBus;
import icu.nothingless.tools.JsonUtil;
import icu.nothingless.tools.ServiceFactory;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * WebSocket 聊天服务器
 * 功能：全双工通信、心跳检测、自动重连支持、Redis 消息总线
 */
@ServerEndpoint(value = "/ws/chat/{userId}", configurator = ChatConfigurator.class)
public class ChatWebSocketServer {

    // 本地会话管理（仅当前服务器）
    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Session> getSessions() {
        return sessions;
    }

    // Redis 消息总线（在 ServletContext 注入）
    private static ChatRedisBus redisBus;

    public static void setRedisBus(ChatRedisBus bus) {
        redisBus = bus;
    }

    // WebSocket 等待消息队列（可供长轮询客户端读取）
    private static final int MAX_QUEUE_CAPACITY = 500;
    private static final ConcurrentHashMap<Long, BlockingQueue<Message>> waitQueues = new ConcurrentHashMap<>();

    private static final IMessageService<Message> messageService = ServiceFactory.getSingleton(IMessageService.class);
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketServer.class);

    // 心跳调度器
    private ScheduledExecutorService heartbeatScheduler;

    // 当前会话信息
    private String userId;
    private Session session;

    // 全局连接健康扫描器
    private static final ScheduledExecutorService globalHealthChecker = Executors
            .newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "global-ws-health-checker");
                t.setDaemon(true);
                return t;
            });

    static {
        // 启动全局健康扫描
        globalHealthChecker.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            int checked = 0, closed = 0;

            for (Map.Entry<String, Session> entry : sessions.entrySet()) {
                String uid = entry.getKey();
                Session sess = entry.getValue();
                checked++;

                try {
                    if (!sess.isOpen()) {
                        logger.warn("Found closed session not cleaned: [{}]", uid);
                        cleanupSession(uid);
                        closed++;
                        continue;
                    }

                    Long lastActivity = (Long) sess.getUserProperties().get("lastHeartbeat");
                    if (lastActivity == null)
                        lastActivity = 0L;

                    // 超过 90 秒无活动，强制关闭
                    if (now - lastActivity > 90000) {
                        logger.warn("Force closing zombie connection: [{}]", uid);
                        try {
                            sess.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Zombie connection"));
                        } catch (IOException e) {
                            logger.error("Force close failed [{}]: {}", uid, e.getMessage());
                        }
                        cleanupSession(uid);
                        closed++;
                    }
                } catch (Exception e) {
                    logger.error("Health check error for [{}]: {}", uid, e.getMessage());
                }
            }

            if (checked > 0) {
                logger.debug("Health check: {} sessions checked, {} closed", checked, closed);
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    // 静态 cleanup，供全局扫描使用
    private static void cleanupSession(String uid) {
        sessions.remove(uid);
        Session wsSession = sessions.remove(uid);
        if (wsSession == null) {
            return; // 已经被别的线程清理过了
        }
        // 关闭 WebSocket
        if (wsSession.isOpen()) {
            try {
                wsSession.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Clean up"));
            } catch (IOException e) {
                logger.warn("Close ws failed for [{}]: {}", uid, e.getMessage());
            }
        }
        // 失效 HttpSession
        HttpSession httpSession = (HttpSession) wsSession.getUserProperties()
                .get(ChatConfigurator.HTTP_SESSION);
        if (httpSession != null) {
            try {
                httpSession.invalidate();
                logger.info("HttpSession invalidated for user [{}]", uid);
            } catch (IllegalStateException e) {
                // 已经失效过了，忽略
            }
        }
        
        if (redisBus != null) {
            redisBus.userOffline(uid);
        }

        try {
            ChatJedisUtil.setUserOffline(Long.valueOf(uid), 0);
        } catch (Exception e) {
            logger.warn("Set offline failed: {}", e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.userId = userId;
        this.session = session;

        // 设置 WebSocket 会话超时（Tomcat 特定）
        if (session.getContainer() instanceof WebSocketContainer) {
            // 设置空闲超时 60 秒
            session.setMaxIdleTimeout(60000);
        }

        sessions.put(userId, session);
        getWaitQueue(Long.valueOf(userId));

        // 注册到 Redis 总线（订阅该用户的私有频道）
        redisBus.userOnline(userId, this::onRedisMessage);

        // 同步设置 JedisUtil 的在线状态
        ChatJedisUtil.setUserOnline(Long.valueOf(userId), 1);

        startClientHeartbeatCheck();

        // 发送连接成功消息
        sendMessage(JsonUtil.toJson(Map.of(
                "type", "CONNECTED",
                "userId", userId,
                "timestamp", System.currentTimeMillis())));

        logger.info("User: [{}] login, Current Online: [{}]", userId, sessions.size());
    }

    @OnMessage
    @SuppressWarnings("unchecked")
    public void onMessage(String message, Session session) {
        try {
            Map<String, Object> msgMap = JsonUtil.fromJson(message, Map.class);
            String msgType = (String) msgMap.get("type");

            switch (msgType) {
                case "HEARTBEAT":
                    handleClientHeartbeat();
                    break;

                case "CHAT":
                    handleChatMessage(msgMap);
                    break;

                case "READ_ACK":
                    handleReadAck(msgMap);
                    break;

                case "FRIEND_APPLY":
                    handleFriendApply(msgMap);
                    break;

                default:
                    sendError("未知消息类型: " + msgType);
            }
        } catch (Exception e) {
            logger.error("Message processing failed", e);
            sendError("消息处理失败: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(CloseReason reason) {
        cleanup();
        logger.info("User: [{}] logout, Reason: [{}]", userId, reason.getReasonPhrase());
    }

    @OnError
    public void onError(Throwable error) {
        logger.error("WebSocket Error [{}]: {}", userId, error.getMessage(), error);
        cleanup();
    }

    // ==================== 消息处理 ====================

    private void handleClientHeartbeat() {
        session.getUserProperties().put("lastHeartbeat", System.currentTimeMillis());
        redisBus.heartbeat(userId);

        sendMessage(JsonUtil.toJson(Map.of(
                "type", "HEARTBEAT_ACK",
                "timestamp", System.currentTimeMillis())));
    }

    /**
     * 【核心修复】处理聊天消息
     * 数据流转：保存DB → 推送到接收方
     */
    private void handleChatMessage(Map<String, Object> msgMap) {
        String toUserId = (String) msgMap.get("toUserId");
        String content = (String) msgMap.get("content");

        if (toUserId == null || content == null || content.isBlank()) {
            sendError("消息格式错误: 缺少 toUserId 或 content");
            return;
        }

        try {
            Long fromId = Long.valueOf(userId);
            Long toId = Long.valueOf(toUserId);

            // 第 1 步：保存到数据库
            var respEntity = messageService.sendMessage(fromId, toId, content, Message.TYPE_TEXT);

            if (respEntity == null || respEntity.isError() || respEntity.getData() == null) {
                sendError("消息保存失败: " + (respEntity != null ? respEntity.getMessage() : "空响应"));
                return;
            }

            Message savedMsg = respEntity.getData();

            // 第 2 步：推送到等待队列（供长轮询客户端拉取）
            pushToWaitQueue(savedMsg.receiverId(), savedMsg);

            // 第 3 步：【关键修复】优先直接推送给本地连接的接收方
            boolean localDelivered = tryLocalPush(toId, savedMsg);

            if (!localDelivered) {
                // 本地不在线，通过 Redis 总线处理（跨服务器或离线）
                String msgJson = JsonUtil.toJson(Map.of(
                        "type", "CHAT",
                        "message", savedMsg));
                redisBus.sendMessage(toUserId, msgJson);
            }

            // 第 4 步：增加未读计数（Redis）
            ChatJedisUtil.incrUnread(toId, fromId);

            // 第 5 步：缓存最近消息
            // ChatJedisUtil.cacheRecentMessage(toId, fromId,
            // MessageBean.fromDTO(savedMsg));

            // 第 6 步：发送成功回执给发送方
            sendMessage(JsonUtil.toJson(Map.of(
                    "type", "SENT_ACK",
                    "messageId", savedMsg.msgId(),
                    "toUserId", toUserId,
                    "timestamp", System.currentTimeMillis())));

            logger.debug("Message sent [{}] -> [{}], msgId={}", userId, toUserId, savedMsg.msgId());

        } catch (NumberFormatException e) {
            sendError("用户ID格式错误");
        } catch (Exception e) {
            logger.error("Send message exception", e);
            sendError("发送失败: " + e.getMessage());
        }
    }

    /**
     * 尝试直接推送给本地连接的接收方
     */
    private boolean tryLocalPush(Long toUserId, Message message) {
        Session targetSession = sessions.get(String.valueOf(toUserId));
        if (targetSession != null && targetSession.isOpen()) {
            try {
                String msgJson = JsonUtil.toJson(Map.of(
                        "type", "CHAT",
                        "message", message));
                targetSession.getBasicRemote().sendText(msgJson);
                logger.debug("Local push success: [{}] -> [{}]", userId, toUserId);
                return true;
            } catch (IOException e) {
                logger.warn("Local push failed: [{}] -> [{}]: {}", userId, toUserId, e.getMessage());
            }
        }
        return false;
    }

    /**
     * 处理 Redis 推送过来的消息（来自其他服务器）
     */
    private void onRedisMessage(String messageJson) {
        // 直接转发给客户端
        sendMessage(messageJson);
    }

    private void handleReadAck(Map<String, Object> msgMap) {
        String messageId = (String) msgMap.get("messageId");
        String fromUserId = (String) msgMap.get("fromUserId");

        // 更新数据库消息状态
        markAsReadAsync(messageId);

        // 通知发送者消息已读
        redisBus.sendMessage(fromUserId, JsonUtil.toJson(Map.of(
                "type", "READ_RECEIPT",
                "messageId", messageId,
                "readBy", userId,
                "timestamp", System.currentTimeMillis())));
    }

    private void handleFriendApply(Map<String, Object> msgMap) {
        String toUserId = (String) msgMap.get("toUserId");
        String applyMsg = msgMap.get("applyMsg") != null ? (String) msgMap.get("applyMsg") : "";

        // 通知对方有新申请
        redisBus.sendMessage(toUserId, JsonUtil.toJson(Map.of(
                "type", "FRIEND_APPLY",
                "fromUserId", userId,
                "applyMsg", applyMsg,
                "timestamp", System.currentTimeMillis())));
    }

    // ==================== 心跳检测 ====================

    private void startClientHeartbeatCheck() {
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-" + userId);
            t.setDaemon(true);
            return t;
        });

        session.getUserProperties().put("lastHeartbeat", System.currentTimeMillis());

        heartbeatScheduler.scheduleAtFixedRate(() -> {

            try {
                // 主动发送 Ping 帧
                if (session != null && session.isOpen()) {
                    session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[] { 0x01 }));
                }
            } catch (IOException e) {
                logger.warn("Ping failed [{}], connection dead: {}", userId, e.getMessage());
                cleanup();
                return;
            }

            Long last = (Long) session.getUserProperties().get("lastHeartbeat");
            if (last == null)
                last = 0L;

            if (System.currentTimeMillis() - last > 90000) {
                logger.warn("Heartbeat timeout: [{}]", userId);
                try {
                    session.close(new CloseReason(
                            CloseReason.CloseCodes.GOING_AWAY,
                            "Heartbeat timeout"));
                } catch (IOException e) {
                    logger.error("Close session failed [{}]: {}", userId, e.getMessage());
                }
                heartbeatScheduler.shutdown();

                // 处理用户下线
                try {
                    IUserService<User> userService = (IUserService<User>) ServiceFactory
                            .getSingleton(IUserService.class);
                    userService.doLogout(User.builder().userId(userId).build());
                } catch (Exception e) {
                    logger.error("Logout failed [{}]: {}", userId, e.getMessage());
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    // ==================== 工具方法 ====================

    private void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                logger.error("Send failed [{}]: {}", userId, e.getMessage());
            }
        }
    }

    private void sendError(String error) {
        sendMessage(JsonUtil.toJson(Map.of(
                "type", "ERROR",
                "message", error)));
    }

    private void cleanup() {
        sessions.remove(userId);
        redisBus.userOffline(userId);

        try {
            ChatJedisUtil.setUserOffline(Long.valueOf(userId), 0);
        } catch (Exception e) {
            logger.warn("Set offline failed: {}", e.getMessage());
        }

        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
        }
    }

    public static BlockingQueue<Message> getWaitQueue(Long userId) {
        if (userId == null) {
            return new LinkedBlockingQueue<>(MAX_QUEUE_CAPACITY);
        }
        return waitQueues.computeIfAbsent(userId, key -> new LinkedBlockingQueue<>(MAX_QUEUE_CAPACITY));
    }

    public static void pushToWaitQueue(Long userId, Message message) {
        if (userId == null || message == null)
            return;
        BlockingQueue<Message> queue = getWaitQueue(userId);
        if (!queue.offer(message)) {
            queue.poll();
            queue.offer(message);
        }
    }

    public static void pushToWaitQueue(Long userId, MessageBean messageBean) {
        if (messageBean == null)
            return;
        pushToWaitQueue(userId, Message.fromEntity(messageBean));
    }

    private void markAsReadAsync(String messageId) {
        // 异步标记已读，由 Service 层实现
        // messageService.markAsRead(messageId);
    }

    // ==================== 静态工具方法 ====================

    public static void pushToUser(String userId, String message) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                logger.error("Push to user failed [{}]: {}", userId, e.getMessage());
            }
        }
    }

    public static boolean isLocalOnline(String userId) {
        Session s = sessions.get(userId);
        return s != null && s.isOpen();
    }

    public static int getLocalOnlineCount() {
        return sessions.size();
    }

    public static void shutdown() {
        // 關閉全局健康檢查器 ★
        if (globalHealthChecker != null && !globalHealthChecker.isShutdown()) {
            globalHealthChecker.shutdownNow();
            try {
                globalHealthChecker.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 關閉所有會話
        sessions.values().forEach(session -> {
            try {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.GOING_AWAY,
                        "Server shutdown"));
            } catch (IOException e) {
                logger.error("Close session failed: {}", e.getMessage());
            }
        });
        sessions.clear();

        // 關閉 RedisBus
        if (redisBus != null) {
            redisBus.shutdown();
        }
    }

    public static ChatRedisBus getRedisBus() {
        return redisBus;
    }
}