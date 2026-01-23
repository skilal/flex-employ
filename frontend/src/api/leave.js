import request from '../utils/request'

// 获取请假列表（管理员）
export function getLeaves(params) {
    return request({
        url: '/leaves',
        method: 'get',
        params
    })
}

// 获取我的请假列表（员工）
export function getMyLeaves(params) {
    return request({
        url: '/leaves/my',
        method: 'get',
        params
    })
}

// 提交请假申请
export function createLeave(data) {
    return request({
        url: '/leaves',
        method: 'post',
        data
    })
}

// 审批请假
export function approveLeave(id, data) {
    return request({
        url: `/leaves/${id}/approve`,
        method: 'put',
        data
    })
}

// 删除请假记录
export function deleteLeave(id) {
    return request({
        url: `/leaves/${id}`,
        method: 'delete'
    })
}
