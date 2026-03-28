<template>
  <div class="sharding-rule">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">
            <el-icon style="margin-right: 5px;">
              <Plus />
            </el-icon>
            新增分片规则
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-select v-model="filterDataSource" placeholder="全部数据源" clearable @change="filterShardingRules"
            style="width: 200px;">
            <el-option label="全部数据源" value="" />
            <el-option v-for="ds in dataSources" :key="ds.name" :label="ds.name" :value="ds.name" />
          </el-select>
        </div>
      </div>
      <el-table :data="filteredShardingRules" style="width: 100%" v-loading="loading"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="name" label="规则名称" width="150" show-overflow-tooltip />
        <el-table-column prop="strategyType" label="分片策略" width="120">
          <template #default="scope">
            <el-tag :type="getStrategyTagType(scope.row.strategyType)" size="small">{{ scope.row.strategyType
              }}</el-tag>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" label-width="120px" :rules="formRules">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="关联数据源" prop="dataSourceId">
          <el-select v-model="form.dataSourceId" placeholder="请选择数据源" value-key="id">
            <el-option v-for="ds in dataSources" :key="ds.id" :label="ds.name" :value="ds.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="逻辑表名" prop="tableName">
          <el-input v-model="form.tableName" placeholder="请输入逻辑表名" />
        </el-form-item>
        <el-form-item label="分片字段" prop="shardingColumn">
          <el-input v-model="form.shardingColumn" placeholder="请输入分片字段" />
        </el-form-item>

        <el-form-item label="分片策略" prop="strategyType">
          <el-select v-model="form.strategyType" placeholder="请选择分片策略" @change="handleStrategyChange">
            <el-option label="HASH" value="HASH" />
            <el-option label="RANGE" value="RANGE" />
            <el-option label="MONTH" value="MONTH" />
            <el-option label="YEAR" value="YEAR" />
            <el-option label="DAY" value="DAY" />
            <el-option label="YEAR_MONTH" value="YEAR_MONTH" />
          </el-select>
        </el-form-item>

        <!-- HASH 策略特有字段 -->
        <template v-if="form.strategyType === 'HASH'">
          <el-form-item label="分片数量" prop="shardingCount">
            <el-input-number v-model="form.shardingStrategy!.shardingCount" :min="1" :precision="0" />
          </el-form-item>
          <el-form-item label="分片起始值" prop="shardingStart">
            <el-input-number v-model="form.shardingStrategy!.shardingStart" :precision="0" />
          </el-form-item>
        </template>

        <!-- RANGE 策略特有字段 -->
        <template v-if="form.strategyType === 'RANGE'">
          <el-form-item label="范围起始值" prop="rangeStart">
            <el-input-number v-model="form.shardingStrategy!.rangeStart" :precision="0" />
          </el-form-item>
          <el-form-item label="分片步长" prop="rangeStep">
            <el-input-number v-model="form.shardingStrategy!.rangeStep" :min="1" :precision="0" />
          </el-form-item>
        </template>

        <!-- MONTH/DAY 策略特有字段 -->
        <template v-if="form.strategyType === 'MONTH' || form.strategyType === 'DAY'">
          <el-form-item label="是否填充零" prop="padZero">
            <el-select v-model="form.shardingStrategy!.padZero" placeholder="请选择">
              <el-option label="否" :value="false" />
              <el-option label="是" :value="true" />
            </el-select>
          </el-form-item>
        </template>

        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述信息" />
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import shardingRuleApi from '@/api/shardingRule';
import dataSourceApi from '@/api/datasource';
import type { ShardingRule, DataSource, ShardingStrategy } from '@/types';

// 分片规则列表
const shardingRules = ref<ShardingRule[]>([]);
const filteredShardingRules = ref<ShardingRule[]>([]);
const loading = ref<boolean>(false);

// 数据源列表
const dataSources = ref<DataSource[]>([]);

// 筛选数据源
const filterDataSource = ref<string>('');

