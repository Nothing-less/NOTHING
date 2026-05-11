package icu.nothingless.tools;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    // ========== 新增：维护线程池和订阅引用，用于关闭 ==========
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

    // ==================== 1. 用户上线/下线 ====================

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

    // ==================== 2. 心跳检测 ====================

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
        // 清理相关资源
    }

    // ==================== 3. 消息发布订阅 ====================

    public void sendMessage(String toUserId, String messageJson) {
        Consumer<String> localHandler = localSubscribers.get(toUserId);
        if (localHandler != null) {
            try {
                localHandler.accept(messageJson);
                return;
            } catch (Exception e) {
                // 本地推送失败，继续走 Redis
            }
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String channel = String.format(KEY_USER_CHANNEL, toUserId);
            if (isOnline(toUserId)) {
                jedis.publish(channel, messageJson);
            } else {
                saveOfflineMessage(toUserId, messageJson);
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
            try (Jedis jedis = jedisPool.getResource()) {
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
                        activePubSubs.remove(userId); // 清理引用
                    }
                };

                // 保存引用，用于后续取消订阅
                activePubSubs.put(userId, pubSub);

                // 阻塞订阅，直到调用 unsubscribe()
                jedis.subscribe(pubSub, channel);

            } catch (Exception e) {
                if (running) {
                    logger.error("订阅异常: {}", e.getMessage(), e);
                }
            } finally {
                activePubSubs.remove(userId);
            }
        });
    }

    /**
     * 取消订阅用户频道 — 关键修复
     */
    public void unsubscribeUserChannel(String userId) {
        JedisPubSub pubSub = activePubSubs.remove(userId);
        if (pubSub != null && pubSub.isSubscribed()) {
            pubSub.unsubscribe(); // 取消订阅，阻塞线程会退出
            logger.info("取消订阅用户频道: {}", userId);
        }
    }

    // ==================== 4. 广播与群组 ====================

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

    // ==================== 5. 工具方法 ====================

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
     * 关闭资源 — 关键修复
     */
    public void shutdown() {
        logger.info("ChatRedisBus 正在关闭...");
        running = false;

        // 1. 取消所有 Redis 订阅（让阻塞线程退出）
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
        activePubSubs.clear();

        // 取消全局订阅
        if (globalPubSub != null && globalPubSub.isSubscribed()) {
            globalPubSub.unsubscribe();
        }

        // 2. 关闭心跳检测线程池
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdownNow();
            try {
                if (!heartbeatScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    heartbeatScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                heartbeatScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 3. 关闭订阅执行器
        if (subscribeExecutor != null && !subscribeExecutor.isShutdown()) {
            subscribeExecutor.shutdownNow();
            try {
                if (!subscribeExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    subscribeExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                subscribeExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("ChatRedisBus 已关闭");
    }
}