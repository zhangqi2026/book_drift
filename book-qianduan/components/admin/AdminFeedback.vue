<template>
  <div class="admin-feedback">
    <h2>用户反馈管理</h2>
    <el-card class="feedback-card">
      <div slot="header" class="card-header">
        <span>所有反馈</span>
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
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户" min-width="120" />
        <el-table-column label="反馈内容" min-width="120">
          <template slot-scope="scope">
            <el-button type="primary" size="small" @click="viewContent(scope.row)">查看内容</el-button>
          </template>
        </el-table-column>
        <el-table-column label="回复" min-width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.reply" type="success" size="small">已回复</el-tag>
            <el-tag v-else type="info" size="small">未回复</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="回复时间" width="160">
          <template slot-scope="scope">
            {{ scope.row.replyTime ? formatDateTime(scope.row.replyTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template slot-scope="scope">
            <el-button type="primary" size="small" @click="openStatusDialog(scope.row)">更改状态</el-button>
            <el-button type="success" size="small" @click="openReplyDialog(scope.row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="feedbackList.length === 0 && !loading" class="empty-data">暂无反馈记录</div>
    </el-card>

    <el-dialog title="反馈内容详情" :visible.sync="contentDialogVisible" width="50%">
      <div class="feedback-content">
        <p><strong>用户：</strong>{{ currentFeedback.username }}</p>
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

    <el-dialog title="更改反馈状态" :visible.sync="statusDialogVisible" width="400px">
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(currentStatusFeedback.status)">{{ getStatusText(currentStatusFeedback.status) }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="statusForm.newStatus" placeholder="请选择新状态" style="width: 100%">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateStatus">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="回复反馈" :visible.sync="replyDialogVisible" width="500px">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="当前回复" v-if="currentReplyFeedback.reply">
          <div class="content-text">{{ currentReplyFeedback.reply }}</div>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            type="textarea"
            v-model="replyForm.reply"
            :rows="5"
            placeholder="请输入回复内容..."
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReply">确定回复</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AdminFeedback',
  data() {
    return {
      feedbackList: [],
      contentDialogVisible: false,
      currentFeedback: {},
      statusDialogVisible: false,
      currentStatusFeedback: {},
      statusForm: {
        newStatus: ''
      },
      replyDialogVisible: false,
      currentReplyFeedback: {},
      replyForm: {
        reply: ''
      },
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false
    }
  },
  mounted() {
    this.fetchAllFeedbacks()
  },
  methods: {
    viewContent(feedback) {
      this.currentFeedback = feedback
      this.contentDialogVisible = true
    },
    openStatusDialog(feedback) {
      this.currentStatusFeedback = feedback
      this.statusForm.newStatus = feedback.status
      this.statusDialogVisible = true
    },
    openReplyDialog(feedback) {
      this.currentReplyFeedback = feedback
      this.replyForm.reply = feedback.reply || ''
      this.replyDialogVisible = true
    },
    confirmUpdateStatus() {
      if (!this.statusForm.newStatus) {
        this.$message.warning('请选择新状态')
        return
      }
      this.updateStatus(this.currentStatusFeedback.id, this.statusForm.newStatus)
    },
    confirmReply() {
      if (!this.replyForm.reply) {
        this.$message.warning('请输入回复内容')
        return
      }
      this.replyFeedback(this.currentReplyFeedback.id, this.replyForm.reply)
    },
    fetchAllFeedbacks() {
      this.loading = true
      this.$axios.post(`/feedback/all/page/${this.pageSize}/${this.currentPage}`)
        .then(response => {
          if (response.code === 20000) {
            this.feedbackList = response.data.records || []
            this.total = response.data.total || 0
          }
        })
        .catch(error => {
          console.error('获取反馈列表失败:', error)
          this.$message.error('获取反馈列表失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    updateStatus(id, status) {
      this.$axios.post('/feedback/updateStatus', {
        id: id,
        status: status
      })
      .then(response => {
        if (response.code === 20000) {
          this.$message.success('状态更新成功')
          this.statusDialogVisible = false
          this.fetchAllFeedbacks()
        } else {
          this.$message.error('状态更新失败')
        }
      })
      .catch(error => {
        console.error('更新状态失败:', error)
        this.$message.error('更新状态失败')
      })
    },
    replyFeedback(id, reply) {
      this.$axios.post('/feedback/reply', {
        id: id,
        reply: reply
      })
      .then(response => {
        if (response.code === 20000) {
          this.$message.success('回复成功')
          this.replyDialogVisible = false
          this.fetchAllFeedbacks()
        } else {
          this.$message.error('回复失败')
        }
      })
      .catch(error => {
        console.error('回复失败:', error)
        this.$message.error('回复失败')
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
      this.fetchAllFeedbacks()
    }
  }
}
</script>

<style scoped>
.admin-feedback {
  padding: 20px;
}

h2 {
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.feedback-card {
  margin-top: 20px;
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
