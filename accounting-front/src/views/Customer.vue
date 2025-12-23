<template>
  <div class="customer-page">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline @submit.prevent="getList">
        <el-form-item label="客户名称">
          <el-input
            v-model="queryParams.customerName"
            placeholder="请输入客户名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input
            v-model="queryParams.phone"
            placeholder="请输入联系电话"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="客户状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 200px"
          >
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="getList">查询</el-button>
          <el-button class="btn-reset" @click="resetQuery">重置</el-button>
          <el-button type="success" class="btn-add" @click="openDialog">新增客户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <span class="title-main">客户列表</span>
            <span class="title-sub">共 <span class="title-number">{{ total }}</span> 位客户</span>
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
        <el-table-column label="客户ID" prop="customerId" width="80" align="center" />
        <el-table-column label="客户名称" prop="customerName" min-width="150" />
        <el-table-column label="联系电话" prop="phone" width="120" />
        <el-table-column label="邮箱" prop="email" min-width="180" />
        <el-table-column label="地址" prop="address" min-width="200" />
        <el-table-column label="注册日期" prop="registrationDate" width="180">
          <template #default="scope">
            {{ scope.row.registrationDate ? formatDate(scope.row.registrationDate) : '未填写' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '启用' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remarks" min-width="200" />
        <el-table-column label="创建时间" prop="createTime" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="270" align="center" fixed="right">
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
                type="danger"
                size="small"
                class="btn-op btn-delete"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
              <el-button
                size="small"
                :class="['btn-op', 'btn-status', scope.row.status === '启用' ? 'btn-disable' : 'btn-enable']"
                @click="handleChangeStatus(scope.row)"
              >
                {{ scope.row.status === '启用' ? '停用' : '启用' }}
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
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="客户ID" prop="customerId" v-if="isEdit">
          <el-input v-model="formData.customerId" disabled />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" maxlength="50" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" maxlength="255" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input
            v-model="formData.address"
            type="textarea"
            placeholder="请输入详细地址"
            maxlength="500"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="注册日期" prop="registrationDate">
          <el-date-picker
            v-model="formData.registrationDate"
            type="datetime"
            placeholder="请选择注册日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="客户状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择客户状态">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="formData.remarks"
            type="textarea"
            placeholder="请输入备注信息"
            maxlength="500"
            :rows="3"
          />
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
// 引入API（与后端接口一一对应）
import {
  addCustomer,
  getCustomerList,
  getCustomerDetail,
  updateCustomer,
  deleteCustomer,
  changeCustomerStatus
} from '@/api/customer'

// 日期格式化工具（适配后端Date类型返回的字符串）
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  // 处理后端返回的Date字符串（如"2023-12-06 15:30:00"）
  const date = new Date(dateStr.replace(/-/g, '/')) // 兼容低版本浏览器
  if (isNaN(date.getTime())) return ''
  return `${date.getFullYear()}-${
    String(date.getMonth() + 1).padStart(2, '0')
  }-${
    String(date.getDate()).padStart(2, '0')
  } ${
    String(date.getHours()).padStart(2, '0')
  }:${
    String(date.getMinutes()).padStart(2, '0')
  }:${
    String(date.getSeconds()).padStart(2, '0')
  }`
}

// 加载状态
const loading = ref(false)
// 表格数据
const tableData = ref([])
// 总条数
const total = ref(0)
// 弹窗显示状态
const dialogVisible = ref(false)
// 弹窗标题
const dialogTitle = ref('新增客户')
// 是否为编辑模式
const isEdit = ref(false)
// 表单引用
const formRef = ref(null)

// 查询参数（与后端接口参数完全一致）
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  customerName: '',
  phone: '',
  status: ''
})

// 表单数据（与后端实体类字段完全一致）
const formData = reactive({
  customerId: null,
  customerName: '',
  phone: '',
  email: '',
  address: '',
  registrationDate: '',
  remarks: '',
  status: '启用' // 默认状态
})

// 表单校验规则（匹配数据库非空约束）
const formRules = reactive({
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择客户状态', trigger: 'change' }
  ]
})

// 页面加载时查询列表
onMounted(() => {
  getList()
})

// 获取客户列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getCustomerList(queryParams)
    // 适配后端ResultVO格式（data为分页数据）
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
  queryParams.customerName = ''
  queryParams.phone = ''
  queryParams.status = ''
  getList()
}

// 打开新增弹窗
const openDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增客户'
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.customerId = null
  formData.customerName = ''
  formData.phone = ''
  formData.email = ''
  formData.address = ''
  formData.registrationDate = ''
  formData.remarks = ''
  formData.status = '启用'
}

// 编辑客户
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑客户'
  resetForm()
  try {
    const res = await getCustomerDetail(row.customerId)
    const data = res.data
    // 表单回显（字段名与后端返回完全一致）
    formData.customerId = data.customerId
    formData.customerName = data.customerName || ''
    formData.phone = data.phone || ''
    formData.email = data.email || ''
    formData.address = data.address || ''
    formData.registrationDate = data.registrationDate || ''
    formData.remarks = data.remarks || ''
    formData.status = data.status || '启用'
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.response?.data?.msg || error.message))
  }
}

// 删除客户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除客户【${row.customerName}】吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteCustomer(row.customerId)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 切换客户状态
const handleChangeStatus = async (row) => {
  const targetStatus = row.status === '启用' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定将客户【${row.customerName}】${targetStatus}吗？`,
      '确认操作',
      { type: 'warning' }
    )
    await changeCustomerStatus({
      customerId: row.customerId,
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
    // 提交数据（字段名与后端实体类完全一致）
    if (isEdit.value) {
      await updateCustomer(formData)
      ElMessage.success('编辑成功')
    } else {
      await addCustomer(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    ElMessage.error(
      isEdit.value 
        ? '编辑失败：' + (error.response?.data?.msg || error.message)
        : '新增失败：' + (error.response?.data?.msg || error.message)
    )
  }
}
</script>

<style scoped>
/* 统一页面容器 */
.customer-page {
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
  min-width: 250px;
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

.btn-delete {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
  border: 1px solid #fca5a5;
}

.btn-delete:hover {
  background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
  color: #b91c1c;
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

/* 统一表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  color: #1e40af;
  font-weight: 600;
  border-bottom: 2px solid #bfdbfe;
}

:deep(.el-table td) {
  border-bottom: 1px solid #e0e7ff;
  padding: 12px 8px;
  font-size: 14px;
}

:deep(.el-table th) {
  padding: 12px 8px;
  font-size: 14px;
}

:deep(.el-table__fixed-right .el-table__cell) {
  padding: 12px 12px !important;
}

:deep(.el-table__fixed-right-patch) {
  background-color: #fff;
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