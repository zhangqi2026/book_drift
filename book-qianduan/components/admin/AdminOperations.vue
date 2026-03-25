<template>
  <div class="admin-operations">
    <div class="page-header">
      <h2>书籍操作</h2>
    </div>
    
    <!-- 操作统计 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card class="stat-card">
            <div class="stat-content">
              <i class="el-icon-document stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ stats.totalBooks }}</div>
                <div class="stat-label">总书籍数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminOperations',
  data() {
    return {
      stats: {
        totalBooks: 0
      }
    }
  },
  mounted() {
    this.fetchStats()
  },
  methods: {
    async fetchStats() {
      try {
        const response = await this.$axios.post('/bookInfo/condition/1/1')
        if (response.code === 20000) {
          this.stats.totalBooks = response.data.total || 0
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.admin-operations {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.stats-container {
  margin-bottom: 40px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 40px;
  color: #F56C6C;
  margin-right: 20px;
}

.stat-text {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>
