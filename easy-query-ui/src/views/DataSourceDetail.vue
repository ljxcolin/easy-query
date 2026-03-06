<template>
  <div class="data-source-detail">
    <div class="content-wrapper">
      <!-- 数据源基本信息 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="数据源名称">{{ dataSource.name }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="dataSource.status === 1 ? 'success' : 'danger'">
              {{ dataSource.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="连接 URL" :span="2">
            <span class="detail-text">{{ dataSource.url }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="用户名">{{ dataSource.username }}</el-descriptions-item>
          <el-descriptions-item label="驱动类">{{ dataSource.driverClassName }}</el-descriptions-item>
          <el-descriptions-item label="最大连接数">{{ dataSource.maximumPoolSize }}</el-descriptions-item>
          <el-descriptions-item label="最小空闲数">{{ dataSource.minimumIdle }}</el-descriptions-item>
        </el-descriptions>
        <div class="card-actions">
          <el-button type="primary" @click="handleEdit">编辑</el-button>
          <el-button type="success" @click="handleTest">测试连接</el-button>
        </div>
      </el-card>

      <!-- 分片规则管理 -->
      <el-card class="sharding-card">
        <template #header>
          <div class="card-header">
            <span>分片规则配置</span>
            <el-button type="primary" @click="handleAddShardingRule">
              <el-icon style="margin-right: 5px;">
                <Plus />
              </el-icon>
              新增分片规则
            </el-button>
          </div>
        </template>
        <el-table :data="shardingRules" style="width: 100%" v-loading="loading"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
          <el-table-column prop="name" label="规则名称" width="150" show-overflow-tooltip />
          <el-table-column prop="strategyType" label="分片策略" width="120">
            <template #default="scope">
              <el-tag :type="getStrategyTagType(scope.row.strategyType)" size="default">{{ scope.row.strategyType
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="tableName" label="逻辑表名" width="150" />
          <el-table-column prop="shardingColumn" label="分片字段" width="120" />
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" @click="handleEditShardingRule(scope.row)">编辑</el-button>
              <el-button size="small" type="success" @click="handleTestShardingRule(scope.row)">测试</el-button>
              <el-button size="small" type="danger" @click="handleDeleteShardingRule(scope.row.name)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 数据源编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="'编辑数据源：' + dataSource.name" width="600px">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="连接 URL">
          <el-input v-model="editForm.url" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="editForm.password" type="password" placeholder="不修改请留空" />
        </el-form-item>
        <el-form-item label="驱动类">
          <el-input v-model="editForm.driverClassName" />
        </el-form-item>
        <el-form-item label="最大连接数">
          <el-input-number v-model="editForm.maximumPoolSize" :min="1" />
        </el-form-item>
        <el-form-item label="最小空闲数">
          <el-input-number v-model="editForm.minimumIdle" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分片规则编辑对话框 -->
    <el-dialog v-model="shardingDialogVisible" :title="shardingDialogTitle" width="600px">
      <el-form ref="shardingFormRef" :model="shardingForm" label-width="120px" :rules="shardingFormRules">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="shardingForm.name" />
        </el-form-item>
        <el-form-item label="关联数据源" prop="dataSourceName">
          <el-select v-model="shardingForm.dataSourceName" placeholder="请选择数据源" :disabled="true">
            <el-option :label="dataSourceName" :value="dataSourceName" />
          </el-select>
        </el-form-item>
        <el-form-item label="逻辑表名" prop="tableName">
          <el-input v-model="shardingForm.tableName" />
        </el-form-item>
        <el-form-item label="分片字段" prop="shardingColumn">
          <el-input v-model="shardingForm.shardingColumn" />
        </el-form-item>

        <el-form-item label="分片策略" prop="strategyType">
          <el-select v-model="shardingForm.strategyType" :disabled="shardingIsEdit" @change="handleStrategyChange">
            <el-option label="HASH" value="HASH" />
            <el-option label="RANGE" value="RANGE" />
            <el-option label="YEAR" value="YEAR" />
            <el-option label="MONTH" value="MONTH" />
            <el-option label="DAY" value="DAY" />
            <el-option label="YEAR_MONTH" value="YEAR_MONTH" />
          </el-select>
        </el-form-item>

        <!-- HASH 策略特有字段 -->
        <template v-if="shardingForm.strategyType === 'HASH'">
          <el-form-item label="分片数量" prop="shardingStrategy.shardingCount">
            <el-input-number v-model="shardingForm.shardingStrategy!.shardingCount" :min="1" :precision="0" />
          </el-form-item>
          <el-form-item label="分片起始值" prop="shardingStrategy.shardingStart">
            <el-input-number v-model="shardingForm.shardingStrategy!.shardingStart" :precision="0" />
          </el-form-item>
        </template>

        <!-- RANGE 策略特有字段 -->
        <template v-if="shardingForm.strategyType === 'RANGE'">
          <el-form-item label="范围起始值" prop="shardingStrategy.rangeStart">
            <el-input-number v-model="shardingForm.shardingStrategy!.rangeStart" :precision="0" />
          </el-form-item>
          <el-form-item label="分片步长" prop="shardingStrategy.rangeStep">
            <el-input-number v-model="shardingForm.shardingStrategy!.rangeStep" :min="1" :precision="0" />
          </el-form-item>
        </template>

        <template v-if="shardingForm.strategyType === 'MONTH' || shardingForm.strategyType === 'DAY'">
          <el-form-item label="是否填充零" prop="shardingStrategy.padZero">
            <el-select v-model="shardingForm.shardingStrategy!.padZero" placeholder="请选择">
              <el-option label="否" :value="false" />
              <el-option label="是" :value="true" />
            </el-select>
          </el-form-item>
        </template>

        <el-form-item label="描述" prop="description">
          <el-input v-model="shardingForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shardingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveShardingRule" :loading="shardingSaving">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 测试分片规则对话框 -->
    <el-dialog v-model="testDialogVisible" title="测试分片规则" width="450px">
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
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import dataSourceApi from '@/api/datasource';
import shardingRuleApi from '@/api/shardingRule';
import type { DataSource, ShardingRule, ShardingStrategy } from '@/types';

const route = useRoute();
const dataSourceId = ref<number>(parseInt(route.params.id as string));
// 数据源名称（用于显示）
const dataSourceName = ref<string>('');

// 数据源详情
const dataSource = reactive<DataSource>({
  id: 0,
  name: '',
  url: '',
  username: '',
  password: '',
  driverClassName: '',
  maximumPoolSize: 0,
  minimumIdle: 0,
  status: 0
});

// 分片规则列表
const shardingRules = ref<ShardingRule[]>([]);
const loading = ref<boolean>(false);

// 编辑对话框
const editDialogVisible = ref<boolean>(false);
const saving = ref<boolean>(false);

const editForm = reactive<DataSource>({
  name: '',
  url: '',
  username: '',
  password: '',
  driverClassName: '',
  maximumPoolSize: 0,
  minimumIdle: 0
});

// 分片规则对话框
const shardingDialogVisible = ref<boolean>(false);
const shardingDialogTitle = ref<string>('');
const shardingIsEdit = ref<boolean>(false);
const shardingSaving = ref<boolean>(false);

// 分片规则表单引用
const shardingFormRef = ref<any>(null);

// 自定义校验器：分片数量（仅 HASH 策略需要）
const validateShardingCount = (rule: any, value: any, callback: Function) => {
  if (shardingForm.strategyType === 'HASH') {
    if (value === undefined || value === null || value === '') {
      callback(new Error('请输入分片数量'));
    } else if (value < 1) {
      callback(new Error('分片数量必须大于等于1'));
    } else {
      callback();
    }
  } else {
    callback();
  }
};

// 自定义校验器：起始索引（仅 HASH 策略需要）
const validateShardingStart = (rule: any, value: any, callback: Function) => {
  if (shardingForm.strategyType === 'HASH' &&
    (value === undefined || value === null || value === '')) {
    callback(new Error('请输入分片起始值'));
  } else {
    callback();
  }
};

// 自定义校验器：起始值（仅 RANGE 策略需要）
const validateRangeStart = (rule: any, value: any, callback: Function) => {
  if (shardingForm.strategyType === 'RANGE' &&
    (value === undefined || value === null || value === '')) {
    callback(new Error('请输入起始值'));
  } else {
    callback();
  }
};

// 自定义校验器：分片步长（仅 RANGE 策略需要）
const validateRangeStep = (rule: any, value: any, callback: Function) => {
  if (shardingForm.strategyType === 'RANGE' &&
    (value === undefined || value === null || value === '')) {
    callback(new Error('请输入分片步长'));
  } else {
    callback();
  }
};

// 自定义校验器：是否填充零（仅 MONTH 和 DAY 策略需要）
const validatePadZero = (rule: any, value: any, callback: Function) => {
  if ((shardingForm.strategyType === 'MONTH' || shardingForm.strategyType === 'DAY') && 
      (value === undefined || value === null)) {
    callback(new Error('请选择是否填充零'));
  } else {
    callback();
  }
};

// 分片规则表单校验规则
const shardingFormRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  strategyType: [{ required: true, message: '请选择分片策略', trigger: 'change' }],
  dataSourceName: [{ required: true, message: '请选择关联数据源', trigger: 'change' }],
  tableName: [{ required: true, message: '请输入逻辑表名', trigger: 'blur' }],
  shardingColumn: [{ required: true, message: '请输入分片字段', trigger: 'blur' }],
  'shardingStrategy.shardingCount': [{ validator: validateShardingCount, trigger: 'blur' }],
  'shardingStrategy.shardingStart': [{ validator: validateShardingStart, trigger: 'blur' }],
  'shardingStrategy.rangeStart': [{ validator: validateRangeStart, trigger: 'blur' }],
  'shardingStrategy.rangeStep': [{ validator: validateRangeStep, trigger: 'blur' }],
  'shardingStrategy.padZero': [{ validator: validatePadZero, trigger: 'change' }],
};

const shardingForm = reactive<Partial<ShardingRule>>({
  id: undefined,
  name: '',
  strategyType: 'HASH',
  dataSourceId: dataSourceId.value,
  dataSourceName: dataSourceName.value,
  tableName: '',
  shardingColumn: '',
  shardingStrategy: {
    shardingCount: undefined,
    shardingStart: undefined,
    rangeStart: undefined,
    rangeStep: undefined,
    padZero: undefined
  } as ShardingStrategy,
  description: '',
});

// 测试对话框
const testDialogVisible = ref<boolean>(false);
const testing = ref<boolean>(false);
const testForm = reactive({
  shardingKey: ''
});
const currentRule = ref<ShardingRule | null>(null);

// 加载数据源详情
const loadDataSourceDetail = async () => {
  try {
    const response = await dataSourceApi.getDataSourceById(dataSourceId.value);
    if (response.code === 200 && response.data) {
      Object.assign(dataSource, response.data);
      dataSourceName.value = response.data.name;
    }
  } catch (error: any) {
    console.error('Error loading data source detail:', error);
    ElMessage.error('加载数据源详情失败');
  }
};

// 加载该数据源的分片规则
const loadShardingRules = async () => {
  loading.value = true;
  try {
    const response = await shardingRuleApi.getAllShardingRules();
    if (response.code === 200 && response.data) {
      shardingRules.value = response.data.filter(
        rule => rule.dataSourceId === dataSourceId.value
      );
      // 初始化 shardingStrategy 字段
      shardingRules.value.forEach(rule => {
        // 初始化 shardingStrategy 字段
        if (rule.strategyConfig) {
          // 将 strategyConfig 转换为 shardingStrategy
          rule.shardingStrategy = JSON.parse(rule.strategyConfig!);
        } else {
          rule.shardingStrategy = {
            shardingCount: undefined,
            shardingStart: undefined,
            rangeStart: undefined,
            rangeStep: undefined,
            padZero: undefined,
          };
        }
      });
    }
  } catch (error) {
    console.error('Error loading sharding rules:', error);
    ElMessage.error('加载分片规则失败');
    shardingRules.value = [];
  } finally {
    loading.value = false;
  }
};

// 获取策略标签类型
const getStrategyTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'HASH': 'primary',
    'RANGE': 'info',
    'YEAR': 'success',
    'MONTH': 'success',
    'DAY': 'success',
    'YEAR_MONTH': 'success',
  };
  return typeMap[type] || '';
};

// 处理分片策略变更
const handleStrategyChange = (strategyType: string) => {
  // 切换策略时重置策略相关字段
  resetShardingStrategyForm(strategyType);
};

// 编辑数据源
const handleEdit = () => {
  Object.assign(editForm, dataSource);
  editDialogVisible.value = true;
};

// 保存编辑
const handleSaveEdit = async () => {
  saving.value = true;
  try {
    const response = await dataSourceApi.updateDataSourceById(editForm.id!, editForm);
    if (response.code === 200) {
      ElMessage.success('更新成功');
      editDialogVisible.value = false;
      loadDataSourceDetail();
    }
  } catch (error: any) {
    console.error('Error updating data source:', error);
    ElMessage.error(error.response?.data?.message || '更新失败');
  } finally {
    saving.value = false;
  }
};

// 测试连接
const handleTest = async () => {
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

// 新增分片规则
const handleAddShardingRule = () => {
  shardingDialogTitle.value = '新增分片规则';
  shardingIsEdit.value = false;
  resetShardingForm();
  resetShardingStrategyForm(shardingForm.strategyType || 'HASH');
  shardingForm.dataSourceId = dataSourceId.value;
  shardingForm.dataSourceName = dataSourceName.value;
  shardingDialogVisible.value = true;
};

// 编辑分片规则
const handleEditShardingRule = (row: ShardingRule) => {
  shardingDialogTitle.value = '编辑分片规则';
  shardingIsEdit.value = true;
  
  // 先重置表单
  resetShardingForm();
  
  // 填充基本信息
  shardingForm.id = row.id;
  shardingForm.name = row.name;
  shardingForm.strategyType = row.strategyType;
  shardingForm.dataSourceId = row.dataSourceId || dataSourceId.value;
  shardingForm.dataSourceName = dataSourceName.value;
  shardingForm.tableName = row.tableName;
  shardingForm.shardingColumn = row.shardingColumn;
  shardingForm.shardingStrategy = row.shardingStrategy;
  shardingForm.description = row.description;
  
  shardingDialogVisible.value = true;
};

// 保存分片规则
const handleSaveShardingRule = async () => {
  // 表单校验
  if (!shardingFormRef.value) return;

  await shardingFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return;

    shardingSaving.value = true;
    try {
      // 确保保存数据源 ID
      shardingForm.dataSourceId = dataSourceId.value;

      // 解构 shardingForm 到一个新对象
      const payload = { ...shardingForm } as ShardingRule ;
      payload.strategyConfig = JSON.stringify(shardingForm.shardingStrategy);
      // 删除 shardingStrategy 字段，避免发送对象
      delete payload.shardingStrategy;

      if (shardingIsEdit.value) {
        const response = await shardingRuleApi.updateShardingRule(payload.id!, payload);
        if (response.code === 200) {
          ElMessage.success('更新成功');
          shardingDialogVisible.value = false;
          loadShardingRules();
        }
      } else {
        const response = await shardingRuleApi.createShardingRule(payload);
        if (response.code === 200) {
          ElMessage.success('新增成功');
          shardingDialogVisible.value = false;
          loadShardingRules();
        }
      }
    } catch (error: any) {
      console.error('Error saving sharding rule:', error);
      ElMessage.error(error.response?.data?.message || '操作失败');
    } finally {
      shardingSaving.value = false;
    }
  });
};

// 删除分片规则
const handleDeleteShardingRule = (row: ShardingRule) => {
  ElMessageBox.confirm('确定要删除[' + row.name + ']吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await shardingRuleApi.deleteShardingRule(row.id!);
      if (response.code === 200) {
        ElMessage.success('删除成功');
        loadShardingRules();
      }
    } catch (error: any) {
      console.error('Error deleting sharding rule:', error);
      ElMessage.error('删除失败');
    }
  }).catch(() => { });
};

