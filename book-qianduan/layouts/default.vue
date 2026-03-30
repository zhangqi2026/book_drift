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
          <img v-if="currentUser.avatar" :src="getAvatarUrl(currentUser.avatar)" class="avatar-img">
          <div v-else class="avatar-placeholder">{{ currentUser.name ? currentUser.name.charAt(0) : '用' }}</div>
        </div>
        <div class="user-details">
          <span class="user-name">{{ currentUser.name || '未登录' }}</span>
          <span class="user-college">普通用户</span>
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
            <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
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
          <nuxt-link to="/user/profile" class="nav-link">
            <i class="el-icon-user"></i>
            <span>个人中心</span>
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
      unreadCount: 0,
      currentUser: {
        id: null,
        name: '',
        college: '',
        student_id: '',
        role: '', // 'admin' 或 'user'
        avatar: ''
      }
    }
  },
  mounted() {
    this.checkLoginStatus()
    this.$root.$on('avatar-updated', (avatarUrl) => {
      this.$set(this.currentUser, 'avatar', avatarUrl)
      const userStr = localStorage.getItem('user')
      if (userStr) {
        const user = JSON.parse(userStr)
        user.avatar = avatarUrl
        localStorage.setItem('user', JSON.stringify(user))
      }
      this.$nextTick(() => {
        this.$forceUpdate()
      })
    })
  },
  beforeDestroy() {
    this.$root.$off('avatar-updated')
  },
  methods: {
    getAvatarUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      return process.env.NODE_ENV === 'development' 
        ? 'http://localhost:10010' + url 
        : url
    },
    async fetchUnreadCount() {
      if (!this.isLoggedIn || this.isAdmin || !this.currentUser?.id) {
        return
      }
      try {
        const response = await this.$axios.get(`/announcementRead/unreadCount/${this.currentUser.id}`)
        if (response.code === 20000) {
          this.unreadCount = response.data || 0
        }
      } catch (error) {
        console.error('获取未读公告数量失败:', error)
      }
    },
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
          // 获取未读公告数量
          this.fetchUnreadCount()
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
    '$route'(to, from) {
      this.checkLoginStatus()
      // 当从公告页面返回时，刷新未读数量
      if (from.path === '/user/announcements') {
        this.fetchUnreadCount()
      }
      // 当从个人中心返回时，确保用户信息已更新
      if (from.path === '/user/profile') {
        const user = localStorage.getItem('user')
        if (user) {
          this.currentUser = JSON.parse(user)
        }
      }
    }
  }
}
</script>

<style>
/* ==========================================
   强制初始样式 - 确保菜单项初始为深绿色
   ========================================== */
.sidebar .nav-link:not(:hover):not(.nuxt-link-active) {
  color: #4a7c59 !important;
}

.sidebar .nav-link:not(:hover):not(.nuxt-link-active) i,
.sidebar .nav-link:not(:hover):not(.nuxt-link-active) span {
  color: #4a7c59 !important;
}

.user-sidebar .nav-link:not(:hover):not(.nuxt-link-active) {
  color: #4a7c59 !important;
}

.user-sidebar .nav-link:not(:hover):not(.nuxt-link-active) i,
.user-sidebar .nav-link:not(:hover):not(.nuxt-link-active) span {
  color: #4a7c59 !important;
}

.admin-sidebar .nav-link:not(:hover):not(.nuxt-link-active) {
  color: #4a7c59 !important;
}

.admin-sidebar .nav-link:not(:hover):not(.nuxt-link-active) i,
.admin-sidebar .nav-link:not(:hover):not(.nuxt-link-active) span {
  color: #4a7c59 !important;
}

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
  width: 240px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  height: 100vh;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 1000;
}

