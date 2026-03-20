package icu.nothingless.dao.impl.LoginDaoImpl;

import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import icu.nothingless.dao.interfaces.iUserDao;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.tools.RedisUtil;
import icu.nothingless.tools.SHA256Util;
import icu.nothingless.tools.ServiceFactory;
import redis.clients.jedis.params.SetParams;

/**
 * 
 * 设计原则：
 * 纯粹代理：对调用方透明，无额外副作用（无异步线程泄漏）
 * 极简一致：同步双删，失败即抛异常，由上层处理
 * 安全并发：原子加锁、防击穿、防穿透
 * 无状态：不维护熔断器等实例状态，支持多实例或单例
 */
public class CacheUserDaoImpl implements iUserDao {
    
    // ==================== 依赖注入 ====================
    
    private final iUserDao userDao;
    private static final Logger logger = LoggerFactory.getLogger(CacheUserDaoImpl.class);
    
    // JSON 序列化器（线程安全）
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    
    // ==================== 缓存配置 ====================
    
    private static final int BASE_CACHE_TTL_SECONDS = 1800;      // 基础 30 分钟
    private static final int TTL_RANDOM_RANGE_SECONDS = 300;      // 随机偏移 5 分钟
    private static final int EMPTY_CACHE_TTL_SECONDS = 60;        // 空值缓存 60 秒
    private static final int LOCK_TTL_SECONDS = 10;               // 互斥锁 10 秒
    
    private static final String KEY_PREFIX_USERNAME = "user:username:";
    private static final String KEY_PREFIX_ID = "user:id:";
    private static final String KEY_PREFIX_LOCK = "user:lock:";
    private static final String EMPTY_PLACEHOLDER = "__EMPTY__";
    
    // ==================== 构造器 ====================
    
    public CacheUserDaoImpl() {
        this.userDao = ServiceFactory.createInstance(iUserDao.class, "userDaoImpl");
    }
    
    public CacheUserDaoImpl(iUserDao userDao) {
        this.userDao = userDao;
    }
    
    // ==================== 核心业务方法 ====================

    /**
     * 根据用户名查询用户（防击穿 + 防雪崩 + 防穿透）
     */
    @Override
    public iUserSTOAdapter findByUsername(String username) throws Exception {
        // 参数校验
        if (isBlank(username)) {
            return UserQueryResult.paramError("用户名不能为空").getUser();
        }
        
        final String normalizedUsername = username.trim();
        final String cacheKey = buildUsernameKey(normalizedUsername);
        
        // 尝试读缓存
        String cachedJson = safeRedisGet(cacheKey);
        
        if (cachedJson != null) {
            if (EMPTY_PLACEHOLDER.equals(cachedJson)) {
                logger.debug("缓存命中空值，username={}", normalizedUsername);
                return UserQueryResult.notFound().getUser();
            }
            
            iUserSTOAdapter user = safeDeserialize(cachedJson);
            if (user != null) {
                return user;
            }
            
            // 反序列化失败，视为缓存失效，继续查DB
            logger.warn("缓存数据损坏，key={}", cacheKey);
        }
        
        // 缓存未命中：加互斥锁防击穿
        String lockKey = buildLockKey(normalizedUsername);
        boolean lockAcquired = tryLock(lockKey);
        
        if (!lockAcquired) {
            // 获取锁失败：短暂等待后重试读缓存
            sleepQuietly(100);
            cachedJson = safeRedisGet(cacheKey);
            
            if (cachedJson != null && !EMPTY_PLACEHOLDER.equals(cachedJson)) {
                iUserSTOAdapter user = safeDeserialize(cachedJson);
                if (user != null) {
                    return user;
                }
            }
            
            // 重试后仍无缓存，直接查 DB（降级）
            logger.warn("获取锁失败，直接查询数据库，username={}", normalizedUsername);
            return queryDBAndCache(normalizedUsername).getUser();
        }
        
        try {
            // 双检锁：获取锁后再查一次缓存
            cachedJson = safeRedisGet(cacheKey);
            if (cachedJson != null) {
                if (EMPTY_PLACEHOLDER.equals(cachedJson)) {
                    return UserQueryResult.notFound().getUser();
                }
                iUserSTOAdapter user = safeDeserialize(cachedJson);
                if (user != null) {
                    return user;
                }
            }
            
            // 查数据库并回填缓存
            return queryDBAndCache(normalizedUsername).getUser();
            
        } finally {
            releaseLock(lockKey);
        }
    }
    
