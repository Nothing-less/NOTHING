package icu.nothingless.pojo.engine;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseEngine<T> {
    // 当前操作的 Bean 类
    protected Class<T> beanClass;

    // 表名
    protected String tableName;

    // 参数映射
    protected Map<String, Object> params = new HashMap<>();

    // 数据库连接
    protected Connection connection;

    // 查询
    public abstract List<T> query(T bean);

    // 插入或更新
    public abstract int save(T bean);

    // 删除 status -> false
    public abstract int delete(T bean);

}
