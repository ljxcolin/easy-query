package com.easyquery.web.loader;

import com.easyquery.core.adapter.DatabaseAdapter;
import com.easyquery.core.adapter.DatabaseAdapterFactory;
import com.easyquery.core.enums.DatabaseType;
import com.easyquery.core.model.DataSourceConfig;
import com.easyquery.core.model.DataSourceEntry;
import com.easyquery.core.model.ShardingRuleConfig;
import com.easyquery.core.model.StrategyConfig;
import com.easyquery.web.entity.DataSourceEntity;
import com.easyquery.web.entity.ShardingRuleEntity;
import com.easyquery.web.service.DataSourceService;
import com.easyquery.web.service.ShardingRuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 应用启动加载器
 * 负责在应用启动时加载数据源和分片规则配置
 */
@Component
public class DataSourceLoader implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSourceLoader.class);
    
    @Autowired
    private DataSourceService dataSourceService;
    
    @Autowired
    private ShardingRuleService shardingRuleService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 运行时数据源映射（按数据源 ID）
    private final Map<Long, DataSourceEntry> dataSourceEntries = new ConcurrentHashMap<>();

    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("========== EasyQuery 启动加载器开始初始化 ==========");
        
        try {
            // 1. 加载所有数据源和关联的分片规则
            loadDataSources();
            
            logger.info("========== EasyQuery 启动加载器初始化完成 ==========");
        } catch (Exception e) {
            logger.error("EasyQuery 启动加载失败", e);
            throw e;
        }
    }
    
    /**
     * 加载所有数据源配置及其关联的分片规则
     */
    private void loadDataSources() {
        logger.info("开始加载数据源配置...");
        
        List<DataSourceEntity> dataSources = dataSourceService.findAll();
        logger.info("找到 {} 个数据源配置", dataSources.size());
        
        List<ShardingRuleEntity> allRules = shardingRuleService.findAll();
        logger.info("找到 {} 个分片规则配置", allRules.size());
        
        // 按数据源 ID 分组分片规则
        Map<Long, List<ShardingRuleEntity>> rulesByDataSourceId = allRules.stream()
                .collect(Collectors.groupingBy(ShardingRuleEntity::getDataSourceId));
        
        for (DataSourceEntity ds : dataSources) {
            try {
                DataSourceEntry entry = createDataSourceEntry(ds, rulesByDataSourceId.get(ds.getId()));
                dataSourceEntries.put(ds.getId(), entry);
                logger.info("数据源 [{}] 加载成功", ds.getName());
            } catch (Exception e) {
                logger.error("数据源 [{}] 加载失败", ds.getName(), e);
            }
        }
        
        logger.info("数据源配置加载完成，共加载 {} 个数据源", dataSourceEntries.size());
    }
    
    /**
     * 为数据源创建 DataSourceEntry，包含关联的分片规则
     */
    private DataSourceEntry createDataSourceEntry(DataSourceEntity ds, List<ShardingRuleEntity> rules) throws Exception {
        // 创建数据源配置
        DataSourceConfig dsConfig = new DataSourceConfig();
        BeanUtils.copyProperties(ds, dsConfig);
        
        // 创建数据库适配器
        DatabaseType dbType = determineDatabaseType(ds.getDriverClassName());
        DatabaseAdapter adapter = DatabaseAdapterFactory.createAdapter(dbType, dsConfig);
        
        // 处理分片规则
        List<ShardingRuleConfig> ruleConfigs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(rules)) {
            for (ShardingRuleEntity rule : rules) {
                ShardingRuleConfig ruleConfig = new ShardingRuleConfig();
                BeanUtils.copyProperties(rule, ruleConfig);
                ruleConfig.setStrategyConfig(parseStrategyConfig(rule.getStrategyConfig()));
                ruleConfigs.add(ruleConfig);
                
                // 不再注册分片策略，执行时从 Spring 容器获取
                logger.info("分片规则 [{}] 加载成功，类型：{}, 数据源：{}, 表：{}", 
                        rule.getName(), rule.getStrategyType(), rule.getDataSourceName(), rule.getTableName());
            }
        }
        
        // 创建 DataSourceEntry
        DataSourceEntry entry = new DataSourceEntry(ds.getId(), dsConfig, ruleConfigs);
        entry.setDatabaseAdapter(adapter);
        entry.refresh();
        
        logger.info("数据源 [{}] 的 DataSourceEntry 创建成功，类型：{}", ds.getName(), dbType);
        return entry;
    }
    
    /**
     * 根据驱动类名判断数据库类型
     */
    private DatabaseType determineDatabaseType(String driverClassName) {
        if (driverClassName.contains("mysql")) {
            return DatabaseType.MYSQL;
        } else if (driverClassName.contains("postgresql")) {
            return DatabaseType.POSTGRESQL;
        } else if (driverClassName.contains("oracle")) {
            return DatabaseType.ORACLE;
        } else if (driverClassName.contains("sqlserver")) {
            return DatabaseType.SQLSERVER;
        }
        return DatabaseType.MYSQL; // 默认返回 MySQL
    }
    
    /**
     * 解析策略配置 JSON
     */
    private StrategyConfig parseStrategyConfig(String configJson) {
        if (configJson == null || configJson.trim().isEmpty()) {
            return new StrategyConfig();
        }
        
        try {
            return objectMapper.readValue(configJson, StrategyConfig.class);
        } catch (Exception e) {
            logger.error("解析策略配置失败：{}", configJson, e);
            return new StrategyConfig();
        }
    }
    
    /**
     * 根据数据源 ID 获取 DataSourceEntry
     */
    public DataSourceEntry getDataSourceEntry(Long dataSourceId) {
        return dataSourceEntries.get(dataSourceId);
    }
    
    /**
     * 根据数据源名称获取数据库适配器
     */
    public DatabaseAdapter getDatabaseAdapter(String dataSourceName) {
        for (DataSourceEntry entry : dataSourceEntries.values()) {
            if (entry.getDataSourceConfig().getName().equals(dataSourceName)) {
                return entry.getDatabaseAdapter();
            }
        }
        return null;
    }
    
    /**
     * 根据数据源 ID 获取数据库适配器
     */
    public DatabaseAdapter getDatabaseAdapterById(Long dataSourceId) {
        DataSourceEntry entry = dataSourceEntries.get(dataSourceId);
        return entry != null ? entry.getDatabaseAdapter() : null;
    }
    
    /**
     * 添加数据源（动态）
     */
    public synchronized void addDataSource(DataSourceEntity ds) {
        if (dataSourceEntries.containsKey(ds.getId())) {
            logger.warn("数据源 [{}] 已存在，跳过添加", ds.getName());
            return;
        }
        
        try {
            // 获取关联的分片规则
            List<ShardingRuleEntity> rules = shardingRuleService.findByDataSourceId(ds.getId());
            DataSourceEntry entry = createDataSourceEntry(ds, rules);
            dataSourceEntries.put(ds.getId(), entry);
            logger.info("动态添加数据源 [{}] 成功", ds.getName());
        } catch (Exception e) {
            logger.error("动态添加数据源 [{}] 失败", ds.getName(), e);
            throw new RuntimeException("添加数据源失败", e);
        }
    }
    
    /**
     * 移除数据源（动态）
     */
    public synchronized void removeDataSource(Long dataSourceId) {
        DataSourceEntry entry = dataSourceEntries.remove(dataSourceId);
        if (entry != null) {
            logger.info("动态移除数据源 [ID:{}] 成功", dataSourceId);
        } else {
            logger.warn("数据源 [ID:{}] 不存在，无法移除", dataSourceId);
        }
    }
    
    /**
     * 更新数据源（动态）
     */
    public synchronized void updateDataSource(DataSourceEntity ds) {
        // 先移除旧的数据源
        removeDataSource(ds.getId());
        // 再添加新的数据源
        addDataSource(ds);
        logger.info("动态更新数据源 [{}] 成功", ds.getName());
    }
    
    /**
     * 添加分片规则（动态）
     */
    public synchronized void addShardingRule(ShardingRuleEntity rule) {
        // 检查数据源是否存在
        DataSourceEntry entry = dataSourceEntries.get(rule.getDataSourceId());
        if (entry == null) {
            logger.warn("数据源 [ID:{}] 不存在，无法添加分片规则", rule.getDataSourceId());
            return;
        }
        
        // 更新 DataSourceEntry 中的分片规则
        if (entry.getShardingRuleConfigs() == null) {
            // DataSourceEntry 构造函数已经初始化了空列表，这里不需要再处理
        }
        
        ShardingRuleConfig ruleConfig = new ShardingRuleConfig();
        BeanUtils.copyProperties(rule, ruleConfig);
        ruleConfig.setStrategyConfig(parseStrategyConfig(rule.getStrategyConfig()));
        entry.getShardingRuleConfigs().add(ruleConfig);
        
        logger.info("动态添加分片规则 [{}] 成功", rule.getName());
    }
    
    /**
     * 移除分片规则（动态）
     */
    public synchronized void removeShardingRule(Long dataSourceId, String tableName) {
        DataSourceEntry entry = dataSourceEntries.get(dataSourceId);
        
        if (entry == null) {
            logger.warn("数据源 [ID:{}] 不存在，无法移除分片规则", dataSourceId);
            return;
        }
        
        if (CollectionUtils.isEmpty(entry.getShardingRuleConfigs())) {
            logger.warn("数据源 [ID:{}] 无分片规则", dataSourceId);
            return;
        }
        
        boolean removed = entry.getShardingRuleConfigs().removeIf(rule -> rule.getTableName().equals(tableName));
        if (removed) {
            logger.info("动态移除分片规则 [ID:{}, 表:{}] 成功", dataSourceId, tableName);
        } else {
            logger.warn("分片规则 [ID:{}, 表:{}] 不存在，无法移除", dataSourceId, tableName);
        }
    }
    
    /**
     * 获取所有已加载的数据源 ID
     */
    public java.util.Set<Long> getLoadedDataSourceIds() {
        return dataSourceEntries.keySet();
    }
    
    /**
     * 获取所有已加载的数据源名称
     */
    public java.util.Set<String> getLoadedDataSourceNames() {
        return dataSourceEntries.values().stream()
                .map(entry -> entry.getDataSourceConfig().getName())
                .collect(Collectors.toSet());
    }
    
    /**
     * 获取所有已加载的 DataSourceEntry
     */
    public java.util.Collection<DataSourceEntry> getAllDataSourceEntries() {
        return dataSourceEntries.values();
    }
}