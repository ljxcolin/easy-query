import request from './request';

export default {
  // 获取所有数据源
  getAllDataSources() {
    return request.get('/api/data-sources');
  },
  // 获取单个数据源
  getDataSource(name) {
    return request.get(`/api/data-sources/${name}`);
  },
  // 创建数据源
  createDataSource(dataSource) {
    return request.post('/api/data-sources', dataSource);
  },
  // 更新数据源
  updateDataSource(name, dataSource) {
    return request.put(`/api/data-sources/${name}`, dataSource);
  },
  // 删除数据源
  deleteDataSource(name) {
    return request.delete(`/api/data-sources/${name}`);
  },
  // 测试数据源连接
  testConnection(dataSource) {
    return request.post('/api/data-sources/test-connection', dataSource);
  }
};