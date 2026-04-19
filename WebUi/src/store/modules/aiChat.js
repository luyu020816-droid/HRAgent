import {
  sendMessage as sendMessageApi,
  getMessageHistory,
  clearMessages as clearMessagesApi,
  uploadFile as uploadFileApi,
  deleteFile,
  getModels
} from '@/api/aiChat'

const state = {
  // 消息相关
  messages: [],              // 当前对话消息列表
  messageLoading: false,     // 消息发送中状态

  // 文件相关
  uploadedFiles: [],         // 已上传文件列表

  // 模型相关
  availableModels: [],       // 可用模型列表
  selectedModel: 'Qwen', // 当前选中的模型

  // UI状态
  typing: false              // AI回复中状态
}

const mutations = {
  // 消息管理
  SET_MESSAGES(state, messages) {
    console.dir(messages);
    state.messages = messages || []
  },

  ADD_MESSAGE(state, message) {
    state.messages.push(message)
  },

  CLEAR_MESSAGES(state) {
    state.messages = []
  },

  SET_MESSAGE_LOADING(state, loading) {
    state.messageLoading = loading
  },

  SET_TYPING(state, typing) {
    state.typing = typing
  },

  // 文件管理
  SET_UPLOADED_FILES(state, files) {
    state.uploadedFiles = files || []
  },

  ADD_UPLOADED_FILE(state, file) {
    state.uploadedFiles.push(file)
  },

  REMOVE_UPLOADED_FILE(state, fileId) {
    const index = state.uploadedFiles.findIndex(f => f.id === fileId)
    if (index > -1) {
      state.uploadedFiles.splice(index, 1)
    }
  },

  UPDATE_FILE_STATUS(state, {fileId, status, progress}) {
    const file = state.uploadedFiles.find(f => f.id === fileId)
    if (file) {
      if (status !== undefined) file.uploadStatus = status
      if (progress !== undefined) file.progress = progress
    }
  },

  // 模型管理
  SET_AVAILABLE_MODELS(state, models) {
    state.availableModels = models || []
  },

  SET_SELECTED_MODEL(state, modelId) {
    state.selectedModel = modelId
  }
}

