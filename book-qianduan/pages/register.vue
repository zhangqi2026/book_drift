<template>
  <div class="register-container">
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
    
    <div class="register-box slide-in">
      <div class="register-header">
        <div class="back-btn" @click="$router.push('/login')">
          <i class="el-icon-arrow-left"></i>
        </div>
        <h2 class="register-title">
          <span class="title-glow">用户注册</span>
        </h2>
      </div>
      
      <el-form :model="registerForm" :rules="registerRules" ref="registerForm" class="register-form">
        <el-form-item prop="studentId">
          <div class="input-wrapper">
            <div class="input-border">
              <el-input v-model="registerForm.studentId" placeholder="请输入学号" prefix-icon="el-icon-user" class="custom-input"></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item prop="name">
          <div class="input-wrapper">
            <div class="input-border">
              <el-input v-model="registerForm.name" placeholder="请输入姓名" prefix-icon="el-icon-user" class="custom-input"></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item prop="college">
          <div class="input-wrapper">
            <div class="input-border">
              <el-input v-model="registerForm.college" placeholder="请输入学院" prefix-icon="el-icon-school" class="custom-input"></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <div class="input-wrapper">
            <div class="input-border">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock" @keyup.enter.native="handleRegister" class="custom-input"></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <div class="input-wrapper">
            <div class="input-border">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="el-icon-lock" @keyup.enter.native="handleRegister" class="custom-input"></el-input>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-button neon-btn" @click="handleRegister" :loading="loading">
            <span v-if="!loading">注 册</span>
          </el-button>
        </el-form-item>
        <div class="register-footer">
          <span>已有账号？</span>
          <nuxt-link to="/login" class="login-link">返回登录</nuxt-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    // 验证密码一致性
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      registerForm: {
        studentId: '',
        name: '',
        college: '',
        password: '',
        confirmPassword: ''
      },
      registerRules: {
        studentId: [
          { required: true, message: '请输入学号', trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        college: [
          { required: true, message: '请输入学院', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      loading: false
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
    async handleRegister() {
      this.$refs.registerForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            // 调用后端 API 进行注册
            const response = await this.$axios.post('/sysUser', {
              studentId: this.registerForm.studentId,
              name: this.registerForm.name,
              college: this.registerForm.college,
              password: this.registerForm.password,
              role: 2 // 默认为普通用户
            })
            
            if (response.code === 20000) {
              this.$message.success('注册成功，请登录')
              this.$router.push('/login')
            } else {
              this.$message.error(response.message || '注册失败')
            }
            this.loading = false
          } catch (error) {
            this.$message.error('注册失败：' + (error.response?.data?.message || error.message))
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.register-container {
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

.register-box {
  width: 520px;
  padding: 45px 50px;
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

.register-box::before {
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

.register-header {
  position: relative;
  margin-bottom: 30px;
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

.register-title {
  text-align: center;
  font-size: 30px;
  font-weight: 800;
  margin: 0;
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

.register-form {
  margin-top: 15px;
}

.register-form >>> .el-form-item {
  margin-bottom: 18px;
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

.register-button {
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

.register-button:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 
    0 15px 45px rgba(171, 240, 209, 0.6),
    0 0 40px rgba(171, 240, 209, 0.5);
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

.register-footer {
  text-align: center;
  margin-top: 30px;
  font-size: 15px;
  color: #8a9a8a;
}

.login-link {
  color: #6a9a7a;
  font-weight: 600;
  text-decoration: none;
  margin-left: 6px;
  transition: all 0.3s ease;
  text-shadow: 0 0 10px rgba(171, 240, 209, 0.4);
}

.login-link:hover {
  color: #7a9d5a;
  text-decoration: underline;
  text-shadow: 0 0 20px rgba(171, 240, 209, 0.6);
}
</style>
