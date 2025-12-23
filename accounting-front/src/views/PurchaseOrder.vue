<template>
  <div class="purchase-order-page">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline @submit.prevent="getList">
        <el-form-item label="订单编号">
          <el-input
            v-model="queryParams.orderNo"
            placeholder="请输入订单编号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="供应商ID">
          <el-input
            v-model="queryParams.supplierId"
            placeholder="请输入供应商ID"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 200px"
          >
            <el-option label="待审核" value="待审核" />
            <el-option label="已审核" value="已审核" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联项目">
          <el-select
            v-model="queryParams.projectId"
            placeholder="请选择项目"
            clearable
            style="width: 200px"
          >
            <el-option 
              v-for="project in projectList" 
              :key="project.projectId" 
              :label="project.projectName" 
              :value="project.projectId" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="getList">查询</el-button>
          <el-button class="btn-reset" @click="resetQuery">重置</el-button>
          <el-button type="success" class="btn-add" @click="openDialog">新增采购订单</el-button>
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
        <el-table-column label="订单ID" prop="id" width="80" align="center" />
        <el-table-column label="订单编号" prop="order_no" min-width="180" />
        <el-table-column label="供应商ID" prop="supplier_id" width="100" align="center" />
        <el-table-column label="供应商名称" prop="supplier_name" min-width="150" />
        <el-table-column label="业务类型" prop="biz_sub_type" width="120" />
        <el-table-column label="关联项目" prop="project_name" width="150">
          <template #default="scope">
            {{ scope.row.project_name || '未关联' }}
          </template>
        </el-table-column>
        <el-table-column label="订单金额" prop="total_amount" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.total_amount) }}
          </template>
        </el-table-column>
        <el-table-column label="订单状态" prop="status" width="120">
          <template #default="scope">
            <el-tag
              :type="getStatusTagType(scope.row.status)"
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="270" align="center" fixed="right">
          <template #default="scope">
            <div class="operation-group">
              <el-button
                type="primary"
                size="small"
                class="btn-op btn-edit"
                icon="el-icon-edit"
                @click="handleEdit(scope.row)"
                :disabled="scope.row.status === '已审核'"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                class="btn-op btn-delete"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
                :disabled="scope.row.status === '已审核'"
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
              <el-button
                type="success"
                size="small"
                class="btn-op btn-audit"
                @click="handleChangeStatus(scope.row)"
                v-if="scope.row.status === '待审核'"
              >
                审核
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
      width="1000px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="订单编号" prop="order_no" v-if="isEdit">
          <el-input v-model="formData.order_no" disabled />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier_id">
          <el-select 
            v-model="formData.supplier_id" 
            placeholder="请选择供应商"
            @change="handleSupplierChange"
          >
            <el-option 
              v-for="supplier in supplierList" 
              :key="supplier.id" 
              :label="supplier.supplierName" 
              :value="supplier.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型" prop="biz_sub_type">
          <el-select v-model="formData.biz_sub_type" placeholder="请选择业务类型">
            <el-option label="材料采购" value="材料采购" />
            <el-option label="服务采购" value="服务采购" />
            <el-option label="固定资产采购" value="固定资产采购" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联项目" prop="project_id">
          <el-select 
            v-model="formData.project_id" 
            placeholder="请选择项目（可选）"
            clearable
            @change="handleProjectChange"
          >
            <el-option 
              v-for="project in projectList" 
              :key="project.projectId" 
              :label="project.projectName" 
              :value="project.projectId" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="订单金额" prop="total_amount">
          <el-input v-model="formData.total_amount" type="number" placeholder="请输入订单金额" />
        </el-form-item>
        
        <!-- 订单明细 -->
        <el-form-item label="订单明细">
          <el-table
            :data="orderLines"
            border
            stripe
            style="width: 100%"
          >
            <el-table-column label="物料编码" prop="material_code">
              <template #default="scope">
                <el-input v-model="scope.row.material_code" placeholder="请输入物料编码" />
              </template>
            </el-table-column>
            <el-table-column label="物料名称" prop="material_name">
              <template #default="scope">
                <el-input v-model="scope.row.material_name" placeholder="请输入物料名称" />
              </template>
            </el-table-column>
            <el-table-column label="规格" prop="spec">
              <template #default="scope">
                <el-input v-model="scope.row.spec" placeholder="请输入规格" />
              </template>
            </el-table-column>
            <el-table-column label="单位" prop="unit">
              <template #default="scope">
                <el-input v-model="scope.row.unit" placeholder="请输入单位" />
              </template>
            </el-table-column>
            <el-table-column label="数量" prop="quantity">
              <template #default="scope">
                <el-input 
                  v-model="scope.row.quantity" 
                  type="number" 
                  placeholder="请输入数量"
                  @change="calculateLineAmount(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="单价" prop="price">
              <template #default="scope">
                <el-input 
                  v-model="scope.row.price" 
                  type="number" 
                  placeholder="请输入单价"
                  @change="calculateLineAmount(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="金额" prop="amount" align="right">
              <template #default="scope">
                {{ scope.row.amount || 0 }}
              </template>
            </el-table-column>
            <el-table-column label="税率" prop="tax_rate">
              <template #default="scope">
                <el-input 
                  v-model="scope.row.tax_rate" 
                  type="number" 
                  placeholder="请输入税率"
                  @change="calculateLineAmount(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="税额" prop="tax_amount" align="right">
              <template #default="scope">
                {{ scope.row.tax_amount || 0 }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  icon="el-icon-delete"
                  @click="removeOrderLine(scope.$index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" size="small" @click="addOrderLine" style="margin-top: 10px">
            新增明细
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="采购订单详情"
      width="1000px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ orderDetail.order_no }}</el-descriptions-item>
        <el-descriptions-item label="供应商ID">{{ orderDetail.supplier_id }}</el-descriptions-item>
        <el-descriptions-item label="供应商名称">{{ orderDetail.supplier_name }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ orderDetail.biz_sub_type }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">{{ formatCurrency(orderDetail.total_amount) }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusTagType(orderDetail.status)">{{ orderDetail.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人ID">{{ orderDetail.create_user_id }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(orderDetail.create_time) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(orderDetail.update_time) }}</el-descriptions-item>
      </el-descriptions>
      
      <h4 style="margin-top: 20px">订单明细</h4>
      <el-table
        :data="orderDetailLines"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column label="物料编码" prop="material_code" />
        <el-table-column label="物料名称" prop="material_name" />
        <el-table-column label="规格" prop="spec" />
        <el-table-column label="单位" prop="unit" />
        <el-table-column label="数量" prop="quantity" align="right" />
        <el-table-column label="单价" prop="price" align="right" />
        <el-table-column label="金额" prop="amount" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="税率" prop="tax_rate" align="right" />
        <el-table-column label="税额" prop="tax_amount" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.tax_amount) }}
          </template>
        </el-table-column>
      </el-table>
      
      <h4 style="margin-top: 20px">生成的分录</h4>
      <el-table
        :data="journalEntries"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column label="借方科目" prop="debit_name" />
        <el-table-column label="贷方科目" prop="credit_name" />
        <el-table-column label="金额" prop="amount" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="税额" prop="tax_amount" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.tax_amount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status === '有效' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" />
      </el-table>
      
      <h4 style="margin-top: 20px">生成的应付单</h4>
      <el-table
        :data="apPayables"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column label="应付单号" prop="payable_no" />
        <el-table-column label="供应商名称" prop="supplier_name" />
        <el-table-column label="应付金额" prop="total_amount" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.total_amount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status === '未付款' ? 'warning' : 'success'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="create_time">
          <template #default="scope">
            {{ formatDate(scope.row.create_time) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 引入API
import {
  addPurchaseOrder,
  getPurchaseOrderList,
  getPurchaseOrderDetail,
  updatePurchaseOrder,
  deletePurchaseOrder,
  batchUpdatePurchaseOrderStatus,
  updatePurchaseOrderStatus
} from '@/api/purchaseOrderApi'
import { getPurchaseOrderLines } from '@/api/purchaseOrderApi'
import { getJournalEntriesBySource } from '@/api/journalEntryApi'
import { getApPayableByOrderId } from '@/api/apPayableApi'
// 引入供应商API
import { getSupplierList } from '@/api/supplier'
import { getAllProjects } from '@/api/project'

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

// 根据状态获取标签类型
const getStatusTagType = (status) => {
  const statusMap = {
    '待审核': 'warning',
    '已审核': 'success'
  }
  return statusMap[status] || 'default'
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
const dialogTitle = ref('新增采购订单')
// 是否为编辑模式
const isEdit = ref(false)
// 表单引用
const formRef = ref(null)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  supplierId: '',
  status: '',
  projectId: null
})

// 表单数据
  const formData = reactive({
    id: null,
    order_no: '',
    supplier_id: '',
    supplier_name: '',
    biz_sub_type: '材料采购',
    total_amount: 0,
    status: '待审核',
    project_id: null,
    project_name: ''
  })

// 订单明细
const orderLines = ref([])

// 订单详情
const orderDetail = reactive({
  id: null,
  order_no: '',
  supplier_id: '',
  supplier_name: '',
  biz_sub_type: '',
  total_amount: 0,
  status: '',
  create_user_id: null,
  create_time: '',
  update_time: ''
})

// 订单明细详情
const orderDetailLines = ref([])

// 分录列表
const journalEntries = ref([])

// 应付单列表
const apPayables = ref([])

// 供应商列表
const supplierList = ref([])

// 项目列表
const projectList = ref([])

// 加载供应商列表（只加载启用状态的供应商，status=1表示启用）
const loadSuppliers = async () => {
  try {
    const res = await getSupplierList({ pageNum: 1, pageSize: 100, status: 1 }) // 只获取启用状态的供应商
    supplierList.value = res.data.records || []
  } catch (error) {
    ElMessage.error('获取供应商列表失败：' + (error.response?.data?.msg || error.message))
  }
}

// 加载项目列表（只加载进行中的项目）
const loadProjects = async () => {
  try {
    const res = await getAllProjects()
    projectList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取项目列表失败：' + (error.response?.data?.msg || error.message))
  }
}

// 处理项目选择变化
const handleProjectChange = (projectId) => {
  if (projectId) {
    const selectedProject = projectList.value.find(p => p.projectId === projectId)
    if (selectedProject) {
      formData.project_name = selectedProject.projectName
    }
  } else {
    formData.project_name = ''
  }
}

// 表单校验规则
const formRules = reactive({
  supplier_id: [
    { required: true, message: '请选择供应商', trigger: 'change' }
  ],
  biz_sub_type: [
    { required: true, message: '请选择业务类型', trigger: 'change' }
  ],
  total_amount: [
    { required: true, message: '请输入订单金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '订单金额必须大于等于0', trigger: 'blur' }
  ]
})

// 页面加载时查询列表
onMounted(() => {
  getList()
  loadSuppliers() // 加载供应商列表
  loadProjects() // 加载项目列表
})

// 获取采购订单列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getPurchaseOrderList(queryParams)
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
  queryParams.orderNo = ''
  queryParams.supplierId = ''
  queryParams.status = ''
  queryParams.projectId = null
  getList()
}

// 供应商选择变化处理
const handleSupplierChange = (supplierId) => {
  const selectedSupplier = supplierList.value.find(item => item.id === supplierId)
  if (selectedSupplier) {
    formData.supplier_name = selectedSupplier.supplierName
  } else {
    formData.supplier_name = ''
  }
}

// 打开新增弹窗
const openDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增采购订单'
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.id = null
  formData.order_no = ''
  formData.supplier_id = ''
  formData.supplier_name = ''
  formData.biz_sub_type = '材料采购'
  formData.total_amount = 0
  formData.status = '待审核'
  formData.project_id = null
  formData.project_name = ''
  orderLines.value = []
  addOrderLine() // 新增一条默认明细
}

