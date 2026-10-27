# AGENTS.md

> 面向在本仓库里写代码的 AI 助手（人类贡献者同样适用）。
> 目标：最短篇幅说清三件事 —— **这是什么、东西在哪、改代码必须守哪些规矩**。
> 与代码冲突时以代码为准，并顺手把本文件改对。

---

## 1. 项目定位

easy-query 是一个 **Java 21 + Spring Boot 3 的分库分表查询平台**，不是一个纯 jar 工具库：

- 数据源与分片规则以**数据库表**（`data_source` / `sharding_rule`）为唯一事实来源；
- 启动时由 `DataSourceLoader` 装配成内存态，运行期增删改**即时生效，无需重启**；
- 对外是 REST API + Vue 管理台，本质是一个「带透明分片能力的 Web SQL 客户端」。

**已实现**：HASH / RANGE / YEAR 三种分片策略 + SELECT 改写（多分片拼 `UNION ALL`）。
**仍是骨架**：MONTH / DAY / YEAR_MONTH 策略、分布式事务、监控，见 §9。

---

## 2. 技术栈（以 pom / package.json 为准，勿凭记忆写版本）

| 层 | 组件 | 版本 |
|---|---|---|
| 语言 | Java | 21（父 pom 固定，勿降级） |
| 构建 | Maven | 3.6+，**仓库无 mvnw** |
| 核心库 | JSqlParser | 4.6（`net.sf.jsqlparser.*`） |
| 连接池 | HikariCP | 5.0.1 |
| 日志 | Log4j2 + slf4j | 2.22.0 / Boot 3.2.0 |
| 后端 | Spring Boot（web / data-jpa / actuator） | 3.2.0 |
| 驱动 | mysql-connector-j | 8.0.33 |
| 测试 | JUnit 5 / Mockito | 5.9.1 / 5.3.1 |
| 前端 | Vue / Vite / TS | 3.4 / 5.2 / 5.9 |
| 前端 UI | Element Plus / Monaco | 2.6 / 0.46 |

---

## 3. 仓库结构

```
easy-query/
├── pom.xml                          # 父 POM：JDK 21、依赖版本、模块聚合
├── AGENTS.md                        # 本文件
├── docs/
│   ├── EASYQUERY_USAGE.md           # 使用说明（API 已过时，见 §9）
│   ├── easy_query_sharding_plan.md  # 早期实现计划
│   └── sql/                         # easy-query.sql=元数据库；db0/db1.sql=分片库样例
├── easy-query-core/                 # 核心库，框架无关
│   └── src/main/java/com/easyquery/core/
│       ├── sharding/                # ShardingStrategy 接口 + HASH/RANGE/YEAR + Factory + SqlCondition
│       ├── parser/                  # JSqlParser 封装，SqlParseResult
│       ├── rewriter/                # SqlRewriter + SelectSqlRewriter（★ 改写核心）
│       ├── adapter/                 # DatabaseAdapter + MySQL/PostgreSQL 实现（HikariCP）
│       ├── result/                  # ResultHandler：ResultSet→List<Map>，含合并/排序/分页/聚合
│       ├── model/                   # DataSourceEntry（★ 运行时单元）、*Config
│       ├── enums/                   # StrategyType / ConditionType / SqlType / DatabaseType
│       ├── utils/                   # TableUtils（表名拼拆，唯一入口）、UrlUtils
│       └── config|monitor|transaction/   # 骨架实现，暂无调用方
├── easy-query-web/                  # Spring Boot 服务
│   └── src/main/java/com/easyquery/web/
│       ├── loader/DataSourceLoader.java   # ★ 启动装配 + 运行时热更新
│       ├── config/EasyQueryConfig.java    # 核心组件 Bean 声明
│       ├── controller/              # DataSource / ShardingRule / Sql / SqlQuery
│       ├── service/{iface,impl}/    # SqlExecutorService ★ 执行链路
│       ├── entity/ repository/      # JPA（data_source / sharding_rule / sql_query）
│       └── model/                   # Response<T>、SqlRequest
├── easy-query-example/              # 命令行示例
└── easy-query-ui/                   # Vue 3 管理台
    └── src/
        ├── views/                   # Home / DataSource / DataSourceDetail / ShardingRule / SqlQuery
        ├── api/                     # request.ts（唯一 axios 实例）+ 各资源封装
        ├── router/index.ts          # hash 路由
        └── types/index.ts           # 与后端 DTO 对齐的 TS 类型
```

