package icu.nothingless.controller;

import icu.nothingless.controller.config.ChatConfigurator;
import icu.nothingless.pojo.bean.MessageBean;
import icu.nothingless.tools.ChatRedisBus;
import icu.nothingless.tools.JsonUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
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
    
    // Redis 消息总线（由 ServletContext 初始化时注入）
    private static ChatRedisBus redisBus;
    
    // 心跳调度器
    private ScheduledExecutorService heartbeatScheduler;
    
    // 当前会话信息
    private String userId;
    private Session session;
    
    /**
     * 设置 RedisBus（由 ContextListener 调用初始化）
     */
    public static void setRedisBus(ChatRedisBus bus) {
        redisBus = bus;
    }
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.userId = userId;
        this.session = session;
        
        // 注册会话
        sessions.put(userId, session);
        
        // 注册到 Redis 总线
        redisBus.userOnline(userId, this::onRedisMessage);
        
        // 启动客户端心跳检测（检查客户端是否活着）
        startClientHeartbeatCheck();
        
        // 发送连接成功确认
        sendMessage(JsonUtil.toJson(Map.of(
            "type", "CONNECTED",
            "userId", userId,
            "timestamp", System.currentTimeMillis()
        )));
        
        System.out.println("用户上线: " + userId + ", 当前在线: " + sessions.size());
    }
    
    @OnMessage
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
        System.out.println("用户离线: " + userId + ", 原因: " + reason.getReasonPhrase());
    }
    
    @OnError
    public void onError(Throwable error) {
        System.err.println("WebSocket 错误 [" + userId + "]: " + error.getMessage());
        cleanup();
    }
    
    // ==================== 消息处理 ====================
    
    /**
     * 处理客户端心跳
     */
    private void handleClientHeartbeat() {
        // 更新 Redis 心跳
        redisBus.heartbeat(userId);
        
        // 回复 pong
        sendMessage(JsonUtil.toJson(Map.of(
            "type", "HEARTBEAT_ACK",
            "timestamp", System.currentTimeMillis()
        )));
    }
    
    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Map<String, Object> msgMap) {
        String toUserId = (String) msgMap.get("toUserId");
        String content = (String) msgMap.get("content");
        
        // 构建消息对象
        MessageBean message = new MessageBean();
        message.setSenderId(Long.valueOf(userId));
        message.setReceiverId(Long.valueOf(toUserId));
        message.setContent(content);
        message.setSendTime(new java.util.Date());
        message.setStatus(0); // 未读
        
        // 保存到数据库（异步）
        saveMessageAsync(message);
        
        // 推送给接收者
        String msgJson = JsonUtil.toJson(Map.of(
            "type", "CHAT",
            "message", message
        ));
        
        redisBus.sendMessage(toUserId, msgJson);
        
        // 发送回执给发送者
        sendMessage(JsonUtil.toJson(Map.of(
            "type", "SENT_ACK",
            "messageId", message.getMsgId(),
            "toUserId", toUserId,
            "timestamp", System.currentTimeMillis()
        )));
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
            "timestamp", System.currentTimeMillis()
        )));
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
            "timestamp", System.currentTimeMillis()
        )));
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
        
        final long[] lastHeartbeat = {System.currentTimeMillis()};
        
        // 记录最后一次心跳时间
        session.getUserProperties().put("lastHeartbeat", lastHeartbeat[0]);
        
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            Long last = (Long) session.getUserProperties().get("lastHeartbeat");
            if (last == null) last = 0L;
            
            // 90 秒无心跳则关闭连接
            if (System.currentTimeMillis() - last > 90000) {
                System.err.println("心跳超时，关闭连接: " + userId);
                try {
                    session.close(new CloseReason(
                        CloseReason.CloseCodes.GOING_AWAY,
                        "Heartbeat timeout"
                    ));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                heartbeatScheduler.shutdown();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    // ==================== 工具方法 ====================
    
    private void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                System.err.println("发送消息失败 [" + userId + "]: " + e.getMessage());
            }
        }
    }
    
    private void sendError(String error) {
        sendMessage(JsonUtil.toJson(Map.of(
            "type", "ERROR",
            "message", error
        )));
    }
    
    private void cleanup() {
        // 移除会话
        sessions.remove(userId);
        
        // 通知 Redis 下线
        redisBus.userOffline(userId);
        
        // 停止心跳检测
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
        }
    }
    
    private void saveMessageAsync(MessageBean message) {
        // 使用线程池异步保存
        // MessageService.save(message);
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
                e.printStackTrace();
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
}