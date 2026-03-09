import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api', // 后端 API 基础路径
    timeout: 10000
})

// 标记是否正在刷新 token，防止并发请求同时触发多次刷新
let isRefreshing = false
// 等待刷新完成的队列
let refreshQueue = []

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 从 localStorage 获取 token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data

        // 如果返回的状态码不是 200，则认为是错误
        if (response.status !== 200) {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }

        // 检查业务状态码，后端返回的code为200表示成功，否则为失败
        if (res.code && res.code !== 200) {
            ElMessage.error(res.message || '操作失败')
            return Promise.reject(new Error(res.message || '操作失败'))
        }

        return res
    },
    async error => {
        const originalRequest = error.config

        // 处理 401（Token 失效），尝试无感刷新
        if (error.response?.status === 401 && !originalRequest._retry) {
            const refreshToken = localStorage.getItem('refreshToken')

            // 没有 refreshToken，直接跳转登录
            if (!refreshToken) {
                localStorage.removeItem('token')
                localStorage.removeItem('refreshToken')
                localStorage.removeItem('userInfo')
                localStorage.removeItem('role')
                router.push('/login')
                return Promise.reject(error)
            }

            // 如果已经在刷新，将本次请求加入队列等待
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    refreshQueue.push({ resolve, reject })
                }).then(newToken => {
                    originalRequest.headers['Authorization'] = `Bearer ${newToken}`
                    return request(originalRequest)
                })
            }

            originalRequest._retry = true
            isRefreshing = true

            try {
                // 用 refreshToken 换取新的 accessToken
                const res = await axios.post('/api/auth/refresh', { refreshToken })
                const newAccessToken = res.data.data.accessToken

                // 更新存储
                localStorage.setItem('token', newAccessToken)

                // 通知队列中所有等待的请求
                refreshQueue.forEach(({ resolve }) => resolve(newAccessToken))
                refreshQueue = []

                // 先重置旗标，再发起重试（避免 finally 时机过早）
                isRefreshing = false

                // 重试原请求
                originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`
                return request(originalRequest)
            } catch (refreshError) {
                // refreshToken 也失效，清除登录态并跳转
                isRefreshing = false
                refreshQueue.forEach(({ reject }) => reject(refreshError))
                refreshQueue = []
                localStorage.removeItem('token')
                localStorage.removeItem('refreshToken')
                localStorage.removeItem('userInfo')
                localStorage.removeItem('role')
                ElMessage.error('登录已过期，请重新登录')
                router.push('/login')
                return Promise.reject(refreshError)
            }
        }

        // 处理其他错误
        if (error.response) {
            switch (error.response.status) {
                case 403:
                    ElMessage.error('没有权限访问')
                    break
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(error.response.data?.message || '请求失败')
            }
        } else {
            ElMessage.error('网络错误，请检查网络连接')
        }

        return Promise.reject(error)
    }
)

export default request
