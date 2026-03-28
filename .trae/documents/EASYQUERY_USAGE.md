# Easy-Query 完整使用指南

## 一、整体架构流程

```
┌─────────────────────────────────────────────────────────────┐
│                    应用启动 (Application Start)              │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  1. EasyQueryStartupLoader.run()                            │
│     - 从数据库加载所有数据源配置                              │
│     - 从数据库加载所有分片规则配置                            │
│     - 创建 DatabaseAdapter 并注册                            │
│     - 创建 ShardingStrategy 并注册                           │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              运行时状态 (Runtime State)                      │
│  - dataSourceAdapters: Map<数据源名称，DatabaseAdapter>      │
│  - shardingStrategies: Map<数据源：表名，ShardingStrategy>   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  2. 动态配置管理 (Dynamic Configuration)                     │
│     - 新增数据源：DataSourceController.createDataSource()   │
│     - 修改数据源：DataSourceController.updateDataSource()   │
│     - 删除数据源：DataSourceController.deleteDataSource()   │
│     - 新增分片规则：ShardingRuleController.create()         │
│     - 修改分片规则：ShardingRuleController.update()         │
│     - 删除分片规则：ShardingRuleController.delete()         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  3. SQL 执行流程 (SQL Execution)                             │
│     SqlController.execute()                                 │
│       ↓                                                     │
│     SqlExecutorService.executeQuery/executeUpdate()         │
│       ↓                                                     │
│     ① 解析 SQL → SqlParser.parse()                          │
│     ② 提取表名 → parseResult.getTables()                    │
│     ③ 获取分片策略 → startupLoader.getShardingStrategy()    │
│     ④ 计算分片 → strategy.doSharding(shardingKey)           │
│     ⑤ 重写 SQL → sqlParser.rewrite()                        │
│     ⑥ 获取连接 → adapter.getConnection()                    │
│     ⑦ 执行 SQL → PreparedStatement.execute()                │
│     ⑧ 处理结果 → resultHandler.handleResultSet()            │
│     ⑨ 返回结果                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 二、应用启动流程

### 2.1 启动入口
```java
@SpringBootApplication
public class EasyQueryApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyQueryApplication.class, args);
    }
}
```

### 2.2 自动加载配置
`EasyQueryStartupLoader` 实现 `ApplicationRunner` 接口，在应用启动时自动执行：

```java
@Component
public class EasyQueryStartupLoader implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. 加载所有数据源
        loadDataSources();
        
        // 2. 加载所有分片规则
        loadShardingRules();
    }
}
```

### 2.3 数据源加载过程
```java
private void loadDataSources() {
    List<DataSourceEntity> dataSources = dataSourceService.findAll();
    
    for (DataSourceEntity ds : dataSources) {
        if (ds.getStatus() == 1) { // 只加载启用的数据源
            createDatabaseAdapter(ds);
            dataSourceAdapters.put(ds.getName(), adapter);
        }
    }
}
```

### 2.4 分片规则加载过程
```java
private void loadShardingRules() {
    List<ShardingRuleEntity> rules = shardingRuleService.findAll();
    
    for (ShardingRuleEntity rule : rules) {
        if (rule.getStatus() == 1) { // 只加载启用的分片规则
            createShardingStrategy(rule);
            String key = rule.getDataSourceName() + ":" + rule.getTableName();
            shardingStrategies.put(key, strategy);
        }
    }
}
```

---

## 三、动态配置管理

### 3.1 新增数据源

**API**: `POST /data-sources`

**请求示例**:
```json
{
  "name": "ds_user",
  "url": "jdbc:mysql://localhost:3306/db_user",
  "username": "root",
  "password": "123456",
  "driverClassName": "com.mysql.cj.jdbc.Driver",
  "maximumPoolSize": 10,
  "minimumIdle": 5,
  "status": 1
}
```

**处理流程**:
1. 检查名称是否已存在
2. 保存到数据库
3. 如果状态为启用（status=1），动态创建 DatabaseAdapter 并注册

### 3.2 修改数据源

**API**: `PUT /data-sources/{id}`

**处理流程**:
1. 根据 ID 查找数据源
2. 更新字段
3. 保存到数据库
4. 从运行时环境移除旧的 DatabaseAdapter
5. 重新创建新的 DatabaseAdapter 并注册

### 3.3 删除数据源

**API**: `DELETE /data-sources/{id}`

**处理流程**:
1. 根据 ID 查找数据源
2. 从数据库删除
3. 从运行时环境移除 DatabaseAdapter

### 3.4 新增分片规则

**API**: `POST /sharding-rules`

**请求示例**:
```json
{
  "name": "user_order_hash",
  "strategyType": "HASH",
  "tableName": "t_order",
  "shardingColumn": "user_id",
  "dataSourceId": 1,
  "dataSourceName": "ds_user",
  "strategyConfig": "{\"shardingCount\":4,\"shardingStart\":0}",
  "status": 1
}
```

**处理流程**:
1. 检查名称是否已存在
2. 检查数据源是否存在
3. 保存到数据库
4. 如果状态为启用，解析 strategyConfig 并创建 ShardingStrategy
5. 注册到 shardingStrategies 映射（key: dataSourceName:tableName）

### 3.5 修改分片规则

**API**: `PUT /sharding-rules/{name}`

**处理流程**:
1. 根据名称查找分片规则
2. 更新字段
3. 保存到数据库
4. 从运行时环境移除旧的 ShardingStrategy
5. 重新创建新的 ShardingStrategy 并注册

---

## 四、SQL 执行流程

### 4.1 执行查询 SQL

**API**: `POST /sql/query`

**请求示例**:
```json
{
  "dataSourceName": "ds_user",
  "sql": "SELECT * FROM t_order WHERE user_id = ?",
  "shardingKey": 12345,
  "params": [12345]
}
```

**执行流程**:

```java
// 1. SqlController 接收请求
@PostMapping("/query")
public Response<List<Map<String, Object>>> executeQuery(@RequestBody SqlRequest sqlRequest) {
    return sqlExecutorService.executeQuery(
        sqlRequest.getDataSourceName(),
        sqlRequest.getSql(),
        sqlRequest.getShardingKey(),
        sqlRequest.getParams()
    );
}

