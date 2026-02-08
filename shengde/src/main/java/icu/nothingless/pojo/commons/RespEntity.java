package icu.nothingless.pojo.commons;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应实体类
 * 作为所有 API 返回的标准载体
 */
public class RespEntity<T> {
    
    /* ==================== 预定义状态码 ==================== */
    
    // 成功 (2xx)
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    
    // 客户端错误 (4xx)
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int CONFLICT = 409;
    public static final int UNPROCESSABLE_ENTITY = 422;
    public static final int TOO_MANY_REQUESTS = 429;
    
    // 服务端错误 (5xx)
    public static final int INTERNAL_ERROR = 500;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    
    /* ==================== 成员变量 ==================== */
    
    /** 状态码 */
    private int code;
    
    /** 状态信息 */
    private String message;
    
    /** 业务数据 */
    private T data;
    
    /** 时间戳 */
    private String timestamp;
    
    /** 请求路径（可选） */
    private String path;
    
    /** 扩展字段（用于额外信息） */
    private Map<String, Object> extra;
    
    /* ==================== 构造方法 ==================== */
    
    public RespEntity() {
        this.timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }
    
    private RespEntity(int code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    /* ==================== 静态工厂方法（成功） ==================== */
    
    /**
     * 快速成功（无数据）
     */
    public static <T> RespEntity<T> success() {
        return new RespEntity<>(OK, "操作成功", null);
    }
    
    /**
     * 成功并返回数据
     */
    public static <T> RespEntity<T> success(T data) {
        return new RespEntity<>(OK, "操作成功", data);
    }
    
    /**
     * 成功并自定义消息
     */
    public static <T> RespEntity<T> success(String message, T data) {
        return new RespEntity<>(OK, message, data);
    }
    
    /**
     * 创建成功（201）
     */
    public static <T> RespEntity<T> created(T data) {
        return new RespEntity<>(CREATED, "创建成功", data);
    }
    
    /* ==================== 静态工厂方法（失败） ==================== */
    
    /**
     * 通用错误
     */
    public static <T> RespEntity<T> error(String message) {
        return new RespEntity<>(INTERNAL_ERROR, message, null);
    }
    
    /**
     * 指定错误码
     */
    public static <T> RespEntity<T> error(int code, String message) {
        return new RespEntity<>(code, message, null);
    }
    
    /**
     * 参数错误（400）
     */
    public static <T> RespEntity<T> badRequest(String message) {
        return new RespEntity<>(BAD_REQUEST, message, null);
    }
    
    /**
     * 未授权（401）
     */
    public static <T> RespEntity<T> unauthorized(String message) {
        return new RespEntity<>(UNAUTHORIZED, message, null);
    }
    
    /**
     * 禁止访问（403）
     */
    public static <T> RespEntity<T> forbidden(String message) {
        return new RespEntity<>(FORBIDDEN, message, null);
    }
    
    /**
     * 资源不存在（404）
     */
    public static <T> RespEntity<T> notFound(String message) {
        return new RespEntity<>(NOT_FOUND, message, null);
    }
    
    /* ==================== 链式操作 ==================== */
    
    public RespEntity<T> withPath(String path) {
        this.path = path;
        return this;
    }
    
    public RespEntity<T> withExtra(String key, Object value) {
        if (this.extra == null) {
            this.extra = new HashMap<>();
        }
        this.extra.put(key, value);
        return this;
    }
    
    public RespEntity<T> withExtra(Map<String, Object> extraMap) {
        if (this.extra == null) {
            this.extra = new HashMap<>();
        }
        this.extra.putAll(extraMap);
        return this;
    }
    
    /* ==================== 便捷判断方法 ==================== */
    
    public boolean isSuccess() {
        return this.code >= 200 && this.code < 300;
    }
    
    public boolean isError() {
        return !isSuccess();
    }
    
    /* ==================== Getter & Setter ==================== */
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public Map<String, Object> getExtra() {
        return extra;
    }
    
    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
    
    /* ==================== 重写 toString ==================== */
    
    @Override
    public String toString() {
        return "RespEntity{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp='" + timestamp + '\'' +
                ", path='" + path + '\'' +
                ", extra=" + extra +
                '}';
    }
}