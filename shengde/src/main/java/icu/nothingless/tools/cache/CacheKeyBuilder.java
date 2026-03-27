package icu.nothingless.tools.cache;

/**
 * 缓存Key构建工具类
 */
public final class CacheKeyBuilder {
    
    private CacheKeyBuilder() {}
    
    /**
     * 构建Key
     * @param prefix 前缀
     * @param identifier 标识符
     * @return 完整Key
     */
    public static String build(String prefix, String identifier) {
        if (identifier == null) {
            return prefix + "null";
        }
        return prefix + identifier;
    }
    
    /**
     * 构建锁Key
     * @param prefix 锁前缀
     * @param identifier 标识符
     * @return 锁Key
     */
    public static String buildLockKey(String prefix, String identifier) {
        return build(prefix, identifier);
    }
}