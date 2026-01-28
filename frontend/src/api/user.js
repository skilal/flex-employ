import request from '../utils/request'

// 获取用户列表
export function getUsers(params) {
    return request({
        url: '/users',
        method: 'get',
        params
    })
}

// 创建用户
export function createUser(data) {
    return request({
        url: '/users',
        method: 'post',
        data
    })
}

// 更新用户
export function updateUser(id, data) {
    return request({
        url: `/users/${id}`,
        method: 'put',
        data
    })
}

// 删除用户
export function deleteUser(id) {
    return request({
        url: `/users/${id}`,
        method: 'delete'
    })
}

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
