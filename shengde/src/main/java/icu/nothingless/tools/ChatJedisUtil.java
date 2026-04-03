package icu.nothingless.tools;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import icu.nothingless.pojo.bean.Message;

public class ChatJedisUtil {
    private static JedisPool pool;
    private static final Gson gson = new Gson();
    
    // Key前缀定义
    public static final String KEY_USER_STATUS = "im:user:status:";      // 用户在线状态
    public static final String KEY_USER_CHANNEL = "im:user:channel:";    // 用户长连接标识(轮询用)
    public static final String KEY_UNREAD_PREFIX = "im:unread:";         // 未读消息计数
    public static final String KEY_MSG_QUEUE = "im:msg:queue:";          // 消息队列(离线用户)
    public static final String KEY_RECENT_MSG = "im:recent:msg:";      // 最近消息缓存
    public static final String KEY_FRIEND_REQ = "im:friend:req:";      // 好友申请通知
    
    // 存储用户在线状态
    public static void setUserOnline(Long userId, Integer status) {
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(KEY_USER_STATUS + userId, 3600, status.toString());
        }
    }
    
    // 获取用户在线状态
    public static Integer getUserStatus(String userId) {
        try (Jedis jedis = pool.getResource()) {
            String status = jedis.get(KEY_USER_STATUS + userId);
            return status == null ? 0 : Integer.parseInt(status);
        }
    }
    
    // 添加未读消息计数
    public static void incrUnread(Long userId, Long friendId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_UNREAD_PREFIX + userId + ":" + friendId;
            jedis.hincrBy(key, "count", 1);
            jedis.expire(key, 7 * 24 * 3600); // 7天过期
        }
    }
    
    // 获取未读消息数
    public static Long getUnreadCount(Long userId, Long friendId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_UNREAD_PREFIX + userId + ":" + friendId;
            String count = jedis.hget(key, "count");
            return count == null ? 0 : Long.parseLong(count);
        }
    }
    
    // 清除未读消息
    public static void clearUnread(Long userId, Long friendId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_UNREAD_PREFIX + userId + ":" + friendId;
            jedis.del(key);
        }
    }
    
    // 存储离线消息到队列
    public static void pushOfflineMessage(Long userId, Message msg) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_MSG_QUEUE + userId;
            jedis.lpush(key, gson.toJson(msg));
            jedis.ltrim(key, 0, 99); // 最多保留100条离线消息
            jedis.expire(key, 7 * 24 * 3600);
        }
    }
    
    // 获取离线消息
    public static List<Message> popOfflineMessages(Long userId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_MSG_QUEUE + userId;
            List<String> list = jedis.lrange(key, 0, -1);
            jedis.del(key);
            
            List<Message> messages = new ArrayList<>();
            for (String json : list) {
                messages.add(gson.fromJson(json, Message.class));
            }
            return messages;
        }
    }
    
    // 缓存最近消息(用于快速加载聊天历史)
    public static void cacheRecentMessage(Long userId, Long friendId, Message msg) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_RECENT_MSG + userId + ":" + friendId;
            jedis.lpush(key, gson.toJson(msg));
            jedis.ltrim(key, 0, 49); // 保留最近50条
            jedis.expire(key, 3 * 24 * 3600);
        }
    }
    
    // 通知好友申请
    public static void notifyFriendRequest(Long targetUserId, Long fromUserId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_FRIEND_REQ + targetUserId;
            jedis.sadd(key, fromUserId.toString());
            jedis.expire(key, 7 * 24 * 3600);
        }
    }
    
    // 获取好友申请通知
    public static Set<String> getFriendRequests(Long userId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_FRIEND_REQ + userId;
            return jedis.smembers(key);
        }
    }
    
    // 清除好友申请通知
    public static void clearFriendRequests(Long userId, Long fromUserId) {
        try (Jedis jedis = pool.getResource()) {
            String key = KEY_FRIEND_REQ + userId;
            jedis.srem(key, fromUserId.toString());
        }
    }
}
