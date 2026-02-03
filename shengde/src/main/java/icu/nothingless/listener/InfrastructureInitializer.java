package icu.nothingless.listener;

import java.io.IOException;

import icu.nothingless.tools.PDBPoolManager;
import icu.nothingless.tools.RedisPoolManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * 基础设施初始化监听器
 * 在应用启动时初始化数据库和缓存连接池
 */
public class InfrastructureInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        System.out.println("Initializing Infrastructure...");

        try {
            // 初始化 PostgreSQL 连接池
            PDBPoolManager.init("PostrgeConfig.properties");

            // 初始化 Redis 连接池
            RedisPoolManager.init("RedisConfig.properties");

            System.out.println("Infrastructure initialized successfully.");

        } catch (IOException e) {
            System.out.println("Failed to initialize infrastructure: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Shutting down Infrastructure...");
        PDBPoolManager.close();
        RedisPoolManager.close();
        System.out.println("Infrastructure shutdown complete.");
    }
}