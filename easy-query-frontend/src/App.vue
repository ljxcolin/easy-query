<template>
  <div class="app">
    <el-container>
      <el-header height="60px" class="header">
        <h1>Easy Query - 数据库分片工具</h1>
      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu :default-active="activeMenu" class="el-menu-vertical-demo">
            <el-menu-item index="/data-source">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据源管理</span>
            </el-menu-item>
            <el-menu-item index="/sharding-rule">
              <el-icon><Setting /></el-icon>
              <span>分片规则配置</span>
            </el-menu-item>
            <el-menu-item index="/sql-query">
              <el-icon><Document /></el-icon>
              <span>SQL查询</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { DataAnalysis, Setting, Document } from '@element-plus/icons-vue';

export default {
  name: 'App',
  components: {
    DataAnalysis,
    Setting,
    Document
  },
  data() {
    return {
      activeMenu: '/data-source'
    };
  },
  watch: {
    $route: {
      handler(to) {
        this.activeMenu = to.path;
      },
      immediate: true
    }
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, sans-serif;
}

.app {
  height: 100vh;
}

.header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header h1 {
  font-size: 20px;
  font-weight: bold;
}

.aside {
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
}

.main {
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
}

.el-menu-vertical-demo {
  height: 100%;
  border-right: none;
}

.el-menu-item {
  margin: 10px 0;
  border-radius: 4px;
}

.el-menu-item:hover {
  background-color: #ecf5ff;
}

.el-menu-item.is-active {
  background-color: #ecf5ff;
  color: #409EFF;
}
</style>