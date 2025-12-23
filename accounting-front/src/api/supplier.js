import request from '@/utils/request'

/**
 * 新增供应商
 * @param {Object} data 供应商信息
 */
export function addSupplier(data) {
  return request({
    url: '/accounting/supplier/add', // 仅保留后端context-path+接口路径
    method: 'post',
    data
  })
}

/**
 * 分页查询供应商列表
 * @param {Object} params 查询参数
 */
export function getSupplierList(params) {
  return request({
    url: '/accounting/supplier/list',
    method: 'get',
    params
  })
}

/**
 * 查询供应商详情
 * @param {Number} id 供应商ID
 */
export function getSupplierDetail(id) {
  return request({
    url: `/accounting/supplier/detail/${id}`,
    method: 'get'
  })
}

/**
 * 修改供应商
 * @param {Object} data 供应商信息（含ID）
 */
export function updateSupplier(data) {
  return request({
    url: '/accounting/supplier/update',
    method: 'put',
    data
  })
}

/**
 * 删除供应商
 * @param {Number} id 供应商ID
 */
export function deleteSupplier(id) {
  return request({
    url: `/accounting/supplier/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 切换供应商状态
 * @param {Number} id 供应商ID
 * @param {Number} status 目标状态
 */
export function changeSupplierStatus(id, status) {
  return request({
    url: '/accounting/supplier/changeStatus',
    method: 'put',
    params: { id, status }
  })
}