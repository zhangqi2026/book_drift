<template>
  <div class="badges-container">
    <h2>漂流勋章</h2>
    <p>通过捐赠和借阅书籍，解锁不同的成就勋章</p>
    
    <!-- 捐赠勋章 -->
    <div class="badge-section">
      <h3>捐赠勋章</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="badge in donationBadges" :key="badge.id">
          <el-card 
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
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="unlockBadge(badge)"
                  icon="el-icon-medal"
                >
                  解锁勋章
                </el-button>
                <el-progress 
                  :percentage="getDonationProgress(badge.required)" 
                  :format="() => `${donatedBooks}/${badge.required}`"
                  :status="getDonationProgress(badge.required) >= 100 ? 'success' : ''"
                ></el-progress>
              </div>
              
              <!-- 未达到条件，显示进度条 -->
              <div class="badge-progress" v-if="!badge.unlocked && !canUnlock(badge)">
                <el-progress 
                  :percentage="getDonationProgress(badge.required)" 
                  :format="() => `${donatedBooks}/${badge.required}`"
                ></el-progress>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 借阅勋章 -->
    <div class="badge-section">
      <h3>借阅勋章</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="badge in borrowingBadges" :key="badge.id">
          <el-card 
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
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="unlockBadge(badge)"
                  icon="el-icon-medal"
                >
                  解锁勋章
                </el-button>
                <el-progress 
                  :percentage="getBorrowingProgress(badge.required)" 
                  :format="() => `${borrowedBooks}/${badge.required}`"
                  :status="getBorrowingProgress(badge.required) >= 100 ? 'success' : ''"
                ></el-progress>
              </div>
              
              <!-- 未达到条件，显示进度条 -->
              <div class="badge-progress" v-if="!badge.unlocked && !canUnlock(badge)">
                <el-progress 
                  :percentage="getBorrowingProgress(badge.required)" 
                  :format="() => `${borrowedBooks}/${badge.required}`"
                ></el-progress>
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
          console.log('用户勋章数据:', medals)
          console.log('捐赠书籍数量:', this.donatedBooks)
          console.log('借阅书籍数量:', this.borrowedBooks)
          
          // 更新捐赠勋章状态
          this.donationBadges.forEach(badge => {
            // 检查是否已解锁（根据勋章类型 + 数量）
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
            console.log(`捐赠勋章【${badge.name}】:`, { 
              required: badge.required, 
              unlocked: badge.unlocked,
              currentCount: this.donatedBooks,
              canUnlock: this.donatedBooks >= badge.required && !badge.unlocked
            })
          })
          
          // 更新借阅勋章状态
          this.borrowingBadges.forEach(badge => {
            // 检查是否已解锁（根据勋章类型 + 数量）
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
            console.log(`借阅勋章【${badge.name}】:`, { 
              required: badge.required, 
              unlocked: badge.unlocked,
              currentCount: this.borrowedBooks,
              canUnlock: this.borrowedBooks >= badge.required && !badge.unlocked
            })
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
      // 已解锁的不能再次解锁
      if (badge.unlocked) {
        console.log(`勋章【${badge.name}】已解锁，不能再次解锁`)
        return false
      }
      
      // 根据勋章类型判断是否达到条件
      if (badge.id <= 4) { // 捐赠勋章
        const canUnlock = this.donatedBooks >= badge.required
        console.log(`捐赠勋章【${badge.name}】: 当前${this.donatedBooks}本，需要${badge.required}本，可以解锁=${canUnlock}`)
        return canUnlock
      } else { // 借阅勋章
        const canUnlock = this.borrowedBooks >= badge.required
        console.log(`借阅勋章【${badge.name}】: 当前${this.borrowedBooks}本，需要${badge.required}本，可以解锁=${canUnlock}`)
        return canUnlock
      }
    },
    // 解锁勋章
    async unlockBadge(badge) {
      const medalType = badge.id <= 4 ? 1 : 2 // 1-捐赠，2-借阅
      
      const unlockData = {
        userId: this.currentUser.id,
        medalName: badge.name,
        medalType: medalType,
        requiredCount: badge.required,
        description: badge.description
        // unlockTime 由后端自动生成
      }
      
      try {
        const response = await this.$axios.post(`/userMedal`, unlockData)
        
        if (response.code === 20000) {
          this.$message.success(`恭喜您解锁了【${badge.name}】勋章！`)
          // 重新获取用户统计数据，更新勋章状态
          await this.fetchUserStats()
        } else {
          this.$message.error(response.message || '解锁失败')
        }
      } catch (error) {
        console.error('解锁勋章错误:', error)
        this.$message.error('解锁失败：' + (error.response?.data?.message || error.message))
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
    },
    // 更新勋章状态（仅作为后备方案，正常情况下不执行）
    updateBadgeStatus() {
      console.log('使用后备方案更新勋章状态')
      // 更新捐赠勋章状态
      this.donationBadges.forEach(badge => {
        badge.unlocked = this.donatedBooks >= badge.required
        badge.unlockTime = null
      })
      
      // 更新借阅勋章状态
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
  padding: 20px;
}

h2 {
  color: #545c64;
  margin-bottom: 10px;
}

p {
  color: #909399;
  margin-bottom: 30px;
}

.badge-section {
  margin-bottom: 40px;
}

h3 {
  color: #545c64;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.badge-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

/* 未解锁的勋章卡片 - 只对图标和文字应用灰色，按钮除外 */
.badge-locked {
  opacity: 0.7;
}

.badge-locked .badge-icon {
  color: #c0c4cc;
}

.badge-locked .badge-name,
.badge-locked .badge-desc {
  color: #c0c4cc;
}

/* 已解锁的勋章卡片 - 正常显示 */
.badge-unlocked {
  filter: grayscale(0%);
  opacity: 1;
}

.badge-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

.badge-icon {
  text-align: center;
  font-size: 48px;
  margin-bottom: 15px;
}

.badge-locked .badge-icon {
  color: #c0c4cc;
}

.badge-unlocked .badge-icon {
  color: #409EFF;
}

.badge-info {
  text-align: center;
}

.badge-name {
  font-size: 16px;
  font-weight: bold;
  color: #545c64;
  margin-bottom: 5px;
}

.badge-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 15px;
}

.badge-unlocked-text {
  color: #67C23A;
  font-weight: bold;
  margin-top: 10px;
}

.unlock-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.badge-action {
  margin-top: 10px;
}

.badge-action .el-button {
  width: 100%;
  margin-bottom: 10px;
}

/* 确保解锁按钮显示为蓝色，不受卡片透明度影响 */
.badge-action .el-button.el-button--primary {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
  color: #fff !important;
  opacity: 1 !important;
}

.badge-progress {
  margin-top: 10px;
}
</style>
