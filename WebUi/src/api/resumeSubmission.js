import request from '@/utils/request'

/**
 * 获取简历投递列表
 * @param {Object} data 查询参数
 * @returns {Promise}
 */
export function getResumeSubmissionList(data) {
  data.baseStatus = [-1, 1, 2];
  return request({
    url: '/resume/list',
    method: 'post',
    data,
  })
}

/**
 * 创建简历投递记录
 * @param {Object} data 投递记录数据
 * @returns {Promise}
 */
export function createResumeSubmission(data) {
  return request({
    url: '/resume/add',
    method: 'post',
    data,
  })
}

/**
 * 更新简历投递记录
 * @param {Object} data 投递记录数据
 * @returns {Promise}
 */
export function updateResumeSubmission(data) {
  return request({
    url: '/resume/update',
    method: 'post',
    data,
  })
}

/**
 * 删除简历投递记录
 * @param {string} id 投递记录ID
 * @returns {Promise}
 */
export function deleteResumeSubmission(id) {
  return request({
    url: '/resume/delete',
    method: 'post',
    data: {id},
  })
}

/**
 * 批量删除简历投递记录
 * @param {Array} ids 投递记录ID数组
 * @returns {Promise}
 */
export function batchDeleteResumeSubmission(ids) {
  return request({
    url: '/resume/deletes',
    method: 'post',
    data: {ids},
  })
}


/**
 * 下载简历文件
 * @param {string} id 投递记录ID
 * @returns {Promise}
 */
export function downloadResume(id) {
  return request({
    url: `/resume/download/${id}`,
    method: 'get',
    responseType: 'blob',
  })
}

/**
 * 获取投递统计数据
 * @returns {Promise}
 */
export function getResumeStatistics() {
  return request({
    url: '/resume/statistics',
    method: 'get',
  })
}


