<template>
  <div class="console-layout">
    <el-container class="layout-container">
      <!-- 顶部栏 -->
      <el-header height="60px" class="header">
        <div class="brand">
          <img class="brand-logo" src="/favicon.svg" alt="EasyQuery" />
          <h1 class="brand-title">EasyQuery 工作台</h1>
        </div>

        <div class="header-right">
          <!-- 主题切换 -->
          <el-tooltip :content="isDark ? '切换到亮色模式' : '切换到暗色模式'" placement="bottom">
            <button class="theme-toggle" @click="handleToggleTheme">
              <el-icon :size="18">
                <Sunny v-if="isDark" />
                <Moon v-else />
              </el-icon>
            </button>
          </el-tooltip>

          <!-- 用户：圆形头像 + 下拉 -->
          <el-dropdown v-if="user" trigger="click" @command="handleUserCommand">
            <span class="user-chip">
              <el-avatar :size="32" :src="defaultAvatar" :alt="user.nickname || user.phone"
                class="user-avatar">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ user.nickname || user.phone }}</span>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="user-panel">
                  <el-avatar :size="44" :src="defaultAvatar" class="user-panel-avatar">{{ avatarText }}</el-avatar>
                  <div class="user-panel-info">
                    <div class="user-panel-name">{{ user.nickname || '未设置昵称' }}</div>
                    <div class="user-panel-phone">{{ user.phone }}</div>
                  </div>
                </div>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container class="body-container">
        <!-- 侧边菜单 -->
        <el-aside width="200px" class="aside">
          <el-menu :default-active="activeMenu" class="console-menu" @select="handleMenuSelect">
            <el-menu-item index="/console">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/console/data-source">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据源</span>
            </el-menu-item>
            <el-menu-item index="/console/sharding-rule">
              <el-icon><Setting /></el-icon>
              <span>分片规则</span>
            </el-menu-item>
            <el-menu-item index="/console/sql-query">
              <el-icon><Document /></el-icon>
              <span>SQL 查询</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 内容区 -->
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
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  DataAnalysis,
  Setting,
  Document,
  HomeFilled,
  SwitchButton,
  Sunny,
  Moon,
  ArrowDown
} from '@element-plus/icons-vue';
import Breadcrumb from './Breadcrumb.vue';
import { clearLogin } from '../utils/auth';
import { toggleTheme, getSavedTheme } from '../utils/theme';
import { useUser, avatarFallbackText } from '../stores/user';

/** 默认头像（静态资源，加载失败时展示昵称首字） */
const defaultAvatar = '/default-avatar.svg';

const route = useRoute();
const router = useRouter();

const { user, clearUser } = useUser();

/** 头像兜底文字：昵称首字，无昵称取手机号后两位 */
const avatarText = computed(() => avatarFallbackText(user.value));

/** 当前激活菜单：详情页等子路由回落到所属一级菜单 */
const activeMenu = computed(() => {
  const path = route.path;
  if (path.startsWith('/console/data-source')) return '/console/data-source';
  if (path.startsWith('/console/sharding-rule')) return '/console/sharding-rule';
  if (path.startsWith('/console/sql-query')) return '/console/sql-query';
  return '/console';
});

/** 当前是否为暗色主题 */
const isDark = ref(getSavedTheme() === 'dark');

/** 切换亮/暗主题 */
const handleToggleTheme = () => {
  isDark.value = toggleTheme() === 'dark';
};

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
        clearUser();
        ElMessage.success('已退出登录');
        router.push('/');
      })
      .catch(() => { });
  }
};
</script>

<style scoped>
.console-layout {
  height: 100vh;
  display: flex;
}

.layout-container {
  flex: 1;
  min-width: 0;
}

.header {
  background: var(--eq-header-bg);
  color: var(--eq-header-color);
  display: flex;
  align-items: center;
  padding: 0 25px;
  box-shadow: 0 2px 12px var(--eq-shadow);
  z-index: 100;
  flex-shrink: 0;
  border-bottom: 1px solid var(--eq-border);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: block;
}

.brand-title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 14px;
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
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 999px;
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

.user-avatar {
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.75);
  background: #fff;
  font-weight: 600;
  color: var(--eq-header-color);
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-arrow {
  font-size: 12px;
  opacity: 0.7;
}

.user-panel {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  min-width: 200px;
}

.user-panel-avatar {
  flex-shrink: 0;
}

.user-panel-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.user-panel-phone {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.body-container {
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
  min-width: 0;
}

.nav-bar {
  background-color: var(--eq-aside-bg);
  border-bottom: 1px solid var(--eq-border);
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
