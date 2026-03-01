<template>
  <div class="sharding-rule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分片规则管理</span>
          <el-button type="primary" @click="handleAdd">新增规则</el-button>
        </div>
      </template>
      <el-table :data="shardingRules" style="width: 100%">
        <el-table-column prop="name" label="规则名称" width="120" />
        <el-table-column prop="strategyType" label="分片策略" width="120" />
        <el-table-column label="配置" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.name)">删除</el-button>
            <el-button size="small" @click="handleTest(scope.row)">测试规则</el-button>
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
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分片策略">
          <el-select v-model="form.strategyType">
            <el-option label="HASH" value="HASH" />
            <el-option label="RANGE" value="RANGE" />
            <el-option label="LIST" value="LIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置">
          <el-input
            v-model="formConfig"
            type="textarea"
            :rows="8"
            placeholder="请输入JSON格式的配置"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 测试规则对话框 -->
    <el-dialog
      v-model="testDialogVisible"
      title="测试分片规则"
      width="400px"
    >
      <el-form :model="testForm" label-width="120px">
        <el-form-item label="分片键值">
          <el-input v-model="testForm.shardingKey" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="testDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleTestRule">测试</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import shardingRuleApi from '../api/shardingRule';

export default {
  name: 'ShardingRule',
  data() {
    return {
      shardingRules: [],
      dialogVisible: false,
      testDialogVisible: false,
      dialogTitle: '新增分片规则',
      form: {
        name: '',
        strategyType: 'HASH',
        config: {}
      },
      formConfig: '',
      testForm: {
        shardingKey: ''
      },
      currentRule: null
    };
  },
  mounted() {
    this.loadShardingRules();
  },
  methods: {
    loadShardingRules() {
      shardingRuleApi.getAllShardingRules().then(response => {
        if (response.code === 200) {
          this.shardingRules = response.data;
        }
      });
    },
    handleAdd() {
      this.dialogTitle = '新增分片规则';
      this.form = {
        name: '',
        strategyType: 'HASH',
        config: {}
      };
      this.formConfig = JSON.stringify(this.form.config, null, 2);
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.dialogTitle = '编辑分片规则';
      this.form = { ...row };
      this.formConfig = JSON.stringify(this.form.config, null, 2);
      this.dialogVisible = true;
    },
    handleSave() {
      try {
        this.form.config = JSON.parse(this.formConfig);
        if (this.dialogTitle === '新增分片规则') {
          shardingRuleApi.createShardingRule(this.form).then(response => {
            if (response.code === 200) {
              this.$message.success('新增成功');
              this.dialogVisible = false;
              this.loadShardingRules();
            }
          });
        } else {
          shardingRuleApi.updateShardingRule(this.form.name, this.form).then(response => {
            if (response.code === 200) {
              this.$message.success('更新成功');
              this.dialogVisible = false;
              this.loadShardingRules();
            }
          });
        }
      } catch (e) {
        this.$message.error('配置格式错误，请输入正确的JSON格式');
      }
    },
    handleDelete(name) {
      this.$confirm('确定要删除该分片规则吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        shardingRuleApi.deleteShardingRule(name).then(response => {
          if (response.code === 200) {
            this.$message.success('删除成功');
            this.loadShardingRules();
          }
        });
      });
    },
    handleTest(row) {
      this.currentRule = row;
      this.testForm.shardingKey = '';
      this.testDialogVisible = true;
    },
    handleTestRule() {
      shardingRuleApi.testShardingRule(this.currentRule, this.testForm.shardingKey).then(response => {
        if (response.code === 200) {
          this.$message.success('测试结果：' + response.data);
          this.testDialogVisible = false;
        } else {
          this.$message.error('测试失败：' + response.message);
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