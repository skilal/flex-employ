import request from '../utils/request'

// 获取考勤列表（管理员）
export function getAttendances(params) {
    return request({
        url: '/attendances',
        method: 'get',
        params
    })
}

// 获取我的考勤列表（员工）
export function getMyAttendances(params) {
    return request({
        url: '/attendances/my',
        method: 'get',
        params
    })
}

// 创建考勤记录
export function createAttendance(data) {
    return request({
        url: '/attendances',
        method: 'post',
        data
    })
}

// 更新考勤记录
export function updateAttendance(id, data) {
    return request({
        url: `/attendances/${id}`,
        method: 'put',
        data
    })
}

// 删除考勤记录
export function deleteAttendance(id) {
    return request({
        url: `/attendances/${id}`,
        method: 'delete'
    })
}

// 扫码打卡
export function qrPunch(data) {
    return request({
        url: '/attendances/qr-punch',
        method: 'post',
        data
    })
}
