package com.easyquery.core.adapter;

import com.easyquery.core.enums.DatabaseType;
import com.easyquery.core.model.DataSourceConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库适配器接口
 */
public interface DatabaseAdapter {
    
    /**
     * 初始化数据库适配器
     * @param config 数据库适配器配置
     */
    void init(DataSourceConfig config);

    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    Connection getConnection() throws SQLException;
    
    /**
     * 释放数据库连接
     * @param connection 数据库连接
     * @throws SQLException SQL异常
     */
    void releaseConnection(Connection connection) throws SQLException;
    
    /**
     * 获取数据库类型
     * @return 数据库类型
     */
    DatabaseType getDatabaseType();
    
    /**
     * 测试数据库连接
     * @return 是否连接成功
     */
    boolean testConnection();

    /**
     * 获取所有表名
     * @return 所有表名
     */
    List<String> getTables() throws SQLException;
}