    /**
     * 登录操作（同步双删保证一致性）
     */
    public Boolean doLogin(iUserSTOAdapter login) throws Exception {
        if (login == null || isBlank(login.getUserId())) {
            return false;
        }
        
        // 更新数据库
        // Boolean result = userDao.doLogin(login);
        Boolean result = true;
        // 同步删除缓存（无论DB是否成功，都尝试清理可能存在的缓存）
        // 若删除失败抛异常，由上层决定重试或忽略
        if (Boolean.TRUE.equals(result)) {
            evictUserCache(login.getUserId(), null);
        }
        
        return result;
    }
    
    /**
     * 注册操作（清理空值缓存）
     */
    @Override
    public Boolean doRegister(Map<String, String> register) throws Exception {
        if (register == null || register.isEmpty()) {
            return false;
        }
        
        Boolean result = userDao.doRegister(register);
        
        // 注册成功，清除"用户不存在"的空值缓存
        String username = register.get("username");
        if (Boolean.TRUE.equals(result) && !isBlank(username)) {
            String cacheKey = buildUsernameKey(username.trim());
            safeRedisDel(cacheKey);
        }
        
        return result;
    }
    
    /**
     * 修改密码（同步双删）
     */
    @Override
    public Boolean updatePwd(String username, String newPassword) throws Exception {
        if (isBlank(username) || isBlank(newPassword)) {
            return false;
        }
        
        final String normalizedUsername = username.trim();
        String userId = getUserIdByUsernameQuietly(normalizedUsername);
        
        // 更新数据库
        Boolean result = userDao.updatePwd(normalizedUsername, newPassword);
        
        // 同步删除缓存
        if (Boolean.TRUE.equals(result)) {
            evictUserCache(userId, normalizedUsername);
        }
        
        return result;
    }
    
    // ==================== 缓存工具方法 ====================
    
    /**
     * 双 Key 缓存用户数据（带随机 TTL 防雪崩）
     */
    private void cacheUserDoubleKey(iUserSTOAdapter user) throws Exception {
        if (user == null) {
            return;
        }
        
        String username = user.getUserAccount();
        String userId = user.getUserId();
        
        if (isBlank(username) || isBlank(userId)) {
            logger.warn("用户数据缺少关键字段，无法缓存");
            return;
        }
        
        String usernameKey = buildUsernameKey(username);
        String idKey = buildIdKey(userId);
        String json = safeSerialize(user);
        
        if (json == null) {
            return;
        }
        
        // 随机 TTL：1800 ~ 2100 秒
        int ttl = BASE_CACHE_TTL_SECONDS + new Random().nextInt(TTL_RANDOM_RANGE_SECONDS);
        
        RedisUtil.pipeline(pipeline -> {
            pipeline.setex(usernameKey, ttl, json);
            pipeline.setex(idKey, ttl, json);
        });
    }
    
    /**
     * 缓存空值（短期）
     */
    private void cacheEmpty(String cacheKey) throws Exception {
        RedisUtil.setex(cacheKey, EMPTY_CACHE_TTL_SECONDS, EMPTY_PLACEHOLDER);
    }
    
    /**
     * 清除用户缓存（同步执行，失败抛异常）
     */
    private void evictUserCache(String userId, String username) throws Exception {
        final String finalUsername;
        
        if (username != null) {
            finalUsername = username;
        } else if (userId != null) {
            finalUsername = getUsernameByUserIdQuietly(userId);
        } else {
            return;
        }
        
        final String finalUserId = userId;
        
        RedisUtil.pipeline(pipeline -> {
            if (finalUsername != null) {
                pipeline.del(buildUsernameKey(finalUsername));
            }
            if (finalUserId != null) {
                pipeline.del(buildIdKey(finalUserId));
            }
        });
        
        logger.debug("缓存清除完成，userId={}, username={}", userId, username);
    }
    
    // ==================== 查询辅助方法 ====================
    
    /**
     * 查询数据库并回填缓存
     */
    private UserQueryResult queryDBAndCache(String username) throws Exception {
        /*
        iUserSTOAdapter user = userDao.findByUsername(username);
        
        if (user != null) {
            cacheUserDoubleKey(user);
            return UserQueryResult.success(user);
        } else {
            cacheEmpty(buildUsernameKey(username));
            return UserQueryResult.notFound();
        }
        
        */
       iUserSTOAdapter user = new UserSTO();
       user.setUserAccount(username);
       String password = SHA256Util.encrypt(username);
       logger.error("Password Encryped::{}",password);
       user.setUserPasswd(password);
       user.setUserId("1");
       return UserQueryResult.success(user);


    }
    
