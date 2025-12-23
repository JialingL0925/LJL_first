import request from '@/utils/request'

// 新增分录
export function addJournalEntry(data) {
  return request({
    url: '/accounting/journalEntry/add',
    method: 'post',
    data
  })
}

// 分页查询分录列表
export function getJournalEntryList(params) {
  return request({
    url: '/accounting/journalEntry/list',
    method: 'get',
    params
  })
}

// 查询分录详情
export function getJournalEntryDetail(id) {
  return request({
    url: `/accounting/journalEntry/detail/${id}`,
    method: 'get'
  })
}

// 修改分录
export function updateJournalEntry(data) {
  return request({
    url: '/accounting/journalEntry/update',
    method: 'put',
    data
  })
}

// 删除分录
export function deleteJournalEntry(id) {
  return request({
    url: `/accounting/journalEntry/delete/${id}`,
    method: 'delete'
  })
}

// 根据来源类型和ID查询分录
export function getJournalEntriesBySource(params) {
  return request({
    url: '/accounting/journalEntry/bySource',
    method: 'get',
    params
  })
}

// 批量新增分录
export function batchAddJournalEntries(data) {
  return request({
    url: '/accounting/journalEntry/batchAdd',
    method: 'post',
    data
  })
}

// 过账单个分录
export function postJournalEntry(id) {
  return request({
    url: '/accounting/journalEntry/post',
    method: 'post',
    params: { id }
  })
}

// 批量过账分录
export function batchPostJournalEntries(ids) {
  return request({
    url: '/accounting/journalEntry/batchPost',
    method: 'post',
    data: ids
  })
}