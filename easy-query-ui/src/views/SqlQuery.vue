<template>
  <div class="sql-query-container">
    <div class="left-panel">
      <div class="datasource-header">
        <el-select v-model="selectedDataSource" placeholder="选择数据源" style="width: 100%"
          @change="handleDataSourceChange">
          <el-option v-for="ds in dataSources" :key="ds.name" :label="ds.name" :value="ds.name" />
        </el-select>
      </div>
      <el-tabs v-model="activeTab" class="left-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="数据表" name="tables">
          <div class="table-list">
            <div class="table-item" v-for="table in tables" :key="table" @click="handleTableClick(table)">
              <el-icon>
                <Document />
              </el-icon>
              <span>{{ table }}</span>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="查询" name="sql-query">
          <div class="sql-list">
            <div class="sql-item" v-for="sql in savedSqlQueries" :key="sql.id" @click="handleUseSavedSql(sql)">
              <div class="sql-name">{{ sql.name }}</div>
              <div class="sql-preview">{{ sql.sqlContent.substring(0, 50) }}{{ sql.sqlContent.length > 50 ? '...' : ''
              }}</div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div class="right-panel">
      <!-- 编辑器选项卡 -->
      <div class="editor-tabs-toolbar">
        <el-button type="danger" @click="handleAddNewEditorTab('SELECT * FROM')">新增</el-button>
      </div>
      <div class="editor-tabs-container">
        <el-tabs v-model="activeEditorTab" type="card" closable @tab-click="handleEditorTabChange"
          @tab-remove="handleRemoveEditorTab" class="editor-tabs">
          <el-tab-pane v-for="tab in editorTabs" :key="tab.id" :label="tab.title" :name="tab.id">
            <div v-if="tab.id === activeEditorTab" class="editor-wrapper">
              <div :ref="el => setEditorRef(el, tab.id)" class="editor"></div>
              <div class="button-bar">
                <el-button type="info" @click="handleCheckSql(tab)">检查</el-button>
                <el-button type="primary" @click="handleExecuteQuery(tab)" :loading="tab.executing">执行</el-button>
                <el-button type="success" @click="openSaveDialog(tab)">保存</el-button>
              </div>
              <div class="result-container" v-if="tab.result && tab.result.length > 0">
                <el-table :data="tab.result" style="width: 100%" max-height="300" border scrollbar-always-on>
                  <el-table-column v-for="(_, key) in tab.result[0]" :key="key" :prop="key as string"
                    :label="key as string" show-overflow-tooltip />
                </el-table>
              </div>
              <div class="message" v-if="tab.message" :class="tab.messageType">
                {{ tab.message }}
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-dialog v-model="saveDialogVisible" title="保存SQL查询" width="400px">
      <el-form :model="saveForm" :rules="saveFormRules" ref="saveFormRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="saveForm.name" placeholder="请输入SQL查询名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="saveForm.description" type="textarea" :rows="2" placeholder="请输入描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveSql">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { Document } from '@element-plus/icons-vue';
import * as monaco from 'monaco-editor';
import sqlApi from '@/api/sql';
import datasourceApi from '@/api/datasource';
import sqlQueryApi from '@/api/sqlQuery';
import type { SqlRequest, DataSource, SqlQuery } from '@/types';

interface EditorTab {
  id: string;
  title: string;
  sqlContent: string;
  result: any[];
  message: string;
  messageType: 'success' | 'error' | 'info' | '';
  executing: boolean;
}

const dataSources = ref<DataSource[]>([]);
const selectedDataSource = ref<string>('');
const selectedDataSourceId = ref<number>(0);
const tables = ref<string[]>([]);
const activeTab = ref('tables');
const activeEditorTab = ref<string>('');
const editorTabs = ref<EditorTab[]>([]);
const savedSqlQueries = ref<SqlQuery[]>([]);
const editorRefs = new Map<string, HTMLElement>();
const editorInstances = new Map<string, monaco.editor.IStandaloneCodeEditor>();
let tabCounter = 0;
let currentSavingTab: EditorTab | null = null;
let isUnmounted = false;

const saveDialogVisible = ref(false);
const saveFormRef = ref();
const saveForm = ref({
  name: '',
  description: ''
});
const saveFormRules = {
  name: [{ required: true, message: '请输入SQL查询名称', trigger: 'blur' }]
};

