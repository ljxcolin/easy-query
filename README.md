# easy-query

## 项目简介

easy-query 是一个开源的分库分表查询工具，旨在简化分布式数据库环境下的查询操作，提供透明的数据分片能力，支持多种数据库类型和分片策略。

## 主要特性

- **多种分片策略**：支持哈希分片、范围分片、列表分片等多种分片策略
- **多数据库支持**：支持 MySQL、PostgreSQL 等多种主流数据库
- **SQL 解析与重写**：自动解析和重写 SQL 语句，实现透明的分片查询
- **结果合并与排序**：支持多分片查询结果的合并、排序和分页
- **分布式事务**：支持跨分片的分布式事务
- **配置管理**：支持配置文件和动态配置，支持配置热更新
- **监控与日志**：提供详细的监控指标和日志记录
- **易于扩展**：模块化设计，易于扩展和定制

## 快速入门

### 环境要求

- Java 11+
- Maven 3.6+
- 数据库（MySQL 5.7+ 或 PostgreSQL 9.6+）

### 安装

1. 克隆项目

```bash
git clone https://github.com/easy-query/easy-query.git
cd easy-query
```

2. 构建项目

```bash
mvn clean install
```

### 基本使用

#### 1. 初始化配置

```java
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
```

#### 2. 执行查询

```java
// 计算分片
String sql = "SELECT * FROM user WHERE id = ?";
Object shardingKey = 1001;
ShardingResult shardingResult = shardingStrategy.doSharding(shardingKey, new HashMap<>());

// 重写SQL
Map<String, Object> params = new HashMap<>();
params.put("id", shardingKey);
String rewrittenSql = sqlParser.rewrite(sql, shardingResult, params);

// 执行查询
try (Connection connection = databaseAdapter.getConnection(shardingResult.getDataSourceName());
     PreparedStatement ps = connection.prepareStatement(rewrittenSql)) {
    ps.setObject(1, shardingKey);
    try (ResultSet rs = ps.executeQuery()) {
        List<Map<String, Object>> results = resultHandler.handleResultSet(rs);
        System.out.println("查询结果: " + results);
    }
}
```

#### 3. 事务操作

```java
// 初始化事务管理器
Map<String, Object> txConfig = new HashMap<>();
txConfig.put("databaseAdapter", databaseAdapter);
TransactionManager transactionManager = TransactionManagerFactory.createTransactionManager(txConfig);

// 开始事务
Transaction transaction = transactionManager.beginTransaction("tx-1");

try {
    // 执行操作
    // ...
    
    // 提交事务
    transactionManager.commit(transaction);
} catch (Exception e) {
    // 回滚事务
    transactionManager.rollback(transaction);
    throw e;
}
```

## 高级特性

### 自定义分片策略

1. 实现 `ShardingStrategy` 接口

```java
public class CustomShardingStrategy implements ShardingStrategy {
    @Override
    public ShardingResult doSharding(Object shardingKey, Map<String, Object> shardingParams) {
        // 自定义分片逻辑
        // ...
        return new ShardingResult("ds0", "t0");
    }
    
    @Override
    public ShardingStrategyType getType() {
        return ShardingStrategyType.CUSTOM;
    }
    
    @Override
    public void init(Map<String, Object> config) {
        // 初始化
    }
}
```

2. 使用自定义分片策略

```java
Map<String, Object> config = new HashMap<>();
config.put("customClassName", "com.example.CustomShardingStrategy");
ShardingStrategy strategy = ShardingStrategyFactory.createStrategy(ShardingStrategyType.CUSTOM, config);
```

### 配置管理

#### 使用配置文件

创建 `easy-query.properties` 文件：

```properties
# 分片配置
sharding.defaultStrategy=HASH
sharding.dataSourceCount=2
sharding.tableCount=4

# 数据库配置
database.defaultType=MYSQL

# 连接池配置
connection.pool.maximumSize=10
connection.pool.minimumIdle=5
```

加载配置文件：

```java
Map<String, Object> config = new HashMap<>();
config.put("configPath", "easy-query.properties");
ConfigManager configManager = ConfigManagerFactory.createConfigManager(config);
```

#### 动态配置

```java
// 更新配置
configManager.updateConfig("sharding.dataSourceCount", 4);

// 获取配置
int dataSourceCount = configManager.getConfig("sharding.dataSourceCount", 2);
```

### 监控

```java
// 初始化监控管理器
MonitorManager monitorManager = MonitorManagerFactory.createDefaultMonitorManager();

// 记录查询开始
monitorManager.recordQueryStart("query-1", "SELECT * FROM user WHERE id = ?");

// 执行查询
// ...

// 记录查询结束
monitorManager.recordQueryEnd("query-1", 100, true, null);

// 获取监控指标
Map<String, Object> metrics = monitorManager.getMetrics();
System.out.println("监控指标: " + metrics);
```

## 架构设计

### 核心模块

- **core**: 核心功能模块
  - **sharding**: 分片相关功能
  - **adapter**: 数据库适配器
  - **parser**: SQL 解析与重写
  - **result**: 结果处理
  - **transaction**: 事务管理
  - **config**: 配置管理
  - **monitor**: 监控与日志

### 模块依赖

```
easy-query-core
├── JSqlParser (SQL解析)
├── HikariCP (连接池)
├── Apache Commons Configuration (配置管理)
├── Log4j2 (日志)
└── JUnit 5, Mockito (测试)
```

## 示例

查看 `easy-query-example` 模块中的 `EasyQueryExample.java` 文件，了解完整的使用示例。

## 贡献

欢迎贡献代码、报告问题或提出建议！

### 开发流程

1. Fork 项目
2. 创建分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

### 代码规范

- 遵循 Java 代码规范
- 编写清晰的文档和注释
- 添加单元测试

## 许可证

本项目采用 Apache 2.0 许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目地址: https://github.com/easy-query/easy-query
- 问题反馈: https://github.com/easy-query/easy-query/issues
