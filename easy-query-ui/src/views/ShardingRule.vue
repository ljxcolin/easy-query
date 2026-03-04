<template>
  <div class="sharding-rule">
    <div class="page-header">
      <h2 class="page-title">分片规则配置</h2>
    </div>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon style="margin-right: 5px;"><Plus /></el-icon>
          新增分片规则
        </el-button>
      </div>
      <el-table :data="shardingRules" style="width: 100%" v-loading="loading" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="name" label="规则名称" width="150" show-overflow-tooltip />
        <el-table-column prop="strategyType" label="分片策略" width="120">
          <template #default="scope">
            <el-tag :type="getStrategyTagType(scope.row.strategyType)" size="medium">{{ scope.row.strategyType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataSourceName" label="关联数据源" width="150">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.dataSourceName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tableName" label="逻辑表名" width="150" />
        <el-table-column prop="shardingColumn" label="分片字段" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="success" @click="handleTest(scope.row)">测试</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.name)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="form" label-width="120px">
        <el-form-item label="规则名称">
          <el-input v-model="form.name" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="分片策略">
          <el-select v-model="form.strategyType" placeholder="请选择分片策略">
            <el-option label="HASH" value="HASH" />
            <el-option label="RANGE" value="RANGE" />
            <el-option label="LIST" value="LIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联数据源">
          <el-select v-model="form.dataSourceId" placeholder="请选择数据源" value-key="id">
            <el-option
              v-for="ds in dataSources"
              :key="ds.id"
              :label="ds.name"
              :value="ds.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="逻辑表名">
          <el-input v-model="form.tableName" placeholder="请输入逻辑表名" />
        </el-form-item>
        <el-form-item label="分片字段">
          <el-input v-model="form.shardingColumn" placeholder="请输入分片字段" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述信息" />
        </el-form-item>
        <el-form-item label="配置 JSON">
          <el-input
            v-model="formConfig"
            type="textarea"
            :rows="8"
            placeholder="请输入 JSON 格式的配置，例如：{&#10;  &quot;dataSourceCount&quot;: 2,&#10;  &quot;tableCount&quot;: 4&#10;}"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 测试规则对话框 -->
    <el-dialog
      v-model="testDialogVisible"
      title="测试分片规则"
      width="450px"
    >
      <el-form :model="testForm" label-width="100px">
        <el-form-item label="分片键值">
          <el-input v-model="testForm.shardingKey" placeholder="请输入分片键值" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="testDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleTestRule" :loading="testing">测试</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import shardingRuleApi from '@/api/shardingRule';
import dataSourceApi from '@/api/datasource';
import type { ShardingRule, DataSource } from '@/types';

// 分片规则列表
const shardingRules = ref<ShardingRule[]>([]);
const loading = ref<boolean>(false);

// 数据源列表
const dataSources = ref<DataSource[]>([]);

// 对话框状态
const dialogVisible = ref<boolean>(false);
const dialogTitle = ref<string>('新增分片规则');
const isEdit = ref<boolean>(false);
const saving = ref<boolean>(false);

// 测试对话框
const testDialogVisible = ref<boolean>(false);
const testing = ref<boolean>(false);

// 表单数据
const form = reactive<Partial<ShardingRule>>({
  name: '',
  strategyType: 'HASH',
  dataSourceName: '',
  tableName: '',
  shardingColumn: '',
  description: '',
  configJson: '{}'
});

const formConfig = ref<string>('{}');

// 测试表单
const testForm = reactive({
  shardingKey: ''
});

const currentRule = ref<ShardingRule | null>(null);

// 加载分片规则列表
const loadShardingRules = async () => {
  loading.value = true;
  try {
    const response = await shardingRuleApi.getAllShardingRules();
    if (response.code === 200 && response.data) {
      shardingRules.value = response.data;
    } else {
      shardingRules.value = [];
    }
  } catch (error) {
    console.error('Error loading sharding rules:', error);
    ElMessage.error('加载分片规则失败');
    shardingRules.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载数据源列表
const loadDataSources = async () => {
  try {
    const response = await dataSourceApi.getAllDataSources();
    if (response.code === 200 && response.data) {
      dataSources.value = response.data;
    }
  } catch (error) {
    console.error('Error loading data sources:', error);
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

// 新增分片规则
const handleAdd = () => {
  dialogTitle.value = '新增分片规则';
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

// 编辑分片规则
const handleEdit = (row: ShardingRule) => {
  dialogTitle.value = '编辑分片规则';
  isEdit.value = true;
  Object.assign(form, row);
  // 查找对应的数据源 ID
  const dataSource = dataSources.value.find(ds => ds.name === row.dataSourceName);
  if (dataSource) {
    form.dataSourceId = dataSource.id;
  }
  formConfig.value = row.configJson || '{}';
  dialogVisible.value = true;
};

// 保存分片规则
const handleSave = async () => {
  saving.value = true;
  try {
    // 验证 JSON 格式
    const config = JSON.parse(formConfig.value);
    form.configJson = JSON.stringify(config);

    if (isEdit.value) {
      const response = await shardingRuleApi.updateShardingRule(form.name!, form);
      if (response.code === 200) {
        ElMessage.success('更新成功');
        dialogVisible.value = false;
        loadShardingRules();
      }
    } else {
      const response = await shardingRuleApi.createShardingRule(form as ShardingRule);
      if (response.code === 200) {
        ElMessage.success('新增成功');
        dialogVisible.value = false;
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
    saving.value = false;
  }
};

// 删除分片规则
const handleDelete = (name: string) => {
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

// 测试分片规则
const handleTest = (row: ShardingRule) => {
  currentRule.value = row;
  testForm.shardingKey = '';
  testDialogVisible.value = true;
};

// 执行测试
const handleTestRule = async () => {
  testing.value = true;
  try {
    if (!currentRule.value) return;
    
    const response = await shardingRuleApi.testShardingRule(currentRule.value, testForm.shardingKey);
    if (response.code === 200) {
      ElMessage.success('测试结果：' + response.data);
      testDialogVisible.value = false;
    } else {
      ElMessage.error('测试失败：' + response.message);
    }
  } catch (error: any) {
    console.error('Error testing sharding rule:', error);
    ElMessage.error('测试失败');
  } finally {
    testing.value = false;
  }
};

// 重置表单
const resetForm = () => {
  form.name = '';
  form.strategyType = 'HASH';
  form.dataSourceId = undefined;
  form.dataSourceName = undefined;
  form.tableName = '';
  form.shardingColumn = '';
  form.description = '';
  form.configJson = '{}';
  formConfig.value = '{}';
};

// 组件挂载时加载数据
onMounted(() => {
  loadShardingRules();
  loadDataSources();
});
</script>

<style scoped>
.sharding-rule {
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

.toolbar {
  margin-bottom: 15px;
  padding: 10px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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
</style>