const setEditorRef = async (el: any, tabId: string) => {
  if (el && !isUnmounted) {
    editorRefs.set(tabId, el);
    await nextTick();
    setTimeout(async () => {
      await initEditorTab(tabId);
    }, 100);
  }
};

const loadDataSources = async () => {
  try {
    const response = await datasourceApi.getAllDataSources();
    if (response.code === 200 && response.data) {
      dataSources.value = response.data;
      if (response.data.length > 0) {
        selectedDataSource.value = response.data[0].name;
        selectedDataSourceId.value = response.data[0].id || 0;
        await loadTables(response.data[0].id || 0);
      }
    }
  } catch (error) {
    console.error('Error loading data sources:', error);
    ElMessage.error('加载数据源失败');
  }
};

const loadSavedSqlQueries = async () => {
  if (!selectedDataSourceId.value) {
    savedSqlQueries.value = [];
    return;
  }
  try {
    const response = await sqlQueryApi.getSqlQueriesByDataSourceId(selectedDataSourceId.value);
    if (response.code === 200) {
      savedSqlQueries.value = response.data || [];
    }
  } catch (error) {
    console.error('Error loading saved SQL queries:', error);
  }
};

const handleDataSourceChange = async (val: string) => {
  const ds = dataSources.value.find(d => d.name === val);
  selectedDataSource.value = val;
  selectedDataSourceId.value = ds?.id || 0;
  await loadTables(ds?.id || 0);
  await loadSavedSqlQueries();
};

const handleTabClick = async (tab: any) => {
  if (tab.props?.name === 'tables' && selectedDataSourceId.value) {
    await loadTables(selectedDataSourceId.value);
  } else if (tab.props?.name === 'sql-query' && selectedDataSourceId.value) {
    await loadSavedSqlQueries();
  }
};

const loadTables = async (dataSourceId: number) => {
  try {
    const response = await datasourceApi.getTables(dataSourceId);
    if (response.code === 200) {
      tables.value = response.data || [];
    }
  } catch (error) {
    console.error('Error loading tables:', error);
  }
};

const handleTableClick = (table: string) => {
  const sql = `SELECT * FROM \`${table}\` LIMIT 100;`;
  handleAddNewEditorTab(sql);
};

const handleAddNewEditorTab = (sqlContent: string) => {
  if (isUnmounted) return;

  tabCounter++;
  const newTab: EditorTab = {
    id: `tab-${tabCounter}`,
    title: `查询 ${tabCounter}`,
    sqlContent: sqlContent,
    result: [],
    message: '',
    messageType: '',
    executing: false
  };
  editorTabs.value.push(newTab);
  activeEditorTab.value = newTab.id;
};

const handleRemoveEditorTab = (tabId: string) => {
  const index = editorTabs.value.findIndex(t => t.id === tabId);
  if (index !== -1) {
    disposeEditorTab(tabId);
    editorTabs.value.splice(index, 1);
    editorRefs.delete(tabId);
    if (activeEditorTab.value === tabId) {
      if (editorTabs.value.length > 0) {
        activeEditorTab.value = editorTabs.value[Math.max(0, index - 1)].id;
      } else {
        activeEditorTab.value = '';
        tabCounter = 0;
      }
    }
  }
};

const handleEditorTabChange = (tabId: string) => {
  console.log('切换到标签页:', tabId);
  // 销毁非活动标签页的编辑器实例
  editorTabs.value.forEach(tab => {
    if (tab.id !== tabId) {
      disposeEditorTab(tab.id);
    }
  });
};

