<template>
  <div class="ai-chat-container">
    <!-- 顶部工具栏 -->
    <div class="chat-header">
      <div class="header-left">
        <div class="chat-title">
          <i class="el-icon-chat-round"></i>
          <span>AI 助手</span>
        </div>
      </div>
      <div class="header-right">
        <!-- 模型选择器 -->
        <el-select
          v-model="selectedModel"
          @change="handleModelChange"
          placeholder="选择模型"
          class="model-selector"
          size="small"
        >
          <el-option
            v-for="model in availableModels"
            :key="model.id"
            :label="model.name"
            :value="model.id"
            :disabled="!model.available"
          >
            <span class="model-option">
              <i :class="model.icon || 'el-icon-cpu'"></i>
              {{ model.name }}
              <span v-if="!model.available" class="model-status">(维护中)</span>
            </span>
          </el-option>
        </el-select>

        <!-- 清空按钮 -->
        <el-button
          @click="handleClearChat"
          size="small"
          type="warning"
          icon="el-icon-delete"
          plain
        >
          清空对话
        </el-button>
      </div>
    </div>

    <!-- 聊天内容区域 -->
    <div class="chat-content" ref="chatContent">
      <!-- 欢迎消息 -->
      <div v-if="messages.length === 0" class="welcome-message">
        <div class="welcome-icon">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <h3>欢迎使用 AI 助手</h3>
        <p>我可以帮助您聊天、生成岗位信息、查询本公司的规章制度、录入简历等。请开始对话吧！</p>
        <div class="quick-actions">
          <el-button
            v-for="act in quickActions"
            :key="act.text"
            size="small"
            type="primary"
            plain
            @click="handleQuickAction(act)"
          >
            {{ act.text }}
          </el-button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div v-else class="message-list">
        <div
          v-for="message in messages"
          :key="message.messageType"
          :class="['message-item', `message-${message.messageType}`]"
        >
          <!-- 用户消息 -->
          <div v-if="message.messageType === 'USER'" class="user-message">
            <div class="message-content">
              <div class="message-text">{{ message.text }}</div>
              <!--              <div v-if="message.files && message.files.length > 0" class="message-files">-->
              <!--                <div-->
              <!--                  v-for="file in message.files"-->
              <!--                  :key="file.id"-->
              <!--                  class="file-item"-->
              <!--                >-->
              <!--                  <i :class="getFileIcon(file.type)"></i>-->
              <!--                  {{ file.name }}-->
              <!--                </div>-->
              <!--              </div>-->
            </div>
            <div class="message-avatar">
              <i class="el-icon-user"></i>
            </div>
          </div>

          <!-- AI消息 -->
          <div v-else-if="message.messageType === 'ASSISTANT'" class="ai-message">
            <div class="message-avatar">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatAIResponse(message.text)"></div>
              <div class="message-actions">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-copy-document"
                  @click="copyToClipboard(message.text)"
                >
                  复制
                </el-button>
                <!--                <el-button-->
                <!--                  size="mini"-->
                <!--                  type="text"-->
                <!--                  icon="el-icon-refresh"-->
                <!--                  @click="regenerateResponse(message.id)"-->
                <!--                >-->
                <!--                  重新生成-->
                <!--                </el-button>-->
              </div>
            </div>
          </div>

          <div class="message-time">
            刚刚
            <!--            {{ formatTime(message.timestamp) }}-->
          </div>
        </div>

        <!-- AI打字指示器 -->
        <div v-if="typing" class="typing-indicator">
          <div class="ai-message">
            <div class="message-avatar">
              <i class="el-icon-cpu"></i>
            </div>
            <div class="message-content">
              <div class="typing-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <div class="typing-text">AI 正在思考中...</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部输入区域 -->
    <div class="chat-input">
      <!-- 文件上传区域 -->
      <div v-if="uploadedFiles.length > 0" class="uploaded-files">
        <div
          v-for="file in uploadedFiles"
          :key="file.id"
          class="file-preview"
        >
          <div class="file-info">
            <i :class="getFileIcon(file.type)"></i>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatFileSize(file.size) }}</span>
          </div>
          <div class="file-actions">
            <el-progress
              v-if="file.uploadStatus === 'uploading'"
              :percentage="file.progress"
              :show-text="false"
              :stroke-width="2"
            ></el-progress>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-close"
              @click="removeFile(file.id)"
            ></el-button>
          </div>
        </div>
      </div>

      <!-- 输入框区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <!-- 文件上传按钮 -->
          <el-upload
            ref="fileUpload"
            :before-upload="handleFileSelect"
            :show-file-list="true"
            :auto-upload="true"
            multiple
            action=""
            accept=".pdf"
            class="file-upload-btn"
          >
            <el-button size="small" icon="el-icon-paperclip" circle></el-button>
          </el-upload>

          <!-- 文本输入框 -->
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            :autosize="{ minRows: 3, maxRows: 8 }"
            placeholder="请输入您的问题..."
            @keydown.enter.prevent="handleEnterKey"
            :disabled="messageLoading"
            class="message-input"
            resize="none"
          ></el-input>

          <!-- 发送按钮 -->
          <el-button
            @click="handleSendMessage"
            :loading="messageLoading"
            :disabled="!canSend"
            type="primary"
            icon="el-icon-s-promotion"
            class="send-btn"
          ></el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapState, mapActions} from 'vuex'
