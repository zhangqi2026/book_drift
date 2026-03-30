<template>
  <div class="ai-chat-page">
    <!-- 粒子背景 -->
    <div class="particles-bg">
      <div v-for="i in 30" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    
    <!-- 装饰性背景元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="grid-lines"></div>
    </div>
    
    <div class="page-content-wrapper">
      <div class="page-header slide-in">
        <h1>
          <span class="title-glow">AI 智能问答</span>
        </h1>
        <p>欢迎使用AI助手，我可以帮你解答各种问题</p>
      </div>
      
      <div class="page-content">
        <div class="chat-section">
          <div class="chat-card section-box">
            <div class="card-header">
              <span class="section-title">对话界面</span>
              <el-button type="text" class="link-btn" @click="clearChat">新建对话</el-button>
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
                  <el-button type="primary" @click="sendMessage" :loading="loading" class="send-btn">发送</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="history-section">
          <div class="history-card section-box">
            <div class="card-header">
              <span class="section-title">历史记录</span>
              <el-button type="text" class="link-btn" @click="clearHistory" size="small">清空</el-button>
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
          </div>
        </div>
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
    getParticleStyle(index) {
      const size = Math.random() * 4 + 2
      const duration = Math.random() * 20 + 10
      const delay = Math.random() * 10
      return {
        width: `${size}px`,
        height: `${size}px`,
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animationDuration: `${duration}s`,
        animationDelay: `${delay}s`
      }
    },
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
      this.$message.success('已开始新对话')
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
  min-height: 100vh;
  background: linear-gradient(135deg, #fef9f0 0%, #f5f0e6 50%, #e8f5e2 100%);
  position: relative;
  overflow-x: hidden;
}

* {
  box-sizing: border-box;
}

/* 粒子背景 */
.particles-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: radial-gradient(circle, #abf0d1, #d4eea7);
  border-radius: 50%;
  box-shadow: 0 0 10px #abf0d1, 0 0 20px #d4eea7;
  animation: particleFloat linear infinite;
  pointer-events: none;
}

@keyframes particleFloat {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-100px) rotate(720deg);
    opacity: 0;
  }
}

/* 装饰性背景元素 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border: 1px solid rgba(171, 240, 209, 0.2);
  box-shadow: 0 0 40px rgba(171, 240, 209, 0.15), inset 0 0 40px rgba(171, 240, 209, 0.08);
  animation: float 12s ease-in-out infinite;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg) scale(1);
  }
  50% {
    transform: translateY(-30px) rotate(180deg) scale(1.03);
  }
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(171, 240, 209, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(171, 240, 209, 0.04) 1px, transparent 1px);
  background-size: 60px 60px;
}

.page-content-wrapper {
  position: relative;
  z-index: 10;
  padding: 20px;
  width: 100%;
  overflow-x: hidden;
  height: 100vh;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.slide-in {
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-header {
  flex-shrink: 0;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(171, 240, 209, 0.4);
  box-shadow: 5px 3px 10px rgba(0, 0, 0, 0.07);
  padding: 20px 28px;
  text-align: center;
  position: relative;
}

.page-header::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #abf0d1, #d4eea7, #fef1d1, #abf0d1);
  border-radius: 20px;
  z-index: -1;
  background-size: 400% 400%;
  animation: borderGlow 4s ease infinite;
  opacity: 0.5;
}

@keyframes borderGlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.page-header h1 {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 800;
}

.title-glow {
  background: linear-gradient(135deg, #6b9a8a 0%, #7a9d5a 50%, #c4a77a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  color: #8a9a8a;
  margin: 0;
  font-size: 14px;
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

/* 区域通用样式 */
.section-box {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(171, 240, 209, 0.3);
  box-shadow: 5px 3px 10px rgba(0, 0, 0, 0.07);
  padding: 20px 24px;
  margin-bottom: 20px;
  transition: all 0.35s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.section-box:hover {
  box-shadow: 6px 4px 12px rgba(0, 0, 0, 0.1);
}

.chat-card, .history-card {
  margin-bottom: 0;
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  flex-shrink: 0;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #5a6a5a;
  margin: 0;
}

.link-btn {
  color: #6a9a7a;
  font-size: 14px;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.link-btn:hover {
  color: #7a9d5a;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0 24px 24px 24px;
  box-sizing: border-box;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.08), rgba(212, 238, 167, 0.08));
  border-radius: 16px;
  border: 1px solid rgba(171, 240, 209, 0.2);
  min-height: 0;
}

