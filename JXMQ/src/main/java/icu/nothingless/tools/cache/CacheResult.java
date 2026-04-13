package icu.nothingless.tools.cache;

/**
 * 缓存查询结果包装类
 * @param <T> 数据类型
 */
public class CacheResult<T> {
    
    public enum Status {
        HIT,        // 缓存命中
        MISS,       // 缓存未命中
        EMPTY_HIT,  // 命中空值（防穿透）
        ERROR       // 发生错误
    }
    
    private final Status status;
    private final T data;
    private final String message;
    
    private CacheResult(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }
    
    // ========== 工厂方法 ==========
    
    public static <T> CacheResult<T> hit(T data) {
        return new CacheResult<>(Status.HIT, data, null);
    }
    
    public static <T> CacheResult<T> miss() {
        return new CacheResult<>(Status.MISS, null, " Cache miss! ");
    }
    
    public static <T> CacheResult<T> emptyHit() {
        return new CacheResult<>(Status.EMPTY_HIT, null, "Cache hit no value! ");
    }
    
    public static <T> CacheResult<T> error(String message) {
        return new CacheResult<>(Status.ERROR, null, message);
    }
    
    // ========== Getter ==========
    
    public Status getStatus() { return status; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    
    public boolean isHit() { 
        return status == Status.HIT || status == Status.EMPTY_HIT; 
    }
    
    public boolean isMiss() { return status == Status.MISS; }
    public boolean isEmptyHit() { return status == Status.EMPTY_HIT; }
}