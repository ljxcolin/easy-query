<template>
  <div class="sql-query">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>SQL查询</span>
          <div class="header-buttons">
            <el-input v-model="shardingKey" placeholder="分片键值" style="width: 200px; margin-right: 10px" />
            <el-button type="primary" @click="handleExecuteQuery">执行查询</el-button>
            <el-button @click="handleExecuteUpdate">执行更新</el-button>
          </div>
        </div>
      </template>
      <div class="editor-container">
        <div ref="editor" class="editor"></div>
      </div>
      <div class="result-container" v-if="result.length > 0">
        <el-table :data="result" style="width: 100%">
          <el-table-column
            v-for="(value, key) in result[0]"
            :key="key"
            :prop="key"
            :label="key"
          />
        </el-table>
      </div>
      <div class="message" v-if="message">
        {{ message }}
      </div>
    </el-card>
  </div>
</template>

<script>
import * as monaco from 'monaco-editor';
import sqlApi from '../api/sql';

export default {
  name: 'SqlQuery',
  data() {
    return {
      editor: null,
      shardingKey: '',
      result: [],
      message: ''
    };
  },
  mounted() {
    this.initEditor();
  },
  beforeUnmount() {
    if (this.editor) {
      this.editor.dispose();
    }
  },
  methods: {
    initEditor() {
      this.editor = monaco.editor.create(this.$refs.editor, {
        value: 'SELECT * FROM user WHERE id = ?',
        language: 'sql',
        theme: 'vs',
        automaticLayout: true,
        minimap: {
          enabled: false
        },
        scrollBeyondLastLine: false
      });
    },
    handleExecuteQuery() {
      const sql = this.editor.getValue();
      const sqlRequest = {
        sql: sql,
        shardingKey: this.shardingKey
      };
      sqlApi.executeQuery(sqlRequest).then(response => {
        if (response.code === 200) {
          this.result = response.data;
          this.message = `查询成功，返回 ${response.data.length} 条记录`;
        } else {
          this.result = [];
          this.message = `查询失败：${response.message}`;
        }
      });
    },
    handleExecuteUpdate() {
      const sql = this.editor.getValue();
      const sqlRequest = {
        sql: sql,
        shardingKey: this.shardingKey
      };
      sqlApi.executeUpdate(sqlRequest).then(response => {
        if (response.code === 200) {
          this.result = [];
          this.message = `更新成功，影响 ${response.data} 行`;
        } else {
          this.result = [];
          this.message = `更新失败：${response.message}`;
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

.header-buttons {
  display: flex;
  align-items: center;
}

.editor-container {
  margin: 20px 0;
}

.editor {
  height: 300px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.result-container {
  margin-top: 20px;
}

.message {
  margin-top: 10px;
  padding: 10px;
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  color: #67c23a;
}
</style>