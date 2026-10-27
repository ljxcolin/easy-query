import request from './request';
import type { Response } from '../types';

// 用户级资源统计
export interface StatsSummary {
  dataSourceCount: number;
  shardingRuleCount: number;
  sqlQueryCount: number;
}

export default {
  // 获取当前用户的资源统计（数据源 / 分片规则 / SQL 查询数量）
  summary(): Promise<Response<StatsSummary>> {
    return request.get('/stats/summary');
  }
};
