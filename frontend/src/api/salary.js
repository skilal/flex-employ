import request from '../utils/request'

// --- 薪资配置模版 (SalaryConfig) ---

// 获取所有薪资配置模版
export function getSalaryConfigs() {
    return request({
        url: '/salary-configs',
        method: 'get'
    })
}

// 获取单个薪资配置
export function getSalaryConfig(id) {
    return request({
        url: `/salary-configs/${id}`,
        method: 'get'
    })
}

// 创建薪资配置
export function createSalaryConfig(data) {
    return request({
        url: '/salary-configs',
        method: 'post',
        data
    })
}

// 更新薪资配置
export function updateSalaryConfig(id, data) {
    return request({
        url: `/salary-configs/${id}`,
        method: 'put',
        data
    })
}

// 删除薪资配置
export function deleteSalaryConfig(id) {
    return request({
        url: `/salary-configs/${id}`,
        method: 'delete'
    })
}

// --- 薪资结算记录 (PaySlip) ---

// 获取薪资流水记录 (PaySlip)
export function getPaySlips(params) {
    return request({
        url: '/salaries',
        method: 'get',
        params
    })
}

// 我的薪资记录
export function getMyPaySlips() {
    return request({
        url: '/salaries/my',
        method: 'get'
    })
}

// 生成薪资记录 (根据配置和考勤)
export function generatePaySlips() {
    return request({
        url: '/salaries/generate',
        method: 'post'
    })
}

// 创建薪资记录
export function createPaySlip(data) {
    return request({
        url: '/salaries',
        method: 'post',
        data
    })
}

// 更新薪资记录
export function updatePaySlip(id, data) {
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
