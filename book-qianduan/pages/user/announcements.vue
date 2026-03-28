<template>
  <div class="announcements-container">
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
    
    <div class="announcements-content">
      <!-- 标题区域 -->
      <div class="welcome-section">
        <div class="welcome-box slide-in">
          <h1 class="welcome-title">
            <span class="title-glow">公告通知</span>
          </h1>
          <p class="welcome-subtitle">最新公告信息</p>
        </div>
      </div>
      
      <!-- 公告列表 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">公告列表</h3>
        </div>
        <div class="announcement-list" v-loading="loading">
          <div 
            v-for="announcement in announcementList" 
            :key="announcement.id" 
            class="announcement-card"
            :class="{ 'unread': !isRead(announcement.id) }"
            @click="handleView(announcement)"
          >
            <div class="card-header">
              <h3 class="announcement-title">
                <span v-if="!isRead(announcement.id)" class="unread-dot"></span>
                {{ announcement.title }}
              </h3>
              <el-tag :type="isRead(announcement.id) ? 'info' : 'danger'" size="small" class="custom-tag">
                {{ isRead(announcement.id) ? '已查看' : '未读' }}
              </el-tag>
            </div>
            <div class="card-meta">
              <span><i class="el-icon-user"></i> {{ announcement.publisherName || '管理员' }}</span>
              <span><i class="el-icon-time"></i> {{ formatDateTime(announcement.publishTime) }}</span>
            </div>
            <div class="card-content">
              {{ announcement.content.length > 150 ? announcement.content.substring(0, 150) + '...' : announcement.content }}
            </div>
          </div>
          
          <div v-if="announcementList.length === 0 && !loading" class="empty-state">
            <p>暂无公告</p>
          </div>
        </div>
        
        <!-- 分页 -->
        <el-pagination
          v-if="total > 0"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          class="custom-pagination"
        />
      </div>
    </div>
    
    <!-- 查看公告详情弹窗 -->
    <el-dialog 
      title="公告详情" 
      :visible.sync="viewDialogVisible" 
      width="700px"
      class="custom-dialog"
    >
      <div class="announcement-detail" v-if="viewAnnouncement">
        <h3>{{ viewAnnouncement.title }}</h3>
        <div class="detail-meta">
          <span><i class="el-icon-user"></i> {{ viewAnnouncement.publisherName || '管理员' }}</span>
          <span><i class="el-icon-time"></i> {{ formatDateTime(viewAnnouncement.publishTime) }}</span>
        </div>
        <el-divider class="custom-divider"></el-divider>
        <div class="detail-content">
          {{ viewAnnouncement.content }}
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <div class="custom-btn custom-btn-default" @click="viewDialogVisible = false">关闭</div>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'UserAnnouncements',
  data() {
    return {
      announcementList: [],
      currentPage: 1,
      pageSize: 5,
      total: 0,
      loading: false,
      viewDialogVisible: false,
      viewAnnouncement: null,
      currentUser: null,
      readAnnouncementIds: []
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchAnnouncements()
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
        this.fetchReadAnnouncementIds()
      }
    },
    async fetchReadAnnouncementIds() {
      if (!this.currentUser?.id) return
      try {
        const response = await this.$axios.get(`/announcementRead/readIds/${this.currentUser.id}`)
        if (response.code === 20000) {
          this.readAnnouncementIds = response.data || []
        }
      } catch (error) {
        console.error('获取已读公告失败:', error)
      }
    },
    isRead(announcementId) {
      return this.readAnnouncementIds.includes(announcementId)
    },
    async fetchAnnouncements() {
      try {
        this.loading = true
        const response = await this.$axios.post(`/announcement/condition/${this.pageSize}/${this.currentPage}`, null, {
          params: { isPublished: 1 }
        })
        
        if (response.code === 20000) {
          this.announcementList = response.data.records || []
          this.total = response.data.total || 0
        }
      } catch (error) {
        this.$message.error('获取公告列表失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    async handleView(announcement) {
      this.viewAnnouncement = announcement
      this.viewDialogVisible = true
      
      if (!this.isRead(announcement.id) && this.currentUser?.id) {
        try {
          await this.$axios.post('/announcementRead/markRead', null, {
            params: {
              announcementId: announcement.id,
              userId: this.currentUser.id
            }
          })
          this.readAnnouncementIds.push(announcement.id)
        } catch (error) {
          console.error('标记已读失败:', error)
        }
      }
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchAnnouncements()
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
.announcements-container {
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

.announcements-content {
  position: relative;
  z-index: 10;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
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

/* 公告列表 */
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-card {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  border-radius: 16px;
  border: 1px solid rgba(171, 240, 209, 0.25);
  padding: 20px;
  cursor: pointer;
  transition: all 0.35s ease;
}

.announcement-card.unread {
  border-left: 4px solid #67c23a;
}

.announcement-card:hover {
  transform: translateY(-5px) scale(1.01);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.announcement-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #5a6a5a;
  flex: 1;
  display: flex;
  align-items: center;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background-color: #67c23a;
  border-radius: 50%;
  margin-right: 10px;
  flex-shrink: 0;
}

.card-meta {
  font-size: 13px;
  color: #8a9a8a;
  margin-bottom: 12px;
}

.card-meta span {
  margin-right: 20px;
}

.card-meta i {
  margin-right: 5px;
}

.card-content {
  color: #6a7a6a;
  line-height: 1.6;
  font-size: 14px;
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

/* 对话框样式 */
.custom-dialog >>> .el-dialog {
  border-radius: 20px;
}

.custom-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.1), rgba(212, 238, 167, 0.1));
  border-radius: 20px 20px 0 0;
  padding: 20px 24px;
}

.custom-dialog >>> .el-dialog__title {
  color: #5a6a5a;
  font-weight: 700;
}

.announcement-detail {
  padding: 10px;
}

.announcement-detail h3 {
  margin: 0 0 12px 0;
  color: #5a6a5a;
  font-size: 18px;
  font-weight: 700;
}

.detail-meta {
  font-size: 13px;
  color: #8a9a8a;
  margin-bottom: 0;
}

.detail-meta span {
  margin-right: 20px;
}

.detail-meta i {
  margin-right: 5px;
}

.custom-divider {
  margin: 15px 0;
  border-top: 1px solid rgba(171, 240, 209, 0.3);
}

.detail-content {
  line-height: 1.8;
  color: #6a7a6a;
  white-space: pre-wrap;
  font-size: 14px;
}

/* 按钮样式 */
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
}

.custom-btn-default {
  background: rgba(171, 240, 209, 0.15);
  color: #5a6a5a;
  border: 1px solid rgba(171, 240, 209, 0.3);
  box-shadow: 4px 2px 8px rgba(0, 0, 0, 0.05);
}

.custom-btn-default:hover {
  background: rgba(171, 240, 209, 0.25);
  transform: translateY(-2px);
  box-shadow: 5px 3px 10px rgba(0, 0, 0, 0.08);
  color: #5a6a5a;
}
</style>
