<template>
  <div class="app">
    <el-container>
      <el-header height="60px" class="header">
        <h1>Easy Query 数据源管理</h1>
      </el-header>
      <el-container class="main-container">
        <el-aside width="200px" class="aside">
        <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleMenuSelect">
          <el-menu-item index="/data-source">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据源管理</span>
          </el-menu-item>
          <el-menu-item index="/sharding-rule">
            <el-icon><Setting /></el-icon>
            <span>分片规则</span>
          </el-menu-item>
          <el-menu-item index="/sql-query">
            <el-icon><Document /></el-icon>
            <span>SQL 查询</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
        <el-main class="main">
        <div class="nav-bar">
          <Breadcrumb />
          <BackButton />
        </div>
        <div class="page-content">
          <router-view />
        </div>
      </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DataAnalysis, Setting, Document } from '@element-plus/icons-vue';
import Breadcrumb from './components/Breadcrumb.vue';
import BackButton from './components/BackButton.vue';

const route = useRoute();
const router = useRouter();
const activeMenu = ref<string>('/data-source');

watch(
  () => route.path,
  (newPath) => {
    activeMenu.value = newPath;
  },
  { immediate: true }
);

const handleMenuSelect = (index: string) => {
  router.push(index);
};
</script>

<style scoped>
.app {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  padding: 0 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
  flex-shrink: 0;
}

.header h1 {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

.main {
  flex: 1;
  padding: 0;
  background-color: #f5f7fa;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
  flex-shrink: 0;
}

.page-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

:deep(.el-menu) {
  border-right: none;
  background-color: transparent;
}

:deep(.el-menu-item) {
  margin: 8px 12px;
  border-radius: 8px;
  height: 50px;
  line-height: 50px;
  transition: all 0.3s;
}

:deep(.el-menu-item:hover) {
  background-color: #f0f9ff;
  color: #409eff;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #e6f7ff 0%, #bae7ff 100%);
  color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

:deep(.el-icon) {
  font-size: 18px;
}

:deep(.el-menu-item span) {
  font-size: 15px;
  font-weight: 500;
}
</style>