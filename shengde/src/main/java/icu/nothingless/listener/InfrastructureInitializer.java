package icu.nothingless.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.DBPools.PDBPoolManager;
import icu.nothingless.tools.DBPools.RedisPoolManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class InfrastructureInitializer implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(InfrastructureInitializer.class);
    private static Boolean switch_flag = true;

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

            logger.info("Infrastructure initialized successfully.");

        } catch (IOException e) {
            logger.error("Failed to initialize infrastructure: ", e.getMessage());
            switch_flag = false;
        }

        /*   
        try {
            logger.info("Initializing ServiceFactory...");
            // 扫描核心业务包
            ServiceFactory.scanPackage("icu.nothingless.dao");
            ServiceFactory.scanPackage("icu.nothingless.service");
            // 将 ServiceFactory 放入上下文，方便 JSP 中通过 EL 表达式访问
            sce.getServletContext().setAttribute("serviceFactory", ServiceFactory.class);
            logger.info("ServiceFactory initialization completed! Scanned {}implementation classes.",ServiceFactory.getAllScannedClasses());
        }catch (Exception e) {
            logger.error("Failed to initialize ServiceFactory: ", e.getMessage());
        }
        */

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if(switch_flag){
            logger.info("Shutting down Infrastructure...");
            PDBPoolManager.close();
            RedisPoolManager.close();
            logger.info("Infrastructure shutdown complete.");
            //////////////////////////////////////////////////////
            logger.info("Starting Clean ServiceFactory...");
            ServiceFactory.clearAllSingletons();
            logger.info("ServiceFactory cleaned completely.");
        }
    }
}