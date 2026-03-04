import request from './request';
import type { SqlRequest, Response } from '../types';

export default {
  // 执行 SQL 查询
  executeQuery(sqlRequest: SqlRequest): Promise<Response<any[]>> {
    return request.post('/sql/query', sqlRequest);
  },
  // 执行 SQL 更新
  executeUpdate(sqlRequest: SqlRequest): Promise<Response<number>> {
    return request.post('/sql/execute', sqlRequest);
  }
};
