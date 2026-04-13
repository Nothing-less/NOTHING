package icu.nothingless.tools.cache;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程安全的JSON序列化工具
 */
public final class JsonSerializer {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);
    
    // 线程安全的ObjectMapper
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    
    private JsonSerializer() {}
    
    /**
     * 序列化对象为JSON字符串
     */
    public static <T> String serialize(T obj) {
        if (obj == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Serialize Failed! \n object={}", obj, e);
            return null;
        }
    }
    
    /**
     * 反序列化JSON字符串为对象
     */
    public static <T> T deserialize(String json, Class<T> clazz) {
        if (isBlank(json)) return null;
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("Deserialize Failed! \n json={}", json, e);
            return null;
        }
    }
    
    /**
     * 安全地反序列化（返回boolean表示是否成功）
     */
    public static <T> DeserializeResult<T> safeDeserialize(String json, Class<T> clazz) {
        if (isBlank(json)) {
            return DeserializeResult.empty();
        }
        try {
            T result = OBJECT_MAPPER.readValue(json, clazz);
            return DeserializeResult.success(result);
        } catch (Exception e) {
            logger.error("Deserialize Failed! \n json={}", json, e);
            return DeserializeResult.failure(e.getMessage());
        }
    }
    
    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 反序列化结果包装
     */
    public static class DeserializeResult<T> {
        private final boolean success;
        private final T data;
        private final String error;
        
        private DeserializeResult(boolean success, T data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
        
        public static <T> DeserializeResult<T> success(T data) {
            return new DeserializeResult<>(true, data, null);
        }
        
        public static <T> DeserializeResult<T> failure(String error) {
            return new DeserializeResult<>(false, null, error);
        }
        
        public static <T> DeserializeResult<T> empty() {
            return new DeserializeResult<>(false, null, "Empty input");
        }
        
        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getError() { return error; }
    }
}