import {getMessageHistory} from "../../api/aiChat";

export default {
  name: 'AiChat',
  data() {
    return {
      inputText: '',
      quickActions: [
        {text: '生成Java岗位招聘信息', prompt: '请生成Java岗位招聘信息'},
        {text: '生成前端岗位招聘信息', prompt: '生成前端岗位招聘信息'}
      ]
    }
  },
  computed: {
    ...mapState('aiChat', [
      'messages',
      'messageLoading',
      'uploadedFiles',
      'selectedModel',
      'availableModels',
      'typing'
    ]),
    canSend() {
      return (this.inputText.trim() || this.uploadedFiles.length > 0) && !this.messageLoading
    }
  },
  async created() {
    await this.fetchModels()
    await this.initChat()
    await this.fetchMessages()
  },
  methods: {
    ...mapActions('aiChat', [
      'sendMessage',
      'clearMessages',
      'uploadFile',
      'removeFile',
      'fetchModels',
      'switchModel',
      'fetchMessages'
    ]),

    async initChat() {
      // 初始化聊天，设置默认模型
      if (this.availableModels.length > 0 && !this.selectedModel) {
        await this.switchModel(this.availableModels[0].id)
      }
    },

    async handleSendMessage() {
      if (!this.canSend) return

      const content = this.inputText.trim()
      const files = [...this.uploadedFiles]

      if (!content && files.length === 0) return

      try {
        await this.sendMessage({
          content,
          files: files.map(f => ({id: f.id, name: f.name, url: f.url})),
          model: this.selectedModel
        })

        this.inputText = ''
        this.scrollToBottom()
      } catch (error) {
        this.$message.error('发送失败，请重试')
      }
    },

    handleEnterKey(event) {
      if (event.shiftKey) {
        return
      }
      event.preventDefault()
      this.handleSendMessage()
    },

    async handleModelChange(modelId) {
      await this.switchModel(modelId)
      this.$message.success('模型切换成功')
    },

    async handleClearChat() {
      try {
        await this.$confirm('确定要清空所有对话记录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await this.clearMessages()
        this.$message.success('对话已清空')
      } catch (error) {
        // 用户取消操作
      }
    },

    handleQuickAction(act) {
      this.inputText = act.prompt
      this.$nextTick(() => {
        this.handleSendMessage()
      })
    },

    async handleFileSelect(file) {
      try {
        await this.uploadFile(file)
        this.$message.success('文件上传成功')
        return false // 阻止默认上传
      } catch (error) {
        this.$message.error('文件上传失败')
        return false
      }
    },

    async regenerateResponse(messageId) {
      // 找到对应的用户消息并重新发送
      const messageIndex = this.messages.findIndex(m => m.id === messageId)
      if (messageIndex > 0) {
        const userMessage = this.messages[messageIndex - 1]
        if (usermessage.messageType === 'user') {
          await this.sendMessage({
            content: userMessage.content,
            files: userMessage.files || [],
            model: this.selectedModel
          })
        }
      }
    },

    copyToClipboard(text) {
      navigator.clipboard.writeText(text).then(() => {
        this.$message.success('已复制到剪贴板')
      }).catch(() => {
        this.$message.error('复制失败')
      })
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const chatContent = this.$refs.chatContent
        if (chatContent) {
          chatContent.scrollTop = chatContent.scrollHeight
        }
      })
    },

    formatTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (diff < 86400000) {
        return `${Math.floor(diff / 3600000)}小时前`
      } else {
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().substring(0, 5)
      }
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    getFileIcon(type) {
      return "el-icon-document"
    },

    formatAIResponse(content) {
      // 简单的Markdown到HTML转换
      return content
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/`(.*?)`/g, '<code>$1</code>')
        .replace(/\n/g, '<br>')
    }
  },

  watch: {
    messages: {
      handler() {
        this.scrollToBottom()
      },
      deep: true
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 220px);
  max-height: 700px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  overflow: hidden;
  margin: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);

  .header-left {
    .chat-title {
      display: flex;
      align-items: center;
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;

      i {
        margin-right: 8px;
        font-size: 20px;
        color: #667eea;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .model-selector {
      width: 160px;

      .model-option {
        display: flex;
        align-items: center;

        i {
          margin-right: 8px;
        }

        .model-status {
          margin-left: auto;
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

.chat-content {
  flex: 1;
  overflow-y: hidden;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
}

.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px;

  .welcome-icon {
    margin-bottom: 24px;

    i {
      font-size: 64px;
      color: #667eea;
      opacity: 0.8;
    }
  }

  h3 {
    margin: 0 0 12px 0;
    font-size: 28px;
    color: #2c3e50;
    font-weight: 300;
  }

  p {
    margin: 0 0 32px 0;
    font-size: 16px;
    color: #7f8c8d;
    line-height: 1.6;
    max-width: 400px;
  }

  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
  }
}

.message-list {
  padding: 24px;
  height: 100%;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 0;
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: transparent;
  }
}

.message-item {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.user-message {
  display: flex;
  margin-top: 26px;
  justify-content: flex-end;
  align-items: flex-start;

  .message-content {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 20px 20px 6px 20px;
    padding: 16px 20px;
    max-width: 70%;
    margin-right: 12px;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

    .message-text {
      line-height: 1.5;
      word-break: break-word;
    }

    .message-files {
      margin-top: 12px;

      .file-item {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 8px;
        margin-bottom: 6px;
        font-size: 14px;

        i {
          margin-right: 8px;
        }
      }
    }
  }

  .message-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

    i {
      font-size: 18px;
      color: white;
    }
  }
}

.ai-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;

  .message-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #36d1dc 0%, #5b86e5 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    flex-shrink: 0;
    box-shadow: 0 4px 12px rgba(54, 209, 220, 0.3);

    i {
      font-size: 18px;
      color: white;
    }
  }

  .message-content {
    background: #f8f9fa;
    border-radius: 20px 20px 20px 6px;
    padding: 16px 20px;
    max-width: 70%;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .message-text {
      line-height: 1.6;
      color: #2c3e50;
      word-break: break-word;

      ::v-deep code {
        background: #e9ecef;
        padding: 2px 6px;
        border-radius: 4px;
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
        font-size: 13px;
      }

      ::v-deep strong {
        color: #495057;
      }

      ::v-deep em {
        color: #6c757d;
      }
    }

    .message-actions {
      margin-top: 12px;
      display: flex;
      gap: 8px;
    }
  }
}

