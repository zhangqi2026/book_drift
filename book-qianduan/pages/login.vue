  <template>
  <div class="login-container">
    <!-- 粒子背景 -->
    <div class="particles-bg">
      <div v-for="i in 50" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    
    <!-- 装饰性背景元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
      <div class="leaf leaf-1"></div>
      <div class="leaf leaf-2"></div>
      <div class="grid-lines"></div>
    </div>
    
    <!-- 开始界面 -->
    <div v-if="!showLogin" class="welcome-box" :class="{ 'slide-in': !showLogin }">
      <div class="welcome-header">
        <div class="logo-icon">
          <i class="el-icon-reading"></i>
          <div class="logo-glow"></div>
        </div>
        <h1 class="welcome-title">
          <span class="title-glow">校园闲置书籍漂流系统</span>
        </h1>
        <p class="welcome-subtitle">让闲置书籍流动起来，让知识传递下去</p>
      </div>
      
      <div class="features">
        <div class="feature-item">
          <div class="feature-icon-wrapper">
            <i class="el-icon-document feature-icon"></i>
            <div class="icon-glow"></div>
          </div>
          <h3>书籍捐赠</h3>
          <p>将闲置书籍捐赠给需要的同学</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon-wrapper">
            <i class="el-icon-reading feature-icon"></i>
            <div class="icon-glow"></div>
          </div>
          <h3>书籍认领</h3>
          <p>免费认领感兴趣的书籍</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon-wrapper">
            <i class="el-icon-medal feature-icon"></i>
            <div class="icon-glow"></div>
          </div>
          <h3>勋章奖励</h3>
          <p>获得捐赠和借阅勋章</p>
        </div>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="goToUserLogin" class="btn-main neon-btn">
          <i class="el-icon-user"></i> 用户登录
        </el-button>
        <el-button type="success" size="large" @click="goToRegister" class="btn-secondary neon-btn">
          <i class="el-icon-edit-outline"></i> 立即注册
        </el-button>
        <el-button size="large" @click="goToAdminLogin" class="btn-admin neon-btn">
          <i class="el-icon-s-tools"></i> 管理员入口
        </el-button>
      </div>
    </div>
    
    <!-- 登录界面 -->
    <div v-else class="login-box" :class="{ 'slide-in': showLogin }">
      <div class="login-header">
        <div class="back-btn" @click="showLogin = false">
          <i class="el-icon-arrow-left"></i>
        </div>
        <h2 class="login-title">
          <span class="title-glow">{{ isAdminLogin ? '管理员登录' : '用户登录' }}</span>
        </h2>
      </div>
      
      <el-form :model="currentLoginForm" :rules="currentLoginRules" ref="loginForm" class="login-form">
        <el-form-item prop="username">
          <div class="input-wrapper">
            <div class="input-border">
              <el-input v-model="currentLoginForm.username" :placeholder="isAdminLogin ? '请输入管理员账号' : '请输入用户名/学号'" prefix-icon="el-icon-user" class="custom-input"></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <div class="input-wrapper">
            <div class="input-border">
              <el-input v-model="currentLoginForm.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" @keyup.enter.native="handleLogin" class="custom-input"></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="currentLoginForm.remember" class="remember-checkbox">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-button neon-btn" @click="handleLogin" :loading="loading">
            <span v-if="!loading">登 录</span>
          </el-button>
        </el-form-item>
        <div class="login-footer" v-if="!isAdminLogin">
          <span>还没有账号？</span>
          <nuxt-link to="/register" class="register-link">立即注册</nuxt-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      showLogin: false,
      isAdminLogin: false,
      loading: false,
      userLoginForm: {
        username: '',
        password: '',
        remember: false
      },
      userLoginRules: {
        username: [
          { required: true, message: '请输入用户名/学号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
        ]
      },
      adminLoginForm: {
        username: '',
        password: '',
        remember: false
      },
      adminLoginRules: {
        username: [
          { required: true, message: '请输入管理员账号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    currentLoginForm() {
      return this.isAdminLogin ? this.adminLoginForm : this.userLoginForm
    },
    currentLoginRules() {
      return this.isAdminLogin ? this.adminLoginRules : this.userLoginRules
    }
  },
  methods: {
    getParticleStyle(index) {
      const size = Math.random() * 6 + 3
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
    goToRegister() {
      this.$router.push('/register')
    },
    goToUserLogin() {
      this.isAdminLogin = false
      this.showLogin = true
    },
    goToAdminLogin() {
      this.isAdminLogin = true
      this.showLogin = true
    },
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            const form = this.currentLoginForm
            console.log(this.isAdminLogin ? '开始管理员登录' : '开始用户登录', '账号:', form.username)
            
            const response = await this.$axios.post('/sysUser/condition/10/1', null, {
              params: { name: form.username }
            })
            
            console.log('后端返回结果:', response)
                
            if (response.code === 20000 && response.data.records.length > 0) {
              console.log('查询到的用户列表:', response.data.records)
              const user = response.data.records.find(u => u.studentId === form.username || u.name === form.username)
                  
              console.log('匹配出的用户:', user)
              
              if (user) {
                console.log('输入的密码:', form.password, '数据库密码:', user.password)
                
                if (this.isAdminLogin) {
                  if (user.role !== 1) {
                    this.$message.error('该账号不是管理员账号')
                    return
                  }
                  
                  if (user.password === form.password) {
                    localStorage.setItem('admin', JSON.stringify({
                      id: user.id,
                      username: user.studentId,
                      name: user.name,
                      role: 'admin'
                    }))
                    
                    localStorage.setItem('isAdminLoggedIn', 'true')
                    
                    this.$router.push('/admin/admin1')
                    this.$message.success('管理员登录成功')
                  } else {
                    this.$message.error('密码错误')
                  }
                } else {
                  if (user.role === 1) {
                    this.$message.error('管理员请使用"管理员入口"登录')
                    return
                  }
                  
                  if (user.password === form.password) {
                    localStorage.setItem('user', JSON.stringify({
                      id: user.id,
                      name: user.name,
                      college: user.college,
                      student_id: user.studentId,
                      role: user.role
                    }))
                        
                    localStorage.setItem('isLoggedIn', 'true')
                        
                    this.$router.push('/user/home')
                    this.$message.success('登录成功')
                  } else {
                    this.$message.error('用户名或密码错误（密码不匹配）')
                  }
                }
              } else {
                this.$message.error('用户名或密码错误（未找到用户）')
              }
            } else {
              console.log('未查询到用户，响应数据:', response.data)
              this.$message.error('用户名或密码错误（无此用户）')
            }
            this.loading = false
          } catch (error) {
            console.error('登录异常:', error)
            this.$message.error('登录失败：' + (error.response?.data?.message || error.message || '网络连接失败，请检查后端是否启动'))
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100vw;
  height: 100vh;
  min-height: 100vh;
  background: linear-gradient(135deg, #fef9f0 0%, #f5f0e6 50%, #e8f5e2 100%);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  overflow: hidden;
}

/* 粒子背景 */
.particles-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
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
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(720deg);
    opacity: 0;
  }
}

/* 装饰性背景元素 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.25), rgba(212, 238, 167, 0.25));
  border: 1px solid rgba(171, 240, 209, 0.4);
  box-shadow: 0 0 40px rgba(171, 240, 209, 0.3), inset 0 0 40px rgba(171, 240, 209, 0.15);
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 500px;
  height: 500px;
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.circle-2 {
  width: 400px;
  height: 400px;
  top: 40%;
  right: -100px;
  animation-delay: 2s;
}

.circle-3 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: 15%;
  animation-delay: 4s;
}

.circle-4 {
  width: 250px;
  height: 250px;
  top: 10%;
  left: 55%;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg) scale(1);
  }
  50% {
    transform: translateY(-40px) rotate(180deg) scale(1.05);
  }
}

/* 叶子形状装饰 */
.leaf {
  position: absolute;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  clip-path: polygon(50% 0%, 80% 30%, 100% 50%, 80% 70%, 50% 100%, 20% 70%, 0% 50%, 20% 30%);
  border: 1px solid rgba(171, 240, 209, 0.3);
  animation: leafRotate 15s linear infinite;
}

.leaf-1 {
  top: 25%;
  right: 20%;
  animation-delay: 0s;
}

.leaf-2 {
  bottom: 25%;
  left: 25%;
  animation-delay: -7s;
}

@keyframes leafRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 网格线背景 */
.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(171, 240, 209, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(171, 240, 209, 0.08) 1px, transparent 1px);
  background-size: 50px 50px;
}

/* 欢迎界面样式 */
.welcome-box {
  width: 980px;
  padding: 60px 50px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(30px);
  border-radius: 30px;
  border: 1px solid rgba(171, 240, 209, 0.5);
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.08),
    0 0 60px rgba(171, 240, 209, 0.15),
    inset 0 0 60px rgba(171, 240, 209, 0.08);
  text-align: center;
  position: relative;
  z-index: 10;
}

