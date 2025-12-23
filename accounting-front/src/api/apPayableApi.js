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