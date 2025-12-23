import request from '@/utils/request'

// 新增采购订单
export function addPurchaseOrder(data) {
  return request({
    url: '/accounting/purchaseOrder/add',
    method: 'post',
    data
  })
}

// 分页查询采购订单列表
export function getPurchaseOrderList(params) {
  return request({
    url: '/accounting/purchaseOrder/list',
    method: 'get',
    params
  })
}

// 查询采购订单详情
export function getPurchaseOrderDetail(id) {
  return request({
    url: `/accounting/purchaseOrder/detail/${id}`,
    method: 'get'
  })
}

// 修改采购订单
export function updatePurchaseOrder(data) {
  return request({
    url: '/accounting/purchaseOrder/update',
    method: 'put',
    data
  })
}

// 删除采购订单
export function deletePurchaseOrder(id) {
  return request({
    url: `/accounting/purchaseOrder/delete/${id}`,
    method: 'delete'
  })
}

// 批量更新采购订单状态
export function batchUpdatePurchaseOrderStatus(params) {
  return request({
    url: '/accounting/purchaseOrder/batchChangeStatus',
    method: 'put',
    params
  })
}

// 更新单个采购订单状态
export function updatePurchaseOrderStatus(id, status) {
  return request({
    url: '/accounting/purchaseOrder/changeStatus',
    method: 'put',
    params: {
      id,
      status
    }
  })
}

// 根据订单ID查询采购订单项
export function getPurchaseOrderLines(orderId) {
  return request({
    url: `/accounting/purchaseOrderLine/list/${orderId}`,
    method: 'get'
  })
}

// 新增采购订单项
export function addPurchaseOrderLine(data) {
  return request({
    url: '/accounting/purchaseOrderLine/add',
    method: 'post',
    data
  })
}

// 批量新增采购订单项
export function batchAddPurchaseOrderLines(data) {
  return request({
    url: '/accounting/purchaseOrderLine/batchAdd',
    method: 'post',
    data
  })
}