.chat-messages::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(171, 240, 209, 0.1);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #abf0d1, #d4eea7);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #7a9d5a, #6b9a8a);
}

.chat-input {
  flex-shrink: 0;
  padding: 20px 0 0 0;
  border-top: 1px solid rgba(171, 240, 209, 0.2);
  margin-top: 20px;
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
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #6b9a8a, #7a9d5a);
  margin-left: 15px;
}

.ai-avatar {
  background: linear-gradient(135deg, #abf0d1, #d4eea7);
  margin-right: 15px;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 14px 18px;
  border-radius: 16px;
  line-height: 1.6;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-message .message-text {
  background: linear-gradient(135deg, #6b9a8a, #7a9d5a);
  color: white;
}

.ai-message .message-text {
  background: white;
  color: #5a6a5a;
  border: 1px solid rgba(171, 240, 209, 0.3);
}

.markdown-content :deep(strong) {
  font-weight: bold;
}

.markdown-content :deep(em) {
  font-style: italic;
}

.markdown-content :deep(code) {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.3), rgba(212, 238, 167, 0.3));
  padding: 2px 8px;
  border-radius: 6px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: #5a6a5a;
}

.markdown-content :deep(h1) {
  font-size: 22px;
  font-weight: bold;
  margin: 14px 0 8px;
  color: #5a6a5a;
}

.markdown-content :deep(h2) {
  font-size: 19px;
  font-weight: bold;
  margin: 12px 0 6px;
  color: #5a6a5a;
}

.markdown-content :deep(h3) {
  font-size: 17px;
  font-weight: bold;
  margin: 10px 0 5px;
  color: #5a6a5a;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #7a9d5a;
  padding-left: 16px;
  margin: 8px 0;
  color: #6a7a6a;
  background: rgba(171, 240, 209, 0.1);
  border-radius: 0 8px 8px 0;
  padding: 12px 16px;
}

.markdown-content :deep(ul) {
  margin: 8px 0;
  padding-left: 24px;
}

.markdown-content :deep(li) {
  margin: 4px 0;
  color: #6a7a6a;
}

.message-time {
  font-size: 12px;
  color: #8a9a8a;
  margin-top: 6px;
}

.user-message .message-time {
  text-align: right;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.hint {
  font-size: 13px;
  color: #8a9a8a;
}

.send-btn {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border: none;
  color: #4a6a5a;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(171, 240, 209, 0.4);
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
  background: rgba(171, 240, 209, 0.1);
  border-radius: 3px;
}

.history-list::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #abf0d1, #d4eea7);
  border-radius: 3px;
}

.history-list::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #7a9d5a, #6b9a8a);
}

.empty-history {
  text-align: center;
  color: #8a9a8a;
  padding: 40px 20px;
  font-size: 14px;
}

.history-item {
  padding: 14px;
  border-bottom: 1px solid rgba(171, 240, 209, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  border-radius: 12px;
  margin-bottom: 8px;
}

.history-item:hover {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  transform: translateX(4px);
}

.history-item-content {
  flex: 1;
  cursor: pointer;
  min-width: 0;
}

.delete-btn {
  color: #f97316;
  margin-left: 10px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.history-title {
  font-size: 14px;
  color: #5a6a5a;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.history-title i {
  margin-right: 6px;
  color: #7a9d5a;
}

.history-time {
  font-size: 12px;
  color: #8a9a8a;
}
</style>
