package com.easyquery.core.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 事务管理器接口
 */
public interface TransactionManager {
    
    /**
     * 开始事务
     * @param transactionId 事务ID
     * @return 事务对象
     * @throws SQLException SQL异常
     */
    Transaction beginTransaction(String transactionId) throws SQLException;
    
    /**
     * 提交事务
     * @param transaction 事务对象
     * @throws SQLException SQL异常
     */
    void commit(Transaction transaction) throws SQLException;
    
    /**
     * 回滚事务
     * @param transaction 事务对象
     * @throws SQLException SQL异常
     */
    void rollback(Transaction transaction) throws SQLException;
    
    /**
     * 获取事务中的数据库连接
     * @param transaction 事务对象
     * @param dataSourceName 数据源名称
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    Connection getConnection(Transaction transaction, String dataSourceName) throws SQLException;
    
    /**
     * 初始化事务管理器
     * @param config 事务管理器配置
     */
    void init(Map<String, Object> config);
}
