import request from '@/utils/request'

/**
 * 获取面试列表
 * @param {Object} data 查询参数
 * @returns {Promise}
 */
export function getInterviewList(data) {
  data.baseStatus = [-2, 2, 3, 4];
  return request({
    url: '/resume/list',
    method: 'post',
    data,
  })
}

/**
 * 根据 rid 查询面试记录
 * @param rid
 * @returns {*}
 */
export function getInterviewListByRId(rid) {
  return request({
    url: '/interview/getlistbyrid?rid=' + rid,
    method: 'get'
  })
}

/**
 * 创建面试记录
 * @param {Object} data 面试记录数据
 * @returns {Promise}
 */
export function createInterview(data) {
  return request({
    url: '/interview/add',
    method: 'post',
    data,
  })
}

/**
 * 更新面试记录
 * @param {Object} data 面试记录数据
 * @returns {Promise}
 */
export function updateInterview(data) {
  return request({
    url: '/interview/update',
    method: 'post',
    data,
  })
}

/**
 * 获取面试统计数据
 * @returns {Promise}
 */
export function getInterviewStatistics() {
  return request({
    url: '/interview/statistics',
    method: 'get',
  })
}

/**
 * 添加面试评价
 * @param {Object} data 面试评价数据
 * @returns {Promise}
 */
export function addInterviewEvaluation(data) {
  return request({
    url: '/interview/update',
    method: 'post',
    data,
  })
}
