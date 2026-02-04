package icu.nothingless.pojo.engine;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import icu.nothingless.pojo.adapter.iSTAdapter;

public abstract class BaseEngine<T extends iSTAdapter<T>, E> {

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

    // 线程安全的实例注册表
    private static final ConcurrentHashMap<Class<?>, BaseEngine<?, ?>> INSTANCES = new ConcurrentHashMap<>();

    protected BaseEngine() {
        // 防止反射攻击（可选）
        if (INSTANCES.containsKey(this.getClass())) {
            throw new IllegalStateException("Instance already exists for " + this.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends BaseEngine<?, E>> E getInstance(Class<E> clazz) {
        return (E) INSTANCES.computeIfAbsent(clazz, k -> {
            try {
                // 调用无参构造函数
                return clazz.getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException
                    | InvocationTargetException e) {
                throw new RuntimeException("Failed to create singleton for " + clazz, e);
            }
        });
    }

}
