<template>
  <div class="home">
    <!-- 欢迎区 -->
    <div class="welcome-container">
      <h1 class="welcome-title">欢迎回来{{ nickname ? '，' + nickname : '' }}</h1>
      <p class="welcome-subtitle">这里是你工作台的整体概览</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card" @click="router.push(card.path)">
          <div class="stat-card-body">
            <div class="stat-icon" :style="{ background: card.gradient }">
              <el-icon :size="26"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ loading ? '…' : card.value }}</span>
              <span class="stat-label">{{ card.title }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card shadow="never" class="quick-card">
      <template #header>
        <span class="quick-title">快捷操作</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" plain @click="router.push('/tool/data-source')">
          <el-icon><Plus /></el-icon>&nbsp;新增数据源
        </el-button>
        <el-button type="success" plain @click="router.push('/tool/sharding-rule')">
          <el-icon><Setting /></el-icon>&nbsp;配置分片规则
        </el-button>
        <el-button type="warning" plain @click="router.push('/tool/sql-query')">
          <el-icon><Document /></el-icon>&nbsp;开始 SQL 查询
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { DataAnalysis, Setting, Document, Plus } from '@element-plus/icons-vue';
import statsApi from '../api/stats';
import { getStoredUser } from '../utils/auth';

const router = useRouter();
const loading = ref(true);

const currentUser = getStoredUser();
const nickname = computed(() => currentUser?.nickname || '');

const counts = ref({ dataSourceCount: 0, shardingRuleCount: 0, sqlQueryCount: 0 });

const statCards = computed(() => [
  {
    title: '数据源',
    value: counts.value.dataSourceCount,
    icon: DataAnalysis,
    path: '/tool/data-source',
    gradient: 'linear-gradient(135deg, #409eff 0%, #79bbff 100%)'
  },
  {
    title: '分片规则',
    value: counts.value.shardingRuleCount,
    icon: Setting,
    path: '/tool/sharding-rule',
    gradient: 'linear-gradient(135deg, #67c23a 0%, #95d475 100%)'
  },
  {
    title: 'SQL 查询',
    value: counts.value.sqlQueryCount,
    icon: Document,
    path: '/tool/sql-query',
    gradient: 'linear-gradient(135deg, #e6a23c 0%, #eebe77 100%)'
  }
]);

onMounted(async () => {
  try {
    const res = await statsApi.summary();
    if (res.code === 200 && res.data) {
      counts.value = res.data;
    }
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.home {
  padding: 20px;
  max-width: 1100px;
  margin: 0 auto;
}

.welcome-container {
  text-align: center;
  margin: 24px 0 32px;
}

.welcome-title {
  font-size: 30px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 10px;
}

.welcome-subtitle {
  font-size: 15px;
  color: var(--el-text-color-secondary);
  font-weight: 400;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 14px;
  border: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card :deep(.el-card__body) {
  padding: 22px;
}

.stat-card-body {
  display: flex;
  align-items: center;
  gap: 18px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--el-text-color-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.quick-card {
  border-radius: 14px;
  border: 1px solid var(--el-border-color-lighter);
}

.quick-title {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
