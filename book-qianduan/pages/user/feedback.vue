<template>
  <div class="feedback-container">
    <h1>反馈与建议</h1>
    <p>如有任何问题或建议，请在此提交，我们会尽快处理</p>
    
    <el-card class="feedback-card">
      <div slot="header">
        <span>提交反馈</span>
      </div>
      <el-form :model="feedbackForm" :rules="rules" ref="feedbackForm" label-width="80px">
        <el-form-item label="反馈内容" prop="content">
          <el-input
            type="textarea"
            v-model="feedbackForm.content"
            placeholder="请详细描述您的问题或建议..."
            :rows="5"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <div class="feedback-history">
      <h2>我的反馈历史</h2>
      <el-card>
        <div slot="header" class="card-header">
          <span>反馈记录</span>
          <el-pagination
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-size="pageSize"
            layout="total, prev, pager, next, jumper"
            :total="total"
          >
          </el-pagination>
        </div>
        <el-table :data="feedbackList" stripe v-loading="loading">
          <el-table-column label="编号" width="80" align="center">
            <template slot-scope="scope">
              {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column label="反馈内容" min-width="120">
            <template slot-scope="scope">
              <el-button type="primary" size="small" @click="viewContent(scope.row)">查看内容</el-button>
            </template>
          </el-table-column>
          <el-table-column label="回复" min-width="120">
            <template slot-scope="scope">
              <el-button v-if="scope.row.reply" type="success" size="small" @click="viewContent(scope.row)">查看回复</el-button>
              <el-tag v-else type="info" size="small">未回复</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
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
        <div v-if="feedbackList.length === 0 && !loading" class="empty-data">暂无反馈记录</div>
      </el-card>
    </div>

    <el-dialog title="反馈内容详情" :visible.sync="contentDialogVisible" width="50%">
      <div class="feedback-content">
        <p><strong>提交时间：</strong>{{ formatDateTime(currentFeedback.createTime) }}</p>
        <p><strong>状态：</strong><el-tag :type="getStatusType(currentFeedback.status)">{{ getStatusText(currentFeedback.status) }}</el-tag></p>
        <el-divider></el-divider>
        <p><strong>反馈内容：</strong></p>
        <div class="content-text">{{ currentFeedback.content }}</div>
        <div v-if="currentFeedback.reply">
          <el-divider></el-divider>
          <p><strong>管理员回复：</strong></p>
          <div class="content-text reply-text">{{ currentFeedback.reply }}</div>
          <p style="margin-top: 10px; color: #909399; font-size: 12px;">回复时间：{{ formatDateTime(currentFeedback.replyTime) }}</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="contentDialogVisible = false">关闭</el-button>
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
  padding: 20px;
}

h1 {
  color: #545c64;
  margin-bottom: 10px;
}

p {
  color: #909399;
  margin-bottom: 40px;
}

.feedback-card {
  margin-bottom: 40px;
}

.feedback-history {
  margin-top: 40px;
}

.feedback-history h2 {
  color: #545c64;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.feedback-content {
  padding: 10px;
}

.feedback-content p {
  margin: 10px 0;
  line-height: 1.6;
}

.content-text {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-top: 10px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
}

.reply-text {
  background-color: #e6f7ff;
  border-left: 4px solid #1890ff;
}
</style>
