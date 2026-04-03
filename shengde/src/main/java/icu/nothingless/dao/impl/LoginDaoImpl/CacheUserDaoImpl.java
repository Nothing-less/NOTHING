package icu.nothingless.dao.impl.LoginDaoImpl;

import icu.nothingless.dao.interfaces.iUserDao;
import icu.nothingless.pojo.adapter.iUserSTOAdapter;
import icu.nothingless.pojo.bean.UserSTO;
import icu.nothingless.tools.SHA256Util;
import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.cache.*;

import java.util.List;
import java.util.Map;

import static icu.nothingless.tools.cache.RedisCacheHelper.*;

/**
 * 用户数据访问层 - Redis缓存代理实现
 * 
 * 设计原则：
 * 纯粹代理：对调用方透明，无额外副作用
 * 极简一致：同步双删，失败即抛异常
 * 安全并发：原子加锁、防击穿、防穿透
 * 无状态：支持多实例或单例
 */
public class CacheUserDaoImpl implements iUserDao {
    
    private final iUserDao userDao;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CacheUserDaoImpl.class);
    
    // Key前缀配置
    private static final String KEY_PREFIX_USERNAME = "user:username:";
    private static final String KEY_PREFIX_ID = "user:id:";
    private static final String KEY_PREFIX_LOCK = "user:lock:";
    
    // ==================== 构造器 ====================
    
    public CacheUserDaoImpl() {
        this.userDao = ServiceFactory.createInstance(iUserDao.class, "userDaoImpl");
    }
    
    public CacheUserDaoImpl(iUserDao userDao) {
        this.userDao = userDao;
    }
    
    // ==================== 核心业务方法 ====================

    @Override
    public iUserSTOAdapter findByUsername(String username) throws Exception {
        if (isBlank(username)) {
            return UserQueryResult.paramError("用户名不能为空").getUser();
        }
        
        final String normalizedUsername = username.trim();
        final String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_USERNAME, normalizedUsername);
        
        // 1. 尝试读缓存
        CacheResult<iUserSTOAdapter> cacheResult = readCache(cacheKey, iUserSTOAdapter.class);
        
        if (cacheResult.isHit()) {
            return cacheResult.getData();
        }
        
        // 2. 缓存未命中：加锁防击穿
        String lockKey = CacheKeyBuilder.buildLockKey(KEY_PREFIX_LOCK, normalizedUsername);
        boolean lockAcquired = tryLock(lockKey);
        
        if (!lockAcquired) {
            // 获取锁失败：短暂等待后重试
            sleepQuietly(RedisCacheConfig.LOCK_WAIT_MILLIS);
            cacheResult = readCache(cacheKey, iUserSTOAdapter.class);
            
            if (cacheResult.isHit()) {
                return cacheResult.getData();
            }
            
            // 降级：直接查DB
            logger.warn("Failed to acquire the lock, directly query the database, username={}", normalizedUsername);
            return queryDBAndCache(normalizedUsername).getUser();
        }
        
        try {
            // 双检锁
            cacheResult = readCache(cacheKey, iUserSTOAdapter.class);
            if (cacheResult.isHit()) {
                return cacheResult.getData();
            }
            
            return queryDBAndCache(normalizedUsername).getUser();
            
        } finally {
            releaseLock(lockKey);
        }
    }
    
    public Boolean doLogin(iUserSTOAdapter login) throws Exception {
        if (login == null || isBlank(login.getUserId())) {
            return false;
        }
        
        Boolean result = true; // userDao.doLogin(login);
        
        if (Boolean.TRUE.equals(result)) {
            evictUserCache(login.getUserId(), null);
        }
        
        return result;
    }
    
    @Override
    public Boolean doRegister(Map<String, String> register) throws Exception {
        if (register == null || register.isEmpty()) {
            return false;
        }
        
        Boolean result = userDao.doRegister(register);
        
        String username = register.get("username");
        if (Boolean.TRUE.equals(result) && !isBlank(username)) {
            safeDel(CacheKeyBuilder.build(KEY_PREFIX_USERNAME, username.trim()));
        }
        
        return result;
    }
    
    @Override
    public Boolean updatePwd(String username, String newPassword) throws Exception {
        if (isBlank(username) || isBlank(newPassword)) {
            return false;
        }
        
        final String normalizedUsername = username.trim();
        String userId = getUserIdByUsernameQuietly(normalizedUsername);
        
        Boolean result = userDao.updatePwd(normalizedUsername, newPassword);
        
        if (Boolean.TRUE.equals(result)) {
            evictUserCache(userId, normalizedUsername);
        }
        
        return result;
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 读取缓存（处理空值占位符）
     */
    private <T> CacheResult<T> readCache(String cacheKey, Class<T> clazz) {
        String cachedJson = safeGet(cacheKey);
        
        if (cachedJson == null) {
            return CacheResult.miss();
        }
        
        if (isEmptyPlaceholder(cachedJson)) {
            logger.debug("Cache hit null，key={}", cacheKey);
            return CacheResult.emptyHit();
        }
        
        T data = JsonSerializer.deserialize(cachedJson, clazz);
        if (data != null) {
            return CacheResult.hit(data);
        }
        
        logger.warn("Cache broken，key={}", cacheKey);
        return CacheResult.miss(); // 视为未命中，重新加载
    }
    
    /**
     * 双Key缓存用户数据
     */
    private void cacheUserDoubleKey(iUserSTOAdapter user) throws Exception {
        if (user == null) return;
        
        String username = user.getUserAccount();
        String userId = user.getUserId();
        
        if (isBlank(username) || isBlank(userId)) {
            logger.warn("User data is missing key fields and cannot be cached");
            return;
        }
        
        String usernameKey = CacheKeyBuilder.build(KEY_PREFIX_USERNAME, username);
        String idKey = CacheKeyBuilder.build(KEY_PREFIX_ID, userId);
        String json = JsonSerializer.serialize(user);
        
        if (json == null) return;
        
        int ttl = randomTtl();
        pipelineSetex(usernameKey, json, idKey, json, ttl);
    }
    
    /**
     * 清除用户缓存
     */
    private void evictUserCache(String userId, String username) throws Exception {
        final String finalUsername = (username != null) ? username 
            : (userId != null ? getUsernameByUserIdQuietly(userId) : null);
        
        if (finalUsername == null && userId == null) return;
        
        String usernameKey = finalUsername != null 
            ? CacheKeyBuilder.build(KEY_PREFIX_USERNAME, finalUsername) : null;
        String idKey = userId != null 
            ? CacheKeyBuilder.build(KEY_PREFIX_ID, userId) : null;
        
        pipelineDelete(usernameKey, idKey);
        
        logger.debug("Cache cleared，userId={}, username={}", userId, username);
    }
    
    /**
     * 查询数据库并回填缓存
     */
    @SuppressWarnings("unused")
    private UserQueryResult queryDBAndCache(String username) throws Exception {
        // 实际项目中调用：iUserSTOAdapter user = userDao.findByUsername(username);
        
        // Mock数据（测试用）
        iUserSTOAdapter user = new UserSTO();
        user.setUserAccount(username);
        user.setUserPasswd(SHA256Util.encrypt(username));
        user.setUserId("1");
        
        if (user != null) {
            cacheUserDoubleKey(user);
            return UserQueryResult.success(user);
        } else {
            cacheEmpty(CacheKeyBuilder.build(KEY_PREFIX_USERNAME, username));
            return UserQueryResult.notFound();
        }
    }
    
    private String getUserIdByUsernameQuietly(String username) {
        try {
            String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_USERNAME, username);
            String json = safeGet(cacheKey);
            
            if (json != null && !isEmptyPlaceholder(json)) {
                iUserSTOAdapter user = JsonSerializer.deserialize(json, iUserSTOAdapter.class);
                if (user != null) return user.getUserId();
            }
            
            iUserSTOAdapter user = userDao.findByUsername(username);
            return user != null ? user.getUserId() : null;
        } catch (Exception e) {
            logger.error("Get user data failed，username={}", username, e);
            return null;
        }
    }
    
    private String getUsernameByUserIdQuietly(String userId) {
        if (isBlank(userId)) return null;
        
        try {
            String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_ID, userId);
            String json = safeGet(cacheKey);
            
            if (json != null && !isEmptyPlaceholder(json)) {
                iUserSTOAdapter user = JsonSerializer.deserialize(json, iUserSTOAdapter.class);
                if (user != null) return user.getUserAccount();
            }
            return null;
        } catch (Exception e) {
            logger.error("Get username failed，userId={}", userId, e);
            return null;
        }
    }
    
    // ==================== 内部结果类 ====================
    
    public static class UserQueryResult {
        public enum Status { SUCCESS, NOT_FOUND, PARAM_ERROR, SYSTEM_ERROR }
        
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
            return new UserQueryResult(Status.NOT_FOUND, null, "User not Exists");
        }
        
        public static UserQueryResult paramError(String message) {
            return new UserQueryResult(Status.PARAM_ERROR, null, message);
        }
        
        public Status getStatus() { return status; }
        public iUserSTOAdapter getUser() { return user; }
        public String getMessage() { return message; }
    }

    @Override
    public List<iUserSTOAdapter> fuzzyQuery(String keyword) throws Exception {
        return userDao.fuzzyQuery(keyword);
    }
}