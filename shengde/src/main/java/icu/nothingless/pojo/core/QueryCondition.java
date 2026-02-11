package icu.nothingless.pojo.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询条件构建器 - 支持链式调用
 */
public class QueryCondition {
    
    public enum Operator {
        EQ("="), NEQ("!="), GT(">"), GTE(">="), LT("<"), LTE("<="), 
        LIKE("LIKE"), IN("IN"), IS_NULL("IS NULL"), IS_NOT_NULL("IS NOT NULL");
        
        private final String sql;
        Operator(String sql) { this.sql = sql; }
        public String getSql() { return sql; }
    }
    
    public static class Condition {
        private String field;
        private Operator operator;
        private Object value;
        private boolean and; // true=AND, false=OR
        
        public Condition(String field, Operator operator, Object value, boolean and) {
            this.field = field;
            this.operator = operator;
            this.value = value;
            this.and = and;
        }
        
        // 添加 Getter 方法
        public String getField() { return field; }
        public Operator getOperator() { return operator; }
        public Object getValue() { return value; }
        public boolean isAnd() { return and; }
    }
    
    private final List<Condition> conditions = new ArrayList<>();
    private String orderByField;
    private boolean orderAsc = true;
    private Integer limit;
    private Integer offset;
    
    public QueryCondition eq(String field, Object value) {
        conditions.add(new Condition(field, Operator.EQ, value, true));
        return this;
    }
    
    public QueryCondition neq(String field, Object value) {
        conditions.add(new Condition(field, Operator.NEQ, value, true));
        return this;
    }
    
    public QueryCondition gt(String field, Object value) {
        conditions.add(new Condition(field, Operator.GT, value, true));
        return this;
    }
    
    public QueryCondition gte(String field, Object value) {
        conditions.add(new Condition(field, Operator.GTE, value, true));
        return this;
    }
    
    public QueryCondition lt(String field, Object value) {
        conditions.add(new Condition(field, Operator.LT, value, true));
        return this;
    }
    
    public QueryCondition lte(String field, Object value) {
        conditions.add(new Condition(field, Operator.LTE, value, true));
        return this;
    }
    
    public QueryCondition like(String field, Object value) {
        conditions.add(new Condition(field, Operator.LIKE, "%" + value + "%", true));
        return this;
    }
    
    public QueryCondition leftLike(String field, Object value) {
        conditions.add(new Condition(field, Operator.LIKE, "%" + value, true));
        return this;
    }
    
    public QueryCondition rightLike(String field, Object value) {
        conditions.add(new Condition(field, Operator.LIKE, value + "%", true));
        return this;
    }
    
    public QueryCondition in(String field, List<?> values) {
        conditions.add(new Condition(field, Operator.IN, values, true));
        return this;
    }
    
    public QueryCondition isNull(String field) {
        conditions.add(new Condition(field, Operator.IS_NULL, null, true));
        return this;
    }
    
    public QueryCondition isNotNull(String field) {
        conditions.add(new Condition(field, Operator.IS_NOT_NULL, null, true));
        return this;
    }
    
    public QueryCondition or() {
        if (!conditions.isEmpty()) {
            // 修改最后一个条件的 and 字段
            Condition last = conditions.get(conditions.size() - 1);
            conditions.set(conditions.size() - 1, 
                new Condition(last.getField(), last.getOperator(), last.getValue(), false));
        }
        return this;
    }
    
    public QueryCondition orderBy(String field, boolean asc) {
        this.orderByField = field;
        this.orderAsc = asc;
        return this;
    }
    
    public QueryCondition orderByDesc(String field) {
        return orderBy(field, false);
    }
    
    public QueryCondition limit(int limit) {
        this.limit = limit;
        return this;
    }
    
    public QueryCondition offset(int offset) {
        this.offset = offset;
        return this;
    }
    
    public QueryCondition page(int pageNum, int pageSize) {
        this.limit = pageSize;
        this.offset = (pageNum - 1) * pageSize;
        return this;
    }
    
    // Getters
    public List<Condition> getConditions() { return conditions; }
    public String getOrderByField() { return orderByField; }
    public boolean isOrderAsc() { return orderAsc; }
    public Integer getLimit() { return limit; }
    public Integer getOffset() { return offset; }
    
    public boolean isEmpty() {
        return conditions.isEmpty() && orderByField == null && limit == null;
    }
    
    public void clear() {
        conditions.clear();
        orderByField = null;
        limit = null;
        offset = null;
    }
}