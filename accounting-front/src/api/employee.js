import request from '@/utils/request'

// 分页查询员工列表
export function getEmployeeList(params) {
  return request({
    url: '/accounting/employee/list',
    method: 'get',
    params // params：pageNum/pageSize/name/phone/status
  })
}

// 获取所有角色列表
export function getAllRoles() {
  return request({
    url: '/accounting/employee/roles',
    method: 'get'
  })
}

// 新增员工
export function addEmployee(data) {
  return request({
    url: '/accounting/employee/add',
    method: 'post',
    data // data字段名：name/phone/password/roleIdStr/status
  })
}

// 修改员工信息
export function updateEmployee(data) {
  return request({
    url: '/accounting/employee/update',
    method: 'put',
    data // data字段名：employeeId/name/phone/password/roleIdStr/status
  })
}

// 删除员工（软删除，设置为停用）
export function deleteEmployee(employeeId) {
  return request({
    url: `/accounting/employee/delete/${employeeId}`,
    method: 'delete'
  })
}

// 查询员工详情
export function getEmployeeDetail(employeeId) {
  return request({
    url: `/accounting/employee/detail/${employeeId}`,
    method: 'get'
  })
}

