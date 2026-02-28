package com.easyquery.core.adapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 数据库适配器接口
 */
public interface DatabaseAdapter {
    
    /**
     * 获取数据库连接
     * @param dataSourceName 数据源名称
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    Connection getConnection(String dataSourceName) throws SQLException;
    
    /**
     * 释放数据库连接
     * @param connection 数据库连接
     * @throws SQLException SQL异常
     */
    void releaseConnection(Connection connection) throws SQLException;
    
    /**
     * 初始化数据库适配器
     * @param config 数据库适配器配置
     */
    void init(Map<String, Object> config);
    
    /**
     * 获取数据库类型
     * @return 数据库类型
     */
    DatabaseType getDatabaseType();
    
    /**
     * 测试数据库连接
     * @param dataSourceName 数据源名称
     * @return 是否连接成功
     */
    boolean testConnection(String dataSourceName);
}