.welcome-box::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #abf0d1, #d4eea7, #fef1d1, #abf0d1);
  border-radius: 30px;
  z-index: -1;
  background-size: 400% 400%;
  animation: borderGlow 3s ease infinite;
  opacity: 0.7;
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

.slide-in {
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.92);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.welcome-header {
  margin-bottom: 55px;
}

.logo-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 30px;
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 10px 40px rgba(171, 240, 209, 0.5);
}

.logo-icon i {
  font-size: 55px;
  color: #5a7c6e;
  position: relative;
  z-index: 2;
}

.logo-glow {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: radial-gradient(circle, rgba(171, 240, 209, 0.5), transparent);
  border-radius: 40px;
  animation: logoPulse 2s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}

.title-glow {
  background: linear-gradient(135deg, #6b9a8a 0%, #7a9d5a 50%, #c4a77a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 40px rgba(171, 240, 209, 0.4);
  animation: titleGlow 3s ease-in-out infinite;
  background-size: 200% 200%;
}

@keyframes titleGlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.welcome-title {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 15px;
}

.welcome-subtitle {
  font-size: 18px;
  color: #7a8a7a;
  font-weight: 400;
}

.features {
  display: flex;
  justify-content: space-around;
  margin-bottom: 55px;
  gap: 30px;
}

.feature-item {
  flex: 1;
  padding: 35px 25px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12) 0%, rgba(212, 238, 167, 0.12) 100%);
  border-radius: 20px;
  border: 1px solid rgba(171, 240, 209, 0.3);
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.feature-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(171, 240, 209, 0.15), transparent);
  transition: left 0.5s;
}

