package icu.nothingless.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.controller.ChatWebSocketServer;
import icu.nothingless.tools.ChatRedisBus;
import icu.nothingless.tools.DBPools.PDBPoolManager;
import icu.nothingless.tools.DBPools.RedisPoolManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class InfrastructureInitializer implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(InfrastructureInitializer.class);
    private static Boolean switch_flag = true;

    private ChatRedisBus redisBus;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            logger.info("Initializing Infrastructure...");
            // 初始化 PostgreSQL 连接池
            PDBPoolManager.init("PostrgeConfig.properties");
            logger.info("PostgreSQL connection pool initialized.");

            // 初始化 Redis 连接池
            RedisPoolManager.init("RedisConfig.properties");
            logger.info("Redis connection pool initialized.");

            initializeChatRedisBus(sce);
            logger.info("Infrastructure initialized successfully.");

        } catch (IOException e) {
            logger.error("Failed to initialize infrastructure: ", e.getMessage());
            switch_flag = false;
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (switch_flag) {
            logger.info("Shutting down Infrastructure...");
            if (redisBus != null) {
                redisBus.shutdown();
            }
            PDBPoolManager.close();
            RedisPoolManager.close();
            logger.info("Infrastructure shutdown complete.");
        }
    }

    private void initializeChatRedisBus(ServletContextEvent sce) {

        JedisPool jedisPool = RedisPoolManager.getJedisPool();

        // 初始化 Redis 消息总线
        String serverId = sce.getServletContext().getContextPath() + "-" + System.currentTimeMillis();
        redisBus = new ChatRedisBus(RedisPoolManager.getJedisPool(), serverId);

        // 注入到 WebSocket Server
        ChatWebSocketServer.setRedisBus(redisBus);

        // 存储到 ServletContext 供其他组件使用
        sce.getServletContext().setAttribute("chatRedisBus", redisBus);
        sce.getServletContext().setAttribute("jedisPool", jedisPool);

        logger.info("Chat service initialization completed，ServerId: " + serverId);
    }
}