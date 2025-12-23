<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="应付单号">
          <el-input v-model="searchForm.payableNo" placeholder="请输入应付单号"></el-input>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择">
            <el-option label="待确认" value="待确认"></el-option>
            <el-option label="已确认" value="已确认"></el-option>
            <el-option label="已核销" value="已核销"></el-option>
            <el-option label="已取消" value="已取消"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="handleSearch">查询</el-button>
          <el-button class="btn-reset" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮和未付款提示 -->
    <el-card shadow="never" class="operate-card">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <el-button type="primary" class="btn-add" @click="handleCreatePayment">创建付款单</el-button>
        </div>
        <div v-if="unpaidCount > 0" class="unpaid-tip">
          <el-alert
            :title="`共有 ${unpaidCount} 笔订单待付款`"
            type="warning"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </el-card>

    <!-- 应付单列表 -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="payableList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column label="应付单号" prop="payable_no" />
        <el-table-column label="供应商" prop="supplier_name" />
        <el-table-column label="应付金额" prop="total_amount" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.total_amount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag
              :type="
                scope.row.status === '待确认' ? 'warning' :
                scope.row.status === '已确认' ? 'info' :
                scope.row.status === '已核销' ? 'success' : 'danger'
              "
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="create_time" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.create_time) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <div class="operation-group">
              <el-button
                type="primary"
                size="small"
                class="btn-op btn-pay"
                @click="handleCreatePaymentForPayable(scope.row)"
                :disabled="scope.row.status === '已核销' || scope.row.status === '已取消'"
              >
                付款
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        :current-page="searchForm.pageNum"
        :page-size="searchForm.pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 付款单列表 -->
    <el-card shadow="never" class="table-card" style="margin-top: 20px;">
      <div class="card-header">
        <h3>付款单列表</h3>
      </div>
      <el-table
        v-loading="paymentLoading"
        :data="paymentList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column label="付款单号" prop="payment_no" />
        <el-table-column label="对应应付单" prop="payable_id" />
        <el-table-column label="供应商" prop="supplier_name" />
        <el-table-column label="付款金额" prop="payment_amount" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.payment_amount) }}
          </template>
        </el-table-column>
        <el-table-column label="付款日期" prop="payment_date" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.payment_date) }}
          </template>
        </el-table-column>
        <el-table-column label="付款方式" prop="payment_method" />
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag
              :type="
                scope.row.status === '待确认' ? 'warning' :
                scope.row.status === '已确认' ? 'success' : 'danger'
              "
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="text"
              size="small"
              @click="handleConfirmPayment(scope.row)"
              :disabled="scope.row.status !== '待确认'"
            >
              确认付款
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="handleCancelPayment(scope.row)"
              :disabled="scope.row.status !== '待确认'"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 付款单分页 -->
      <el-pagination
        v-if="paymentTotal > 0"
        :current-page="paymentSearchForm.pageNum"
        :page-size="paymentSearchForm.pageSize"
        :total="paymentTotal"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePaymentCurrentChange"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>

    <!-- 创建付款单弹窗 -->
    <el-dialog
      v-model="paymentDialogVisible"
      title="创建付款单"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="paymentForm" :rules="paymentRules" ref="paymentFormRef" label-width="100px">
        <el-form-item label="应付单" prop="payable_id">
          <el-select v-model="paymentForm.payable_id" placeholder="请选择应付单" @change="handlePayableChange" filterable>
            <el-option
              v-for="payable in payableOptions"
              :key="payable.id"
              :label="payable.payable_no + ' - ' + payable.supplier_name + ' (¥' + formatCurrency(payable.total_amount) + ')'"
              :value="payable.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="供应商" prop="supplier_name">
          <el-input v-model="paymentForm.supplier_name" disabled></el-input>
        </el-form-item>
        <el-form-item label="应付金额" prop="total_amount">
          <el-input v-model="paymentForm.total_amount" disabled></el-input>
        </el-form-item>
        <el-form-item label="付款金额" prop="payment_amount">
          <el-input
            v-model="paymentForm.payment_amount"
            type="number"
            placeholder="请输入付款金额"
            :min="0.01"
            :max="paymentForm.total_amount"
            step="0.01"
          ></el-input>
        </el-form-item>
        <el-form-item label="付款日期" prop="payment_date">
          <el-date-picker
            v-model="paymentForm.payment_date"
            type="date"
            placeholder="选择付款日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="付款方式" prop="payment_method">
          <el-select v-model="paymentForm.payment_method" placeholder="请选择付款方式">
            <el-option label="银行转账" value="银行转账"></el-option>
            <el-option label="现金" value="现金"></el-option>
            <el-option label="支票" value="支票"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitPayment">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { getApPayableList, getApPaymentList, createApPayment, confirmApPayment, cancelApPayment, countUnpaidPayables } from '@/api/apPaymentApi'

