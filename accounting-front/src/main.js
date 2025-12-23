import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router/index.js' 

// 1. 仅创建一次Vue实例
const app = createApp(App)

// 2. 仅注册一次路由和ElementPlus插件（顺序不影响）
app.use(router)
app.use(ElementPlus)

// 3. 打印路由表（可选，验证用）
console.log('最终路由表：', router.getRoutes()) 

// 4. 仅挂载一次到#app（必须放在最后）
app.mount('#app')