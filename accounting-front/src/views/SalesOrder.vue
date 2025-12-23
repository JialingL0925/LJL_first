<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="订单类型">
          <el-select v-model="searchForm.orderType" placeholder="请选择订单类型" clearable>
            <el-option label="产品销售" value="产品销售" />
            <el-option label="服务销售" value="服务销售" />
            <el-option label="固定资产销售" value="固定资产销售" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审核" value="DRAFT" />
            <el-option label="已审核" value="APPROVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="handleSearch">查询</el-button>
          <el-button class="btn-reset" @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-add" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
      <div class="table-header">
          <div class="table-title">
            <span class="title-main">销售订单列表</span>
            <span class="title-sub">共 <span class="title-number">{{ total }}</span> 条记录</span>
      </div>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="salesOrderList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="customerName" label="客户名称" width="120" />
        <el-table-column prop="orderDate" label="订单日期" width="150" />
        <el-table-column prop="orderType" label="订单类型" width="120" />
        <el-table-column prop="totalAmount" label="订单金额" width="150" :formatter="formatAmount" />
        <el-table-column prop="status" label="状态" width="120" :formatter="formatStatus" />
        <el-table-column prop="createBy" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="210" fixed="right" align="center">
          <template #default="scope">
            <div class="operation-group">
              <el-button size="small" class="btn-op btn-view" @click="handleView(scope.row.id)">详情</el-button>
              <el-button 
                size="small" 
                class="btn-op btn-edit"
                @click="handleEdit(scope.row)"
                :disabled="scope.row.status === 'APPROVED'"
              >
                编辑
              </el-button>
              <el-button v-if="scope.row.status === 'DRAFT'" size="small" class="btn-op btn-audit" @click="handleAudit(scope.row.id)">审核</el-button>
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
        />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="80%"
      :before-close="handleCloseDialog"
    >
      <el-form :model="salesOrderForm" ref="salesOrderForm" label-width="120px">
        <!-- 订单主信息 -->
        <el-card shadow="never" class="main-info-card">
          <template #header>
            <div class="card-header">
              <span>订单主信息</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="订单编号" prop="orderNo">
                <el-input v-model="salesOrderForm.orderNo" placeholder="系统自动生成" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="客户名称" prop="customerId">
                <el-select v-model="salesOrderForm.customerId" placeholder="请选择客户名称" @change="handleCustomerChange">
                  <el-option
                    v-for="customer in customerList"
                    :key="customer.customerId"
                    :label="customer.customerName"
                    :value="customer.customerId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="订单日期" prop="orderDate">
                <el-date-picker
                  v-model="salesOrderForm.orderDate"
                  type="date"
                  placeholder="选择订单日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="订单类型" prop="orderType">
                 <el-select v-model="salesOrderForm.orderType" placeholder="请选择订单类型">
                   <el-option label="产品销售" value="产品销售" />
                   <el-option label="服务销售" value="服务销售" />
                   <el-option label="固定资产销售" value="固定资产销售" />
                 </el-select>
               </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注" prop="remark">
                <el-input v-model="salesOrderForm.remark" type="textarea" placeholder="请输入备注" :rows="3" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 订单明细 -->
        <el-card shadow="never" class="detail-info-card">
          <template #header>
            <div class="card-header">
              <span>订单明细</span>
              <el-button type="primary" size="small" @click="handleAddDetail">新增明细</el-button>
            </div>
          </template>
          <el-table :data="salesOrderForm.details" stripe border>
            <el-table-column prop="productId" label="产品ID" width="150">
              <template #default="scope">
                <el-input v-model="scope.row.productId" placeholder="请输入产品ID" />
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="产品名称" width="200">
              <template #default="scope">
                <el-input v-model="scope.row.productName" placeholder="请输入产品名称" />
              </template>
            </el-table-column>
            <el-table-column prop="spec" label="规格" width="150">
              <template #default="scope">
                <el-input v-model="scope.row.spec" placeholder="请输入规格" />
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="120">
              <template #default="scope">
                <el-input v-model="scope.row.unit" placeholder="请输入单位" />
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="120">
              <template #default="scope">
                <el-input v-model.number="scope.row.quantity" type="number" placeholder="请输入数量" @change="calculateTotal" />
              </template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="单价" width="150">
              <template #default="scope">
                <el-input v-model.number="scope.row.unitPrice" type="number" placeholder="请输入单价" @change="calculateTotal" />
              </template>
            </el-table-column>
            <el-table-column prop="taxRate" label="税率(%)" width="150">
              <template #default="scope">
                <el-input v-model.number="scope.row.taxRate" type="number" placeholder="请输入税率" @change="calculateTotal" />
              </template>
            </el-table-column>
            <el-table-column prop="taxAmount" label="税额" width="150">
              <template #default="scope">
                <el-input v-model.number="scope.row.taxAmount" type="number" placeholder="税额" disabled />
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="150">
              <template #default="scope">
                <el-input v-model.number="scope.row.amount" type="number" placeholder="金额" disabled />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" type="danger" @click="handleDeleteDetail(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      title="销售订单详情"
      v-model="detailDialogVisible"
      width="80%"
      :before-close="handleCloseDetailDialog"
    >
      <!-- 订单信息 -->
      <el-card shadow="never" class="detail-card">
        <template #header>
          <div class="card-header">
            <span>订单信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ salesOrderDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户ID">{{ salesOrderDetail.customerId }}</el-descriptions-item>
          <el-descriptions-item label="订单日期">{{ salesOrderDetail.orderDate || salesOrderDetail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="订单类型">{{ salesOrderDetail.orderType }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">{{ formatAmount(salesOrderDetail.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(salesOrderDetail.status)">{{ formatStatus(salesOrderDetail.status) }}</el-tag>
        </el-descriptions-item>
          <el-descriptions-item label="创建人" :span="2">{{ salesOrderDetail.createBy }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ salesOrderDetail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ salesOrderDetail.remark }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 订单明细 -->
      <el-card shadow="never" class="detail-card">
        <template #header>
          <div class="card-header">
            <span>订单明细</span>
          </div>
        </template>
        <el-table :data="salesOrderDetail.details" stripe border>
          <el-table-column prop="productId" label="产品编码" />
          <el-table-column prop="productName" label="产品名称" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column prop="unitPrice" label="单价">
            <template #default="scope">
              ¥{{ scope.row.unitPrice.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额">
            <template #default="scope">
              ¥{{ scope.row.amount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="spec" label="规格" />
          <el-table-column prop="unit" label="单位" />
        </el-table>
      </el-card>

      <!-- 应收款单 -->
      <el-card v-if="salesOrderDetail.receivable" shadow="never" class="detail-card">
        <template #header>
          <div class="card-header">
            <span>应收款单</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="应收单号">{{ salesOrderDetail.receivable.receivableNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ salesOrderDetail.receivable.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="金额">{{ formatAmount(salesOrderDetail.receivable.amount) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(salesOrderDetail.receivable.status)">{{ formatReceivableStatus(salesOrderDetail.receivable.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDate(salesOrderDetail.receivable.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 会计分录 -->
      <el-card v-if="salesOrderDetail.accountingEntries && salesOrderDetail.accountingEntries.length > 0" shadow="never" class="detail-card">
        <template #header>
          <div class="card-header">
            <span>会计分录</span>
          </div>
        </template>
        <el-table :data="salesOrderDetail.accountingEntries" stripe border>
          <el-table-column label="借方科目" prop="debitName"></el-table-column>
          <el-table-column label="贷方科目" prop="creditName"></el-table-column>
          <el-table-column label="金额" :formatter="formatAmount" prop="amount"></el-table-column>
          <el-table-column label="税额" :formatter="formatAmount" prop="taxAmount"></el-table-column>
          <el-table-column label="状态" prop="status"></el-table-column>
          <el-table-column label="备注" prop="remark"></el-table-column>
        </el-table>
      </el-card>
    </el-dialog>
  </div>
</template>

<script>
import { addSalesOrder, getSalesOrderList, getSalesOrderDetail, updateSalesOrder, auditSalesOrder } from '@/api/salesOrderApi'
import { getCustomerList } from '@/api/customer'

export default {
  name: 'SalesOrder',
  data() {
    return {
      // 搜索表单
      searchForm: {
        orderNo: '',
        customerName: '',
        orderType: '',
        status: '',
        dateRange: []
      },
      // 销售订单列表
      salesOrderList: [],
      // 加载状态
      loading: false,
      // 分页信息
      currentPage: 1,
      pageSize: 10,
      total: 0,
      // 新增/编辑弹窗
      dialogVisible: false,
      dialogTitle: '新增销售订单',
      // 销售订单表单
      salesOrderForm: {
        id: null,
        orderNo: '',
        customerId: null,
        customerName: '',
        orderDate: '',
        orderType: '',
        totalAmount: 0,
        status: 'PENDING',
        remark: '',
        details: []
      },
      // 客户列表
      customerList: [],
      // 详情弹窗
      detailDialogVisible: false,
      // 销售订单详情
      salesOrderDetail: {}
    }
  },
  mounted() {
    this.getSalesOrderList()
    this.loadCustomerList()
  },
  methods: {
    // 加载客户列表（只加载启用状态的客户）
    loadCustomerList() {
      getCustomerList({ pageNum: 1, pageSize: 100, status: '启用' }).then(res => {
        if (res.code === 200) {
          this.customerList = res.data.records || []
        } else {
          this.$message.error(res.msg)
        }
      }).catch(error => {
        this.$message.error('获取客户列表失败')
      })
    },
    // 生成订单编号
    generateOrderNo() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      const second = String(now.getSeconds()).padStart(2, '0')
      const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
      return `SO${year}${month}${day}${hour}${minute}${second}${random}`
    },
    // 查询销售订单列表
    getSalesOrderList() {
      this.loading = true
      const params = {
        pageNum: this.currentPage,
        pageSize: this.pageSize,
        orderNo: this.searchForm.orderNo,
        customerName: this.searchForm.customerName,
        orderType: this.searchForm.orderType,
        status: this.searchForm.status,
        startTime: this.searchForm.dateRange[0] || '',
        endTime: this.searchForm.dateRange[1] || ''
      }
      getSalesOrderList(params).then(res => {
        if (res.code === 200) {
          // 转换字段名，将下划线命名法转换为驼峰命名法
          this.salesOrderList = res.data.records.map(order => ({
            id: order.id,
            orderNo: order.order_no,
            customerName: order.customer_name,
            orderDate: order.create_time, // 订单日期映射到创建时间
            orderType: order.biz_sub_type,
            // 明确转换为数字类型
            totalAmount: parseFloat(order.total_amount) || 0,
            status: order.status || '',
            createBy: order.create_user_id || '未知',
            createTime: order.create_time,
            updateTime: order.update_time
          }))
          this.total = res.data.total
        } else {
          this.$message.error(res.msg)
        }
        this.loading = false
      }).catch(error => {
        this.$message.error('获取销售订单列表失败')
        this.loading = false
      })
    },
    // 搜索
    handleSearch() {
      this.currentPage = 1
      this.getSalesOrderList()
    },
    // 处理客户选择变化
    handleCustomerChange() {
      // 找到选中的客户对象
      const selectedCustomer = this.customerList.find(customer => customer.customerId === this.salesOrderForm.customerId)
      if (selectedCustomer) {
        this.salesOrderForm.customerName = selectedCustomer.customerName
      }
    },
    // 重置
    handleReset() {
      this.searchForm = {
        orderNo: '',
        customerName: '',
        orderType: '',
        status: '',
        dateRange: []
      }
      this.currentPage = 1
      this.getSalesOrderList()
    },
    // 分页大小改变
    handleSizeChange(val) {
      this.pageSize = val
      this.getSalesOrderList()
    },
    // 当前页码改变
    handleCurrentChange(val) {
      this.currentPage = val
      this.getSalesOrderList()
    },
    // 格式化金额
    formatAmount(row, column, value) {
      // 支持两种调用方式：
      // 1. 表格格式化器调用: formatAmount(row, column, value)
      // 2. 直接调用: formatAmount(value)
      let amount = 0
      if (typeof row === 'object' && row !== null && column) {
        // 表格格式化器调用方式
        amount = Number(row.totalAmount || value) || 0
      } else {
        // 直接调用方式
        amount = Number(row) || 0
      }
      return `¥${amount.toFixed(2)}`
    },
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr.replace(/-/g, '/'))
      if (isNaN(date.getTime())) return ''
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    // 获取状态标签类型
    getStatusTagType(status) {
      const statusMap = {
        'DRAFT': 'info',
        'PENDING': 'warning',
        'APPROVED': 'success',
        '待确认': 'warning', // 添加待确认状态
        'UNPAID': 'warning',
        'PAID': 'success',
        'CLOSED': 'danger'
      }
      return statusMap[status] || 'info'
    },
    // 格式化应收款状态
    formatReceivableStatus(status) {
      const statusMap = {
        '待确认': '待确认',
        'UNPAID': '未付款',
        'PARTIALLY_PAID': '部分付款',
        'PAID': '已付款',
        'CLOSED': '已关闭'
      }
      return statusMap[status] || status
    },
    // 格式化状态
    formatStatus(row, column, value) {
      // 支持两种调用方式：
      // 1. 表格格式化器调用: formatStatus(row, column, value)
      // 2. 直接调用: formatStatus(status)
      let status = ''
      if (typeof row === 'object' && row !== null && column) {
        // 表格格式化器调用方式
        status = row.status || value || ''
      } else {
        // 直接调用方式
        status = row || ''
      }
      
      const statusMap = {
        'DRAFT': '待审核',
        'APPROVED': '已审核'
      }
      return statusMap[status] || status
    },
    // 新增销售订单
    handleAdd() {
      this.dialogTitle = '新增销售订单'
      this.salesOrderForm = {
        id: null,
        orderNo: this.generateOrderNo(), // 自动生成订单编号
        customerId: null,
        customerName: '',
        orderDate: '',
        orderType: '',
        totalAmount: 0,
        status: 'DRAFT',
        remark: '',
        details: []
      }
      this.handleAddDetail()
      this.dialogVisible = true
    },
    // 编辑销售订单
    handleEdit(row) {
      // 检查订单状态，已审核的订单不允许编辑
      if (row.status === 'APPROVED') {
        this.$message.warning('已审核的订单不允许编辑')
        return
      }
      this.dialogTitle = '编辑销售订单'
      getSalesOrderDetail(row.id).then(res => {
        if (res.code === 200) {
          const detail = res.data || {}
          
          // 处理订单主表信息，转换下划线命名为驼峰命名
          const order = detail.order || {}
          
          // 处理订单明细，转换下划线命名为驼峰命名
          console.log('后端返回的订单明细原始数据:', detail.lines)
          const details = (detail.lines || []).map(line => {
            console.log('单个订单项数据:', line)
            console.log('price字段类型:', typeof line.price, '值:', line.price)
            console.log('amount字段类型:', typeof line.amount, '值:', line.amount)
            // 直接使用原始数值，后端返回的已经是普通数字类型
            return {
              id: line.id || '',
              productId: line.product_code || '',
              productName: line.product_name || '',
              spec: line.spec || '',
              unit: line.unit || '',
              quantity: line.quantity || 0,
              unitPrice: line.price || 0,
              amount: line.amount || 0,
              taxRate: line.tax_rate || 0,
              taxAmount: line.tax_amount || 0
            }
          })
          console.log('转换后的订单明细数据:', details)
          // 构造salesOrderForm对象
          this.salesOrderForm = {
            id: order.id || null,
            orderNo: order.order_no || this.generateOrderNo(),
            customerId: order.customer_id || null,
            customerName: order.customer_name || '',
            orderDate: order.create_time || '',
            orderType: order.biz_sub_type || '',
            totalAmount: order.total_amount || 0,
            status: order.status || 'DRAFT',
            remark: order.remark || '',
            details: details
          }
          
          this.dialogVisible = true
        } else {
          this.$message.error(res.msg)
        }
      }).catch(error => {
        this.$message.error('获取销售订单详情失败')
      })
    },
    // 查看销售订单详情
    handleView(id) {
      this.detailDialogVisible = false // 先关闭弹窗，避免闪烁
      this.loading = true
      getSalesOrderDetail(id).then(res => {
        this.loading = false
        if (res.code === 200) {
          // 打印后端返回的原始数据，以便调试
          console.log('原始响应数据:', res.data)
          console.log('原始订单明细:', res.data.lines)
          
          // 后端返回的是包含order、lines等键的Map结构
          const detail = res.data || {}
          
          // 处理订单主表信息，转换下划线命名为驼峰命名
          const order = detail.order || {}
          
          // 处理订单明细，转换下划线命名为驼峰命名
          const details = (detail.lines || []).map(line => ({
            id: line.id || '',
            productId: line.product_code || '',
            productName: line.product_name || '',
            spec: line.spec || '',
            unit: line.unit || '',
            // 直接使用数值，从控制台输出看后端返回的是普通数字类型
            quantity: parseFloat(line.quantity) || 0,
            unitPrice: parseFloat(line.price) || 0,
            amount: parseFloat(line.amount) || 0,
            taxRate: parseFloat(line.tax_rate) || 0,
            taxAmount: parseFloat(line.tax_amount) || 0
          }))
          
          // 处理应收款单，转换下划线命名为驼峰命名
          let receivable = null
          if (detail.receivable) {
            receivable = {
              receivableNo: detail.receivable.receivable_no || '',
              customerName: detail.receivable.customer_name || '',
              amount: detail.receivable.total_amount || 0,
              status: detail.receivable.status || '',
              createTime: detail.receivable.create_time || ''
            }
          }
          
          // 处理会计分录，直接使用后端返回的数据，不再拆分为借方和贷方分录
          const accountingEntries = (detail.accountingEntries || []).map(entry => ({
            id: entry.id || '',
            sourceType: entry.source_type || '',
            sourceId: entry.source_id || '',
            debitCode: entry.debit_code || '',
            debitName: entry.debit_name || '',
            creditCode: entry.credit_code || '',
            creditName: entry.credit_name || '',
            amount: Number(entry.amount) || 0, // 使用Number()替代parseFloat()避免精度丢失
            taxAmount: Number(entry.tax_amount) || 0, // 使用Number()替代parseFloat()避免精度丢失
            status: entry.status || '',
            remark: entry.remark || '',
            createTime: entry.create_time || ''
          }))
          
          // 一次性设置salesOrderDetail对象，确保所有属性都存在
          this.salesOrderDetail = {
            id: order.id || '',
            orderNo: order.order_no || '',
            customerId: order.customer_id || '',
            customerName: order.customer_name || '',
            orderType: order.biz_sub_type || '',
            orderDate: order.create_time || '', // 使用创建时间作为订单日期
            totalAmount: order.total_amount || 0,
            status: order.status || '',
            createBy: order.create_user_id || '未知',
            createTime: order.create_time || '',
            updateTime: order.update_time || '',
            details: details,
            receivable: receivable,
            accountingEntries: accountingEntries
          }
          
          // API调用成功后再打开弹窗
          this.detailDialogVisible = true
        } else {
          this.$message.error(res.msg)
        }
      }).catch(error => {
        console.error('获取销售订单详情失败:', error)
        this.$message.error('获取销售订单详情失败')
      })
    },
    // 关闭弹窗
    handleCloseDialog() {
      this.dialogVisible = false
      if (this.$refs.salesOrderForm) {
        this.$refs.salesOrderForm.resetFields()
      }
    },
    // 关闭详情弹窗
    handleCloseDetailDialog() {
      this.detailDialogVisible = false
    },
    // 新增明细
    handleAddDetail() {
      this.salesOrderForm.details.push({
        productId: '',
        productName: '',
        quantity: 0,
        unitPrice: 0,
        taxRate: 0, // 添加税率字段
        taxAmount: 0, // 添加税额字段
        amount: 0
      })
    },
    // 删除明细
    handleDeleteDetail(index) {
      this.salesOrderForm.details.splice(index, 1)
      this.calculateTotal()
    },
    // 计算总金额
    calculateTotal() {
      let total = 0
      this.salesOrderForm.details.forEach(detail => {
        // 计算金额 = 数量 * 单价
        detail.amount = (detail.quantity || 0) * (detail.unitPrice || 0)
        // 计算税额 = 金额 * 税率 / 100
        detail.taxAmount = detail.amount * (detail.taxRate || 0) / 100
        // 总金额 = 金额 + 税额
        total += detail.amount + detail.taxAmount
      })
      this.salesOrderForm.totalAmount = total
    },
    // 提交表单
    handleSubmit() {
      this.$refs.salesOrderForm.validate((valid) => {
        if (valid) {
          if (!this.salesOrderForm.customerId) {
            this.$message.error('请选择客户名称')
            return
          }
          if (this.salesOrderForm.details.length === 0) {
            this.$message.error('请至少添加一条订单明细')
            return
          }
          
          // 计算总金额
          this.calculateTotal()
          
          if (this.salesOrderForm.id) {
            // 修改销售订单
            // 将salesOrderForm转换为后端期望的SalesOrderDTO格式
            const orderData = {
              order: {
                id: this.salesOrderForm.id,
                orderNo: this.salesOrderForm.orderNo,
                customer_id: this.salesOrderForm.customerId, // 注意字段名使用下划线格式
                customer_name: this.salesOrderForm.customerName, // 添加customerName字段
                orderDate: this.salesOrderForm.orderDate,
                orderType: this.salesOrderForm.orderType,
                biz_sub_type: this.salesOrderForm.orderType, // 添加biz_sub_type字段，使用orderType的值
                totalAmount: this.salesOrderForm.totalAmount,
                status: this.salesOrderForm.status,
                remark: this.salesOrderForm.remark
              },
              lines: this.salesOrderForm.details.map(detail => ({
                product_code: detail.productId, // 将productId映射为product_code
                product_name: detail.productName, // 将productName映射为product_name
                spec: detail.spec || '', // 添加spec字段，默认为空字符串
                unit: detail.unit || '', // 添加unit字段，默认为空字符串
                quantity: detail.quantity,
                price: detail.unitPrice, // 将unitPrice映射为price
                tax_rate: detail.taxRate, // 将taxRate映射为tax_rate
                tax_amount: detail.taxAmount, // 将taxAmount映射为tax_amount
                amount: detail.amount
              }))
            }
            updateSalesOrder(orderData).then(res => {
              if (res.code === 200) {
                this.$message.success('修改销售订单成功')
                this.dialogVisible = false
                this.getSalesOrderList()
              } else {
                this.$message.error(res.msg)
              }
            }).catch(error => {
              this.$message.error('修改销售订单失败')
            })
          } else {
            // 新增销售订单
            // 将salesOrderForm转换为后端期望的SalesOrderDTO格式
            const orderData = {
              order: {
                id: this.salesOrderForm.id,
                orderNo: this.salesOrderForm.orderNo,
                customer_id: this.salesOrderForm.customerId, // 注意字段名使用下划线格式
                customer_name: this.salesOrderForm.customerName, // 添加customerName字段
                orderDate: this.salesOrderForm.orderDate,
                orderType: this.salesOrderForm.orderType,
                biz_sub_type: this.salesOrderForm.orderType, // 添加biz_sub_type字段，使用orderType的值
                totalAmount: this.salesOrderForm.totalAmount,
                status: this.salesOrderForm.status,
                remark: this.salesOrderForm.remark
              },
              lines: this.salesOrderForm.details.map(detail => ({
                product_code: detail.productId, // 将productId映射为product_code
                product_name: detail.productName, // 将productName映射为product_name
                spec: detail.spec || '', // 添加spec字段，默认为空字符串
                unit: detail.unit || '', // 添加unit字段，默认为空字符串
                quantity: detail.quantity,
                price: detail.unitPrice, // 将unitPrice映射为price
                tax_rate: detail.taxRate, // 将taxRate映射为tax_rate
                tax_amount: detail.taxAmount, // 将taxAmount映射为tax_amount
                amount: detail.amount
              }))
            }
            addSalesOrder(orderData).then(res => {
              if (res.code === 200) {
                this.$message.success('新增销售订单成功')
                this.dialogVisible = false
                this.getSalesOrderList()
              } else {
                this.$message.error(res.msg)
              }
            }).catch(error => {
              this.$message.error('新增销售订单失败')
            })
          }
        } else {
          return false
        }
      })
    },
    // 审核销售订单
    handleAudit(id) {
      this.$confirm('确定要审核该销售订单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 这里假设用户ID从登录信息中获取
        const userId = 1
        auditSalesOrder({ id, userId }).then(res => {
          if (res.code === 200) {
            this.$message.success('审核销售订单成功')
            this.getSalesOrderList()
          } else {
            this.$message.error(res.msg)
          }
        }).catch(error => {
          this.$message.error('审核销售订单失败')
        })
      }).catch(() => {
        this.$message.info('已取消审核')
      })
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
  gap: 6px;
  flex-wrap: nowrap;
  width: 100%;
  min-width: 200px;
}

.btn-op {
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 12px;
  transition: all 0.2s ease;
  border: none;
  font-weight: 500;
  width: 60px;
  min-width: 60px;
  max-width: 60px;
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
  overflow: hidden;
  text-overflow: ellipsis;
}

.btn-op:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

:deep(.btn-op) {
  width: 60px !important;
  min-width: 60px !important;
  max-width: 60px !important;
  height: 28px !important;
  min-height: 28px !important;
  max-height: 28px !important;
  padding: 4px 8px !important;
  text-align: center !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  line-height: 1 !important;
  flex-shrink: 0 !important;
  overflow: hidden !important;
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

.main-info-card {
  margin-bottom: 20px;
}

.detail-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.detail-card {
  margin-bottom: 20px;
}
</style>