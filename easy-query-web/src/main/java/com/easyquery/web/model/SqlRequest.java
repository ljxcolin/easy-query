package com.easyquery.web.model;

public class SqlRequest {
    private String sql;
    private Object shardingKey;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(Object shardingKey) {
        this.shardingKey = shardingKey;
    }
}
