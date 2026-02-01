package icu.nothingless.tools.DBTools;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.SetParams;

public class RedisUtil {

    // =================== String 操作 ===================

    public static void set(String key, String value) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            jedis.set(key, value);
        }
    }

    public static void setex(String key, int seconds, String value) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            jedis.setex(key, seconds, value);
        }
    }

    /**
     * 带条件的设置（NX-不存在才设置/XX-存在才设置）
     */
    public static boolean set(String key, String value, SetParams params) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            String result = jedis.set(key, value, params);
            return "OK".equals(result);
        }
    }

    public static String get(String key) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.get(key);
        }
    }

    public static boolean exists(String key) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.exists(key);
        }
    }

    public static void del(String... keys) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            jedis.del(keys);
        }
    }

    public static Long incr(String key) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.incr(key);
        }
    }

    public static Long expire(String key, int seconds) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.expire(key, seconds);
        }
    }

    // =================== Hash 操作 ===================

    public static void hset(String key, String field, String value) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            jedis.hset(key, field, value);
        }
    }

    public static String hget(String key, String field) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.hget(key, field);
        }
    }

    public static Map<String, String> hgetAll(String key) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.hgetAll(key);
        }
    }

    public static void hdel(String key, String... fields) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            jedis.hdel(key, fields);
        }
    }

    // =================== List 操作 ===================

    public static void lpush(String key, String... values) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            jedis.lpush(key, values);
        }
    }

    public static String rpop(String key) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.rpop(key);
        }
    }

    public static List<String> lrange(String key, long start, long end) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.lrange(key, start, end);
        }
    }

    public static Long llen(String key) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.llen(key);
        }
    }

    // =================== Set 操作 ===================

    public static void sadd(String key, String... members) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            jedis.sadd(key, members);
        }
    }

    public static Set<String> smembers(String key) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.smembers(key);
        }
    }

    public static boolean sismember(String key, String member) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return jedis.sismember(key, member);
        }
    }

    // =================== 分布式锁 ===================

    /**
     * 尝试获取分布式锁
     * 
     * @param lockKey       锁标识
     * @param requestId     请求标识（用于释放锁时验证）
     * @param expireSeconds 锁过期时间（防止死锁）
     * @return 是否获取成功
     */
    public static boolean tryLock(String lockKey, String requestId, int expireSeconds) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            SetParams params = new SetParams();
            params.nx(); // 仅当key不存在时才设置
            params.ex(expireSeconds); // 设置过期时间
            String result = jedis.set(lockKey, requestId, params);
            return "OK".equals(result);
        }
    }

    /**
     * 释放分布式锁（使用Lua脚本保证原子性）
     */
    public static boolean releaseLock(String lockKey, String requestId) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, 1, lockKey, requestId);
            return Long.valueOf(1).equals(result);
        }
    }

    // =================== 高级操作 ===================

    /**
     * 批量操作（Pipeline 提升性能，减少网络往返）
     * 使用示例：
     * RedisUtil.pipeline(pipeline -> {
     * pipeline.set("key1", "value1");
     * pipeline.set("key2", "value2");
     * pipeline.incr("counter");
     * });
     */
    public static void pipeline(Consumer<Pipeline> pipelineConsumer) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            Pipeline pipeline = jedis.pipelined();
            pipelineConsumer.accept(pipeline);
            pipeline.sync(); // 执行所有命令
        }
    }

    /**
     * 执行自定义命令（使用 Function 接口）
     * 使用示例：
     * String result = RedisUtil.execute(jedis -> jedis.get("key"));
     * 
     * @param function 接收 Jedis 实例，返回结果
     * @return 操作结果
     */
    public static <T> T execute(Function<Jedis, T> function) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            return function.apply(jedis);
        }
    }

    /**
     * 执行无返回值的自定义命令
     */
    public static void execute(Consumer<Jedis> consumer) {
        try (Jedis jedis = RedisPoolManager.getJedis()) {
            consumer.accept(jedis);
        }
    }

    // =================== 监控和工具 ===================

    /**
     * 获取连接池信息
     */
    public static String getPoolInfo() {
        JedisPool pool = getPool();
        if (pool != null) {
            return String.format("Active: %d, Idle: %d, Waiters: %d",
                    pool.getNumActive(),
                    pool.getNumIdle(),
                    pool.getNumWaiters());
        }
        return "Pool not initialized";
    }

    /**
     * 获取原始连接池（用于高级监控）
     */
    public static JedisPool getPool() {
        try {
            java.lang.reflect.Field field = RedisPoolManager.class.getDeclaredField("jedisPool");
            field.setAccessible(true);
            return (JedisPool) field.get(null);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            return null;
        }
    }
}