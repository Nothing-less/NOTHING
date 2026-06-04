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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 聊天服务器
 * 功能：全双工通信、心跳检测、自动重连支持
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

    // 消息字段 & 类型常量
    private static final String KEY_TYPE = "type";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_MESSAGE_ID = "messageId";
    private static final String KEY_TO_USER_ID = "toUserId";
    private static final String KEY_FROM_USER_ID = "fromUserId";
    private static final String KEY_READ_BY = "readBy";
    private static final String KEY_APPLY_MSG = "applyMsg";

    private static final String TYPE_CONNECTED = "CONNECTED";
    private static final String TYPE_HEARTBEAT = "HEARTBEAT";
    private static final String TYPE_HEARTBEAT_ACK = "HEARTBEAT_ACK";
    private static final String TYPE_CHAT = "CHAT";
    private static final String TYPE_SENT_ACK = "SENT_ACK";
    private static final String TYPE_READ_RECEIPT = "READ_RECEIPT";
    private static final String TYPE_ERROR = "ERROR";
    private static final String TYPE_READ_ACK = "READ_ACK";
    private static final String TYPE_FRIEND_APPLY = "FRIEND_APPLY";

    // 简化发送 JSON 的工具
    private void sendJson(Map<String, Object> payload) {
        sendMessage(JsonUtil.toJson(payload));
    }

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

        // 注册到 Redis 总线
        redisBus.userOnline(userId, this::onRedisMessage);

        // 同步设置 JedisUtil 的在线状态
        ChatJedisUtil.setUserOnline(Long.valueOf(userId), 1);

        startClientHeartbeatCheck();
        sendMessage(JsonUtil.toJson(Map.of(
                "type", "CONNECTED",
                "userId", userId,
                "timestamp", System.currentTimeMillis())));
        logger.info("User: [{}] login ，Current Online number: [{}]", userId, sessions.size());
    }

    @OnMessage
    @SuppressWarnings("unchecked")
    public void onMessage(String message, Session session) {
        try {
            // 解析消息
            Map<String, Object> msgMap = JsonUtil.fromJson(message, Map.class);
            String msgType = (String) msgMap.get("type");

            switch (msgType) {
                case "HEARTBEAT":
                    // 客户端心跳 pong
                    handleClientHeartbeat();
                    break;

                case "CHAT":
                    // 聊天消息
                    handleChatMessage(msgMap);
                    break;

                case "READ_ACK":
                    // 已读回执
                    handleReadAck(msgMap);
                    break;

                case "FRIEND_APPLY":
                    // 好友申请（通过 WebSocket 实时通知）
                    handleFriendApply(msgMap);
                    break;

                default:
                    sendError("未知消息类型: " + msgType);
            }
        } catch (Exception e) {
            sendError("消息处理失败: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(CloseReason reason) {
        cleanup();
        logger.info("User: [{}] logout ，Reason: [{}]", userId, reason.getReasonPhrase());
    }

    @OnError
    public void onError(Throwable error) {
        logger.error("WebSocket Error [{}]: {}", userId, error.getMessage(), error);
        cleanup();
    }

    // ==================== 消息处理 ====================

    /**
     * 处理客户端心跳
     */
    private void handleClientHeartbeat() {

        // 更新 Redis 心跳
        session.getUserProperties().put("lastHeartbeat", System.currentTimeMillis());
        redisBus.heartbeat(userId);

        // 回复 pong
        sendMessage(
                JsonUtil.toJson(
                        Map.of(
                                "type", "HEARTBEAT_ACK",
                                "timestamp", System.currentTimeMillis())));
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Map<String, Object> msgMap) {
        String toUserId = (String) msgMap.get("toUserId");
        String content = (String) msgMap.get("content");

        if (toUserId == null || content == null || content.isBlank()) {
            sendError("消息格式错误: 缺少 toUserId 或 content");
            return;
        }

        try {
            // 第 1 步：保存到数据库
            var respEntity = messageService.sendMessage(
                    Long.valueOf(userId),
                    Long.valueOf(toUserId),
                    content,
                    Message.TYPE_TEXT);

            if (respEntity == null || respEntity.isError() || respEntity.getData() == null) {
                sendError("消息保存失败: " + (respEntity != null ? respEntity.getMessage() : "空响应"));
                return;
            }

            Message savedMsg = respEntity.getData();

            // 第 2 步：推送到等待队列（供长轮询客户端拉取）
            ChatWebSocketServer.pushToWaitQueue(savedMsg.receiverId(), savedMsg);

            // 第 3 步：构建消息 JSON 并 Redis 发布
            String msgJson = JsonUtil.toJson(Map.of(
                    "type", "CHAT",
                    "message", savedMsg));

            redisBus.sendMessage(toUserId, msgJson);

            // 第 4 步：发送成功回执
            sendMessage(JsonUtil.toJson(Map.of(
                    "type", "SENT_ACK",
                    "messageId", savedMsg.msgId(),
                    "toUserId", toUserId,
                    "timestamp", System.currentTimeMillis())));

            logger.debug("消息发送成功 [{}] -> [{}], msgId={}", userId, toUserId, savedMsg.msgId());

        } catch (NumberFormatException e) {
            sendError("用户ID格式错误");
        } catch (Exception e) {
            logger.error("发送消息异常", e);
            sendError("发送失败: " + e.getMessage());
        }
    }

    /**
     * 处理 Redis 推送过来的消息
     */
    private void onRedisMessage(String messageJson) {
        // 直接转发给客户端
        sendMessage(messageJson);
    }

    /**
     * 处理已读回执
     */
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

    /**
     * 处理好友申请（实时通知）
     */
    private void handleFriendApply(Map<String, Object> msgMap) {
        String toUserId = (String) msgMap.get("toUserId");

        // 通知对方有新申请
        redisBus.sendMessage(toUserId, JsonUtil.toJson(Map.of(
                "type", "FRIEND_APPLY",
                "fromUserId", userId,
                "applyMsg", msgMap.get("applyMsg"),
                "timestamp", System.currentTimeMillis())));
    }

    // ==================== 心跳检测 ====================

    /**
     * 启动客户端心跳检测
     * 如果 90 秒未收到客户端心跳，认为断线
     */
    private void startClientHeartbeatCheck() {
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-" + userId);
            t.setDaemon(true);
            return t;
        });

        final long[] lastHeartbeat = { System.currentTimeMillis() };

        // 记录最后一次心跳时间
        session.getUserProperties().put("lastHeartbeat", lastHeartbeat[0]);

        heartbeatScheduler.scheduleAtFixedRate(() -> {
            Long last = (Long) session.getUserProperties().get("lastHeartbeat");
            if (last == null)
                last = 0L;

            logger.debug("Last heart beat: <{}>", last);
            // 90 秒无心跳则关闭连接
            if (System.currentTimeMillis() - last > 90000) {
                logger.warn("Heartbeat Connect Time Out: [{}]", userId);
                try {
                    session.close(new CloseReason(
                            CloseReason.CloseCodes.GOING_AWAY,
                            "Heartbeat timeout"));

                } catch (IOException e) {
                    logger.error("Close WebSocket Connection Failed [{}]: {}", userId, e.getMessage(), e);
                }
                heartbeatScheduler.shutdown();
                // 处理用户下线
                IUserService<User> userService = (IUserService<User>) ServiceFactory.getSingleton(IUserService.class);
                userService.doLogout(User.builder().userId(userId).build());

            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    // ==================== 工具方法 ====================

    private void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                logger.error("Message Send Failed [{}]: {}", userId, e.getMessage(), e);
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

        // ===== 新增：同步设置 JedisUtil 的离线状态 =====
        try {
            ChatJedisUtil.setUserOffline(Long.valueOf(userId), 0);
        } catch (Exception e) {
            logger.warn("Failed to set user offline in JedisUtil: {}", e.getMessage());
        }
        // =============================================

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
        if (userId == null || message == null) {
            return;
        }
        BlockingQueue<Message> queue = getWaitQueue(userId);
        if (!queue.offer(message)) {
            queue.poll();
            queue.offer(message);
        }
    }

    public static void pushToWaitQueue(Long userId, MessageBean messageBean) {
        if (messageBean == null) {
            return;
        }
        pushToWaitQueue(userId, Message.fromEntity(messageBean));
    }

    private void saveMessageAsync(MessageBean message) {
        // 使用线程池异步保存
        
    }

    private void markAsReadAsync(String messageId) {
        // 异步标记已读
    }

    // ==================== 静态工具方法 ====================

    /**
     * 主动推送消息给指定用户（供其他 Service 调用）
     */
    public static void pushToUser(String userId, String message) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                logger.error("Push to User Failed [{}]: {}", userId, e.getMessage(), e);
            }
        }
    }

    /**
     * 检查用户是否连接在当前服务器
     */
    public static boolean isLocalOnline(String userId) {
        Session s = sessions.get(userId);
        return s != null && s.isOpen();
    }

    /**
     * 获取当前服务器在线人数
     */
    public static int getLocalOnlineCount() {
        return sessions.size();
    }

    public static void shutdown() {
        // 关闭所有会话
        sessions.values().forEach(session -> {
            try {
                session.close(new CloseReason(
                        CloseReason.CloseCodes.GOING_AWAY,
                        "Server shutdown"));
            } catch (IOException e) {
                logger.error("Close WebSocket Connection Failed [{}]: {}", e.getMessage(), e);
            }
        });
        sessions.clear();
        redisBus.shutdown();
    }

    public static ChatRedisBus getRedisBus() {
        return redisBus;
    }

}