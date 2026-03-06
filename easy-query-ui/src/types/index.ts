// 通用响应类型
export interface Response<T = any> {
  code: number;
  message: string;
  data: T;
}

// 数据源实体
export interface DataSource {
  id?: number;
  name: string;
  url: string;
  username: string;
  password: string;
  driverClassName?: string;
  maximumPoolSize?: number;
  minimumIdle?: number;
  connectionTimeout?: number;
  idleTimeout?: number;
  status?: number;
  createdTime?: string;
  updatedTime?: string;
}

// 分片策略实体
export interface ShardingStrategy {
  // HASH 策略字段
  shardingCount?: number;
  shardingStart?: number;
  // RANGE 策略字段
  rangeStart?: number;
  rangeStep?: number;
  // MONTH/DAY 策略字段
  padZero?: boolean;
}

// 分片规则实体
export interface ShardingRule {
  id?: number;
  name: string;
  dataSourceId?: number;
  dataSourceName?: string;
  tableName?: string;
  shardingColumn?: string;
  strategyType: string;
  strategyConfig?: string;
  description?: string;
  status?: number;
  createdTime?: string;
  updatedTime?: string;
  // 嵌套的策略配置对象
  shardingStrategy?: ShardingStrategy;
}

// SQL 请求
export interface SqlRequest {
  sql: string;
  shardingKey?: any;
}

// 分页参数
export interface PageParams {
  page: number;
  size: number;
}

// 分页响应
export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}
