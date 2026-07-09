package icu.nothingless.controller;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 聊天服务器
 * 功能：全双工通信、心跳检测、自动重连支持、Redis 消息总线
 */
@ServerEndpoint(value = "/ws/chat/{userId}", configurator = ChatConfigurator.class)
public class ChatWebSocketServer {

    // 本地会话管理（仅当前服务器）
    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

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

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.userId = userId;
        this.session = session;
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
            ChatJedisUtil.cacheRecentMessage(toId, fromId, MessageBean.fromDTO(savedMsg));

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
     * 【新增】尝试直接推送给本地连接的接收方
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
            Long last = (Long) session.getUserProperties().get("lastHeartbeat");
            if (last == null) last = 0L;

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
                    IUserService<User> userService = (IUserService<User>) ServiceFactory.getSingleton(IUserService.class);
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
        if (userId == null || message == null) return;
        BlockingQueue<Message> queue = getWaitQueue(userId);
        if (!queue.offer(message)) {
            queue.poll();
            queue.offer(message);
        }
    }

    public static void pushToWaitQueue(Long userId, MessageBean messageBean) {
        if (messageBean == null) return;
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
        if (redisBus != null) {
            redisBus.shutdown();
        }
    }

    public static ChatRedisBus getRedisBus() {
        return redisBus;
    }
}