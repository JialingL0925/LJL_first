<template>
  <!-- 模板部分不变，保持之前美化后的样式 -->
  <div class="supplier-page">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline @submit.prevent="getList">
        <el-form-item label="供应商名称">
          <el-input
            v-model="queryParams.supplierName"
            placeholder="请输入供应商名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input
            v-model="queryParams.contactPerson"
            placeholder="请输入联系人"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="btn-search" @click="getList">查询</el-button>
          <el-button class="btn-reset" @click="resetQuery">重置</el-button>
          <el-button type="success" class="btn-add" @click="openDialog">新增供应商</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never" style="margin-top: 10px">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <span class="title-main">供应商列表</span>
            <span class="title-sub">共 <span class="title-number">{{ total }}</span> 家供应商</span>
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
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="供应商名称" prop="supplierName" min-width="150" />
        <el-table-column label="联系人" prop="contactPerson" width="100" />
        <el-table-column label="联系电话" prop="phone" width="120" />
        <el-table-column label="营业执照编号" prop="businessLicense" width="180" />
        
        <el-table-column label="开户银行" width="150">
          <template #default="scope">
            {{ scope.row.bankName || '未填写' }}
          </template>
        </el-table-column>
        
        <el-table-column label="银行账号" width="200">
          <template #default="scope">
            {{ scope.row.bankAccount || '未填写' }}
          </template>
        </el-table-column>

        <!-- 状态列：严格判断数值类型 -->
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="scope">
            <el-tag 
              :type="scope.row.status === 1 ? 'success' : 'danger'" 
              size="small" 
              style="padding: 4px 10px; border-radius: 4px"
            >
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 操作列：严格判断数值类型 -->
        <!-- 操作列：宽度加大，保证三颗按钮在一行内展示 -->
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
                :class="['btn-op', 'btn-status', scope.row.status === 1 ? 'btn-disable' : 'btn-enable']"
                @click="() => changeStatusHandle(scope.row, scope.row.status === 1 ? 0 : 1)"
                :loading="scope.row.statusLoading"
                icon="el-icon-refresh-left"
              >
                {{ scope.row.status === 1 ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
        style="margin-top: 20px; text-align: right; padding: 10px 0"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
      style="border-radius: 8px"
    >
      <el-form
        ref="addFormRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        style="padding: 10px 0"
      >
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="formData.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="formData.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业执照编号" prop="businessLicense">
          <el-input v-model="formData.businessLicense" placeholder="请输入营业执照编号" />
        </el-form-item>
        <el-form-item label="开户银行" prop="bankName">
          <el-input v-model="formData.bankName" placeholder="请输入开户银行" />
        </el-form-item>
        <el-form-item label="银行账号" prop="bankAccount">
          <el-input v-model="formData.bankAccount" placeholder="请输入银行账号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" style="border-radius: 4px">取消</el-button>
        <el-button type="primary" @click="submitForm" style="border-radius: 4px">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
import {
  addSupplier,
  getSupplierList,
  getSupplierDetail,
  updateSupplier,
  deleteSupplier as deleteSupplierApi,
  changeSupplierStatus
} from '@/api/supplier'

// 加载状态
const loading = ref(false)
// 表格数据
const tableData = ref([])
// 分页总数
const total = ref(0)
// 表单引用
const addFormRef = ref(null)
// 弹窗显示状态
const dialogVisible = ref(false)
// 弹窗标题
const dialogTitle = ref('新增供应商')
// 操作类型（add/edit）
const actionType = ref('add')

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  supplierName: '',
  contactPerson: ''
})

// 表单数据（匹配后端驼峰字段）
const formData = reactive({
  id: '',
  supplierName: '',
  contactPerson: '',
  phone: '',
  businessLicense: '',
  bankName: '',
  bankAccount: '',
  status: '1'
})