// 对话框状态
const dialogVisible = ref<boolean>(false);
const dialogTitle = ref<string>('新增分片规则');
const isEdit = ref<boolean>(false);
const saving = ref<boolean>(false);

// 测试对话框
const testDialogVisible = ref<boolean>(false);
const testing = ref<boolean>(false);

// 表单引用
const formRef = ref<any>(null);

// 表单数据
const form = reactive<Partial<ShardingRule>>({
  id: undefined,
  name: '',
  strategyType: 'HASH',
  dataSourceId: undefined,
  dataSourceName: '',
  tableName: '',
  shardingColumn: '',
  shardingStrategy: {
    shardingCount: undefined,
    shardingStart: undefined,
    rangeStart: undefined,
    rangeStep: undefined,
    padZero: undefined
  } as ShardingStrategy,
  description: ''
});

// 测试表单
const testForm = reactive({
  shardingKey: ''
});

const currentRule = ref<ShardingRule | null>(null);

// 自定义校验器：分片数量
const validateShardingCount = (rule: any, value: any, callback: Function) => {
  if ((form.strategyType === 'HASH' || form.strategyType === 'RANGE') &&
    (value === undefined || value === null || value === '')) {
    callback(new Error('请输入分片数量'));
  } else {
    callback();
  }
};

// 自定义校验器：分片起始值
const validateShardingStart = (rule: any, value: any, callback: Function) => {
  if (form.strategyType === 'HASH' &&
    (value === undefined || value === null || value === '')) {
    callback(new Error('请输入分片起始值'));
  } else {
    callback();
  }
};

// 自定义校验器：范围起始值
const validateRangeStart = (rule: any, value: any, callback: Function) => {
  if (form.strategyType === 'RANGE' &&
    (value === undefined || value === null || value === '')) {
    callback(new Error('请输入范围起始值'));
  } else {
    callback();
  }
};

// 自定义校验器：分片步长
const validateRangeStep = (rule: any, value: any, callback: Function) => {
  if (form.strategyType === 'RANGE' &&
    (value === undefined || value === null || value === '')) {
    callback(new Error('请输入分片步长'));
  } else {
    callback();
  }
};

// 自定义校验器：是否填充零
const validatePadZero = (rule: any, value: any, callback: Function) => {
  if ((form.strategyType === 'MONTH' || form.strategyType === 'DAY') &&
    (value === undefined || value === null)) {
    callback(new Error('请选择是否填充零'));
  } else {
    callback();
  }
};

// 表单校验规则
const formRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  strategyType: [{ required: true, message: '请选择分片策略', trigger: 'change' }],
  dataSourceId: [{ required: true, message: '请选择关联数据源', trigger: 'change' }],
  tableName: [{ required: true, message: '请输入逻辑表名', trigger: 'blur' }],
  shardingColumn: [{ required: true, message: '请输入分片字段', trigger: 'blur' }],
  'strategyConfig.shardingCount': [{ validator: validateShardingCount, trigger: 'blur' }],
  'strategyConfig.shardingStart': [{ validator: validateShardingStart, trigger: 'blur' }],
  'strategyConfig.rangeStart': [{ validator: validateRangeStart, trigger: 'blur' }],
  'strategyConfig.rangeStep': [{ validator: validateRangeStep, trigger: 'blur' }],
  'strategyConfig.padZero': [{ validator: validatePadZero, trigger: 'change' }]
};

// 加载分片规则列表
const loadShardingRules = async () => {
  loading.value = true;
  try {
    const response = await shardingRuleApi.getAllShardingRules();
    if (response.code === 200 && response.data) {
      shardingRules.value = response.data;
      filterShardingRules();
    } else {
      shardingRules.value = [];
      filteredShardingRules.value = [];
    }
  } catch (error) {
    console.error('Error loading sharding rules:', error);
    ElMessage.error('加载分片规则失败');
    shardingRules.value = [];
    filteredShardingRules.value = [];
  } finally {
    loading.value = false;
  }
};

