<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline @submit.prevent="getList">
        <el-form-item label="项目名称">
          <el-input
            v-model="queryParams.projectName"
            placeholder="请输入项目名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="项目编号">
          <el-input
            v-model="queryParams.projectCode"
            placeholder="请输入项目编号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 200px"
          >
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已暂停" value="已暂停" />
            <el-option label="已取消" value="已取消" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="getList">查询</el-button>
          <el-button class="btn-reset" @click="resetQuery">重置</el-button>
          <el-button type="success" class="btn-add" @click="openDialog">新增项目</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <span class="title-main">项目列表</span>
            <span class="title-sub">共 <span class="title-number">{{ total }}</span> 个项目</span>
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
        <el-table-column label="项目ID" prop="projectId" width="80" align="center" />
        <el-table-column label="项目编号" prop="projectCode" min-width="120" />
        <el-table-column label="项目名称" prop="projectName" min-width="150" />
        <el-table-column label="项目经理" prop="projectManager" width="120" />
        <el-table-column label="开始日期" prop="startDate" width="120">
          <template #default="scope">
            {{ scope.row.startDate ? formatDate(scope.row.startDate, 'YYYY-MM-DD') : 'N/A' }}
          </template>
        </el-table-column>
        <el-table-column label="结束日期" prop="endDate" width="120">
          <template #default="scope">
            {{ scope.row.endDate ? formatDate(scope.row.endDate, 'YYYY-MM-DD') : 'N/A' }}
          </template>
        </el-table-column>
        <el-table-column label="预算金额" prop="budget" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.budget) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description" min-width="200" />
        <el-table-column label="创建时间" prop="createTime" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" align="center" fixed="right">
          <template #default="scope">
            <div class="operation-group">
              <el-button
                type="primary"
                size="small"
                class="btn-op btn-edit"
                icon="el-icon-edit"
                @click="handleEdit(scope.row)"
                :disabled="scope.row.status === '已完成' || scope.row.status === '已取消'"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                class="btn-op btn-delete"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
                :disabled="scope.row.status === '已完成' || scope.row.status === '已取消'"
              >
                取消项目
              </el-button>
              <el-button
                size="small"
                :class="['btn-op', 'btn-status', scope.row.status === '进行中' ? 'btn-pause' : 'btn-resume']"
                @click="handleChangeStatus(scope.row)"
                v-if="scope.row.status === '进行中' || scope.row.status === '已暂停'"
              >
                {{ scope.row.status === '进行中' ? '暂停' : '启动' }}
              </el-button>
              <el-button
                size="small"
                class="btn-op btn-complete"
                type="success"
                @click="handleComplete(scope.row)"
                v-if="scope.row.status === '进行中' || scope.row.status === '已暂停'"
              >
                完成
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
        <el-form-item label="项目ID" prop="projectId" v-if="isEdit">
          <el-input v-model="formData.projectId" disabled />
        </el-form-item>
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="formData.projectCode" placeholder="请输入项目编号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="formData.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目经理" prop="projectManager">
          <el-input v-model="formData.projectManager" placeholder="请输入项目经理" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            placeholder="请选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            placeholder="请选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预算金额" prop="budget">
          <el-input-number
            v-model="formData.budget"
            :min="0"
            :precision="2"
            :step="100"
            placeholder="请输入预算金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="项目状态" prop="status" v-if="isEdit">
          <el-select v-model="formData.status" placeholder="请选择项目状态" :disabled="formData.status === '已完成' || formData.status === '已取消'">
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已暂停" value="已暂停" />
            <el-option label="已取消" value="已取消" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            placeholder="请输入项目描述"
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
import {
  addProject,
  getProjectList,
  getProjectDetail,
  updateProject,
  deleteProject,
  changeProjectStatus
} from '@/api/project'

// 日期格式化工具
const formatDate = (dateStr, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!dateStr) return ''
  const date = new Date(dateStr.replace(/-/g, '/'))
  if (isNaN(date.getTime())) return ''
  
  if (format === 'YYYY-MM-DD') {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
  
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

// 金额格式化
const formatCurrency = (amount) => {
  if (amount == null || amount === undefined) return '¥0.00'
  return `¥${Number(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`
}

// 状态标签类型
const getStatusTagType = (status) => {
  const statusMap = {
    '进行中': 'success',
    '已完成': 'info',
    '已暂停': 'warning',
    '已取消': 'danger'
  }
  return statusMap[status] || 'info'
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
const dialogTitle = ref('新增项目')
// 是否为编辑模式
const isEdit = ref(false)
// 表单引用
const formRef = ref(null)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  projectName: '',
  projectCode: '',
  status: ''
})

