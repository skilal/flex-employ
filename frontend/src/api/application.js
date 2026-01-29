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

// 检查员工是否已在岗
export function checkWorkerStatus(positionId) {
    return request({
        url: '/applications/check-worker-status',
        method: 'get',
        params: { positionId }
    })
}

// 检查工作时间冲突
export function checkTimeConflict(positionId) {
    return request({
        url: '/applications/check-time-conflict',
        method: 'get',
        params: { positionId }
    })
}
