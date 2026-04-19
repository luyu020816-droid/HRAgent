/**
 * AI聊天相关API接口 - 简化版本
 */
import request from '@/utils/request'

// 消息管理API

/**
 * 发送消息到AI助手
 * @param {Object} data 消息数据
 * @param {string} data.content 消息内容
 * @param {Array} data.files 附件文件列表
 * @param {string} data.model 使用的模型ID
 */
export function sendMessage(data) {
  return request({
    url: '/aichat/sendmsg',
    method: 'post',
    data
  })
}

/**
 * 获取当前会话的消息历史
 */
export function getMessageHistory() {
  return request({
    url: '/aichat/getmessages',
    method: 'get'
  })
}

/**
 * 清空当前对话记录
 */
export function clearMessages() {
  return request({
    url: '/aichat/clearmessages',
    method: 'post'
  })
}

// 文件管理API

/**
 * 上传文件
 * @param {File} file 文件对象
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('myfile', file)

  return request({
    url: '/aichat/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 * @param {string} fileId 文件ID
 */
export function deleteFile(fileId) {
  return request({
    url: `/aichat/files/${fileId}`,
    method: 'delete'
  })
}

// 模型管理API

/**
 * 获取可用模型列表
 */
export function getModels() {
  return request({
    url: '/aichat/models',
    method: 'get'
  })
}

