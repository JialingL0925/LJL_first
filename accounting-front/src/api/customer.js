import request from '@/utils/request'

// 新增客户（参数与后端实体类字段一致）
export function addCustomer(data) {
  return request({
    url: '/accounting/customer/add',
    method: 'post',
    data // data字段名：customerName/phone/email等，与后端完全匹配
  })
}

// 分页查询客户列表（参数与后端接口一致）
export function getCustomerList(params) {
  return request({
    url: '/accounting/customer/list',
    method: 'get',
    params // params：pageNum/pageSize/customerName等
  })
}

// 查询客户详情（路径参数与后端一致）
export function getCustomerDetail(customerId) {
  return request({
    url: `/accounting/customer/detail/${customerId}`,
    method: 'get'
  })
}

// 修改客户（参数与后端实体类一致）
export function updateCustomer(data) {
  return request({
    url: '/accounting/customer/update',
    method: 'put',
    data
  })
}

// 删除客户（路径参数与后端一致）
export function deleteCustomer(customerId) {
  return request({
    url: `/accounting/customer/delete/${customerId}`,
    method: 'delete'
  })
}

// 切换客户状态（参数与后端接口一致）
export function changeCustomerStatus(params) {
  return request({
    url: '/accounting/customer/changeStatus',
    method: 'put',
    params // params：customerId/status
  })
}