---

## 4. 核心概念（动手前必须看懂）

| 概念 | 说明 |
|---|---|
| **逻辑表 / 物理表** | 逻辑表 `t_order` → 物理表 `t_order_0..N`。拼/拆只走 `TableUtils` |
| **DataSourceEntry** | 运行时唯一单元：`Map<Long, DataSourceEntry>`，key 是**数据源 ID**。内含 `DataSourceConfig`、`DatabaseAdapter`、`List<ShardingRuleConfig>`（已填充 `availableTableNames`）、`tables` |
| **规则匹配** | 同一数据源内按**逻辑表名精确匹配**，大小写敏感；一个数据源可挂多条规则 |
| **分片结果** | 策略返回的是**后缀列表**（如 `["1","2"]`），不是完整表名 |
| **后缀数值** | RANGE/YEAR 把分片键统一换算成「后缀数值」再与可用表比对：RANGE = `floor((v-rangeStart)/rangeStep)+shardingStart`，YEAR = 年份 |
| **策略实例** | `ShardingStrategyFactory` 用静态 `Map.of` 持有**单例**，所有数据源共用 → 策略必须无状态 |
| **绑定表** | JOIN 右表也有规则且 `shardingColumn` 与主表相同 → 沿用主表后缀；否则右表不改写 |
| **StrategyConfig** | HASH→`shardingCount`/`shardingStart`；RANGE→`rangeStart`/`rangeStep`；时间类→`padZero` |

---

## 5. 一次查询的时序

```
POST /api/sql/query  {dataSourceId, sql}
 └─ SqlController → SqlExecutorService
     ├─ DataSourceLoader.getDataSourceEntry(id)          // 无则抛 SQLException
     ├─ SqlParser.parse(sql)                             // JSqlParser → SqlParseResult
     ├─ SelectSqlRewriter.rewrite(entry, parseResult)
     │   ├─ 在 entry 里按 FROM 表名找 ShardingRuleConfig
     │   ├─ 递归解析 WHERE（AND/Parenthesis）→ List<SqlCondition>
     │   ├─ strategy.doSharding(rule, conditions) → 后缀列表
     │   ├─ 每个后缀 clone 一份 SQL、替换表名、按需改 JOIN 右表
     │   └─ 多分片 → buildUnionSql(UNION ALL)
     └─ adapter.getConnection() → PreparedStatement → ResultHandler.handleResultSet
```

⚠️ 分片键来自 **SQL 的 WHERE 条件**，不是请求参数。找不到分片条件时退化为「全部分片」。

---

## 6. 构建与运行

```bash
# 前置：JDK 21、Maven 3.6+（无 wrapper，需自行安装）、Node 18+
mvn clean install -DskipTests            # 全量构建
mvn -pl easy-query-core -am test         # 只测核心模块
mvn -pl easy-query-web spring-boot:run   # 后端 :8080

cd easy-query-ui
npm install
npm run dev          # 前端 :3000，/api 经 vite 代理重写后转发到 :8080
npm run type-check   # vue-tsc，改 TS 后必跑
```

数据库：先执行 `docs/sql/easy-query.sql` 建元数据库；分片库需自行创建 `user_0..user_N` 等物理表（`db0.sql`/`db1.sql` 只是单表空壳）。

> 本机事实：Java 21.0.9 ✓、Node 22 ✓、Maven **未安装**、`node_modules`/`target` 均不存在 → 首次构建会下载全部依赖。

