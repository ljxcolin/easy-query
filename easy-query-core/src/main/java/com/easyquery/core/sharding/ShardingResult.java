package com.easyquery.core.sharding;

/**
 * 分片结果
 */
public class ShardingResult {
    
    /**
     * 数据源名称
     */
    private String dataSourceName;
    
    /**
     * 表名
     */
    private String tableName;
    
    public ShardingResult() {
    }
    
    public ShardingResult(String dataSourceName, String tableName) {
        this.dataSourceName = dataSourceName;
        this.tableName = tableName;
    }
    
    public String getDataSourceName() {
        return dataSourceName;
    }
    
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    @Override
    public String toString() {
        return "ShardingResult{" +
                "dataSourceName='" + dataSourceName + '\'' +
                ", tableName='" + tableName + '\'' +
                '}';
    }
}
