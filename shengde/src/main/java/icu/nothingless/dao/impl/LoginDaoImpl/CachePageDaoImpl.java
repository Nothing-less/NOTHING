package icu.nothingless.dao.impl.LoginDaoImpl;

import icu.nothingless.dao.interfaces.IPageDao;
import icu.nothingless.exceptions.PageItemException;
import icu.nothingless.pojo.adapter.IPageItemAdpter;
import icu.nothingless.tools.ServiceFactory;
import icu.nothingless.tools.cache.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static icu.nothingless.tools.cache.RedisCacheHelper.*;

/**
 * Page数据访问层 - Redis缓存代理实现
 */
public class CachePageDaoImpl implements IPageDao<IPageItemAdpter> {

    private static final Logger logger = LoggerFactory.getLogger(CachePageDaoImpl.class);
    private final IPageDao<IPageItemAdpter> pageDao;

    // Key前缀配置
    private static final String KEY_PREFIX_PAGE_NAME = "page:name:";
    private static final String KEY_PREFIX_PAGE_ID = "page:id:";
    private static final String KEY_PREFIX_KIDS = "page:kids:";
    private static final String KEY_PREFIX_PARENT = "page:parent:";
    private static final String KEY_PREFIX_LOCK = "page:lock:";

    @SuppressWarnings("unchecked")
    public CachePageDaoImpl() {
        this.pageDao = ServiceFactory.createInstance(IPageDao.class, "pageDaoImpl");
    }

    public CachePageDaoImpl(IPageDao<IPageItemAdpter> pageDao) {
        this.pageDao = pageDao;
    }

    @Override
    public List<IPageItemAdpter> getKidPages(String pageName) throws Exception {
        if (isBlank(pageName)) {
            logger.warn("getKidPages: pageName为空");
            return Collections.emptyList();
        }

        final String normalizedName = pageName.trim();
        final String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_KIDS, normalizedName);

        // 尝试读缓存
        CacheResult<List<IPageItemAdpter>> cacheResult = readListCache(cacheKey);

        if (cacheResult.isHit()) {
            return cacheResult.getData() != null ? cacheResult.getData() : Collections.emptyList();
        }

        if (cacheResult.isEmptyHit()) {
            return Collections.emptyList();
        }

        // 缓存未命中：加锁防击穿
        String lockKey = CacheKeyBuilder.buildLockKey(KEY_PREFIX_LOCK, normalizedName);
        boolean lockAcquired = tryLock(lockKey);

        if (!lockAcquired) {
            // 获取锁失败：短暂等待后重试
            sleepQuietly(RedisCacheConfig.LOCK_WAIT_MILLIS);
            cacheResult = readListCache(cacheKey);

            if (cacheResult.isHit()) {
                return cacheResult.getData() != null ? cacheResult.getData() : Collections.emptyList();
            }

            // 降级：直接查DB（不缓存）
            logger.warn("获取锁失败，直接查询数据库，pageName={}", normalizedName);
            return queryDBWithoutCache(normalizedName); // ← 调用降级方法
        }