// 新增订单明细
const addOrderLine = () => {
  const newLine = {
    id: null,
    order_id: formData.id,
    material_code: '',
    material_name: '',
    spec: '',
    unit: '',
    quantity: 0,
    price: 0,
    amount: 0,
    tax_rate: 0,
    tax_amount: 0
  }
  orderLines.value.push(newLine)
}

// 删除订单明细
const removeOrderLine = (index) => {
  orderLines.value.splice(index, 1)
  calculateTotalAmount() // 删除后重新计算总金额
}

// 计算行金额
const calculateLineAmount = (line) => {
  // 确保数值有效
  const quantity = parseFloat(line.quantity) || 0
  const price = parseFloat(line.price) || 0
  const taxRate = parseFloat(line.tax_rate) || 0
  
  // 计算金额 = 数量 * 单价
  line.amount = quantity * price
  
  // 计算税额 = 金额 * 税率 / 100
  line.tax_amount = line.amount * taxRate / 100
  
  // 重新计算总金额
  calculateTotalAmount()
}

// 计算订单总金额
const calculateTotalAmount = () => {
  let total = 0
  orderLines.value.forEach(line => {
    total += parseFloat(line.amount) || 0
  })
  formData.total_amount = total
}

// 编辑采购订单
const handleEdit = async (row) => {
  // 检查订单状态，已审核的订单不允许编辑
  if (row.status === '已审核') {
    ElMessage.warning('已审核的订单不允许编辑')
    return
  }
  
  isEdit.value = true
  dialogTitle.value = '编辑采购订单'
  resetForm()
  try {
    const res = await getPurchaseOrderDetail(row.id)
    const data = res.data
    
    // 再次检查订单状态（防止在获取详情期间状态发生变化）
    if (data.order.status === '已审核') {
      ElMessage.warning('该订单已审核，不允许编辑')
      return
    }
    
    // 设置订单主信息
    formData.id = data.order.id
    formData.order_no = data.order.order_no || ''
    formData.supplier_id = data.order.supplier_id || ''
    formData.supplier_name = data.order.supplier_name || ''
    formData.biz_sub_type = data.order.biz_sub_type || '普通采购'
    formData.total_amount = data.order.total_amount || 0
    formData.status = data.order.status || '草稿'
    formData.project_id = data.order.project_id || null
    formData.project_name = data.order.project_name || ''
    
    // 设置订单明细
    orderLines.value = data.lines || []
    // 重新计算总金额
    calculateTotalAmount()
    
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.response?.data?.msg || error.message))
  }
}

