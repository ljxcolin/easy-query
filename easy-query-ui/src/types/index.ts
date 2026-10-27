// 通用响应类型
export interface Response<T = any> {
  code: number;
  message: string;
  data: T;
}

// 数据源连接状态
export type ConnectionStatus = 'CONNECTED' | 'DISCONNECTED';

// 数据源实体（响应体）
export interface DataSource {
  id?: number;
  userId?: number;
  name: string;
  url: string;
  username: string;
  // 仅提交时携带；服务端响应不含密码字段
  password?: string;
  driverClassName?: string;
  maximumPoolSize?: number;
  minimumIdle?: number;
  connectionTimeout?: number;
  idleTimeout?: number;
  // 连接状态：由服务端缓存检测结果
  connectionStatus?: ConnectionStatus;
  connectionStatusLabel?: string;
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
  userId?: number;
  name: string;
  dataSourceId?: number;
  dataSourceName?: string;
  tableName?: string;
  shardingColumn?: string;
  strategyType: string;
  strategyConfig?: string;
  description?: string;
  createdTime?: string;
  updatedTime?: string;
  // 嵌套的策略配置对象
  shardingStrategy?: ShardingStrategy;
}

// SQL 请求
export interface SqlRequest {
  sql: string;
  dataSourceId?: number;
}

// SQL查询实体
export interface SqlQuery {
  id?: number;
  userId?: number;
  name: string;
  dataSourceId: number;
  dataSourceName?: string;
  sqlContent: string;
  description?: string;
  createdTime?: string;
  updatedTime?: string;
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