.message-time {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.typing-indicator {
  .typing-dots {
    display: flex;
    align-items: center;
    height: 24px;

    span {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #36d1dc;
      margin: 0 3px;
      animation: typing 1.4s infinite;

      &:nth-child(2) {
        animation-delay: 0.2s;
      }

      &:nth-child(3) {
        animation-delay: 0.4s;
      }
    }
  }

  .typing-text {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.chat-input {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding: 16px 24px;
}

.uploaded-files {
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.file-preview {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 8px 12px;
  border: 1px solid #e9ecef;

  .file-info {
    display: flex;
    align-items: center;
    margin-right: 12px;

    i {
      margin-right: 6px;
      color: #6c757d;
    }

    .file-name {
      font-size: 14px;
      color: #495057;
      margin-right: 8px;
    }

    .file-size {
      font-size: 12px;
      color: #999;
    }
  }

  .file-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.input-area {
  .input-wrapper {
    display: flex;
    align-items: flex-end;
    gap: 12px;
    background: white;
    border-radius: 12px;
    padding: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    .file-upload-btn {
      flex-shrink: 0;
    }

    .message-input {
      flex: 1;

      ::v-deep .el-textarea__inner {
        border: none;
        resize: none;
        padding: 20px;
        font-size: 16px;
        line-height: 1.8;
        min-height: 80px !important;
        height: auto !important;
        box-shadow: none;

        &:focus {
          box-shadow: none;
        }
      }
    }

    .send-btn {
      flex-shrink: 0;
      border-radius: 8px;
      padding: 12px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .ai-chat-container {
    margin: 10px;
    border-radius: 12px;
    height: calc(100vh - 100px);
    max-height: 600px;
  }

  .chat-header {
    padding: 12px 16px;

    .header-right {
      gap: 8px;

      .model-selector {
        width: 120px;
      }
    }
  }

  .message-list {
    padding: 16px;
  }

  .user-message .message-content,
  .ai-message .message-content {
    max-width: 85%;
  }

  .chat-input {
    padding: 12px 16px;
  }

  .quick-actions {
    flex-direction: column;
    width: 100%;
  }
}

// 隐藏滚动条但保持滚动功能
.message-list {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* Internet Explorer 10+ */
}

.message-list::-webkit-scrollbar {
  width: 0;
  height: 0;
  background: transparent;
}
</style>
