package com.easyquery.core.transaction;

import com.easyquery.core.transaction.impl.DefaultTransactionManager;
import java.util.Map;

/**
 * 事务管理器工厂
 */
public class TransactionManagerFactory {
    
    /**
     * 创建事务管理器实例
     * @param config 事务管理器配置
     * @return 事务管理器实例
     */
    public static TransactionManager createTransactionManager(Map<String, Object> config) {
        TransactionManager transactionManager;
        
        // 检查是否使用自定义事务管理器
        if (config.containsKey("customClassName")) {
            try {
                String className = (String) config.get("customClassName");
                Class<?> clazz = Class.forName(className);
                transactionManager = (TransactionManager) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create custom transaction manager", e);
            }
        } else {
            // 使用默认事务管理器
            transactionManager = new DefaultTransactionManager();
        }
        
        // 初始化事务管理器
        transactionManager.init(config);
        
        return transactionManager;
    }
    
    /**
     * 创建默认的事务管理器实例
     * @return 默认事务管理器实例
     */
    public static TransactionManager createDefaultTransactionManager() {
        TransactionManager transactionManager = new DefaultTransactionManager();
        transactionManager.init(java.util.Collections.emptyMap());
        return transactionManager;
    }
}
