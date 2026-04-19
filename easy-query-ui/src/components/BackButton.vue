<template>
  <div class="back-button-container">
    <el-button 
      v-if="canGoBack"
      @click="handleBack" 
      :icon="ArrowLeft"
      circle
      size="default"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();

const canGoBack = computed(() => {
  // 如果不是首页，则可以返回
  return route.path !== '/data-source';
});

const handleBack = () => {
  // 返回上一页或默认返回数据源页
  router.back();
};
</script>

<style scoped>
.back-button-container {
  display: flex;
  align-items: center;
}

:deep(.el-button) {
  border: none;
  background-color: transparent;
  color: #606266;
  transition: all 0.3s;
}

:deep(.el-button:hover) {
  background-color: #f5f7fa;
  color: #409eff;
  transform: translateX(-3px);
}

:deep(.el-button.is-disabled:hover) {
  background-color: transparent;
  transform: none;
}
</style>
