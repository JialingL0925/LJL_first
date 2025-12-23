import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 导入组件（确保路径和文件名100%匹配）
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import Supplier from '@/views/Supplier.vue'
import Customer from '@/views/Customer.vue'
import AccountingSubjectList from '@/views/AccountingSubjectList.vue'
import PurchaseOrder from '@/views/PurchaseOrder.vue'
import JournalEntry from '@/views/JournalEntry.vue'
// 路由规则数组
const routes = [
  { path: '/', redirect: '/login' },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '员工登录' }
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { title: '系统首页', requiresAuth: true }
  },
  {
    path: '/supplier',
    name: 'Supplier',
    component: Supplier,
    meta: { title: '供应商管理', requiresAuth: true }
  },
  {
    path: '/customer',
    name: 'Customer',
    component: Customer,
    meta: { title: '客户管理', requiresAuth: true }
  },
  // 新增会计科目管理路由
  {
    path: '/accountingSubject',
    name: 'AccountingSubject',
    component: AccountingSubjectList,
    meta: { title: '会计科目管理', requiresAuth: true }
  },
  // 新增采购订单管理路由
  {
    path: '/purchaseOrder',
    name: 'PurchaseOrder',
    component: PurchaseOrder,
    meta: { title: '采购订单管理', requiresAuth: true }
  },
  {
    path: '/salesOrder',
    name: 'SalesOrder',
    component: () => import('@/views/SalesOrder.vue'),
    meta: {
      title: '销售订单管理',
      icon: 'ticket',
      requiresAuth: true
    }
  },
  // 新增分录管理路由
  {
    path: '/journalEntry',
    name: 'JournalEntry',
    component: JournalEntry,
    meta: { title: '分录管理', requiresAuth: true }
  },
  // 新增过账管理路由
  {
    path: '/posting',
    name: 'Posting',
    component: () => import('@/views/Posting.vue'),
    meta: { title: '凭证过账', requiresAuth: true }
  },
  // 新增付款管理路由
  {
    path: '/apPayment',
    name: 'ApPayment',
    component: () => import('@/views/ApPayment.vue'),
    meta: { title: '付款管理', requiresAuth: true }
  },
  // 新增收款管理路由
  {
    path: '/arPayment',
    name: 'ArPayment',
    component: () => import('@/views/ArPayment.vue'),
    meta: { title: '收款管理', requiresAuth: true }
  },
  // 新增报表管理路由
  {
    path: '/report',
    name: 'Report',
    component: () => import('@/views/Report.vue'),
    meta: { title: '报表管理', requiresAuth: true }
  },
  // 新增项目管理路由
  {
    path: '/project',
    name: 'Project',
    component: () => import('@/views/Project.vue'),
    meta: { title: '项目管理', requiresAuth: true }
  },
  // 新增员工管理路由（仅管理员可见）
  {
    path: '/employee',
    name: 'Employee',
    component: () => import('@/views/Employee.vue'),
    meta: { title: '员工管理', requiresAuth: true }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导入权限工具
import { hasPermission } from '@/utils/permission'

// 导航守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '记账系统'
  const token = localStorage.getItem('token')
  
  // 检查是否需要认证
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
    // 检查权限（登录后）
    if (token && to.meta.requiresAuth) {
      const role = localStorage.getItem('role')
      const roleId = localStorage.getItem('roleId')
      console.log('🔐 路由守卫 - 当前角色：', role, '角色ID：', roleId, '访问路径：', to.path)
      
      // 如果没有角色信息，跳转到登录页
      if (!role && !roleId) {
        console.warn('⚠️ 未找到角色信息，跳转到登录页')
        next('/login')
        return
      }
      
      // 项目管理页面只对管理员（roleId为1）开放
      if (to.path === '/project') {
        if (roleId !== '1' && roleId !== 1 && role !== 'ADMIN') {
          console.warn('❌ 非管理员无权访问项目管理页面')
          ElMessage.warning('您没有权限访问项目管理页面')
          next('/home')
          return
        }
        console.log('✅ 管理员权限，允许访问项目管理')
        next()
        return
      }
      
      // 管理员（roleId为1）拥有所有权限，直接放行
      if (roleId === '1' || roleId === 1 || role === 'ADMIN') {
        console.log('✅ 管理员权限，放行')
        next()
        return
      }
      
      // 其他角色检查权限
      const hasAccess = hasPermission(to.path, role)
      console.log('🔍 权限检查结果：', hasAccess, '角色：', role, '路径：', to.path)
      
      if (!hasAccess) {
        console.warn('❌ 无权限访问：', to.path, '角色：', role)
        ElMessage.warning('您没有权限访问该页面')
        if (to.path !== '/home') {
          next('/home')
        } else {
          next(false)
        }
        return
      }
      
      console.log('✅ 权限验证通过')
    }
  
  next()
})

// 关键：默认导出路由实例（必须添加！）
export default router