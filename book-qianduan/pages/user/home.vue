<template>
  <div class="home-container">
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
    
    <div class="home-content">
      <!-- 顶部欢迎区域 -->
      <div class="welcome-section">
        <div class="welcome-box slide-in">
          <h1 class="welcome-title">
            <span class="title-glow">欢迎回来，{{ currentUser.name }}</span>
          </h1>
          <p class="welcome-subtitle">开启今天的书籍漂流之旅</p>
          <el-button type="primary" size="medium" @click="showScanner = true" icon="el-icon-camera" class="scan-btn">
            扫码借书
          </el-button>
        </div>
      </div>
      
      <!-- 数据卡片区域 -->
      <div class="stats-section">
        <el-row :gutter="24">
          <el-col :span="6">
            <div class="stat-card" @click="goToBorrowedBooks">
              <div class="stat-text" style="text-align: center; width: 100%;">
                <div class="stat-value">{{ borrowedBooks }}</div>
                <div class="stat-label">已借阅书籍</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-text" style="text-align: center; width: 100%;">
                <div class="stat-value">{{ unlockedBadges }}</div>
                <div class="stat-label">获得勋章</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-text" style="text-align: center; width: 100%;">
                <div class="stat-value">{{ donatedBooks }}</div>
                <div class="stat-label">已捐赠书籍</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card" @click="goToRanking">
              <div class="stat-text" style="text-align: center; width: 100%;">
                <div class="stat-value">{{ userRank ? (userRank.rank > 100 ? '100+' : userRank.rank) : '-' }}</div>
                <div class="stat-label">我的排名</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <!-- 活跃度展示 -->
      <div class="activity-section">
        <div class="section-box">
          <div class="section-header">
            <h3 class="section-title">我的活跃度</h3>
            <el-button type="text" class="link-btn" @click="goToRanking">查看排行榜</el-button>
          </div>
          <el-row :gutter="24">
            <el-col :span="6">
              <div class="activity-item">
                <div class="activity-value">{{ userRank ? userRank.activityScore : 0 }}</div>
                <div class="activity-label">总活跃度</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="activity-item">
                <div class="activity-value">{{ userRank ? userRank.dailyActivityScore : 0 }}</div>
                <div class="activity-label">今日活跃度</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="activity-item">
                <div class="activity-value">{{ userRank ? userRank.weeklyActivityScore : 0 }}</div>
                <div class="activity-label">本周活跃度</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="activity-item">
                <div class="activity-value">{{ userRank ? userRank.monthlyActivityScore : 0 }}</div>
                <div class="activity-label">本月活跃度</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <!-- 记录区域 -->
      <div class="records-section">
        <!-- 捐赠记录 -->
        <div class="section-box record-box">
          <div class="section-header">
            <h3 class="section-title">捐赠记录（共 {{ donationTotal }} 本）</h3>
          </div>
          <div class="table-wrapper">
            <el-table :data="donationRecords" class="custom-table" stripe>
              <el-table-column prop="bookName" label="书籍名称" />
              <el-table-column prop="author" label="作者" width="120" />
              <el-table-column label="捐赠时间" width="180">
                <template slot-scope="scope">
                  {{ formatDateTime(scope.row.donateTime) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template slot-scope="scope">
                  <el-tag size="small" :type="scope.row.bookStatus === 1 ? 'success' : 'primary'" class="custom-tag">
                    {{ scope.row.bookStatus === 1 ? '待认领' : '已漂流' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="donationRecords.length === 0" class="empty-state">
              <p>暂无捐赠记录</p>
            </div>
          </div>
          <el-pagination
            v-if="donationTotal > 0"
            @current-change="handleDonationPageChange"
            :current-page="donationCurrentPage"
            :page-size="donationPageSize"
            layout="total, prev, pager, next"
            :total="donationTotal"
            class="custom-pagination"
          />
        </div>
        
        <!-- 借阅记录 -->
        <div class="section-box record-box">
          <div class="section-header">
            <h3 class="section-title">借阅记录（共 {{ borrowTotal }} 本）</h3>
          </div>
          <div class="table-wrapper">
            <el-table :data="borrowRecords" class="custom-table" stripe>
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
                  <el-tag size="small" :type="getBookStatusTagType(scope.row.bookStatus)" class="custom-tag">
                    {{ getBookStatusText(scope.row.bookStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="borrowRecords.length === 0" class="empty-state">
              <p>暂无借阅记录</p>
            </div>
          </div>
          <el-pagination
            v-if="borrowTotal > 0"
            @current-change="handleBorrowPageChange"
            :current-page="borrowCurrentPage"
            :page-size="borrowPageSize"
            layout="total, prev, pager, next"
            :total="borrowTotal"
            class="custom-pagination"
          />
        </div>
      </div>
    </div>
    
    <!-- 扫码组件 -->
    <qr-scanner
      :visible="showScanner"
      :current-user-id="currentUser.id"
      mode="borrow"
      @close="showScanner = false"
      @success="handleScanSuccess"
    />
  </div>
</template>

<script>
import QrScanner from '@/components/QrScanner.vue'

export default {
  components: {
    QrScanner
  },
  data() {
    return {
      showScanner: false,
      currentUser: {
        name: ''
      },
      donatedBooks: 0,
      borrowedBooks: 0,
      unlockedBadges: 0,
      userRank: null,
      recentActivities: [],
      donationRecords: [],
      borrowRecords: [],
      donationCurrentPage: 1,
      donationPageSize: 5,
      donationTotal: 0,
      borrowCurrentPage: 1,
      borrowPageSize: 5,
      borrowTotal: 0
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchUserStats()
    this.fetchRecentActivities()
    this.fetchUserRank()
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
    goToBorrowedBooks() {
      this.$router.push('/user/borrowed-books')
    },
    goToRanking() {
      this.$router.push('/user/ranking')
    },
    async fetchUserRank() {
      try {
        const res = await this.$axios.get(`/userActivity/userRank/${this.currentUser.id}`, {
          params: { rankType: 'total' }
        })
        if (res.code === 20000 && res.data) {
          this.userRank = res.data
        }
      } catch (error) {
        console.error('获取用户排名失败:', error)
      }
    },
    async fetchUserStats() {
      try {
        let donatedBooks = 0
        let borrowedBooks = 0
        
        const borrowRecordResponse = await this.$axios.post(`/bookClaimRecord/condition/${this.borrowPageSize}/${this.borrowCurrentPage}`, null, {
          params: { userId: this.currentUser.id }
        })
        
        if (borrowRecordResponse.code === 20000) {
          const borrowedRecords = borrowRecordResponse.data.records.filter(record => !record.returnTime)
          borrowedBooks = borrowedRecords.length || 0
          this.borrowedBooks = borrowedBooks
          this.borrowRecords = borrowRecordResponse.data.records || []
          this.borrowTotal = borrowRecordResponse.data.total || 0
        }
            
        const donateRecordResponse = await this.$axios.post(`/bookInfo/condition/${this.donationPageSize}/${this.donationCurrentPage}`, null, {
          params: { donorId: this.currentUser.id }
        })
        
        if (donateRecordResponse.code === 20000) {
          donatedBooks = donateRecordResponse.data.total || 0
          this.donatedBooks = donatedBooks
          this.donationRecords = donateRecordResponse.data.records || []
          this.donationTotal = donateRecordResponse.data.total || 0
        }
        
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
    handleDonationPageChange(val) {
      this.donationCurrentPage = val
      this.fetchUserStats()
    },
    handleBorrowPageChange(val) {
      this.borrowCurrentPage = val
      this.fetchUserStats()
    },
    fetchRecentActivities() {
      try {
        const activities = localStorage.getItem('recentActivities')
        if (activities) {
          this.recentActivities = JSON.parse(activities)
        } else {
          this.recentActivities = []
        }
      } catch (error) {
        console.error('获取最近活动失败:', error)
        this.$message.error('获取最近活动失败')
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
    getBookStatusTagType(status) {
      switch (status) {
        case 1: return 'warning'
        case 2: return 'success'
        case 3: return 'info'
        default: return 'info'
      }
    },
    getBookStatusText(status) {
      switch (status) {
        case 1: return '待认领'
        case 2: return '已认领'
        case 3: return '已归还'
        default: return '未知'
      }
    },
    handleScanSuccess(action, bookInfo) {
      this.fetchUserStats()
    }
  }
}
</script>

<style scoped>
.home-container {
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

.home-content {
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

.scan-btn {
  border-radius: 12px;
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border: none;
  color: #4a6a5a;
  font-weight: 600;
  padding: 12px 24px;
  transition: all 0.3s ease;
}

.scan-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(171, 240, 209, 0.5);
}

/* 统计卡片区域 */
.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(171, 240, 209, 0.3);
  box-shadow: 5px 2px 10px rgba(0, 0, 0, 0.06);
  padding: 20px 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.35s ease;
  height: 110px;
}

.stat-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
}

.stat-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #5a6a5a;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #8a9a8a;
  font-weight: 500;
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

/* 活跃度区域 */
.activity-section {
  margin-bottom: 20px;
}

.activity-item {
  text-align: center;
  padding: 16px 0;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  border-radius: 14px;
  border: 1px solid rgba(171, 240, 209, 0.25);
}

.activity-value {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #6b9a8a 0%, #7a9d5a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
}

.activity-label {
  font-size: 13px;
  color: #8a9a8a;
  font-weight: 500;
}

/* 记录区域 */
.records-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.record-box {
  margin-bottom: 0;
}

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
  color: #9a9a9a;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

.custom-pagination {
  margin-top: 15px;
  display: flex;
  justify-content: center;
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

/* 确保弹窗层级高于遮罩层 */
.custom-dialog >>> .el-overlay {
  z-index: 9998 !important;
}

.custom-dialog >>> .el-dialog__wrapper {
  z-index: 9999 !important;
}

.custom-dialog >>> .el-dialog {
  z-index: 10000 !important;
  pointer-events: auto !important;
}
</style>

<style>
/* 全局样式，确保弹窗层级正确 */
body .el-overlay {
  z-index: 9998 !important;
}

body .el-dialog__wrapper {
  z-index: 9999 !important;
}

body .el-dialog {
  z-index: 10000 !important;
  pointer-events: auto !important;
}
</style>
