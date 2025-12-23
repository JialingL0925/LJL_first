import request from '@/utils/request'

// 分页查询应收单列表
export function getArReceivableList(params) {
  return request({
    url: '/accounting/arReceivable/list',
    method: 'get',
    params
  })
}

// 查询应收单详情
export function getArReceivableDetail(id) {
  return request({
    url: `/accounting/arReceivable/detail/${id}`,
    method: 'get'
  })
}

// 分页查询收款单列表
export function getArPaymentList(params) {
  return request({
    url: '/accounting/arPayment/list',
    method: 'get',
    params
  })
}

// 查询收款单详情
export function getArPaymentDetail(id) {
  return request({
    url: `/accounting/arPayment/detail/${id}`,
    method: 'get'
  })
}

// 创建收款单
export function createArPayment(data) {
  return request({
    url: '/accounting/arPayment/create',
    method: 'post',
    data
  })
}

// 确认收款单
export function confirmArPayment(id, userId) {
  return request({
    url: `/accounting/arPayment/confirm/${id}`,
    method: 'put',
    params: { userId }
  })
}

// 取消收款单
export function cancelArPayment(id) {
  return request({
    url: `/accounting/arPayment/cancel/${id}`,
    method: 'put'
  })
}

// 统计未收款订单数量
export function countUnpaidReceivables() {
  return request({
    url: '/accounting/arReceivable/countUnpaid',
    method: 'get'
  })
}