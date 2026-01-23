import request from '../utils/request'

// 获取申请列表（管理员）
export function getApplications(params) {
    return request({
        url: '/applications',
        method: 'get',
        params
    })
}

// 获取我的申请列表（员工）
export function getMyApplications(params) {
    return request({
        url: '/applications/my',
        method: 'get',
        params
    })
}

// 提交申请
export function createApplication(data) {
    return request({
        url: '/applications',
        method: 'post',
        data
    })
}

// 审批申请
export function approveApplication(id, data) {
    return request({
        url: `/applications/${id}/approve`,
        method: 'put',
        data
    })
}

// 删除申请
export function deleteApplication(id) {
    return request({
        url: `/applications/${id}`,
        method: 'delete'
    })
}
