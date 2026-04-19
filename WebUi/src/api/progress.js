import request from '@/utils/request'

export function getList(data) {
  return request({
    url: '/resume/list',
    method: 'post',
    data,
  })
}
