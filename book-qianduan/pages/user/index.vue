<template>
  <div class="home-container">
    <h1>欢迎来到校园闲置书籍漂流系统</h1>
    <p>这是一个帮助同学们共享闲置书籍的平台</p>
    <div class="action-buttons">
      <el-button type="primary" size="large" @click="goToLogin">登录</el-button>
      <el-button type="success" size="large" @click="goToRegister">注册</el-button>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    goToLogin() {
      this.$router.push('/login')
    },
    goToRegister() {
      this.$router.push('/register')
    }
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

h1 {
  color: #545c64;
  margin-bottom: 20px;
}

p {
  color: #909399;
  margin-bottom: 40px;
}

.action-buttons {
  display: flex;
  gap: 20px;
}
</style>
