package com.easyquery.web.model;

/**
 * SQL 请求模型
 */
public class SqlRequest {
    /**
     * 数据源名称
     */
    private Long dataSourceId;
    
    /**
     * SQL 语句
     */
    private String sql;

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

}
