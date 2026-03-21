package icu.nothingless.tools.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ServiceFactoryConfig {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFactoryConfig.class);
    
    private static final String DEFAULT_CONFIG = "service-factory.yml";
    private static final String EXTERNAL_CONFIG = "config/service-factory.yml";
    
    private List<String> scanPackages = new ArrayList<>();
    private List<String> excludePackages = new ArrayList<>();
    private String scanMode = "lazy";
    private boolean autoScan = true;
    
    // 单例配置实例
    private static volatile ServiceFactoryConfig INSTANCE;
    private static final Object LOCK = new Object();
    
    public static ServiceFactoryConfig getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = load();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * 加载配置（优先级：外部文件 > classpath）
     */
    private static ServiceFactoryConfig load() {
        // 优先级 1：外部配置文件（jar 同级目录的 config/）
        Path externalPath = Paths.get(EXTERNAL_CONFIG);
        if (Files.exists(externalPath)) {
            try (InputStream is = Files.newInputStream(externalPath)) {
                return parseYaml(is);
            } catch (IOException e) {
                logger.error("Failed to load external config: ", e);
            }
        }
        
        // 优先级 2：classpath 根目录
        InputStream classpathStream = ServiceFactoryConfig.class
            .getClassLoader().getResourceAsStream(DEFAULT_CONFIG);
        if (classpathStream != null) {
            try (InputStream is = classpathStream) {
                return parseYaml(is);
            } catch (IOException e) {
                logger.error("Failed to load classpath config: ", e);
            }
        }
        
        // 优先级 3：使用默认配置（空列表，依赖手动扫描）
        System.out.println("No service-factory.yml found, using default empty config");
        return new ServiceFactoryConfig();
    }
    
    private static ServiceFactoryConfig parseYaml(InputStream is) {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(is);
        if (data == null) return new ServiceFactoryConfig();
        
        Map<String, Object> sfConfig = (Map<String, Object>) data.get("service-factory");
        if (sfConfig == null) return new ServiceFactoryConfig();
        
        ServiceFactoryConfig config = new ServiceFactoryConfig();
        
        // 解析扫描包
        List<String> packages = (List<String>) sfConfig.get("scan-packages");
        if (packages != null) config.scanPackages = packages;
        
        // 解析排除包
        List<String> excludes = (List<String>) sfConfig.get("exclude-packages");
        if (excludes != null) config.excludePackages = excludes;
        
        // 解析扫描模式
        String mode = (String) sfConfig.get("scan-mode");
        if (mode != null) config.scanMode = mode;
        
        // 解析自动扫描
        Boolean auto = (Boolean) sfConfig.get("auto-scan");
        if (auto != null) config.autoScan = auto;
        
        System.out.println("ServiceFactory config loaded: " + config.scanPackages);
        return config;
    }
    
    // Getters
    public List<String> getScanPackages() { return Collections.unmodifiableList(scanPackages); }
    public List<String> getExcludePackages() { return Collections.unmodifiableList(excludePackages); }
    public String getScanMode() { return scanMode; }
    public boolean isAutoScan() { return autoScan; }
    
    public boolean hasPackages() { return !scanPackages.isEmpty(); }
}