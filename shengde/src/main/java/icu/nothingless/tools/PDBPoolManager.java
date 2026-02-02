package icu.nothingless.tools;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * PostgreSQL 连接池管理器
 * 使用 HikariCP 作为连接池实现
 */
public final class PDBPoolManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PDBPoolManager.class);

    // 懒加载 + 线程安全
    private static class Holder {
        static final HikariDataSource INSTANCE = initializePool();
    }

    // 配置记录类
    private record PoolConfig(
            String jdbcUrl,
            String username,
            String password,
            String driverClassName,
            int maxPoolSize,
            int minIdle,
            long connectionTimeout,
            long maxLifetime) {
    }

    private static volatile String configPath; // 初始化校验

    /**
     * 初始化连接池
     * 
     * @param path 配置文件路径
     * @throws IllegalStateException 如果配置加载失败或必填项缺失
     */
    public static void init(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Config path must not be empty");
        }

        // 简单的防止重复初始化不同配置的校验
        if (configPath != null && !configPath.equals(path)) {
            LOGGER.warn("Pool already initialized with config: {0}, ignoring: {1}",
                    configPath, path);
            return;
        }

        configPath = path;

        // 触发 Holder 初始化
        var ds = Holder.INSTANCE;
        LOGGER.info("Database pool ready: {0}", ds.getPoolName());
    }

    /**
     * 获取数据库连接（使用完后务必 try-with-resources 关闭）
     */
    public static Connection getConnection() throws SQLException {
        if (Holder.INSTANCE.isClosed()) {
            throw new SQLException("Connection pool has been closed");
        }
        return Holder.INSTANCE.getConnection();
    }

    /**
     * 关闭连接池
     */
    public static void close() {
        if (!Holder.INSTANCE.isClosed()) {
            Holder.INSTANCE.close();
            LOGGER.info("Database pool closed");
        }
    }

    // ============ 私有初始化逻辑 ============

    private static HikariDataSource initializePool() {
        try {
            var config = loadConfig();
            var hikariConfig = createHikariConfig(config);
            var dataSource = new HikariDataSource(hikariConfig);

            // 预热连接池（静默处理失败，不影响启动）
            warmUpPool(dataSource);

            return dataSource;

        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to initialize database pool: " + e.getMessage());
        }
    }

    private static PoolConfig loadConfig() throws IOException {
        var props = new Properties();
        var path = Objects.requireNonNull(configPath, "Config path not set, call init() first");

        try (InputStream is = PDBPoolManager.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Config file not found in classpath: " + path);
            }
            props.load(is);
        }

        // 函数式提取配置，带默认值和必填校验
        return new PoolConfig(
                requireProperty(props, "jdbc.url"),
                requireProperty(props, "jdbc.username"),
                requireProperty(props, "jdbc.password"),
                props.getProperty("jdbc.driver", "org.postgresql.Driver"),
                parseInt(props, "jdbc.pool.maxsize", 20),
                parseInt(props, "jdbc.pool.minsize", 5),
                parseLong(props, "jdbc.pool.timeout", 30000),
                parseLong(props, "jdbc.pool.maxlifetime", 1800000));
    }

    private static HikariConfig createHikariConfig(PoolConfig cfg) {
        var config = new HikariConfig();

        // 基础连接配置
        config.setJdbcUrl(cfg.jdbcUrl());
        config.setUsername(cfg.username());
        config.setPassword(cfg.password());
        config.setDriverClassName(cfg.driverClassName());

        // 连接池调优
        config.setMaximumPoolSize(cfg.maxPoolSize());
        config.setMinimumIdle(cfg.minIdle());
        config.setConnectionTimeout(cfg.connectionTimeout());
        config.setMaxLifetime(cfg.maxLifetime());

        // 连接检测（防止数据库关闭空闲连接导致的 8 小时问题）
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);

        // PostgreSQL 特有优化（使用 Map 批量设置，更整洁）
        Map.of(
                "cachePrepStmts", "true",
                "prepStmtCacheSize", "250",
                "prepStmtCacheSqlLimit", "2048",
                "useServerPrepStmts", "true").forEach(config::addDataSourceProperty);

        // 可选：设置池名称，便于监控
        config.setPoolName("PostgreSQL-Primary-Pool");

        return config;
    }

    private static void warmUpPool(HikariDataSource ds) {
        try (var conn = ds.getConnection();
                var stmt = conn.createStatement()) {

            // 实际执行一次查询确保连接有效
            stmt.execute("SELECT version()");

            LOGGER.info("PostgreSQL pool initialized | Pool size: {0}/{1} | URL: {2}",
                    ds.getMinimumIdle(), ds.getMaximumPoolSize(), ds.getJdbcUrl());

        } catch (SQLException e) {
            // 预热失败记录警告但不阻断，连接池本身会在首次使用时重试
            LOGGER.warn("Pool warm-up query failed", e);
        }
    }

    // ============ 工具方法 ============

    private static String requireProperty(Properties props, String key) {
        var value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing required configuration: " + key);
        }
        return value;
    }

    private static int parseInt(Properties props, String key, int defaultValue) {
        var value = props.getProperty(key);
        if (value == null)
            return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid integer value for " + key + ": " + value);
        }
    }

    private static long parseLong(Properties props, String key, long defaultValue) {
        var value = props.getProperty(key);
        if (value == null)
            return defaultValue;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid long value for " + key + ": " + value);
        }
    }

    // 防止实例化
    private PDBPoolManager() {
    }
}