.feature-item:hover::before {
  left: 100%;
}

.feature-item:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 20px 50px rgba(171, 240, 209, 0.35);
  border-color: rgba(171, 240, 209, 0.6);
}

.feature-icon-wrapper {
  width: 90px;
  height: 90px;
  margin: 0 auto 22px;
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 10px 30px rgba(171, 240, 209, 0.4);
}

.icon-glow {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  background: radial-gradient(circle, rgba(171, 240, 209, 0.4), transparent);
  border-radius: 30px;
  animation: iconGlow 2s ease-in-out infinite;
}

@keyframes iconGlow {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.15);
  }
}

.feature-icon {
  font-size: 42px;
  color: #5a7c6e;
  position: relative;
  z-index: 2;
}

.feature-item h3 {
  font-size: 21px;
  color: #5a6a5a;
  margin-bottom: 10px;
  font-weight: 600;
}

.feature-item p {
  font-size: 14px;
  color: #8a9a8a;
  line-height: 1.7;
}

.action-buttons {
  display: flex;
  gap: 25px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  height: 55px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  padding: 0 35px;
  transition: all 0.4s ease;
}

.neon-btn {
  position: relative;
  overflow: hidden;
}

.neon-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.neon-btn:hover::before {
  left: 100%;
}

.btn-main {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border: none;
  color: #4a6a5a;
  box-shadow: 
    0 8px 30px rgba(171, 240, 209, 0.5),
    0 0 20px rgba(171, 240, 209, 0.3);
}

.btn-main:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 
    0 15px 45px rgba(171, 240, 209, 0.6),
    0 0 40px rgba(171, 240, 209, 0.5);
}

.btn-secondary {
  background: linear-gradient(135deg, #d4eea7 0%, #abf0d1 100%);
  border: none;
  color: #5a6a5a;
  box-shadow: 
    0 8px 30px rgba(212, 238, 167, 0.5),
    0 0 20px rgba(212, 238, 167, 0.3);
}

.btn-secondary:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 
    0 15px 45px rgba(212, 238, 167, 0.6),
    0 0 40px rgba(212, 238, 167, 0.5);
}

