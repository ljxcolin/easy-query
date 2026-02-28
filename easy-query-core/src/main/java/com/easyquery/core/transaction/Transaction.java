package com.easyquery.core.transaction;

import java.sql.Connection;
import java.util.Map;

/**
 * 事务接口
 */
public interface Transaction {
    
    /**
     * 获取事务ID
     * @return 事务ID
     */
    String getTransactionId();
    
    /**
     * 获取事务状态
     * @return 事务状态
     */
    TransactionStatus getStatus();
    
    /**
     * 设置事务状态
     * @param status 事务状态
     */
    void setStatus(TransactionStatus status);
    
    /**
     * 获取事务中的数据库连接映射
     * @return 数据库连接映射
     */
    Map<String, Connection> getConnectionMap();
    
    /**
     * 添加数据库连接到事务
     * @param dataSourceName 数据源名称
     * @param connection 数据库连接
     */
    void addConnection(String dataSourceName, Connection connection);
    
    /**
     * 获取事务中的数据库连接
     * @param dataSourceName 数据源名称
     * @return 数据库连接
     */
    Connection getConnection(String dataSourceName);
}
