import request from '@/utils/request'

// 生成科目余额表
export function getAccountBalance(params) {
  return request({
    url: '/accounting/report/accountBalance',
    method: 'get',
    params
  })
}

// 生成利润表
export function getProfitLoss(params) {
  return request({
    url: '/accounting/report/profitLoss',
    method: 'get',
    params
  })
}

// 生成资产负债表
export function getBalanceSheet(params) {
  return request({
    url: '/accounting/report/balanceSheet',
    method: 'get',
    params
  })
}

// 生成现金流量表
export function getCashFlow(params) {
  return request({
    url: '/accounting/report/cashFlow',
    method: 'get',
    params
  })
}

// 生成项目报表
export function getProjectReport(params) {
  return request({
    url: '/accounting/report/projectReport',
    method: 'get',
    params
  })
}





