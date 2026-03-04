# easy-query-ui TypeScript + Vue 3 Setup 迁移指南

## 迁移概述

已成功将 easy-query-ui 项目从 JavaScript + Options API 迁移到 **TypeScript + Vue 3 Composition API (setup 语法糖)**。

## 主要变更

### 1. TypeScript 配置

#### 新增文件
- `tsconfig.json` - TypeScript 主配置文件
- `tsconfig.node.json` - Vite 配置的 TypeScript 支持
- `src/vite-env.d.ts` - Vite 环境类型声明
- `src/types/index.ts` - 通用类型定义
- `src/main.ts` - 入口文件（从 main.js 迁移）
- `vite.config.ts` - Vite 配置（从 vite.config.js 迁移）

#### 类型定义
```typescript
// src/types/index.ts
export interface Response<T = any> {
  code: number;
  message: string;
  data: T;
}

export interface DataSource {
  id?: number;
  name: string;
  url: string;
  // ... 其他字段
}

export interface ShardingStrategy {
  id?: number;
  name: string;
  strategyType: string;
  // ... 其他字段
}
```

### 2. API 模块迁移

**从 JavaScript 到 TypeScript：**

```typescript
// src/api/datasource.ts
import request from './request';
import type { DataSource, Response } from '../types';

export default {
  getAllDataSources(): Promise<Response<DataSource[]>> {
    return request.get('');
  },
  // ... 其他方法
};
```

**改进：**
- ✅ 完整的类型推断
- ✅ API 响应类型安全
- ✅ 参数类型检查

### 3. 组件迁移（Options API → Composition API）

#### DataSource.vue 迁移示例

**Before (Options API):**
```javascript
export default {
  name: 'DataSource',
  data() {
    return {
      dataSources: [],
      loading: false
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
    }
  }
};
```

**After (Composition API + TypeScript):**
```typescript
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import dataSourceApi from '@/api/datasource';
import type { DataSource } from '@/types';

const dataSources = ref<DataSource[]>([]);
const loading = ref<boolean>(false);

const loadDataSources = async () => {
  loading.value = true;
  try {
    const response = await dataSourceApi.getAllDataSources();
    if (response.code === 200 && response.data) {
      dataSources.value = response.data;
    }
  } catch (error) {
    ElMessage.error('加载数据源失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadDataSources();
});
</script>
```

### 4. 改进的功能

#### DataSource 组件
- ✅ 完整的 TypeScript 类型支持
- ✅ 异步/等待语法
- ✅ 完善的错误处理
- ✅ 加载状态指示器
- ✅ 保存状态指示器
- ✅ 编辑时禁用名称字段

#### ShardingRule 组件
- ✅ JSON 配置验证
- ✅ 策略类型标签显示
- ✅ 测试功能增强
- ✅ 完整的类型安全

#### SqlQuery 组件
- ✅ Monaco 编辑器类型支持
- ✅ 消息类型区分（成功/错误）
- ✅ 执行状态指示器
- ✅ 更好的错误提示

## 开发命令

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 类型检查
npm run type-check

# 构建（开发环境）
npm run build:dev

# 构建（生产环境）
npm run build:prod

# 预览生产构建
npm run preview
```

## 文件结构

```
easy-query-ui/
├── src/
│   ├── api/                    # API 模块
│   │   ├── request.ts          # Axios 实例配置
│   │   ├── datasource.ts       # 数据源 API
│   │   ├── shardingRule.ts     # 分片规则 API
│   │   └── sql.ts              # SQL 查询 API
│   ├── components/             # Vue 组件
│   │   ├── DataSource.vue      # 数据源管理（setup + TS）
│   │   ├── ShardingRule.vue    # 分片规则管理（setup + TS）
│   │   └── SqlQuery.vue        # SQL 查询（setup + TS）
│   ├── types/                  # 类型定义
│   │   └── index.ts            # 通用类型
│   ├── App.vue                 # 根组件
│   ├── main.ts                 # 入口文件
│   ├── vite-env.d.ts           # Vite 类型声明
│   └── router/                 # 路由配置
├── tsconfig.json               # TypeScript 配置
├── tsconfig.node.json          # Node 环境 TS 配置
├── vite.config.ts              # Vite 配置
└── package.json
```

## TypeScript 配置亮点

### tsconfig.json
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "strict": true,              // 严格类型检查
    "noUnusedLocals": true,      // 检查未使用的局部变量
    "noUnusedParameters": true,  // 检查未使用的参数
    "jsx": "preserve",           // 保留 JSX 供 Vue 处理
    "types": ["vite/client"]     // Vite 类型
  }
}
```

## 类型安全特性

1. **API 响应类型安全**
   ```typescript
   const response = await dataSourceApi.getAllDataSources();
   // response 类型为 Promise<Response<DataSource[]>>
   ```

2. **组件 Props 类型检查**
   ```typescript
   interface Props {
     title: string;
     count?: number;
   }
   
   const props = defineProps<Props>();
   ```

3. **事件类型安全**
   ```typescript
   const emit = defineEmits<{
     (e: 'save', data: DataSource): void;
     (e: 'cancel'): void;
   }>();
   ```

## 迁移优势

### 开发体验
- ✅ 智能代码补全
- ✅ 实时类型错误提示
- ✅ 更好的 IDE 支持
- ✅ 重构更安全

### 代码质量
- ✅ 类型安全，减少运行时错误
- ✅ 更好的代码文档（类型即文档）
- ✅ 更容易维护
- ✅ 更清晰的 API 设计

### 性能
- ✅ Composition API 更好的 Tree-shaking
- ✅ 更小的打包体积
- ✅ 更快的运行时性能

## 注意事项

1. **TypeScript 版本**: 5.9.3+
2. **Vue 版本**: 3.4.21+
3. **Vite 版本**: 5.2.0+
4. **严格模式**: 启用所有严格类型检查选项

## 后续优化建议

1. 添加 ESLint + Prettier 配置
2. 配置 Vitest 进行单元测试
3. 添加 Pinia 进行状态管理
4. 使用 VueUse 工具库
5. 添加 Husky + lint-staged 进行代码质量检查

## 参考资源

- [Vue 3 TypeScript 支持](https://vuejs.org/guide/typescript/overview.html)
- [Composition API 文档](https://vuejs.org/guide/extras/composition-api-faq.html)
- [TypeScript 官方文档](https://www.typescriptlang.org/)
- [Vite + Vue + TypeScript 模板](https://vitejs.dev/guide/)