---

## 7. 编码约束（硬性）

### 7.1 模块边界

1. **core 不依赖 Spring 容器**：不写 `@Component`/`@Autowired`，日志只用 slf4j API。需要容器能力时在 web 层包一层。
2. core 新增可注入组件遵循 `接口 + impl/ 实现 + XxxFactory`，并在 `EasyQueryConfig` 注册 Bean。
   例外：`DatabaseAdapter`、`ShardingStrategy` 由工厂按配置动态创建，**不进容器**。
3. web 层只做编排与 IO，**分片算法、SQL 改写一律留在 core**。

### 7.2 分片实现

4. 新增策略三件套齐活：`StrategyType` 加枚举 → 实现类 → `ShardingStrategyFactory` 静态 Map 注册。缺一不可。
5. **`setAvailableTables` 必须实现且不得抛异常**。`DataSourceEntry.refresh()` 会对每条规则调用它，早期 RANGE/YEAR 空实现抛 `UnsupportedOperationException`，直接把整个数据源拖垮过；现在 refresh 已做单规则隔离，但规则本身仍不许抛。
6. 策略实现必须**无状态**（实例全局共享）；可变数据只能挂在 `ShardingRuleConfig` 上。
7. 物理表名一律走 `TableUtils.getPhysicalTableName/getSuffix/getPrefix`，**禁止手写 `table + "_" + n`**。
8. 定位不到分片时返回**全部可用分片**，不要返回 `null`、不要抛异常。

### 7.3 SQL 处理

9. 只通过 JSqlParser 的 **AST** 改写，禁止对 SQL 字符串做 `replace`/拼接。
10. 当前支持：`PlainSelect`、`SetOperationList`；WHERE 中 `AND`/括号/`=`/`IN`/`BETWEEN`/`!=`/`>`/`>=`/`<`/`<=`/`LIKE`/`IS [NOT] NULL`。
    **不支持**（该条件被忽略，退化为全分片）：`OR`、子查询形式的 `IN`、表达式函数包裹的分片列。
11. 改写必须保留 `?` 占位符语义，别把占位符替换成字面量。
12. RANGE/YEAR 的比较类条件会**多扫边界分片**（如 `id > 1999` 仍带上 1999 所在分片）。这是有意取舍：改写后的 SQL 保留了原谓词，多扫不会多出数据，少扫才会漏数据。

### 7.4 API

13. 统一返回 `Response<T>{code,message,data}`，HTTP 状态恒为 200，业务码放 body；controller 继承 `BaseController`。参考 `SqlController` 的 try/catch 范式，别把异常堆栈直接抛给前端。
14. 路径用资源复数：`/data-sources`、`/sharding-rules`、`/sql`、`/sql-queries`。
15. 数据源/规则的写操作必须**先落库、再刷新 `DataSourceLoader` 内存态**，两端不能只改一边。
16. 更新分片规则时，必须**先记下旧的 `dataSourceId + tableName`** 再去 `BeanUtils.copyProperties`，否则旧规则会残留在运行时。

### 7.5 前端

17. HTTP 只走 `src/api/request.ts` 的 axios 实例（baseURL 取 `VITE_API_BASE_URL`），**禁止组件内直接 import axios**。
18. 类型集中在 `src/types/index.ts`，后端 DTO 变了同步改这里。
19. 组件用 `<script setup lang="ts">` + Composition API；UI 用 Element Plus，SQL 编辑用 Monaco。
20. 路由保持 `createWebHashHistory`（静态部署无需 rewrite 配置），不要改成 history 模式。

### 7.6 通用

21. 注释 / Javadoc 用中文，保持现有风格；对外公共方法写 `@param`/`@return`。
22. 日志用 `LoggerFactory.getLogger(X.class)`，禁止 `System.out`。
23. 依赖版本统一放父 pom `<properties>`，子模块只引用不写 `<version>`（现存历史遗留除外）。
24. 改 JPA 实体**必须同步** `docs/sql/easy-query.sql`（当前已漂移，见 §9）。
25. 不提交 `.idea/`、`target/`、`node_modules/`、`dist/`。
26. 文件编码 UTF-8，换行沿用仓库现状（CRLF）。

