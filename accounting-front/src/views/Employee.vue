<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline @submit.prevent="getList">
        <el-form-item label="员工姓名">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入员工姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="queryParams.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="员工状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 200px"
          >
            <el-option label="在职" value="在职" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="getList">查询</el-button>
          <el-button class="btn-reset" @click="resetQuery">重置</el-button>
          <el-button type="success" class="btn-add" @click="openDialog">新增员工</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <span class="title-main">员工列表</span>
            <span class="title-sub">共 <span class="title-number">{{ total }}</span> 位员工</span>
          </div>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column label="员工ID" prop="employeeId" width="100" align="center" />
        <el-table-column label="员工姓名" prop="name" min-width="120" />
        <el-table-column label="手机号" prop="phone" min-width="140" />
        <el-table-column label="角色" prop="roleName" min-width="120">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.roleName)">
              {{ scope.row.roleName || '未分配' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '在职' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <div class="operation-group">
              <el-button
                type="primary"
                size="small"
                class="btn-op btn-edit"
                icon="el-icon-edit"
                @click="handleEdit(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                :class="['btn-op', 'btn-status', scope.row.status === '在职' ? 'btn-disable' : 'btn-enable']"
                @click="handleChangeStatus(scope.row)"
              >
                {{ scope.row.status === '在职' ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="员工ID" prop="employeeId" v-if="isEdit">
          <el-input v-model="formData.employeeId" disabled />
        </el-form-item>
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="修改密码" prop="password" v-if="isEdit">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="留空则不修改密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="formData.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roleList"
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="员工状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择员工状态" style="width: 100%">
            <el-option label="在职" value="在职" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addEmployee,
  getEmployeeList,
  getEmployeeDetail,
  updateEmployee,
  getAllRoles
} from '@/api/employee'

// 加载状态
const loading = ref(false)
// 表格数据
const tableData = ref([])
// 总条数
const total = ref(0)
// 弹窗显示状态
const dialogVisible = ref(false)
// 弹窗标题
const dialogTitle = ref('新增员工')
// 是否为编辑模式
const isEdit = ref(false)
// 表单引用
const formRef = ref(null)
// 角色列表
const roleList = ref([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  phone: '',
  status: ''
})

// 表单数据
const formData = reactive({
  employeeId: null,
  name: '',
  phone: '',
  password: '',
  roleId: null,
  status: '在职' // 默认状态
})

// 表单校验规则
const formRules = reactive({
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    {
      validator: (rule, value, callback) => {
        if (!isEdit.value && (!value || value.trim() === '')) {
          callback(new Error('请输入密码'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  status: [
    { required: true, message: '请选择员工状态', trigger: 'change' }
  ]
})

// 获取角色标签类型
const getRoleTagType = (roleName) => {
  const roleTypeMap = {
    'ADMIN': 'danger',
    'ACCOUNTANT': 'success',
    'PURCHASER': 'warning',
    'CASHIER': 'info',
    'SALES': 'primary'
  }
  return roleTypeMap[roleName] || ''
}

// 页面加载时查询列表和角色
onMounted(() => {
  loadRoles()
  getList()
})

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await getAllRoles()
    roleList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取角色列表失败：' + (error.response?.data?.msg || error.message))
  }
}

// 获取员工列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getEmployeeList(queryParams)
    // 适配后端返回格式
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取列表失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.pageNum = 1
  queryParams.name = ''
  queryParams.phone = ''
  queryParams.status = ''
  getList()
}

// 打开新增弹窗
const openDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增员工'
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.employeeId = null
  formData.name = ''
  formData.phone = ''
  formData.password = ''
  formData.roleId = null
  formData.status = '在职'
}

// 编辑员工
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑员工'
  resetForm()
  try {
    const res = await getEmployeeDetail(row.employeeId)
    const data = res.data
    // 表单回显
    formData.employeeId = data.employeeId
    formData.name = data.name || ''
    formData.phone = data.phone || ''
    formData.roleId = data.roleId || null
    formData.status = data.status || '在职'
    formData.password = '' // 编辑时不显示密码
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.response?.data?.msg || error.message))
  }
}

// 切换员工状态
const handleChangeStatus = async (row) => {
  const targetStatus = row.status === '在职' ? '停用' : '在职'
  try {
    await ElMessageBox.confirm(
      `确定将员工【${row.name}】${targetStatus}吗？`,
      '确认操作',
      { type: 'warning' }
    )
    await updateEmployee({
      employeeId: row.employeeId,
      status: targetStatus
    })
    ElMessage.success(`${targetStatus}成功`)
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 提交表单（新增/编辑）
const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    
    // 构建提交数据
    const submitData = {
      name: formData.name.trim(),
      phone: formData.phone.trim(),
      status: formData.status
    }
    
    // 角色ID
    if (formData.roleId) {
      submitData.roleIdStr = String(formData.roleId)
    }
    
    // 密码（新增时必须，编辑时可选）
    if (!isEdit.value) {
      if (!formData.password || formData.password.trim() === '') {
        ElMessage.error('请输入密码')
        return
      }
      submitData.password = formData.password
    } else {
      // 编辑时，如果填写了密码则更新
      if (formData.password && formData.password.trim() !== '') {
        submitData.password = formData.password
      }
      submitData.employeeId = formData.employeeId
    }
    
    // 提交数据
    if (isEdit.value) {
      await updateEmployee(submitData)
      ElMessage.success('编辑成功')
    } else {
      await addEmployee(submitData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    if (error.message && !error.message.includes('validate')) {
      ElMessage.error(
        isEdit.value 
          ? '编辑失败：' + (error.response?.data?.msg || error.message)
          : '新增失败：' + (error.response?.data?.msg || error.message)
      )
    }
  }
}
</script>

<style scoped>
/* 统一页面容器 */
.page-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  min-height: calc(100vh - 64px);
}

/* 统一搜索卡片 */
.search-card {
  background: #fff;
  padding: 20px;
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.08);
  border: 1px solid #e0e7ff;
}

/* 统一表格卡片 */
.table-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.08);
  border: 1px solid #e0e7ff;
}

