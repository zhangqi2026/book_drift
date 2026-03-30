<template>
  <div class="profile-container">
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
    
    <div class="profile-content">
      <!-- 标题区域 -->
      <div class="welcome-section">
        <div class="welcome-box slide-in">
          <h1 class="welcome-title">
            <span class="title-glow">个人中心</span>
          </h1>
          <p class="welcome-subtitle">管理您的个人信息</p>
        </div>
      </div>
      
      <!-- 用户信息卡片 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">基本信息</h3>
          <el-button type="primary" size="small" @click="showEditInfo = true" icon="el-icon-edit">
            编辑信息
          </el-button>
        </div>
        
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              name="file"
            >
              <img v-if="currentUser.avatar" :src="getAvatarUrl(currentUser.avatar)" class="avatar">
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div class="avatar-tip">点击头像更换</div>
          </div>
        </div>
        
        <div class="info-display">
          <div class="info-item">
            <span class="info-label">学号</span>
            <span class="info-value">{{ currentUser.studentId || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">姓名</span>
            <span class="info-value">{{ currentUser.name || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">学院</span>
            <span class="info-value">{{ currentUser.college || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">累计借书</span>
            <span class="info-value">{{ currentUser.borrowCount || 0 }} 次</span>
          </div>
          <div class="info-item">
            <span class="info-label">总活跃度</span>
            <span class="info-value">{{ currentUser.activityScore || 0 }} 分</span>
          </div>
        </div>
      </div>
      
      <!-- 安全设置 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">安全设置</h3>
        </div>
        
        <div class="security-actions">
          <el-button type="warning" @click="showChangePassword = true" icon="el-icon-lock">
            修改密码
          </el-button>
          <el-button type="danger" @click="handleDeleteAccount" icon="el-icon-delete">
            注销账号
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 编辑信息弹窗 -->
    <el-dialog 
      title="编辑个人信息" 
      :visible.sync="showEditInfo" 
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-input v-model="editForm.college" placeholder="请输入学院" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showEditInfo = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateInfo" :loading="updating">
          保存
        </el-button>
      </span>
    </el-dialog>
    
    <!-- 修改密码弹窗 -->
    <el-dialog 
      title="修改密码" 
      :visible.sync="showChangePassword" 
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
          确认修改
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      currentUser: {
        id: null,
        studentId: '',
        name: '',
        college: '',
        borrowCount: 0,
        activityScore: 0,
        avatar: ''
      },
      showEditInfo: false,
      showChangePassword: false,
      updating: false,
      changingPassword: false,
      editForm: {
        name: '',
        college: ''
      },
      editRules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        college: [{ required: true, message: '请输入学院', trigger: 'blur' }]
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.passwordForm.newPassword) {
                callback(new Error('两次输入的密码不一致'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  mounted() {
    this.getCurrentUser()
  },
  computed: {
    uploadUrl() {
      return process.env.NODE_ENV === 'development' 
        ? 'http://localhost:10010/file/upload' 
        : '/file/upload'
    },
    uploadHeaders() {
      return {}
    }
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
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB!')
        return false
      }
      return true
    },
    async handleAvatarSuccess(response, file) {
      if (response.code === 20000) {
        const avatarUrl = response.data.url
        const oldAvatarUrl = this.currentUser.avatar
        try {
          if (oldAvatarUrl) {
            try {
              await this.$axios.delete('/file/delete', {
                params: { fileUrl: oldAvatarUrl }
              })
            } catch (deleteError) {
              console.warn('删除旧头像失败，但继续更新头像:', deleteError)
            }
          }
          
          const updateResponse = await this.$axios.put('/sysUser', {
            id: this.currentUser.id,
            avatar: avatarUrl
          })
          
          if (updateResponse.code === 20000) {
            this.currentUser.avatar = avatarUrl
            localStorage.setItem('user', JSON.stringify(this.currentUser))
            this.$root.$emit('avatar-updated', avatarUrl)
            this.$message.success('头像更新成功!')
          } else {
            this.$message.error(updateResponse.message || '头像更新失败')
          }
        } catch (error) {
          console.error('头像更新失败:', error)
          this.$message.error('头像更新失败')
        }
      } else {
        this.$message.error(response.message || '上传失败')
      }
    },
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
        this.fetchUserDetail()
      } else {
        this.$router.push('/login')
      }
    },
    async fetchUserDetail() {
      try {
        const response = await this.$axios.get(`/sysUser/${this.currentUser.id}`)
        if (response.code === 20000 && response.data) {
          this.currentUser = { ...this.currentUser, ...response.data }
          localStorage.setItem('user', JSON.stringify(this.currentUser))
        }
      } catch (error) {
        console.error('获取用户详情失败:', error)
      }
    },
    handleUpdateInfo() {
      this.$refs.editFormRef.validate(async (valid) => {
        if (!valid) return
        
        this.updating = true
        try {
          const response = await this.$axios.put('/sysUser', {
            id: this.currentUser.id,
            name: this.editForm.name,
            college: this.editForm.college
          })
          
          if (response.code === 20000) {
            this.$message.success('信息更新成功！')
            this.currentUser.name = this.editForm.name
            this.currentUser.college = this.editForm.college
            localStorage.setItem('user', JSON.stringify(this.currentUser))
            this.showEditInfo = false
          } else {
            this.$message.error(response.message || '更新失败')
          }
        } catch (error) {
          this.$message.error('更新失败：' + (error.response?.data?.message || error.message))
        } finally {
          this.updating = false
        }
      })
    },
    handleChangePassword() {
      this.$refs.passwordFormRef.validate(async (valid) => {
        if (!valid) return
        
        this.changingPassword = true
        try {
          const response = await this.$axios.put('/sysUser', {
            id: this.currentUser.id,
            password: this.passwordForm.newPassword
          })
          
          if (response.code === 20000) {
            this.$message.success('密码修改成功！请重新登录')
            this.showChangePassword = false
            this.handleLogout()
          } else {
            this.$message.error(response.message || '修改失败')
          }
        } catch (error) {
          this.$message.error('修改失败：' + (error.response?.data?.message || error.message))
        } finally {
          this.changingPassword = false
        }
      })
    },
    handleDeleteAccount() {
      this.$confirm('确定要注销账号吗？此操作不可恢复！', '警告', {
        confirmButtonText: '确定注销',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }).then(async () => {
        try {
          if (this.currentUser.avatar) {
            await this.$axios.delete('/file/delete', {
              params: { fileUrl: this.currentUser.avatar }
            })
          }
          
          const response = await this.$axios.delete(`/sysUser/${this.currentUser.id}`)
          
          if (response.code === 20000) {
            this.$message.success('账号已注销')
            this.handleLogout()
          } else {
            this.$message.error(response.message || '注销失败')
          }
        } catch (error) {
          this.$message.error('注销失败：' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {})
    },
    handleLogout() {
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('user')
      this.$router.push('/login')
    }
  },
  watch: {
    showEditInfo(val) {
      if (val) {
        this.editForm.name = this.currentUser.name || ''
        this.editForm.college = this.currentUser.college || ''
      }
    },
    showChangePassword(val) {
      if (!val) {
        this.passwordForm = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
        this.$refs.passwordFormRef?.clearValidate()
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
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

.profile-content {
  position: relative;
  z-index: 10;
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
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

/* 头像区域 */
.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(171, 240, 209, 0.2);
}

.avatar-wrapper {
  text-align: center;
}

.avatar-uploader >>> .el-upload {
  border: 2px dashed rgba(171, 240, 209, 0.6);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.avatar-uploader >>> .el-upload:hover {
  border-color: #abf0d1;
  box-shadow: 0 0 20px rgba(171, 240, 209, 0.3);
}

.avatar-uploader-icon {
  font-size: 36px;
  color: #abf0d1;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #8a9a8a;
}

/* 信息展示 */
.info-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  border-radius: 12px;
  border: 1px solid rgba(171, 240, 209, 0.2);
}

.info-label {
  font-size: 15px;
  font-weight: 600;
  color: #5a6a5a;
}

.info-value {
  font-size: 15px;
  color: #6a7a6a;
  font-weight: 500;
}

/* 安全设置 */
.security-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
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
</style>