// 2. SqlExecutorService 执行查询
public List<Map<String, Object>> executeQuery(...) throws SQLException {
    // ① 获取数据库适配器
    DatabaseAdapter adapter = startupLoader.getDatabaseAdapter(dataSourceName);
    
    // ② 解析 SQL
    SqlParseResult parseResult = sqlParser.parse(sql);
    String tableName = parseResult.getTables().get(0);
    
    // ③ 获取分片策略
    ShardingStrategy strategy = startupLoader.getShardingStrategy(dataSourceName, tableName);
    
    // ④ 计算分片
    if (strategy != null && shardingKey != null) {
        ShardingResult shardingResult = strategy.doSharding(shardingKey);
        // 结果：{dataSourceName: "ds_user_0", tableName: "t_order_2"}
        
        // ⑤ 重写 SQL
        String rewrittenSql = sqlParser.rewrite(sql, shardingResult);
        // "SELECT * FROM t_order_2 WHERE user_id = ?"
        
        // ⑥ 获取连接
        Connection conn = adapter.getConnection(shardingResult.getDataSourceName());
        
        // ⑦ 执行查询
        PreparedStatement stmt = conn.prepareStatement(rewrittenSql);
        // 设置参数...
        ResultSet rs = stmt.executeQuery();
        
        // ⑧ 处理结果
        List<Map<String, Object>> results = resultHandler.handleResultSet(rs);
        
        // ⑨ 返回结果
        return results;
    }
}
```

### 4.2 执行更新 SQL

**API**: `POST /sql/execute`

**请求示例**:
```json
{
  "dataSourceName": "ds_user",
  "sql": "INSERT INTO t_order (user_id, amount) VALUES (?, ?)",
  "shardingKey": 12345,
  "params": [12345, 100.00]
}
```

**执行流程**: 与查询类似，只是执行的是 `executeUpdate()`

### 4.3 自动判断 SQL 类型

**API**: `POST /sql/execute-auto`

自动判断是查询还是更新，返回相应结果。

---

## 五、分片策略详解

### 5.1 HASH 策略

**配置示例**:
```json
{
  "shardingCount": 4,
  "shardingStart": 0
}
```

**计算逻辑**:
```java
int index = Math.abs(shardingKey.hashCode() % shardingCount);
String tableName = baseTableName + "_" + (index + shardingStart);
// user_id=12345 → t_order_2 (12345 % 4 = 1)
```

### 5.2 RANGE 策略

**配置示例**:
```json
{
  "rangeStart": 0,
  "rangeStep": 1000
}
```

**计算逻辑**:
```java
int index = (shardingKey - rangeStart) / rangeStep;
String tableName = baseTableName + "_" + index;
// user_id=1500 → t_order_1 ((1500-0)/1000 = 1)
```

### 5.3 MONTH/DAY 策略

**配置示例**:
```json
{
  "padZero": true
}
```

**计算逻辑**:
```java
LocalDateTime time = (LocalDateTime) shardingKey;
String suffix = padZero ? String.format("%02d", month) : String.valueOf(month);
String tableName = baseTableName + "_" + suffix;
// 2024-03 → t_order_03 (padZero=true)
```

---

## 六、完整示例

### 6.1 场景：电商订单分库分表

**需求**:
- 2 个数据库：ds_order_0, ds_order_1
- 每个库 4 张订单表：t_order_0 ~ t_order_3
- 按 user_id 哈希分片

**步骤 1: 创建数据源**
```bash
POST /data-sources
{
  "name": "ds_order_0",
  "url": "jdbc:mysql://localhost:3306/order_db_0",
  "username": "root",
  "password": "123456",
  "driverClassName": "com.mysql.cj.jdbc.Driver",
  "status": 1
}

