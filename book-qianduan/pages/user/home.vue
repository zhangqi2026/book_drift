<template>
  <div class="home-container">
    <h1>欢迎回来，{{ currentUser.name }}</h1>
    <p>这是您的个人首页</p>
    
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="stat-card" @click="goToBorrowedBooks" style="cursor: pointer;">
            <div class="stat-content">
              <i class="el-icon-reading stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ borrowedBooks }}</div>
                <div class="stat-label">已借阅书籍</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-content">
              <i class="el-icon-medal stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ unlockedBadges }}</div>
                <div class="stat-label">获得勋章</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-content">
              <i class="el-icon-document stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ donatedBooks }}</div>
                <div class="stat-label">已捐赠书籍</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="recent-activities">
      <h2>最近活动</h2>
      <el-timeline>
        <el-timeline-item
          v-for="(activity, index) in recentActivities"
          :key="index"
          :timestamp="activity.timestamp"
          placement="top">
          {{ activity.content }}
        </el-timeline-item>
      </el-timeline>
    </div>
    
    <!-- 捐赠记录 -->
    <div class="records-section">
      <h3>我的记录</h3>
      <el-card class="record-card" shadow="hover">
        <div slot="header">
          <span>捐赠记录（共 {{ donationTotal }} 本）</span>
        </div>
        <el-table :data="donationRecords" stripe size="small">
          <el-table-column prop="bookName" label="书籍名称" />
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column label="捐赠时间" width="180">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.donateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag size="small" :type="scope.row.bookStatus === 1 ? 'success' : 'primary'">
                {{ scope.row.bookStatus === 1 ? '待认领' : '已漂流' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="donationRecords.length === 0" class="empty-data">暂无捐赠记录</div>
        <el-pagination
          @current-change="handleDonationPageChange"
          :current-page="donationCurrentPage"
          :page-size="donationPageSize"
          layout="total, prev, pager, next, jumper"
          :total="donationTotal"
          style="margin-top: 15px; text-align: right;"
        />
      </el-card>
      
      <!-- 借阅记录 -->
      <el-card class="record-card" shadow="hover" style="margin-top: 20px;">
        <div slot="header">
          <span>借阅记录（共 {{ borrowTotal }} 本）</span>
        </div>
        <el-table :data="borrowRecords" stripe size="small">
          <el-table-column prop="bookName" label="书籍名称" />
          <el-table-column prop="author" label="作者" width="100" />
          <el-table-column prop="donorName" label="捐赠人" width="100">
            <template slot-scope="scope">
              {{ scope.row.donorName || '匿名捐赠者' }}
            </template>
          </el-table-column>
          <el-table-column label="借书时间" width="160">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.claimTime) }}
            </template>
          </el-table-column>
          <el-table-column label="还书时间" width="160">
            <template slot-scope="scope">
              {{ scope.row.returnTime ? formatDateTime(scope.row.returnTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="书籍状态" width="100">
            <template slot-scope="scope">
              <el-tag size="small" :type="getBookStatusTagType(scope.row.bookStatus)">
                {{ getBookStatusText(scope.row.bookStatus) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="borrowRecords.length === 0" class="empty-data">暂无借阅记录</div>
        <el-pagination
          @current-change="handleBorrowPageChange"
          :current-page="borrowCurrentPage"
          :page-size="borrowPageSize"
          layout="total, prev, pager, next, jumper"
          :total="borrowTotal"
          style="margin-top: 15px; text-align: right;"
        />
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentUser: {
        name: ''
      },
      donatedBooks: 0, // 已捐赠书籍数量
      borrowedBooks: 0, // 已借阅书籍数量
      unlockedBadges: 0, // 已解锁勋章数量
      recentActivities: [],
      donationRecords: [], // 捐赠记录列表
      borrowRecords: [], // 借阅记录列表
      // 捐赠记录分页
      donationCurrentPage: 1,
      donationPageSize: 10,
      donationTotal: 0,
      // 借阅记录分页
      borrowCurrentPage: 1,
      borrowPageSize: 10,
      borrowTotal: 0
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchUserStats()
    this.fetchRecentActivities()
  },
  methods: {
    getCurrentUser() {
      // 从本地存储获取用户信息
      const user = localStorage.getItem('user')
      if (user) {
        this.currentUser = JSON.parse(user)
      } else {
        // 如果没有用户信息，跳转到登录页面
        this.$router.push('/login')
      }
    },
    // 跳转到借阅书籍页面
    goToBorrowedBooks() {
      this.$router.push('/user/borrowed-books')
    },
    // 获取用户统计数据
    async fetchUserStats() {
      try {
        let donatedBooks = 0
        let borrowedBooks = 0
        
        // 1. 获取借阅记录（分页查询）
        const borrowRecordResponse = await this.$axios.post(`/bookClaimRecord/condition/${this.borrowPageSize}/${this.borrowCurrentPage}`, null, {
          params: { userId: this.currentUser.id }
        })
        
        if (borrowRecordResponse.code === 20000) {
          // 过滤出未归还的记录（returnTime 为空的）
          const borrowedRecords = borrowRecordResponse.data.records.filter(record => !record.returnTime)
          borrowedBooks = borrowedRecords.length || 0
          this.borrowedBooks = borrowedBooks
          // 获取借阅记录
          this.borrowRecords = borrowRecordResponse.data.records || []
          this.borrowTotal = borrowRecordResponse.data.total || 0
        }
            
        // 2. 获取实际的捐赠记录（按用户 ID 作为捐赠人查询书籍）
        const donateRecordResponse = await this.$axios.post(`/bookInfo/condition/${this.donationPageSize}/${this.donationCurrentPage}`, null, {
          params: { donorId: this.currentUser.id }
        })
        
        if (donateRecordResponse.code === 20000) {
          // 使用实际的捐赠记录总数
          donatedBooks = donateRecordResponse.data.total || 0
          this.donatedBooks = donatedBooks
          // 获取捐赠记录
          this.donationRecords = donateRecordResponse.data.records || []
          this.donationTotal = donateRecordResponse.data.total || 0
        }
        
        // 3. 从后端查询用户实际已获得的勋章数量
        const medalResponse = await this.$axios.post(`/userMedal/condition/1000/1`, null, {
          params: { userId: this.currentUser.id }
        })
        
        if (medalResponse.code === 20000) {
          this.unlockedBadges = medalResponse.data.total || 0
        }
      } catch (error) {
        console.error('获取用户统计数据失败:', error)
        this.$message.error('获取用户统计数据失败')
      }
    },
    // 处理捐赠记录分页变化
    handleDonationPageChange(val) {
      this.donationCurrentPage = val
      this.fetchUserStats()
    },
    // 处理借阅记录分页变化
    handleBorrowPageChange(val) {
      this.borrowCurrentPage = val
      this.fetchUserStats()
    },
    // 获取最近活动
    fetchRecentActivities() {
      try {
        // 从本地存储获取最近活动
        const activities = localStorage.getItem('recentActivities')
        if (activities) {
          this.recentActivities = JSON.parse(activities)
        } else {
          // 如果没有活动记录，显示空数组
          this.recentActivities = []
        }
      } catch (error) {
        console.error('获取最近活动失败:', error)
        this.$message.error('获取最近活动失败')
      }
    },
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      const date = new Date(dateTime)
      if (isNaN(date.getTime())) return '-'
      
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    // 获取书籍状态标签类型
    getBookStatusTagType(status) {
      switch (status) {
        case 1: return 'warning'  // 待认领
        case 2: return 'success'  // 已认领
        case 3: return 'info'     // 已归还
        default: return 'info'
      }
    },
    // 获取书籍状态文本
    getBookStatusText(status) {
      switch (status) {
        case 1: return '待认领'
        case 2: return '已认领'
        case 3: return '已归还'
        default: return '未知'
      }
    }
  }
}
</script>

<style scoped>
.home-container {
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
  color: #409EFF;
  margin-right: 20px;
}

.stat-text {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #545c64;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.recent-activities {
  margin-top: 40px;
}

h2 {
  color: #545c64;
  margin-bottom: 20px;
}

.records-section {
  margin-top: 40px;
}

.records-section h3 {
  color: #545c64;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.record-card {
  margin-bottom: 20px;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
