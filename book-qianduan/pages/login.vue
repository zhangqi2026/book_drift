  <template>
  <div class="login-container">
    <!-- 装饰性背景元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
    </div>
    
    <!-- 开始界面 -->
    <div v-if="!showLogin" class="welcome-box" :class="{ 'slide-in': !showLogin }">
      <div class="welcome-header">
        <div class="logo-icon">
          <i class="el-icon-reading"></i>
        </div>
        <h1 class="welcome-title">校园闲置书籍漂流系统</h1>
        <p class="welcome-subtitle">让闲置书籍流动起来，让知识传递下去</p>
      </div>
      
      <div class="features">
        <div class="feature-item">
          <div class="feature-icon-wrapper">
            <i class="el-icon-document feature-icon"></i>
          </div>
          <h3>书籍捐赠</h3>
          <p>将闲置书籍捐赠给需要的同学</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon-wrapper">
            <i class="el-icon-reading feature-icon"></i>
          </div>
          <h3>书籍认领</h3>
          <p>免费认领感兴趣的书籍</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon-wrapper">
            <i class="el-icon-medal feature-icon"></i>
          </div>
          <h3>勋章奖励</h3>
          <p>获得捐赠和借阅勋章</p>
        </div>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="showLogin = true" class="btn-main">
          <i class="el-icon-user"></i> 用户登录
        </el-button>
        <el-button type="success" size="large" @click="goToRegister" class="btn-secondary">
          <i class="el-icon-edit-outline"></i> 立即注册
        </el-button>
        <el-button size="large" @click="handleAdminLogin" class="btn-admin">
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
        <h2 class="login-title">用户登录</h2>
      </div>
      
      <el-form :model="userLoginForm" :rules="userLoginRules" ref="userLoginForm" class="login-form">
        <el-form-item prop="username">
          <div class="input-wrapper">
            <i class="el-icon-user input-icon"></i>
            <el-input v-model="userLoginForm.username" placeholder="请输入用户名/学号"></el-input>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <div class="input-wrapper">
            <i class="el-icon-lock input-icon"></i>
            <el-input v-model="userLoginForm.password" type="password" placeholder="请输入密码" @keyup.enter.native="handleUserLogin"></el-input>
          </div>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="userLoginForm.remember" class="remember-checkbox">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-button" @click="handleUserLogin" :loading="userLoading">
            <span v-if="!userLoading">登 录</span>
          </el-button>
        </el-form-item>
        <div class="login-footer">
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
      },
      userLoading: false,
      adminLoading: false
    }
  },
  methods: {
    goToRegister() {
      this.$router.push('/register')
    },
    handleUserLogin() {
      this.$refs.userLoginForm.validate(async valid => {
        if (valid) {
          this.userLoading = true
          try {
            console.log('开始登录，用户名/学号:', this.userLoginForm.username)
            
            // 调用后端 API 进行用户登录验证
            const response = await this.$axios.post('/sysUser/condition/10/1', null, {
              params: { name: this.userLoginForm.username }
            })
            
            console.log('后端返回结果:', response)
                
            if (response.code === 20000 && response.data.records.length > 0) {
              console.log('查询到的用户列表:', response.data.records)
              // 查找匹配的用户
              const user = response.data.records.find(u => u.studentId === this.userLoginForm.username || u.name === this.userLoginForm.username)
                  
              console.log('匹配出的用户:', user)
              
              if (user) {
                console.log('输入的密码:', this.userLoginForm.password, '数据库密码:', user.password)
                
                // 检查是否为管理员角色
                if (user.role === 1) {
                  this.$message.error('管理员请使用“管理员入口”登录')
                  return
                }
                
                if (user.password === this.userLoginForm.password) {
                  // 保存用户信息到本地存储
                  localStorage.setItem('user', JSON.stringify({
                    id: user.id,
                    name: user.name,
                    college: user.college,
                    student_id: user.studentId,
                    role: user.role
                  }))
                      
                  // 保存登录状态
                  localStorage.setItem('isLoggedIn', 'true')
                      
                  // 普通用户只能进入用户首页
                  this.$router.push('/user/home')
                      
                  this.$message.success('登录成功')
                } else {
                  this.$message.error('用户名或密码错误（密码不匹配）')
                }
              } else {
                this.$message.error('用户名或密码错误（未找到用户）')
              }
            } else {
              console.log('未查询到用户，响应数据:', response.data)
              this.$message.error('用户名或密码错误（无此用户）')
            }
            this.userLoading = false
          } catch (error) {
            console.error('登录异常:', error)
            this.$message.error('登录失败：' + (error.response?.data?.message || error.message || '网络连接失败，请检查后端是否启动'))
            this.userLoading = false
          }
        }
      })
    },
    handleAdminLogin() {
      console.log('点击管理员入口按钮')
      this.$prompt('请输入管理员学号', '管理员登录', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '学号不能为空'
      }).then(({ value }) => {
        console.log('输入的管理员学号:', value)
        this.$prompt('请输入密码', '管理员登录', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputType: 'password',
          inputPattern: /.+/,
          inputErrorMessage: '密码不能为空'
        }).then(async ({ value: password }) => {
          console.log('输入的管理员密码:', password)
          
          try {
            // 调用后端 API 进行管理员验证
            const response = await this.$axios.post('/sysUser/condition/10/1', null, {
              params: { name: value }
            })
            
            console.log('后端返回结果:', response)
            
            if (response.code === 20000 && response.data.records.length > 0) {
              // 查找匹配的用户（通过学号或姓名）
              const admin = response.data.records.find(u => u.studentId === value || u.name === value)
              
              console.log('匹配到的管理员:', admin)
              
              if (admin) {
                // 检查是否为管理员角色（role === 1）
                if (admin.role === 1) {
                  // 验证密码
                  if (admin.password === password) {
                    console.log('管理员登录验证成功')
                    
                    // 保存管理员信息到本地存储
                    localStorage.setItem('admin', JSON.stringify({
                      id: admin.id,
                      username: admin.studentId,
                      name: admin.name,
                      role: 'admin'
                    }))
                    
                    // 保存登录状态
                    localStorage.setItem('isAdminLoggedIn', 'true')
                    
                    console.log('准备跳转到管理员后台')
                    // 跳转到管理员主页
                    this.$router.push('/admin/admin1')
                    
                    this.$message.success('管理员登录成功')
                  } else {
                    console.log('密码错误')
                    this.$message.error('管理员密码错误')
                  }
                } else {
                  console.log('该用户不是管理员，role:', admin.role)
                  this.$message.error('该账号不是管理员账号')
                }
              } else {
                console.log('未找到该学号的管理员')
                this.$message.error('未找到该学号的管理员')
              }
            } else {
              console.log('未查询到该用户')
              this.$message.error('该学号不存在')
            }
          } catch (error) {
            console.error('管理员登录异常:', error)
            this.$message.error('登录失败：' + (error.response?.data?.message || error.message || '网络连接失败'))
          }
        }).catch((err) => {
          console.log('取消输入密码或发生错误:', err)
          // 取消输入密码
        })
      }).catch((err) => {
        console.log('取消输入账号或发生错误:', err)
        // 取消输入账号
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
  height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
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
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3));
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  top: 50%;
  right: -80px;
  animation-delay: 2s;
}

