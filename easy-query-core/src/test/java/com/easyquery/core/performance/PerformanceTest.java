package com.easyquery.core.performance;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.adapter.DatabaseAdapterFactory;
import com.easyquery.core.adapter.DatabaseType;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * easy-query 性能测试
 */
public class PerformanceTest {
    
    private static ShardingStrategy shardingStrategy;
    private static SqlParser sqlParser;
    private static ResultHandler resultHandler;
    private static TransactionManager transactionManager;
    private static DatabaseAdapter databaseAdapter;
    
    @BeforeAll
    public static void setUp() {
        // 初始化分片策略
        Map<String, Object> shardingConfig = new HashMap<>();
        shardingConfig.put("dataSourceCount", 2);
        shardingConfig.put("tableCount", 4);
        shardingStrategy = ShardingStrategyFactory.createStrategy(ShardingStrategyType.HASH, shardingConfig);
        
        // 初始化SQL解析器
        sqlParser = SqlParserFactory.createDefaultParser();
        
        // 初始化结果处理器
        resultHandler = ResultHandlerFactory.createDefaultResultHandler();
        
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
        databaseAdapter = DatabaseAdapterFactory.createAdapter(DatabaseType.MYSQL, dbConfig);
        
        // 初始化事务管理器
        Map<String, Object> txConfig = new HashMap<>();
        txConfig.put("databaseAdapter", databaseAdapter);
        transactionManager = TransactionManagerFactory.createTransactionManager(txConfig);
    }
    
    /**
     * 测试分片策略性能
     */
    @Test
    public void testShardingStrategyPerformance() {
        int iterations = 100000;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            Object shardingKey = i;
            ShardingResult result = shardingStrategy.doSharding(shardingKey, new HashMap<>());
        }
        
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Sharding Strategy Performance Test:");
        System.out.println("Iterations: " + iterations);
        System.out.println("Elapsed Time: " + elapsedTime + " ms");
        System.out.println("Operations per second: " + (iterations * 1000.0 / elapsedTime));
        System.out.println();
    }
    
    /**
     * 测试SQL解析与重写性能
     */
    @Test
    public void testSqlParserPerformance() {
        int iterations = 10000;
        String sql = "SELECT * FROM user WHERE id = ?";
        Object shardingKey = 1001;
        ShardingResult shardingResult = shardingStrategy.doSharding(shardingKey, new HashMap<>());
        Map<String, Object> params = new HashMap<>();
        params.put("id", shardingKey);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            String rewrittenSql = sqlParser.rewrite(sql, shardingResult, params);
        }
        
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("SQL Parser Performance Test:");
        System.out.println("Iterations: " + iterations);
        System.out.println("Elapsed Time: " + elapsedTime + " ms");
        System.out.println("Operations per second: " + (iterations * 1000.0 / elapsedTime));
        System.out.println();
    }
    
    /**
     * 测试结果处理性能
     */
    @Test
    public void testResultHandlerPerformance() {
        // 由于需要实际的ResultSet，这里简化测试
        System.out.println("Result Handler Performance Test:");
        System.out.println("Note: Requires actual ResultSet for comprehensive testing");
        System.out.println();
    }
    
    /**
     * 测试并发性能
     */
    @Test
    public void testConcurrentPerformance() throws InterruptedException {
        int threadCount = 10;
        int queriesPerThread = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < queriesPerThread; j++) {
                        Object shardingKey = threadId * queriesPerThread + j;
                        ShardingResult result = shardingStrategy.doSharding(shardingKey, new HashMap<>());
                        String sql = "SELECT * FROM user WHERE id = ?";
                        Map<String, Object> params = new HashMap<>();
                        params.put("id", shardingKey);
                        String rewrittenSql = sqlParser.rewrite(sql, result, params);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        int totalQueries = threadCount * queriesPerThread;
        
        System.out.println("Concurrent Performance Test:");
        System.out.println("Threads: " + threadCount);
        System.out.println("Queries per thread: " + queriesPerThread);
        System.out.println("Total queries: " + totalQueries);
        System.out.println("Elapsed Time: " + elapsedTime + " ms");
        System.out.println("Queries per second: " + (totalQueries * 1000.0 / elapsedTime));
        System.out.println();
        
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }
}
