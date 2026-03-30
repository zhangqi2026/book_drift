<template>
  <div class="records-container">
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
    
    <div class="records-content">
      <!-- 标题区域 -->
      <div class="welcome-section">
        <div class="welcome-box slide-in">
          <h1 class="welcome-title">
            <span class="title-glow">我的记录</span>
          </h1>
          <p class="welcome-subtitle">查看我的捐赠记录和借阅记录</p>
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
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentUser: {
        id: null,
        name: '',
        college: '',
        student_id: ''
      },
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
    this.fetchRecords()
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
    async fetchRecords() {
      try {
        const donateRecordResponse = await this.$axios.post(`/bookInfo/condition/${this.donationPageSize}/${this.donationCurrentPage}`, null, {
          params: { donorId: this.currentUser.id }
        })
        
        if (donateRecordResponse.code === 20000) {
          this.donationRecords = donateRecordResponse.data.records || []
          this.donationTotal = donateRecordResponse.data.total || 0
        }
    
        const borrowRecordResponse = await this.$axios.post(`/bookClaimRecord/condition/${this.borrowPageSize}/${this.borrowCurrentPage}`, null, {
          params: { userId: this.currentUser.id }
        })
        
        if (borrowRecordResponse.code === 20000) {
          this.borrowRecords = borrowRecordResponse.data.records || []
          this.borrowTotal = borrowRecordResponse.data.total || 0
        }
      } catch (error) {
        console.error('获取记录数据失败:', error)
        this.$message.error('获取记录数据失败')
      }
    },
    handleDonationPageChange(val) {
      this.donationCurrentPage = val
      this.fetchRecords()
    },
    handleBorrowPageChange(val) {
      this.borrowCurrentPage = val
      this.fetchRecords()
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
    }
  }
}
</script>

<style scoped>
.records-container {
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

.records-content {
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
  color: #9aaba;
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
</style>
