<template>
  <div class="feedback-container">
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
    
    <div class="feedback-content">
      <!-- 标题区域 -->
      <div class="welcome-section">
        <div class="welcome-box slide-in">
          <h1 class="welcome-title">
            <span class="title-glow">反馈与建议</span>
          </h1>
          <p class="welcome-subtitle">如有任何问题或建议，请在此提交，我们会尽快处理</p>
        </div>
      </div>
      
      <!-- 提交反馈区域 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">提交反馈</h3>
        </div>
        <el-form :model="feedbackForm" :rules="rules" ref="feedbackForm" label-width="80px" class="custom-form">
          <el-form-item label="反馈内容" prop="content">
            <el-input
              type="textarea"
              v-model="feedbackForm.content"
              placeholder="请详细描述您的问题或建议..."
              :rows="5"
              class="custom-textarea"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="custom-btn custom-btn-primary" @click="submitFeedback">提交反馈</el-button>
            <el-button class="custom-btn custom-btn-default" @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 反馈历史区域 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">我的反馈历史</h3>
          <el-pagination
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total"
            class="custom-pagination"
          />
        </div>
        <div class="table-wrapper">
          <el-table :data="feedbackList" stripe v-loading="loading" class="custom-table">
            <el-table-column label="编号" width="80" align="center">
              <template slot-scope="scope">
                {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column label="反馈内容" min-width="120">
              <template slot-scope="scope">
                <el-button type="text" class="link-btn" @click="viewContent(scope.row)">查看内容</el-button>
              </template>
            </el-table-column>
            <el-table-column label="回复" min-width="120">
              <template slot-scope="scope">
                <el-button v-if="scope.row.reply" type="text" class="link-btn link-btn-success" @click="viewContent(scope.row)">查看回复</el-button>
                <el-tag v-else type="info" size="small" class="custom-tag">未回复</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)" class="custom-tag">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="180">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="回复时间" width="180">
              <template slot-scope="scope">
                {{ scope.row.replyTime ? formatDateTime(scope.row.replyTime) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          <div v-if="feedbackList.length === 0 && !loading" class="empty-state">
            <p>暂无反馈记录</p>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="反馈内容详情" :visible.sync="contentDialogVisible" width="50%" class="custom-dialog">
      <div class="feedback-content-detail">
        <p><strong>提交时间：</strong>{{ formatDateTime(currentFeedback.createTime) }}</p>
        <p><strong>状态：</strong><el-tag :type="getStatusType(currentFeedback.status)" class="custom-tag">{{ getStatusText(currentFeedback.status) }}</el-tag></p>
        <el-divider class="custom-divider"></el-divider>
        <p><strong>反馈内容：</strong></p>
        <div class="content-text">{{ currentFeedback.content }}</div>
        <div v-if="currentFeedback.reply">
          <el-divider class="custom-divider"></el-divider>
          <p><strong>管理员回复：</strong></p>
          <div class="content-text reply-text">{{ currentFeedback.reply }}</div>
          <p style="margin-top: 10px; color: #8a9a8a; font-size: 12px;">回复时间：{{ formatDateTime(currentFeedback.replyTime) }}</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button class="custom-btn custom-btn-default" @click="contentDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      feedbackForm: {
        content: ''
      },
      rules: {
        content: [
          { required: true, message: '请输入反馈内容', trigger: 'blur' },
          { min: 5, message: '反馈内容至少5个字符', trigger: 'blur' }
        ]
      },
      feedbackList: [],
      currentUser: {},
      contentDialogVisible: false,
      currentFeedback: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchFeedbackHistory()
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
    getCurrentUser() {
      const user = localStorage.getItem('user')
      if (user) {
        this.currentUser = JSON.parse(user)
      } else {
        this.$router.push('/login')
      }
    },
    viewContent(feedback) {
      this.currentFeedback = feedback
      this.contentDialogVisible = true
    },
    submitFeedback() {
      this.$refs.feedbackForm.validate(async (valid) => {
        if (valid) {
          try {
            const response = await this.$axios.post('/feedback/submit', {
              userId: this.currentUser.id,
              content: this.feedbackForm.content
            })
            
            if (response.code === 20000) {
              this.$message.success('反馈提交成功')
              this.resetForm()
              this.fetchFeedbackHistory()
            } else {
              this.$message.error('反馈提交失败')
            }
          } catch (error) {
            console.error('提交反馈失败:', error)
            this.$message.error('提交反馈失败')
          }
        }
      })
    },
    resetForm() {
      this.$refs.feedbackForm.resetFields()
    },
    fetchFeedbackHistory() {
      if (!this.currentUser) return
      
      this.loading = true
      this.$axios.post(`/feedback/user/page/${this.currentUser.id}/${this.pageSize}/${this.currentPage}`)
        .then(response => {
          if (response.code === 20000) {
            this.feedbackList = response.data.records || []
            this.total = response.data.total || 0
          }
        })
        .catch(error => {
          console.error('获取反馈历史失败:', error)
        })
        .finally(() => {
          this.loading = false
        })
    },
    getStatusType(status) {
      switch (status) {
        case 'PENDING': return 'warning'
        case 'PROCESSED': return 'primary'
        case 'CLOSED': return 'success'
        default: return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'PENDING': return '待处理'
        case 'PROCESSED': return '处理中'
        case 'CLOSED': return '已关闭'
        default: return '未知'
      }
    },
    formatDateTime(dateTime) {
      if (!dateTime) return '-'  
      const date = new Date(dateTime)  
      if (isNaN(date.getTime())) return '-'  
      
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchFeedbackHistory()
    }
  }
}
</script>