/* 普通用户侧边栏样式 */
.user-sidebar {
  background: linear-gradient(180deg, #fff9e6 0%, #f1f8e9 100%);
}

/* 管理员侧边栏样式 */
.admin-sidebar {
  background: linear-gradient(180deg, #f1f8e9 0%, #e8f5e9 100%);
}

/* ==========================================
   强制初始样式 - 确保所有菜单项即时生效
   ========================================== */

/* 强制侧边栏所有 span 标签颜色为 #4a7c59 */
.sidebar span,
.user-sidebar span,
.admin-sidebar span,
.sidebar .nav-link span,
.user-sidebar .nav-link span,
.admin-sidebar .nav-link span,
.sidebar .user-name,
.user-sidebar .user-name,
.admin-sidebar .user-name,
.sidebar .user-college,
.user-sidebar .user-college,
.admin-sidebar .user-college {
  color: #4a7c59 !important;
}

/* 普通用户端强制初始样式 */
.user-sidebar .nav-link {
  color: #4a7c59 !important;
}

.user-sidebar .nav-link i {
  color: #4a7c59 !important;
}

.user-sidebar .nav-link span {
  color: #4a7c59 !important;
}

/* 管理员端强制初始样式 */
.admin-sidebar .nav-link {
  color: #4a7c59 !important;
}

.admin-sidebar .nav-link i {
  color: #4a7c59 !important;
}

.admin-sidebar .nav-link span {
  color: #4a7c59 !important;
}

/* 管理员端菜单项hover时的颜色统一为 #4a7c59 */
.admin-sidebar .nav-link:hover:not(.nuxt-link-active) i,
.admin-sidebar .nav-link:hover:not(.nuxt-link-active) span {
  color: #4a7c59 !important;
}

/* 管理员端菜单项选中时的颜色统一为 #4a7c59 */
.admin-sidebar .nav-link.nuxt-link-active i,
.admin-sidebar .nav-link.nuxt-link-active span {
  color: #4a7c59 !important;
}

/* 管理员端所有状态下颜色都保持 #4a7c59 */
.admin-sidebar .nav-link:hover i,
.admin-sidebar .nav-link:hover span,
.admin-sidebar .nav-link.nuxt-link-active i,
.admin-sidebar .nav-link.nuxt-link-active span,
.admin-sidebar .nav-link.nuxt-link-active:hover i,
.admin-sidebar .nav-link.nuxt-link-active:hover span {
  color: #4a7c59 !important;
}

.logo {
  padding: 24px 20px;
  text-align: center;
  border-bottom: 1px solid #e8f5e9;
}

.logo a {
  color: #4a7c59;
  font-size: 17px;
  font-weight: 700;
  text-decoration: none;
  letter-spacing: 0.3px;
}

/* 管理员端顶部标题改为深灰色 */
.admin-sidebar .logo a {
  color: #5a5a5a;
}

/* 用户信息样式 */
.user-info {
  padding: 24px 20px;
  text-align: center;
  border-bottom: 1px solid #e8f5e9;
}

.user-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 14px;
  border: 3px solid #5cdb95;
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(92, 219, 149, 0.15);
}

/* 管理员头像样式 */
.admin-avatar {
  background: linear-gradient(135deg, #f97316 0%, #fb923c 100%);
  border-color: #f97316;
}

.avatar-placeholder {
  color: white !important;
  font-size: 30px;
  font-weight: 700;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 16px;
  font-weight: 700;
  color: #2c5f2d !important;
  margin-bottom: 4px;
}

.user-college {
  font-size: 13px;
  color: #888 !important;
}

/* 管理员端用户角色文字改为深绿色 */
.admin-sidebar .user-college {
  color: #4a7c59 !important;
}

/* 导航菜单样式 */
.nav-menu {
  list-style: none;
  margin: 0;
  padding: 16px 12px;
  flex: 1;
}

.nav-item {
  margin-bottom: 4px;
}

.nav-link {
  color: #4a7c59;
  text-decoration: none;
  padding: 16px 16px;
  display: flex;
  align-items: center;
  border-radius: 8px;
  transition: all 0.3s ease;
  line-height: 56px;
  position: relative;
}

.nav-link i {
  margin-right: 14px;
  font-size: 22px;
  width: 24px;
  text-align: center;
  transition: all 0.3s ease;
}

.nav-link span {
  font-size: 20px;
  font-weight: 500;
  flex: 1;
  transition: all 0.3s ease;
}

.unread-badge {
  position: absolute;
  top: 8px;
  right: 12px;
  background: linear-gradient(135deg, #98e6d0 0%, #7dd3c0 100%);
  color: #2c5f4e;
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 10px;
  min-width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(125, 211, 192, 0.4);
  animation: badgePulse 2s ease-in-out infinite;
  z-index: 10;
}

@keyframes badgePulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.08);
  }
}

