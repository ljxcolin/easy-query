import request from './request';
import type { SqlQuery, Response } from '../types';

export default {
  // 获取所有SQL查询
  getAllSqlQueries(): Promise<Response<SqlQuery[]>> {
    return request.get('/sql-queries');
  },
  // 根据数据源ID获取SQL查询列表
  getSqlQueriesByDataSourceId(dataSourceId: number): Promise<Response<SqlQuery[]>> {
    return request.get(`/sql-queries/data-source/${dataSourceId}`);
  },
  // 获取单个SQL查询
  getSqlQueryById(id: number): Promise<Response<SqlQuery>> {
    return request.get(`/sql-queries/${id}`);
  },
  // 创建SQL查询
  createSqlQuery(sqlQuery: SqlQuery): Promise<Response<SqlQuery>> {
    return request.post('/sql-queries', sqlQuery);
  },
  // 更新SQL查询
  updateSqlQueryById(id: number, sqlQuery: SqlQuery): Promise<Response<SqlQuery>> {
    return request.put(`/sql-queries/${id}`, sqlQuery);
  },
  // 删除SQL查询
  deleteSqlQueryById(id: number): Promise<Response<void>> {
    return request.delete(`/sql-queries/${id}`);
  }
};
