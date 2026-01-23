import request from '../utils/request'

// 获取岗位列表
export function getPositions(params) {
    return request({
        url: '/positions',
        method: 'get',
        params
    })
}

// 获取招聘中的岗位
export function getRecruitingPositions(params) {
    return request({
        url: '/positions/recruiting',
        method: 'get',
        params
    })
}

// 创建岗位
export function createPosition(data) {
    return request({
        url: '/positions',
        method: 'post',
        data
    })
}

// 更新岗位
export function updatePosition(id, data) {
    return request({
        url: `/positions/${id}`,
        method: 'put',
        data
    })
}

// 删除岗位
export function deletePosition(id) {
    return request({
        url: `/positions/${id}`,
        method: 'delete'
    })
}
