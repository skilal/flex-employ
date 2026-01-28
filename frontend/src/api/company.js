import request from '../utils/request'

// 获取公司列表
export const getCompanies = (params) => {
    return request({
        url: '/companies',
        method: 'get',
        params
    })
}

// 根据ID获取公司
export const getCompanyById = (id) => {
    return request({
        url: `/companies/${id}`,
        method: 'get'
    })
}

// 创建公司
export const createCompany = (data) => {
    return request({
        url: '/companies',
        method: 'post',
        data
    })
}

// 更新公司
export const updateCompany = (id, data) => {
    return request({
        url: `/companies/${id}`,
        method: 'put',
        data
    })
}

// 删除公司
export const deleteCompany = (id) => {
    return request({
        url: `/companies/${id}`,
        method: 'delete'
    })
}
