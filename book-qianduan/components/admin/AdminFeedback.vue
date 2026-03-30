<template>
  <div class="admin-feedback">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-box slide-in">
        <h1 class="welcome-title">
          <span class="title-glow">用户反馈管理</span>
        </h1>
        <p class="welcome-subtitle">处理和回复用户提交的所有反馈</p>
      </div>
    </div>
    
    <!-- 反馈列表 -->
    <div class="section-box">
      <div class="section-header">
        <h3 class="section-title">所有反馈</h3>
        <el-pagination
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next, jumper"
          :total="total"
          class="custom-pagination"
        >
        </el-pagination>
      </div>
      <el-table :data="feedbackList" stripe v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户" min-width="120" />
        <el-table-column label="反馈内容" min-width="120">
          <template slot-scope="scope">
            <el-button type="primary" size="small" @click="viewContent(scope.row)" class="action-btn">查看内容</el-button>
          </template>
        </el-table-column>
        <el-table-column label="回复" min-width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.reply" type="success" size="small" class="custom-tag">已回复</el-tag>
            <el-tag v-else type="info" size="small" class="custom-tag">未回复</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template slot-scope="scope">
            <span :class="['status-tag', getStatusClass(scope.row.status)]">
              {{ getStatusText(scope.row.status) }}
            </span>
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
            <el-button type="primary" size="small" @click="openStatusDialog(scope.row)" class="action-btn">更改状态</el-button>
            <el-button type="success" size="small" @click="openReplyDialog(scope.row)" class="action-btn">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="feedbackList.length === 0 && !loading" class="empty-data">暂无反馈记录</div>
    </div>

    <el-dialog title="反馈内容详情" :visible.sync="contentDialogVisible" width="50%" class="custom-dialog">
      <div class="feedback-content">
        <p><strong>用户：</strong>{{ currentFeedback.username }}</p>
        <p><strong>提交时间：</strong>{{ formatDateTime(currentFeedback.createTime) }}</p>
        <p><strong>状态：</strong><span :class="['status-tag', getStatusClass(currentFeedback.status)]">{{ getStatusText(currentFeedback.status) }}</span></p>
        <el-divider></el-divider>
        <p><strong>反馈内容：</strong></p>
        <div class="content-text">{{ currentFeedback.content }}</div>
        <div v-if="currentFeedback.reply">
          <el-divider></el-divider>
          <p><strong>管理员回复：</strong></p>
          <div class="content-text reply-text">{{ currentFeedback.reply }}</div>
          <p style="margin-top: 10px; color: #8a9a8a; font-size: 12px;">回复时间：{{ formatDateTime(currentFeedback.replyTime) }}</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="contentDialogVisible = false" class="action-btn">关闭</el-button>
      </span>
    </el-dialog>

    <el-dialog title="更改反馈状态" :visible.sync="statusDialogVisible" width="400px" class="custom-dialog">
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="当前状态">
          <span :class="['status-tag', getStatusClass(currentStatusFeedback.status)]">{{ getStatusText(currentStatusFeedback.status) }}</span>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="statusForm.newStatus" placeholder="请选择新状态" style="width: 100%" class="status-select">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="statusDialogVisible = false" class="action-btn">取消</el-button>
        <el-button type="primary" @click="confirmUpdateStatus" class="action-btn">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="回复反馈" :visible.sync="replyDialogVisible" width="500px" class="custom-dialog">
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
        <el-button @click="replyDialogVisible = false" class="action-btn">取消</el-button>
        <el-button type="primary" @click="confirmReply" class="action-btn">确定回复</el-button>
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
    getStatusClass(status) {
      switch (status) {
        case 'PENDING': return 'status-pending'
        case 'PROCESSED': return 'status-processing'
        case 'CLOSED': return 'status-closed'
        default: return ''
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
  position: relative;
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
  margin: 0 0 15px 0;
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
  box-shadow: 6px 4px 12px rgba(0, 0, 0, 0.1);
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

/* 表格样式 */
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

/* 分页器样式 */
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

/* 按钮样式 */
.action-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

/* 标签样式 */
.custom-tag {
  border-radius: 8px;
  font-weight: 500;
}

/* 弹窗样式 */
.custom-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border-radius: 4px 4px 0 0;
}

.custom-dialog >>> .el-dialog__title {
  color: #5a6a5a;
  font-weight: 700;
}

.empty-data {
  text-align: center;
  color: #8a9a8a;
  padding: 20px;
}

.feedback-content {
  padding: 10px;
}

.feedback-content p {
  margin: 10px 0;
  line-height: 1.6;
  color: #6a7a6a;
}

.content-text {
  padding: 15px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border-radius: 8px;
  margin-top: 10px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  color: #5a6a5a;
}

.reply-text {
  background: rgba(171, 240, 209, 0.2);
  border-left: 4px solid #abf0d1;
}

/* 自定义状态标签样式 */
.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;
}

.status-pending {
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #e65100;
  border: 1px solid #ffcc80;
}

.status-processing {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  color: #2e7d32;
  border: 1px solid #a5d6a7;
}

.status-closed {
  background: linear-gradient(135deg, #f5f5f5, #e0e0e0);
  color: #616161;
  border: 1px solid #bdbdbd;
}

/* 下拉选择框样式 */
.status-select {
  width: 100%;
}

.status-select >>> .el-input__inner {
  border-radius: 8px !important;
  border: 1px solid rgba(171, 240, 209, 0.5) !important;
  background: rgba(255, 255, 255, 0.9) !important;
  transition: all 0.3s ease !important;
}

.status-select >>> .el-input__inner:hover {
  border-color: rgba(171, 240, 209, 0.8) !important;
}

.status-select >>> .el-input__inner:focus {
  border-color: #7a9d5a !important;
  box-shadow: 0 0 0 3px rgba(171, 240, 209, 0.2) !important;
}

.status-select >>> .el-input__suffix {
  transition: all 0.3s ease;
}

.status-select >>> .el-input__suffix .el-select__caret {
  color: #7a9d5a;
}
</style>
