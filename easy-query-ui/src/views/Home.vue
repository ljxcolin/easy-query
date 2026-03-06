<template>
  <div class="home">
    <div class="welcome-container">
      <h1 class="welcome-title">欢迎使用 Easy Query</h1>
      <p class="welcome-subtitle">数据源管理与分片查询工具</p>
    </div>
  </div>
</template>

<script setup lang="ts">
// 首页暂时为空白页面
</script>

<style scoped>
.home {
  padding: 40px 20px;
  min-height: calc(100vh - 120px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.welcome-container {
  text-align: center;
  margin-top: 60px;
}

.welcome-title {
  font-size: 36px;
  font-weight: 600;
  color: #1565c0;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #1565c0 0%, #1976d2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 18px;
  color: #64b5f6;
  font-weight: 400;
}
</style>
