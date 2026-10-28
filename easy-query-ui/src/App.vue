<template>
  <!-- 项目介绍页与登录页：全屏渲染，不带工作台布局 -->
  <router-view v-if="isFullScreenPage" />

  <!-- 工作台：独立布局组件 -->
  <ConsoleLayout v-else />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import ConsoleLayout from './components/ConsoleLayout.vue';

const route = useRoute();

/** 全屏页面（项目介绍页 / 登录页）不走工作台布局 */
const isFullScreenPage = computed(() => route.path === '/' || route.path === '/login');
</script>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>
