import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: 'http://175.178.218.28:8080/accounting', // 云服务器地址（直接访问后端8080端口）
  timeout: 5000
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    // 登录请求无需携带token
    if (config.url?.includes('/user/login')) {
      delete config.headers['Authorization']
    }
    // 检查请求地址是否为空
    if (!config.url) {
      ElMessage.error('请求地址为空')
      return Promise.reject(new Error('请求地址为空'))
    }
    return config
  },
  (error) => {
    ElMessage.error('请求异常，请重试')
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 递归处理BigDecimal类型，将其转换为数字
    function handleBigDecimal(obj) {
      if (obj === null || obj === undefined) {
        return obj
      }
      // 处理字符串类型的数字
      if (typeof obj === 'string') {
        // 移除可能的逗号分隔符和非数字字符
        obj = obj.replace(/[^0-9.]/g, '')
        return !isNaN(parseFloat(obj)) ? parseFloat(obj) : obj
      }
      // 处理BigDecimal对象（通常具有scale、precision等属性）
      if (typeof obj === 'object' && obj.scale !== undefined && obj.precision !== undefined) {
        // 不同序列化库可能有不同的属性名，尝试从常见属性中获取值
        const value = obj.value || obj.unscaledValue || obj
        return parseFloat(value)
      }
      // 递归处理数组
      if (Array.isArray(obj)) {
        return obj.map(item => handleBigDecimal(item))
      }
      // 递归处理普通对象
      if (typeof obj === 'object') {
        const result = {}
        for (const key in obj) {
          if (obj.hasOwnProperty(key)) {
            result[key] = handleBigDecimal(obj[key])
          }
        }
        return result
      }
      // 处理其他类型
      return obj
    }
    
    // 容错：响应数据为空的情况
    if (!res) {
      ElMessage.error('响应数据为空')
      return Promise.reject(new Error('响应数据为空'))
    }
    
    // 处理响应数据中的BigDecimal类型
    const processedRes = handleBigDecimal(res)
    
    // 根据后端实际返回的ResultVO格式判断
    if (res.code !== undefined) {
      // 业务成功：code为200
      if (res.code === 200) {
        return res
      }
      // 业务失败：提示错误信息并进入catch
      else {
        ElMessage.error(res.msg || '操作失败')
        return Promise.reject(new Error(res.msg || '操作失败'))
      }
    }
    // 兼容其他可能的响应格式
    return res
  },
  (error) => {
    // 网络或HTTP错误处理
    if (error.response) {
      ElMessage.error(`服务器错误：${error.response.status}，${error.message}`)
    } else {
      ElMessage.error('网络异常，请检查后端服务是否启动')
    }
    return Promise.reject(error)
  }
)

export default service