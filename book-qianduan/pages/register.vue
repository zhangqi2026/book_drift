<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="register-title">校园闲置书籍漂流系统 - 用户注册</h2>
      <el-form :model="registerForm" :rules="registerRules" ref="registerForm" class="register-form">
        <el-form-item prop="studentId">
          <el-input v-model="registerForm.studentId" placeholder="请输入学号" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入姓名" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="college">
          <el-input v-model="registerForm.college" placeholder="请输入学院" prefix-icon="el-icon-school"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" prefix-icon="el-icon-lock"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-button" @click="handleRegister" :loading="loading">注册</el-button>
        </el-form-item>
        <div class="register-footer">
          <span>已有账号？</span>
          <nuxt-link to="/login">返回登录</nuxt-link>
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
  height: 100vh;
  background-color: #f5f5f5;
}

.register-box {
  width: 450px;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #545c64;
}

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #606266;
}

.register-footer a {
  color: #409EFF;
  text-decoration: none;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
