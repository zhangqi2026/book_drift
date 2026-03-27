<template>
  <div class="ai-chat-page">
    <div class="page-header">
      <h1>AI 智能问答</h1>
      <p>欢迎使用AI助手，我可以帮你解答各种问题</p>
    </div>
    
    <div class="page-content">
      <div class="chat-section">
        <el-card class="chat-card">
          <div slot="header" class="card-header">
            <span>对话界面</span>
            <el-button type="text" @click="clearChat">清空对话</el-button>
          </div>
          <div class="card-body">
            <div class="chat-messages" ref="chatMessages">
              <div v-for="(message, index) in messages" :key="index" 
                   :class="['message', message.role === 'user' ? 'user-message' : 'ai-message']">
                <div class="message-avatar">
                  <div :class="['avatar', message.role === 'user' ? 'user-avatar' : 'ai-avatar']">
                    {{ message.role === 'user' ? '用' : 'AI' }}
                  </div>
                </div>
                <div class="message-content">
                  <div class="message-text" v-if="message.role === 'user'">{{ message.content }}</div>
                  <div class="message-text markdown-content" v-else v-html="renderMarkdown(message.content)"></div>
                  <div class="message-time">{{ formatTime(message.time) }}</div>
                </div>
              </div>
              <div v-if="loading" class="message ai-message">
                <div class="message-avatar">
                  <div class="avatar ai-avatar">AI</div>
                </div>
                <div class="message-content">
                  <div class="message-text">
                    <i class="el-icon-loading"></i> 正在思考中...
                  </div>
                </div>
              </div>
            </div>
            <div class="chat-input">
              <el-input
                type="textarea"
                :rows="3"
                v-model="inputMessage"
                placeholder="请输入你的问题..."
                @keydown.ctrl.enter.native="handleKeyDown"
              ></el-input>
              <div class="input-actions">
                <span class="hint">按 Ctrl+Enter 发送</span>
                <el-button type="primary" @click="sendMessage" :loading="loading">发送</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <div class="history-section">
        <el-card class="history-card">
          <div slot="header" class="card-header">
            <span>历史记录</span>
            <el-button type="text" @click="clearHistory" size="small">清空</el-button>
          </div>
          <div class="history-list">
            <div v-if="historyList.length === 0" class="empty-history">
              暂无历史记录
            </div>
            <div v-for="(item, index) in historyList" :key="index" 
                 class="history-item">
              <div class="history-item-content" @click="loadHistory(item)">
                <div class="history-title">
                  <i class="el-icon-chat-line-round"></i>
                  {{ item.title }}
                </div>
                <div class="history-time">{{ formatTime(item.createTime) }}</div>
              </div>
              <el-button 
                type="text" 
                size="small" 
                icon="el-icon-delete" 
                class="delete-btn"
                @click.stop="deleteSession(item.sessionId)"
                title="删除此会话">
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      messages: [],
      inputMessage: '',
      loading: false,
      historyList: [],
      currentUser: {},
      currentSessionId: null
    }
  },
  mounted() {
    this.getCurrentUser()
    this.loadHistoryList()
    this.addWelcomeMessage()
  },
  methods: {
    renderMarkdown(content) {
      if (!content) return ''
      
      let html = content
      
      html = html.replace(/&/g, '&amp;')
      html = html.replace(/</g, '&lt;')
      html = html.replace(/>/g, '&gt;')
      
      html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      html = html.replace(/__(.*?)__/g, '<strong>$1</strong>')
      
      html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
      html = html.replace(/_(.*?)_/g, '<em>$1</em>')
      
      html = html.replace(/`(.*?)`/g, '<code>$1</code>')
      
      html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>')
      html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>')
      html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>')
      
      html = html.replace(/^\> (.*$)/gim, '<blockquote>$1</blockquote>')
      
      html = html.replace(/^- (.*$)/gim, '<li>$1</li>')
      html = html.replace(/^\* (.*$)/gim, '<li>$1</li>')
      
      html = html.replace(/(?:<li>.*<\/li>\s*)+/g, match => {
        return '<ul>' + match + '</ul>'
      })
      
      html = html.replace(/\n/g, '<br>')
      
      return html
    },
    getCurrentUser() {
      const user = localStorage.getItem('user')
      if (user) {
        this.currentUser = JSON.parse(user)
      } else {
        this.$router.push('/login')
      }
    },
    handleKeyDown(event) {
      event.preventDefault()
      this.sendMessage()
    },
    async sendMessage() {
      if (!this.inputMessage.trim()) {
        this.$message.warning('请输入问题')
        return
      }
      
      const userMessage = {
        role: 'user',
        content: this.inputMessage.trim(),
        time: new Date()
      }
      
      this.messages.push(userMessage)
      const question = this.inputMessage
      this.inputMessage = ''
      this.loading = true
      
      this.scrollToBottom()
      
      try {
        const aiResponse = await this.callAI(question)
        
        const aiMessage = {
          role: 'assistant',
          content: aiResponse,
          time: new Date()
        }
        
        this.messages.push(aiMessage)
        
        await this.saveConversation(question, aiResponse)
        
      } catch (error) {
        console.error('AI调用失败:', error)
        this.$message.error('AI服务暂时不可用，请稍后重试')
        
        const errorMessage = {
          role: 'assistant',
          content: '抱歉，我暂时无法回答你的问题，请稍后再试。',
          time: new Date()
        }
        this.messages.push(errorMessage)
      } finally {
        this.loading = false
        this.scrollToBottom()
      }
    },
    async callAI(question) {
      const response = await this.$axios.post('/ai/chat', {
        question: question
      })
      
      if (response.code === 20000) {
        return response.data
      } else {
        throw new Error(response.message || 'AI调用失败')
      }
    },
    async saveConversation(question, answer) {
      if (!this.currentSessionId) {
        this.currentSessionId = Date.now()
      }
      
      try {
        await this.$axios.post('/ai/save', {
          userId: this.currentUser.id,
          sessionId: this.currentSessionId,
          question: question,
          answer: answer,
          title: question.substring(0, 30) + (question.length > 30 ? '...' : '')
        })
        
        this.loadHistoryList()
      } catch (error) {
        console.error('保存对话失败:', error)
      }
    },
    async loadHistoryList() {
      if (!this.currentUser.id) return
      
      try {
        const response = await this.$axios.post(`/ai/history/${this.currentUser.id}`)
        if (response.code === 20000) {
          this.historyList = response.data || []
        }
      } catch (error) {
        console.error('加载历史记录失败:', error)
      }
    },
    async loadHistory(item) {
      try {
        const response = await this.$axios.post(`/ai/session/${item.sessionId}`)
        if (response.code === 20000) {
          this.messages = response.data.map(msg => ({
            role: msg.role,
            content: msg.content,
            time: new Date(msg.time)
          }))
          this.currentSessionId = item.sessionId
          this.scrollToBottom()
        }
      } catch (error) {
        console.error('加载会话失败:', error)
        this.$message.error('加载会话失败')
      }
    },
    clearChat() {
      this.messages = []
      this.currentSessionId = null
      this.addWelcomeMessage()
      this.$message.success('对话已清空')
    },
    addWelcomeMessage() {
      this.messages.push({
        role: 'assistant',
        content: '你好！📚 我是你的书籍知识助手\n\n我可以帮你：\n• 推荐各类优质书籍\n• 解答关于书籍的问题\n• 分享阅读建议和心得\n\n请告诉我你感兴趣的书籍类型或主题，我会为你推荐合适的书籍！😊',
        time: new Date()
      })
    },
    async deleteSession(sessionId) {
      this.$confirm('确定要删除此会话吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await this.$axios.post(`/ai/delete/${sessionId}`)
          this.$message.success('删除成功')
          if (this.currentSessionId === sessionId) {
            this.clearChat()
          }
          this.loadHistoryList()
        } catch (error) {
          console.error('删除会话失败:', error)
          this.$message.error('删除会话失败')
        }
      }).catch(() => {
      })
    },
    async clearHistory() {
      try {
        await this.$axios.post(`/ai/clear/${this.currentUser.id}`)
        this.historyList = []
        this.$message.success('历史记录已清空')
      } catch (error) {
        console.error('清空历史记录失败:', error)
        this.$message.error('清空历史记录失败')
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const chatMessages = this.$refs.chatMessages
        if (chatMessages) {
          chatMessages.scrollTop = chatMessages.scrollHeight
        }
      })
    },
    formatTime(date) {
      if (!date) return ''
      const d = new Date(date)
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      return `${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.ai-chat-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  flex-shrink: 0;
  margin-bottom: 20px;
}

.page-header h1 {
  color: #545c64;
  margin: 0 0 10px 0;
  font-size: 24px;
}

.page-header p {
  color: #909399;
  margin: 0;
}

.page-content {
  flex: 1;
  display: flex;
  gap: 20px;
  min-height: 0;
}

.chat-section {
  flex: 5;
  min-width: 0;
}

.history-section {
  flex: 1;
  min-width: 0;
}

.chat-card, .history-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 0;
}

.chat-messages::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.chat-input {
  flex-shrink: 0;
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

.message {
  display: flex;
  margin-bottom: 20px;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 14px;
}

.user-avatar {
  background-color: #409eff;
  margin-left: 15px;
}

.ai-avatar {
  background-color: #67c23a;
  margin-right: 15px;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
  word-break: break-word;
}

.user-message .message-text {
  background-color: #409eff;
  color: white;
}

.ai-message .message-text {
  background-color: white;
  color: #303133;
}

.markdown-content :deep(strong) {
  font-weight: bold;
}

.markdown-content :deep(em) {
  font-style: italic;
}

.markdown-content :deep(code) {
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.markdown-content :deep(h1) {
  font-size: 24px;
  font-weight: bold;
  margin: 16px 0 8px;
}

.markdown-content :deep(h2) {
  font-size: 20px;
  font-weight: bold;
  margin: 14px 0 7px;
}

.markdown-content :deep(h3) {
  font-size: 18px;
  font-weight: bold;
  margin: 12px 0 6px;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 8px 0;
  color: #606266;
}

.markdown-content :deep(ul) {
  margin: 8px 0;
  padding-left: 24px;
}

.markdown-content :deep(li) {
  margin: 4px 0;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.user-message .message-time {
  text-align: right;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.hint {
  font-size: 12px;
  color: #909399;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.history-list::-webkit-scrollbar {
  width: 6px;
}

.history-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.history-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.history-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.empty-history {
  text-align: center;
  color: #909399;
  padding: 40px 20px;
}

.history-item {
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background-color 0.3s;
}

.history-item:hover {
  background-color: #f5f7fa;
}

.history-item-content {
  flex: 1;
  cursor: pointer;
  min-width: 0;
}

.delete-btn {
  color: #f56c6c;
  margin-left: 10px;
  opacity: 0;
  transition: opacity 0.3s;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.history-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-title i {
  margin-right: 5px;
  color: #409eff;
}

.history-time {
  font-size: 12px;
  color: #909399;
}
</style>
