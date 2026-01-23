import request from '../utils/request'

// 获取薪资列表（管理员）
export function getSalaries(params) {
    return request({
        url: '/salaries',
        method: 'get',
        params
    })
}

// 获取我的薪资列表（员工）
export function getMySalaries(params) {
    return request({
        url: '/salaries/my',
        method: 'get',
        params
    })
}

// 创建薪资记录
export function createSalary(data) {
    return request({
        url: '/salaries',
        method: 'post',
        data
    })
}

// 更新薪资记录
export function updateSalary(id, data) {
    return request({
        url: `/salaries/${id}`,
        method: 'put',
        data
    })
}

// 删除薪资记录
export function deleteSalary(id) {
    return request({
        url: `/salaries/${id}`,
        method: 'delete'
    })
}
