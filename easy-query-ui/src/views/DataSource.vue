<template>
  <div class="data-source">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">新增数据源</el-button>
        </div>
      </div>
      <el-table :data="dataSources" style="width: 100%" v-loading="loading" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="name" label="数据源名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="url" label="连接 URL" min-width="300" show-overflow-tooltip>
          <template #default="scope">
            <span class="url-text">{{ scope.row.url }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="driverClassName" label="驱动类" min-width="200" show-overflow-tooltip />
        <el-table-column prop="maximumPoolSize" label="最大连接数" min-width="100" align="center">
          <template #default="scope">
            <el-tag size="small" type="info">{{ scope.row.maximumPoolSize }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="minimumIdle" label="最小空闲数" min-width="100" align="center">
          <template #default="scope">
            <el-tag size="small" type="success">{{ scope.row.minimumIdle }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="connectionStatus" label="连接状态" min-width="110" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.connectionStatus === 'CONNECTED' ? 'success' : 'danger'" size="small">
              {{ scope.row.connectionStatus === 'CONNECTED' ? '连接成功' : '连接失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleViewDetail(scope.row)">查看</el-button>
            <el-button size="small" type="warning" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="success" :loading="testingId === scope.row.id"
              @click="handleTest(scope.row)">测试</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 数据源新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="form.name" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="连接 URL" prop="url">
          <el-input v-model="form.url" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" :type="passwordVisible ? 'text' : 'password'"
            :placeholder="isEdit ? '不修改请留空' : '请输入数据库密码'">
            <template #suffix>
              <el-icon @click="passwordVisible = !passwordVisible" style="cursor: pointer">
                <component :is="passwordVisible ? 'View' : 'Hide'" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="驱动类" prop="driverClassName">
          <el-input v-model="form.driverClassName" />
        </el-form-item>
        <el-form-item label="最大连接数" prop="maximumPoolSize">
          <el-input-number v-model="form.maximumPoolSize" :min="1" />
        </el-form-item>
        <el-form-item label="最小空闲数" prop="minimumIdle">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import dataSourceApi from '@/api/datasource';
import type { DataSource } from '@/types';

const router = useRouter();

// 数据源相关
const dataSources = ref<DataSource[]>([]);
const loading = ref<boolean>(false);

// 对话框状态
const dialogVisible = ref<boolean>(false);
const dialogTitle = ref<string>('新增数据源');
const isEdit = ref<boolean>(false);
const saving = ref<boolean>(false);
const formRef = ref<FormInstance>();
const passwordVisible = ref<boolean>(false);
// 正在检测连接的数据源 ID
const testingId = ref<number | null>(null);

// 密码仅在新增时必填；编辑时留空表示不修改
const formRules = computed<FormRules>(() => ({
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  url: [{ required: true, message: '请输入连接 URL', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: isEdit.value ? [] : [{ required: true, message: '请输入密码', trigger: 'blur' }],
  driverClassName: [{ required: true, message: '请输入驱动类', trigger: 'blur' }],
  maximumPoolSize: [{ required: true, message: '请输入最大连接数', trigger: 'blur' }],
  minimumIdle: [{ required: true, message: '请输入最小空闲数', trigger: 'blur' }]
}));

// 表单数据
const form = reactive<DataSource>({
  id: undefined,
  name: '',
  url: '',
  username: '',
  password: '',
  driverClassName: 'com.mysql.cj.jdbc.Driver',
  maximumPoolSize: 10,
  minimumIdle: 5
});

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
  // 服务端不返回密码，编辑时留空表示保持原密码
  form.password = '';
  dialogVisible.value = true;
};

const handleSave = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (!valid) return;

    saving.value = true;
    try {
      // 仅提交可写字段：不携带 id / 连接状态等只读字段
      const payload: DataSource = {
        name: form.name,
        url: form.url,
        username: form.username,
        driverClassName: form.driverClassName,
        maximumPoolSize: form.maximumPoolSize,
        minimumIdle: form.minimumIdle,
        connectionTimeout: form.connectionTimeout,
        idleTimeout: form.idleTimeout
      };
      // 编辑时密码留空则不提交，由后端保留原密码
      if (form.password) {
        payload.password = form.password;
      }

      if (isEdit.value) {
        const response = await dataSourceApi.updateDataSourceById(form.id!, payload);
        if (response.code === 200) {
          ElMessage.success('更新成功');
          dialogVisible.value = false;
          loadDataSources();
        } else {
          ElMessage.error(response.message || '更新失败');
        }
      } else {
        const response = await dataSourceApi.createDataSource(payload);
        if (response.code === 200) {
          ElMessage.success('新增成功');
          dialogVisible.value = false;
          loadDataSources();
        } else {
          ElMessage.error(response.message || '新增失败');
        }
      }
    } catch (error: any) {
      console.error('Error saving data source:', error);
      ElMessage.error(error.response?.data?.message || '操作失败');
    } finally {
      saving.value = false;
    }
  });
};

const handleDelete = (row: DataSource) => {
  ElMessageBox.confirm('确定要删除该数据源吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await dataSourceApi.deleteDataSourceById(row.id!);
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
  if (!dataSource.id) return;
  testingId.value = dataSource.id;
  try {
    const response = await dataSourceApi.testConnection(dataSource.id);
    if (response.code === 200 && response.data) {
      // 直接同步该行最新连接状态
      dataSource.connectionStatus = response.data.connectionStatus;
      dataSource.connectionStatusLabel = response.data.connectionStatusLabel;
      if (response.data.connectionStatus === 'CONNECTED') {
        ElMessage.success('连接成功');
      } else {
        ElMessage.error('连接失败，请检查连接配置');
      }
    } else {
      ElMessage.error(response.message || '连接检测失败');
    }
  } catch (error: any) {
    console.error('Error testing connection:', error);
    ElMessage.error('连接检测失败');
  } finally {
    testingId.value = null;
  }
};

// 查看详情
const handleViewDetail = (row: DataSource) => {
  router.push(`/console/data-source/${row.id}`);
};

const resetForm = () => {
  form.id = undefined;
  form.name = '';
  form.url = '';
  form.username = '';
  form.password = '';
  form.driverClassName = 'com.mysql.cj.jdbc.Driver';
  form.maximumPoolSize = 10;
  form.minimumIdle = 5;
};

// 组件挂载时加载数据
onMounted(() => {
  loadDataSources();
});
</script>

<style scoped>
.data-source {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
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

.url-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
