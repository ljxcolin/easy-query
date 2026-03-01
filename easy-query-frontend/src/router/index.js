import { createRouter, createWebHashHistory } from 'vue-router';
import DataSource from '../components/DataSource.vue';
import ShardingRule from '../components/ShardingRule.vue';
import SqlQuery from '../components/SqlQuery.vue';

const routes = [
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