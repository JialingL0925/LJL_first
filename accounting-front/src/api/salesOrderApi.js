import request from '@/utils/request'

// 新增销售订单
export function addSalesOrder(data) {
  return request({
    url: '/accounting/salesOrder/add',
    method: 'post',
    data
  })
}

// 分页查询销售订单列表
export function getSalesOrderList(params) {
  return request({
    url: '/accounting/salesOrder/list',
    method: 'get',
    params
  })
}

// 查询销售订单详情
export function getSalesOrderDetail(id) {
  return request({
    url: `/accounting/salesOrder/detail/${id}`,
    method: 'get'
  })
}

// 修改销售订单
export function updateSalesOrder(data) {
  return request({
    url: '/accounting/salesOrder/update',
    method: 'put',
    data
  })
}

// 审核销售订单
export function auditSalesOrder(params) {
  return request({
    url: '/accounting/salesOrder/audit',
    method: 'put',
    params
  })
}

// 批量更新销售订单状态
export function batchUpdateSalesOrderStatus(params) {
  return request({
    url: '/accounting/salesOrder/batchUpdateStatus',
    method: 'put',
    params
  })
}

// 根据订单编号查询销售订单
export function getSalesOrderByOrderNo(orderNo) {
  return request({
    url: `/accounting/salesOrder/byOrderNo/${orderNo}`,
    method: 'get'
  })
}