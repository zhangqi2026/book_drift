<template>
  <div class="app-layout">
    <!-- 普通用户左侧导航栏 -->
    <div class="sidebar user-sidebar" v-if="isLoggedIn && !isAdmin">
      <div class="logo">
        <nuxt-link to="/user/home">校园闲置书籍漂流</nuxt-link>
      </div>

      <!-- 用户信息区域 -->
      <div class="user-info">
        <div class="user-avatar">
          <div class="avatar-placeholder">{{ currentUser.name ? currentUser.name.charAt(0) : '用' }}</div>
        </div>
        <div class="user-details">
          <span class="user-name">{{ currentUser.name || '未登录' }}</span>
          <span class="user-college">{{ currentUser.college || '' }}</span>
        </div>
      </div>

      <!-- 普通用户导航菜单 -->
      <ul class="nav-menu">
        <li class="nav-item">
          <nuxt-link to="/user/home" class="nav-link">
            <i class="el-icon-s-home"></i>
            <span>首页</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/user/user1" class="nav-link">
            <i class="el-icon-reading"></i>
            <span>借阅大厅</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/user/records" class="nav-link">
            <i class="el-icon-document"></i>
            <span>我的记录</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/user/badges" class="nav-link">
            <i class="el-icon-medal"></i>
            <span>漂流勋章</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/user/about" class="nav-link">
            <i class="el-icon-info"></i>
            <span>关于我们</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/user/announcements" class="nav-link">
            <i class="el-icon-bell"></i>
            <span>公告通知</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/user/feedback" class="nav-link">
            <i class="el-icon-message"></i>
            <span>反馈建议</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/user/ai-chat" class="nav-link">
            <i class="el-icon-service"></i>
            <span>AI 问答</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link" @click.prevent="handleLogout">
            <i class="el-icon-switch-button"></i>
            <span>退出登录</span>
          </a>
        </li>
      </ul>
    </div>

    <!-- 管理员左侧导航栏 -->
    <div class="sidebar admin-sidebar" v-if="isLoggedIn && isAdmin">
      <div class="logo">
        <nuxt-link to="/admin/admin1">校园闲置书籍漂流</nuxt-link>
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
        <li class="nav-item">
          <nuxt-link to="/admin/admin1" class="nav-link">
            <i class="el-icon-s-platform"></i>
            <span>首页</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/admin/books" class="nav-link">
            <i class="el-icon-reading"></i>
            <span>借阅大厅</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/admin/users" class="nav-link">
            <i class="el-icon-user"></i>
            <span>信息管理</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/admin/operations" class="nav-link">
            <i class="el-icon-s-operation"></i>
            <span>书籍操作</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <nuxt-link to="/admin/feedback" class="nav-link">
            <i class="el-icon-message"></i>
            <span>反馈管理</span>
          </nuxt-link>
        </li>
        <li class="nav-item">
          <a href="#" class="nav-link" @click.prevent="handleLogout">
            <i class="el-icon-switch-button"></i>
            <span>退出登录</span>
          </a>
        </li>
      </ul>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content" :class="{ 'full-width': !isLoggedIn }">
      <nuxt/>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      isLoggedIn: false,
      isAdmin: false,
      currentUser: {
        id: null,
        name: '',
        college: '',
        student_id: '',
        role: '' // 'admin' 或 'user'
      }
    }
  },
  mounted() {
    this.checkLoginStatus()
  },
  methods: {
    checkLoginStatus() {
      // 检查普通用户登录状态
      const isLoggedIn = localStorage.getItem('isLoggedIn')
      this.isLoggedIn = isLoggedIn === 'true'

      // 检查管理员登录状态
      const isAdminLoggedIn = localStorage.getItem('isAdminLoggedIn')
      this.isAdmin = isAdminLoggedIn === 'true'

      if (this.isLoggedIn) {
        // 从本地存储获取普通用户信息
        const user = localStorage.getItem('user')
        if (user) {
          this.currentUser = JSON.parse(user)
          this.currentUser.role = 'user'
        }

        // 如果当前在登录页面，重定向到首页
        if (this.$route.path === '/login' || this.$route.path === '/') {
          this.$router.push('/user/home')
        }
      } else if (this.isAdmin) {
        // 从本地存储获取管理员信息
        const admin = localStorage.getItem('admin')
        if (admin) {
          this.currentUser = JSON.parse(admin)
          this.currentUser.role = 'admin'
        }

        // 如果当前在登录页面，重定向到管理员主页
        if (this.$route.path === '/login' || this.$route.path === '/') {
          this.$router.push('/admin/admin1')
        }
      }
    },
    handleLogout() {
      // 清除所有本地存储
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('user')
      localStorage.removeItem('isAdminLoggedIn')
      localStorage.removeItem('admin')

      // 重置用户信息
      this.isLoggedIn = false
      this.isAdmin = false
      this.currentUser = {
        id: null,
        name: '',
        college: '',
        student_id: '',
        role: ''
      }

      // 跳转到登录页面
      this.$router.push('/login')
    }
  },
  watch: {
    '$route'() {
      this.checkLoginStatus()
    }
  }
}
</script>

<style>
html, body, #__nuxt, #__layout, .app-layout {
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.app-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

/* 侧边栏通用样式 */
.sidebar {
  width: 220px;
  color: white;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
  z-index: 1000;
  box-shadow: 3px 0 8px rgba(0,0,0,0.06);
}

/* 普通用户侧边栏样式 */
.user-sidebar {
  background-color: #545c64;
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

.logo a {
  color: white;
  font-size: 18px;
  font-weight: bold;
  text-decoration: none;
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

.nav-link:hover, .nav-link.nuxt-link-active {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  height: 100vh;
  overflow-y: auto;
  margin-left: 220px;
}

.main-content.full-width {
  flex: 1;
  margin-left: 0;
}
</style>
