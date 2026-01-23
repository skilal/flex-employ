import request from '../utils/request'

// 获取在岗员工列表
export function getWorkers(params) {
    return request({
        url: '/workers',
        method: 'get',
        params
    })
}

// 获取我的在岗记录
export function getMyWorkerRecord(params) {
    return request({
        url: '/workers/my',
        method: 'get',
        params
    })
}

// 添加在岗员工
export function createWorker(data) {
    return request({
        url: '/workers',
        method: 'post',
        data
    })
}

// 更新在岗员工
export function updateWorker(id, data) {
    return request({
        url: `/workers/${id}`,
        method: 'put',
        data
    })
}

// 删除在岗员工
export function deleteWorker(id) {
    return request({
        url: `/workers/${id}`,
        method: 'delete'
    })
}
