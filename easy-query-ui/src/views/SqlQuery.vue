<template>
  <div class="sql-query">
    <div class="page-header">
      <h2 class="page-title">SQL 查询</h2>
    </div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>SQL 查询</span>
          <div class="header-buttons">
            <el-input v-model="shardingKey" placeholder="分片键值" style="width: 200px; margin-right: 10px" />
            <el-button type="primary" @click="handleExecuteQuery" :loading="executing">执行查询</el-button>
            <el-button @click="handleExecuteUpdate" :loading="executing">执行更新</el-button>
          </div>
        </div>
      </template>
      <div class="editor-container">
        <div ref="editorRef" class="editor"></div>
      </div>
      <div class="result-container" v-if="result.length > 0">
        <el-table :data="result" style="width: 100%">
          <el-table-column
            v-for="(value, key) in result[0]"
            :key="key as string"
            :prop="key as string"
            :label="key as string"
          />
        </el-table>
      </div>
      <div class="message" v-if="message" :class="messageType">
        {{ message }}
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { ElMessage } from 'element-plus';
import * as monaco from 'monaco-editor';
import sqlApi from '@/api/sql';
import type { SqlRequest } from '@/types';

// 编辑器引用
const editorRef = ref<HTMLElement | null>(null);
let editorInstance: monaco.editor.IStandaloneCodeEditor | null = null;

// 状态
const shardingKey = ref<string>('');
const result = ref<any[]>([]);
const message = ref<string>('');
const messageType = ref<'success' | 'error' | ''>('');
const executing = ref<boolean>(false);

// 初始化编辑器
const initEditor = () => {
  if (!editorRef.value) return;
  
  editorInstance = monaco.editor.create(editorRef.value, {
    value: 'SELECT * FROM user WHERE id = ?',
    language: 'sql',
    theme: 'vs',
    automaticLayout: true,
    minimap: {
      enabled: false
    },
    scrollBeyondLastLine: false,
    fontSize: 14
  });
};

// 执行查询
const handleExecuteQuery = async () => {
  if (!editorInstance) return;
  
  executing.value = true;
  message.value = '';
  
  try {
    const sqlRequest: SqlRequest = {
      sql: editorInstance.getValue(),
      shardingKey: shardingKey.value
    };
    
    const response = await sqlApi.executeQuery(sqlRequest);
    if (response.code === 200) {
      result.value = response.data || [];
      message.value = `查询成功，返回 ${response.data?.length || 0} 条记录`;
      messageType.value = 'success';
    } else {
      result.value = [];
      message.value = `查询失败：${response.message}`;
      messageType.value = 'error';
    }
  } catch (error: any) {
    console.error('Error executing query:', error);
    result.value = [];
    message.value = `查询失败：${error.response?.data?.message || '未知错误'}`;
    messageType.value = 'error';
    ElMessage.error('查询执行失败');
  } finally {
    executing.value = false;
  }
};

// 执行更新
const handleExecuteUpdate = async () => {
  if (!editorInstance) return;
  
  executing.value = true;
  message.value = '';
  
  try {
    const sqlRequest: SqlRequest = {
      sql: editorInstance.getValue(),
      shardingKey: shardingKey.value
    };
    
    const response = await sqlApi.executeUpdate(sqlRequest);
    if (response.code === 200) {
      result.value = [];
      message.value = `更新成功，影响 ${response.data} 行`;
      messageType.value = 'success';
      ElMessage.success('更新执行成功');
    } else {
      result.value = [];
      message.value = `更新失败：${response.message}`;
      messageType.value = 'error';
      ElMessage.error('更新执行失败');
    }
  } catch (error: any) {
    console.error('Error executing update:', error);
    result.value = [];
    message.value = `更新失败：${error.response?.data?.message || '未知错误'}`;
    messageType.value = 'error';
    ElMessage.error('更新执行失败');
  } finally {
    executing.value = false;
  }
};

// 组件挂载时初始化编辑器
onMounted(() => {
  initEditor();
});

// 组件卸载时销毁编辑器
onBeforeUnmount(() => {
  if (editorInstance) {
    editorInstance.dispose();
  }
});
</script>

<style scoped>
.sql-query {
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
  border-radius: 4px;
}

.message.success {
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
  color: #67c23a;
}

.message.error {
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  color: #f56c6c;
}
</style>
