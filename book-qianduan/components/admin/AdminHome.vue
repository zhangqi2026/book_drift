<template>
  <div class="admin-home">
    <h1>欢迎回来，{{ currentUser.name }}</h1>
    <p>这是您的管理控制台</p>
    
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <i class="el-icon-reading stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ stats.totalBooks }}</div>
                <div class="stat-label">总书籍数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <i class="el-icon-user stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ stats.totalUsers }}</div>
                <div class="stat-label">总用户数</div>
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
  name: 'AdminHome',
  props: {
    currentUser: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      stats: {
        totalBooks: 0,
        totalUsers: 0
      }
    }
  },
  mounted() {
    this.fetchStats()
  },
  methods: {
    async fetchStats() {
      try {
        // 调用后端 API 获取统计数据
        const [booksRes, usersRes] = await Promise.all([
          this.$axios.post('/bookInfo/condition/1/1'),
          this.$axios.post('/sysUser/condition/1/1')
        ])
        
        if (booksRes.code === 20000) {
          this.stats.totalBooks = booksRes.data.total || 0
        }
        
        if (usersRes.code === 20000) {
          this.stats.totalUsers = usersRes.data.total || 0
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        this.$message.error('获取统计数据失败')
      }
    }
  }
}
</script>

<style scoped>
.admin-home {
  padding: 20px;
}

h1 {
  color: #303133;
  margin-bottom: 10px;
}

p {
  color: #909399;
  margin-bottom: 40px;
}

.stats-container {
  margin-bottom: 40px;
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
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
