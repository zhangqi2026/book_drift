<template>
  <div class="admin-home-container">
    <!-- 管理员左侧导航栏 -->
    <div class="sidebar admin-sidebar">
      <div class="logo">
        <span>校园闲置书籍漂流</span>
      </div>

      <!-- 管理员信息区域 -->
      <div class="user-info">
        <div class="user-avatar admin-avatar">
          <div class="avatar-placeholder">{{ currentUser.name ? currentUser.name.charAt(0) : '管' }}</div>
        </div>
        <div class="user-details">
          <span class="user-name">{{ currentUser.name || '管理员' }}</span>
          <span class="user-college">系统管理员</span>
        </div>
      </div>

      <!-- 管理员导航菜单 -->
      <ul class="nav-menu">
        <li class="nav-item" :class="{ 'active': activeTab === 'home' }" @click="switchTab('home')">
          <div class="nav-link">
            <i class="el-icon-s-platform"></i>
            <span>首页</span>
          </div>
        </li>
        <li class="nav-item" :class="{ 'active': activeTab === 'books' }" @click="switchTab('books')">
          <div class="nav-link">
            <i class="el-icon-reading"></i>
            <span>书籍大厅</span>
          </div>
        </li>
        <li class="nav-item" :class="{ 'active': activeTab === 'users' }" @click="switchTab('users')">
          <div class="nav-link">
            <i class="el-icon-user"></i>
            <span>信息管理</span>
          </div>
        </li>
        <li class="nav-item" :class="{ 'active': activeTab === 'medals' }" @click="switchTab('medals')">
          <div class="nav-link">
            <i class="el-icon-medal"></i>
            <span>勋章管理</span>
          </div>
        </li>
        <li class="nav-item" :class="{ 'active': activeTab === 'feedback' }" @click="switchTab('feedback')">
          <div class="nav-link">
            <i class="el-icon-message"></i>
            <span>反馈管理</span>
          </div>
        </li>
        <li class="nav-item" :class="{ 'active': activeTab === 'tags' }" @click="switchTab('tags')">
          <div class="nav-link">
            <i class="el-icon-price-tag"></i>
            <span>标签管理</span>
          </div>
        </li>
        <li class="nav-item" :class="{ 'active': activeTab === 'announcements' }" @click="switchTab('announcements')">
          <div class="nav-link">
            <i class="el-icon-bell"></i>
            <span>公告管理</span>
          </div>
        </li>
        <li class="nav-item">
          <div class="nav-link" @click="handleLogout">
            <i class="el-icon-switch-button"></i>
            <span>退出登录</span>
          </div>
        </li>
      </ul>
    </div>

    <!-- 主内容区域 -->
    <div class="admin-content">
      <!-- 首页 -->
      <admin-home 
        v-if="activeTab === 'home'" 
        :current-user="currentUser"
      />
      
      <!-- 书籍大厅 -->
      <admin-books v-if="activeTab === 'books'" />
      
      <!-- 信息管理 -->
      <admin-users v-if="activeTab === 'users'" />
      
      <!-- 勋章管理 -->
      <admin-medals v-if="activeTab === 'medals'" />
      
      <!-- 反馈管理 -->
      <admin-feedback v-if="activeTab === 'feedback'" />
      
      <!-- 标签管理 -->
      <admin-tags v-if="activeTab === 'tags'" />
      
      <!-- 公告管理 -->
      <admin-announcements 
        v-if="activeTab === 'announcements'" 
        :current-user="currentUser"
      />
    </div>
  </div>
</template>

<script>
import AdminHome from '@/components/admin/AdminHome'
import AdminBooks from '@/components/admin/AdminBooks'
import AdminUsers from '@/components/admin/AdminUsers'
import AdminMedals from '@/components/admin/AdminMedals'
import AdminFeedback from '@/components/admin/AdminFeedback'
import AdminTags from '@/components/admin/AdminTags'
import AdminAnnouncements from '@/components/admin/AdminAnnouncements'

export default {
  components: {
    AdminHome,
    AdminBooks,
    AdminUsers,
    AdminMedals,
    AdminFeedback,
    AdminTags,
    AdminAnnouncements
  },
  data() {
    return {
      activeTab: 'home',
      currentUser: {
        name: ''
      }
    }
  },
  mounted() {
    this.getAdminInfo()
  },
  methods: {
    switchTab(tab) {
      this.activeTab = tab
    },
    getAdminInfo() {
      const admin = localStorage.getItem('admin')
      if (admin) {
        this.currentUser = JSON.parse(admin)
      } else {
        this.$router.push('/login')
      }
    },
    handleLogout() {
      localStorage.removeItem('admin')
      localStorage.removeItem('isAdminLoggedIn')
      this.$router.push('/login')
    }
  }
}
</script>

<style>
html, body, #__nuxt, #__layout, .admin-home-container {
  height: 100%;
  margin: 0;
  padding: 0;
}

.admin-home-container {
  display: flex;
  height: 100%;
}

/* 侧边栏通用样式 */
.sidebar {
  width: 220px;
  color: white;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  height: 100%;
  overflow-y: auto;
  z-index: 1000;
}

/* 管理员侧边栏样式 */
.admin-sidebar {
  background-color: #303133;
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo span {
  color: white;
  font-size: 18px;
  font-weight: bold;
}

/* 用户信息样式 */
.user-info {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 15px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  background-color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 管理员头像样式 */
.admin-avatar {
  background-color: #F56C6C;
}

.avatar-placeholder {
  color: white;
  font-size: 36px;
  font-weight: bold;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.user-college {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

/* 导航菜单样式 */
.nav-menu {
  list-style: none;
  margin: 0;
  padding: 20px 0;
}

.nav-item {
  margin-bottom: 5px;
  cursor: pointer;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  transition: background-color 0.3s;
}

.nav-link i {
  margin-right: 10px;
  font-size: 18px;
}

.nav-link span {
  flex: 1;
}

.nav-item:hover .nav-link,
.nav-item.active .nav-link {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 主内容区域样式 */
.admin-content {
  flex: 1;
  margin-left: 220px;
  padding: 20px;
  overflow-y: auto;
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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  color: #303133;
  margin: 0;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.book-list-table,
.user-list-table,
.pending-books-table {
  margin-bottom: 20px;
}

.pending-books-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
