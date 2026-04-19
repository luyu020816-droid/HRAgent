import request from '@/utils/request'

/**
 * 获取Offer列表
 * @param {Object} data 查询参数
 * @returns {Promise}
 */
export function getOfferList(data) {
  return request({
    url: '/offer/list',
    method: 'post',
    data,
  })
}

/**
 * 创建Offer记录
 * @param {Object} data Offer数据
 * @returns {Promise}
 */
export function createOffer(data) {
  return request({
    url: '/offer/add',
    method: 'post',
    data,
  })
}

/**
 * 更新Offer记录
 * @param {Object} data Offer数据
 * @returns {Promise}
 */
export function updateOffer(data) {
  return request({
    url: '/offer/update',
    method: 'post',
    data,
  })
}

/**
 * 删除Offer记录
 * @param {string} id Offer ID
 * @returns {Promise}
 */
export function deleteOffer(id) {
  return request({
    url: '/offer/delete',
    method: 'post',
    data: {id},
  })
}

/**
 * 批量删除Offer记录
 * @param {Array} ids Offer ID数组
 * @returns {Promise}
 */
export function batchDeleteOffer(ids) {
  return request({
    url: '/offer/batchDelete',
    method: 'post',
    data: {ids},
  })
}

/**
 * 发送Offer邮件
 * @param {string} id Offer ID
 * @returns {Promise}
 */
export function sendOfferEmail(id) {
  return request({
    url: '/offer/sendemail',
    method: 'post',
    data: {id},
  })
}

/**
 * 撤回Offer
 * @param {string} id Offer ID
 * @returns {Promise}
 */
export function withdrawOffer(id) {
  return request({
    url: '/offer/rollback',
    method: 'post',
    data: {id},
  })
}

/**
 * 获取Offer统计数据
 * @returns {Promise}
 */
export function getOfferStatistics() {
  return request({
    url: '/offer/statistics',
    method: 'get',
  })
}

/**
 * 获取职位选项列表
 * @returns {Promise}
 */
export function getJobOptions() {
  
}