// 表单校验规则
const formRules = reactive({
  supplierName: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ],
  businessLicense: [
    { required: true, message: '请输入营业执照编号', trigger: 'blur' }
  ],
  bankName: [
    { required: true, message: '请输入开户银行', trigger: 'blur' }
  ],
  bankAccount: [
    { required: true, message: '请输入银行账号', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

// 初始化加载列表
onMounted(() => {
  getList()
})

// 获取供应商列表：终极修复 - 正确处理0的情况
const getList = async () => {
  loading.value = true
  try {
    const res = await getSupplierList(queryParams)
    console.log('【列表接口】后端返回原始数据：', res)
    const result = res.data || res
    
    // 映射后端驼峰字段到表格，状态赋值逻辑修复（关键！）
    tableData.value = (result.records || []).map(item => {
      // 打印原始状态，确认后端返回值
      console.log('【原始状态】id=', item.id, 'status=', item.status, '类型=', typeof item.status);
      // 正确逻辑：只有status为undefined/null时才取1，0/1都保留原值
      const status = item.status === undefined || item.status === null ? 1 : Number(item.status);
      return {
        id: item.id,
        supplierName: item.supplierName || '',
        contactPerson: item.contactPerson || '',
        phone: item.phone || '',
        businessLicense: item.businessLicense || '',
        bankName: item.bankName,
        bankAccount: item.bankAccount,
        status: status, // 保留0/1的原始值
        statusLoading: false
      }
    })
    
    total.value = result.total || 0
    console.log('【处理后状态】', tableData.value.map(item => ({id: item.id, status: item.status})));
  } catch (error) {
    console.error('【列表接口】获取失败：', error)
    ElMessage.error('获取供应商列表失败：' + (error.response?.data?.msg || error.message || '网络异常'));
  } finally {
    loading.value = false;
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.pageNum = 1
  queryParams.supplierName = ''
  queryParams.contactPerson = ''
  getList()
}

// 打开新增弹窗
const openDialog = () => {
  actionType.value = 'add'
  dialogTitle.value = '新增供应商'
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (addFormRef.value) {
    addFormRef.value.resetFields()
  }
  formData.id = ''
  formData.supplierName = ''
  formData.contactPerson = ''
  formData.phone = ''
  formData.businessLicense = ''
  formData.bankName = ''
  formData.bankAccount = ''
  formData.status = '1'
}

// 编辑供应商：修复状态回显
const handleEdit = async (row) => {
  actionType.value = 'edit'
  dialogTitle.value = '编辑供应商'
  resetForm()
  try {
    const res = await getSupplierDetail(row.id)
    console.log('【详情接口】返回数据：', res)
    const detail = res.data || res
    
    Object.assign(formData, {
      id: detail.id || '',
      supplierName: detail.supplierName || '',
      contactPerson: detail.contactPerson || '',
      phone: detail.phone || '',
      businessLicense: detail.businessLicense || '',
      bankName: detail.bankName || '',
      bankAccount: detail.bankAccount || '',
      status: String(detail.status) || '1'
    })
    
    dialogVisible.value = true
  } catch (error) {
    console.error('【详情接口】获取失败：', error)
    ElMessage.error('获取供应商详情失败：' + (error.response?.data?.msg || error.message || '网络异常'))
  }
}

// 删除供应商
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该供应商吗？删除后不可恢复！',
      '温馨提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await deleteSupplierApi(row.id)
    console.log('【删除接口】返回结果：', res)
    ElMessage.success('删除供应商成功！')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('【删除接口】失败：', error)
      ElMessage.error('删除失败：' + (error.response?.data?.msg || error.message || '操作取消'))
    }
  }
}

// 切换供应商状态
const changeStatusHandle = async (row, targetStatus) => {
  row.statusLoading = true;
  const originalStatus = row.status;
  const rowIndex = tableData.value.findIndex(item => item.id === row.id);
  
  try {
    const res = await changeSupplierStatus(row.id, targetStatus);
    console.log('【状态切换】返回结果：', res);
    
    row.status = targetStatus;
    tableData.value.splice(rowIndex, 1, { ...row });
    
    ElMessage.success(res.msg || '状态切换成功！');
    setTimeout(() => {
      getList();
    }, 300);
  } catch (error) {
    row.status = originalStatus;
    tableData.value.splice(rowIndex, 1, { ...row });
    console.error('【状态切换】失败：', error);
    ElMessage.error('状态切换失败：' + (error.response?.data?.msg || error.message || '网络异常'));
  } finally {
    row.statusLoading = false;
    tableData.value.splice(rowIndex, 1, { ...row });
  }
}

// 提交表单
const submitForm = async () => {
  if (!addFormRef.value) return
  
  try {
    await addFormRef.value.validate()
    
    const submitData = {
      id: formData.id,
      supplierName: formData.supplierName.trim(),
      contactPerson: formData.contactPerson.trim(),
      phone: formData.phone.trim(),
      businessLicense: formData.businessLicense.trim(),
      bankName: formData.bankName.trim(),
      bankAccount: formData.bankAccount.trim(),
      status: Number(formData.status)
    }
    
    console.log(`【${actionType.value}接口】提交数据：`, submitData)
    
    let res
    if (actionType.value === 'add') {
      res = await addSupplier(submitData)
      ElMessage.success('新增供应商成功！')
    } else {
      res = await updateSupplier(submitData)
      ElMessage.success('编辑供应商成功！')
    }
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error(`【${actionType.value}接口】失败：`, error)
    ElMessage.error(
      actionType.value === 'add' 
        ? '新增失败：' + (error.response?.data?.msg || error.message || '服务器异常') 
        : '编辑失败：' + (error.response?.data?.msg || error.message || '服务器异常')
    )
  }
}

// 表格选择事件
</script>

<style scoped>
/* 统一页面容器 */
.supplier-page {
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

:deep(.el-form-item) {
  margin-bottom: 15px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}
</style>