<style scoped>
.feedback-container {
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

.feedback-content {
  position: relative;
  z-index: 10;
  padding: 20px;
  width: 100%;
  overflow-x: hidden;
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

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 20px;
}

.welcome-box {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(171, 240, 209, 0.4);
  box-shadow: 5px 3px 10px rgba(0, 0, 0, 0.07);
  padding: 20px 28px;
  text-align: center;
  position: relative;
}

.welcome-box::before {
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

.welcome-title {
  font-size: 24px;
  font-weight: 800;
  margin: 0 0 6px 0;
}

.title-glow {
  background: linear-gradient(135deg, #6b9a8a 0%, #7a9d5a 50%, #c4a77a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 14px;
  color: #8a9a8a;
  margin: 0;
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
}

.section-box:hover {
  transform: translateY(-2px);
  box-shadow: 6px 4px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #5a6a5a;
  margin: 0;
}

/* 表单样式 */
.custom-form >>> .el-form-item__label {
  color: #5a6a5a;
  font-weight: 500;
}

.custom-textarea >>> .el-textarea__inner {
  border-radius: 12px;
  border: 1px solid rgba(171, 240, 209, 0.4);
  background: rgba(255, 255, 255, 0.9);
  color: #5a6a5a;
  transition: all 0.3s ease;
}

.custom-textarea >>> .el-textarea__inner:focus {
  border-color: #abf0d1;
  box-shadow: 0 0 0 3px rgba(171, 240, 209, 0.2);
}

/* 按钮样式 */
.custom-btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.35s ease;
  border: none;
}

.custom-btn-primary {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  color: #4a6a5a;
  box-shadow: 5px 2px 10px rgba(0, 0, 0, 0.06);
}

.custom-btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  color: #4a6a5a;
}

.custom-btn-default {
  background: rgba(171, 240, 209, 0.15);
  color: #5a6a5a;
  border: 1px solid rgba(171, 240, 209, 0.3);
  box-shadow: 4px 2px 8px rgba(0, 0, 0, 0.05);
}

.custom-btn-default:hover {
  background: rgba(171, 240, 209, 0.25);
  transform: translateY(-3px);
  box-shadow: 5px 3px 10px rgba(0, 0, 0, 0.08);
  color: #5a6a5a;
}

/* 链接按钮 */
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

.link-btn-success {
  color: #67c23a;
}

.link-btn-success:hover {
  color: #85ce61;
}

/* 表格样式 */
.table-wrapper {
  position: relative;
}

.custom-table {
  border-radius: 12px;
  overflow: hidden;
}

.custom-table >>> th {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.2), rgba(212, 238, 167, 0.2));
  color: #5a6a5a;
  font-weight: 600;
  border: none;
}

.custom-table >>> td {
  color: #6a7a6a;
  border-color: rgba(171, 240, 209, 0.15);
}

.custom-table >>> .el-table__row:hover > td {
  background: rgba(171, 240, 209, 0.08);
}

.custom-tag {
  border-radius: 8px;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 25px 20px;
  color: #8a9a8a;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

.custom-pagination {
  display: flex;
  justify-content: flex-end;
}

.custom-pagination >>> .el-pager li {
  border-radius: 8px;
  margin: 0 4px;
  font-weight: 500;
}

.custom-pagination >>> .el-pager li.active {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  color: #4a6a5a;
}

.custom-pagination >>> .btn-prev,
.custom-pagination >>> .btn-next {
  border-radius: 8px;
}

/* 对话框样式 */
.custom-dialog >>> .el-dialog {
  border-radius: 20px;
}

.custom-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.1), rgba(212, 238, 167, 0.1));
  border-radius: 20px 20px 0 0;
  padding: 20px 24px;
}

.custom-dialog >>> .el-dialog__title {
  color: #5a6a5a;
  font-weight: 700;
}

.feedback-content-detail {
  padding: 10px;
}

.feedback-content-detail p {
  margin: 10px 0;
  line-height: 1.6;
  color: #6a7a6a;
}

.content-text {
  padding: 15px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  border-radius: 12px;
  border: 1px solid rgba(171, 240, 209, 0.25);
  margin-top: 10px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  color: #5a6a5a;
}

.reply-text {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.2), rgba(212, 238, 167, 0.2));
  border-left: 4px solid #abf0d1;
}

.custom-divider {
  margin: 20px 0;
  border-top: 1px solid rgba(171, 240, 209, 0.3);
}
</style>
