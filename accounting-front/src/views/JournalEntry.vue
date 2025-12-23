<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline @submit.prevent="getList">
        <el-form-item label="来源类型">
          <el-select
            v-model="queryParams.source_type"
            placeholder="请选择来源类型"
            clearable
            style="width: 200px"
          >
            <el-option label="采购订单" value="purchase_order" />
            <el-option label="销售订单" value="sales_order" />
            <el-option label="手动录入" value="manual" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 200px"
          >
            <el-option label="有效" value="有效" />
            <el-option label="无效" value="无效" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="getList">查询</el-button>
          <el-button class="btn-reset" @click="resetQuery">重置</el-button>
          <el-button type="success" class="btn-add" @click="openDialog">手动录分录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column label="分录ID" prop="id" width="80" align="center" />
        <el-table-column label="来源类型" prop="source_type" width="120">
          <template #default="scope">
            {{ getSourceTypeName(scope.row.source_type) }}
          </template>
        </el-table-column>
        <el-table-column label="来源ID" prop="source_id" width="100" align="center" />
        <el-table-column label="借方科目" prop="debit_name" min-width="150" />
        <el-table-column label="贷方科目" prop="credit_name" min-width="150" />
        <el-table-column label="金额" prop="amount" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="税额" prop="tax_amount" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.tax_amount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '有效' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="200" />
        <el-table-column label="创建人ID" prop="create_user_id" width="100" align="center" />
        <el-table-column label="创建时间" prop="create_time" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.create_time) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="update_time" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.update_time) }}
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
                v-if="scope.row.source_type === 'manual'"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                class="btn-op btn-delete"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
                v-if="scope.row.source_type === 'manual'"
              >
                删除
              </el-button>
              <el-button
                type="info"
                size="small"
                class="btn-op btn-view"
                icon="el-icon-view"
                @click="handleView(scope.row)"
              >
                查看
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
        <el-form-item label="来源类型" prop="source_type">
          <el-select v-model="formData.source_type" placeholder="请选择来源类型">
            <el-option label="手动录入" value="manual" />
          </el-select>
        </el-form-item>
        <el-form-item label="借方科目" prop="debit_code">
          <el-select v-model="formData.debit_code" placeholder="请选择或输入借方科目" clearable style="width: 100%" @change="handleDebitSubjectChange" filterable>
            <el-option v-for="subject in allSubjects" :key="subject.subjectId" :label="`${subject.subjectCode} - ${subject.subjectName}`" :value="subject.subjectCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="贷方科目" prop="credit_code">
          <el-select v-model="formData.credit_code" placeholder="请选择或输入贷方科目" clearable style="width: 100%" @change="handleCreditSubjectChange" filterable>
            <el-option v-for="subject in allSubjects" :key="subject.subjectId" :label="`${subject.subjectCode} - ${subject.subjectName}`" :value="subject.subjectCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input v-model.number="formData.amount" type="number" placeholder="请输入金额" />
        </el-form-item>
        <el-form-item label="税额" prop="tax_amount">
          <el-input v-model.number="formData.tax_amount" type="number" placeholder="请输入税额" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="有效" value="有效" />
            <el-option label="无效" value="无效" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
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

    <!-- 分录详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="分录详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="分录ID">{{ entryDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="来源类型">{{ getSourceTypeName(entryDetail.source_type) }}</el-descriptions-item>
        <el-descriptions-item label="来源ID">{{ entryDetail.source_id }}</el-descriptions-item>
        <el-descriptions-item label="借方科目编码">{{ entryDetail.debit_code }}</el-descriptions-item>
        <el-descriptions-item label="借方科目名称">{{ entryDetail.debit_name }}</el-descriptions-item>
        <el-descriptions-item label="贷方科目编码">{{ entryDetail.credit_code }}</el-descriptions-item>
        <el-descriptions-item label="贷方科目名称">{{ entryDetail.credit_name }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ formatCurrency(entryDetail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="税额">{{ formatCurrency(entryDetail.tax_amount) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="entryDetail.status === '有效' ? 'success' : 'danger'">
            {{ entryDetail.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ entryDetail.remark }}</el-descriptions-item>
        <el-descriptions-item label="创建人ID">{{ entryDetail.create_user_id }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(entryDetail.create_time) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(entryDetail.update_time) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 引入API
import {
  addJournalEntry,
  getJournalEntryList,
  getJournalEntryDetail,
  updateJournalEntry,
  deleteJournalEntry
} from '@/api/journalEntryApi'
// 引入科目API
import accountingSubjectApi from '@/api/accountingSubjectApi'

// 日期格式化工具
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr.replace(/-/g, '/'))
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

// 金额格式化工具
const formatCurrency = (amount) => {
  if (!amount) return '¥0.00'
  return '¥' + Number(amount).toFixed(2)
}

// 获取来源类型名称
const getSourceTypeName = (sourceType) => {
  const sourceTypeMap = {
    'purchase_order': '采购订单',
    'sales_order': '销售订单',
    'manual': '手动录入'
  }
  return sourceTypeMap[sourceType] || sourceType
}

// 加载状态
const loading = ref(false)
// 表格数据
const tableData = ref([])
// 总条数
const total = ref(0)
// 弹窗显示状态
const dialogVisible = ref(false)
// 详情弹窗显示状态
const detailVisible = ref(false)
// 弹窗标题
const dialogTitle = ref('手动录分录')
// 是否为编辑模式
const isEdit = ref(false)
// 表单引用
const formRef = ref(null)
// 科目相关变量
const subjectList = ref([]) // 树形科目列表
const allSubjects = ref([]) // 扁平科目列表
// 搜索相关变量
const debitLoading = ref(false)
const creditLoading = ref(false)
const filteredDebitSubjects = ref([])
const filteredCreditSubjects = ref([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  source_type: '',
  status: ''
})

// 表单数据
const formData = reactive({
  id: null,
  source_type: 'manual',
  source_id: null,
  debit_code: '',
  debit_name: '',
  credit_code: '',
  credit_name: '',
  amount: 0,
  tax_amount: 0,
  status: '有效',
  remark: ''
})

// 分录详情
const entryDetail = reactive({
  id: null,
  source_type: '',
  source_id: null,
  debit_code: '',
  debit_name: '',
  credit_code: '',
  credit_name: '',
  amount: 0,
  tax_amount: 0,
  status: '',
  remark: '',
  create_user_id: null,
  create_time: '',
  update_time: ''
})

// 表单校验规则
const formRules = reactive({
  source_type: [
    { required: true, message: '请选择来源类型', trigger: 'change' }
  ],
  debit_code: [
    { required: true, message: '请选择借方科目', trigger: 'change' }
  ],
  credit_code: [
    { required: true, message: '请选择贷方科目', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

// 获取所有科目列表
const getAllSubjects = async () => {
  try {
    // 递归获取所有科目
    const fetchAllSubjects = async (parentId = 0) => {
      const subjects = await accountingSubjectApi.treeList(parentId)
      
      for (const subject of subjects) {
        // 只处理有编码的科目
        if (subject.subjectCode) {
          allSubjects.value.push(subject)
        }
        
        // 递归获取子科目
        if (subject.subjectId) {
          await fetchAllSubjects(subject.subjectId)
        }
      }
    }
    
    // 清空之前的数据
    allSubjects.value = []
    
    // 开始递归获取所有科目
    await fetchAllSubjects(0)
    
    // 调试信息
    console.log('科目列表获取成功，共', allSubjects.value.length, '个科目')
    console.log('科目列表数据:', allSubjects.value)
    
  } catch (error) {
    console.error('获取科目列表详细错误：', error)
    ElMessage.error('获取科目列表失败：' + (error.response?.data?.msg || error.message))
  }
}

// 处理借方科目选择
const handleDebitSubjectChange = (code) => {
  if (!code) {
    formData.debit_name = ''
    return
  }
  // 查找对应的科目名称
  const subject = allSubjects.value.find(s => s.subjectCode === code)
  if (subject) {
    formData.debit_name = subject.subjectName
  }
}

// 处理贷方科目选择
const handleCreditSubjectChange = (code) => {
  if (!code) {
    formData.credit_name = ''
    return
  }
  // 查找对应的科目名称
  const subject = allSubjects.value.find(s => s.subjectCode === code)
  if (subject) {
    formData.credit_name = subject.subjectName
  }
}

// 页面加载时查询列表和科目列表
onMounted(() => {
  getList()
  getAllSubjects()
})

// 获取分录列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getJournalEntryList(queryParams)
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
  queryParams.source_type = ''
  queryParams.status = ''
  getList()
}

// 打开新增弹窗
const openDialog = () => {
  isEdit.value = false
  dialogTitle.value = '手动录分录'
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.id = null
  formData.source_type = 'MANUAL'
  formData.source_id = null
  formData.debit_code = ''
  formData.debit_name = ''
  formData.credit_code = ''
  formData.credit_name = ''
  formData.amount = 0
  formData.tax_amount = 0
  formData.status = '有效'
  formData.remark = ''
}

// 编辑分录
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑分录'
  resetForm()
  try {
    const res = await getJournalEntryDetail(row.id)
    const data = res.data
    formData.id = data.id
    formData.source_type = data.source_type || 'manual'
    formData.source_id = data.source_id || null
    formData.debit_code = data.debit_code || ''
    formData.debit_name = data.debit_name || ''
    formData.credit_code = data.credit_code || ''
    formData.credit_name = data.credit_name || ''
    formData.amount = data.amount || 0
    formData.tax_amount = data.tax_amount || 0
    formData.status = data.status || '有效'
    formData.remark = data.remark || ''
    
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.response?.data?.msg || error.message))
  }
}

// 查看分录详情
const handleView = async (row) => {
  detailVisible.value = true
  loading.value = true
  try {
    const res = await getJournalEntryDetail(row.id)
    const data = res.data
    Object.assign(entryDetail, {
      id: data.id,
      source_type: data.source_type || '',
      source_id: data.source_id || null,
      debit_code: data.debit_code || '',
      debit_name: data.debit_name || '',
      credit_code: data.credit_code || '',
      credit_name: data.credit_name || '',
      amount: data.amount || 0,
      tax_amount: data.tax_amount || 0,
      status: data.status || '',
      remark: data.remark || '',
      create_user_id: data.create_user_id || null,
      create_time: data.create_time || '',
      update_time: data.update_time || ''
    })
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
}

// 删除分录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除分录吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteJournalEntry(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  try {
    // 确保金额和税额是数字类型
    formData.amount = Number(formData.amount)
    formData.tax_amount = Number(formData.tax_amount)
    
    // 先进行表单验证
    await formRef.value.validate()
    
    // 验证通过后再提交数据
    if (isEdit.value) {
      await updateJournalEntry(formData)
      ElMessage.success('编辑成功')
    } else {
      await addJournalEntry(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    // 如果是表单验证失败，错误对象会包含validateFields: true
    if (error && error.validateFields) {
      ElMessage.warning('表单验证失败，请检查输入内容')
    } else {
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

.btn-delete {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
  border: 1px solid #fca5a5;
}

.btn-delete:hover {
  background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
  color: #b91c1c;
}

.btn-view {
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  color: #0369a1;
  border: 1px solid #7dd3fc;
}

.btn-view:hover {
  background: linear-gradient(135deg, #bae6fd 0%, #7dd3fc 100%);
  color: #075985;
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