        try {
            // 双检锁
            cacheResult = readListCache(cacheKey);
            if (cacheResult.isHit()) {
                return cacheResult.getData() != null ? cacheResult.getData() : Collections.emptyList();
            }

            return queryDBAndCacheKids(normalizedName); // ← 调用带缓存的方法

        } finally {
            releaseLock(lockKey);
        }
    }

    @Override
    public List<IPageItemAdpter> getKidPages(IPageItemAdpter page) throws Exception {
        if (page == null) {
            return Collections.emptyList();
        }

        String cacheKey = page.page_name();
        if (isBlank(cacheKey)) {
            cacheKey = page.page_id();
        }

        if (isBlank(cacheKey)) {
            return pageDao.getKidPages(page);
        }

        return getKidPages(cacheKey);
    }

    @Override
    public IPageItemAdpter getParentPage(String pageName) {
        if (isBlank(pageName)) {
            return null;
        }

        final String normalizedName = pageName.trim();
        final String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_PARENT, normalizedName);

        try {
            CacheResult<IPageItemAdpter> cacheResult = readSingleCache(cacheKey);

            if (cacheResult.isHit()) {
                return cacheResult.getData();
            }

            if (cacheResult.isEmptyHit()) {
                return null;
            }

            String lockKey = CacheKeyBuilder.buildLockKey(KEY_PREFIX_LOCK, "parent:" + normalizedName);
            boolean lockAcquired = tryLock(lockKey);

            if (!lockAcquired) {
                sleepQuietly(RedisCacheConfig.LOCK_WAIT_MILLIS);
                cacheResult = readSingleCache(cacheKey);
                if (cacheResult.isHit()) {
                    return cacheResult.getData();
                }
                return queryDBParentQuietly(normalizedName); // ← 调用降级方法
            }

            try {
                cacheResult = readSingleCache(cacheKey);
                if (cacheResult.isHit()) {
                    return cacheResult.getData();
                }
                return queryDBAndCacheParent(normalizedName); // ← 调用带缓存的方法

            } finally {
                releaseLock(lockKey);
            }

        } catch (Exception e) {
            logger.error("获取父页面异常，pageName={}", pageName, e);
            return queryDBParentQuietly(normalizedName);
        }
    }

    // ==================== 缓存读取方法 ====================

    @SuppressWarnings("unchecked")
    private CacheResult<List<IPageItemAdpter>> readListCache(String cacheKey) {
        String cachedJson = safeGet(cacheKey);

        if (cachedJson == null) {
            return CacheResult.miss();
        }

        if (isEmptyPlaceholder(cachedJson)) {
            logger.debug("缓存命中空值，key={}", cacheKey);
            return CacheResult.emptyHit();
        }

        try {
            List<IPageItemAdpter> list = JsonSerializer.deserialize(cachedJson, List.class);
            if (list != null) {
                return CacheResult.hit(list);
            }
        } catch (Exception e) {
            logger.warn("列表缓存反序列化失败，key={}", cacheKey, e);
        }

        return CacheResult.miss();
    }

    private CacheResult<IPageItemAdpter> readSingleCache(String cacheKey) {
        String cachedJson = safeGet(cacheKey);

        if (cachedJson == null) {
            return CacheResult.miss();
        }

        if (isEmptyPlaceholder(cachedJson)) {
            return CacheResult.emptyHit();
        }

        IPageItemAdpter page = JsonSerializer.deserialize(cachedJson, IPageItemAdpter.class);
        if (page != null) {
            return CacheResult.hit(page);
        }

        return CacheResult.miss();
    }

    // ==================== 数据库查询方法 ====================

    /**
     * 查询子页面并回填缓存（主方法）
     */
    private List<IPageItemAdpter> queryDBAndCacheKids(String pageName) throws Exception {
        try {
            List<IPageItemAdpter> result = pageDao.getKidPages(pageName);

            if (result != null && !result.isEmpty()) {
                cacheKidsResult(pageName, result);
            } else {
                cacheEmpty(CacheKeyBuilder.build(KEY_PREFIX_KIDS, pageName));
            }

            return result != null ? result : Collections.emptyList();

        } catch (Exception e) {
            logger.error("查询子页面失败，pageName={}", pageName, e);
            throw new PageItemException("查询子页面失败: " + pageName, e);
        }
    }

    /**
     * 降级查询（不缓存，用于获取锁失败时）
     */
    private List<IPageItemAdpter> queryDBWithoutCache(String pageName) {
        try {
            List<IPageItemAdpter> result = pageDao.getKidPages(pageName);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            logger.error("降级查询失败，pageName={}", pageName, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询父页面并回填缓存（主方法）
     */
    private IPageItemAdpter queryDBAndCacheParent(String pageName) throws Exception {
        IPageItemAdpter parent = pageDao.getParentPage(pageName);

        if (parent != null) {
            cacheParentResult(pageName, parent);
        } else {
            cacheEmpty(CacheKeyBuilder.build(KEY_PREFIX_PARENT, pageName));
        }

        return parent;
    }

    /**
     * 降级查询父页面（不抛异常）
     */
    private IPageItemAdpter queryDBParentQuietly(String pageName) {
        try {
            return pageDao.getParentPage(pageName);
        } catch (Exception e) {
            logger.error("查询父页面失败，pageName={}", pageName, e);
            return null;
        }
    }

    // ==================== 缓存写入方法 ====================

    private void cacheKidsResult(String pageName, List<IPageItemAdpter> kids) {
        if (kids == null || kids.isEmpty())
            return;

        String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_KIDS, pageName);
        String json = JsonSerializer.serialize(kids);

        if (json == null)
            return;

        safeSetex(cacheKey, randomTtl(), json);

        for (IPageItemAdpter kid : kids) {
            cacheSinglePage(kid);
        }
    }

    private void cacheSinglePage(IPageItemAdpter page) {
        if (page == null)
            return;

        String pageId = page.page_id();
        String pageName = page.page_name();
        String json = JsonSerializer.serialize(page);

        if (json == null)
            return;

        int ttl = randomTtl();

        if (!isBlank(pageId)) {
            safeSetex(CacheKeyBuilder.build(KEY_PREFIX_PAGE_ID, pageId), ttl, json);
        }

        if (!isBlank(pageName)) {
            safeSetex(CacheKeyBuilder.build(KEY_PREFIX_PAGE_NAME, pageName), ttl, json);
        }
    }

    private void cacheParentResult(String pageName, IPageItemAdpter parent) {
        if (parent == null)
            return;

        String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_PARENT, pageName);
        String json = JsonSerializer.serialize(parent);

        if (json != null) {
            safeSetex(cacheKey, randomTtl(), json);
        }
    }

    // ==================== 缓存失效方法 ====================

    public void evictPageCache(String pageName) {
        if (isBlank(pageName))
            return;

        String kidsKey = CacheKeyBuilder.build(KEY_PREFIX_KIDS, pageName);
        String parentKey = CacheKeyBuilder.build(KEY_PREFIX_PARENT, pageName);
        String nameKey = CacheKeyBuilder.build(KEY_PREFIX_PAGE_NAME, pageName);

        pipelineDelete(kidsKey, parentKey, nameKey);

        logger.debug("页面缓存清除完成，pageName={}", pageName);
    }

    public void evictPageCacheById(String pageId) {
        if (isBlank(pageId))
            return;

        String cacheKey = CacheKeyBuilder.build(KEY_PREFIX_PAGE_ID, pageId);
        String json = safeGet(cacheKey);

        if (json != null && !isEmptyPlaceholder(json)) {
            IPageItemAdpter page = JsonSerializer.deserialize(json, IPageItemAdpter.class);
            if (page != null && !isBlank(page.page_name())) {
                evictPageCache(page.page_name());
            }
        }

        safeDel(cacheKey);
    }
}