/* 统一表格标题 */
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 2px solid #e0e7ff;
  margin-bottom: 16px;
}

.table-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.title-main {
  font-size: 18px;
  font-weight: 600;
  color: #1e40af;
}

.title-sub {
  font-size: 14px;
  color: #64748b;
}

.title-number {
  font-size: 16px;
  font-weight: 700;
  color: #3b82f6;
  margin: 0 2px;
}

/* 统一按钮样式 - 商业蓝色主题 */
.btn-search {
  padding: 10px 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn-search:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
}

.btn-reset {
  padding: 10px 24px;
  border-radius: 8px;
  color: #475569;
  border: 1px solid #cbd5e1;
  background: #fff;
  transition: all 0.3s ease;
}

.btn-reset:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

.btn-add {
  padding: 10px 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
  font-weight: 500;
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

/* 统一表格操作按钮 */
.operation-group {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
  width: 100%;
  min-width: 200px;
  padding: 0 12px;
  box-sizing: border-box;
}

.btn-op {
  border-radius: 6px;
  padding: 4px 6px;
  font-size: 12px;
  transition: all 0.2s ease;
  border: none;
  font-weight: 500;
  width: 58px;
  min-width: 58px;
  max-width: 58px;
  height: 28px;
  min-height: 28px;
  max-height: 28px;
  text-align: center;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  flex-shrink: 0;
  box-sizing: border-box;
}

.btn-op:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

.btn-op:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-edit {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
  border: 1px solid #93c5fd;
}

.btn-edit:hover {
  background: linear-gradient(135deg, #bfdbfe 0%, #93c5fd 100%);
  color: #1e3a8a;
}

/* 启用按钮 - 绿色 */
.btn-status.btn-enable {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #15803d;
  border: 1px solid #86efac;
}

.btn-status.btn-enable:hover {
  background: linear-gradient(135deg, #bbf7d0 0%, #86efac 100%);
  color: #166534;
}

/* 停用按钮 - 灰色 */
.btn-status.btn-disable {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #64748b;
  border: 1px solid #cbd5e1;
}

.btn-status.btn-disable:hover {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  color: #475569;
}

:deep(.btn-op) {
  width: 58px !important;
  min-width: 58px !important;
  max-width: 58px !important;
  height: 28px !important;
  min-height: 28px !important;
  max-height: 28px !important;
  padding: 4px 6px !important;
  text-align: center !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  line-height: 1 !important;
  flex-shrink: 0 !important;
  box-sizing: border-box !important;
}

/* 统一表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  color: #1e40af;
  font-weight: 600;
  border-bottom: 2px solid #bfdbfe;
  padding: 12px 8px;
  font-size: 14px;
}

:deep(.el-table td) {
  border-bottom: 1px solid #e0e7ff;
  padding: 12px 8px;
  font-size: 14px;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #f8fafc;
}

:deep(.el-table--border) {
  border: 1px solid #cbd5e1;
}

:deep(.el-table--border th),
:deep(.el-table--border td) {
  border-right: 1px solid #e0e7ff;
}

:deep(.el-table__fixed-right .el-table__cell) {
  padding: 12px 12px !important;
}

:deep(.el-table__fixed-right-patch) {
  background-color: #fff;
}

/* 统一分页样式 */
:deep(.el-pagination) {
  margin-top: 20px;
  justify-content: flex-end;
}

:deep(.el-pagination .btn-next),
:deep(.el-pagination .btn-prev) {
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #475569;
}

:deep(.el-pagination .btn-next:hover),
:deep(.el-pagination .btn-prev:hover) {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
}

:deep(.el-pagination .number) {
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #475569;
}

:deep(.el-pagination .number:hover) {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
}

:deep(.el-pagination .number.is-active) {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-color: #2563eb;
  color: #fff;
}
</style>

