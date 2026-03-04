import request from './request';
import type { DataSource, Response } from '../types';

export default {
  // 获取所有数据源
  getAllDataSources(): Promise<Response<DataSource[]>> {
    return request.get('/data-sources');
  },
  // 获取单个数据源
  getDataSource(name: string): Promise<Response<DataSource>> {
    return request.get(`/data-sources/${name}`);
  },
  // 创建数据源
  createDataSource(dataSource: DataSource): Promise<Response<DataSource>> {
    return request.post('/data-sources', dataSource);
  },
  // 更新数据源
  updateDataSource(name: string, dataSource: DataSource): Promise<Response<DataSource>> {
    return request.put(`/data-sources/${name}`, dataSource);
  },
  // 删除数据源
  deleteDataSource(name: string): Promise<Response<void>> {
    return request.delete(`/data-sources/${name}`);
  },
  // 测试数据源连接
  testConnection(dataSource: DataSource): Promise<Response<boolean>> {
    return request.post('/data-sources/test-connection', dataSource);
  }
};
