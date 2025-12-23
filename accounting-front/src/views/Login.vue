<template>
  <div class="login-page">
    <div class="login-panel">
      <!-- 左侧品牌区 -->
      <div class="login-left">
        <div class="brand-logo">
          <div class="brand-icon">
            <span class="brand-icon-square"></span>
          </div>
          <div class="brand-text">
            <div class="brand-title">记账系统</div>
            <div class="brand-subtitle">小微企业智能财务助手</div>
          </div>
        </div>
        <div class="brand-desc">
          <p>简化单据、自动生成分录、实时查看往来与报表，</p>
          <p>让记账更简单，让经营更清晰。</p>
        </div>
      </div>

      <!-- 右侧登录/注册卡片 -->
      <div class="login-card">
        <h1 class="login-title">{{ isRegister ? '员工注册' : '员工登录' }}</h1>
        <p class="login-subtitle">{{ isRegister ? '填写信息注册新账号' : '使用预留手机号和密码登录系统' }}</p>
        
        <!-- 登录表单 -->
        <el-form
          v-if="!isRegister"
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-width="80px"
          class="login-form"
        >
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="loginForm.phone"
              placeholder="请输入手机号"
              clearable
              size="large"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              clearable
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item class="login-btn-group">
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              @click="handleLogin"
              :loading="isLoading"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 注册表单 -->
        <el-form
          v-else
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="80px"
          class="login-form"
        >
          <el-form-item label="姓名" prop="name">
            <el-input
              v-model="registerForm.name"
              placeholder="请输入姓名"
              clearable
              size="large"
            />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              clearable
              size="large"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              clearable
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              clearable
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item label="身份" prop="roleId">
            <el-select
              v-model="registerForm.roleId"
              placeholder="请选择身份"
              size="large"
              style="width: 100%"
              @change="(val) => {
                console.log('角色选择变化，新值：', val, '类型：', typeof val)
                registerFormRef?.validateField('roleId')
              }"
            >
              <el-option label="会计" :value="2" />
              <el-option label="采购" :value="3" />
              <el-option label="出纳" :value="4" />
              <el-option label="销售" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item class="login-btn-group">
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              @click="handleRegister"
              :loading="isLoading"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 切换登录/注册 -->
        <div class="switch-mode">
          <span v-if="!isRegister">还没有账号？</span>
          <span v-else>已有账号？</span>
          <el-link type="primary" @click="toggleMode" :underline="false">
            {{ isRegister ? '立即登录' : '立即注册' }}
          </el-link>
        </div>

        <div v-if="!isRegister" class="login-tips">
          测试账号：<span>13800138000 / 123456</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 恢复axios的登录请求导入（核心改回点）
import { userLogin, userRegister } from '@/api/login'

const router = useRouter()
const isLoading = ref(false)
const isRegister = ref(false) // 切换登录/注册模式
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loginForm = ref({
  phone: '13800138000',
  password: '123456'
})

const registerForm = ref({
  name: '',
  phone: '',
  password: '',
  confirmPassword: '',
  roleId: null
})

