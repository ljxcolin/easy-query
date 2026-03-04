<template>
  <div class="data-source">
    <div class="page-header">
      <h2 class="page-title">数据源管理</h2>
    </div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据源管理</span>
          <el-button type="primary" @click="handleAdd">新增数据源</el-button>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" class="data-tabs">
        <el-tab-pane label="数据源管理" name="datasource">
          <el-table :data="dataSources" style="width: 100%" v-loading="loading" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
            <el-table-column prop="name" label="数据源名称" width="140" show-overflow-tooltip />
            <el-table-column prop="url" label="连接 URL" min-width="200">
              <template #default="scope">
                <el-tooltip :content="scope.row.url" placement="top" :disabled="scope.row.url.length < 50">
                  <span class="url-text">{{ scope.row.url }}</span>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="driverClassName" label="驱动类" width="220" show-overflow-tooltip />
            <el-table-column prop="maximumPoolSize" label="最大连接数" width="100" align="center">
              <template #default="scope">
                <el-tag size="small" type="info">{{ scope.row.maximumPoolSize }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="minimumIdle" label="最小空闲数" width="100" align="center">
              <template #default="scope">
                <el-tag size="small" type="success">{{ scope.row.minimumIdle }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ scope.row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button size="small" type="success" @click="handleTest(scope.row)">测试</el-button>
                <el-button size="small" type="warning" @click="handleShardingConfig(scope.row)">分片</el-button>
                <el-button size="small" type="danger" @click="handleDelete(scope.row.name)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="分片规则配置" name="sharding">
          <div class="sharding-toolbar">
            <el-button type="primary" @click="handleAddShardingRule">
              <el-icon style="margin-right: 5px;"><Plus /></el-icon>
              新增分片规则
            </el-button>
          </div>
          <el-table :data="shardingRules" style="width: 100%" v-loading="shardingLoading" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
            <el-table-column prop="name" label="规则名称" width="150" show-overflow-tooltip />
            <el-table-column prop="strategyType" label="分片策略" width="120">
              <template #default="scope">
                <el-tag :type="getStrategyTagType(scope.row.strategyType)" size="medium">{{ scope.row.strategyType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dataSourceName" label="数据源" width="150" />
            <el-table-column prop="tableName" label="逻辑表名" width="150" />
            <el-table-column prop="shardingColumn" label="分片字段" width="120" />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleEditShardingRule(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteShardingRule(scope.row.name)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 数据源新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="form" label-width="120px">
        <el-form-item label="数据源名称">
          <el-input v-model="form.name" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="连接 URL">
          <el-input v-model="form.url" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="驱动类">
          <el-input v-model="form.driverClassName" />
        </el-form-item>
        <el-form-item label="最大连接数">
          <el-input-number v-model="form.maximumPoolSize" :min="1" />
        </el-form-item>
        <el-form-item label="最小空闲数">
          <el-input-number v-model="form.minimumIdle" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分片规则新增/编辑对话框 -->
    <el-dialog
      v-model="shardingDialogVisible"
      :title="shardingDialogTitle"
      width="600px"
    >
      <el-form :model="shardingForm" label-width="120px">
        <el-form-item label="规则名称">
          <el-input v-model="shardingForm.name" :disabled="shardingIsEdit" />
        </el-form-item>
        <el-form-item label="分片策略">
          <el-select v-model="shardingForm.strategyType">
            <el-option label="HASH" value="HASH" />
            <el-option label="RANGE" value="RANGE" />
            <el-option label="LIST" value="LIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联数据源">
          <el-select v-model="shardingForm.dataSourceName" placeholder="请选择数据源">
            <el-option
              v-for="ds in dataSources"
              :key="ds.name"
              :label="ds.name"
              :value="ds.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="逻辑表名">
          <el-input v-model="shardingForm.tableName" />
        </el-form-item>
        <el-form-item label="分片字段">
          <el-input v-model="shardingForm.shardingColumn" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="shardingForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="配置 JSON">
          <el-input
            v-model="shardingFormConfig"
            type="textarea"
            :rows="8"
            placeholder="请输入 JSON 格式的配置"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shardingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveShardingRule" :loading="shardingSaving">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import dataSourceApi from '@/api/datasource';
import shardingRuleApi from '@/api/shardingRule';
import type { DataSource, ShardingRule } from '@/types';

// 选项卡
const activeTab = ref<string>('datasource');

// 数据源相关
const dataSources = ref<DataSource[]>([]);
const loading = ref<boolean>(false);

// 对话框状态
const dialogVisible = ref<boolean>(false);
const dialogTitle = ref<string>('新增数据源');
const isEdit = ref<boolean>(false);
const saving = ref<boolean>(false);

// 表单数据
const form = reactive<DataSource>({
  name: '',
  url: '',
  username: '',
  password: '',
  driverClassName: 'com.mysql.cj.jdbc.Driver',
  maximumPoolSize: 10,
  minimumIdle: 5
});

// 分片规则相关
const shardingRules = ref<ShardingRule[]>([]);
const shardingLoading = ref<boolean>(false);
const shardingDialogVisible = ref<boolean>(false);
const shardingDialogTitle = ref<string>('新增分片规则');
const shardingIsEdit = ref<boolean>(false);
const shardingSaving = ref<boolean>(false);

const shardingForm = reactive<Partial<ShardingRule>>({
  name: '',
  strategyType: 'HASH',
  dataSourceName: '',
  tableName: '',
  shardingColumn: '',
  description: '',
  configJson: '{}'
});

const shardingFormConfig = ref<string>('{}');

// 加载数据源列表
const loadDataSources = async () => {
  loading.value = true;
  try {
    const response = await dataSourceApi.getAllDataSources();
    if (response.code === 200 && response.data) {
      dataSources.value = response.data;
    } else {
      dataSources.value = [];
    }
  } catch (error) {
    console.error('Error loading data sources:', error);
    ElMessage.error('加载数据源失败');
    dataSources.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载分片规则列表
const loadShardingRules = async () => {
  shardingLoading.value = true;
  try {
    const response = await shardingRuleApi.getAllShardingRules();
    if (response.code === 200 && response.data) {
      shardingRules.value = response.data;
    } else {
      shardingRules.value = [];
    }
  } catch (error) {
    console.error('Error loading sharding rules:', error);
    shardingRules.value = [];
  } finally {
    shardingLoading.value = false;
  }
};

// 获取策略标签类型
const getStrategyTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'HASH': '',
    'RANGE': 'success',
    'LIST': 'warning'
  };
  return typeMap[type] || '';
};

// ========== 数据源操作 ==========
const handleAdd = () => {
  dialogTitle.value = '新增数据源';
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const handleEdit = (row: DataSource) => {
  dialogTitle.value = '编辑数据源';
  isEdit.value = true;
  Object.assign(form, row);
  dialogVisible.value = true;
};

const handleSave = async () => {
  saving.value = true;
  try {
    if (isEdit.value) {
      const response = await dataSourceApi.updateDataSource(form.name!, form);
      if (response.code === 200) {
        ElMessage.success('更新成功');
        dialogVisible.value = false;
        loadDataSources();
      }
    } else {
      const response = await dataSourceApi.createDataSource(form);
      if (response.code === 200) {
        ElMessage.success('新增成功');
        dialogVisible.value = false;
        loadDataSources();
      }
    }
  } catch (error: any) {
    console.error('Error saving data source:', error);
    ElMessage.error(error.response?.data?.message || '操作失败');
  } finally {
    saving.value = false;
  }
};

const handleDelete = (name: string) => {
  ElMessageBox.confirm('确定要删除该数据源吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await dataSourceApi.deleteDataSource(name);
      if (response.code === 200) {
        ElMessage.success('删除成功');
        loadDataSources();
      }
    } catch (error: any) {
      console.error('Error deleting data source:', error);
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

const handleTest = async (dataSource: DataSource) => {
  try {
    const response = await dataSourceApi.testConnection(dataSource);
    if (response.code === 200) {
      ElMessage.success('连接测试成功');
    } else {
      ElMessage.error('连接测试失败');
    }
  } catch (error: any) {
    console.error('Error testing connection:', error);
    ElMessage.error('连接测试失败');
  }
};

// 配置分片规则
const handleShardingConfig = (row: DataSource) => {
  activeTab.value = 'sharding';
  ElMessage.info('请切换到"分片规则配置"标签页进行操作');
};

const resetForm = () => {
  form.name = '';
  form.url = '';
  form.username = '';
  form.password = '';
  form.driverClassName = 'com.mysql.cj.jdbc.Driver';
  form.maximumPoolSize = 10;
  form.minimumIdle = 5;
};

// ========== 分片规则操作 ==========
const handleAddShardingRule = () => {
  shardingDialogTitle.value = '新增分片规则';
  shardingIsEdit.value = false;
  resetShardingForm();
  shardingDialogVisible.value = true;
};

const handleEditShardingRule = (row: ShardingRule) => {
  shardingDialogTitle.value = '编辑分片规则';
  shardingIsEdit.value = true;
  Object.assign(shardingForm, row);
  shardingFormConfig.value = row.configJson || '{}';
  shardingDialogVisible.value = true;
};

const handleSaveShardingRule = async () => {
  shardingSaving.value = true;
  try {
    const config = JSON.parse(shardingFormConfig.value);
    shardingForm.configJson = JSON.stringify(config);

    if (shardingIsEdit.value) {
      const response = await shardingRuleApi.updateShardingRule(shardingForm.name!, shardingForm as ShardingRule);
      if (response.code === 200) {
        ElMessage.success('更新成功');
        shardingDialogVisible.value = false;
        loadShardingRules();
      }
    } else {
      const response = await shardingRuleApi.createShardingRule(shardingForm as ShardingRule);
      if (response.code === 200) {
        ElMessage.success('新增成功');
        shardingDialogVisible.value = false;
        loadShardingRules();
      }
    }
  } catch (error: any) {
    console.error('Error saving sharding rule:', error);
    if (error instanceof SyntaxError) {
      ElMessage.error('配置格式错误，请输入正确的 JSON 格式');
    } else {
      ElMessage.error(error.response?.data?.message || '操作失败');
    }
  } finally {
    shardingSaving.value = false;
  }
};

const handleDeleteShardingRule = (name: string) => {
  ElMessageBox.confirm('确定要删除该分片规则吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await shardingRuleApi.deleteShardingRule(name);
      if (response.code === 200) {
        ElMessage.success('删除成功');
        loadShardingRules();
      }
    } catch (error: any) {
      console.error('Error deleting sharding rule:', error);
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

const resetShardingForm = () => {
  shardingForm.name = '';
  shardingForm.strategyType = 'HASH';
  shardingForm.dataSourceName = '';
  shardingForm.tableName = '';
  shardingForm.shardingColumn = '';
  shardingForm.description = '';
  shardingForm.configJson = '{}';
  shardingFormConfig.value = '{}';
};

// 组件挂载时加载数据
onMounted(() => {
  loadDataSources();
  loadShardingRules();
});
</script>

<style scoped>
.data-source {
  padding: 0;
}

.page-header {
  padding: 15px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  margin: -20px -20px 20px -20px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.sharding-toolbar {
  margin-bottom: 15px;
  padding: 10px 0;
}

.data-tabs {
  margin-top: 10px;
}

.url-text {
  color: #409eff;
  cursor: pointer;
  word-break: break-all;
}

.url-text:hover {
  text-decoration: underline;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  font-weight: 600;
}

:deep(.el-button + .el-button) {
  margin-left: 5px;
}

:deep(.el-tabs__item) {
  font-size: 15px;
}

:deep(.el-dialog__header) {
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__footer) {
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}
</style>
