package icu.nothingless.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.tools.DBPools.PDBPoolManager;
import icu.nothingless.tools.DBPools.RedisPoolManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * 基础设施初始化监听器
 * 在应用启动时初始化数据库和缓存连接池
 */
public class InfrastructureInitializer implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(InfrastructureInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        logger.info("Initializing Infrastructure...");

        try {
            // 初始化 PostgreSQL 连接池
            PDBPoolManager.init("PostrgeConfig.properties");
            logger.info("PostgreSQL connection pool initialized.");

            // 初始化 Redis 连接池
            RedisPoolManager.init("RedisConfig.properties");
            logger.info("Redis connection pool initialized.");

            logger.info("Infrastructure initialized successfully.");

        } catch (IOException e) {
            logger.error("Failed to initialize infrastructure: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.error("Shutting down Infrastructure...");
        PDBPoolManager.close();
        RedisPoolManager.close();
        logger.error("Infrastructure shutdown complete.");
    }
}