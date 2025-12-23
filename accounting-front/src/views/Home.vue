<template>
  <div class="home-page">
    <el-container style="height: 100vh;">
      <!-- 侧边导航栏 -->
      <el-aside width="220px" class="sidebar">
        <div class="menu-header">
          <div class="logo">
            <el-icon class="logo-icon"><Wallet /></el-icon>
            <span class="logo-text">记账系统</span>
          </div>
        </div>
        <el-menu
          default-active="/home"
          class="el-menu-vertical-demo"
          router
          :collapse="false"
        >
          <el-menu-item index="/home">
            <el-icon><House /></el-icon>
            <template #title>系统首页</template>
          </el-menu-item>
          <el-menu-item index="/supplier" v-if="hasMenuPermission('/supplier')">
            <el-icon><UserFilled /></el-icon>
            <template #title>供应商管理</template>
          </el-menu-item>
          <el-menu-item index="/customer" v-if="hasMenuPermission('/customer')">
            <el-icon><User /></el-icon>
            <template #title>客户管理</template>
          </el-menu-item>
          <el-menu-item index="/accountingSubject" v-if="hasMenuPermission('/accountingSubject')">
            <el-icon><Wallet /></el-icon>
            <template #title>会计科目管理</template>
          </el-menu-item>
          <el-menu-item index="/purchaseOrder" v-if="hasMenuPermission('/purchaseOrder')">
            <el-icon><ShoppingCart /></el-icon>
            <template #title>采购订单</template>
          </el-menu-item>
          <el-menu-item index="/salesOrder" v-if="hasMenuPermission('/salesOrder')">
            <el-icon><Ticket /></el-icon>
            <template #title>销售订单</template>
          </el-menu-item>
          <el-menu-item index="/journalEntry" v-if="hasMenuPermission('/journalEntry')">
            <el-icon><Document /></el-icon>
            <template #title>分录管理</template>
          </el-menu-item>
          <el-menu-item index="/posting" v-if="hasMenuPermission('/posting')">
            <el-icon><Check /></el-icon>
            <template #title>凭证过账</template>
          </el-menu-item>
          <el-menu-item index="/apPayment" v-if="hasMenuPermission('/apPayment')">
            <el-icon><Money /></el-icon>
            <template #title>付款管理</template>
          </el-menu-item>
          <el-menu-item index="/arPayment" v-if="hasMenuPermission('/arPayment')">
            <el-icon><CreditCard /></el-icon>
            <template #title>收款管理</template>
          </el-menu-item>
          <el-menu-item index="/report" v-if="hasMenuPermission('/report')">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>报表管理</template>
          </el-menu-item>
          <el-menu-item index="/project" v-if="isAdminUser">
            <el-icon><Folder /></el-icon>
            <template #title>项目管理</template>
          </el-menu-item>
          <el-menu-item index="/employee" v-if="isAdminUser">
            <el-icon><UserFilled /></el-icon>
            <template #title>员工管理</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-left">
            <h3 class="page-title">系统首页</h3>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-icon class="user-icon"><User /></el-icon>
                <span class="user-name">{{ phone }}</span>
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
              </div>
            <template #dropdown>
              <el-dropdown-menu>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <!-- 欢迎横幅 -->
          <div class="welcome-banner">
            <div class="banner-content">
              <div class="banner-text">
                <h1 class="banner-title">欢迎回来，{{ phone }}！</h1>
                <p class="banner-subtitle">今天是 {{ currentDate }}，祝您工作愉快</p>
              </div>
              <div class="banner-icon">
                <el-icon class="icon-large"><DataAnalysis /></el-icon>
              </div>
            </div>
          </div>

          <!-- 统计卡片 -->
          <el-row :gutter="20" class="stats-row">
            <el-col :xs="24" :sm="12" :md="6" v-for="(stat, index) in stats" :key="index">
              <el-card class="stat-card" :class="`stat-card-${index}`" shadow="hover">
                <div class="stat-content">
                  <div class="stat-icon-wrapper">
                    <el-icon class="stat-icon" :class="`icon-${index}`">
                      <component :is="stat.icon" />
                    </el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stat.value }}</div>
                    <div class="stat-label">{{ stat.label }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 待办提醒和登录信息 -->
          <el-row :gutter="20" class="info-cards-row">
            <el-col :xs="24" :md="12">
              <el-card class="info-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><Document /></el-icon>
                    <span>待办提醒</span>
                  </div>
                </template>
                <div class="todo-content">
                  <div 
                    v-if="unpaidPayableCount > 0" 
                    class="todo-item"
                    @click="handleQuickAction('/apPayment')"
                  >
                    <el-icon class="todo-icon warning"><Money /></el-icon>
                    <div class="todo-text">
                      <div class="todo-title">待付款订单</div>
                      <div class="todo-desc">有 {{ unpaidPayableCount }} 笔订单待付款</div>
                    </div>
                    <el-icon class="todo-arrow"><ArrowRight /></el-icon>
                  </div>
                  <div 
                    v-if="unpaidReceivableCount > 0" 
                    class="todo-item"
                    @click="handleQuickAction('/arPayment')"
                  >
                    <el-icon class="todo-icon success"><CreditCard /></el-icon>
                    <div class="todo-text">
                      <div class="todo-title">待收款订单</div>
                      <div class="todo-desc">有 {{ unpaidReceivableCount }} 笔订单待收款</div>
                    </div>
                    <el-icon class="todo-arrow"><ArrowRight /></el-icon>
                  </div>
                  <div v-if="!hasTodoItems" class="todo-empty">
                    <el-icon><CircleCheck /></el-icon>
                    <span>暂无待办事项</span>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-card class="info-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><User /></el-icon>
                    <span>登录信息</span>
                  </div>
                </template>
                <div class="login-content">
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="登录账号">
                      <el-tag type="info">{{ userInfo.phone }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="登录身份">
                      <el-tag type="primary">{{ currentRoleName }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="登录状态">
                      <el-tag type="success">已登录</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="登录时间">
                      {{ loginTime }}
                    </el-descriptions-item>
                  </el-descriptions>
            </div>
          </el-card>
            </el-col>
          </el-row>

          <!-- 数据可视化 -->
          <el-row :gutter="20" class="chart-row">
            <el-col :xs="24" :md="12">
              <el-card class="chart-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><DataAnalysis /></el-icon>
                    <span>订单统计</span>
                  </div>
                </template>
                <div class="chart-content">
                  <div class="chart-item">
                    <div class="chart-label">采购订单</div>
                    <div class="chart-bar-wrapper">
                      <div class="chart-bar" :style="{ width: purchaseOrderPercent + '%' }">
                        <span class="chart-value">{{ purchaseOrderCount }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="chart-item">
                    <div class="chart-label">销售订单</div>
                    <div class="chart-bar-wrapper">
                      <div class="chart-bar sales" :style="{ width: salesOrderPercent + '%' }">
                        <span class="chart-value">{{ salesOrderCount }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-card class="chart-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><Money /></el-icon>
                    <span>收支概览</span>
                  </div>
                </template>
                <div class="chart-content">
                  <div class="revenue-item">
                    <div class="revenue-label">
                      <el-icon class="revenue-icon income"><ArrowRight /></el-icon>
                      <span>本月收入</span>
                    </div>
                    <div class="revenue-value income">¥{{ formatAmount(monthlyIncome) }}</div>
                  </div>
                  <div class="revenue-item">
                    <div class="revenue-label">
                      <el-icon class="revenue-icon expense"><ArrowRight /></el-icon>
                      <span>本月支出</span>
                    </div>
                    <div class="revenue-value expense">¥{{ formatAmount(monthlyExpense) }}</div>
                  </div>
                  <div class="revenue-summary">
                    <div class="summary-item">
                      <span class="summary-label">净额</span>
                      <span class="summary-value" :class="netAmount >= 0 ? 'positive' : 'negative'">
                        ¥{{ formatAmount(Math.abs(netAmount)) }}
                      </span>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { isAdmin, hasPermission } from '@/utils/permission'
import { 
  House,
  UserFilled,
  ShoppingCart,
  User,
  Wallet,
  Document,
  Ticket,
  DocumentCopy,
  DataAnalysis,
  Operation,
  ArrowDown,
  SwitchButton,
  Money,
  CreditCard,
  Folder,
  ArrowRight,
  CircleCheck,
  Check
} from '@element-plus/icons-vue'
import { getSupplierList } from '@/api/supplier'
import { getCustomerList } from '@/api/customer'
import { getEmployeeList } from '@/api/employee'
import { getProjectList } from '@/api/project'
import { countUnpaidPayables, getApPaymentList } from '@/api/apPaymentApi'
import { countUnpaidReceivables, getArPaymentList } from '@/api/arPaymentApi'
import { getPurchaseOrderList } from '@/api/purchaseOrderApi'
import { getSalesOrderList } from '@/api/salesOrderApi'

const router = useRouter()
const phone = ref('')
const userInfo = ref({
  phone: '',
  token: ''
})
const loginTime = ref('')

// 检查是否为管理员（用于显示员工管理菜单）
const isAdminUser = computed(() => isAdmin())

// 检查菜单权限
const hasMenuPermission = (path) => {
  const role = localStorage.getItem('role')
  return hasPermission(path, role)
}

// 获取当前角色中文名称
const currentRoleName = computed(() => {
  const role = localStorage.getItem('role') || 'ADMIN'
  const roleMap = {
    'ADMIN': '管理员',
    'ACCOUNTANT': '会计',
    'PURCHASER': '采购',
    'CASHIER': '出纳',
    'SALES': '销售'
  }
  return roleMap[role] || '未知'
})

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekday = weekdays[now.getDay()]
  return `${year}年${month}月${day}日 ${weekday}`
})

// 统计卡片数据
const stats = ref([
  { key: 'supplierCount', label: '供应商数量', value: '--', icon: 'UserFilled' },
  { key: 'customerCount', label: '客户数量', value: '--', icon: 'User' },
  { key: 'employeeCount', label: '员工数量', value: '--', icon: 'UserFilled' },
  { key: 'projectCount', label: '项目数量', value: '--', icon: 'Folder' }
])

// 待办事项数量（用于待办提醒卡片）
const unpaidPayableCount = ref(0)
const unpaidReceivableCount = ref(0)

// 可视化数据
const purchaseOrderCount = ref(0)
const salesOrderCount = ref(0)
const monthlyIncome = ref(0)  // 本月收入
const monthlyExpense = ref(0)  // 本月支出

// 计算百分比（用于柱状图）
const purchaseOrderPercent = computed(() => {
  const total = purchaseOrderCount.value + salesOrderCount.value
  return total > 0 ? Math.round((purchaseOrderCount.value / total) * 100) : 0
})

const salesOrderPercent = computed(() => {
  const total = purchaseOrderCount.value + salesOrderCount.value
  return total > 0 ? Math.round((salesOrderCount.value / total) * 100) : 0
})

// 计算净额
const netAmount = computed(() => {
  return monthlyIncome.value - monthlyExpense.value
})

// 格式化金额
const formatAmount = (amount) => {
  if (!amount) return '0.00'
  return Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}


onMounted(() => {
  const token = localStorage.getItem('token')
  const loginPhone = localStorage.getItem('phone') || '13800138000'
  if (token) {
    userInfo.value.token = token
    userInfo.value.phone = loginPhone
    phone.value = loginPhone
    
    // 设置登录时间
    const now = new Date()
    loginTime.value = now.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })

    // 加载首页统计数据
    loadDashboardStats()
    // 加载待办事项数量
    loadTodoCounts()
    // 加载可视化数据
    loadChartData()
  } else {
    router.push('/login')
  }
})

// 加载首页统计数据
const loadDashboardStats = async () => {
  try {
    // 供应商数量
    const supplierRes = await getSupplierList({ pageNum: 1, pageSize: 1 })
    if (supplierRes.code === 200 && supplierRes.data && supplierRes.data.total !== undefined) {
      const total = supplierRes.data.total
      const stat = stats.value.find(s => s.key === 'supplierCount')
      if (stat) stat.value = total
    }

    // 客户数量
    const customerRes = await getCustomerList({ pageNum: 1, pageSize: 1 })
    if (customerRes.code === 200 && customerRes.data && customerRes.data.total !== undefined) {
      const total = customerRes.data.total
      const stat = stats.value.find(s => s.key === 'customerCount')
      if (stat) stat.value = total
    }

    // 员工数量
    try {
      const employeeRes = await getEmployeeList({ pageNum: 1, pageSize: 1 })
      if (employeeRes.code === 200 && employeeRes.data && employeeRes.data.total !== undefined) {
        const total = employeeRes.data.total
        const stat = stats.value.find(s => s.key === 'employeeCount')
        if (stat) stat.value = total
      }
    } catch (error) {
      console.error('查询员工数量失败：', error)
    }

    // 项目数量
    try {
      const projectRes = await getProjectList({ pageNum: 1, pageSize: 1 })
      if (projectRes.code === 200 && projectRes.data && projectRes.data.total !== undefined) {
        const total = projectRes.data.total
        const stat = stats.value.find(s => s.key === 'projectCount')
        if (stat) stat.value = total
      }
    } catch (error) {
      console.error('查询项目数量失败：', error)
    }
  } catch (error) {
    console.error('加载首页统计数据失败：', error)
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('phone')
  ElMessage.success('已成功退出登录')
  router.push('/login')
}

const handleQuickAction = (path) => {
  router.push(path)
}

// 加载可视化数据
const loadChartData = async () => {
  try {
    // 获取采购订单数量
    const purchaseRes = await getPurchaseOrderList({ pageNum: 1, pageSize: 1 })
    if (purchaseRes.code === 200 && purchaseRes.data && purchaseRes.data.total !== undefined) {
      purchaseOrderCount.value = purchaseRes.data.total
    }
  } catch (error) {
    console.error('查询采购订单数量失败：', error)
  }

  try {
    // 获取销售订单数量
    const salesRes = await getSalesOrderList({ pageNum: 1, pageSize: 1 })
    if (salesRes.code === 200 && salesRes.data && salesRes.data.total !== undefined) {
      salesOrderCount.value = salesRes.data.total
    }
  } catch (error) {
    console.error('查询销售订单数量失败：', error)
  }

  try {
    // 获取本月支出（已确认的付款金额）
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const startDate = `${year}-${month}-01`
    const endDate = `${year}-${month}-${new Date(year, now.getMonth() + 1, 0).getDate()}`
    
    const paymentRes = await getApPaymentList({
      pageNum: 1,
      pageSize: 1000, // 获取足够多的数据
      status: '已确认',
      paymentDateStart: startDate,
      paymentDateEnd: endDate
    })
    
    if ((paymentRes.code === 200 || paymentRes.success) && paymentRes.data && paymentRes.data.records) {
      const total = paymentRes.data.records.reduce((sum, item) => {
        return sum + (Number(item.payment_amount) || 0)
      }, 0)
      monthlyExpense.value = total
    }
  } catch (error) {
    console.error('查询本月支出失败：', error)
  }

  try {
    // 获取本月收入（已确认的收款金额）
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const startDate = `${year}-${month}-01`
    const endDate = `${year}-${month}-${new Date(year, now.getMonth() + 1, 0).getDate()}`
    
    const paymentRes = await getArPaymentList({
      pageNum: 1,
      pageSize: 1000, // 获取足够多的数据
      status: '已确认',
      paymentDateStart: startDate,
      paymentDateEnd: endDate
    })
    
    if ((paymentRes.code === 200 || paymentRes.success) && paymentRes.data && paymentRes.data.records) {
      const total = paymentRes.data.records.reduce((sum, item) => {
        return sum + (Number(item.payment_amount) || 0)
      }, 0)
      monthlyIncome.value = total
    }
  } catch (error) {
    console.error('查询本月收入失败：', error)
  }
}

// 加载待办事项数量
const loadTodoCounts = async () => {
  try {
    // 待付款订单数量
    const unpaidPayableRes = await countUnpaidPayables()
    if (unpaidPayableRes.success && unpaidPayableRes.data !== undefined) {
      unpaidPayableCount.value = unpaidPayableRes.data
    } else if (unpaidPayableRes.code === 200 && unpaidPayableRes.data !== undefined) {
      unpaidPayableCount.value = unpaidPayableRes.data
    }
  } catch (error) {
    console.error('查询待付款订单数量失败：', error)
  }

  try {
    // 待收款订单数量
    const unpaidReceivableRes = await countUnpaidReceivables()
    if (unpaidReceivableRes.success && unpaidReceivableRes.data !== undefined) {
      unpaidReceivableCount.value = unpaidReceivableRes.data
    } else if (unpaidReceivableRes.code === 200 && unpaidReceivableRes.data !== undefined) {
      unpaidReceivableCount.value = unpaidReceivableRes.data
    }
  } catch (error) {
    console.error('查询待收款订单数量失败：', error)
  }
}

// 检查是否有待办事项
const hasTodoItems = computed(() => {
  return unpaidPayableCount.value > 0 || unpaidReceivableCount.value > 0
})
</script>

<style scoped>
.home-page {
  margin: 0;
  padding: 0;
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg, #1e3a8a 0%, #1e40af 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.menu-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
}

.logo-icon {
  font-size: 24px;
  color: #60a5fa;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.el-menu-vertical-demo {
  background: transparent;
  border: none;
  padding: 10px 0;
}

.el-menu-vertical-demo :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.8);
  margin: 5px 10px;
  border-radius: 8px;
  transition: all 0.3s;
}

