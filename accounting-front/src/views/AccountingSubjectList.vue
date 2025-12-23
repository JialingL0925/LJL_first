<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="科目名称">
          <el-input v-model="searchForm.subjectName" placeholder="请输入科目名称"></el-input>
        </el-form-item>
        <el-form-item label="科目类型">
          <el-select v-model="searchForm.subjectType" placeholder="请选择">
            <el-option label="资产" value="资产"></el-option>
            <el-option label="负债" value="负债"></el-option>
            <el-option label="所有者权益" value="所有者权益"></el-option>
            <el-option label="成本" value="成本"></el-option>
            <el-option label="损益" value="损益"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择">
            <el-option label="启用" value="启用"></el-option>
            <el-option label="停用" value="停用"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="getList">查询</el-button>
          <el-button class="btn-reset" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card shadow="never" class="operate-card">
      <el-button type="success" class="btn-add" @click="handleAdd">新增科目</el-button>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="subjectCode" label="科目编码" width="120"></el-table-column>
        <el-table-column prop="subjectName" label="科目名称" min-width="150"></el-table-column>
        <el-table-column prop="subjectLevel" label="科目级别" width="100">
          <template #default="scope">
            {{ scope.row.subjectLevel }}级
          </template>
        </el-table-column>
        <el-table-column prop="subjectType" label="科目类型" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '启用' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            <!-- 原生日期格式化，兼容空值 -->
            {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString() : '' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <div class="operation-group">
              <el-button type="primary" size="small" class="btn-op btn-edit" @click="handleEdit(scope.row)">编辑</el-button>
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: right"
      >
      </el-pagination>
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
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="科目编码" prop="subjectCode">
          <el-input v-model="form.subjectCode" placeholder="请输入科目编码"></el-input>
        </el-form-item>
        <el-form-item label="科目名称" prop="subjectName">
          <el-input v-model="form.subjectName" placeholder="请输入科目名称"></el-input>
        </el-form-item>
        <el-form-item label="科目级别" prop="subjectLevel">
          <el-select v-model="form.subjectLevel" placeholder="请选择科目级别">
            <el-option label="一级" value="1"></el-option>
            <el-option label="二级" value="2"></el-option>
            <el-option label="三级" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="父科目ID" prop="parentSubjectId">
          <el-input v-model="form.parentSubjectId" placeholder="无父科目则填0"></el-input>
        </el-form-item>
        <el-form-item label="科目类型" prop="subjectType">
          <el-select v-model="form.subjectType" placeholder="请选择科目类型">
            <el-option label="资产" value="资产"></el-option>
            <el-option label="负债" value="负债"></el-option>
            <el-option label="所有者权益" value="所有者权益"></el-option>
            <el-option label="成本" value="成本"></el-option>
            <el-option label="损益" value="损益"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="启用" value="启用"></el-option>
            <el-option label="停用" value="停用"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="停用原因" v-if="form.status === '停用'">
          <el-input 
            v-model="form.deactivationReason" 
            type="textarea" 
            placeholder="请输入停用原因"
            :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 状态切换弹窗 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="切换状态"
      width="400px"
    >
      <div v-if="currentRow.status === '启用'">
        <p>确定要停用【{{ currentRow.subjectName }}】吗？</p>
        <el-form-item label="停用原因" style="margin-top: 20px">
          <el-input 
            v-model="deactivationReason" 
            type="textarea" 
            placeholder="请输入停用原因"
            :rows="3"
          ></el-input>
        </el-form-item>
      </div>
      <div v-else>
        <p>确定要启用【{{ currentRow.subjectName }}】吗？</p>
      </div>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmChangeStatus"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import accountingSubjectApi from '@/api/accountingSubjectApi'

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const tableData = ref([])

// 查询条件
const searchForm = reactive({
  subjectName: '',
  status: '',
  subjectType: ''
})

// 弹窗相关
const dialogVisible = ref(false)
const statusDialogVisible = ref(false)
const dialogTitle = ref('新增科目')
const formRef = ref(null)
const currentRow = ref({})
const deactivationReason = ref('')

// 表单数据
const form = reactive({
  subjectId: '',
  subjectCode: '',
  subjectName: '',
  subjectLevel: '',
  parentSubjectId: 0,
  subjectType: '',
  status: '启用',
  deactivationReason: ''
})

// 表单校验规则
const rules = reactive({
  subjectCode: [{ required: true, message: '请输入科目编码', trigger: 'blur' }],
  subjectName: [{ required: true, message: '请输入科目名称', trigger: 'blur' }],
  subjectLevel: [{ required: true, message: '请选择科目级别', trigger: 'change' }],
  subjectType: [{ required: true, message: '请选择科目类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

// 初始化列表
onMounted(() => {
  getList()
})

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    const res = await accountingSubjectApi.pageList(
      currentPage.value,
      pageSize.value,
      searchForm
    )
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 重置查询条件（新增页码重置）
const resetSearch = () => {
  searchForm.subjectName = ''
  searchForm.status = ''
  searchForm.subjectType = ''
  currentPage.value = 1 // 重置页码到第一页
  getList()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  getList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  getList()
}

// 新增
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增科目'
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  resetForm()
  dialogTitle.value = '编辑科目'
  dialogVisible.value = true
  // 查询详情
  const res = await accountingSubjectApi.getById(row.subjectId)
  Object.assign(form, res)
}

// 切换状态
const handleChangeStatus = (row) => {
  currentRow.value = row
  deactivationReason.value = ''
  statusDialogVisible.value = true
}

// 确认切换状态
const confirmChangeStatus = async () => {
  try {
    const newStatus = currentRow.value.status === '启用' ? '停用' : '启用'
    await accountingSubjectApi.changeStatus(
      currentRow.value.subjectId,
      newStatus,
      newStatus === '停用' ? deactivationReason.value : ''
    )
    ElMessage.success('状态切换成功')
    statusDialogVisible.value = false
    getList()
  } catch (error) {
    ElMessage.error('状态切换失败：' + error.message)
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    let res
    if (form.subjectId) {
      // 修改
      res = await accountingSubjectApi.update(form)
    } else {
      // 新增
      res = await accountingSubjectApi.save(form)
    }
    if (res.success) {
      ElMessage.success(res.message)
      dialogVisible.value = false
      getList()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('表单校验失败：' + error.message)
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.subjectId = ''
  form.subjectCode = ''
  form.subjectName = ''
  form.subjectLevel = ''
  form.parentSubjectId = 0
  form.subjectType = ''
  form.status = '启用'
  form.deactivationReason = ''
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

.operate-card {
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