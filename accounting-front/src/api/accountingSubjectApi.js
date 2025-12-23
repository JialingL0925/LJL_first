import request from '@/utils/request'

// 会计科目API（保持原路径，仅调整数据提取）
export default {
  // 分页查询
  pageList(current, size, params) {
    return request({
      url: '/accounting/accountingSubject/page', // 保持原路径
      method: 'get',
      params: { current, size, ...params }
    }).then(response => {
      // 适配后端格式：{success: true, data: {records: [], total: 1}}
      if (response.success && response.data) {
        return {
          records: response.data.records || [],
          total: response.data.total || 0
        }
      }
      return { records: [], total: 0 }
    }).catch(error => {
      console.error('分页查询接口异常:', error)
      return { records: [], total: 0 }
    })
  },

  // 树形查询
  treeList(parentSubjectId = 0) {
    return request({
      url: '/accounting/accountingSubject/tree', // 保持原路径
      method: 'get',
      params: { parentSubjectId }
    }).then(response => {
      if (response.success && response.data) {
        return response.data || []
      }
      return []
    }).catch(error => {
      console.error('树形查询接口异常:', error)
      return []
    })
  },

  // 新增
  save(data) {
    return request({
      url: '/accounting/accountingSubject', // 保持原路径
      method: 'post',
      data
    }).then(response => {
      return response // 直接返回{success: true/false, message: ''}
    }).catch(error => {
      console.error('新增接口异常:', error)
      return { success: false, message: '网络异常' }
    })
  },

  // 修改
  update(data) {
    return request({
      url: '/accounting/accountingSubject', // 保持原路径
      method: 'put',
      data
    }).then(response => {
      return response
    }).catch(error => {
      console.error('修改接口异常:', error)
      return { success: false, message: '网络异常' }
    })
  },

  // 切换状态
  changeStatus(subjectId, status, deactivationReason) {
    return request({
      url: '/accounting/accountingSubject/changeStatus', // 保持原路径
      method: 'put',
      params: { subjectId, status, deactivationReason }
    }).then(response => {
      return response
    }).catch(error => {
      console.error('状态切换接口异常:', error)
      return { success: false, message: '网络异常' }
    })
  },

  // 根据ID查询
  getById(id) {
    return request({
      url: `/accounting/accountingSubject/${id}`, // 保持原路径
      method: 'get'
    }).then(response => {
      if (response.success && response.data) {
        return response.data || null
      }
      return null
    }).catch(error => {
      console.error('详情查询接口异常:', error)
      return null
    })
  }
}