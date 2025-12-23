<template>
  <div class="report-page">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 科目余额表 -->
        <el-tab-pane label="科目余额表" name="accountBalance">
          <div class="report-header">
            <el-form :model="accountBalanceParams" inline>
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="accountBalanceParams.startDate"
                  type="date"
                  placeholder="选择开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="accountBalanceParams.endDate"
                  type="date"
                  placeholder="选择结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="快捷选择">
                <el-button-group>
                  <el-button size="small" @click="setQuickRange('month', 'account')">本月</el-button>
                  <el-button size="small" @click="setQuickRange('quarter', 'account')">本季</el-button>
                  <el-button size="small" @click="setQuickRange('year', 'account')">本年</el-button>
                </el-button-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadAccountBalance">查询</el-button>
                <el-button @click="exportAccountBalance">导出</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <el-table
            v-loading="accountBalanceLoading"
            :data="accountBalanceData"
            border
            stripe
            style="width: 100%; margin-top: 20px"
          >
            <el-table-column prop="subjectCode" label="科目编码" width="150" align="center" />
            <el-table-column prop="subjectName" label="科目名称" min-width="200" />
            <el-table-column prop="subjectType" label="科目类型" width="120" align="center" />
            <el-table-column prop="beginBalance" label="期初余额" width="150" align="right">
              <template #default="scope">
                {{ formatCurrency(scope.row.beginBalance) }}
              </template>
            </el-table-column>
            <el-table-column prop="debitAmount" label="本期借方" width="150" align="right">
              <template #default="scope">
                {{ formatCurrency(scope.row.debitAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="creditAmount" label="本期贷方" width="150" align="right">
              <template #default="scope">
                {{ formatCurrency(scope.row.creditAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="endBalance" label="期末余额" width="150" align="right">
              <template #default="scope">
                {{ formatCurrency(scope.row.endBalance) }}
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 科目余额表可视化图表 -->
          <el-row :gutter="20" v-if="accountBalanceData.length > 0" style="margin-top: 20px">
            <el-col :span="12">
              <el-card shadow="never">
                <template #header>
                  <span>科目余额分布（期末余额）</span>
                </template>
                <div ref="accountBalanceChartRef" style="width: 100%; height: 400px"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="never">
                <template #header>
                  <span>科目类型占比</span>
                </template>
                <div ref="accountTypeChartRef" style="width: 100%; height: 400px"></div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 利润表 -->
        <el-tab-pane label="利润表" name="profitLoss">
          <div class="report-header">
            <el-form :model="profitLossParams" inline>
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="profitLossParams.startDate"
                  type="date"
                  placeholder="选择开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="profitLossParams.endDate"
                  type="date"
                  placeholder="选择结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="快捷选择">
                <el-button-group>
                  <el-button size="small" @click="setQuickRange('month', 'profit')">本月</el-button>
                  <el-button size="small" @click="setQuickRange('quarter', 'profit')">本季</el-button>
                  <el-button size="small" @click="setQuickRange('year', 'profit')">本年</el-button>
                </el-button-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadProfitLoss">查询</el-button>
                <el-button @click="exportProfitLoss">导出</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div v-if="profitLossData" style="margin-top: 20px">
            <el-card shadow="never">
              <h3>利润表（{{ profitLossData.period }}）</h3>
              <el-divider />
              <el-descriptions :column="2" border>
                <el-descriptions-item label="营业收入">
                  {{ formatCurrency(profitLossData.revenue) }}
                </el-descriptions-item>
                <el-descriptions-item label="营业成本">
                  {{ formatCurrency(profitLossData.costOfSales) }}
                </el-descriptions-item>
                <el-descriptions-item label="毛利润">
                  <span style="color: #67c23a; font-weight: bold">
                    {{ formatCurrency(profitLossData.grossProfit) }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="营业费用">
                  {{ formatCurrency(profitLossData.operatingExpenses) }}
                </el-descriptions-item>
                <el-descriptions-item label="营业利润">
                  <span style="color: #409eff; font-weight: bold">
                    {{ formatCurrency(profitLossData.operatingProfit) }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="净利润">
                  <span style="color: #e6a23c; font-weight: bold">
                    {{ formatCurrency(profitLossData.netProfit) }}
                  </span>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
            <el-card shadow="never" style="margin-top: 20px">
              <h4>明细项</h4>
              <el-table :data="profitLossData.items" border stripe style="width: 100%">
                <el-table-column prop="subjectCode" label="科目编码" width="150" align="center" />
                <el-table-column prop="subjectName" label="科目名称" min-width="200" />
                <el-table-column prop="itemType" label="项目类型" width="120" align="center" />
                <el-table-column prop="amount" label="金额" width="150" align="right">
                  <template #default="scope">
                    {{ formatCurrency(scope.row.amount) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
            
            <!-- 利润表可视化图表 -->
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="24">
                <el-card shadow="never">
                  <template #header>
                    <span>利润结构分析</span>
                  </template>
                  <div ref="profitLossChartRef" style="width: 100%; height: 400px"></div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 资产负债表 -->
        <el-tab-pane label="资产负债表" name="balanceSheet">
          <div class="report-header">
            <el-form :model="balanceSheetParams" inline>
              <el-form-item label="报表日期">
                <el-date-picker
                  v-model="balanceSheetParams.reportDate"
                  type="date"
                  placeholder="选择报表日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadBalanceSheet">查询</el-button>
                <el-button @click="exportBalanceSheet">导出</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div v-if="balanceSheetData" style="margin-top: 20px">
            <el-card shadow="never">
              <h3>资产负债表（{{ balanceSheetData.reportDate }}）</h3>
              <el-divider />
              <el-row :gutter="20">
                <!-- 资产 -->
                <el-col :span="12">
                  <el-card shadow="never">
                    <h4>资产</h4>
                    <el-table :data="balanceSheetData.assets" border stripe style="width: 100%">
                      <el-table-column prop="subjectCode" label="科目编码" width="120" align="center" />
                      <el-table-column prop="subjectName" label="科目名称" min-width="150" />
                      <el-table-column prop="category" label="分类" width="120" align="center" />
                      <el-table-column prop="balance" label="余额" width="150" align="right">
                        <template #default="scope">
                          {{ formatCurrency(scope.row.balance) }}
                        </template>
                      </el-table-column>
                    </el-table>
                    <div style="margin-top: 10px; text-align: right; font-weight: bold">
                      资产合计：{{ formatCurrency(balanceSheetData.totalAssets) }}
                    </div>
                  </el-card>
                </el-col>
                <!-- 负债和所有者权益 -->
                <el-col :span="12">
                  <el-card shadow="never">
                    <h4>负债</h4>
                    <el-table :data="balanceSheetData.liabilities" border stripe style="width: 100%">
                      <el-table-column prop="subjectCode" label="科目编码" width="120" align="center" />
                      <el-table-column prop="subjectName" label="科目名称" min-width="150" />
                      <el-table-column prop="category" label="分类" width="120" align="center" />
                      <el-table-column prop="balance" label="余额" width="150" align="right">
                        <template #default="scope">
                          {{ formatCurrency(scope.row.balance) }}
                        </template>
                      </el-table-column>
                    </el-table>
                    <div style="margin-top: 10px; text-align: right; font-weight: bold">
                      负债合计：{{ formatCurrency(balanceSheetData.totalLiabilities) }}
                    </div>
                    <h4 style="margin-top: 20px">所有者权益</h4>
                    <el-table :data="balanceSheetData.equity" border stripe style="width: 100%">
                      <el-table-column prop="subjectCode" label="科目编码" width="120" align="center" />
                      <el-table-column prop="subjectName" label="科目名称" min-width="150" />
                      <el-table-column prop="category" label="分类" width="120" align="center" />
                      <el-table-column prop="balance" label="余额" width="150" align="right">
                        <template #default="scope">
                          {{ formatCurrency(scope.row.balance) }}
                        </template>
                      </el-table-column>
                    </el-table>
                    <div style="margin-top: 10px; text-align: right; font-weight: bold">
                      所有者权益合计：{{ formatCurrency(balanceSheetData.totalEquity) }}
                    </div>
                    <div style="margin-top: 10px; text-align: right; font-weight: bold; color: #409eff">
                      负债和所有者权益总计：{{ formatCurrency(getTotalLiabilitiesAndEquity()) }}
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </el-card>
            
            <!-- 资产负债表可视化图表 -->
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="24">
                <el-card shadow="never">
                  <template #header>
                    <span>资产、负债、权益结构</span>
                  </template>
                  <div ref="balanceSheetChartRef" style="width: 100%; height: 400px"></div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 项目报表 -->
        <el-tab-pane label="项目报表" name="projectReport">
          <div class="report-header">
            <el-form :model="projectReportParams" inline>
              <el-form-item label="项目">
                <el-select
                  v-model="projectReportParams.projectId"
                  placeholder="请选择项目（留空查询所有）"
                  clearable
                  filterable
                  style="width: 250px"
                >
                  <el-option
                    v-for="project in projectList"
                    :key="project.projectId"
                    :label="`${project.projectCode} - ${project.projectName}`"
                    :value="project.projectId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="projectReportParams.startDate"
                  type="date"
                  placeholder="选择开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="projectReportParams.endDate"
                  type="date"
                  placeholder="选择结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadProjectReport">查询</el-button>
                <el-button @click="exportProjectReport">导出</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div v-if="projectReportData && projectReportData.length > 0" style="margin-top: 20px">
            <el-card
              v-for="report in projectReportData"
              :key="report.projectId"
              shadow="never"
              style="margin-bottom: 20px"
            >
              <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <h3 style="margin: 0">
                    {{ report.projectName }}（{{ report.projectCode }}）
                  </h3>
                  <el-tag :type="getProjectStatusTagType(report.status)">
                    {{ report.status }}
                  </el-tag>
                </div>
              </template>
              
              <!-- 项目基本信息 -->
              <el-descriptions :column="3" border style="margin-bottom: 20px">
                <el-descriptions-item label="项目经理">
                  {{ report.projectManager || '未指定' }}
                </el-descriptions-item>
                <el-descriptions-item label="开始日期">
                  {{ formatDate(report.startDate, 'YYYY-MM-DD') || '未设置' }}
                </el-descriptions-item>
                <el-descriptions-item label="结束日期">
                  {{ formatDate(report.endDate, 'YYYY-MM-DD') || '未设置' }}
                </el-descriptions-item>
                <el-descriptions-item label="预算金额">
                  {{ formatCurrency(report.budget) }}
                </el-descriptions-item>
                <el-descriptions-item label="预算使用率">
                  {{ formatCurrency(report.budgetUtilization) }}%
                </el-descriptions-item>
              </el-descriptions>

              <!-- 收支统计 -->
              <el-row :gutter="20" style="margin-bottom: 20px">
                <el-col :span="6">
                  <el-card shadow="never" style="text-align: center; background: #f0f9ff">
                    <div style="font-size: 14px; color: #606266; margin-bottom: 10px">采购订单</div>
                    <div style="font-size: 24px; font-weight: bold; color: #409eff; margin-bottom: 5px">
                      {{ report.purchaseOrderCount || 0 }}
                    </div>
                    <div style="font-size: 16px; color: #909399">
                      {{ formatCurrency(report.purchaseAmount) }}
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="6">
                  <el-card shadow="never" style="text-align: center; background: #f0fdf4">
                    <div style="font-size: 14px; color: #606266; margin-bottom: 10px">销售订单</div>
                    <div style="font-size: 24px; font-weight: bold; color: #67c23a; margin-bottom: 5px">
                      {{ report.salesOrderCount || 0 }}
                    </div>
                    <div style="font-size: 16px; color: #909399">
                      {{ formatCurrency(report.salesAmount) }}
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="6">
                  <el-card shadow="never" style="text-align: center; background: #fef0f0">
                    <div style="font-size: 14px; color: #606266; margin-bottom: 10px">总支出</div>
                    <div style="font-size: 24px; font-weight: bold; color: #f56c6c; margin-bottom: 5px">
                      {{ formatCurrency(report.totalExpense) }}
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="6">
                  <el-card shadow="never" style="text-align: center; background: #fff7e6">
                    <div style="font-size: 14px; color: #606266; margin-bottom: 10px">净利润</div>
                    <div
                      style="font-size: 24px; font-weight: bold; margin-bottom: 5px"
                      :style="{ color: Number(report.netProfit) >= 0 ? '#e6a23c' : '#f56c6c' }"
                    >
                      {{ formatCurrency(report.netProfit) }}
                    </div>
                  </el-card>
                </el-col>
              </el-row>

              <!-- 采购订单明细 -->
              <el-card shadow="never" style="margin-bottom: 20px" v-if="report.purchaseOrders && report.purchaseOrders.length > 0">
                <template #header>
                  <h4 style="margin: 0">采购订单明细</h4>
                </template>
                <el-table :data="report.purchaseOrders" border stripe style="width: 100%">
                  <el-table-column prop="orderNo" label="订单编号" width="180" />
                  <el-table-column prop="supplierName" label="供应商" min-width="150" />
                  <el-table-column prop="amount" label="订单金额" width="150" align="right">
                    <template #default="scope">
                      {{ formatCurrency(scope.row.amount) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100" align="center">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === '已审核' ? 'success' : 'info'">
                        {{ scope.row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180">
                    <template #default="scope">
                      {{ formatDate(scope.row.createTime) }}
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>

              <!-- 销售订单明细 -->
              <el-card shadow="never" v-if="report.salesOrders && report.salesOrders.length > 0">
                <template #header>
                  <h4 style="margin: 0">销售订单明细</h4>
                </template>
                <el-table :data="report.salesOrders" border stripe style="width: 100%">
                  <el-table-column prop="orderNo" label="订单编号" width="180" />
                  <el-table-column prop="customerName" label="客户" min-width="150" />
                  <el-table-column prop="amount" label="订单金额" width="150" align="right">
                    <template #default="scope">
                      {{ formatCurrency(scope.row.amount) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100" align="center">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === '已审核' ? 'success' : 'info'">
                        {{ scope.row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180">
                    <template #default="scope">
                      {{ formatDate(scope.row.createTime) }}
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-card>
          </div>
          <el-empty v-else-if="!projectReportLoading" description="暂无数据" style="margin-top: 40px" />
        </el-tab-pane>

        <!-- 现金流量表（简化版） -->
        <el-tab-pane label="现金流量表" name="cashFlow">
          <div class="report-header">
            <el-form :model="cashFlowParams" inline>
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="cashFlowParams.startDate"
                  type="date"
                  placeholder="选择开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="cashFlowParams.endDate"
                  type="date"
                  placeholder="选择结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 200px"
                />
              </el-form-item>
              <el-form-item label="快捷选择">
                <el-button-group>
                  <el-button size="small" @click="setQuickRange('month', 'cash')">本月</el-button>
                  <el-button size="small" @click="setQuickRange('quarter', 'cash')">本季</el-button>
                  <el-button size="small" @click="setQuickRange('year', 'cash')">本年</el-button>
                </el-button-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadCashFlow">查询</el-button>
                <el-button @click="exportCashFlow">导出</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div v-if="cashFlowData" style="margin-top: 20px">
            <el-card shadow="never">
              <h3>现金流量表（{{ cashFlowData.period }}）</h3>
              <el-divider />
              <el-table
                v-loading="cashFlowLoading"
                :data="cashFlowData.items"
                border
                stripe
                style="width: 100%"
              >
                <el-table-column prop="itemName" label="项目" min-width="220" />
                <el-table-column prop="category" label="类别" width="120" align="center" />
                <el-table-column prop="amount" label="金额" width="160" align="right">
                  <template #default="scope">
                    {{ formatCurrency(scope.row.amount) }}
                  </template>
                </el-table-column>
              </el-table>

              <el-descriptions :column="2" border style="margin-top: 20px">
                <el-descriptions-item label="经营活动产生的现金流量净额">
                  {{ formatCurrency(cashFlowData.netOperating) }}
                </el-descriptions-item>
                <el-descriptions-item label="投资活动产生的现金流量净额">
                  {{ formatCurrency(cashFlowData.netInvesting) }}
                </el-descriptions-item>
                <el-descriptions-item label="筹资活动产生的现金流量净额">
                  {{ formatCurrency(cashFlowData.netFinancing) }}
                </el-descriptions-item>
                <el-descriptions-item label="现金及现金等价物净增加额">
                  <span style="font-weight: bold; color: #409eff">
                    {{ formatCurrency(cashFlowData.netIncrease) }}
                  </span>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
            
            <!-- 现金流量表可视化图表 -->
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="24">
                <el-card shadow="never">
                  <template #header>
                    <span>现金流量分析</span>
                  </template>
                  <div ref="cashFlowChartRef" style="width: 100%; height: 400px"></div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'
import * as echarts from 'echarts'
import { getAccountBalance, getProfitLoss, getBalanceSheet, getCashFlow, getProjectReport } from '@/api/reportApi'
import { getAllProjects } from '@/api/project'

// 图表引用
const accountBalanceChartRef = ref(null)
const accountTypeChartRef = ref(null)
const profitLossChartRef = ref(null)
const balanceSheetChartRef = ref(null)
const cashFlowChartRef = ref(null)

// 图表实例
let accountBalanceChart = null
let accountTypeChart = null
let profitLossChart = null
let balanceSheetChart = null
let cashFlowChart = null

const activeTab = ref('accountBalance')

// 科目余额表
const accountBalanceLoading = ref(false)
const accountBalanceData = ref([])
const accountBalanceParams = ref({
  startDate: '',
  endDate: ''
})

// 利润表
const profitLossLoading = ref(false)
const profitLossData = ref(null)
const profitLossParams = ref({
  startDate: '',
  endDate: ''
})

// 资产负债表
const balanceSheetLoading = ref(false)
const balanceSheetData = ref(null)
const balanceSheetParams = ref({
  reportDate: ''
})

// 现金流量表
const cashFlowLoading = ref(false)
const cashFlowData = ref(null)
const cashFlowParams = ref({
  startDate: '',
  endDate: ''
})

// 项目报表
const projectReportLoading = ref(false)
const projectReportData = ref([])
const projectReportParams = ref({
  projectId: null,
  startDate: '',
  endDate: ''
})
const projectList = ref([])

// 工具函数：格式化日期
const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  if (format === 'YYYY-MM-DD') {
    return `${y}-${m}-${day}`
  }
  return `${y}-${m}-${day} ${hour}:${minute}:${second}`
}

// 一键快捷时间范围：本月 / 本季 / 本年
const setQuickRange = (type, target) => {
  const now = new Date()
  const year = now.getFullYear()
  let start
  let end

  if (type === 'month') {
    // 本月
    start = new Date(year, now.getMonth(), 1)
    end = new Date(year, now.getMonth() + 1, 0)
  } else if (type === 'quarter') {
    // 本季：1-3, 4-6, 7-9, 10-12
    const currentMonth = now.getMonth() // 0-11
    const quarterStartMonth = Math.floor(currentMonth / 3) * 3
    start = new Date(year, quarterStartMonth, 1)
    end = new Date(year, quarterStartMonth + 3, 0)
  } else if (type === 'year') {
    // 本年
    start = new Date(year, 0, 1)
    end = new Date(year, 11, 31)
  } else {
    return
  }

  const startStr = formatDate(start)
  const endStr = formatDate(end)

  if (target === 'account') {
    accountBalanceParams.value.startDate = startStr
    accountBalanceParams.value.endDate = endStr
  } else if (target === 'profit') {
    profitLossParams.value.startDate = startStr
    profitLossParams.value.endDate = endStr
  } else if (target === 'cash') {
    cashFlowParams.value.startDate = startStr
    cashFlowParams.value.endDate = endStr
  }
}

// 初始化日期（默认当前月份），并自动加载一次科目余额表
onMounted(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() // 0-11

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)

  const firstDayStr = formatDate(firstDay)
  const lastDayStr = formatDate(lastDay)
  
  accountBalanceParams.value.startDate = firstDayStr
  accountBalanceParams.value.endDate = lastDayStr
  profitLossParams.value.startDate = firstDayStr
  profitLossParams.value.endDate = lastDayStr
  balanceSheetParams.value.reportDate = lastDayStr
  cashFlowParams.value.startDate = firstDayStr
  cashFlowParams.value.endDate = lastDayStr
  projectReportParams.value.startDate = firstDayStr
  projectReportParams.value.endDate = lastDayStr

  // 加载项目列表
  loadProjectList()

  // 页面进入时默认展示科目余额表
  loadAccountBalance()
})

// 加载科目余额表
const loadAccountBalance = async () => {
  if (!accountBalanceParams.value.startDate || !accountBalanceParams.value.endDate) {
    ElMessage.warning('请选择开始日期和结束日期')
    return
  }
  accountBalanceLoading.value = true
  try {
    const res = await getAccountBalance(accountBalanceParams.value)
    if (res.code === 200) {
      accountBalanceData.value = res.data || []
      ElMessage.success('查询成功')
      // 渲染图表
      await nextTick()
      renderAccountBalanceChart()
      renderAccountTypeChart()
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    accountBalanceLoading.value = false
  }
}

// 加载利润表
const loadProfitLoss = async () => {
  if (!profitLossParams.value.startDate || !profitLossParams.value.endDate) {
    ElMessage.warning('请选择开始日期和结束日期')
    return
  }
  profitLossLoading.value = true
  try {
    const res = await getProfitLoss(profitLossParams.value)
    if (res.code === 200) {
      profitLossData.value = res.data
      ElMessage.success('查询成功')
      // 渲染图表
      await nextTick()
      renderProfitLossChart()
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    profitLossLoading.value = false
  }
}

// 加载资产负债表
const loadBalanceSheet = async () => {
  if (!balanceSheetParams.value.reportDate) {
    ElMessage.warning('请选择报表日期')
    return
  }
  balanceSheetLoading.value = true
  try {
    const res = await getBalanceSheet(balanceSheetParams.value)
    if (res.code === 200) {
      balanceSheetData.value = res.data
      ElMessage.success('查询成功')
      // 渲染图表
      await nextTick()
      renderBalanceSheetChart()
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    balanceSheetLoading.value = false
  }
}

// 加载现金流量表
const loadCashFlow = async () => {
  if (!cashFlowParams.value.startDate || !cashFlowParams.value.endDate) {
    ElMessage.warning('请选择开始日期和结束日期')
    return
  }
  cashFlowLoading.value = true
  try {
    const res = await getCashFlow(cashFlowParams.value)
    if (res.code === 200) {
      cashFlowData.value = res.data
      ElMessage.success('查询成功')
      // 渲染图表
      await nextTick()
      renderCashFlowChart()
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    cashFlowLoading.value = false
  }
}

// 标签切换
const handleTabChange = (tabName) => {
  if (tabName === 'accountBalance' && accountBalanceData.value.length === 0) {
    loadAccountBalance()
  } else if (tabName === 'profitLoss' && !profitLossData.value) {
    loadProfitLoss()
  } else if (tabName === 'balanceSheet' && !balanceSheetData.value) {
    loadBalanceSheet()
  } else if (tabName === 'cashFlow' && !cashFlowData.value) {
    loadCashFlow()
  } else if (tabName === 'projectReport' && projectReportData.value.length === 0) {
    loadProjectReport()
  }
}

// 格式化货币
const formatCurrency = (value) => {
  if (value == null || value === undefined) return '0.00'
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 加载项目列表
const loadProjectList = async () => {
  try {
    const res = await getAllProjects()
    if (res.code === 200) {
      projectList.value = res.data || []
    }
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

// 加载项目报表
const loadProjectReport = async () => {
  projectReportLoading.value = true
  try {
    const params = {}
    if (projectReportParams.value.projectId) {
      params.projectId = projectReportParams.value.projectId
    }
    if (projectReportParams.value.startDate) {
      params.startDate = projectReportParams.value.startDate
    }
    if (projectReportParams.value.endDate) {
      params.endDate = projectReportParams.value.endDate
    }
    const res = await getProjectReport(params)
    if (res.code === 200) {
      projectReportData.value = res.data || []
      ElMessage.success('查询成功')
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    projectReportLoading.value = false
  }
}

// 获取项目状态标签类型
const getProjectStatusTagType = (status) => {
  const statusMap = {
    '进行中': 'success',
    '已完成': 'info',
    '已暂停': 'warning',
    '已取消': 'danger'
  }
  return statusMap[status] || 'info'
}

// 导出项目报表
const exportProjectReport = () => {
  if (projectReportData.value.length === 0) {
    ElMessage.warning('请先查询数据')
    return
  }
  
  try {
    const wb = XLSX.utils.book_new()
    
    projectReportData.value.forEach((report, index) => {
      const sheetData = []
      
      // 项目基本信息
      sheetData.push(['项目报表'])
      sheetData.push(['项目名称', report.projectName])
      sheetData.push(['项目编号', report.projectCode])
      sheetData.push(['项目经理', report.projectManager || '未指定'])
      sheetData.push(['项目状态', report.status])
      sheetData.push(['预算金额', formatCurrency(report.budget)])
      sheetData.push(['预算使用率', formatCurrency(report.budgetUtilization) + '%'])
      sheetData.push([])
      
      // 统计信息
      sheetData.push(['统计信息'])
      sheetData.push(['采购订单数量', report.purchaseOrderCount || 0])
      sheetData.push(['采购订单金额', formatCurrency(report.purchaseAmount)])
      sheetData.push(['销售订单数量', report.salesOrderCount || 0])
      sheetData.push(['销售订单金额', formatCurrency(report.salesAmount)])
      sheetData.push(['总支出', formatCurrency(report.totalExpense)])
      sheetData.push(['总收入', formatCurrency(report.totalIncome)])
      sheetData.push(['净利润', formatCurrency(report.netProfit)])
      sheetData.push([])
      
      // 采购订单明细
      if (report.purchaseOrders && report.purchaseOrders.length > 0) {
        sheetData.push(['采购订单明细'])
        sheetData.push(['订单编号', '供应商', '订单金额', '状态', '创建时间'])
        report.purchaseOrders.forEach(order => {
          sheetData.push([
            order.orderNo,
            order.supplierName || '',
            formatCurrency(order.amount),
            order.status,
            formatDate(order.createTime, 'YYYY-MM-DD')
          ])
        })
        sheetData.push([])
      }
      
      // 销售订单明细
      if (report.salesOrders && report.salesOrders.length > 0) {
        sheetData.push(['销售订单明细'])
        sheetData.push(['订单编号', '客户', '订单金额', '状态', '创建时间'])
        report.salesOrders.forEach(order => {
          sheetData.push([
            order.orderNo,
            order.customerName || '',
            formatCurrency(order.amount),
            order.status,
            formatDate(order.createTime, 'YYYY-MM-DD')
          ])
        })
      }
      
      const ws = XLSX.utils.aoa_to_sheet(sheetData)
      const sheetName = report.projectName || `项目${index + 1}`
      XLSX.utils.book_append_sheet(wb, ws, sheetName.substring(0, 31)) // Excel工作表名称最多31个字符
    })
    
    const fileName = `项目报表_${projectReportParams.value.startDate || '全部'}_${projectReportParams.value.endDate || '全部'}`
    XLSX.writeFile(wb, `${fileName}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 计算负债和所有者权益总计
const getTotalLiabilitiesAndEquity = () => {
  if (!balanceSheetData.value) return 0
  const liabilities = Number(balanceSheetData.value.totalLiabilities) || 0
  const equity = Number(balanceSheetData.value.totalEquity) || 0
  return liabilities + equity
}

// 导出Excel工具函数
const exportToExcel = (data, headers, fileName) => {
  if (!data || data.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }

  try {
    // 准备表头
    const worksheetData = [headers]
    
    // 准备数据行
    data.forEach(row => {
      const rowData = headers.map(header => {
        const value = row[header.key]
        return value !== null && value !== undefined ? value : ''
      })
      worksheetData.push(rowData)
    })
    
    // 创建工作簿和工作表
    const ws = XLSX.utils.aoa_to_sheet(worksheetData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
    
    // 设置列宽
    const colWidths = headers.map(() => ({ wch: 15 }))
    ws['!cols'] = colWidths
    
    // 导出文件
    XLSX.writeFile(wb, `${fileName}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 导出科目余额表
const exportAccountBalance = () => {
  if (accountBalanceData.value.length === 0) {
    ElMessage.warning('请先查询数据')
    return
  }
  
  try {
    const headers = [['科目编码', '科目名称', '科目类型', '期初余额', '本期借方', '本期贷方', '期末余额']]
    const data = accountBalanceData.value.map(item => [
      item.subjectCode,
      item.subjectName,
      item.subjectType,
      formatCurrency(item.beginBalance),
      formatCurrency(item.debitAmount),
      formatCurrency(item.creditAmount),
      formatCurrency(item.endBalance)
    ])
    
    const ws = XLSX.utils.aoa_to_sheet([...headers, ...data])
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '科目余额表')
    
    // 设置列宽
    ws['!cols'] = [
      { wch: 15 }, // 科目编码
      { wch: 20 }, // 科目名称
      { wch: 12 }, // 科目类型
      { wch: 15 }, // 期初余额
      { wch: 15 }, // 本期借方
      { wch: 15 }, // 本期贷方
      { wch: 15 }  // 期末余额
    ]
    
    const fileName = `科目余额表_${accountBalanceParams.value.startDate}_${accountBalanceParams.value.endDate}`
    XLSX.writeFile(wb, `${fileName}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 导出利润表
const exportProfitLoss = () => {
  if (!profitLossData.value) {
    ElMessage.warning('请先查询数据')
    return
  }
  
  try {
    const wb = XLSX.utils.book_new()
    
    // 第一张表：汇总数据
    const summaryData = [
      ['利润表', profitLossData.value.period],
      [],
      ['项目', '金额'],
      ['营业收入', formatCurrency(profitLossData.value.revenue)],
      ['营业成本', formatCurrency(profitLossData.value.costOfSales)],
      ['毛利润', formatCurrency(profitLossData.value.grossProfit)],
      ['营业费用', formatCurrency(profitLossData.value.operatingExpenses)],
      ['营业利润', formatCurrency(profitLossData.value.operatingProfit)],
      ['净利润', formatCurrency(profitLossData.value.netProfit)]
    ]
    const ws1 = XLSX.utils.aoa_to_sheet(summaryData)
    XLSX.utils.book_append_sheet(wb, ws1, '利润汇总')
    
    // 第二张表：明细数据
    if (profitLossData.value.items && profitLossData.value.items.length > 0) {
      const detailHeaders = [['科目编码', '科目名称', '项目类型', '金额']]
      const detailData = profitLossData.value.items.map(item => [
        item.subjectCode,
        item.subjectName,
        item.itemType,
        formatCurrency(item.amount)
      ])
      const ws2 = XLSX.utils.aoa_to_sheet([...detailHeaders, ...detailData])
      XLSX.utils.book_append_sheet(wb, ws2, '明细项')
    }
    
    const fileName = `利润表_${profitLossParams.value.startDate}_${profitLossParams.value.endDate}`
    XLSX.writeFile(wb, `${fileName}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 导出资产负债表
const exportBalanceSheet = () => {
  if (!balanceSheetData.value) {
    ElMessage.warning('请先查询数据')
    return
  }
  
  try {
    const wb = XLSX.utils.book_new()
    
    // 资产表
    const assetHeaders = [['科目编码', '科目名称', '分类', '余额']]
    const assetData = balanceSheetData.value.assets.map(item => [
      item.subjectCode,
      item.subjectName,
      item.category,
      formatCurrency(item.balance)
    ])
    assetData.push(['', '', '资产合计', formatCurrency(balanceSheetData.value.totalAssets)])
    const ws1 = XLSX.utils.aoa_to_sheet([...assetHeaders, ...assetData])
    XLSX.utils.book_append_sheet(wb, ws1, '资产')
    
    // 负债表
    const liabilityHeaders = [['科目编码', '科目名称', '分类', '余额']]
    const liabilityData = balanceSheetData.value.liabilities.map(item => [
      item.subjectCode,
      item.subjectName,
      item.category,
      formatCurrency(item.balance)
    ])
    liabilityData.push(['', '', '负债合计', formatCurrency(balanceSheetData.value.totalLiabilities)])
    const ws2 = XLSX.utils.aoa_to_sheet([...liabilityHeaders, ...liabilityData])
    XLSX.utils.book_append_sheet(wb, ws2, '负债')
    
    // 所有者权益表
    const equityHeaders = [['科目编码', '科目名称', '分类', '余额']]
    const equityData = balanceSheetData.value.equity.map(item => [
      item.subjectCode,
      item.subjectName,
      item.category,
      formatCurrency(item.balance)
    ])
    equityData.push(['', '', '所有者权益合计', formatCurrency(balanceSheetData.value.totalEquity)])
    const total = Number(balanceSheetData.value.totalLiabilities) + Number(balanceSheetData.value.totalEquity)
    equityData.push(['', '', '负债和所有者权益总计', formatCurrency(total)])
    const ws3 = XLSX.utils.aoa_to_sheet([...equityHeaders, ...equityData])
    XLSX.utils.book_append_sheet(wb, ws3, '所有者权益')
    
    const fileName = `资产负债表_${balanceSheetParams.value.reportDate}`
    XLSX.writeFile(wb, `${fileName}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 导出现金流量表
const exportCashFlow = () => {
  if (!cashFlowData.value || !cashFlowData.value.items || cashFlowData.value.items.length === 0) {
    ElMessage.warning('请先查询数据')
    return
  }

  try {
    const wb = XLSX.utils.book_new()

    const headers = [['项目', '类别', '金额']]
    const data = cashFlowData.value.items.map(item => [
      item.itemName,
      item.category,
      formatCurrency(item.amount)
    ])
    data.push(['经营活动净额', '', formatCurrency(cashFlowData.value.netOperating)])
    data.push(['投资活动净额', '', formatCurrency(cashFlowData.value.netInvesting)])
    data.push(['筹资活动净额', '', formatCurrency(cashFlowData.value.netFinancing)])
    data.push(['现金净增加额', '', formatCurrency(cashFlowData.value.netIncrease)])

    const ws = XLSX.utils.aoa_to_sheet([...headers, ...data])
    XLSX.utils.book_append_sheet(wb, ws, '现金流量表')

    ws['!cols'] = [
      { wch: 26 },
      { wch: 12 },
      { wch: 16 }
    ]

    const fileName = `现金流量表_${cashFlowParams.value.startDate}_${cashFlowParams.value.endDate}`
    XLSX.writeFile(wb, `${fileName}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// ========== 图表渲染函数 ==========

// 渲染科目余额分布图（柱状图）
const renderAccountBalanceChart = () => {
  if (!accountBalanceChartRef.value || accountBalanceData.value.length === 0) return
  
  if (accountBalanceChart) {
    accountBalanceChart.dispose()
  }
  
  accountBalanceChart = echarts.init(accountBalanceChartRef.value)
  
  // 取前10个余额最大的科目
  const sortedData = [...accountBalanceData.value]
    .filter(item => Math.abs(item.endBalance) > 0)
    .sort((a, b) => Math.abs(b.endBalance) - Math.abs(a.endBalance))
    .slice(0, 10)
  
  const option = {
    title: {
      text: '科目余额TOP10',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const param = params[0]
        return `${param.name}<br/>${param.seriesName}: ${formatCurrency(param.value)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedData.map(item => item.subjectName),
      axisLabel: {
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => {
          if (Math.abs(value) >= 10000) {
            return (value / 10000).toFixed(1) + '万'
          }
          return value.toFixed(0)
        }
      }
    },
    series: [{
      name: '期末余额',
      type: 'bar',
      data: sortedData.map(item => item.endBalance),
      itemStyle: {
        color: (params) => {
          return params.value >= 0 ? '#409eff' : '#f56c6c'
        }
      }
    }]
  }
  
  accountBalanceChart.setOption(option)
}

// 渲染科目类型占比图（饼图）
const renderAccountTypeChart = () => {
  if (!accountTypeChartRef.value || accountBalanceData.value.length === 0) return
  
  if (accountTypeChart) {
    accountTypeChart.dispose()
  }
  
  accountTypeChart = echarts.init(accountTypeChartRef.value)
  
  // 按科目类型统计余额
  const typeMap = {}
  accountBalanceData.value.forEach(item => {
    const type = item.subjectType || '其他'
    if (!typeMap[type]) {
      typeMap[type] = 0
    }
    typeMap[type] += Math.abs(item.endBalance)
  })
  
  const data = Object.keys(typeMap).map(type => ({
    name: type,
    value: typeMap[type]
  }))
  
  const option = {
    title: {
      text: '科目类型余额占比',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [{
      name: '余额',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      data: data
    }]
  }
  
  accountTypeChart.setOption(option)
}

// 渲染利润表图表（柱状图）
const renderProfitLossChart = () => {
  if (!profitLossChartRef.value || !profitLossData.value) return
  
  if (profitLossChart) {
    profitLossChart.dispose()
  }
  
  profitLossChart = echarts.init(profitLossChartRef.value)
  
  const option = {
    title: {
      text: '利润结构分析',
      left: 'center',
      textStyle: { fontSize: 16 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        let result = params[0].name + '<br/>'
        params.forEach(param => {
          result += `${param.seriesName}: ${formatCurrency(param.value)}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['营业收入', '营业成本', '毛利润', '营业费用', '营业利润', '净利润'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['金额']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => {
          if (Math.abs(value) >= 10000) {
            return (value / 10000).toFixed(1) + '万'
          }
          return value.toFixed(0)
        }
      }
    },
    series: [
      {
        name: '营业收入',
        type: 'bar',
        data: [profitLossData.value.revenue || 0],
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '营业成本',
        type: 'bar',
        data: [profitLossData.value.costOfSales || 0],
        itemStyle: { color: '#f56c6c' }
      },
      {
        name: '毛利润',
        type: 'bar',
        data: [profitLossData.value.grossProfit || 0],
        itemStyle: { color: '#409eff' }
      },
      {
        name: '营业费用',
        type: 'bar',
        data: [profitLossData.value.operatingExpenses || 0],
        itemStyle: { color: '#e6a23c' }
      },
      {
        name: '营业利润',
        type: 'bar',
        data: [profitLossData.value.operatingProfit || 0],
        itemStyle: { color: '#909399' }
      },
      {
        name: '净利润',
        type: 'bar',
        data: [profitLossData.value.netProfit || 0],
        itemStyle: { color: '#606266' }
      }
    ]
  }
  
  profitLossChart.setOption(option)
}

// 渲染资产负债表图表（饼图）
const renderBalanceSheetChart = () => {
  if (!balanceSheetChartRef.value || !balanceSheetData.value) return
  
  if (balanceSheetChart) {
    balanceSheetChart.dispose()
  }
  
  balanceSheetChart = echarts.init(balanceSheetChartRef.value)
  
  const option = {
    title: {
      text: '资产、负债、权益结构',
      left: 'center',
      textStyle: { fontSize: 16 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [{
      name: '金额',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      data: [
        {
          value: Math.abs(balanceSheetData.value.totalAssets || 0),
          name: '资产',
          itemStyle: { color: '#409eff' }
        },
        {
          value: Math.abs(balanceSheetData.value.totalLiabilities || 0),
          name: '负债',
          itemStyle: { color: '#f56c6c' }
        },
        {
          value: Math.abs(balanceSheetData.value.totalEquity || 0),
          name: '所有者权益',
          itemStyle: { color: '#67c23a' }
        }
      ]
    }]
  }
  
  balanceSheetChart.setOption(option)
}

// 渲染现金流量表图表（柱状图）
const renderCashFlowChart = () => {
  if (!cashFlowChartRef.value || !cashFlowData.value) return
  
  if (cashFlowChart) {
    cashFlowChart.dispose()
  }
  
  cashFlowChart = echarts.init(cashFlowChartRef.value)
  
  const option = {
    title: {
      text: '现金流量分析',
      left: 'center',
      textStyle: { fontSize: 16 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        let result = params[0].name + '<br/>'
        params.forEach(param => {
          result += `${param.seriesName}: ${formatCurrency(param.value)}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['经营活动', '投资活动', '筹资活动', '净增加额'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['金额']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => {
          if (Math.abs(value) >= 10000) {
            return (value / 10000).toFixed(1) + '万'
          }
          return value.toFixed(0)
        }
      }
    },
    series: [
      {
        name: '经营活动',
        type: 'bar',
        data: [cashFlowData.value.netOperating || 0],
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '投资活动',
        type: 'bar',
        data: [cashFlowData.value.netInvesting || 0],
        itemStyle: { color: '#409eff' }
      },
      {
        name: '筹资活动',
        type: 'bar',
        data: [cashFlowData.value.netFinancing || 0],
        itemStyle: { color: '#e6a23c' }
      },
      {
        name: '净增加额',
        type: 'bar',
        data: [cashFlowData.value.netIncrease || 0],
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
  
  cashFlowChart.setOption(option)
}
</script>

<style scoped>
.report-page {
  padding: 20px;
}

.report-header {
  margin-bottom: 20px;
}

h3 {
  margin: 0 0 20px 0;
  color: #409eff;
}

h4 {
  margin: 0 0 15px 0;
  color: #606266;
}
</style>

