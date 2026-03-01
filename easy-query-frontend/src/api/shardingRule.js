import request from './request';

export default {
  // 获取所有分片规则
  getAllShardingRules() {
    return request.get('/api/sharding-rules');
  },
  // 获取单个分片规则
  getShardingRule(name) {
    return request.get(`/api/sharding-rules/${name}`);
  },
  // 创建分片规则
  createShardingRule(shardingRule) {
    return request.post('/api/sharding-rules', shardingRule);
  },
  // 更新分片规则
  updateShardingRule(name, shardingRule) {
    return request.put(`/api/sharding-rules/${name}`, shardingRule);
  },
  // 删除分片规则
  deleteShardingRule(name) {
    return request.delete(`/api/sharding-rules/${name}`);
  },
  // 测试分片规则
  testShardingRule(shardingRule, shardingKey) {
    return request.post('/api/sharding-rules/test', shardingRule, {
      params: { shardingKey }
    });
  }
};