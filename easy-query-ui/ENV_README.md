# Easy Query UI 环境配置说明

## 目录结构
```
easy-query-ui/
├── .env                      # 默认环境配置
├── .env.development          # 开发环境配置
├── .env.production           # 生产环境配置
├── .env.example              # 环境配置示例
├── vite.config.js            # Vite 配置文件
├── package.json              # 项目依赖和脚本
└── src/
    └── api/
        └── request.js        # HTTP 请求封装
```

## 环境配置

### 开发环境 (.env.development)
```bash
VITE_APP_TITLE=Easy Query - Development
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_ENV=development
```

**特点：**
- API 基础路径包含 `/api` 前缀
- Vite 代理会自动去掉 `/api` 前缀转发到后端
- 启用源码映射（source map）
- 不进行代码压缩

### 生产环境 (.env.production)
```bash
VITE_APP_TITLE=Easy Query
VITE_API_BASE_URL=/api
VITE_APP_ENV=production
```

**特点：**
- 使用相对路径 `/api`
- 需要配置 Nginx 反向代理并去掉 `/api` 前缀
- 代码压缩和混淆
- 去除 console 日志

## NPM 脚本

```bash
# 开发模式（默认使用 development 环境）
npm run dev

# 生产环境构建
npm run build
# 或
npm run build:prod

# 开发环境构建
npm run build:dev

# 预览生产构建
npm run preview
```

## API 代理配置

### 开发环境
所有 API 请求都带有 `/api` 前缀，Vite 代理会自动去掉前缀转发到后端：

```javascript
// 前端请求
GET /api/data-sources

// Vite 代理去掉 /api 前缀，转发到后端
GET http://localhost:8080/data-sources
```

**Vite 代理配置：**
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, '')
  }
}
```

### 生产环境
生产环境使用 `/api` 相对路径，需要配置 Nginx 反向代理并去掉前缀：

**Nginx 配置示例：**
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # 反向代理 API 请求，去掉 /api 前缀
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 使用环境变量

在 Vue 组件中使用：
```vue
<script setup>
const appTitle = import.meta.env.VITE_APP_TITLE
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL
const appEnv = import.meta.env.VITE_APP_ENV
</script>
```

## 构建输出

生产构建后，文件输出到 `dist` 目录：
```
dist/
├── index.html
└── static/
    ├── assets/
    │   ├── index-[hash].css
    │   └── index-[hash].js
    └── ...
```

## 注意事项

1. **环境变量前缀**：只有以 `VITE_` 开头的变量才会被暴露给客户端代码
2. **热更新**：修改 `.env` 文件后，开发服务器会自动重启
3. **构建优化**：生产构建会移除 console.log 和 debugger 语句
4. **API 路径**：确保后端服务的 API 路径与前端请求路径匹配（去掉 `/api` 前缀）
