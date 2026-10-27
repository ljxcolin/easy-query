<template>
  <!-- 项目介绍页与登录页：全屏渲染，不带工具区布局 -->
  <router-view v-if="isFullScreenPage" />

  <!-- 工具区布局 -->
  <div v-else class="app">
    <el-container>
      <el-header height="60px" class="header">
        <div class="flex items-center gap-2">
          <h1>EasyQuery 数据源管理与分片查询工具</h1>
        </div>
        <div class="ml-auto flex items-center gap-3">
          <!-- 主题切换 -->
          <el-tooltip :content="isDark ? '切换到亮色模式' : '切换到暗色模式'" placement="bottom">
            <button class="theme-toggle" @click="handleToggleTheme">
              <el-icon :size="18">
                <Sunny v-if="isDark" />
                <Moon v-else />
              </el-icon>
            </button>
          </el-tooltip>

          <el-dropdown v-if="currentUser" @command="handleUserCommand">
            <span class="user-chip">
              <el-icon><User /></el-icon>
              {{ currentUser.nickname }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ currentUser.phone }}</el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container class="main-container">
        <el-aside width="200px" class="aside">
        <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleMenuSelect">
          <el-menu-item index="/tool">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/tool/data-source">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据源</span>
          </el-menu-item>
          <el-menu-item index="/tool/sharding-rule">
            <el-icon><Setting /></el-icon>
            <span>分片规则</span>
          </el-menu-item>
          <el-menu-item index="/tool/sql-query">
            <el-icon><Document /></el-icon>
            <span>SQL 查询</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
        <el-main class="main">
        <div class="nav-bar">
          <Breadcrumb />
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
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { DataAnalysis, Setting, Document, HomeFilled, User, SwitchButton, Sunny, Moon } from '@element-plus/icons-vue';
import Breadcrumb from './components/Breadcrumb.vue';
import { getStoredUser, clearLogin } from './utils/auth';
import { toggleTheme, getSavedTheme } from './utils/theme';

const route = useRoute();
const router = useRouter();
const activeMenu = ref<string>('/tool/data-source');

/** 全屏页面（项目介绍页 / 登录页）不走工具区布局 */
const isFullScreenPage = computed(() => route.path === '/' || route.path === '/login');

const currentUser = ref(getStoredUser());

/** 当前是否为暗色主题 */
const isDark = ref(getSavedTheme() === 'dark');

/** 切换亮/暗主题 */
const handleToggleTheme = () => {
  isDark.value = toggleTheme() === 'dark';
};

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

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        clearLogin();
        currentUser.value = null;
        ElMessage.success('已退出登录');
        router.push('/');
      })
      .catch(() => {});
  }
};
</script>

<!-- 主题变量（全局）：亮/暗两套工作台配色 -->
<style>
:root {
  --eq-header-bg: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  --eq-header-color: #1565c0;
  --eq-header-chip-bg: rgba(255, 255, 255, 0.6);
  --eq-header-chip-bg-hover: rgba(255, 255, 255, 0.9);
  --eq-aside-bg: #ffffff;
  --eq-main-bg: #f5f7fa;
  --eq-border: #e6e6e6;
  --eq-shadow: rgba(0, 0, 0, 0.05);
}

html.dark {
  --eq-header-bg: #16181d;
  --eq-header-color: #93b8e8;
  --eq-header-chip-bg: rgba(255, 255, 255, 0.08);
  --eq-header-chip-bg-hover: rgba(255, 255, 255, 0.16);
  --eq-aside-bg: #1d1e1f;
  --eq-main-bg: #101012;
  --eq-border: #333438;
  --eq-shadow: rgba(0, 0, 0, 0.4);
}
</style>

<style scoped>
.app {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: var(--eq-header-bg);
  color: var(--eq-header-color);
  display: flex;
  align-items: center;
  padding: 0 25px;
  box-shadow: 0 2px 12px rgba(21, 101, 192, 0.15);
  z-index: 100;
  flex-shrink: 0;
  border-bottom: 1px solid var(--eq-border);
}

.header h1 {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.theme-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  background: var(--eq-header-chip-bg);
  color: var(--eq-header-color);
  transition: background 0.2s, transform 0.2s;
}

.theme-toggle:hover {
  background: var(--eq-header-chip-bg-hover);
  transform: rotate(15deg);
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 8px;
  background: var(--eq-header-chip-bg);
  color: var(--eq-header-color);
  font-size: 14px;
  font-weight: 500;
  outline: none;
  transition: background 0.2s;
}

.user-chip:hover {
  background: var(--eq-header-chip-bg-hover);
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.aside {
  background-color: var(--eq-aside-bg);
  border-right: 1px solid var(--eq-border);
  box-shadow: 2px 0 8px var(--eq-shadow);
  flex-shrink: 0;
}

.main {
  flex: 1;
  padding: 0;
  background-color: var(--eq-main-bg);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--eq-aside-bg);
  border-bottom: 1px solid var(--eq-border);
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
  background-color: var(--el-fill-color-light);
  color: var(--el-color-primary);
}

:deep(.el-menu-item.is-active) {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  box-shadow: 0 2px 8px var(--eq-shadow);
}

:deep(.el-icon) {
  font-size: 18px;
}

:deep(.el-menu-item span) {
  font-size: 15px;
  font-weight: 500;
}
</style>