// 筛选分片规则
const filterShardingRules = () => {
  if (!filterDataSource.value) {
    filteredShardingRules.value = [...shardingRules.value];
  } else {
    filteredShardingRules.value = shardingRules.value.filter(
      rule => rule.dataSourceName === filterDataSource.value
    );
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
    'HASH': 'primary',
    'RANGE': 'info',
    'YEAR': 'success',
    'MONTH': 'success',
    'DAY': 'success',
    'YEAR_MONTH': 'success',
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

  // 重置表单
  resetForm();

  // 填充基本信息
  form.id = row.id;
  form.name = row.name;
  form.strategyType = row.strategyType;
  form.tableName = row.tableName;
  form.shardingColumn = row.shardingColumn;
  form.description = row.description;

  // 查找对应的数据源 ID
  const dataSource = dataSources.value.find(ds => ds.name === row.dataSourceName);
  if (dataSource) {
    form.dataSourceId = dataSource.id;
    form.dataSourceName = dataSource.name;
  }

  dialogVisible.value = true;
};

// 保存分片规则
const handleSave = async () => {
  // 表单校验
  if (!formRef.value) return;

  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return;

    saving.value = true;
    try {
      const payload = { ...form } as ShardingRule;
      form.strategyConfig = JSON.stringify(form.shardingStrategy);
      // 删除 shardingStrategy 字段，避免发送对象
      delete payload.shardingStrategy;

      if (isEdit.value) {
        const response = await shardingRuleApi.updateShardingRule(payload.id!, payload);
        if (response.code === 200) {
          ElMessage.success('更新成功');
          dialogVisible.value = false;
          loadShardingRules();
        }
      } else {
        const response = await shardingRuleApi.createShardingRule(payload as ShardingRule);
        if (response.code === 200) {
          ElMessage.success('新增成功');
          dialogVisible.value = false;
          loadShardingRules();
        }
      }
    } catch (error: any) {
      console.error('Error saving sharding rule:', error);
      ElMessage.error(error.response?.data?.message || '操作失败');
    } finally {
      saving.value = false;
    }
  });
};

// 删除分片规则
const handleDelete = (row: ShardingRule) => {
  ElMessageBox.confirm(`确定要删除分片规则 ${row.name} 吗？`, '警告', {
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
  form.id = undefined;
  form.name = '';
  form.strategyType = 'HASH';
  form.dataSourceId = undefined;
  form.dataSourceName = '';
  form.tableName = '';
  form.shardingColumn = '';
  form.description = '';

  // 初始化并重置 shardingStrategy
  if (!form.shardingStrategy) {
    form.shardingStrategy = {};
  }

  handleStrategyChange(form.strategyType);

  // 清空表单校验状态
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 处理策略变更
const handleStrategyChange = (strategyType: string) => {
  // 重置策略相关字段
  if (form.shardingStrategy) {
    form.shardingStrategy.shardingCount = undefined;
    form.shardingStrategy.shardingStart = undefined;
    form.shardingStrategy.rangeStart = undefined;
    form.shardingStrategy.rangeStep = undefined;
    form.shardingStrategy.padZero = undefined;

    // 根据策略类型设置默认值
    if (strategyType === 'HASH') {
      form.shardingStrategy.shardingCount = 1;
      form.shardingStrategy.shardingStart = 0;
    } else if (strategyType === 'RANGE') {
      form.shardingStrategy.rangeStart = 0;
      form.shardingStrategy.rangeStep = 1;
    } else if (strategyType === 'MONTH' || strategyType === 'DAY') {
      form.shardingStrategy.padZero = false;
    }
  }
};

// 组件挂载时加载数据
onMounted(() => {
  loadShardingRules();
  loadDataSources();
});
</script>

<style scoped>
.sharding-rule {
  padding: 20px;
}

.toolbar {
  margin-bottom: 15px;
  padding: 10px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
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