---

## 8. 改完自查清单

- [ ] 新增策略 → 枚举、实现、工厂注册、`setAvailableTables` 四处都动了？
- [ ] 改实体 → `docs/sql/easy-query.sql` 对得上？`@Column(name=...)` 拼写与脚本一致？
- [ ] 改接口/DTO → `src/types/index.ts` 与前端 `api/*.ts` 同步？
- [ ] 改 `EasyQueryConfig` → 新 Bean 能被 `SqlExecutorService` 注入到？
- [ ] 动过分片/改写 → 跑 `easy-query-core` 的 `ShardingStrategyTest` 与 `SelectSqlRewriterTest`，并覆盖单分片、多分片（`IN`/`BETWEEN`）、无分片键三种情况？
- [ ] 前端改动 → `npm run type-check` 通过？

---

## 9. 已知技术债 / 雷区（别当成「已实现」）

1. **MONTH / DAY / YEAR_MONTH 未实现**：`StrategyType` 里有枚举，但 `ShardingStrategyFactory` 拿不到实现会返回 `null`。现在 refresh 会告警并跳过该规则（不再崩溃），查询命中这类规则时会明确报「无法从 Spring 容器获取分片策略」。新增策略请按 §7.2 的三件套补。
2. **脚本与实体漂移**：脚本是 `sharding_strategy` + `data_source.status`；实体是 `@Table("sharding_rule")` 且**无** `status` 字段；`sql_query` 脚本缺 `data_source_id`。按脚本建库后需按实体校正。
3. **文档滞后**：README / `EASYQUERY_USAGE.md` 仍写 `{dataSourceName, shardingKey, params}`，实际是 `{dataSourceId, sql}`。
4. **参数未绑定**：`SqlExecutorService` 用 `PreparedStatement` 却从不 `setXxx`，只能跑内联字面量 SQL。
5. **安全**：密码明文存库明文返回、接口无鉴权、可执行任意 SQL。定位是内网工具，对外暴露前必须加认证授权。
6. **并发**：`DataSourceLoader` 运行时向 `ArrayList` 增删规则，与查询线程无同步（宜改 `CopyOnWriteArrayList`）。
7. **未接线**：`ResultHandler` 的 merge/sort/page/aggregation、`TransactionManager`、`MonitorManager`、`LogManager` 都没有真实调用方。
8. **pom 杂物**：`easy-query-core/pom.xml` 末尾有多余字符（`</dependencies>s`），XML 合法但应清理。
9. **测试**：已有 `ShardingStrategyTest`（HASH/RANGE/YEAR 路由 + refresh 隔离）与 `SelectSqlRewriterTest`（改写与 UNION 拼接），但本机无 Maven 无法直接 `mvn test`；`SelectSqlParseTest` 仍是打印型，无 CI 配置。
10. **RANGE/YEAR 的已知取舍**：比较条件会多扫边界分片（结果正确、多一次查询）；`t_order_2024` 这类 4 位后缀表也会被 RANGE 的 `\d+` 规则收进可用表，所以同一逻辑表不要同时配 RANGE 与 YEAR 规则。

---

## 10. 术语表

| 词 | 含义 |
|---|---|
| 分片键 | `sharding_rule.sharding_column`，参与分片计算的列 |
| 分片条件 | 从 WHERE 中抽出的 `SqlCondition(column, ConditionType, value)` |
| 可用表 | 数据源里形如 `<逻辑表>_<纯数字>` 的表，由 `setAvailableTables` 过滤得出 |
| 全分片 | 无法定位时的退化策略：遍历所有可用表 |
| 绑定表 | 分片列相同的 JOIN 表，与主表共用同一后缀 |
