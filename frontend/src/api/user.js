import request from '../utils/request'

// 获取用户信息
export function getUserInfo(id) {
    return request({
        url: `/users/${id}`,
        method: 'get'
    })
}

// 更新用户信息
export function updateUserInfo(id, data) {
    return request({
        url: `/users/${id}`,
        method: 'put',
        data
    })
}

// 修改密码
export function updatePassword(id, data) {
    return request({
        url: `/users/${id}/password`,
        method: 'put',
        data
    })
}
