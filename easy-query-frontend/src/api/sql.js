import request from './request';

export default {
  // 执行SQL查询
  executeQuery(sqlRequest) {
    return request.post('/api/sql/query', sqlRequest);
  },
  // 执行SQL更新
  executeUpdate(sqlRequest) {
    return request.post('/api/sql/execute', sqlRequest);
  }
};