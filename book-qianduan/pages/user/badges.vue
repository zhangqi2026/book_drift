<template>
  <div class="badges-container">
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
    
    <div class="badges-content">
      <!-- 标题区域 -->
      <div class="welcome-section">
        <div class="welcome-box slide-in">
          <h1 class="welcome-title">
            <span class="title-glow">漂流勋章</span>
          </h1>
          <p class="welcome-subtitle">通过捐赠和借阅书籍，解锁不同的成就勋章</p>
        </div>
      </div>
      
      <!-- 捐赠勋章 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">捐赠勋章</h3>
        </div>
        <el-row :gutter="24">
          <el-col :span="6" v-for="badge in donationBadges" :key="badge.id">
            <div 
              class="badge-card" 
              :class="{ 'badge-locked': !badge.unlocked, 'badge-unlocked': badge.unlocked }"
            >
              <div class="badge-icon">
                <i :class="badge.icon"></i>
              </div>
              <div class="badge-info">
                <div class="badge-name">{{ badge.name }}</div>
                <div class="badge-desc">{{ badge.description }}</div>
                
                <!-- 已解锁状态 -->
                <div class="badge-unlocked-text" v-if="badge.unlocked">
                  <i class="el-icon-check"></i> 已解锁
                  <div class="unlock-time" v-if="badge.unlockTime">
                    解锁时间：{{ formatDateTime(badge.unlockTime) }}
                  </div>
                </div>
                
                <!-- 未解锁但达到条件，显示解锁按钮 -->
                <div class="badge-action" v-if="!badge.unlocked && canUnlock(badge)">
                  <div class="custom-btn custom-btn-primary" @click="unlockBadge(badge)">
                    <i class="el-icon-medal"></i> 解锁勋章
                  </div>
                  <div class="custom-progress">
                    <div class="progress-bar" :style="{ width: getDonationProgress(badge.required) + '%' }"></div>
                    <div class="progress-text">{{ donatedBooks }}/{{ badge.required }}</div>
                  </div>
                </div>
                
                <!-- 未达到条件，显示进度条 -->
                <div class="badge-progress" v-if="!badge.unlocked && !canUnlock(badge)">
                  <div class="custom-progress">
                    <div class="progress-bar" :style="{ width: getDonationProgress(badge.required) + '%' }"></div>
                    <div class="progress-text">{{ donatedBooks }}/{{ badge.required }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <!-- 借阅勋章 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">借阅勋章</h3>
        </div>
        <el-row :gutter="24">
          <el-col :span="6" v-for="badge in borrowingBadges" :key="badge.id">
            <div 
              class="badge-card" 
              :class="{ 'badge-locked': !badge.unlocked, 'badge-unlocked': badge.unlocked }"
            >
              <div class="badge-icon">
                <i :class="badge.icon"></i>
              </div>
              <div class="badge-info">
                <div class="badge-name">{{ badge.name }}</div>
                <div class="badge-desc">{{ badge.description }}</div>
                
                <!-- 已解锁状态 -->
                <div class="badge-unlocked-text" v-if="badge.unlocked">
                  <i class="el-icon-check"></i> 已解锁
                  <div class="unlock-time" v-if="badge.unlockTime">
                    解锁时间：{{ formatDateTime(badge.unlockTime) }}
                  </div>
                </div>
                
                <!-- 未解锁但达到条件，显示解锁按钮 -->
                <div class="badge-action" v-if="!badge.unlocked && canUnlock(badge)">
                  <div class="custom-btn custom-btn-primary" @click="unlockBadge(badge)">
                    <i class="el-icon-medal"></i> 解锁勋章
                  </div>
                  <div class="custom-progress">
                    <div class="progress-bar" :style="{ width: getBorrowingProgress(badge.required) + '%' }"></div>
                    <div class="progress-text">{{ borrowedBooks }}/{{ badge.required }}</div>
                  </div>
                </div>
                
                <!-- 未达到条件，显示进度条 -->
                <div class="badge-progress" v-if="!badge.unlocked && !canUnlock(badge)">
                  <div class="custom-progress">
                    <div class="progress-bar" :style="{ width: getBorrowingProgress(badge.required) + '%' }"></div>
                    <div class="progress-text">{{ borrowedBooks }}/{{ badge.required }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
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
      donatedBooks: 0, // 已捐赠书籍数量（从后端统计）
      borrowedBooks: 0, // 已借阅书籍数量（从后端统计）
      donationBadges: [
        {
          id: 1,
          name: '初出茅庐',
          description: '捐赠第1本书',
          icon: 'el-icon-medal',
          required: 1,
          unlocked: false
        },
        {
          id: 2,
          name: '小有成就',
          description: '捐赠10本书',
          icon: 'el-icon-trophy',
          required: 10,
          unlocked: false
        },
        {
          id: 3,
          name: '书香门第',
          description: '捐赠20本书',
          icon: 'el-icon-star-off',
          required: 20,
          unlocked: false
        },
        {
          id: 4,
          name: '藏书家',
          description: '捐赠50本书',
          icon: 'el-icon-star-on',
          required: 50,
          unlocked: false
        }
      ],
      borrowingBadges: [
        {
          id: 5,
          name: '初学者',
          description: '借阅第1本书',
          icon: 'el-icon-reading',
          required: 1,
          unlocked: false
        },
        {
          id: 6,
          name: '书虫',
          description: '借阅10本书',
          icon: 'el-icon-notebook-2',
          required: 10,
          unlocked: false
        },
        {
          id: 7,
          name: '知识探索者',
          description: '借阅20本书',
          icon: 'el-icon-document',
          required: 20,
          unlocked: false
        },
        {
          id: 8,
          name: '博学者',
          description: '借阅50本书',
          icon: 'el-icon-s-claim',
          required: 50,
          unlocked: false
        }
      ]
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchUserStats()
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
      // 从本地存储获取用户信息
      const user = localStorage.getItem('user')
      if (user) {
        this.currentUser = JSON.parse(user)
      } else {
        // 如果没有用户信息，跳转到登录页面
        this.$router.push('/login')
      }
    },
    // 获取用户统计数据
    async fetchUserStats() {
      try {
        // 1. 获取实际的捐赠记录（按用户 ID 作为捐赠人查询书籍）
        const donateResponse = await this.$axios.post(`/bookInfo/condition/1000/1`, null, {
          params: { donorId: this.currentUser.id }
        })
        
        if (donateResponse.code === 20000) {
          this.donatedBooks = donateResponse.data.total || 0
        }
        
        // 2. 获取借阅记录（从 book_claim_record 表查询）
        const borrowResponse = await this.$axios.post(`/bookClaimRecord/condition/1000/1`, null, {
          params: { userId: this.currentUser.id }
        })
        
        if (borrowResponse.code === 20000) {
          // 统计所有借阅记录数量（包括已归还和未归还）
          this.borrowedBooks = borrowResponse.data.total || 0
        }
        
        // 3. 获取用户勋章列表（用于判断勋章是否已解锁）
        const medalResponse = await this.$axios.post(`/userMedal/condition/1000/1`, null, {
          params: { userId: this.currentUser.id }
        })
        
        if (medalResponse.code === 20000) {
          const medals = medalResponse.data.records || []
          
          // 更新捐赠勋章状态
          this.donationBadges.forEach(badge => {
            const unlockedMedal = medals.find(medal => 
              medal.medalType === 1 && medal.requiredCount === badge.required
            )
            
            if (unlockedMedal) {
              badge.unlocked = true
              badge.unlockTime = unlockedMedal.unlockTime
            } else {
              badge.unlocked = false
              badge.unlockTime = null
            }
          })
          
          // 更新借阅勋章状态
          this.borrowingBadges.forEach(badge => {
            const unlockedMedal = medals.find(medal => 
              medal.medalType === 2 && medal.requiredCount === badge.required
            )
            
            if (unlockedMedal) {
              badge.unlocked = true
              badge.unlockTime = unlockedMedal.unlockTime
            } else {
              badge.unlocked = false
              badge.unlockTime = null
            }
          })
        } else {
          // 如果没有勋章数据，只根据数量判断
          this.updateBadgeStatus()
        }
      } catch (error) {
        console.error('获取用户统计数据失败:', error)
        this.$message.error('获取用户统计数据失败')
      }
    },
    // 计算捐赠进度
    getDonationProgress(required) {
      return Math.min((this.donatedBooks / required) * 100, 100)
    },
    // 计算借阅进度
    getBorrowingProgress(required) {
      return Math.min((this.borrowedBooks / required) * 100, 100)
    },
    // 判断是否可以解锁（达到条件但未解锁）
    canUnlock(badge) {
      if (badge.unlocked) {
        return false
      }
      
      if (badge.id <= 4) {
        return this.donatedBooks >= badge.required
      } else {
        return this.borrowedBooks >= badge.required
      }
    },
    // 解锁勋章
    async unlockBadge(badge) {
      const medalType = badge.id <= 4 ? 1 : 2
      
      const unlockData = {
        userId: this.currentUser.id,
        medalName: badge.name,
        medalType: medalType,
        requiredCount: badge.required,
        description: badge.description
      }
      
      try {
        const response = await this.$axios.post(`/userMedal`, unlockData)
        
        if (response.code === 20000) {
          this.$message.success(`恭喜您解锁了【${badge.name}】勋章！`)
          await this.fetchUserStats()
        } else {
          this.$message.error(response.message || '解锁失败')
        }
      } catch (error) {
        console.error('解锁勋章错误:', error)
        this.$message.error('解锁失败')
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
      
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    // 更新勋章状态（仅作为后备方案）
    updateBadgeStatus() {
      this.donationBadges.forEach(badge => {
        badge.unlocked = this.donatedBooks >= badge.required
        badge.unlockTime = null
      })
      
      this.borrowingBadges.forEach(badge => {
        badge.unlocked = this.borrowedBooks >= badge.required
        badge.unlockTime = null
      })
    }
  }
}
</script>

<style scoped>
.badges-container {
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

.badges-content {
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

/* 勋章卡片样式 */
.badge-card {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  border-radius: 16px;
  border: 1px solid rgba(171, 240, 209, 0.25);
  padding: 24px 20px;
  margin-bottom: 24px;
  transition: all 0.35s ease;
  text-align: center;
}

.badge-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
}

/* 未解锁的勋章卡片 */
.badge-locked {
  opacity: 0.6;
  background: linear-gradient(135deg, rgba(200, 200, 200, 0.12), rgba(180, 180, 180, 0.12));
  border-color: rgba(200, 200, 200, 0.25);
}

.badge-locked .badge-icon {
  color: #c0c4cc;
}

.badge-locked .badge-name,
.badge-locked .badge-desc {
  color: #c0c4cc;
}

/* 已解锁的勋章卡片 */
.badge-unlocked {
  opacity: 1;
}

.badge-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.badge-unlocked .badge-icon {
  color: #67c23a;
  text-shadow: 0 0 20px rgba(103, 194, 58, 0.3);
}

.badge-info {
  text-align: center;
}

.badge-name {
  font-size: 16px;
  font-weight: 700;
  color: #5a6a5a;
  margin-bottom: 8px;
}

.badge-desc {
  font-size: 13px;
  color: #8a9a8a;
  margin-bottom: 16px;
}

.badge-unlocked-text {
  color: #67c23a;
  font-weight: 600;
  margin-top: 12px;
  font-size: 14px;
}

.unlock-time {
  font-size: 12px;
  color: #8a9a8a;
  margin-top: 6px;
}

.badge-action {
  margin-top: 12px;
}

.badge-progress {
  margin-top: 12px;
}

/* 自定义按钮 */
.custom-btn {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.35s ease;
  border: none;
  cursor: pointer;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  margin-bottom: 12px;
}

.custom-btn-primary {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  color: #4a6a5a;
  box-shadow: 5px 2px 10px rgba(0, 0, 0, 0.06);
}

.custom-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  color: #4a6a5a;
}

/* 自定义进度条 */
.custom-progress {
  position: relative;
  height: 10px;
  background: rgba(171, 240, 209, 0.2);
  border-radius: 5px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #abf0d1 0%, #d4eea7 100%);
  border-radius: 5px;
  transition: width 0.5s ease;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  color: #5a6a5a;
  font-weight: 600;
}
</style>
