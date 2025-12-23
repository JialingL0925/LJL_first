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
            <el-option label="待过账" value="待过账" />
            <el-option label="已生效" value="已生效" />
            <el-option label="已过账" value="已过账" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="getList">查询</el-button>
          <el-button class="btn-reset" @click="resetQuery">重置</el-button>
          <el-button type="success" class="btn-batch-post" @click="handleBatchPost" :disabled="selectedRows.length === 0">
            批量过账 ({{ selectedRows.length }})
          </el-button>
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
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" :selectable="checkSelectable" />
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
            <el-tag :type="getStatusType(scope.row.status)">
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
        <el-table-column label="过账时间" prop="post_time" width="180" v-if="showPostTime">
          <template #default="scope">
            {{ formatDate(scope.row.post_time) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <div class="operation-group">
              <el-button
                type="success"
                size="small"
                class="btn-op btn-post"
                @click="handlePost(scope.row)"
                v-if="canPost(scope.row)"
              >
                过账
              </el-button>
              <el-button
                type="info"
                size="small"
                class="btn-op btn-view"
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
          <el-tag :type="getStatusType(entryDetail.status)">
            {{ entryDetail.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ entryDetail.remark }}</el-descriptions-item>
        <el-descriptions-item label="创建人ID">{{ entryDetail.create_user_id }}</el-descriptions-item>
        <el-descriptions-item label="过账人ID" v-if="entryDetail.post_user_id">{{ entryDetail.post_user_id }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(entryDetail.create_time) }}</el-descriptions-item>
        <el-descriptions-item label="过账时间" v-if="entryDetail.post_time">{{ formatDate(entryDetail.post_time) }}</el-descriptions-item>
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
  getJournalEntryList,
  getJournalEntryDetail,
  postJournalEntry,
  batchPostJournalEntries
} from '@/api/journalEntryApi'

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

// 获取状态标签类型
const getStatusType = (status) => {
  const statusMap = {
    '已过账': 'success',
    '待过账': 'warning',
    '已生效': 'info',
    '有效': 'success',
    '无效': 'danger',
    '已作废': 'danger'
  }
  return statusMap[status] || 'info'
}

// 判断是否可以过账
const canPost = (row) => {
  return row.status !== '已过账' && row.status !== '已作废' && row.status !== '无效'
}

// 判断是否可以选择（用于批量过账）
const checkSelectable = (row) => {
  return canPost(row)
}

// 加载状态
const loading = ref(false)
// 表格数据
const tableData = ref([])
// 总条数
const total = ref(0)
// 选中的行
const selectedRows = ref([])
// 详情弹窗显示状态
const detailVisible = ref(false)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  source_type: '',
  status: ''
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
  update_time: '',
  post_user_id: null,
  post_time: ''
})

// 是否显示过账时间列
const showPostTime = computed(() => {
  return queryParams.status === '已过账' || tableData.value.some(row => row.post_time)
})

// 页面加载时查询列表
onMounted(() => {
  getList()
})

// 获取分录列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getJournalEntryList(queryParams)
    console.log('📊 后端返回的完整数据:', res)
    console.log('📊 查询参数:', queryParams)
    if (res.data && res.data.records && res.data.records.length > 0) {
      console.log('📊 第一条数据:', res.data.records[0])
      console.log('📊 第一条数据的状态:', res.data.records[0].status)
    }
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    console.log('📊 表格数据已更新，共', tableData.value.length, '条')
  } catch (error) {
    console.error('❌ 获取列表失败:', error)
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
      update_time: data.update_time || '',
      post_user_id: data.post_user_id || null,
      post_time: data.post_time || ''
    })
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
}

// 过账单个分录
const handlePost = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要过账分录 ID ${row.id} 吗？过账后将无法修改或删除。`,
      '确认过账',
      { type: 'warning' }
    )
    await postJournalEntry(row.id)
    ElMessage.success('过账成功')
    getList()
    // 清除选中项
    selectedRows.value = selectedRows.value.filter(item => item.id !== row.id)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('过账失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 批量过账
const handleBatchPost = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要过账的分录')
    return
  }

  // 过滤出可以过账的分录
  const canPostRows = selectedRows.value.filter(row => canPost(row))

  if (canPostRows.length === 0) {
    ElMessage.warning('选中的分录中没有可以过账的记录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要批量过账 ${canPostRows.length} 条分录吗？过账后将无法修改或删除。`,
      '确认批量过账',
      { type: 'warning' }
    )
    
    const ids = canPostRows.map(row => row.id)
    await batchPostJournalEntries(ids)
    ElMessage.success(`成功过账 ${canPostRows.length} 条分录`)
    selectedRows.value = []
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量过账失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
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

.btn-batch-post {
  padding: 10px 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
  transition: all 0.3s ease;
  font-weight: 500;
  color: #fff;
}

.btn-batch-post:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.4);
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.btn-batch-post:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 统一表格操作按钮 */
.operation-group {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
  width: 100%;
  min-width: 160px;
  padding: 0 8px;
  box-sizing: border-box;
}

.btn-op {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
  transition: all 0.2s ease;
  border: none;
  font-weight: 500;
  min-width: 60px;
  height: 32px;
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

.btn-post {
  background: linear-gradient(135deg, #86efac 0%, #4ade80 100%);
  color: #166534;
  border: 1px solid #86efac;
}

.btn-post:hover {
  background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
  color: #14532d;
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
  min-width: 60px !important;
  height: 32px !important;
  padding: 6px 12px !important;
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
  padding: 12px 8px !important;
}

:deep(.el-table__fixed-right) {
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
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