    /**
     * 通过 username 获取 userId（静默，不抛异常）
     */
    private String getUserIdByUsernameQuietly(String username) {
        try {
            String cacheKey = buildUsernameKey(username);
            String json = safeRedisGet(cacheKey);
            
            if (json != null && !EMPTY_PLACEHOLDER.equals(json)) {
                iUserSTOAdapter user = safeDeserialize(json);
                if (user != null) {
                    return user.getUserId();
                }
            }
            
            iUserSTOAdapter user = userDao.findByUsername(username);
            return user != null ? user.getUserId() : null;
        } catch (Exception e) {
            logger.error("获取 userId 失败，username={}", username, e);
            return null;
        }
    }
    
    /**
     * 通过 userId 获取 username（静默）
     */
    private String getUsernameByUserIdQuietly(String userId) {
        if (isBlank(userId)) {
            return null;
        }
        
        try {
            String cacheKey = buildIdKey(userId);
            String json = safeRedisGet(cacheKey);
            
            if (json != null && !EMPTY_PLACEHOLDER.equals(json)) {
                iUserSTOAdapter user = safeDeserialize(json);
                if (user != null) {
                    return user.getUserAccount();
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("获取 username 失败，userId={}", userId, e);
            return null;
        }
    }
    
    // ==================== 分布式锁（原子操作） ====================
    
    /**
     * 尝试获取锁（原子 SET NX EX）
     */
    private boolean tryLock(String lockKey) {
        try {
            // 原子操作：SET key value NX EX 10
            SetParams params = new SetParams();
            params.nx();  // 不存在才设置
            params.ex(LOCK_TTL_SECONDS);  // 过期时间10秒
            
            return RedisUtil.set(lockKey, "1", params);
        } catch (Exception e) {
            logger.error("获取锁失败，lockKey={}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 释放锁
     */
    private void releaseLock(String lockKey) {
        try {
            RedisUtil.del(lockKey);
        } catch (Exception e) {
            logger.warn("释放锁失败，lockKey={}", lockKey, e);
        }
    }
    
    // ==================== 安全的 Redis 操作包装 ====================
    
    private String safeRedisGet(String key) {
        try {
            return RedisUtil.get(key);
        } catch (Exception e) {
            logger.error("Redis get 失败，key={}", key, e);
            return null;
        }
    }
    
    private void safeRedisDel(String key) {
        try {
            RedisUtil.del(key);
        } catch (Exception e) {
            logger.error("Redis del 失败，key={}", key, e);
        }
    }
    
    // ==================== 序列化工具 ====================
    
    private String safeSerialize(iUserSTOAdapter user) {
        if (user == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(user);
        } catch (Exception e) {
            logger.error("序列化失败", e);
            return null;
        }
    }
    
    private iUserSTOAdapter safeDeserialize(String json) {
        if (isBlank(json)) return null;
        try {
            return OBJECT_MAPPER.readValue(json, iUserSTOAdapter.class);
        } catch (Exception e) {
            logger.error("反序列化失败，json={}", json, e);
            return null;
        }
    }
    
    // ==================== Key 生成工具 ====================
    
    private String buildUsernameKey(String username) {
        return KEY_PREFIX_USERNAME + username;
    }
    
    private String buildIdKey(String userId) {
        return KEY_PREFIX_ID + userId;
    }
    
    private String buildLockKey(String username) {
        return KEY_PREFIX_LOCK + username;
    }
    
    // ==================== 通用工具方法 ====================
    
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // ==================== 业务结果对象 ====================
    
    public static class UserQueryResult {
        public enum Status {
            SUCCESS, NOT_FOUND, PARAM_ERROR, SYSTEM_ERROR
        }
        
        private final Status status;
        private final iUserSTOAdapter user;
        private final String message;
        
        private UserQueryResult(Status status, iUserSTOAdapter user, String message) {
            this.status = status;
            this.user = user;
            this.message = message;
        }
        
        public static UserQueryResult success(iUserSTOAdapter user) {
            return new UserQueryResult(Status.SUCCESS, user, null);
        }
        
        public static UserQueryResult notFound() {
            return new UserQueryResult(Status.NOT_FOUND, null, "用户不存在");
        }
        
        public static UserQueryResult paramError(String message) {
            return new UserQueryResult(Status.PARAM_ERROR, null, message);
        }
        
        public static UserQueryResult systemError(String message) {
            return new UserQueryResult(Status.SYSTEM_ERROR, null, message);
        }
        
        public Status getStatus() { return status; }
        public iUserSTOAdapter getUser() { return user; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return status == Status.SUCCESS; }
    }
}