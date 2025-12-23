import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src') // 配置@指向src文件夹
    }
  },
  server: {
    port: 5173, // 前端运行端口
    open: true, // 启动后自动打开浏览器
    cors: true // 允许跨域（备用）
  }
})