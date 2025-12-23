import request from '@/utils/request'

// 分页查询应付单列表
export function getApPayableList(params) {
  return request({
    url: '/accounting/apPayable/list',
    method: 'get',
    params
  })
}

// 查询应付单详情
export function getApPayableDetail(id) {
  return request({
    url: `/accounting/apPayable/detail/${id}`,
    method: 'get'
  })
}

// 根据订单ID查询应付单
export function getApPayableByOrderId(orderId) {
  return request({
    url: `/accounting/apPayable/byOrderId/${orderId}`,
    method: 'get'
  })
}

// 根据供应商ID查询应付单
export function getApPayableBySupplierId(supplierId) {
  return request({
    url: `/accounting/apPayable/bySupplierId/${supplierId}`,
    method: 'get'
  })
}

// 批量更新应付单状态
export function batchUpdateApPayableStatus(params) {
  return request({
    url: '/accounting/apPayable/batchUpdateStatus',
    method: 'put',
    params
  })
}

// 分页查询付款单列表
export function getApPaymentList(params) {
  return request({
    url: '/accounting/apPayment/list',
    method: 'get',
    params
  })
}

// 查询付款单详情
export function getApPaymentDetail(id) {
  return request({
    url: `/accounting/apPayment/detail/${id}`,
    method: 'get'
  })
}

// 创建付款单
export function createApPayment(data) {
  return request({
    url: '/accounting/apPayment/create',
    method: 'post',
    data
  })
}

// 确认付款单
export function confirmApPayment(id, userId) {
  return request({
    url: `/accounting/apPayment/confirm/${id}`,
    method: 'put',
    params: { userId }
  })
}

// 取消付款单
export function cancelApPayment(id) {
  return request({
    url: `/accounting/apPayment/cancel/${id}`,
    method: 'put'
  })
}

// 统计未付款订单数量
export function countUnpaidPayables() {
  return request({
    url: '/accounting/apPayable/countUnpaid',
    method: 'get'
  })
}