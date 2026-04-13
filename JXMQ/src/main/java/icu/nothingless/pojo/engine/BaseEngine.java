package icu.nothingless.pojo.engine;

import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import icu.nothingless.pojo.adapter.IAdapter;

public abstract class BaseEngine<T extends IAdapter<T>, E extends BaseEngine> {

    // 查询
    public abstract List<T> query(T bean) throws Exception;

    // 插入
    public abstract Long save(T bean) throws Exception;

    // 更新
    public abstract Long update(T bean) throws Exception;
    
    // 删除 status -> false
    public abstract Long delete(T bean) throws Exception;

    public abstract Map<String, Object> toMap(T bean) throws Exception;

    public abstract T toBean(Map<String, Object> map) throws Exception;

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
