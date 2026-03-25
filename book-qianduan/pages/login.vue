  <template>
  <div class="login-container">
    <!-- 开始界面 -->
    <div v-if="!showLogin" class="welcome-box">
      <h1 class="welcome-title">校园闲置书籍漂流系统</h1>
      <p class="welcome-subtitle">让闲置书籍流动起来，让知识传递下去</p>
      
      <div class="features">
        <div class="feature-item">
          <i class="el-icon-document feature-icon"></i>
          <h3>书籍捐赠</h3>
          <p>将闲置书籍捐赠给需要的同学</p>
        </div>
        <div class="feature-item">
          <i class="el-icon-reading feature-icon"></i>
          <h3>书籍认领</h3>
          <p>免费认领感兴趣的书籍</p>
        </div>
        <div class="feature-item">
          <i class="el-icon-medal feature-icon"></i>
          <h3>勋章奖励</h3>
          <p>获得捐赠和借阅勋章</p>
        </div>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="showLogin = true">用户登录</el-button>
        <el-button type="success" size="large" @click="goToRegister">立即注册</el-button>
        <el-button size="large" @click="handleAdminLogin">管理员入口</el-button>
      </div>
    </div>
    
    <!-- 登录界面 -->
    <div v-else class="login-box">
      <h2 class="login-title">用户登录</h2>
      <el-form :model="userLoginForm" :rules="userLoginRules" ref="userLoginForm" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="userLoginForm.username" placeholder="请输入用户名/学号" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="userLoginForm.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" @keyup.enter.native="handleUserLogin"></el-input>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="userLoginForm.remember">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-button" @click="handleUserLogin" :loading="userLoading">登录</el-button>
        </el-form-item>
        <div class="login-footer">
          <span>还没有账号？</span>
          <nuxt-link to="/register">立即注册</nuxt-link>
          <span style="margin-left: 15px;">|</span>
          <span style="margin-left: 15px; cursor: pointer; color: #409EFF;" @click="showLogin = false">返回首页</span>
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.welcome-box {
  width: 900px;
  padding: 50px;
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.welcome-title {
  font-size: 36px;
  color: #545c64;
  margin-bottom: 15px;
}

.welcome-subtitle {
  font-size: 18px;
  color: #909399;
  margin-bottom: 50px;
}

.features {
  display: flex;
  justify-content: space-around;
  margin-bottom: 50px;
}

.feature-item {
  flex: 1;
  padding: 20px;
}

.feature-icon {
  font-size: 60px;
  color: #409EFF;
  margin-bottom: 15px;
}

.feature-item h3 {
  font-size: 20px;
  color: #545c64;
  margin-bottom: 10px;
}

.feature-item p {
  font-size: 14px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.action-buttons .el-button {
  width: 140px;
  height: 45px;
  font-size: 16px;
}

.login-box {
  width: 450px;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #545c64;
  font-size: 24px;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
}

.login-footer {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #606266;
}

.login-footer a {
  color: #409EFF;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>
