package com.easyquery.example;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.adapter.DatabaseAdapterFactory;
import com.easyquery.core.adapter.DatabaseType;
import com.easyquery.core.config.ConfigManager;
import com.easyquery.core.config.ConfigManagerFactory;
import com.easyquery.core.monitor.LogManager;
import com.easyquery.core.monitor.LogManagerFactory;
import com.easyquery.core.monitor.MonitorManager;
import com.easyquery.core.monitor.MonitorManagerFactory;
import com.easyquery.core.parser.SqlParser;
import com.easyquery.core.parser.SqlParserFactory;
import com.easyquery.core.result.ResultHandler;
import com.easyquery.core.result.ResultHandlerFactory;
import com.easyquery.core.sharding.ShardingResult;
import com.easyquery.core.sharding.ShardingStrategy;
import com.easyquery.core.sharding.ShardingStrategyFactory;
import com.easyquery.core.sharding.ShardingStrategyType;
import com.easyquery.core.transaction.Transaction;
import com.easyquery.core.transaction.TransactionManager;
import com.easyquery.core.transaction.TransactionManagerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * easy-query 示例应用
 */
public class EasyQueryExample {
    
    public static void main(String[] args) {
        try {
            // 初始化配置管理器
            ConfigManager configManager = ConfigManagerFactory.createDefaultConfigManager();
            
            // 初始化日志管理器
            LogManager logManager = LogManagerFactory.createDefaultLogManager();
            
            // 初始化监控管理器
            MonitorManager monitorManager = MonitorManagerFactory.createDefaultMonitorManager();
            
            // 初始化分片策略
            Map<String, Object> shardingConfig = new HashMap<>();
            shardingConfig.put("dataSourceCount", 2);
            shardingConfig.put("tableCount", 4);
            ShardingStrategy shardingStrategy = ShardingStrategyFactory.createStrategy(ShardingStrategyType.HASH, shardingConfig);
            
            // 初始化数据库适配器
            Map<String, Object> dbConfig = new HashMap<>();
            Map<String, Map<String, Object>> dataSources = new HashMap<>();
            
            // 配置数据源1
            Map<String, Object> ds0Config = new HashMap<>();
            ds0Config.put("url", "jdbc:mysql://localhost:3306/db0");
            ds0Config.put("username", "root");
            ds0Config.put("password", "password");
            dataSources.put("ds0", ds0Config);
            
            // 配置数据源2
            Map<String, Object> ds1Config = new HashMap<>();
            ds1Config.put("url", "jdbc:mysql://localhost:3306/db1");
            ds1Config.put("username", "root");
            ds1Config.put("password", "password");
            dataSources.put("ds1", ds1Config);
            
            dbConfig.put("dataSources", dataSources);
            DatabaseAdapter databaseAdapter = DatabaseAdapterFactory.createAdapter(DatabaseType.MYSQL, dbConfig);
            
            // 初始化SQL解析器
            SqlParser sqlParser = SqlParserFactory.createDefaultParser();
            
            // 初始化结果处理器
            ResultHandler resultHandler = ResultHandlerFactory.createDefaultResultHandler();
            
            // 初始化事务管理器
            Map<String, Object> txConfig = new HashMap<>();
            txConfig.put("databaseAdapter", databaseAdapter);
            TransactionManager transactionManager = TransactionManagerFactory.createTransactionManager(txConfig);
            
            // 示例1：简单查询
            logManager.info("=== 示例1：简单查询 ===");
            String queryId = "query-1";
            String sql = "SELECT * FROM user WHERE id = ?";
            Object shardingKey = 1001;
            
            // 记录查询开始
            monitorManager.recordQueryStart(queryId, sql);
            long startTime = System.currentTimeMillis();
            
            // 计算分片
            ShardingResult shardingResult = shardingStrategy.doSharding(shardingKey, new HashMap<>());
            logManager.info("分片结果: {}", shardingResult);
            
            // 重写SQL
            Map<String, Object> params = new HashMap<>();
            params.put("id", shardingKey);
            String rewrittenSql = sqlParser.rewrite(sql, shardingResult, params);
            logManager.info("重写后的SQL: {}", rewrittenSql);
            
            // 执行查询
            try (Connection connection = databaseAdapter.getConnection(shardingResult.getDataSourceName());
                 PreparedStatement ps = connection.prepareStatement(rewrittenSql)) {
                ps.setObject(1, shardingKey);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String, Object>> results = resultHandler.handleResultSet(rs);
                    logManager.info("查询结果: {}", results);
                }
            }
            
            // 记录查询结束
            long elapsedTime = System.currentTimeMillis() - startTime;
            monitorManager.recordQueryEnd(queryId, elapsedTime, true, null);
            monitorManager.recordShardingOperation(queryId, shardingResult.getDataSourceName(), shardingResult.getTableName(), elapsedTime);
            
            // 示例2：事务操作
            logManager.info("\n=== 示例2：事务操作 ===");
            String txId = "tx-1";
            Transaction transaction = transactionManager.beginTransaction(txId);
            
            try {
                // 执行插入操作
                String insertSql = "INSERT INTO user (id, name, age) VALUES (?, ?, ?)";
                Object insertShardingKey = 2001;
                ShardingResult insertShardingResult = shardingStrategy.doSharding(insertShardingKey, new HashMap<>());
                
                Connection txConnection = transactionManager.getConnection(transaction, insertShardingResult.getDataSourceName());
                PreparedStatement insertPs = txConnection.prepareStatement(insertSql);
                insertPs.setObject(1, insertShardingKey);
                insertPs.setObject(2, "John Doe");
                insertPs.setObject(3, 30);
                int rows = insertPs.executeUpdate();
                logManager.info("插入操作影响行数: {}", rows);
                
                // 提交事务
                transactionManager.commit(transaction);
                logManager.info("事务提交成功");
            } catch (Exception e) {
                // 回滚事务
                transactionManager.rollback(transaction);
                logManager.error("事务执行失败", e);
            }
            
            // 打印监控指标
            logManager.info("\n=== 监控指标 ===");
            Map<String, Object> metrics = monitorManager.getMetrics();
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                logManager.info("{}: {}", entry.getKey(), entry.getValue());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