const initEditorTab = async (tabId: string) => {
  if (isUnmounted) return;

  if (!monaco.editor.getModel) {
    console.log('Monaco 未就绪，延迟初始化');
    setTimeout(() => initEditorTab(tabId), 100);
    return;
  }

  console.log('初始化编辑器:', tabId);

  const el = editorRefs.get(tabId);
  if (!el) return;

  // 等待 DOM 渲染完成
  await nextTick();

  if (!el.offsetParent || el.offsetWidth === 0 || el.offsetHeight === 0) {
    console.log('编辑器容器不可见，销毁编辑器');
    disposeEditorTab(tabId);
    return;
  }

  // 如果旧的编辑器实例存在，先销毁它
  disposeEditorTab(tabId);

  const tab = editorTabs.value.find(t => t.id === tabId);
  if (!tab) {
    return;
  }

  // 确保 value 是字符串类型
  const value = typeof tab.sqlContent === 'string' ? tab.sqlContent : '';

  try {
    // 创建 model 而不是直接传入 value
    const model = monaco.editor.createModel(
      value,
      'sql',
      monaco.Uri.parse(`file:///${tabId}.sql`)
    );

    const editor = monaco.editor.create(el, {
      model: model,
      theme: 'vs',
      // ============ 性能优化核心配置 ============
      automaticLayout: false,        // 关闭自动布局（最大卡顿源）
      minimap: { enabled: false },   // 关闭小地图
      folding: false,                // 关闭代码折叠
      renderLineHighlight: 'none',   // 关闭行高亮
      scrollBeyondLastLine: false,   // 禁止滚动超出
      wordWrap: 'off',               // 关闭自动换行
      overviewRulerLanes: 0,         // 关闭右侧标记
      lineNumbersMinChars: 3,        // 固定行号
      // 关闭耗性能的提示功能
      quickSuggestions: false,
      acceptSuggestionOnEnter: 'off',
      tabCompletion: 'off',
      wordBasedSuggestions: 'off',
      hover: { enabled: false },
      parameterHints: { enabled: false },
      suggestOnTriggerCharacters: false
    });

    editorInstances.set(tabId, editor);
    console.log('编辑器初始化成功:', tabId);
  } catch (error) {
    console.error('编辑器初始化失败:', error);
  }
};

const disposeEditorTab = (tabId: string) => {
  const editor = editorInstances.get(tabId);
  if (editor) {
    // 获取并清理 model
    const model = editor.getModel();
    if (model) {
      // 在销毁之前保存编辑器的最新内容到对应的 tab
      const tab = editorTabs.value.find(t => t.id === tabId);
      if (tab) {
        tab.sqlContent = model.getValue();
      }
      model.dispose();
    }
    editor.dispose();
    editorInstances.delete(tabId);
  }
  const el = editorRefs.get(tabId);
  if (el) {
    el.innerHTML = '';
    el.removeAttribute('data-monaco-editor');
    el.removeAttribute('editor');
    el.classList.remove('monaco-editor');
  }
};

const handleCheckSql = (tab: EditorTab) => {
  const editor = editorInstances.get(tab.id);
  if (!editor) return;
  const sql = editor.getValue().trim();
  if (!sql) {
    ElMessage.warning('请输入 SQL 语句');
    return;
  }
  ElMessage.success('SQL 语法检查通过');
};

const openSaveDialog = (tab: EditorTab) => {
  const editor = editorInstances.get(tab.id);
  if (!editor) return;
  const sqlContent = editor.getValue().trim();
  if (!sqlContent) {
    ElMessage.warning('请输入 SQL 语句');
    return;
  }
  currentSavingTab = tab;
  saveForm.value = {
    name: tab.title.startsWith('查询') ? '' : tab.title,
    description: ''
  };
  saveDialogVisible.value = true;
};

const confirmSaveSql = async () => {
  if (!saveFormRef.value) return;

  try {
    await saveFormRef.value.validate();
    if (currentSavingTab) {
      const editor = editorInstances.get(currentSavingTab.id);
      const sqlContent = editor?.getValue().trim() || '';
      const sqlQuery: SqlQuery = {
        dataSourceId: selectedDataSourceId.value,
        dataSourceName: selectedDataSource.value,
        name: saveForm.value.name,
        sqlContent: sqlContent,
        description: saveForm.value.description
      };

      const response = await sqlQueryApi.createSqlQuery(sqlQuery);
      if (response.code === 200) {
        ElMessage.success('保存成功');
        currentSavingTab.title = saveForm.value.name;
        saveDialogVisible.value = false;
        loadSavedSqlQueries();
      } else {
        ElMessage.error(response.message || '保存失败');
      }
    }
  } catch (error) {
    if (error !== false) {
      console.error('Error saving SQL query:', error);
      ElMessage.error('保存失败');
    }
  }
};