const actions = {
  // 获取消息历史
  async fetchMessages({commit}) {
    try {
      const response = await getMessageHistory()
      if (response.code === 200) {
        commit('SET_MESSAGES', response.data || [])
      }
    } catch (error) {
      console.error('获取消息历史失败:', error)
      commit('SET_MESSAGES', [])
    }
  },

  // 发送消息
  async sendMessage({commit, state}, {content, files, model}) {
    if (!content.trim() && (!files || files.length === 0)) {
      return
    }

    commit('SET_MESSAGE_LOADING', true)
    commit('SET_TYPING', false)

    // 创建用户消息
    const userMessage = {
      id: `msg_${Date.now()}_user`,
      messageType: 'USER',
      text: content.trim(),
      files: files || [],
      timestamp: new Date(),
      status: 'sent'
    }

    commit('ADD_MESSAGE', userMessage)

    try {
      // 调用API发送消息
      const response = await sendMessageApi({
        content: content.trim(),
        files: (files || []).map(f => ({id: f.id, name: f.name, url: f.url})),
        model: model || state.selectedModel
      })

      if (response.code === 200) {
        // 显示打字指示器
        commit('SET_TYPING', true)

        // 模拟AI回复延迟
        setTimeout(() => {
          commit('SET_TYPING', false)

          // 添加AI回复消息
          const aiMessage = {
            id: `msg_${Date.now()}_ai`,
            messageType: 'ASSISTANT',
            text: response.data || '抱歉，我暂时无法回复您的问题。',
            timestamp: new Date(),
            status: 'received'
          }

          commit('ADD_MESSAGE', aiMessage)
        }, 1000 + Math.random() * 2000) // 1-3秒随机延迟
      } else {
        throw new Error(response.message || '发送失败')
      }
    } catch (error) {
      commit('SET_TYPING', false)
      console.error('发送消息失败:', error)

      // 添加错误消息
      const errorMessage = {
        id: `msg_${Date.now()}_error`,
        messageType: 'system',
        text: `发送失败：${error.message || '网络错误，请重试'}`,
        timestamp: new Date(),
        status: 'failed'
      }
      commit('ADD_MESSAGE', errorMessage)

      throw error
    } finally {
      commit('SET_MESSAGE_LOADING', false)
      // 发送成功后清空已上传文件
      commit('SET_UPLOADED_FILES', [])
    }
  },

  // 清空消息
  async clearMessages({commit}) {
    try {
      await clearMessagesApi()
      commit('CLEAR_MESSAGES')
      return true
    } catch (error) {
      console.error('清空消息失败:', error)
      // 即使API失败，也清空本地消息
      commit('CLEAR_MESSAGES')
      throw error
    }
  },

  // 上传文件
  async uploadFile({commit}, file) {
    try {
      // 创建文件对象
      const fileObj = {
        id: `file_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        name: file.name,
        type: file.type,
        size: file.size,
        uploadStatus: 'uploading',
        progress: 0,
        originalFile: file
      }

      commit('ADD_UPLOADED_FILE', fileObj)

      // 模拟上传进度
      const progressInterval = setInterval(() => {
        const currentFile = state.uploadedFiles.find(f => f.id === fileObj.id)
        if (currentFile && currentFile.progress < 90) {
          commit('UPDATE_FILE_STATUS', {
            fileId: fileObj.id,
            progress: Math.min(currentFile.progress + Math.random() * 30, 90)
          })
        }
      }, 200)

      try {
        // 调用上传API
        const response = await uploadFileApi(file)
        clearInterval(progressInterval)

        if (response.code === 200) {
          commit('UPDATE_FILE_STATUS', {
            fileId: fileObj.id,
            status: 'success',
            progress: 100
          })

          // 更新文件信息
          const uploadedFile = {
            ...fileObj,
            id: response.data.id || fileObj.id,
            url: response.data.url,
            uploadStatus: 'success',
            progress: 100
          }

          commit('REMOVE_UPLOADED_FILE', fileObj.id)
          commit('ADD_UPLOADED_FILE', uploadedFile)

          return uploadedFile
        } else {
          throw new Error(response.message || '上传失败')
        }
      } catch (apiError) {
        clearInterval(progressInterval)
        commit('UPDATE_FILE_STATUS', {
          fileId: fileObj.id,
          status: 'failed'
        })
        throw apiError
      }
    } catch (error) {
      console.error('文件上传失败:', error)
      throw error
    }
  },

  // 删除文件
  async removeFile({commit}, fileId) {
    try {
      // 如果文件已上传到服务器，调用删除API
      const file = state.uploadedFiles.find(f => f.id === fileId)
      if (file && file.url && file.uploadStatus === 'success') {
        await deleteFile(fileId)
      }

      commit('REMOVE_UPLOADED_FILE', fileId)
    } catch (error) {
      console.error('删除文件失败:', error)
      // 即使删除API失败，也要从本地移除
      commit('REMOVE_UPLOADED_FILE', fileId)
    }
  },

  // 获取可用模型
  async fetchModels({commit}) {
    try {
      const response = await getModels()
      if (response.code === 200 && response.data) {
        const models = response.data.map(model => ({
          id: model.id,
          name: model.name || model.id,
          available: model.available !== false,
          icon: model.icon || 'el-icon-cpu',
          description: model.description || ''
        }))
        commit('SET_AVAILABLE_MODELS', models)
      } else {
        // 设置默认模型列表
        commit('SET_AVAILABLE_MODELS', [
          {id: 'Qwen', name: 'Qwen', available: true, icon: 'el-icon-cpu'},
          {id: 'DeepSeek', name: 'DeepSeek', available: true, icon: 'el-icon-cpu'}
        ])
      }
    } catch (error) {
      console.error('获取模型列表失败:', error)
      // 设置默认模型列表
      commit('SET_AVAILABLE_MODELS', [
        {id: 'Qwen', name: 'Qwen', available: true, icon: 'el-icon-cpu'},
        {id: 'DeepSeek', name: 'DeepSeek', available: true, icon: 'el-icon-cpu'}
      ])
    }
  },

  // 切换模型
  async switchModel({commit}, modelId) {
    commit('SET_SELECTED_MODEL', modelId)
  }
}

const getters = {
  // 当前会话的消息数量
  messageCount: state => {
    return state.messages.length
  },

  // 已上传成功的文件
  successUploadedFiles: state => {
    return state.uploadedFiles.filter(f => f.uploadStatus === 'success')
  },

  // 正在上传的文件
  uploadingFiles: state => {
    return state.uploadedFiles.filter(f => f.uploadStatus === 'uploading')
  },

  // 上传失败的文件
  failedUploadFiles: state => {
    return state.uploadedFiles.filter(f => f.uploadStatus === 'failed')
  },

  // 获取指定模型信息
  getModelById: state => modelId => {
    return state.availableModels.find(m => m.id === modelId)
  },

  // 当前选中的模型信息
  currentModel: state => {
    return state.availableModels.find(m => m.id === state.selectedModel)
  },

  // 可用模型列表
  availableModelOptions: state => {
    return state.availableModels.filter(m => m.available)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
