package icu.nothingless.tools;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import icu.nothingless.pojo.bean.MessageBean;

public class ChatJedisUtil {
    private static JedisPool pool;
    
    // 【修复】使用自定义 GSON 实例，支持 LocalDateTime 序列化
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .create();

    public static void init(JedisPool jedisPool) {
        pool = jedisPool;
    }
    public static JedisPool getPool() {
        return pool;
    }
    public static void closePool() {
        if (pool != null) {
            pool.close();
        }
    }

    // Key前缀定义
    public static final String KEY_USER_STATUS = "im:user:status:";
    public static final String KEY_USER_CHANNEL = "im:user:channel:";
    public static final String KEY_UNREAD_PREFIX = "im:unread:";
    public static final String KEY_MSG_QUEUE = "im:msg:queue:";
    public static final String KEY_RECENT_MSG = "im:recent:msg:";
    public static final String KEY_FRIEND_REQ = "im:friend:req:";

    // LocalDateTime 序列化器
    private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(formatter));
        }
    }

    // LocalDateTime 反序列化器
    private static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }

    // ========== 原有方法保持不变 ==========

    public static void setUserOnline(Long userId, Integer status) {
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(KEY_USER_STATUS + userId, 3600, status.toString());
        }
    }

    public static void setUserOffline(Long userId, Integer status) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(KEY_USER_STATUS + userId);
            jedis.setex(KEY_USER_STATUS + userId, 3600, ""+status.toString());
        }
    }

    public static Integer getUserStatus(String userId) {
        try (Jedis jedis = pool.getResource()) {
            String status = jedis.get(KEY_USER_STATUS + userId);
            return status == null ? 0 : Integer.parseInt(status);
        }
    }

    public static void incrUnread(Long userId, Long friendId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_UNREAD_PREFIX + userId + ":" + friendId;
            jedis.hincrBy(key, "count", 1);
            jedis.expire(key, 7 * 24 * 3600);
        }
    }

    public static Long getUnreadCount(Long userId, Long friendId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_UNREAD_PREFIX + userId + ":" + friendId;
            String count = jedis.hget(key, "count");
            return count == null ? 0 : Long.parseLong(count);
        }
    }

    public static void clearUnread(Long userId, Long friendId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_UNREAD_PREFIX + userId + ":" + friendId;
            jedis.del(key);
        }
    }

    public static void pushOfflineMessage(Long userId, MessageBean msg) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_MSG_QUEUE + userId;
            jedis.lpush(key, gson.toJson(msg));
            jedis.ltrim(key, 0, 99);
            jedis.expire(key, 7 * 24 * 3600);
        }
    }

    public static List<MessageBean> popOfflineMessages(Long userId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_MSG_QUEUE + userId;
            List<String> list = jedis.lrange(key, 0, -1);
            jedis.del(key);

            List<MessageBean> messages = new ArrayList<>();
            for (String json : list) {
                messages.add(gson.fromJson(json, MessageBean.class));
            }
            return messages;
        }
    }

    public static void cacheRecentMessage(Long userId, Long friendId, MessageBean msg) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_RECENT_MSG + userId + ":" + friendId;
            jedis.lpush(key, gson.toJson(msg));
            jedis.ltrim(key, 0, 49);
            jedis.expire(key, 3 * 24 * 3600);
        }
    }

    public static void notifyFriendRequest(Long targetUserId, Long fromUserId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_FRIEND_REQ + targetUserId;
            jedis.sadd(key, fromUserId.toString());
            jedis.expire(key, 7 * 24 * 3600);
        }
    }

    public static Set<String> getFriendRequests(Long userId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_FRIEND_REQ + userId;
            return jedis.smembers(key);
        }
    }

    public static void clearFriendRequests(Long userId, Long fromUserId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_FRIEND_REQ + userId;
            jedis.srem(key, fromUserId.toString());
        }
    }

    public static void restoreFriendRequests(Long userId, Long friendId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_FRIEND_REQ + userId;
            jedis.sadd(key, friendId.toString());
            jedis.expire(key, 7 * 24 * 3600);
        }
    }
}