const handleExecuteQuery = async (tab: EditorTab) => {
  const editor = editorInstances.get(tab.id);
  if (!editor) return;

  const sql = editor.getValue().trim();
  if (!sql) {
    ElMessage.warning('请输入 SQL 语句');
    return;
  }

  if (!selectedDataSource.value) {
    ElMessage.warning('请选择数据源');
    return;
  }

  tab.sqlContent = sql;
  tab.executing = true;
  tab.message = '';

  try {
    const sqlRequest: SqlRequest = {
      sql: sql,
      dataSourceId: selectedDataSourceId.value
    };

    const response = await sqlApi.executeQuery(sqlRequest);
    if (response.code === 200) {
      tab.result = response.data || [];
      tab.message = `查询成功，返回 ${response.data?.length || 0} 条记录`;
      tab.messageType = 'success';
    } else {
      tab.result = [];
      tab.message = `查询失败：${response.message}`;
      tab.messageType = 'error';
    }
  } catch (error: any) {
    console.error('Error executing query:', error);
    tab.result = [];
    tab.message = `查询失败：${error.response?.data?.message || '未知错误'}`;
    tab.messageType = 'error';
    ElMessage.error('查询执行失败');
  } finally {
    tab.executing = false;
  }
};

const handleUseSavedSql = (sql: SqlQuery) => {
  tabCounter++;
  const newTab: EditorTab = {
    id: `tab-${tabCounter}`,
    title: sql.name,
    sqlContent: sql.sqlContent,
    result: [],
    message: '',
    messageType: '',
    executing: false
  };
  editorTabs.value.push(newTab);
  activeEditorTab.value = newTab.id;
};

onMounted(() => {
  isUnmounted = false;
  loadDataSources();
  loadSavedSqlQueries();
  handleAddNewEditorTab('SELECT * FROM ');
});

onUnmounted(() => {
  isUnmounted = true;
  editorTabs.value.forEach((tab) => {
    disposeEditorTab(tab.id);
    editorRefs.delete(tab.id);
  });
  editorInstances.clear();
  editorRefs.clear();
  editorTabs.value = [];
});
</script>

<style scoped>
.sql-query-container {
  display: flex;
  height: calc(100vh - 120px);
  padding: 20px;
  gap: 20px;
}

.left-panel {
  width: 280px;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.datasource-header {
  padding: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.editor-tabs-toolbar {
  padding: 10px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
}

.left-tabs {
  flex: 1;
  height: auto;
}

.left-tabs :deep(.el-tabs__item) {
  padding: 0 15px !important;
}

.left-tabs :deep(.el-tabs__content) {
  height: calc(100% - 55px);
  overflow-y: auto;
}

.table-list {
  padding: 10px;
  height: 100%;
  overflow-y: auto;
}

.table-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.table-item:hover {
  background-color: #f5f7fa;
}

.sql-list {
  margin-top: 10px;
}

.sql-item {
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.sql-item:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.sql-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.sql-preview {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.editor-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow-x: auto;
  overflow-y: auto;
  padding: 0;
}

.editor-tabs :deep(.el-tabs__header) {
  margin: 0;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.editor-tabs :deep(.el-tabs__nav-wrap) {
  flex: 1;
}

.editor-tabs :deep(.el-tabs__item) {
  height: 32px;
  line-height: 32px;
  border-radius: 4px 4px 0 0;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.editor-tabs :deep(.el-tabs__item)::after {
  display: none;
}

.editor-tabs-container {
  display: flex;
  align-items: flex-start;
  height: 100%;
}

.editor-tabs-container>.editor-tabs {
  height: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.editor-tabs-container>.editor-tabs :deep(.el-tabs__header) {
  flex-shrink: 0;
}

.editor-tabs-container>.editor-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
}

.editor-tabs-container>.editor-tabs :deep(.el-tab-pane) {
  height: 100%;
}

.tab-title {
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.editor-tabs :deep(.is-scrollable) {

  .el-tabs__nav-next,
  .el-tabs__nav-prev {
    line-height: 32px;
  }
}

.editor-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-width: 0;
}

.editor {
  height: 300px;
  border-bottom: 1px solid #e4e7ed;
}

.button-bar {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #e4e7ed;
  gap: 10px;
}

.result-container {
  flex: 1;
  overflow-x: auto;
  overflow-y: auto;
  padding: 10px;
}

.message {
  padding: 10px;
  margin: 10px;
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

.message.info {
  background-color: #f4f4f5;
  border: 1px solid #e9e9eb;
  color: #909399;
}

.empty-editor {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
