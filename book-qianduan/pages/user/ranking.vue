<template>
  <div class="ranking-container">
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
    
    <div class="ranking-content">
      <!-- 标题区域 -->
      <div class="welcome-section">
        <div class="welcome-box slide-in">
          <h1 class="welcome-title">
            <span class="title-glow">用户活跃度排行榜</span>
          </h1>
          <p class="welcome-subtitle">查看校园书籍漂流用户排名</p>
        </div>
      </div>
      
      <!-- 排行榜类型选择 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">排行榜</h3>
        </div>
        
        <div class="rank-selectors">
          <div class="rank-type-selector">
            <el-radio-group v-model="rankType" @change="handleRankTypeChange">
              <el-radio-button label="total">总榜</el-radio-button>
              <el-radio-button label="monthly">月榜</el-radio-button>
              <el-radio-button label="weekly">周榜</el-radio-button>
              <el-radio-button label="daily">日榜</el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="rank-size-selector">
            <span>展示：</span>
            <el-radio-group v-model="rankSize" @change="handleRankSizeChange">
              <el-radio-button label="10">TOP 10</el-radio-button>
              <el-radio-button label="50">TOP 50</el-radio-button>
              <el-radio-button label="100">全部</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>
      
      <!-- 我的排名 -->
      <div class="section-box" v-if="currentUser">
        <div class="section-header">
          <h3 class="section-title">我的排名</h3>
        </div>
        <div class="my-rank-card">
          <div class="my-rank-number">
            <span class="rank-value">{{ myRank ? (myRank.rank > 100 ? '100+' : myRank.rank) : '未上榜' }}</span>
          </div>
          <div class="my-rank-info">
            <div class="my-activity-score">
              <span class="score-label">我的活跃度：</span>
              <span class="score-value">{{ myRank ? getActivityScore(myRank) : 0 }} 分</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 排行榜列表 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">排名列表</h3>
        </div>
        <div class="ranking-list" v-loading="loading">
          <div 
            v-for="(user, index) in rankingList" 
            :key="user.id" 
            class="rank-card"
            :class="'rank-' + user.rank"
          >
            <div class="rank-badge">
              <i v-if="user.rank === 1" class="el-icon-medal"></i>
              <i v-else-if="user.rank === 2" class="el-icon-medal"></i>
              <i v-else-if="user.rank === 3" class="el-icon-medal"></i>
              <span v-else>{{ user.rank }}</span>
            </div>
            <div class="user-avatar">
              <div class="avatar-placeholder">{{ user.name.charAt(0) }}</div>
            </div>
            <div class="user-details">
              <div class="user-name">{{ user.name }}</div>
              <div class="user-college">{{ user.college || '未知学院' }}</div>
            </div>
            <div class="activity-score">
              <span class="score-number">{{ getActivityScore(user) }}</span>
              <span class="score-label">分</span>
            </div>
          </div>
          
          <div v-if="rankingList.length === 0 && !loading" class="empty-state">
            <p>暂无排行榜数据</p>
          </div>
        </div>
        
        <!-- 分页 -->
        <el-pagination
          v-if="total > 0"
          @current-change="handlePageChange"
          :current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          class="custom-pagination"
        />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentUser: null,
      rankType: 'total',
      rankSize: '10',
      rankingList: [],
      myRank: null,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchRankingList()
    this.fetchMyRank()
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
    async fetchRankingList() {
      try {
        this.loading = true
        const res = await this.$axios.post(`/userActivity/rank/${this.pageSize}/${this.currentPage}`, null, {
          params: { rankType: this.rankType }
        })
        if (res.code === 20000 && res.data) {
          this.rankingList = res.data.records || []
          this.total = res.data.total || 0
        }
      } catch (error) {
        console.error('获取排行榜失败:', error)
        this.$message.error('获取排行榜失败')
      } finally {
        this.loading = false
      }
    },
    async fetchMyRank() {
      try {
        const res = await this.$axios.get(`/userActivity/userRank/${this.currentUser.id}`, {
          params: { rankType: this.rankType }
        })
        if (res.code === 20000 && res.data) {
          this.myRank = res.data
        }
      } catch (error) {
        console.error('获取我的排名失败:', error)
      }
    },
    handleRankTypeChange() {
      this.currentPage = 1
      this.fetchRankingList()
      this.fetchMyRank()
    },
    handleRankSizeChange() {
      this.pageSize = parseInt(this.rankSize)
      this.currentPage = 1
      this.fetchRankingList()
    },
    handlePageChange(val) {
      this.currentPage = val
      this.fetchRankingList()
    },
    getActivityScore(user) {
      switch (this.rankType) {
        case 'daily':
          return user.dailyActivityScore || 0
        case 'weekly':
          return user.weeklyActivityScore || 0
        case 'monthly':
          return user.monthlyActivityScore || 0
        case 'total':
        default:
          return user.activityScore || 0
      }
    }
  }
}
</script>