// 搜索表单
const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  payableNo: '',
  supplierName: '',
  status: ''
})

// 付款单搜索表单
const paymentSearchForm = reactive({
  pageNum: 1,
  pageSize: 10
})

// 表格数据
const payableList = ref([])
const paymentList = ref([])
const total = ref(0)
const paymentTotal = ref(0)
const loading = ref(false)
const paymentLoading = ref(false)
const unpaidCount = ref(0) // 未付款订单数量

// 创建付款单弹窗
const paymentDialogVisible = ref(false)
const paymentFormRef = ref()
const paymentForm = reactive({
  payable_id: null,
  supplier_id: null,
  supplier_name: '',
  total_amount: 0,
  payment_amount: 0,
  payment_date: new Date(),
  payment_method: '银行转账'
})

// 付款单选项
const payableOptions = ref([])

// 表单验证规则
const paymentRules = reactive({
  payable_id: [
    { required: true, message: '请选择应付单', trigger: 'change' }
  ],
  payment_amount: [
    { required: true, message: '请输入付款金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '付款金额必须大于0', trigger: 'blur' }
  ],
  payment_date: [
    { required: true, message: '请选择付款日期', trigger: 'change' }
  ],
  payment_method: [
    { required: true, message: '请选择付款方式', trigger: 'change' }
  ]
})

// 格式化金额
const formatCurrency = (value) => {
  if (!value) return '0.00'
  return Number(value).toFixed(2)
}

// 格式化日期
const formatDate = (value) => {
  if (!value) return ''
  const date = new Date(value)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 查询应付单列表
const getPayableList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: searchForm.pageNum,
      pageSize: searchForm.pageSize,
      payableNo: searchForm.payableNo,
      status: searchForm.status
    }
    const res = await getApPayableList(params)
    if (res.success) {
      payableList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error('查询失败：' + res.msg)
    }
  } catch (error) {
      ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 查询未付款订单数量
const getUnpaidCount = async () => {
  try {
    const res = await countUnpaidPayables()
    if (res.success) {
      unpaidCount.value = res.data || 0
    }
  } catch (error) {
    console.error('查询未付款订单数量失败：', error)
  }
}

// 查询付款单列表
const getPaymentList = async () => {
  paymentLoading.value = true
  try {
    const params = {
      pageNum: paymentSearchForm.pageNum,
      pageSize: paymentSearchForm.pageSize
    }
    const res = await getApPaymentList(params)
    if (res.success) {
      paymentList.value = res.data.records || []
      paymentTotal.value = res.data.total || 0
    } else {
      ElMessage.error('查询付款单失败：' + res.msg)
    }
  } catch (error) {
    ElMessage.error('查询付款单失败：' + error.message)
  } finally {
    paymentLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  getPayableList()
}

// 重置
const handleReset = () => {
  searchForm.payableNo = ''
  searchForm.supplierName = ''
  searchForm.status = ''
  searchForm.pageNum = 1
  getPayableList()
}

// 分页变化
const handleCurrentChange = (pageNum) => {
  searchForm.pageNum = pageNum
  getPayableList()
}

// 付款单分页变化
const handlePaymentCurrentChange = (pageNum) => {
  paymentSearchForm.pageNum = pageNum
  getPaymentList()
}


// 打开创建付款单弹窗
const handleCreatePayment = async () => {
  resetPaymentForm()
  // 加载所有可用的应付单（排除已核销和已取消的）
  await loadAvailablePayables()
  paymentDialogVisible.value = true
}

// 加载可用的应付单选项
const loadAvailablePayables = async () => {
  try {
    const params = {
      pageNum: 1,
      pageSize: 1000, // 获取足够多的数据
      status: '' // 不限制状态，但会在前端过滤
    }
    const res = await getApPayableList(params)
    if (res.success) {
      // 过滤掉已核销和已取消的应付单
      payableOptions.value = (res.data.records || []).filter(
        payable => payable.status !== '已核销' && payable.status !== '已取消'
      )
    }
  } catch (error) {
    console.error('加载应付单选项失败：', error)
    payableOptions.value = []
  }
}

// 当选择应付单时，自动填充相关信息
const handlePayableChange = (payableId) => {
  const selectedPayable = payableOptions.value.find(p => p.id === payableId)
  if (selectedPayable) {
    paymentForm.supplier_id = selectedPayable.supplier_id
    paymentForm.supplier_name = selectedPayable.supplier_name
    paymentForm.total_amount = selectedPayable.total_amount
    paymentForm.payment_amount = selectedPayable.total_amount // 默认付款金额等于应付金额
  }
}

// 为特定应付单创建付款单
const handleCreatePaymentForPayable = async (payable) => {
  resetPaymentForm()
  // 加载可用的应付单选项（确保当前应付单在列表中）
  await loadAvailablePayables()
  // 设置默认值
  paymentForm.payable_id = payable.id
  paymentForm.supplier_id = payable.supplier_id
  paymentForm.supplier_name = payable.supplier_name
  paymentForm.total_amount = payable.total_amount
  paymentForm.payment_amount = payable.total_amount
  paymentDialogVisible.value = true
}

// 重置付款单表单
const resetPaymentForm = () => {
  paymentForm.payable_id = null
  paymentForm.supplier_id = null
  paymentForm.supplier_name = ''
  paymentForm.total_amount = 0
  paymentForm.payment_amount = 0
  paymentForm.payment_date = new Date()
  paymentForm.payment_method = '银行转账'
  if (paymentFormRef.value) {
    paymentFormRef.value.resetFields()
  }
}

// 提交付款单
const handleSubmitPayment = async () => {
  if (!paymentFormRef.value) return
  
  // 先验证表单
  const valid = await new Promise(resolve => {
    paymentFormRef.value.validate(valid => resolve(valid))
  })
  
  if (!valid) return
  
  let loadingInstance
  try {
    loadingInstance = ElLoading.service({ text: '提交中...' })
    // 构建与后端ApPayment实体类完全匹配的数据结构
    const formData = {
      payable_id: paymentForm.payable_id,
      supplier_id: paymentForm.supplier_id,
      supplier_name: paymentForm.supplier_name,
      payment_amount: paymentForm.payment_amount, // 不转换为Number，保持原始值让axios自动处理类型转换
      payment_date: typeof paymentForm.payment_date === 'string' ? paymentForm.payment_date + ' 00:00:00' : paymentForm.payment_date.toISOString().split('T')[0] + ' 00:00:00',
      payment_method: paymentForm.payment_method,
      create_user_id: 1 // 假设当前用户ID为1，可以从本地存储或其他方式获取
    }
    console.log('提交的付款单数据:', formData) // 调试信息
    const res = await createApPayment(formData)
    
    // 直接关闭loading
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
    
    if (res && res.success) {
      ElMessage.success('创建付款单成功')
      paymentDialogVisible.value = false
      getPaymentList() // 刷新付款单列表
    } else {
      ElMessage.error('创建失败：' + (res?.msg || '未知错误'))
    }
  } catch (error) {
    // 确保在错误情况下也关闭loading
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
    ElMessage.error('创建失败：' + error.message)
    console.error('创建付款单异常:', error) // 调试信息
  }
}

// 确认付款
const handleConfirmPayment = async (payment) => {
  try {
    const res = await confirmApPayment(payment.id, 1) // 假设当前用户ID为1
    if (res.success) {
      ElMessage.success('确认付款成功')
      getPaymentList() // 刷新付款单列表
      getPayableList() // 刷新应付单列表
      getUnpaidCount() // 刷新未付款订单数量
    } else {
      ElMessage.error('确认失败：' + res.msg)
    }
  } catch (error) {
    ElMessage.error('确认失败：' + error.message)
  }
}

// 取消付款
const handleCancelPayment = async (payment) => {
  try {
    const res = await cancelApPayment(payment.id)
    if (res.success) {
      ElMessage.success('取消付款成功')
      getPaymentList() // 刷新付款单列表
    } else {
      ElMessage.error('取消失败：' + res.msg)
    }
  } catch (error) {
    ElMessage.error('取消失败：' + error.message)
  }
}

// 页面加载时获取数据
onMounted(() => {
  getPayableList()
  getPaymentList()
  getUnpaidCount() // 查询未付款订单数量
})
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

.unpaid-tip {
  flex: 1;
  margin-left: 20px;
  max-width: 400px;
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

.btn-pay {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
  border: 1px solid #93c5fd;
}

.btn-pay:hover {
  background: linear-gradient(135deg, #bfdbfe 0%, #93c5fd 100%);
  color: #1e3a8a;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}
</style>