.btn-admin {
  background: rgba(254, 241, 209, 0.9);
  color: #8a7a5a;
  border: 2px solid rgba(171, 240, 209, 0.6);
  backdrop-filter: blur(10px);
}

.btn-admin:hover {
  background: rgba(171, 240, 209, 0.3);
  border-color: #abf0d1;
  color: #5a7a6a;
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(171, 240, 209, 0.4);
}

/* 登录界面样式 */
.login-box {
  width: 480px;
  padding: 55px 50px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(30px);
  border-radius: 30px;
  border: 1px solid rgba(171, 240, 209, 0.5);
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.08),
    0 0 60px rgba(171, 240, 209, 0.15),
    inset 0 0 60px rgba(171, 240, 209, 0.08);
  position: relative;
  z-index: 10;
}

.login-box::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #abf0d1, #d4eea7, #fef1d1, #abf0d1);
  border-radius: 30px;
  z-index: -1;
  background-size: 400% 400%;
  animation: borderGlow 3s ease infinite;
  opacity: 0.7;
}

.login-header {
  position: relative;
  margin-bottom: 40px;
}

.back-btn {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 45px;
  height: 45px;
  background: rgba(171, 240, 209, 0.3);
  border-radius: 12px;
  border: 1px solid rgba(171, 240, 209, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.back-btn:hover {
  background: rgba(171, 240, 209, 0.5);
  border-color: #abf0d1;
  transform: translateY(-50%) translateX(-5px);
  box-shadow: 0 5px 20px rgba(171, 240, 209, 0.4);
}

.back-btn i {
  font-size: 22px;
  color: #5a7a6a;
}

.login-title {
  text-align: center;
  font-size: 30px;
  font-weight: 800;
  margin: 0;
}

.login-form {
  margin-top: 15px;
}

.input-wrapper {
  position: relative;
}

.input-border {
  position: relative;
  padding: 2px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.4), rgba(212, 238, 167, 0.4));
  transition: all 0.3s ease;
}

.input-wrapper:focus-within .input-border {
  background: linear-gradient(135deg, #abf0d1, #d4eea7);
  box-shadow: 0 0 30px rgba(171, 240, 209, 0.5);
}

.custom-input {
  width: 100%;
}

.custom-input >>> .el-input__inner {
  height: 52px;
  border-radius: 12px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  color: #5a6a5a;
  transition: all 0.3s ease;
  font-size: 15px;
  padding-left: 48px !important;
}

.custom-input >>> .el-input__inner::placeholder {
  color: #aaa;
}

.custom-input >>> .el-input__inner:focus {
  background: #ffffff;
}

.custom-input >>> .el-input__prefix {
  left: 14px;
}

.custom-input >>> .el-input__prefix .el-input__icon {
  font-size: 19px;
  color: #9ab;
  line-height: 52px;
  transition: color 0.3s ease;
}

.input-wrapper:focus-within .custom-input >>> .el-input__prefix .el-input__icon {
  color: #6a9a7a;
}

.remember-checkbox {
  color: #7a8a7a;
}

.remember-checkbox >>> .el-checkbox__label {
  color: #7a8a7a;
}

.login-button {
  width: 100%;
  height: 54px;
  font-size: 18px;
  font-weight: 700;
  border-radius: 14px;
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border: none;
  color: #4a6a5a;
  box-shadow: 
    0 8px 30px rgba(171, 240, 209, 0.5),
    0 0 20px rgba(171, 240, 209, 0.3);
  transition: all 0.4s ease;
  margin-top: 15px;
}

.login-button:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 
    0 15px 45px rgba(171, 240, 209, 0.6),
    0 0 40px rgba(171, 240, 209, 0.5);
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  font-size: 15px;
  color: #8a9a8a;
}

.register-link {
  color: #6a9a7a;
  font-weight: 600;
  text-decoration: none;
  margin-left: 6px;
  transition: all 0.3s ease;
  text-shadow: 0 0 10px rgba(171, 240, 209, 0.4);
}

.register-link:hover {
  color: #7a9d5a;
  text-decoration: underline;
  text-shadow: 0 0 20px rgba(171, 240, 209, 0.6);
}
</style>
