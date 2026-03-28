package com.easyquery.core.sharding;

import com.easyquery.core.enums.ConditionType;

/**
 * SQL条件
 */
public class SqlCondition {
    
    /**
     * 列名
     */
    private String columnName;
    
    /**
     * 条件类型
     */
    private ConditionType conditionType;

    /**
     * 值
     */
    private Object value;
    
    public SqlCondition() {
    }
    
    public SqlCondition(String columnName, ConditionType conditionType, Object value) {
        this.columnName = columnName;
        this.conditionType = conditionType;
        this.value = value;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    public ConditionType getConditionType() {
        return conditionType;
    }
    
    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "SqlCondition{" +
                "columnName='" + columnName + '\'' +
                ", conditionType=" + conditionType +
                ", value=" + value +
                '}';
    }
}
