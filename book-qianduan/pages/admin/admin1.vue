<template>
  <div class="admin-home-container">
    <!-- 管理员导航栏 -->
    <div class="admin-navbar">
      <div class="navbar-logo">
        <span>校园闲置书籍漂流管理系统</span>
      </div>
      <div class="navbar-menu">
        <div 
          class="menu-item" 
          :class="{ 'active': activeTab === 'home' }"
          @click="switchTab('home')"
        >
          <i class="el-icon-s-platform"></i>
          <span>首页</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 'active': activeTab === 'books' }"
          @click="switchTab('books')"
        >
          <i class="el-icon-reading"></i>
          <span>书籍大厅</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 'active': activeTab === 'users' }"
          @click="switchTab('users')"
        >
          <i class="el-icon-user"></i>
          <span>信息管理</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 'active': activeTab === 'medals' }"
          @click="switchTab('medals')"
        >
          <i class="el-icon-medal"></i>
          <span>勋章管理</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 'active': activeTab === 'feedback' }"
          @click="switchTab('feedback')"
        >
          <i class="el-icon-message"></i>
          <span>反馈管理</span>
        </div>
        <div 
          class="menu-item" 
          :class="{ 'active': activeTab === 'tags' }"
          @click="switchTab('tags')"
        >
          <i class="el-icon-price-tag"></i>
          <span>标签管理</span>
        </div>
      </div>
      <div class="navbar-user">
        <div class="user-info">
          <span class="user-name">{{ currentUser.name }}</span>
          <span class="user-role">管理员</span>
        </div>
        <el-button type="text" @click="handleLogout" icon="el-icon-switch-button">退出登录</el-button>
      </div>
    </div>
    
    <!-- 页面内容 -->
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

export default {
  components: {
    AdminHome,
    AdminBooks,
    AdminUsers,
    AdminMedals,
    AdminFeedback,
    AdminTags
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
    // 切换标签页
    switchTab(tab) {
      this.activeTab = tab
    },
    // 获取管理员信息
    getAdminInfo() {
      const admin = localStorage.getItem('admin')
      if (admin) {
        this.currentUser = JSON.parse(admin)
      } else {
        this.$router.push('/login')
      }
    },
    // 退出登录
    handleLogout() {
      localStorage.removeItem('admin')
      localStorage.removeItem('isAdminLoggedIn')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.admin-home-container {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 管理员导航栏样式 */
.admin-navbar {
  height: 60px;
  background-color: #303133;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.navbar-logo {
  font-size: 18px;
  font-weight: bold;
  color: white;
}

.navbar-menu {
  display: flex;
  gap: 20px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
}

.menu-item:hover {
  color: white;
}

.menu-item.active {
  color: white;
  border-bottom: 2px solid #F56C6C;
}

.menu-item i {
  margin-right: 8px;
  font-size: 18px;
}

.navbar-user {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-right: 20px;
}

.user-name {
  font-size: 16px;
  font-weight: bold;
  color: white;
}

.user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

/* 管理员内容样式 */
.admin-content {
  flex: 1;
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
