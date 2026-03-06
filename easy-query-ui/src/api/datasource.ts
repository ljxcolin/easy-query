import request from './request';
import type { DataSource, Response } from '../types';

export default {
  // 获取所有数据源
  getAllDataSources(): Promise<Response<DataSource[]>> {
    return request.get('/data-sources');
  },
  // 获取单个数据源 (按名称)
  getDataSourceByName(name: string): Promise<Response<DataSource>> {
    return request.get(`/data-sources/name/${name}`);
  },
  // 获取单个数据源 (按 ID)
  getDataSourceById(id: number): Promise<Response<DataSource>> {
    return request.get(`/data-sources/${id}`);
  },
  // 创建数据源
  createDataSource(dataSource: DataSource): Promise<Response<DataSource>> {
    return request.post('/data-sources', dataSource);
  },
  // 更新数据源 (按 ID)
  updateDataSourceById(id: number, dataSource: DataSource): Promise<Response<DataSource>> {
    return request.put(`/data-sources/${id}`, dataSource);
  },
  // 删除数据源 (按 ID)
  deleteDataSourceById(id: number): Promise<Response<void>> {
    return request.delete(`/data-sources/${id}`);
  },
  // 测试数据源连接
  testConnection(dataSource: DataSource): Promise<Response<boolean>> {
    return request.post('/data-sources/test-connection', dataSource);
  }
};