const loginRules = ref({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = ref({
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, message: '姓名长度不能少于2位', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  roleId: [
    { 
      required: true, 
      message: '请选择身份', 
      trigger: ['change', 'blur']
    }
  ]
})

// 切换登录/注册模式
const toggleMode = () => {
  isRegister.value = !isRegister.value
  // 清空表单
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
  if (registerFormRef.value) {
    registerFormRef.value.resetFields()
    // 重置角色选择
    registerForm.value.roleId = null
  }
}

const handleLogin = async () => {
  // 1. 表单验证
  try {
    await loginFormRef.value.validate()
    console.log('✅ 表单验证通过，准备发送登录请求：', loginForm.value)
  } catch (error) {
    ElMessage.warning('请完善表单信息')
    console.log('❌ 表单验证失败：', error)
    return
  }

  isLoading.value = true
  try {
    // ========== 改回axios请求（核心改回点） ==========
    const res = await userLogin(loginForm.value)
    console.log('✅ axios请求成功，后端响应：', res)
    
    // 2. 验证响应字段（兜底容错）
    if (!res || !res.data) {
      throw new Error('响应数据异常，缺少Token')
    }
    
    // 3. 保存Token和角色信息 + 提示 + 跳转
    const loginData = res.data
    console.log('📥 登录返回数据：', loginData)
    
    // 处理Token和角色信息
    if (typeof loginData === 'string') {
      // 兼容旧格式：直接返回token字符串
      localStorage.setItem('token', loginData)
      localStorage.setItem('role', 'ADMIN')
      localStorage.setItem('roleId', '1')
      localStorage.setItem('phone', loginForm.value.phone) // 保存手机号
      console.log('⚠️ 使用旧格式，默认设置为管理员')
    } else if (loginData && typeof loginData === 'object') {
      // 新格式：包含token、role、roleId的对象
      localStorage.setItem('token', loginData.token || loginData)
      localStorage.setItem('phone', loginForm.value.phone) // 保存手机号
      if (loginData.role) {
        localStorage.setItem('role', loginData.role)
        localStorage.setItem('roleId', String(loginData.roleId || '1'))
        console.log('✅ 保存角色信息 - 角色：', loginData.role, '角色ID：', loginData.roleId, '手机号：', loginForm.value.phone)
      } else {
        // 如果没有角色信息，默认为管理员
        localStorage.setItem('role', 'ADMIN')
        localStorage.setItem('roleId', '1')
        console.log('⚠️ 未找到角色信息，默认设置为管理员')
      }
    } else {
      throw new Error('登录响应数据格式异常')
    }
    
    // 验证保存的角色信息
    const savedRole = localStorage.getItem('role')
    const savedRoleId = localStorage.getItem('roleId')
    const savedPhone = localStorage.getItem('phone')
    console.log('💾 验证保存的用户信息 - 角色：', savedRole, '角色ID：', savedRoleId, '手机号：', savedPhone)
    
    // 重要：验证角色ID和角色名称的对应关系
    if (savedRoleId === '1' && savedRole !== 'ADMIN') {
      console.error('❌ 角色ID和角色名称不匹配！角色ID=1应该是ADMIN，但实际是：', savedRole)
      ElMessage.warning('角色信息异常，请重新登录')
    }
    if (savedRoleId === '2' && savedRole !== 'ACCOUNTANT') {
      console.error('❌ 角色ID和角色名称不匹配！角色ID=2应该是ACCOUNTANT，但实际是：', savedRole)
      ElMessage.warning('角色信息异常，请重新登录')
    }
    if (savedRoleId === '3' && savedRole !== 'PURCHASER') {
      console.error('❌ 角色ID和角色名称不匹配！角色ID=3应该是PURCHASER，但实际是：', savedRole)
      ElMessage.warning('角色信息异常，请重新登录')
    }
    if (savedRoleId === '4' && savedRole !== 'CASHIER') {
      console.error('❌ 角色ID和角色名称不匹配！角色ID=4应该是CASHIER，但实际是：', savedRole)
      ElMessage.warning('角色信息异常，请重新登录')
    }
    if (savedRoleId === '5' && savedRole !== 'SALES') {
      console.error('❌ 角色ID和角色名称不匹配！角色ID=5应该是SALES，但实际是：', savedRole)
      ElMessage.warning('角色信息异常，请重新登录')
    }
    
    ElMessage.success(res.msg || '登录成功')
    
    // 延迟跳转，确保localStorage已保存，然后强制刷新页面以确保菜单正确更新
    setTimeout(() => {
      router.push('/home').then(() => {
        // 强制刷新页面以确保菜单根据新角色正确显示
        console.log('🔄 登录成功，刷新页面以更新菜单')
        window.location.reload()
      }).catch(err => {
        console.error('路由跳转失败：', err)
        ElMessage.error('页面跳转失败，请刷新页面')
      })
    }, 200)

  } catch (error) {
    // 4. 详细打印错误，定位根因
    console.error('❌ 登录失败详情：', {
      错误信息: error.msg || error.message,
      错误对象: error,
      手机号: loginForm.value.phone,
      密码: loginForm.value.password
    })
    ElMessage.error(error.msg || error.message || '登录失败，请检查账号密码')
  } finally {
    // 5. 重置loading状态
    isLoading.value = false
  }
}

const handleRegister = async () => {
  // 调试：打印当前表单值
  console.log('📝 注册表单当前值：', registerForm.value)
  console.log('📝 roleId值：', registerForm.value.roleId, '类型：', typeof registerForm.value.roleId)
  
  // 1. 表单验证
  try {
    // 先验证所有字段
    await registerFormRef.value.validate()
    
    // 额外验证roleId（确保已选择）
    const roleId = registerForm.value.roleId
    console.log('🔍 提交前roleId检查，值：', roleId, '类型：', typeof roleId)
    if (roleId === null || roleId === undefined || roleId === '') {
      console.warn('⚠️ roleId验证失败，当前值：', roleId)
      ElMessage.warning('请选择身份')
      return
    }
    
    // 确保roleId是有效数字（2-5）
    if (typeof roleId !== 'number' || roleId < 2 || roleId > 5) {
      console.warn('⚠️ roleId值无效，当前值：', roleId)
      ElMessage.warning('请选择有效的身份')
      return
    }
    
    console.log('✅ 注册表单验证通过，准备发送注册请求：', registerForm.value)
  } catch (error) {
    console.log('❌ 注册表单验证失败：', error)
    console.log('❌ 验证错误详情：', error)
    // Element Plus的validate失败会抛出错误对象，检查字段错误
    if (error && typeof error === 'object') {
      const fields = Object.keys(error)
      if (fields.includes('roleId')) {
        ElMessage.warning('请选择身份')
      } else {
        ElMessage.warning('请完善表单信息')
      }
    } else {
      ElMessage.warning('请完善表单信息')
    }
    return
  }

  isLoading.value = true
  try {
    const res = await userRegister({
      name: registerForm.value.name,
      phone: registerForm.value.phone,
      password: registerForm.value.password,
      roleId: registerForm.value.roleId
    })
    console.log('✅ 注册请求成功，后端响应：', res)
    
    ElMessage.success(res.msg || '注册成功')
    
    // 注册成功后切换到登录页面
    setTimeout(() => {
      isRegister.value = false
      loginForm.value.phone = registerForm.value.phone
      loginForm.value.password = ''
      registerFormRef.value.resetFields()
    }, 1500)

  } catch (error) {
    console.error('❌ 注册失败详情：', {
      错误信息: error.msg || error.message,
      错误对象: error
    })
    ElMessage.error(error.msg || error.message || '注册失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle at top left, #4f46e5 0, #1d4ed8 25%, #0f172a 60%, #020617 100%);
}

.login-panel {
  width: 880px;
  max-width: 95vw;
  min-height: 420px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.45);
  display: flex;
  overflow: hidden;
}

.login-left {
  flex: 1.1;
  padding: 40px 40px 40px 48px;
  background: linear-gradient(135deg, #eef2ff 0%, #e0f2fe 40%, #ecfeff 100%);
  border-radius: 24px 0 0 24px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.brand-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(135deg, #4f46e5 0%, #3b82f6 50%, #22c55e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 30px rgba(59, 130, 246, 0.35);
}

.brand-icon-square {
  width: 26px;
  height: 18px;
  border-radius: 6px;
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.1);
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  letter-spacing: 1px;
}

.brand-subtitle {
  font-size: 14px;
  color: #4b5563;
  margin-top: 4px;
}

.brand-desc {
  margin-top: 8px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.8;
}

.login-card {
  flex: 1;
  padding: 40px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-title {
  text-align: left;
  color: #111827;
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: 700;
}

.login-subtitle {
  margin: 0 0 28px 0;
  font-size: 13px;
  color: #6b7280;
}

.login-form {
  margin-top: 8px;
}

.login-btn-group {
  margin-top: 8px;
  text-align: center;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  border-radius: 999px;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.3);
}

.switch-mode {
  margin-top: 20px;
  font-size: 14px;
  color: #6b7280;
  text-align: center;
}

.switch-mode .el-link {
  margin-left: 8px;
  font-weight: 500;
}

.login-tips {
  margin-top: 18px;
  font-size: 12px;
  color: #9ca3af;
  text-align: center;
}

.login-tips span {
  color: #2563eb;
  font-weight: 500;
}

@media (max-width: 768px) {
  .login-panel {
    flex-direction: column;
    border-radius: 20px;
  }

  .login-left {
    border-radius: 20px 20px 0 0;
    padding: 24px 20px;
  }

  .login-card {
    padding: 24px 20px 28px;
  }

  .brand-desc {
    display: none;
  }
}
</style>*** End Patch```}}/>