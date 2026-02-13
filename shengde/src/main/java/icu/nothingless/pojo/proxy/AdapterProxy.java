package icu.nothingless.pojo.proxy;

import icu.nothingless.pojo.adapter.iSTAdapter2;
import icu.nothingless.pojo.adapter.iUserSTOAdapter2;
import icu.nothingless.pojo.bean.UserSTO2;
import icu.nothingless.pojo.core.QueryCondition;
import icu.nothingless.pojo.engine.iEngine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态代理类 - 实现接口实例化的核心
 */
public class AdapterProxy<T extends iSTAdapter2<T>> implements InvocationHandler {
    
    private final T target;              // 实际数据载体
    private final iEngine<T> engine;     // 对应的引擎
    private QueryCondition condition;    // 当前查询条件
    
    /**
     * 创建代理实例（核心工厂方法）
     */
    @SuppressWarnings("unchecked")
    public static <T extends iSTAdapter2<T>> T create(Class<iUserSTOAdapter2> interfaceClass, 
                                                     Class<UserSTO2> implClass,
                                                     iEngine<T> engine2) {
        try {
            T instance = (T) implClass.getDeclaredConstructor().newInstance();
            AdapterProxy<T> proxy = new AdapterProxy<>(instance, engine2);
            
            return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                null,
                proxy
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy for " + interfaceClass, e);
        }
    }
    
    private AdapterProxy(T target, iEngine<T> engine) {
        this.target = target;
        this.engine = engine;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        
        // 拦截 CRUD 方法
        switch (methodName) {
            case "save":
                return engine.save(target);
            case "delete":
                return engine.delete(target);
            case "deletePhysical":
                return engine.deletePhysical(target);
            case "query":
                clearConditionIfNeeded();
                return engine.query(target);
            case "queryOne":
                clearConditionIfNeeded();
                return engine.queryOne(target);
            case "queryById":
                return engine.queryById(args[0]);
            case "queryAll":
                return engine.queryAll();
            case "execute":
                List<T> result = engine.queryWithCondition(target, condition);
                clearCondition();
                return result;
            case "executeOne":
                T oneResult = engine.queryOneWithCondition(target, condition);
                clearCondition();
                return oneResult;
            case "getQueryCondition":
                return condition;
            case "clearCondition":
                clearCondition();
                return proxy; // 返回代理支持链式调用
            case "getAdapterClass":
                // 从目标对象获取
                Method targetMethod = findMethod(target.getClass(), "getAdapterClass");
                if (targetMethod != null) {
                    return targetMethod.invoke(target);
                }
                // 默认实现：从接口推断
                for (Class<?> iface : proxy.getClass().getInterfaces()) {
                    if (iSTAdapter2.class.isAssignableFrom(iface)) {
                        Class<T> adapterClass = (Class<T>) iface;
                        return adapterClass;
                    }
                }
                return null;
        }
        
        // 拦截链式查询条件方法
        if (conditionMethods.containsKey(methodName)) {
            if (condition == null) {
                condition = new QueryCondition();
            }
            ConditionMethod cm = conditionMethods.get(methodName);
            Object value = args.length > 1 ? args[1] : null;
            cm.apply(condition, (String) args[0], value);
            return proxy; // 返回代理支持链式调用
        }
        
        // 特殊处理 or()
        if (methodName.equals("or")) {
            if (condition != null && !condition.getConditions().isEmpty()) {
                condition.or();
            }
            return proxy;
        }
        
        // 排序
        if (methodName.equals("orderBy")) {
            if (condition == null) condition = new QueryCondition();
            condition.orderBy((String) args[0], (Boolean) args[1]);
            return proxy;
        }
        
        // 限制条数
        if (methodName.equals("limit")) {
            if (condition == null) condition = new QueryCondition();
            condition.limit((Integer) args[0]);
            return proxy;
        }
        
        // 偏移量
        if (methodName.equals("offset")) {
            if (condition == null) condition = new QueryCondition();
            condition.offset((Integer) args[0]);
            return proxy;
        }
        
        // 其他方法委托给实际对象
        return method.invoke(target, args);
    }
    
    private void clearConditionIfNeeded() {
        // query() 方法不使用 condition，但不清除，供下次使用
    }
    
    private void clearCondition() {
        if (condition != null) {
            condition.clear();
        }
    }
    
    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }
    
    // 链式条件方法映射
    private static final Map<String, ConditionMethod> conditionMethods = new HashMap<>();
    
    static {
        conditionMethods.put("eq", (c, f, v) -> c.eq(f, v));
        conditionMethods.put("neq", (c, f, v) -> c.neq(f, v));
        conditionMethods.put("gt", (c, f, v) -> c.gt(f, v));
        conditionMethods.put("gte", (c, f, v) -> c.gte(f, v));
        conditionMethods.put("lt", (c, f, v) -> c.lt(f, v));
        conditionMethods.put("lte", (c, f, v) -> c.lte(f, v));
        conditionMethods.put("like", (c, f, v) -> c.like(f, v));
        conditionMethods.put("leftLike", (c, f, v) -> c.leftLike(f, v));
        conditionMethods.put("rightLike", (c, f, v) -> c.rightLike(f, v));
        conditionMethods.put("in", (c, f, v) -> c.in(f, (List<?>) v));
        conditionMethods.put("isNull", (c, f, v) -> c.isNull(f));
        conditionMethods.put("isNotNull", (c, f, v) -> c.isNotNull(f));
    }
    
    @FunctionalInterface
    interface ConditionMethod {
        void apply(QueryCondition c, String field, Object value);
    }
}