.el-menu-vertical-demo :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.el-menu-vertical-demo :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
  color: #fff;
  font-weight: 500;
}

.el-menu-vertical-demo :deep(.el-menu-item .el-icon) {
  margin-right: 8px;
}

/* 顶部导航栏 */
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  border-bottom: 1px solid #e5e7eb;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f3f4f6;
}

.user-icon {
  font-size: 18px;
  color: #6b7280;
}

.user-name {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.arrow-icon {
  font-size: 12px;
  color: #9ca3af;
}

/* 主内容区 */
.main-content {
  padding: 24px;
  background: transparent;
  overflow-y: auto;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
  color: #fff;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.banner-text {
  flex: 1;
}

.banner-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #fff;
}

.banner-subtitle {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
}

.banner-icon {
  font-size: 64px;
  opacity: 0.3;
}

.icon-large {
  font-size: 64px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon {
  font-size: 28px;
}

.stat-card-0 .stat-icon-wrapper {
  background: linear-gradient(135deg, #9333ea 0%, #7c3aed 100%);
}

.stat-card-0 .stat-icon {
  color: #fff;
}

.stat-card-1 .stat-icon-wrapper {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.stat-card-1 .stat-icon {
  color: #fff;
}

.stat-card-2 .stat-icon-wrapper {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
}

.stat-card-2 .stat-icon {
  color: #fff;
}

.stat-card-3 .stat-icon-wrapper {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.stat-card-3 .stat-icon {
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

/* 信息卡片 */
.info-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  margin-bottom: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.info-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 待办提醒 */
.todo-content {
  padding: 8px 0;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

/* 登录信息内容 */
.login-content {
  padding: 8px 0;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 信息卡片行 */
.info-cards-row {
  display: flex;
  align-items: stretch;
}

.info-cards-row .el-col {
  display: flex;
}

.info-cards-row .el-col .info-card {
  width: 100%;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background: #f9fafb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e5e7eb;
}

.todo-item:hover {
  background: #f3f4f6;
  border-color: #3b82f6;
  transform: translateX(4px);
}

.todo-item:last-child {
  margin-bottom: 0;
}

.todo-icon {
  font-size: 24px;
  margin-right: 12px;
}

.todo-icon.warning {
  color: #f59e0b;
}

.todo-icon.success {
  color: #10b981;
}

.todo-text {
  flex: 1;
}

.todo-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.todo-desc {
  font-size: 13px;
  color: #6b7280;
}

.todo-arrow {
  font-size: 16px;
  color: #9ca3af;
  margin-left: 12px;
}

.todo-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9ca3af;
  font-size: 14px;
}

.todo-empty .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #d1d5db;
}

/* 图表卡片 */
.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 12px;
  border: 1px solid #e0e7ff;
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.08);
}

.chart-content {
  padding: 10px 0;
}

/* 订单统计图表 */
.chart-item {
  margin-bottom: 24px;
}

.chart-item:last-child {
  margin-bottom: 0;
}

.chart-label {
  font-size: 14px;
  color: #475569;
  margin-bottom: 8px;
  font-weight: 500;
}

.chart-bar-wrapper {
  width: 100%;
  height: 36px;
  background: #f1f5f9;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.chart-bar {
  height: 100%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 12px;
  transition: width 0.6s ease;
  position: relative;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.chart-bar.sales {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.chart-value {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

/* 收支概览 */
.revenue-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e0e7ff;
}

.revenue-item:last-child {
  border-bottom: none;
}

.revenue-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #475569;
  font-weight: 500;
}

.revenue-icon {
  font-size: 16px;
}

.revenue-icon.income {
  color: #10b981;
  transform: rotate(-90deg);
}

.revenue-icon.expense {
  color: #ef4444;
  transform: rotate(90deg);
}

.revenue-value {
  font-size: 18px;
  font-weight: 700;
}

.revenue-value.income {
  color: #10b981;
}

.revenue-value.expense {
  color: #ef4444;
}

.revenue-summary {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 2px solid #e0e7ff;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  font-size: 16px;
  font-weight: 600;
  color: #1e40af;
}

.summary-value {
  font-size: 20px;
  font-weight: 700;
}

.summary-value.positive {
  color: #10b981;
}

.summary-value.negative {
  color: #ef4444;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .banner-content {
    flex-direction: column;
    text-align: center;
  }
  
  .banner-icon {
    margin-top: 16px;
  }
  
  .stats-row {
    margin-bottom: 16px;
  }
  
  .main-content {
    padding: 16px;
  }
}
</style>