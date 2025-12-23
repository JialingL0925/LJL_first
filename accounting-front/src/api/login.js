import request from '@/utils/request'

export function userLogin(data) {
  // 核心：将 {phone: '', password: ''} 转为表单格式（适配后端@RequestParam）
  const params = new URLSearchParams()
  // 确保参数名和后端一致：phone（不是username）
  params.append('phone', data.phone)
  params.append('password', data.password)

  return request({
    url: '/user/login', // 拼接后最终路径：http://localhost:8080/accounting/user/login（匹配后端）
    method: 'post',
    data: params // 必须传URLSearchParams对象，否则axios会默认转JSON
  })
}

export function userRegister(data) {
  // 注册接口：将 {name: '', phone: '', password: '', roleId: ''} 转为表单格式
  const params = new URLSearchParams()
  params.append('name', data.name)
  params.append('phone', data.phone)
  params.append('password', data.password)
  params.append('roleId', data.roleId)

  return request({
    url: '/user/register',
    method: 'post',
    data: params
  })
}