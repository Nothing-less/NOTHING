package icu.nothingless.tools.cache;

import icu.nothingless.tools.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.params.SetParams;

import java.util.Random;

/**
 * Redis缓存操作助手
 * 封装了防击穿、防穿透、防雪崩的通用逻辑
 */
public class RedisCacheHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheHelper.class);
    private static final Random RANDOM = new Random();
    
    private RedisCacheHelper() {}
    
    // ==================== 基础Redis操作包装 ====================
    
    /**
     * 安全的Redis get操作
     */
    public static String safeGet(String key) {
        try {
            return RedisUtil.get(key);
        } catch (Exception e) {
            logger.error("Redis get FAILED，key={}", key, e);
            return null;
        }
    }
    
    /**
     * 安全的Redis del操作
     */
    public static void safeDel(String key) {
        try {
            RedisUtil.del(key);
        } catch (Exception e) {
            logger.error("Redis del FAILED，key={}", key, e);
        }
    }
    
    /**
     * 安全的Redis setex操作
     */
    public static void safeSetex(String key, int seconds, String value) {
        try {
            RedisUtil.setex(key, seconds, value);
        } catch (Exception e) {
            logger.error("Redis setex FAILED，key={}", key, e);
        }
    }
    
    // ==================== 分布式锁 ====================
    
    /**
     * 尝试获取分布式锁
     * @param lockKey 锁Key
     * @return 是否获取成功
     */
    public static boolean tryLock(String lockKey) {
        try {
            SetParams params = new SetParams()
                .nx()  // 不存在才设置
                .ex(RedisCacheConfig.LOCK_TTL_SECONDS);
            
            return RedisUtil.set(lockKey, RedisCacheConfig.LOCK_VALUE, params);
        } catch (Exception e) {
            logger.error("Get lock key FAILED，lockKey={}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 释放分布式锁
     */
    public static void releaseLock(String lockKey) {
        try {
            RedisUtil.del(lockKey);
        } catch (Exception e) {
            logger.warn("Release lockFAILED，lockKey={}", lockKey, e);
        }
    }
    
    // ==================== 缓存工具方法 ====================
    
    /**
     * 生成随机TTL（防雪崩）
     */
    public static int randomTtl() {
        return RedisCacheConfig.BASE_CACHE_TTL_SECONDS 
             + RANDOM.nextInt(RedisCacheConfig.TTL_RANDOM_RANGE_SECONDS);
    }
    
    /**
     * 缓存空值（防穿透）
     */
    public static void cacheEmpty(String cacheKey) {
        safeSetex(cacheKey, RedisCacheConfig.EMPTY_CACHE_TTL_SECONDS, 
                  RedisCacheConfig.EMPTY_PLACEHOLDER);
    }
    
    /**
     * 判断缓存值是否为空值占位符
     */
    public static boolean isEmptyPlaceholder(String cachedValue) {
        return RedisCacheConfig.EMPTY_PLACEHOLDER.equals(cachedValue);
    }
    
    /**
     * 使用Pipeline批量删除
     */
    public static void pipelineDelete(String... keys) {
        if (keys == null || keys.length == 0) return;
        
        try {
            RedisUtil.pipeline(pipeline -> {
                for (String key : keys) {
                    if (key != null) {
                        pipeline.del(key);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Pipeline delete FAILED", e);
        }
    }
    
    /**
     * 使用Pipeline批量设置
     */
    public static void pipelineSetex(String key1, String value1, 
                                      String key2, String value2, int ttl) {
        try {
            RedisUtil.pipeline(pipeline -> {
                pipeline.setex(key1, ttl, value1);
                if (key2 != null && value2 != null) {
                    pipeline.setex(key2, ttl, value2);
                }
            });
        } catch (Exception e) {
            logger.error("Pipeline setex FAILED", e);
        }
    }
    
    // ==================== 通用工具 ====================
    
    /**
     * 静默休眠
     */
    public static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 检查字符串是否为空
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}