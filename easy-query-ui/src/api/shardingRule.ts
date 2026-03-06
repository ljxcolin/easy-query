import request from './request';
import type { ShardingRule, Response } from '../types';

export default {
  // 获取所有分片规则
  getAllShardingRules(): Promise<Response<ShardingRule[]>> {
    return request.get('/sharding-rules');
  },
  // 获取单个分片规则
  getShardingRule(name: string): Promise<Response<ShardingRule>> {
    return request.get(`/sharding-rules/${name}`);
  },
  // 创建分片规则
  createShardingRule(shardingRule: ShardingRule): Promise<Response<ShardingRule>> {
    return request.post('/sharding-rules', shardingRule);
  },
  // 更新分片规则
  updateShardingRule(id: number, shardingRule: ShardingRule): Promise<Response<ShardingRule>> {
    return request.put(`/sharding-rules/${id}`, shardingRule);
  },
  // 删除分片规则
  deleteShardingRule(id: number): Promise<Response<void>> {
    return request.delete(`/sharding-rules/${id}`);
  },
  // 测试分片规则
  testShardingRule(shardingRule: ShardingRule, shardingKey: any): Promise<Response<string>> {
    return request.post('/sharding-rules/test', shardingRule, {
      params: { shardingKey }
    });
  }
};