/* 首页等重要菜单项字体稍大 */
.nav-item:first-child .nav-link span {
  font-size: 21px;
  font-weight: 600;
}

/* 管理员端首页菜单项也应用同样样式 */
.admin-sidebar .nav-item:first-child .nav-link span {
  font-size: 21px;
  font-weight: 600;
}

/* ==========================================
   普通用户端导航栏样式（有颜色变化）
   ========================================== */

/* 普通用户端未选中项 hover 动效 */
.user-sidebar .nav-link:hover:not(.nuxt-link-active) {
  background-color: #f0fdf4;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(92, 219, 149, 0.08);
}

.user-sidebar .nav-link:hover:not(.nuxt-link-active) i,
.user-sidebar .nav-link:hover:not(.nuxt-link-active) span {
  color: #5cdb95 !important;
}

/* 普通用户端选中态特效 */
.user-sidebar .nav-link.nuxt-link-active {
  background: linear-gradient(90deg, #e8f5e9 0%, #f1f8e9 100%);
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(92, 219, 149, 0.12);
}

.user-sidebar .nav-link.nuxt-link-active i,
.user-sidebar .nav-link.nuxt-link-active span {
  color: #38b000 !important;
}

/* 普通用户端退出登录按钮特殊样式 */
.user-sidebar .nav-item:last-child .nav-link {
  margin-top: 8px;
  color: #f97316 !important;
}

.user-sidebar .nav-item:last-child .nav-link i,
.user-sidebar .nav-item:last-child .nav-link span {
  color: #f97316 !important;
}

.user-sidebar .nav-item:last-child .nav-link:hover:not(.nuxt-link-active) {
  background-color: #fff7ed;
  color: #ea580c !important;
}

.user-sidebar .nav-item:last-child .nav-link:hover:not(.nuxt-link-active) i,
.user-sidebar .nav-item:last-child .nav-link:hover:not(.nuxt-link-active) span {
  color: #ea580c !important;
}

/* ==========================================
   管理员端导航栏样式（颜色统一为 #4a7c59）
   ========================================== */

/* 管理员端所有状态下背景和交互效果保持，但颜色统一 */
.admin-sidebar .nav-link:hover:not(.nuxt-link-active) {
  background-color: #f0fdf4;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(92, 219, 149, 0.08);
}

.admin-sidebar .nav-link.nuxt-link-active {
  background: linear-gradient(90deg, #e8f5e9 0%, #f1f8e9 100%);
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(92, 219, 149, 0.12);
}

/* 通用点击反馈（两者共用） */
.nav-link:active {
  transform: scale(0.97);
}

/* 管理员端退出登录按钮颜色统一为 #4a7c59，使用 !important 确保稳定性 */
.admin-sidebar .nav-item:last-child .nav-link {
  color: #4a7c59 !important;
}

.admin-sidebar .nav-item:last-child .nav-link i,
.admin-sidebar .nav-item:last-child .nav-link span {
  color: #4a7c59 !important;
}

.admin-sidebar .nav-item:last-child .nav-link:hover:not(.nuxt-link-active) {
  color: #4a7c59 !important;
}

.admin-sidebar .nav-item:last-child .nav-link:hover:not(.nuxt-link-active) i,
.admin-sidebar .nav-item:last-child .nav-link:hover:not(.nuxt-link-active) span {
  color: #4a7c59 !important;
}

/* 普通用户端模块分割线 */
.user-sidebar .nav-item:nth-child(6) {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 3px solid #a5d6a7;
}

/* 管理员端模块分割线 */
.admin-sidebar .nav-item:nth-child(5) {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 3px solid #a5d6a7;
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  height: 100vh;
  overflow-y: auto;
  position: relative;
}

.main-content.full-width {
  flex: 1;
  margin-left: 0;
}

/* 侧边栏滚动条美化 */
.sidebar::-webkit-scrollbar {
  width: 4px;
}

.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(92, 219, 149, 0.2);
  border-radius: 2px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: rgba(92, 219, 149, 0.3);
}
</style>
