package com.easyquery.core.parser;

import java.util.List;
import java.util.Map;

/**
 * SQL解析结果
 */
public class SqlParseResult {
    
    /**
     * SQL类型
     */
    private SqlType sqlType;
    
    /**
     * 表名列表
     */
    private List<String> tableNames;
    
    /**
     * 分片键
     */
    private String shardingKey;
    
    /**
     * 分片键值
     */
    private Object shardingKeyValue;
    
    /**
     * SQL参数
     */
    private Map<String, Object> params;
    
    /**
     * 原始SQL
     */
    private String originalSql;
    
    public SqlParseResult() {
    }
    
    public SqlType getSqlType() {
        return sqlType;
    }
    
    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }
    
    public List<String> getTableNames() {
        return tableNames;
    }
    
    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }
    
    public String getShardingKey() {
        return shardingKey;
    }
    
    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }
    
    public Object getShardingKeyValue() {
        return shardingKeyValue;
    }
    
    public void setShardingKeyValue(Object shardingKeyValue) {
        this.shardingKeyValue = shardingKeyValue;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    public String getOriginalSql() {
        return originalSql;
    }
    
    public void setOriginalSql(String originalSql) {
        this.originalSql = originalSql;
    }
}
