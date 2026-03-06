<template>
  <div class="breadcrumb-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item>
        <el-button type="info" @click="handleBack" circle size="small" :disabled="!canGoBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
      </el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/' }">
        <el-icon><HomeFilled /></el-icon>
        首页
      </el-breadcrumb-item>
      <el-breadcrumb-item 
        v-if="currentName"
      >
        {{ currentName }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { HomeFilled, ArrowLeft } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

const canGoBack = computed(() => {
  return route.path !== '/';
});

const currentName = computed(() => {
  const pathSegments = route.path.split('/').filter(Boolean);
  if (pathSegments.length > 0) {
    const segment = pathSegments[0];
    return getSegmentName(segment);
  }
  return '';
});

const handleBack = () => {
  router.back();
};

const getSegmentName = (segment: string): string => {
  const nameMap: Record<string, string> = {
    'data-source': '数据源管理',
    'sql-query': 'SQL 查询',
    'sharding-rule': '分片规则'
  };
  return nameMap[segment] || segment;
};
</script>

<style scoped>
.breadcrumb-container {
  padding: 12px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

:deep(.el-breadcrumb) {
  font-size: 14px;
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__item .el-icon) {
  margin-right: 4px;
  font-size: 16px;
  color: #1976d2;
}

:deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  color: #1565c0;
  font-weight: 500;
}

:deep(.el-breadcrumb__inner:hover) {
  color: #1976d2;
}

:deep(.el-breadcrumb__separator) {
  margin: 0 8px;
  color: #64b5f6;
}

:deep(.el-button) {
  margin-right: 8px;
}
</style>
