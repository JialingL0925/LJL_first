import request from '@/utils/request'

// 新增项目
export function addProject(data) {
  return request({
    url: '/accounting/project/add',
    method: 'post',
    data
  })
}

// 分页查询项目列表
export function getProjectList(params) {
  return request({
    url: '/accounting/project/list',
    method: 'get',
    params
  })
}

// 查询项目详情
export function getProjectDetail(projectId) {
  return request({
    url: `/accounting/project/detail/${projectId}`,
    method: 'get'
  })
}

// 修改项目
export function updateProject(data) {
  return request({
    url: '/accounting/project/update',
    method: 'put',
    data
  })
}

// 删除项目
export function deleteProject(projectId) {
  return request({
    url: `/accounting/project/delete/${projectId}`,
    method: 'delete'
  })
}

// 获取所有项目列表（用于下拉框）
export function getAllProjects() {
  return request({
    url: '/accounting/project/all',
    method: 'get'
  })
}

// 切换项目状态
export function changeProjectStatus(projectId, status) {
  return request({
    url: '/accounting/project/changeStatus',
    method: 'put',
    params: { projectId, status }
  })
}

