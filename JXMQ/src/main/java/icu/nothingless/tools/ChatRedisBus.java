package icu.nothingless.tools;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Redis 消息总线
 * 功能：发布订阅、消息队列、在线状态、心跳检测
 */
public class ChatRedisBus {
    
    // Redis Key 常量
    private static final String KEY_USER_ONLINE = "chat:online:%s";      // 用户在线状态（Hash：serverId, lastHeartbeat）
    private static final String KEY_USER_MSG_QUEUE = "chat:queue:%s";    // 用户消息队列（List）
    private static final String KEY_USER_CHANNEL = "chat:channel:%s";    // 用户私有频道
    private static final String KEY_GLOBAL_CHANNEL = "chat:global";     // 全局广播频道
    private static final String KEY_HEARTBEAT_ZSET = "chat:heartbeat";    // 心跳有序集合（用于检测离线）
    
    // 配置参数
    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;   // 心跳超时时间
    private static final int MSG_QUEUE_EXPIRE_SECONDS = 7 * 24 * 3600; // 消息队列保留7天
    private static final int MAX_OFFLINE_MSG = 100;          // 最大离线消息数
    
    private final JedisPool jedisPool;
    private final String serverId;  // 当前服务器标识
    private final ExecutorService subscribeExecutor;
    private volatile boolean running = true;
    
    // 本地 WebSocket 会话管理（仅保存当前服务器的连接）
    private final ConcurrentHashMap<String, Consumer<String>> localSubscribers = new ConcurrentHashMap<>();
    
