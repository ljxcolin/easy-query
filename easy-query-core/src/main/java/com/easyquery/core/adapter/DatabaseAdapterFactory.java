package com.easyquery.core.adapter;

import com.easyquery.core.adapter.impl.MySqlAdapter;
import com.easyquery.core.adapter.impl.PostgreSqlAdapter;
import com.easyquery.core.enums.DatabaseType;
import com.easyquery.core.model.DataSourceConfig;

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
    public static DatabaseAdapter createAdapter(DatabaseType databaseType, DataSourceConfig config) {
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
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
        
        // 初始化适配器
        adapter.init(config);
        
        return adapter;
    }

}
