package com.easyquery.core.adapter;

import com.easyquery.core.adapter.impl.MySqlAdapter;
import com.easyquery.core.adapter.impl.PostgreSqlAdapter;
import java.util.Map;

/**
 * 数据库适配器工厂
 */
public class DatabaseAdapterFactory {
    
    /**
     * 创建数据库适配器实例
     * @param databaseType 数据库类型
     * @param config 数据库适配器配置
     * @return 数据库适配器实例
     */
    public static DatabaseAdapter createAdapter(DatabaseType databaseType, Map<String, Object> config) {
        DatabaseAdapter adapter;
        
        switch (databaseType) {
            case MYSQL:
                adapter = new MySqlAdapter();
                break;
            case POSTGRESQL:
                adapter = new PostgreSqlAdapter();
                break;
            case ORACLE:
                // 后续实现
                throw new UnsupportedOperationException("Oracle adapter not implemented yet");
            case SQLSERVER:
                // 后续实现
                throw new UnsupportedOperationException("SQL Server adapter not implemented yet");
            case OTHER:
                // 自定义适配器
                if (config.containsKey("customClassName")) {
                    try {
                        String className = (String) config.get("customClassName");
                        Class<?> clazz = Class.forName(className);
                        adapter = (DatabaseAdapter) clazz.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create custom database adapter", e);
                    }
                } else {
                    throw new IllegalArgumentException("Custom database adapter class name is required");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
        
        // 初始化适配器
        adapter.init(config);
        
        return adapter;
    }
    
    /**
     * 创建默认的数据库适配器实例
     * @return 默认数据库适配器实例
     */
    public static DatabaseAdapter createDefaultAdapter() {
        DatabaseAdapter adapter = new MySqlAdapter();
        adapter.init(java.util.Collections.emptyMap());
        return adapter;
    }
}
