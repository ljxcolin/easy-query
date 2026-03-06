import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router';
import Home from '../views/Home.vue';
import DataSource from '../views/DataSource.vue';
import DataSourceDetail from '../views/DataSourceDetail.vue';
import ShardingRule from '../views/ShardingRule.vue';
import SqlQuery from '../views/SqlQuery.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/data-source',
    name: 'DataSource',
    component: DataSource
  },
  {
    path: '/data-source/:id',
    name: 'DataSourceDetail',
    component: DataSourceDetail
  },
  {
    path: '/sharding-rule',
    name: 'ShardingRule',
    component: ShardingRule
  },
  {
    path: '/sql-query',
    name: 'SqlQuery',
    component: SqlQuery
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export default router;