    public ChatRedisBus(JedisPool jedisPool, String serverId) {
        this.jedisPool = jedisPool;
        this.serverId = serverId;
        this.subscribeExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "redis-subscribe-" + r.hashCode());
            t.setDaemon(true);
            return t;
        });
        
        // 启动心跳检测线程
        startHeartbeatChecker();
    }
    
    // ==================== 1. 用户上线/下线 ====================
    
    /**
     * 用户上线 - WebSocket 连接时调用
     */
    public void userOnline(String userId, Consumer<String> messageHandler) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = String.format(KEY_USER_ONLINE, userId);
            long now = System.currentTimeMillis();
            
            // 记录用户在线，归属到当前服务器
            jedis.hset(key, "serverId", serverId);
            jedis.hset(key, "connectTime", String.valueOf(now));
            jedis.hset(key, "lastHeartbeat", String.valueOf(now));
            jedis.expire(key, HEARTBEAT_TIMEOUT_SECONDS);
            
            // 注册本地消息处理器
            localSubscribers.put(userId, messageHandler);
            
            // 订阅用户私有频道（异步，避免阻塞）
            subscribeUserChannel(userId);
            
            // 推送离线消息
            deliverOfflineMessages(userId, messageHandler);
        }
    }
    
    /**
     * 用户下线 - WebSocket 断开时调用
     */
    public void userOffline(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 清除在线状态
            jedis.del(String.format(KEY_USER_ONLINE, userId));
            
            // 移除本地订阅
            localSubscribers.remove(userId);
            
            // 注意：Redis 的订阅连接需要单独关闭，见 unsubscribeUserChannel
        }
    }
    
    // ==================== 2. 心跳检测 ====================
    
    /**
     * 更新心跳
     */
    public void heartbeat(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = String.format(KEY_USER_ONLINE, userId);
            long now = System.currentTimeMillis();
            
            // 更新心跳时间
            jedis.hset(key, "lastHeartbeat", String.valueOf(now));
            jedis.expire(key, HEARTBEAT_TIMEOUT_SECONDS);
            
            // 同时更新心跳集合（用于全局检测）
            jedis.zadd(KEY_HEARTBEAT_ZSET, now, userId);
        }
    }
    
    /**
     * 检查用户是否在线（实时）
     */
    public boolean isOnline(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = String.format(KEY_USER_ONLINE, userId);
            return jedis.exists(key);
        }
    }
    
    /**
     * 获取用户在线状态（包含详细信息的 Map）
     */
    public Map<String, String> getOnlineStatus(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = String.format(KEY_USER_ONLINE, userId);
            return jedis.hgetAll(key);
        }
    }
    
    /**
     * 启动心跳检测线程 - 自动清理超时用户
     */
    private void startHeartbeatChecker() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-checker");
            t.setDaemon(true);
            return t;
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                long timeout = System.currentTimeMillis() - (HEARTBEAT_TIMEOUT_SECONDS * 1000);
                
                // 获取超时用户
                List<String> timeoutUsers = jedis.zrangeByScore(KEY_HEARTBEAT_ZSET, 0, timeout);
                
                for (String userId : timeoutUsers) {
                    // 再次确认是否真的离线（可能刚好更新）
                    String key = String.format(KEY_USER_ONLINE, userId);
                    String lastHeartbeat = jedis.hget(key, "lastHeartbeat");
                    
                    if (lastHeartbeat == null || 
                        Long.parseLong(lastHeartbeat) < timeout) {
                        
                        // 标记离线
                        jedis.del(key);
                        jedis.zrem(KEY_HEARTBEAT_ZSET, userId);
                        
                        System.out.println("用户离线检测: " + userId);
                        
                        // 触发离线回调（可扩展）
                        onUserTimeout(userId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 30, 30, TimeUnit.SECONDS); // 每30秒检查一次
    }
    
    /**
     * 用户超时回调 - 可扩展清理逻辑
     */
    private void onUserTimeout(String userId) {
        // 清理相关资源，通知好友等
    }
    
    // ==================== 3. 消息发布订阅 ====================
    
    /**
     * 发送消息给指定用户
     * 策略：先尝试推送到本地 WebSocket，失败则存入 Redis 队列
     */
    public void sendMessage(String toUserId, String messageJson) {
        // 1. 尝试本地推送（用户连接在当前服务器）
        Consumer<String> localHandler = localSubscribers.get(toUserId);
        if (localHandler != null) {
            try {
                localHandler.accept(messageJson);
                return; // 推送成功，结束
            } catch (Exception e) {
                // 本地推送失败，继续走 Redis
            }
        }
        
        // 2. 用户不在本地，发布到 Redis 频道
        try (Jedis jedis = jedisPool.getResource()) {
            String channel = String.format(KEY_USER_CHANNEL, toUserId);
            
            // 检查用户是否在线（在任何服务器）
            if (isOnline(toUserId)) {
                // 在线：发布到频道，目标服务器的订阅线程会接收
                jedis.publish(channel, messageJson);
            } else {
                // 离线：存入消息队列
                saveOfflineMessage(toUserId, messageJson);
            }
        }
    }
    
    /**
     * 保存离线消息
     */
    private void saveOfflineMessage(String userId, String messageJson) {
        try (Jedis jedis = jedisPool.getResource()) {
            String queueKey = String.format(KEY_USER_MSG_QUEUE, userId);
            
            // 使用 Pipeline 保证原子性
            Pipeline pipe = jedis.pipelined();
            pipe.lpush(queueKey, messageJson);
            pipe.ltrim(queueKey, 0, MAX_OFFLINE_MSG - 1); // 保留最近100条
            pipe.expire(queueKey, MSG_QUEUE_EXPIRE_SECONDS);
            pipe.sync();
        }
    }
    
    /**
     * 推送离线消息给刚上线的用户
     */
    private void deliverOfflineMessages(String userId, Consumer<String> handler) {
        try (Jedis jedis = jedisPool.getResource()) {
            String queueKey = String.format(KEY_USER_MSG_QUEUE, userId);
            
            // 批量获取离线消息
            List<String> messages = jedis.lrange(queueKey, 0, -1);
            if (messages.isEmpty()) return;
            
            // 推送给客户端
            for (String msg : messages) {
                handler.accept(msg);
            }
            
            // 清空队列
            jedis.del(queueKey);
        }
    }
    
    /**
     * 订阅用户私有频道（每个用户一个独立订阅线程）
     */
    private void subscribeUserChannel(String userId) {
        subscribeExecutor.submit(() -> {
            // 每个订阅需要独立的 Jedis 连接
            try (Jedis jedis = jedisPool.getResource()) {
                String channel = String.format(KEY_USER_CHANNEL, userId);
                
                JedisPubSub pubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        // 收到消息，推送给本地 WebSocket
                        Consumer<String> handler = localSubscribers.get(userId);
                        if (handler != null) {
                            handler.accept(message);
                        }
                    }
                    
                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        System.out.println("订阅频道: " + channel);
                    }
                    
                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        System.out.println("取消订阅: " + channel);
                    }
                };
                
                // 阻塞订阅，直到取消订阅
                jedis.subscribe(pubSub, channel);
                
            } catch (Exception e) {
                if (running) {
                    System.err.println("订阅异常: " + e.getMessage());
                    // 可加入重试逻辑
                }
            }
        });
    }
    
    /**
     * 取消订阅（用户下线时）
     */
    public void unsubscribeUserChannel(String userId) {
        // 通过发布特殊消息触发订阅线程退出，或维护 pubSub 引用 map
        // 简化处理：直接关闭连接会导致异常，建议维护 JedisPubSub 引用
    }
    
    // ==================== 4. 广播与群组 ====================
    
    /**
     * 全局广播
     */
    public void broadcast(String messageJson) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(KEY_GLOBAL_CHANNEL, messageJson);
        }
    }
    
    /**
     * 订阅全局频道
     */
    public void subscribeGlobal(Consumer<String> handler) {
        subscribeExecutor.submit(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                JedisPubSub pubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        handler.accept(message);
                    }
                };
                jedis.subscribe(pubSub, KEY_GLOBAL_CHANNEL);
            }
        });
    }
    
    // ==================== 5. 工具方法 ====================
    
    /**
     * 获取在线用户列表
     */
    public Set<String> getOnlineUsers() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 扫描所有在线用户
            Set<String> users = new HashSet<>();
            String pattern = KEY_USER_ONLINE.replace("%s", "*");
            
            // 使用 SCAN 避免阻塞
            String cursor = "0";
            do {
                ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(pattern).count(100));
                users.addAll(scan.getResult());
                cursor = scan.getCursor();
            } while (!cursor.equals("0"));
            
            // 提取 userId
            return users.stream()
                .map(key -> key.replace("chat:online:", ""))
                .collect(Collectors.toSet());
        }
    }
    
    /**
     * 关闭资源
     */
    public void shutdown() {
        running = false;
        subscribeExecutor.shutdown();
    }
}