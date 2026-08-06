package icu.nothingless.tools;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.controller.server.ChatWebSocketServer;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.service.interfaces.IUserService;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

/**
 * Redis 消息总线
 * 功能：发布订阅、消息队列、在线状态、心跳检测
 */
public class ChatRedisBus {
    private static final Logger logger = LoggerFactory.getLogger(ChatRedisBus.class);

    // Redis Key 常量
    private static final String KEY_USER_ONLINE = "chat:online:%s";
    private static final String KEY_USER_MSG_QUEUE = "chat:queue:%s";
    private static final String KEY_USER_CHANNEL = "chat:channel:%s";
    private static final String KEY_GLOBAL_CHANNEL = "chat:global";
    private static final String KEY_HEARTBEAT_ZSET = "chat:heartbeat";

    // 配置参数
    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;
    private static final int MSG_QUEUE_EXPIRE_SECONDS = 7 * 24 * 3600;
    private static final int MAX_OFFLINE_MSG = 100;

    private final JedisPool jedisPool;
    private final String serverId;
    private final ExecutorService subscribeExecutor;
    private volatile boolean running = true;

    // 保存 subscribe 佔用的 Jedis 連接，用於強制斷開
    private final ConcurrentHashMap<String, Jedis> activeJedisConnections = new ConcurrentHashMap<>();

    // ========== 维护线程池和订阅引用，用于关闭 ==========
    private ScheduledExecutorService heartbeatScheduler;
    private final ConcurrentHashMap<String, JedisPubSub> activePubSubs = new ConcurrentHashMap<>();
    private JedisPubSub globalPubSub;
    // =======================================================

    private final ConcurrentHashMap<String, Consumer<String>> localSubscribers = new ConcurrentHashMap<>();

