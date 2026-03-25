<template>
  <div class="admin-page">
    <h1>反馈管理</h1>
    <AdminFeedback />
  </div>
</template>

<script>
import AdminFeedback from '@/components/admin/AdminFeedback.vue'

export default {
  name: 'AdminFeedbackPage',
  components: {
    AdminFeedback
  },
  data() {
    return {
      currentUser: {}
    }
  },
  mounted() {
    this.getCurrentUser()
  },
  methods: {
    getCurrentUser() {
      const admin = localStorage.getItem('admin')
      if (admin) {
        this.currentUser = JSON.parse(admin)
      } else {
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style scoped>
.admin-page {
  padding: 20px;
}

h1 {
  color: #303133;
  margin-bottom: 20px;
}
</style>