.circle-3 {
  width: 250px;
  height: 250px;
  bottom: -50px;
  left: 20%;
  animation-delay: 4s;
}

.circle-4 {
  width: 200px;
  height: 200px;
  top: 20%;
  left: 60%;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
  }
}

/* 欢迎界面样式 */
.welcome-box {
  width: 950px;
  padding: 60px 50px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  text-align: center;
  position: relative;
  z-index: 1;
}

.slide-in {
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.welcome-header {
  margin-bottom: 50px;
}

.logo-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.logo-icon i {
  font-size: 50px;
  color: white;
}

.welcome-title {
  font-size: 38px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 15px;
}

.welcome-subtitle {
  font-size: 18px;
  color: #6b7280;
  font-weight: 400;
}

.features {
  display: flex;
  justify-content: space-around;
  margin-bottom: 50px;
  gap: 30px;
}

.feature-item {
  flex: 1;
  padding: 30px 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 16px;
  transition: all 0.3s ease;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.feature-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 40px rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.3);
}

.feature-icon-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.feature-icon {
  font-size: 40px;
  color: white;
}

.feature-item h3 {
  font-size: 20px;
  color: #1f2937;
  margin-bottom: 10px;
  font-weight: 600;
}

.feature-item p {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  padding: 0 30px;
  transition: all 0.3s ease;
}

.btn-main {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-main:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.5);
}

.btn-secondary {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.3);
}

.btn-secondary:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(16, 185, 129, 0.4);
}

.btn-admin {
  background: rgba(255, 255, 255, 0.9);
  color: #4b5563;
  border: 2px solid #e5e7eb;
}

.btn-admin:hover {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

/* 登录界面样式 */
.login-box {
  width: 460px;
  padding: 50px 45px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

.login-header {
  position: relative;
  margin-bottom: 35px;
}

.back-btn {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  background: #f3f4f6;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #e5e7eb;
  transform: translateY(-50%) translateX(-3px);
}

.back-btn i {
  font-size: 20px;
  color: #4b5563;
}

.login-title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.login-form {
  margin-top: 10px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 15px;
  font-size: 20px;
  color: #9ca3af;
  z-index: 1;
}

.input-wrapper .el-input__inner {
  padding-left: 45px !important;
  height: 50px;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  font-size: 15px;
}

.input-wrapper .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.remember-checkbox {
  color: #6b7280;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 17px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  margin-top: 10px;
}

.login-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.5);
}

.login-footer {
  text-align: center;
  margin-top: 25px;
  font-size: 15px;
  color: #6b7280;
}

.register-link {
  color: #667eea;
  font-weight: 600;
  text-decoration: none;
  margin-left: 5px;
  transition: all 0.3s ease;
}

.register-link:hover {
  color: #764ba2;
  text-decoration: underline;
}
</style>
