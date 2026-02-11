package icu.nothingless.pojo.core;

import icu.nothingless.pojo.adapter.iSTAdapter2;
import icu.nothingless.pojo.factory.iAdapterFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 框架中心注册表 - 统一管理所有适配器和工厂
 */
public class FrameworkRegistry {
    
    private static final FrameworkRegistry INSTANCE = new FrameworkRegistry();
    
    // 存储接口类 -> 工厂的映射
    private final Map<Class<? extends iSTAdapter2>, iAdapterFactory> factories = new ConcurrentHashMap<>();
    
    // 存储接口类 -> 表名的映射
    private final Map<Class<? extends iSTAdapter2>, String> tableNames = new ConcurrentHashMap<>();
    
    // 存储字段映射（Java字段名 -> 数据库列名）
    private final Map<Class<? extends iSTAdapter2>, Map<String, String>> columnMappings = new ConcurrentHashMap<>();
    
    private FrameworkRegistry() {}
    
    public static FrameworkRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册工厂
     */
    public <T extends iSTAdapter2<T>> void register(Class<T> adapterClass, iAdapterFactory<T> factory) {
        factories.put(adapterClass, factory);
    }
    
    /**
     * 注册表名
     */
    public <T extends iSTAdapter2<T>> void registerTableName(Class<T> adapterClass, String tableName) {
        tableNames.put(adapterClass, tableName);
    }
    
    /**
     * 注册字段映射
     */
    public <T extends iSTAdapter2<T>> void registerColumnMapping(Class<T> adapterClass, 
                                                                 Map<String, String> mapping) {
        columnMappings.put(adapterClass, mapping);
    }
    
    /**
     * 获取工厂
     */
    public <T extends iSTAdapter2<T>> iAdapterFactory<T> getFactory(Class<T> adapterClass) {
        iAdapterFactory<T> factory = (iAdapterFactory<T>) factories.get(adapterClass);
        if (factory == null) {
            throw new IllegalStateException("No factory registered for " + adapterClass.getName());
        }
        return factory;
    }
    
    /**
     * 创建适配器实例（核心方法）
     */
    public <T extends iSTAdapter2<T>> T createAdapter(Class<T> adapterClass) {
        iAdapterFactory<T> factory = getFactory(adapterClass);
        if (factory == null) {
            throw new IllegalStateException("No factory registered for " + adapterClass.getName());
        }
        return factory.createAdapter();
    }
    
    /**
     * 获取表名
     */
    public <T extends iSTAdapter2<T>> String getTableName(Class<T> adapterClass) {
        return tableNames.get(adapterClass);
    }
    
    /**
     * 获取字段映射
     */
    public <T extends iSTAdapter2<T>> Map<String, String> getColumnMapping(Class<T> adapterClass) {
        Map<String, String> mapping = columnMappings.get(adapterClass);
        return mapping != null ? mapping : new ConcurrentHashMap<>();
    }
    
    /**
     * 检查是否已注册
     */
    public boolean isRegistered(Class<? extends iSTAdapter2> adapterClass) {
        return factories.containsKey(adapterClass);
    }
    
    /**
     * 获取所有已注册的适配器类型
     */
    public Map<Class<? extends iSTAdapter2>, iAdapterFactory> getAllFactories() {
        return new ConcurrentHashMap<>(factories);
    }
}