<template>
  <div class="admin-feedback">
    <h2>用户反馈管理</h2>
    <el-card class="feedback-card">
      <div slot="header">
        <span>所有反馈</span>
      </div>
      <el-table :data="feedbackList" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="content" label="反馈内容" />
        <el-table-column prop="status" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="200">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-select v-model="scope.row.status" @change="updateStatus(scope.row.id, scope.row.status)" placeholder="选择状态">
              <el-option label="待处理" value="PENDING" />
              <el-option label="处理中" value="PROCESSED" />
              <el-option label="已关闭" value="CLOSED" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="feedbackList.length === 0" class="empty-data">暂无反馈记录</div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AdminFeedback',
  data() {
    return {
      feedbackList: []
    }
  },
  mounted() {
    this.fetchAllFeedbacks()
  },
  methods: {
    fetchAllFeedbacks() {
      this.$axios.post('/feedback/all')
        .then(response => {
          if (response.code === 20000) {
            this.feedbackList = response.data
          }
        })
        .catch(error => {
          console.error('获取反馈列表失败:', error)
          this.$message.error('获取反馈列表失败')
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
        } else {
          this.$message.error('状态更新失败')
          // 恢复原状态
          this.fetchAllFeedbacks()
        }
      })
      .catch(error => {
        console.error('更新状态失败:', error)
        this.$message.error('更新状态失败')
        // 恢复原状态
        this.fetchAllFeedbacks()
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

.feedback-card {
  margin-top: 20px;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
