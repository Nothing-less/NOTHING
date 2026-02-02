package icu.nothingless.tools.DBPools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPoolManager {

    private static volatile JedisPool jedisPool;
    private static final Object lock = new Object();

    public static void init(String configPath) throws IOException {
        if (jedisPool != null) {
            return;
        }

        synchronized (lock) {
            if (jedisPool != null) {
                return;
            }

            Properties props = new Properties();
            try (InputStream is = RedisPoolManager.class.getClassLoader()
                    .getResourceAsStream(configPath)) {
                props.load(is);
            }

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(Integer.parseInt(
                    props.getProperty("redis.pool.maxtotal", "50")));
            config.setMaxIdle(Integer.parseInt(
                    props.getProperty("redis.pool.maxidle", "10")));
            config.setMinIdle(5);

            String host = props.getProperty("redis.host", "localhost");
            int port = Integer.parseInt(props.getProperty("redis.port", "6379"));
            String password = props.getProperty("redis.password");
            int database = Integer.parseInt(props.getProperty("redis.database", "0"));

            if (password != null && !password.isEmpty()) {
                jedisPool = new JedisPool(config, host, port, 3000, password, database);
            } else {
                jedisPool = new JedisPool(config, host, port, 3000, null, database);
            }

            // 测试连接
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
                System.out.println("Redis Pool initialized: " + host + ":" + port);
            }
        }
    }

    /**
     * 获取 Jedis 连接（用完必须关闭）
     */
    public static Jedis getJedis() {
        if (jedisPool == null) {
            throw new IllegalStateException("RedisPool not initialized");
        }
        return jedisPool.getResource();
    }

    /**
     * 关闭连接池
     */
    public static void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            System.out.println("Redis Pool closed.");
        }
    }
}