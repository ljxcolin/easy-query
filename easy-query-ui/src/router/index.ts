import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router';
import DataSource from '../views/DataSource.vue';
import ShardingRule from '../views/ShardingRule.vue';
import SqlQuery from '../views/SqlQuery.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/data-source'
  },
  {
    path: '/data-source',
    name: 'DataSource',
    component: DataSource
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
