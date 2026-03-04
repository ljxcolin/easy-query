<template>
  <div class="breadcrumb-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }" v-if="items.length > 1">
        <el-icon><HomeFilled /></el-icon>
        首页
      </el-breadcrumb-item>
      <el-breadcrumb-item 
        v-for="(item, index) in items" 
        :key="index"
        :to="item.path"
      >
        <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
        {{ item.name }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { HomeFilled } from '@element-plus/icons-vue';

interface BreadcrumbItem {
  name: string;
  path: string;
  icon?: string;
}

const route = useRoute();

const items = computed<BreadcrumbItem[]>(() => {
  const breadcrumbItems: BreadcrumbItem[] = [];
  
  // 根据当前路由路径生成面包屑
  const pathSegments = route.path.split('/').filter(Boolean);
  
  pathSegments.forEach((segment, index) => {
    const path = '/' + pathSegments.slice(0, index + 1).join('/');
    const name = getSegmentName(segment);
    const icon = getSegmentIcon(segment);
    
    breadcrumbItems.push({
      name,
      path,
      icon
    });
  });
  
  return breadcrumbItems;
});

const getSegmentName = (segment: string): string => {
  const nameMap: Record<string, string> = {
    'data-source': '数据源管理',
    'sql-query': 'SQL 查询',
    'sharding-rules': '分片规则'
  };
  return nameMap[segment] || segment;
};

const getSegmentIcon = (segment: string): string | undefined => {
  const iconMap: Record<string, string> = {
    'data-source': 'DataAnalysis',
    'sql-query': 'Document',
    'sharding-rules': 'Setting'
  };
  return iconMap[segment];
};
</script>

<style scoped>
.breadcrumb-container {
  padding: 15px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

:deep(.el-breadcrumb) {
  font-size: 14px;
}

:deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__item .el-icon) {
  margin-right: 4px;
  font-size: 16px;
}

:deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  color: #606266;
}

:deep(.el-breadcrumb__inner:hover) {
  color: #409eff;
}

:deep(.el-breadcrumb__separator) {
  margin: 0 8px;
  color: #c0c4cc;
}
</style>
