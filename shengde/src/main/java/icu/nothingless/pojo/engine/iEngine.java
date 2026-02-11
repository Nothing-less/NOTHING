package icu.nothingless.pojo.engine;

import icu.nothingless.pojo.adapter.iSTAdapter2;
import icu.nothingless.pojo.core.QueryCondition;

import java.sql.Connection;
import java.util.List;

/**
 * 通用引擎接口 - 与具体表完全解耦
 */
public interface iEngine<T extends iSTAdapter2<T>> {
    
    // ========== 配置方法 ==========
    
    void setConnection(Connection connection);
    
    void setTableName(String tableName);
    
    void setColumnMapping(java.util.Map<String, String> mapping);
    
    void setPrimaryKey(String primaryKey);
    
    // ========== CRUD 实现 ==========
    
    Long save(T bean);
    
    Long delete(T bean);
    
    Long deletePhysical(T bean);
    
    List<T> query(T bean);
    
    T queryOne(T bean);
    
    T queryById(Object id);
    
    List<T> queryAll();
    
    List<T> queryWithCondition(T bean, QueryCondition condition);
    
    T queryOneWithCondition(T bean, QueryCondition condition);
    
    // ========== 批量操作 ==========
    
    int batchSave(List<T> beans);
    
    int batchDelete(List<T> beans);
    
    // ========== 统计 ==========
    
    long count(T bean);
    
    long countWithCondition(T bean, QueryCondition condition);
    
    boolean exists(T bean);
}