// 表单数据
const formData = reactive({
  projectId: null,
  projectCode: '',
  projectName: '',
  projectManager: '',
  startDate: '',
  endDate: '',
  budget: 0,
  status: '进行中',
  description: ''
})

// 表单校验规则
const formRules = reactive({
  projectCode: [
    { required: true, message: '请输入项目编号', trigger: 'blur' }
  ],
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ]
})

// 页面加载时查询列表
onMounted(() => {
  getList()
})

// 获取项目列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getProjectList(queryParams)
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
  queryParams.projectName = ''
  queryParams.projectCode = ''
  queryParams.status = ''
  getList()
}

// 打开新增弹窗
const openDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增项目'
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.projectId = null
  formData.projectCode = ''
  formData.projectName = ''
  formData.projectManager = ''
  formData.startDate = ''
  formData.endDate = ''
  formData.budget = 0
  formData.status = '进行中'
  formData.description = ''
}

// 编辑项目
const handleEdit = async (row) => {
  if (row.status === '已完成' || row.status === '已取消') {
    ElMessage.warning('已完成或已取消的项目不允许编辑')
    return
  }
  isEdit.value = true
  dialogTitle.value = '编辑项目'
  resetForm()
  try {
    const res = await getProjectDetail(row.projectId)
    const data = res.data
    formData.projectId = data.projectId
    formData.projectCode = data.projectCode || ''
    formData.projectName = data.projectName || ''
    formData.projectManager = data.projectManager || ''
    formData.startDate = data.startDate ? formatDate(data.startDate, 'YYYY-MM-DD') : ''
    formData.endDate = data.endDate ? formatDate(data.endDate, 'YYYY-MM-DD') : ''
    formData.budget = data.budget || 0
    formData.status = data.status || '进行中'
    formData.description = data.description || ''
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.response?.data?.msg || error.message))
  }
}

// 删除项目（软删除，状态改为"已取消"）
const handleDelete = async (row) => {
  if (row.status === '已完成') {
    ElMessage.warning('已完成的项目不允许删除')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定取消项目【${row.projectName}】吗？`,
      '确认取消',
      { type: 'warning' }
    )
    await deleteProject(row.projectId)
    ElMessage.success('项目已取消')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 切换项目状态
const handleChangeStatus = async (row) => {
  let targetStatus = ''
  let actionText = ''
  if (row.status === '进行中') {
    targetStatus = '已暂停'
    actionText = '暂停'
  } else if (row.status === '已暂停') {
    targetStatus = '进行中'
    actionText = '启动'
  }
  
  try {
    await ElMessageBox.confirm(
      `确定将项目【${row.projectName}】${actionText}吗？`,
      '确认操作',
      { type: 'warning' }
    )
    await changeProjectStatus(row.projectId, targetStatus)
    ElMessage.success(`${actionText}成功`)
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 完成项目
const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定将项目【${row.projectName}】标记为已完成吗？`,
      '确认操作',
      { type: 'warning' }
    )
    await changeProjectStatus(row.projectId, '已完成')
    ElMessage.success('项目已完成')
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
    
    // 编辑时检查状态
    if (isEdit.value && (formData.status === '已完成' || formData.status === '已取消')) {
      ElMessage.warning('已完成或已取消的项目不允许编辑')
      return
    }
    
    // 准备提交数据，处理日期格式
    const submitData = {
      ...formData,
      // 如果日期为空字符串，则设置为null
      startDate: formData.startDate || null,
      endDate: formData.endDate || null,
      // 确保预算金额为数字类型
      budget: formData.budget || 0
    }
    
    // 提交数据
    if (isEdit.value) {
      await updateProject(submitData)
      ElMessage.success('编辑成功')
    } else {
      await addProject(submitData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(
      isEdit.value 
        ? '编辑失败：' + (error.response?.data?.msg || error.message || '未知错误')
        : '新增失败：' + (error.response?.data?.msg || error.message || '未知错误')
    )
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
  min-width: 300px;
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

.btn-op:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.btn-op:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
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

/* 暂停按钮 - 警告色（黄色/橙色） */
.btn-status.btn-pause {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  border: 1px solid #fcd34d;
}

.btn-status.btn-pause:hover {
  background: linear-gradient(135deg, #fde68a 0%, #fcd34d 100%);
  color: #78350f;
}

/* 启动按钮 - 成功色（绿色） */
.btn-status.btn-resume {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #15803d;
  border: 1px solid #86efac;
}

.btn-status.btn-resume:hover {
  background: linear-gradient(135deg, #bbf7d0 0%, #86efac 100%);
  color: #166534;
}

.btn-complete {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #15803d;
  border: 1px solid #86efac;
}

.btn-complete:hover {
  background: linear-gradient(135deg, #bbf7d0 0%, #86efac 100%);
  color: #166534;
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