// 测试分片规则
const handleTestShardingRule = (row: ShardingRule) => {
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

// 重置分片表单
const resetShardingForm = () => {
  shardingForm.id = undefined;
  shardingForm.name = '';
  shardingForm.dataSourceId = dataSourceId.value;
  shardingForm.dataSourceName = dataSourceName.value;
  shardingForm.tableName = '';
  shardingForm.shardingColumn = '';
  shardingForm.strategyType = 'HASH';
  shardingForm.description = '';

  // 清空表单校验状态
  if (shardingFormRef.value) {
    shardingFormRef.value.clearValidate();
  }
};

// 重置分片策略表单
const resetShardingStrategyForm = (strategyType: string) => {
  // 初始化 strategyConfig
  if (!shardingForm.shardingStrategy) {
    shardingForm.shardingStrategy = {};
  }
  
  shardingForm.shardingStrategy.shardingCount = undefined;
  shardingForm.shardingStrategy.shardingStart = undefined;
  shardingForm.shardingStrategy.rangeStart = undefined;
  shardingForm.shardingStrategy.rangeStep = undefined;
  shardingForm.shardingStrategy.padZero = undefined;
  
  if (strategyType === 'HASH') {
    shardingForm.shardingStrategy.shardingCount = 1;
    shardingForm.shardingStrategy.shardingStart = 0;
  }
  if (strategyType === 'RANGE') {
    shardingForm.shardingStrategy.rangeStart = 0;
    shardingForm.shardingStrategy.rangeStep = 1;
  }
  if (strategyType === 'MONTH' || strategyType === 'DAY') {
    shardingForm.shardingStrategy.padZero = false;
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadDataSourceDetail();
  loadShardingRules();
});
</script>

<style scoped>
.data-source-detail {
  padding: 20px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card,
.sharding-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.card-actions {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
}

.detail-text {
  color: #606266;
  word-break: break-all;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  width: 120px;
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
