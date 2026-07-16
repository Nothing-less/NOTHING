package icu.nothingless.dao.impl;

import icu.nothingless.commons.R;
import icu.nothingless.dao.interfaces.IUserDao;
import icu.nothingless.pojo.adapter.IUserAdapter;
import icu.nothingless.pojo.dto.User;
import icu.nothingless.tools.ChatJedisUtil;
import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.cache.*;
import static icu.nothingless.tools.cache.RedisCacheHelper.*;

import java.util.List;
import java.util.Map;

/**
 * 用户数据访问层 - Redis缓存代理
 */
public class CacheUserDaoImpl implements IUserDao<User> {

    private final IUserDao userDao = ServiceFactory.createInstance(IUserDao.class, "userDaoImpl");
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CacheUserDaoImpl.class);

    // Key前缀配置
    private static final String KEY_PREFIX_USERNAME = "user:username:";
    private static final String KEY_PREFIX_ID = "user:id:";
    private static final String KEY_PREFIX_LOCK = "user:lock:";

    // ==================== 核心业务方法 ====================

    @Override
    public R findByUsername(String username) throws Exception {
        if (isBlank(username)) {
            return R.error("Empty Name");
        }
        return userDao.findByUsername(username);
    }
    /*
     * 
     * public iUserSTOAdapter findByUsername(String username) throws Exception {
     * if (isBlank(username)) {
     * return UserQueryResult.paramError("用户名不能为空").getUser();
     * }
     * 
     * final String normalizedUsername = username.trim();
     * final String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_USERNAME,
     * normalizedUsername);
     * 
     * // 尝试读缓存
     * CacheResult<iUserSTOAdapter> cacheResult = readCache(cacheKey,
     * iUserSTOAdapter.class);
     * 
     * if (cacheResult.isHit()) {
     * return cacheResult.getData();
     * }
     * 
     * // 缓存未命中：加锁防击穿
     * String lockKey = CacheKeyBuilder.buildLockKey(KEY_PREFIX_LOCK,
     * normalizedUsername);
     * boolean lockAcquired = tryLock(lockKey);
     * 
     * if (!lockAcquired) {
     * // 获取锁失败：短暂等待后重试
     * sleepQuietly(RedisCacheConfig.LOCK_WAIT_MILLIS);
     * cacheResult = readCache(cacheKey, iUserSTOAdapter.class);
     * 
     * if (cacheResult.isHit()) {
     * return cacheResult.getData();
     * }
     * 
     * // 降级：直接查DB
     * logger.
     * warn("Failed to acquire the lock, directly query the database, username={}",
     * normalizedUsername);
     * return queryDBAndCache(normalizedUsername).getUser();
     * }
     * 
     * try {
     * // 双检锁
     * cacheResult = readCache(cacheKey, iUserSTOAdapter.class);
     * if (cacheResult.isHit()) {
     * return cacheResult.getData();
     * }
     * 
     * return queryDBAndCache(normalizedUsername).getUser();
     * 
     * } finally {
     * releaseLock(lockKey);
     * }
     * }
     */

    @Override
    public R doLogin(User login) throws Exception {
        if (login == null) {
            return R.error("Illegal User");
        }
        try {
            R result = userDao.doLogin(login);
            if (result.isSuccess()) {
                User tmpUser = (User) result.data();
                evictUserCache(tmpUser.userId(), tmpUser.userAccount());
                cacheUserDoubleKey(User.forLogin(tmpUser));
                ChatJedisUtil.setUserOnline(Long.valueOf(tmpUser.userId()), User.STATUS_ACTIVE_CODE);
            }
            return result;
        } catch (Exception e) {
            logger.error("Cache login Failed!", e);
            return R.error("Cache login Failed!");
        }

    }

    @Override
    public R doLogout(User currentUser) throws Exception {
        // do logout
        if (currentUser == null) {
            return R.error("Illegal User");
        }
        try {
            R result = userDao.doLogout(currentUser);
            if (result.isSuccess()) {
                User tmpUser = readCache(CacheKeyBuilder.build(KEY_PREFIX_ID, currentUser.userId()), User.class)
                        .getData();
                ChatJedisUtil.setUserOffline(Long.valueOf(currentUser.userId()), User.STATUS_INACTIVE_CODE);
                evictUserCache(currentUser.userId(), tmpUser != null ? tmpUser.userAccount() : null);
                cacheUserDoubleKey(User.forLogout(currentUser));
                return R.success("Logout Successful");
            } else {
                return R.error("Cache user logout Failed with DB logout failure!");
            }
        } catch (Exception e) {
            logger.error("Cache user logout failed!", e);
            return R.error("Cache user logout failed!");
        }

    }

    @Override
    public R doRegister(User register) throws Exception {
        if (register == null) {
            return R.error("Illegal Register");
        }

        R result = userDao.doRegister(register);

        String username = register.userAccount();
        if (result.isSuccess() && !isBlank(username)) {
            safeDel(CacheKeyBuilder.build(KEY_PREFIX_USERNAME, username.trim()));
        }

        return R.success(username + " Register Successful ");
    }

    @Override
    public R doUpdate(User newTarget) throws Exception {
        if (isBlank(newTarget.userAccount()) || isBlank(newTarget.userPasswd())) {
            return R.error("Illegal Update");
        }

        final String normalizedUsername = newTarget.userAccount().trim();
        String userId = getUserIdByUsernameQuietly(normalizedUsername);

        R result = userDao.doUpdate(newTarget);

        if (result.isSuccess()) {
            evictUserCache(userId, normalizedUsername);
        }
        return R.success(newTarget);
    }

    @Override
    public R doSearch(String str) throws Exception {
        return userDao.doSearch(str);
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
    private void cacheUserDoubleKey(User user) throws Exception {
        if (user == null)
            return;

        String username = user.userAccount();
        String userId = user.userId();

        if (isBlank(username) || isBlank(userId)) {
            logger.warn("User data is missing key fields and cannot be cached");
            return;
        }

        String usernameKey = CacheKeyBuilder.build(KEY_PREFIX_USERNAME, username);
        String idKey = CacheKeyBuilder.build(KEY_PREFIX_ID, userId);
        String json = JsonSerializer.serialize(user.withoutPasswd());

        if (json == null)
            return;

        int ttl = randomTtl();
        pipelineSetex(usernameKey, json, idKey, json, ttl);
    }

    /**
     * 清除用户缓存
     */
    private void evictUserCache(String userId, String username) throws Exception {
        final String finalUsername = (username != null) ? username
                : (userId != null ? getUsernameByUserIdQuietly(userId) : null);

        if (finalUsername == null && userId == null)
            return;

        String usernameKey = finalUsername != null
                ? CacheKeyBuilder.build(KEY_PREFIX_USERNAME, finalUsername)
                : null;
        String idKey = userId != null
                ? CacheKeyBuilder.build(KEY_PREFIX_ID, userId)
                : null;

        pipelineDelete(usernameKey, idKey);

        logger.debug("Cache cleared，userId={}, username={}", userId, username);
    }

    /**
     * 查询数据库并回填缓存
     */
    @SuppressWarnings("unused")
    private User queryDBAndCache(String username) throws Exception {
        R queryResult = userDao.findByUsername(username);
        if (queryResult.isSuccess()) {
            User user = (User) queryResult.data();
            cacheUserDoubleKey(user);
            return user;
        } else {
            cacheEmpty(CacheKeyBuilder.build(KEY_PREFIX_USERNAME, username));
            return null;
        }
    }

    private String getUserIdByUsernameQuietly(String username) {
        try {
            String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_USERNAME, username);
            String json = safeGet(cacheKey);

            if (json != null && !isEmptyPlaceholder(json)) {
                User user = JsonSerializer.deserialize(json, User.class);
                if (user != null)
                    return user.userId();
            }
            R user = userDao.findByUsername(username);

            return user.isSuccess() ? "" + user.data() : null;
        } catch (Exception e) {
            logger.error("Get user data failed，username={}", username, e);
            return null;
        }
    }

    private String getUsernameByUserIdQuietly(String userId) {
        if (isBlank(userId))
            return null;

        try {
            String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_ID, userId);
            String json = safeGet(cacheKey);

            if (json != null && !isEmptyPlaceholder(json)) {
                IUserAdapter user = JsonSerializer.deserialize(json, IUserAdapter.class);
                if (user != null)
                    return user.getUserAccount();
            }
            return null;
        } catch (Exception e) {
            logger.error("Get username failed，userId={}", userId, e);
            return null;
        }
    }
    @Override
    public R doLogoutForAll() throws Exception {
        R result = userDao.doLogoutForAll();
        if(!result.isSuccess()){
           return R.error("Logout For All Failed!");
        }
        List<Map<String, String>> onlineUsers = (List<Map<String, String>>) result.data();
        for(Map<String, String> one: onlineUsers){
            String userId = one.get("USERID");
            String username = one.get("USERACCOUNT");
            evictUserCache(userId, username);
            ChatJedisUtil.setUserOffline(Long.valueOf(userId), User.STATUS_INACTIVE_CODE);
        }
        return R.success("All users have been kick off!");
    }

}