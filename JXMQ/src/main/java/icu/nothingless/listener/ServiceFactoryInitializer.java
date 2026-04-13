package icu.nothingless.listener;

import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.config.ServiceFactoryConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class ServiceFactoryInitializer implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceFactoryInitializer.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("=== Starting ServiceFactory Initialize ===");
        
        ServiceFactoryConfig config = ServiceFactoryConfig.getInstance();
        
        // 检查配置
        if (!config.hasPackages()) {
            logger.warn("The scan package path is not configured, please set scan-packages in service-factory.yml");
            return;
        }
        
        logger.info("Scanning Mode: {}", config.getScanMode());
        logger.info("Scanning Packages: {}", config.getScanPackages());
        
        // 饥饿加载：立即触发扫描
        if ("eager".equals(config.getScanMode())) {
            try {
                ServiceFactory.eagerScan();
                logger.info("Scanning Completed, A total of {} Implementation Classes of Interfaces are Found.", 
                    ServiceFactory.getAllScannedClasses().size());
            } catch (Exception e) {
                logger.error("ServiceFactory scan failed!", e);
                // throw new RuntimeException("ServiceFactory Initialize failed", e);
            }
        }
         
        logger.info("=== ServiceFactory Initialize Completed ===");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServiceFactory.clearAllSingletons();
        logger.info("=== ServiceFactory destroyed ===");
    }
}