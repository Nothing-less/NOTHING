package icu.nothingless.tools.cache;

/**
 * Redis缓存通用配置
 */
public final class RedisCacheConfig {
    
    private RedisCacheConfig() {}
    
    // ==================== TTL 配置 ====================
    
    /** 基础缓存时间：30分钟 */
    public static final int BASE_CACHE_TTL_SECONDS = 1800;
    
    /** 随机偏移范围：5分钟（用于防雪崩） */
    public static final int TTL_RANDOM_RANGE_SECONDS = 300;
    
    /** 空值缓存时间：60秒（防穿透） */
    public static final int EMPTY_CACHE_TTL_SECONDS = 60;
    
    /** 互斥锁时间：10秒（防击穿） */
    public static final int LOCK_TTL_SECONDS = 10;
    
    // ==================== 通用占位符 ====================
    
    /** 空值占位符 */
    public static final String EMPTY_PLACEHOLDER = "__EMPTY__";
    
    // ==================== 锁相关 ====================
    
    /** 锁的值（任意字符串即可） */
    public static final String LOCK_VALUE = "1";
    
    /** 获取锁失败后的等待时间（毫秒） */
    public static final long LOCK_WAIT_MILLIS = 100;
}