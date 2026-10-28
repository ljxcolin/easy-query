import { defineConfig, loadEnv, type UserConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import viteCompression from 'vite-plugin-compression'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))

// https://vitejs.dev/config/
export default defineConfig(({ mode }): UserConfig => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [
      vue(),
      tailwindcss(),
      // 构建时预生成 .gz 产物：由 Web 服务器 / Spring 直接返回，避免运行时压缩开销
      viteCompression({
        verbose: false,
        disable: mode !== 'production', // 仅生产构建压缩
        threshold: 10240, // 小于 10KB 的资源不压缩
        algorithm: 'gzip',
        ext: '.gz',
        deleteOriginFile: false // 保留原文件，便于不支持 gzip 的客户端回退
      })
    ],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    optimizeDeps: {
      include: ['monaco-editor']
    },
    server: {
      port: 3000,
      host: true,
      open: mode !== 'production',
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'static',
      sourcemap: false,
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: true,
          drop_debugger: true
        }
      }
    }
  }
})
