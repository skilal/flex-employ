import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    // 初始化时检查并清理无效的角色数据
    const storedRole = localStorage.getItem('role') || ''
    const storedToken = localStorage.getItem('token') || ''

    // 如果角色是中文（旧数据），自动清除所有登录信息
    if (storedRole && !['ADMIN', 'EMPLOYEE'].includes(storedRole)) {
        console.warn('检测到无效的角色数据，已自动清除:', storedRole)
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        localStorage.removeItem('userInfo')
    }

    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
    const token = ref(['ADMIN', 'EMPLOYEE'].includes(storedRole) ? storedToken : '')
    const role = ref(['ADMIN', 'EMPLOYEE'].includes(storedRole) ? storedRole : '')

    // 设置用户信息
    const setUserInfo = (info) => {
        userInfo.value = info
        role.value = info.role
        localStorage.setItem('role', info.role)
        localStorage.setItem('userInfo', JSON.stringify(info))
    }

    // 设置 token
    const setToken = (newToken) => {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    // 登出
    const logout = () => {
        userInfo.value = null
        token.value = ''
        role.value = ''
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('role')
    }

    // 检查是否登录
    const isLoggedIn = () => {
        return !!token.value
    }

    // 检查是否是管理员
    const isAdmin = () => {
        return role.value === 'ADMIN'
    }

    return {
        userInfo,
        token,
        role,
        setUserInfo,
        setToken,
        logout,
        isLoggedIn,
        isAdmin
    }
})
