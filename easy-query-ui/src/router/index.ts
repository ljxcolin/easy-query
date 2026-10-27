import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router';
import Landing from '../views/Landing.vue';
import Login from '../views/Login.vue';
import Home from '../views/Home.vue';
import DataSource from '../views/DataSource.vue';
import DataSourceDetail from '../views/DataSourceDetail.vue';
import ShardingRule from '../views/ShardingRule.vue';
import SqlQuery from '../views/SqlQuery.vue';
import { isLoggedIn } from '../utils/auth';

const routes: RouteRecordRaw[] = [
  {
    // 项目介绍首页（无需登录）
    path: '/',
    name: 'Landing',
    component: Landing
  },
  {
    // 登录注册页（无需登录）
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    // 以下为工具区，必须登录
    path: '/tool',
    name: 'Home',
    component: Home
  },
  {
    path: '/tool/data-source',
    name: 'DataSource',
    component: DataSource
  },
  {
    path: '/tool/data-source/:id',
    name: 'DataSourceDetail',
    component: DataSourceDetail
  },
  {
    path: '/tool/sharding-rule',
    name: 'ShardingRule',
    component: ShardingRule
  },
  {
    path: '/tool/sql-query',
    name: 'SqlQuery',
    component: SqlQuery
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

// 路由守卫：工具区页面必须登录，未登录重定向到登录页
const WHITE_LIST = ['/', '/login'];

router.beforeEach((to) => {
  if (WHITE_LIST.includes(to.path)) {
    // 已登录用户访问登录页时直接进入工具首页
    if (to.path === '/login' && isLoggedIn()) {
      return { path: '/tool' };
    }
    return true;
  }
  if (!isLoggedIn()) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  return true;
});

export default router;
