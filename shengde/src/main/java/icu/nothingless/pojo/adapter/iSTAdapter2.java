package icu.nothingless.pojo.adapter;

import icu.nothingless.pojo.core.QueryCondition;

import java.util.List;

/**
 * 最基础适配器接口 - 完全解耦，无任何实现依赖
 */
public interface iSTAdapter2<T extends iSTAdapter2<T>> {
    
    // 获取当前适配器的类类型（用于框架识别）
    Class<T> getAdapterClass();
    
    // ========== CRUD 操作 ==========
    
    /**
     * 保存（插入或更新，根据主键是否存在自动判断）
     */
    Long save();
    
    /**
     * 删除（逻辑删除，设置 status = false）
     */
    Long delete();
    
    /**
     * 物理删除（直接从数据库删除）
     */
    Long deletePhysical();
    
    /**
     * 根据当前对象非空字段查询
     */
    List<T> query();
    
    /**
     * 查询单条
     */
    T queryOne();
    
    /**
     * 根据主键查询
     */
    T queryById(Object id);
    
    /**
     * 查询所有
     */
    List<T> queryAll();
    
    // ========== 链式查询条件 ==========
    
    /**
     * 等于
     */
    T eq(String field, Object value);
    
    /**
     * 不等于
     */
    T neq(String field, Object value);
    
    /**
     * 大于
     */
    T gt(String field, Object value);
    
    /**
     * 大于等于
     */
    T gte(String field, Object value);
    
    /**
     * 小于
     */
    T lt(String field, Object value);
    
    /**
     * 小于等于
     */
    T lte(String field, Object value);
    
    /**
     * 模糊查询（前后模糊）
     */
    T like(String field, Object value);
    
    /**
     * 左模糊
     */
    T leftLike(String field, Object value);
    
    /**
     * 右模糊
     */
    T rightLike(String field, Object value);
    
    /**
     * IN 查询
     */
    T in(String field, List<?> values);
    
    /**
     * IS NULL
     */
    T isNull(String field);
    
    /**
     * IS NOT NULL
     */
    T isNotNull(String field);
    
    /**
     * 切换到 OR 条件（默认是 AND）
     */
    T or();
    
    /**
     * 排序
     */
    T orderBy(String field, boolean asc);
    
    /**
     * 倒序排序（便捷方法）
     */
    default T orderByDesc(String field) {
        return orderBy(field, false);
    }
    
    /**
     * 限制条数
     */
    T limit(int limit);
    
    /**
     * 偏移量
     */
    T offset(int offset);
    
    /**
     * 分页（便捷方法）
     */
    default T page(int pageNum, int pageSize) {
        return limit(pageSize).offset((pageNum - 1) * pageSize);
    }
    
    /**
     * 执行查询（带条件）
     */
    List<T> execute();
    
    /**
     * 执行查询单条
     */
    T executeOne();
    
    /**
     * 获取当前查询条件（供框架使用）
     */
    QueryCondition getQueryCondition();
    
    /**
     * 清空查询条件
     */
    void clearCondition();
}