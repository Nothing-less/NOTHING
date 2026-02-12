package icu.nothingless.pojo.engine;

import icu.nothingless.pojo.adapter.iSTAdapter2;
import icu.nothingless.pojo.core.FrameworkRegistry;
import icu.nothingless.pojo.core.QueryCondition;
import icu.nothingless.pojo.core.QueryCondition.Condition;
import icu.nothingless.pojo.core.QueryCondition.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 通用引擎实现 - 通过反射处理任意适配器
 */
public class GenericEngine<T extends iSTAdapter2<T>> implements iEngine<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericEngine.class);
    
    private Connection connection;
    private String tableName;
    private String primaryKey = "id";
    private Map<String, String> columnMapping = new ConcurrentHashMap<>();
    private Map<String, Method> getterCache = new ConcurrentHashMap<>();
    private Map<String, Method> setterCache = new ConcurrentHashMap<>();
    
    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    @Override
    public void setColumnMapping(Map<String, String> mapping) {
        this.columnMapping = mapping;
    }
    
    @Override
    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
    
    @Override
    public Long save(T bean) {
        try {
            Object idValue = invokeGetter(bean, primaryKey);
            if (idValue == null || (idValue instanceof String && ((String) idValue).isEmpty())) {
                return insert(bean);
            } else {
                return update(bean);
            }
        } catch (Exception e) {
            logger.error("Save failed", e);
            return -1L;
        }
    }
    
    private Long insert(T bean) throws Exception {
        Map<String, Object> fieldValues = extractNonNullFields(bean);
        fieldValues.remove(toColumnName(primaryKey));
        
        String columns = String.join(", ", fieldValues.keySet());
        String placeholders = fieldValues.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
        
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            int i = 1;
            for (Object value : fieldValues.values()) {
                ps.setObject(i++, value);
            }
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
            return (long) affectedRows;
        }
    }
    
    private Long update(T bean) throws Exception {
        Map<String, Object> fieldValues = extractNonNullFields(bean);
        Object idValue = fieldValues.remove(toColumnName(primaryKey));
        
        if (idValue == null) {
            throw new IllegalArgumentException("Primary key value is required for update");
        }
        
        String setClause = fieldValues.keySet().stream()
            .map(k -> k + " = ?")
            .collect(Collectors.joining(", "));
        
        String sql = String.format("UPDATE %s SET %s WHERE %s = ?", 
            tableName, setClause, toColumnName(primaryKey));
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int i = 1;
            for (Object value : fieldValues.values()) {
                ps.setObject(i++, value);
            }
            ps.setObject(i, idValue);
            
            return (long) ps.executeUpdate();
        }
    }
    
    @Override
    public Long delete(T bean) {
        try {
            Object idValue = invokeGetter(bean, primaryKey);
            if (idValue == null) {
                logger.error("Delete failed: primary key is null");
                return -1L;
            }
            
            String sql = String.format("UPDATE %s SET user_status = ? WHERE %s = ?", 
                tableName, toColumnName(primaryKey));
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setBoolean(1, false);
                ps.setObject(2, idValue);
                return (long) ps.executeUpdate();
            }
        } catch (Exception e) {
            logger.error("Delete failed", e);
            return -1L;
        }
    }
    
    @Override
    public Long deletePhysical(T bean) {
        try {
            Object idValue = invokeGetter(bean, primaryKey);
            if (idValue == null) {
                logger.error("Physical delete failed: primary key is null");
                return -1L;
            }
            
            String sql = String.format("DELETE FROM %s WHERE %s = ?", 
                tableName, toColumnName(primaryKey));
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setObject(1, idValue);
                return (long) ps.executeUpdate();
            }
        } catch (Exception e) {
            logger.error("Physical delete failed", e);
            return -1L;
        }
    }
    
    @Override
    public List<T> query(T bean) {
        try {
            Map<String, Object> fieldValues = extractNonNullFields(bean);
            if (fieldValues.isEmpty()) {
                return queryAll();
            }
            
            String whereClause = fieldValues.keySet().stream()
                .map(k -> k + " = ?")
                .collect(Collectors.joining(" AND "));
            
            String sql = String.format("SELECT * FROM %s WHERE %s", tableName, whereClause);
            
            return executeQuery(sql, new ArrayList<>(fieldValues.values()), bean.getAdapterClass());
        } catch (Exception e) {
            logger.error("Query failed", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public T queryOne(T bean) {
        List<T> results = query(bean);
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public T queryById(Object id) {
        try {
            String sql = String.format("SELECT * FROM %s WHERE %s = ?", 
                tableName, toColumnName(primaryKey));
            
            List<T> results = executeQuery(sql, Collections.singletonList(id), null);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.error("Query by id failed", e);
            return null;
        }
    }
    
    @Override
    public List<T> queryAll() {
        try {
            String sql = String.format("SELECT * FROM %s", tableName);
            return executeQuery(sql, Collections.emptyList(), null);
        } catch (Exception e) {
            logger.error("Query all failed", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<T> queryWithCondition(T bean, QueryCondition condition) {
        if (condition == null || condition.isEmpty()) {
            return query(bean);
        }
        
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName).append(" WHERE 1=1");
            List<Object> params = new ArrayList<>();
            
            for (Condition cond : condition.getConditions()) {
                String column = toColumnName(cond.getField());
                sql.append(cond.isAnd() ? " AND " : " OR ");
                
                if (cond.getOperator() == Operator.IS_NULL || cond.getOperator() == Operator.IS_NOT_NULL) {
                    sql.append(column).append(" ").append(cond.getOperator().getSql());
                } else if (cond.getOperator() == Operator.IN) {
                    List<?> values = (List<?>) cond.getValue();
                    String placeholders = values.stream().map(v -> "?").collect(Collectors.joining(", "));
                    sql.append(column).append(" IN (").append(placeholders).append(")");
                    params.addAll(values);
                } else {
                    sql.append(column).append(" ").append(cond.getOperator().getSql()).append(" ?");
                    params.add(cond.getValue());
                }
            }
            
            if (condition.getOrderByField() != null) {
                sql.append(" ORDER BY ").append(toColumnName(condition.getOrderByField()))
                   .append(condition.isOrderAsc() ? " ASC" : " DESC");
            }
            
            if (condition.getLimit() != null) {
                sql.append(" LIMIT ").append(condition.getLimit());
            }
            
            if (condition.getOffset() != null) {
                sql.append(" OFFSET ").append(condition.getOffset());
            }
            
            return executeQuery(sql.toString(), params, bean.getAdapterClass());
        } catch (Exception e) {
            logger.error("Query with condition failed", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public T queryOneWithCondition(T bean, QueryCondition condition) {
        List<T> results = queryWithCondition(bean, condition);
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public int batchSave(List<T> beans) {
        int count = 0;
        for (T bean : beans) {
            if (save(bean) > 0) count++;
        }
        return count;
    }
    
    @Override
    public int batchDelete(List<T> beans) {
        int count = 0;
        for (T bean : beans) {
            if (delete(bean) > 0) count++;
        }
        return count;
    }
    
    @Override
    public long count(T bean) {
        try {
            String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
            return executeCount(sql, Collections.emptyList());
        } catch (Exception e) {
            logger.error("Count failed", e);
            return 0;
        }
    }
    
    @Override
    public long countWithCondition(T bean, QueryCondition condition) {
        if (condition == null || condition.isEmpty()) {
            return count(bean);
        }
        
        try {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName).append(" WHERE 1=1");
            List<Object> params = new ArrayList<>();
            
            for (Condition cond : condition.getConditions()) {
                sql.append(cond.isAnd() ? " AND " : " OR ");
                sql.append(toColumnName(cond.getField())).append(" ").append(cond.getOperator().getSql()).append(" ?");
                params.add(cond.getValue());
            }
            
            return executeCount(sql.toString(), params);
        } catch (Exception e) {
            logger.error("Count with condition failed", e);
            return 0;
        }
    }
    
    @Override
    public boolean exists(T bean) {
        return count(bean) > 0;
    }
    
    // ========== 辅助方法 ==========
    
    private List<T> executeQuery(String sql, List<Object> params, Class<T> adapterClass) throws SQLException {
        List<T> results = new ArrayList<>();
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    T instance = createInstance(adapterClass);
                    populateBean(instance, rs);
                    results.add(instance);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return results;
    }
    
    private long executeCount(String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }
    
    private T createInstance(Class<T> adapterClass) throws Exception {
        if (adapterClass == null) {
            // 尝试从注册表获取
            throw new IllegalArgumentException("Adapter class is null");
        }
        
        return (T) FrameworkRegistry.getInstance().createAdapter(adapterClass);
    }
    
    private void populateBean(T bean, ResultSet rs) throws Exception {
        for (String fieldName : getAllFieldNames(bean.getClass())) {
            String columnName = toColumnName(fieldName);
            try {
                Object value = rs.getObject(columnName);
                if (value != null) {
                    invokeSetter(bean, fieldName, value);
                }
            } catch (SQLException e) {
                // 列不存在，跳过
            }
        }
    }
    
    private Map<String, Object> extractNonNullFields(T bean) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        
        for (String fieldName : getAllFieldNames(bean.getClass())) {
            Object value = invokeGetter(bean, fieldName);
            if (value != null) {
                result.put(toColumnName(fieldName), value);
            }
        }
        
        return result;
    }
    
    private Object invokeGetter(T bean, String fieldName) throws Exception {
        String key = bean.getClass().getName() + "." + fieldName;
        Method getter = getterCache.computeIfAbsent(key, k -> findGetter(bean.getClass(), fieldName));
        
        if (getter != null) {
            return getter.invoke(bean);
        }
        return null;
    }
    
    private void invokeSetter(T bean, String fieldName, Object value) throws Exception {
        String key = bean.getClass().getName() + "." + fieldName;
        Method setter = setterCache.computeIfAbsent(key, k -> findSetter(bean.getClass(), fieldName));
        
        if (setter != null) {
            Class<?> paramType = setter.getParameterTypes()[0];
            Object convertedValue = convertType(value, paramType);
            setter.invoke(bean, convertedValue);
        }
    }
    
    private Method findGetter(Class<?> clazz, String fieldName) {
        String getterName = "get" + capitalize(fieldName);
        String boolGetterName = "is" + capitalize(fieldName);
        
        for (Method method : clazz.getMethods()) {
            if ((method.getName().equals(getterName) || method.getName().equals(boolGetterName))
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(void.class)) {
                return method;
            }
        }
        return null;
    }
    
    private Method findSetter(Class<?> clazz, String fieldName) {
        String setterName = "set" + capitalize(fieldName);
        
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }
    
    private List<String> getAllFieldNames(Class<?> clazz) {
        List<String> fields = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            String name = method.getName();
            if (name.startsWith("get") && !name.equals("getClass") 
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(void.class)) {
                String fieldName = uncapitalize(name.substring(3));
                fields.add(fieldName);
            } else if (name.startsWith("is") && method.getParameterCount() == 0
                       && method.getReturnType().equals(boolean.class)) {
                String fieldName = uncapitalize(name.substring(2));
                fields.add(fieldName);
            }
        }
        return fields;
    }
    
    private String toColumnName(String fieldName) {
        return columnMapping.getOrDefault(fieldName, camelToUnderscore(fieldName));
    }
    
    private String camelToUnderscore(String camelCase) {
        StringBuilder result = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append("_").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString().toUpperCase();
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    private String uncapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
    
    private Object convertType(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;
        
        if (targetType.equals(String.class)) {
            return value.toString();
        } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
            return Integer.valueOf(value.toString());
        } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
            return Long.valueOf(value.toString());
        } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
            return Boolean.valueOf(value.toString());
        } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
            return Double.valueOf(value.toString());
        }
        
        return value;
    }
}