POST /data-sources
{
  "name": "ds_order_1",
  "url": "jdbc:mysql://localhost:3306/order_db_1",
  "username": "root",
  "password": "123456",
  "driverClassName": "com.mysql.cj.jdbc.Driver",
  "status": 1
}
```

**步骤 2: 创建分片规则**
```bash
POST /sharding-rules
{
  "name": "order_hash_rule",
  "strategyType": "HASH",
  "tableName": "t_order",
  "shardingColumn": "user_id",
  "dataSourceId": 1,
  "dataSourceName": "ds_order_0",
  "strategyConfig": "{\"shardingCount\":4,\"shardingStart\":0}",
  "status": 1
}
```

**步骤 3: 执行查询**
```bash
POST /sql/query
{
  "dataSourceName": "ds_order_0",
  "sql": "SELECT * FROM t_order WHERE user_id = ?",
  "shardingKey": 12345,
  "params": [12345]
}
```

**执行过程**:
1. user_id=12345 → 12345 % 4 = 1
2. 路由到：ds_order_0.t_order_1
3. 执行：`SELECT * FROM t_order_1 WHERE user_id = 12345`
4. 返回结果

---

## 七、监控与日志

### 7.1 查看已加载的配置

**查看数据源**:
```java
startupLoader.getLoadedDataSourceNames()
// 返回：["ds_order_0", "ds_order_1"]
```

**查看分片规则**:
```java
startupLoader.getLoadedShardingRuleKeys()
// 返回：["ds_order_0:t_order"]
```

### 7.2 日志输出

应用启动时会输出详细日志：
```
========== EasyQuery 启动加载器开始初始化 ==========
开始加载数据源配置...
找到 2 个数据源配置
数据源 [ds_order_0] 加载成功
数据源 [ds_order_1] 加载成功
数据源配置加载完成，共加载 2 个数据源
开始加载分片规则配置...
找到 1 个分片规则配置
分片规则 [order_hash_rule] 加载成功
分片规则配置加载完成，共加载 1 个分片规则
========== EasyQuery 启动加载器初始化完成 ==========
```

---

## 八、注意事项

1. **数据源名称唯一性**: 数据源名称必须唯一，不能重复
2. **分片规则命名**: 分片规则按名称管理，名称不能重复
3. **状态管理**: status=1 表示启用，status=0 表示禁用
4. **动态更新**: 修改配置后会自动重新加载到运行时环境
5. **错误处理**: 创建/更新失败时会回滚事务
6. **连接管理**: 使用 HikariCP 连接池，自动管理连接生命周期
7. **SQL 注入**: 使用 PreparedStatement 防止 SQL 注入
8. **事务支持**: 暂未实现分布式事务（待扩展）

---

## 九、扩展方向

1. **事务管理**: 实现跨分片的分布式事务
2. **读写分离**: 支持主从数据库读写分离
3. **结果合并**: 支持跨分片的查询结果合并、排序、分页
4. **聚合函数**: 支持 SUM、COUNT、AVG 等聚合函数的跨分片计算
5. **SQL 解析增强**: 支持更复杂的 SQL（JOIN、子查询等）
6. **监控指标**: 增加性能监控和统计指标
7. **配置热更新**: 支持配置文件热更新

---

## 十、总结

通过以上流程，Easy-Query 实现了：

✅ **自动加载**: 应用启动时自动加载数据源和分片规则  
✅ **动态配置**: 支持运行时动态增删改配置  
✅ **透明分片**: 应用层无需关心分片细节  
✅ **自动路由**: 根据分片键自动路由到正确的分片  
✅ **SQL 重写**: 自动替换表名执行 SQL  
✅ **灵活扩展**: 支持自定义分片策略和数据库类型

完整的分库分表解决方案！
