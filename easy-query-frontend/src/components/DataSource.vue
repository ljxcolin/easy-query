<template>
  <div class="data-source">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据源管理</span>
          <el-button type="primary" @click="handleAdd">新增数据源</el-button>
        </div>
      </template>
      <el-table :data="dataSources" style="width: 100%">
        <el-table-column prop="name" label="数据源名称" width="120" />
        <el-table-column prop="url" label="连接URL" />
        <el-table-column prop="username" label="用户名" width="100" />
        <el-table-column prop="driverClassName" label="驱动类" width="200" />
        <el-table-column prop="maximumPoolSize" label="最大连接数" width="100" />
        <el-table-column prop="minimumIdle" label="最小空闲数" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.name)">删除</el-button>
            <el-button size="small" @click="handleTest(scope.row)">测试连接</el-button>
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
        <el-form-item label="数据源名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="连接URL">
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
          <el-button type="primary" @click="handleSave">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import dataSourceApi from '../api/datasource';

export default {
  name: 'DataSource',
  data() {
    return {
      dataSources: [],
      dialogVisible: false,
      dialogTitle: '新增数据源',
      form: {
        name: '',
        url: '',
        username: '',
        password: '',
        driverClassName: '',
        maximumPoolSize: 10,
        minimumIdle: 5
      }
    };
  },
  mounted() {
    this.loadDataSources();
  },
  methods: {
    loadDataSources() {
      dataSourceApi.getAllDataSources().then(response => {
        if (response.code === 200) {
          this.dataSources = response.data;
        }
      });
    },
    handleAdd() {
      this.dialogTitle = '新增数据源';
      this.form = {
        name: '',
        url: '',
        username: '',
        password: '',
        driverClassName: '',
        maximumPoolSize: 10,
        minimumIdle: 5
      };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.dialogTitle = '编辑数据源';
      this.form = { ...row };
      this.dialogVisible = true;
    },
    handleSave() {
      if (this.dialogTitle === '新增数据源') {
        dataSourceApi.createDataSource(this.form).then(response => {
          if (response.code === 200) {
            this.$message.success('新增成功');
            this.dialogVisible = false;
            this.loadDataSources();
          }
        });
      } else {
        dataSourceApi.updateDataSource(this.form.name, this.form).then(response => {
          if (response.code === 200) {
            this.$message.success('更新成功');
            this.dialogVisible = false;
            this.loadDataSources();
          }
        });
      }
    },
    handleDelete(name) {
      this.$confirm('确定要删除该数据源吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        dataSourceApi.deleteDataSource(name).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功');
            this.loadDataSources();
          }
        });
      });
    },
    handleTest(dataSource) {
      dataSourceApi.testConnection(dataSource).then(response => {
        if (response.code === 200) {
          this.$message.success('连接测试成功');
        } else {
          this.$message.error('连接测试失败');
        }
      });
    }
  }
};
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>