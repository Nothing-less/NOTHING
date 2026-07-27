package icu.nothingless.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.controller.config.GlobalConfig;
import icu.nothingless.controller.server.ChatWebSocketServer;
import icu.nothingless.service.impl.UserServiceImpl;
import icu.nothingless.service.interfaces.IUserService;
import icu.nothingless.tools.ChatJedisUtil;
import icu.nothingless.tools.ChatRedisBus;
import icu.nothingless.tools.DBPools.PDBPoolManager;
import icu.nothingless.tools.DBPools.RedisPoolManager;
import icu.nothingless.tools.ServiceFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import redis.clients.jedis.JedisPool;

public class InfrastructureInitializer implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(InfrastructureInitializer.class);
    private static Boolean switch_flag = true;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            logger.info("Initializing Infrastructure...");

            GlobalConfig.CONFIG_MAP.size();
            // GlobalConfig.CONFIG_MAP.forEach(
            // (k, v) -> logger.error(Fmt.of("({})--({})",k, v))
            // );

            // 初始化 PostgreSQL 连接池
            PDBPoolManager.init("PostrgeConfig.properties");
            logger.info("PostgreSQL connection pool initialized.");

            // 初始化 Redis 连接池
            RedisPoolManager.init("RedisConfig.properties");
            logger.info("Redis connection pool initialized.");

            initializeChatRedisBus(sce);
            initChatJedisUtil(sce);
            logger.info("Infrastructure initialized successfully.");

        } catch (IOException e) {
            logger.error("Failed to initialize infrastructure: ", e.getMessage());
            switch_flag = false;
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (!switch_flag)
            return;

        logger.info("Shutting down Infrastructure...");
        try {
            IUserService userService = ServiceFactory.createInstance(IUserService.class, "userServiceImpl");
            if (userService instanceof UserServiceImpl userServiceImpl) {
                userServiceImpl.doLogoutForAll();
                logger.info("All users logged out.");
            }
        } catch (Exception e) {
            logger.error("Error logging out all users: ", e);
        }

        // 先关闭 ChatRedisBus（需要在线程池关闭前取消订阅）
        try {
            ChatRedisBus redisBus = (ChatRedisBus) sce.getServletContext().getAttribute("chatRedisBus");
            if (redisBus != null) {
                redisBus.shutdown();
                logger.info("ChatRedisBus shutdown.");
            }
        } catch (Exception e) {
            logger.error("Error shutting down ChatRedisBus: ", e);
        }
        // 2. 关闭 WebSocket
        try {
            ChatWebSocketServer.shutdown();
            logger.info("WebSocket server shutdown.");
        } catch (Exception e) {
            logger.error("Error shutting down WebSocket server: ", e);
        }
        // 3. 关闭连接池
        try {
            PDBPoolManager.close();
            logger.info("PostgreSQL pool closed.");
        } catch (Exception e) {
            logger.error("Error closing PostgreSQL pool: ", e);
        }
        try {
            RedisPoolManager.close();
            logger.info("Redis pool closed.");
        } catch (Exception e) {
            logger.error("Error closing Redis pool: ", e);
        }

        // ★ 新增：強制註銷由當前 WebApp 加載的 JDBC 驅動
        try {
            java.util.Enumeration<java.sql.Driver> drivers = java.sql.DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                java.sql.Driver driver = drivers.nextElement();
                if (driver.getClass().getClassLoader() == Thread.currentThread().getContextClassLoader()) {
                    java.sql.DriverManager.deregisterDriver(driver);
                    logger.info("JDBC driver deregistered: {}", driver.getClass().getName());
                }
            }
        } catch (Exception e) {
            logger.error("Error deregistering JDBC drivers: ", e);
        }

        logger.info("Infrastructure shutdown complete.");
    }

    private void initializeChatRedisBus(ServletContextEvent sce) {

        JedisPool jedisPool = RedisPoolManager.getJedisPool();

        // 初始化 Redis 消息总线
        String serverId = sce.getServletContext().getContextPath() + "-" + System.currentTimeMillis();
        ChatRedisBus redisBus = new ChatRedisBus(RedisPoolManager.getJedisPool(), serverId);

        // 注入到 WebSocket Server
        ChatWebSocketServer.setRedisBus(redisBus);

        // 存储到 ServletContext 供其他组件使用
        sce.getServletContext().setAttribute("chatRedisBus", redisBus);
        sce.getServletContext().setAttribute("jedisPool", jedisPool);

        logger.info("Chat service initialization completed，ServerId: " + serverId);
    }

    private void initChatJedisUtil(ServletContextEvent sce) {
        JedisPool jedisPool = (JedisPool) sce.getServletContext().getAttribute("jedisPool");
        ChatJedisUtil.init(jedisPool);
    }
}