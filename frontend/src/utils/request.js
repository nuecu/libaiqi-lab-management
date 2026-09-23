import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import dayjs from 'dayjs'

// ISO 日期时间（含 T）才格式化；纯日期 YYYY-MM-DD 与纯时间 HH:mm:ss 保持原样
const ISO_DT_RE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?(\.\d+)?Z?$/

const normalizeDates = (val) => {
  if (Array.isArray(val)) {
    val.forEach((v, i) => { val[i] = normalizeDates(v) })
    return val
  }
  if (val && typeof val === 'object') {
    Object.keys(val).forEach(k => { val[k] = normalizeDates(val[k]) })
    return val
  }
  if (typeof val === 'string' && ISO_DT_RE.test(val)) {
    const d = dayjs(val)
    return d.isValid() ? d.format('YYYY-MM-DD HH:mm:ss') : val
  }
  return val
}

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    if (res && 'data' in res) normalizeDates(res.data)
    return res
  },
  error => {
    const status = error.response && error.response.status
    if (status === 401) {
      // 令牌缺失/过期：后端以 HTTP 401 返回，清除本地令牌并跳转登录
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else if (status === 403) {
      // 已登录但无权限
      ElMessage.error('没有权限访问该资源')
    } else {
      ElMessage.error((error.response && error.response.data && error.response.data.message) || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