    public ChatRedisBus(JedisPool jedisPool, String serverId) {
        this.jedisPool = jedisPool;
        this.serverId = serverId;
        this.subscribeExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "redis-subscribe-" + r.hashCode());
            t.setDaemon(true);
            return t;
        });

        startHeartbeatChecker();
    }

    // ==================== 用户上线/下线 ====================

    public void userOnline(String userId, Consumer<String> messageHandler) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = String.format(KEY_USER_ONLINE, userId);
            long now = System.currentTimeMillis();

            jedis.hset(key, "serverId", serverId);
            jedis.hset(key, "connectTime", String.valueOf(now));
            jedis.hset(key, "lastHeartbeat", String.valueOf(now));
            jedis.expire(key, HEARTBEAT_TIMEOUT_SECONDS);

            localSubscribers.put(userId, messageHandler);
            subscribeUserChannel(userId);
            deliverOfflineMessages(userId, messageHandler);
        }
    }

    public void userOffline(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(String.format(KEY_USER_ONLINE, userId));
            localSubscribers.remove(userId);
        }
        // 关键：取消该用户的 Redis 订阅
        unsubscribeUserChannel(userId);
    }

    // ==================== 心跳检测 ====================

    public void heartbeat(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = String.format(KEY_USER_ONLINE, userId);
            long now = System.currentTimeMillis();

            jedis.hset(key, "lastHeartbeat", String.valueOf(now));
            jedis.expire(key, HEARTBEAT_TIMEOUT_SECONDS);
            jedis.zadd(KEY_HEARTBEAT_ZSET, now, userId);
        }
    }

    public boolean isOnline(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(String.format(KEY_USER_ONLINE, userId));
        }
    }

    public Map<String, String> getOnlineStatus(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(String.format(KEY_USER_ONLINE, userId));
        }
    }

    /**
     * 启动心跳检测线程
     */
    private void startHeartbeatChecker() {
        // 保存引用，以便关闭
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-checker");
            t.setDaemon(true);
            return t;
        });

        heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (!running)
                return; // 快速退出检查

            try (Jedis jedis = jedisPool.getResource()) {
                long timeout = System.currentTimeMillis() - (HEARTBEAT_TIMEOUT_SECONDS * 1000);
                List<String> timeoutUsers = jedis.zrangeByScore(KEY_HEARTBEAT_ZSET, 0, timeout);

                for (String userId : timeoutUsers) {
                    String key = String.format(KEY_USER_ONLINE, userId);
                    String lastHeartbeat = jedis.hget(key, "lastHeartbeat");

                    if (lastHeartbeat == null ||
                            Long.parseLong(lastHeartbeat) < timeout) {

                        jedis.del(key);
                        jedis.zrem(KEY_HEARTBEAT_ZSET, userId);
                        // logger.info("用户离线检测: {}", userId);
                        onUserTimeout(userId);
                    }
                }
            } catch (Exception e) {
                if (running) {
                    logger.error("Heartbeat checker failed: {}", e.getMessage(), e);
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    private void onUserTimeout(String userId) {
        logger.info("User timeout detected by Redis: [{}]", userId);

        // 清理 Redis 在线
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(String.format(KEY_USER_ONLINE, userId));
            jedis.zrem(KEY_HEARTBEAT_ZSET, userId);
        }

        // 如果用户连接在当前服务器，强制关闭 WebSocket
        Session sess = ChatWebSocketServer.getSessions().get(userId);
        if (sess != null && sess.isOpen()) {
            try {
                sess.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Redis timeout"));
            } catch (IOException e) {
                logger.error("Close timeout session failed [{}]: {}", userId, e.getMessage());
            }
            ChatWebSocketServer.getSessions().remove(userId);
        }

        // 同步 JedisUtil 状态
        try {
            ChatJedisUtil.setUserOffline(Long.valueOf(userId), 0);
            IUserService<User> userService = (IUserService<User>) ServiceFactory
                    .getSingleton(IUserService.class);
            userService.doLogout(User.builder().userId(userId).build());
        } catch (Exception e) {
            logger.warn("Set offline failed: {}", e.getMessage());
        }
    }

    // ==================== 消息发布订阅 ====================
    public void sendMessage(String toUserId, String messageJson) {
        // 方案：优先本地推送，本地不在线则走 Redis
        Consumer<String> localHandler = localSubscribers.get(toUserId);
        boolean localSuccess = false;

        if (localHandler != null) {
            try {
                localHandler.accept(messageJson);
                localSuccess = true;
                logger.debug("本地推送成功: {}", toUserId);
            } catch (Exception e) {
                logger.warn("本地推送失败: {}", toUserId);
            }
        }

        // 如果本地推送失败，走 Redis（可能是其他服务器或已离线）
        // if (!localSuccess) {
        // try (Jedis jedis = jedisPool.getResource()) {
        // String channel = String.format(KEY_USER_CHANNEL, toUserId);
        // jedis.publish(channel, messageJson);
        // }
        // }
        if (!localSuccess) {
            // 检查用户是否在线（任何服务器）
            if (!isOnline(toUserId)) {
                // 用户离线，存储离线消息
                saveOfflineMessage(toUserId, messageJson);
                logger.debug("用户离线，消息已存入离线队列: {}", toUserId);
            } else {
                // 用户在线但在其他服务器，走 Redis Publish
                try (Jedis jedis = jedisPool.getResource()) {
                    String channel = String.format(KEY_USER_CHANNEL, toUserId);
                    jedis.publish(channel, messageJson);
                }
            }
        }
    }

    private void saveOfflineMessage(String userId, String messageJson) {
        try (Jedis jedis = jedisPool.getResource()) {
            String queueKey = String.format(KEY_USER_MSG_QUEUE, userId);
            Pipeline pipe = jedis.pipelined();
            pipe.lpush(queueKey, messageJson);
            pipe.ltrim(queueKey, 0, MAX_OFFLINE_MSG - 1);
            pipe.expire(queueKey, MSG_QUEUE_EXPIRE_SECONDS);
            pipe.sync();
        }
    }

    private void deliverOfflineMessages(String userId, Consumer<String> handler) {
        try (Jedis jedis = jedisPool.getResource()) {
            String queueKey = String.format(KEY_USER_MSG_QUEUE, userId);
            List<String> messages = jedis.lrange(queueKey, 0, -1);
            if (messages.isEmpty())
                return;

            for (String msg : messages) {
                handler.accept(msg);
            }
            jedis.del(queueKey);
        }
    }

    /**
     * 订阅用户私有频道
     */
    private void subscribeUserChannel(String userId) {
        subscribeExecutor.submit(() -> {
            Jedis jedis = null; // ★ 改為手動管理
            try {
                jedis = jedisPool.getResource();
                String channel = String.format(KEY_USER_CHANNEL, userId);

                JedisPubSub pubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        Consumer<String> handler = localSubscribers.get(userId);
                        if (handler != null) {
                            handler.accept(message);
                        }
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        logger.info("订阅频道: {}", channel);
                    }

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        logger.info("取消订阅: {}", channel);
                        activePubSubs.remove(userId);
                    }
                };

                activePubSubs.put(userId, pubSub);
                activeJedisConnections.put(userId, jedis); // ★ 保存連接引用

                jedis.subscribe(pubSub, channel);

            } catch (Exception e) {
                if (running) {
                    logger.error("订阅异常: {}", e.getMessage(), e);
                }
            } finally {
                activePubSubs.remove(userId);
                activeJedisConnections.remove(userId);
                if (jedis != null) {
                    jedis.close(); // 確保歸還/關閉
                }
            }
        });
    }

    /**
     * 取消订阅用户频道
     */
    public void unsubscribeUserChannel(String userId) {
        JedisPubSub pubSub = activePubSubs.remove(userId);
        if (pubSub != null && pubSub.isSubscribed()) {
            pubSub.unsubscribe(); // 取消订阅，阻塞线程会退出
            logger.info("取消订阅用户频道: {}", userId);
        }
    }

    // ==================== 广播与群组 ====================

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
                globalPubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        handler.accept(message);
                    }
                };
                jedis.subscribe(globalPubSub, KEY_GLOBAL_CHANNEL);
            } catch (Exception e) {
                if (running) {
                    logger.error("全局订阅异常: {}", e.getMessage(), e);
                }
            }
        });
    }

    // ==================== 工具方法 ====================

    public Set<String> getOnlineUsers() {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> users = new HashSet<>();
            String pattern = KEY_USER_ONLINE.replace("%s", "*");

            String cursor = "0";
            do {
                ScanResult<String> scan = jedis.scan(cursor, new ScanParams().match(pattern).count(100));
                users.addAll(scan.getResult());
                cursor = scan.getCursor();
            } while (!cursor.equals("0"));

            return users.stream()
                    .map(key -> key.replace("chat:online:", ""))
                    .collect(Collectors.toSet());
        }
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        logger.info("ChatRedisBus 正在关闭...");
        running = false;

        // 取消所有 Redis 订阅
        for (Map.Entry<String, JedisPubSub> entry : activePubSubs.entrySet()) {
            try {
                JedisPubSub pubSub = entry.getValue();
                if (pubSub != null && pubSub.isSubscribed()) {
                    pubSub.unsubscribe();
                }
            } catch (Exception e) {
                logger.warn("取消订阅失败 [{}]: {}", entry.getKey(), e.getMessage());
            }
        }

        // ★ 強制關閉 subscribe 佔用的 Jedis 連接（中斷 socket 阻塞）
        for (Map.Entry<String, Jedis> entry : activeJedisConnections.entrySet()) {
            try {
                Jedis jedis = entry.getValue();
                if (jedis != null) {
                    jedis.close(); // 強制斷開 socket，讓阻塞線程拋異常退出
                }
            } catch (Exception e) {
                logger.warn("强制关闭 Jedis 连接失败 [{}]: {}", entry.getKey(), e.getMessage());
            }
        }
        activeJedisConnections.clear();
        activePubSubs.clear();

        // 取消全局订阅
        if (globalPubSub != null && globalPubSub.isSubscribed()) {
            globalPubSub.unsubscribe();
        }

        // 关闭心跳检测线程池
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdownNow();
            try {
                heartbeatScheduler.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 关闭订阅执行器
        if (subscribeExecutor != null && !subscribeExecutor.isShutdown()) {
            subscribeExecutor.shutdownNow();
            try {
                subscribeExecutor.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logger.info("ChatRedisBus 已关闭");
    }
}