<style scoped>
.ranking-container {
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

.ranking-content {
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

/* 选择器区域 */
.rank-selectors {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rank-type-selector,
.rank-size-selector {
  display: flex;
  align-items: center;
}

.rank-size-selector span {
  margin-right: 10px;
  color: #5a6a5a;
  font-weight: 500;
}

/* 我的排名卡片 */
.my-rank-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border-radius: 16px;
  border: 2px solid rgba(171, 240, 209, 0.3);
  padding: 24px;
}

.my-rank-number {
  flex: 0 0 120px;
  text-align: center;
}

.rank-value {
  font-size: 48px;
  font-weight: 800;
  background: linear-gradient(135deg, #6b9a8a 0%, #7a9d5a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.my-rank-info {
  flex: 1;
}

.my-activity-score {
  font-size: 16px;
  color: #5a6a5a;
}

.score-label {
  margin-right: 10px;
  font-weight: 500;
}

.score-value {
  font-size: 28px;
  font-weight: 800;
  color: #6b9a8a;
}

/* 排行榜列表 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  border-radius: 16px;
  border: 1px solid rgba(171, 240, 209, 0.25);
  padding: 16px 20px;
  transition: all 0.35s ease;
}

.rank-card:hover {
  transform: translateY(-3px) scale(1.01);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
}

/* 前三名特殊样式 */
.rank-card.rank-1 {
  border: 2px solid #FFD700;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.15), rgba(212, 238, 167, 0.12));
}

.rank-card.rank-2 {
  border: 2px solid #C0C0C0;
  background: linear-gradient(135deg, rgba(192, 192, 192, 0.15), rgba(212, 238, 167, 0.12));
}

.rank-card.rank-3 {
  border: 2px solid #CD7F32;
  background: linear-gradient(135deg, rgba(205, 127, 50, 0.15), rgba(212, 238, 167, 0.12));
}

.rank-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  font-weight: 800;
  font-size: 18px;
  margin-right: 16px;
  flex-shrink: 0;
}

/* 第1名：金色渐变 */
.rank-card.rank-1 .rank-badge {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.4);
}

/* 第2名：银色渐变 */
.rank-card.rank-2 .rank-badge {
  background: linear-gradient(135deg, #C0C0C0, #A9A9A9);
  color: white;
  box-shadow: 0 0 15px rgba(192, 192, 192, 0.4);
}

/* 第3名：铜色渐变 */
.rank-card.rank-3 .rank-badge {
  background: linear-gradient(135deg, #CD7F32, #8B4513);
  color: white;
  box-shadow: 0 0 15px rgba(205, 127, 50, 0.4);
}

/* 第4名及以后：浅灰色圆形 + 深灰色数字 */
.rank-card:not(.rank-1):not(.rank-2):not(.rank-3) .rank-badge {
  background: #F0F0F0;
  color: #666666;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #abf0d1, #d4eea7);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.avatar-placeholder {
  color: white;
  font-size: 20px;
  font-weight: 700;
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 700;
  color: #5a6a5a;
  font-size: 16px;
  margin-bottom: 4px;
}

.user-college {
  font-size: 13px;
  color: #8a9a8a;
}

.activity-score {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.score-number {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-right: 6px;
}

.score-label {
  font-size: 14px;
  color: #8a9a8a;
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

/* Radio Button 样式优化 */
.rank-type-selector >>> .el-radio-button__inner,
.rank-size-selector >>> .el-radio-button__inner {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.rank-type-selector >>> .el-radio-button__orig-radio:checked + .el-radio-button__inner,
.rank-size-selector >>> .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border-color: #abf0d1;
  color: #4a6a5a;
  box-shadow: 5px 2px 10px rgba(0, 0, 0, 0.06);
}
</style>