// 查看采购订单详情
const handleView = async (row) => {
  detailVisible.value = true
  loading.value = true
  try {
    // 获取订单详情
    const res = await getPurchaseOrderDetail(row.id)
    const data = res.data
    Object.assign(orderDetail, {
      id: data.order.id,
      order_no: data.order.order_no || '',
      supplier_id: data.order.supplier_id || '',
      supplier_name: data.order.supplier_name || '',
      biz_sub_type: data.order.biz_sub_type || '',
      total_amount: data.order.total_amount || 0,
      status: data.order.status || '',
      create_user_id: data.order.create_user_id || null,
      create_time: data.order.create_time || '',
      update_time: data.order.update_time || ''
    })
    
    // 设置订单明细
    orderDetailLines.value = data.lines || []
    
    // 获取分录
    const entriesRes = await getJournalEntriesBySource({ sourceType: 'purchase_order', sourceId: row.id })
    journalEntries.value = entriesRes.data || []
    
    // 获取应付单
    const payablesRes = await getApPayableByOrderId(row.id)
    apPayables.value = payablesRes.data || []
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
}

// 删除采购订单
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除采购订单【${row.order_no}】吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await deletePurchaseOrder(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 审核采购订单
const handleChangeStatus = async (row) => {
  // 审核按钮只应该将状态改为'已审核'
  const targetStatus = '已审核'
  try {
    await ElMessageBox.confirm(
      `确定将采购订单【${row.order_no}】审核吗？`,
      '确认操作',
      { type: 'warning' }
    )
    console.log('审核参数:', { id: row.id, status: targetStatus })
    const response = await updatePurchaseOrderStatus(row.id, targetStatus)
    console.log('审核响应:', response)
    ElMessage.success('审核成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    
    // 计算总金额
    let totalAmount = 0
    orderLines.value.forEach(line => {
      line.amount = line.quantity * line.price
      line.tax_amount = line.amount * line.tax_rate / 100
      totalAmount += line.amount + line.tax_amount
    })
    formData.total_amount = totalAmount
    
    // 准备提交数据
    const submitData = {
      order: {
        ...formData
      },
      lines: orderLines.value
    }
    
    console.log('提交数据:', submitData)
    
    // 提交数据
    if (isEdit.value) {
      // 编辑时再次检查订单状态
      if (formData.status === '已审核') {
        ElMessage.warning('该订单已审核，不允许编辑')
        return
      }
      const response = await updatePurchaseOrder(submitData)
      console.log('更新响应:', response)
      ElMessage.success('编辑成功')
    } else {
      await addPurchaseOrder(submitData)
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

.btn-view {
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  color: #0369a1;
  border: 1px solid #7dd3fc;
}

.btn-view:hover {
  background: linear-gradient(135deg, #bae6fd 0%, #7dd3fc 100%);
  color: #075985;
}

.btn-audit {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #15803d;
  border: 1px solid #86efac;
}

.btn-audit:hover {
  background: linear-gradient(135deg, #bbf7d0 0%, #86efac 100%);
  color: #166534;
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