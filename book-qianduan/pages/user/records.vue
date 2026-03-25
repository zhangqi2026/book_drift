<template>
  <div class="records-container">
    <h2>我的记录</h2>
    <p>查看我的捐赠记录和借阅记录</p>
    
    <!-- 捐赠记录 -->
    <el-card class="record-card" shadow="hover">
      <div slot="header">
        <span>捐赠记录（共 {{ donationTotal }} 本）</span>
      </div>
      <el-table :data="donationRecords" stripe size="small">
        <el-table-column prop="bookName" label="书籍名称" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column label="捐赠时间" width="180">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.donateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.bookStatus === 1 ? 'success' : 'primary'">
              {{ scope.row.bookStatus === 1 ? '待认领' : '已漂流' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="donationRecords.length === 0" class="empty-data">暂无捐赠记录</div>
      <el-pagination
        @current-change="handleDonationPageChange"
        :current-page="donationCurrentPage"
        :page-size="donationPageSize"
        layout="total, prev, pager, next, jumper"
        :total="donationTotal"
        style="margin-top: 15px; text-align: right;"
      />
    </el-card>
    
    <!-- 借阅记录 -->
    <el-card class="record-card" shadow="hover" style="margin-top: 20px;">
      <div slot="header">
        <span>借阅记录（共 {{ borrowTotal }} 本）</span>
      </div>
      <el-table :data="borrowRecords" stripe size="small">
        <el-table-column prop="bookName" label="书籍名称" />
        <el-table-column prop="author" label="作者" width="100" />
        <el-table-column prop="donorName" label="捐赠人" width="100">
          <template slot-scope="scope">
            {{ scope.row.donorName || '匿名捐赠者' }}
          </template>
        </el-table-column>
        <el-table-column label="借书时间" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.claimTime) }}
          </template>
        </el-table-column>
        <el-table-column label="还书时间" width="160">
          <template slot-scope="scope">
            {{ scope.row.returnTime ? formatDateTime(scope.row.returnTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="书籍状态" width="100">
          <template slot-scope="scope">
            <el-tag size="small" :type="getBookStatusTagType(scope.row.bookStatus)">
              {{ getBookStatusText(scope.row.bookStatus) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="borrowRecords.length === 0" class="empty-data">暂无借阅记录</div>
      <el-pagination
        @current-change="handleBorrowPageChange"
        :current-page="borrowCurrentPage"
        :page-size="borrowPageSize"
        layout="total, prev, pager, next, jumper"
        :total="borrowTotal"
        style="margin-top: 15px; text-align: right;"
      />
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentUser: {
        id: null,
        name: '',
        college: '',
        student_id: ''
      },
      donationRecords: [], // 捐赠记录列表
      borrowRecords: [], // 借阅记录列表
      // 捐赠记录分页
      donationCurrentPage: 1,
      donationPageSize: 10,
      donationTotal: 0,
      // 借阅记录分页
      borrowCurrentPage: 1,
      borrowPageSize: 10,
      borrowTotal: 0
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchRecords()
  },
  methods: {
    getCurrentUser() {
      // 从本地存储获取用户信息
      const user = localStorage.getItem('user')
      if (user) {
        this.currentUser = JSON.parse(user)
      } else {
        // 如果没有用户信息，跳转到登录页面
        this.$router.push('/login')
      }
    },
    // 获取记录数据
    async fetchRecords() {
      try {
        // 1. 获取捐赠记录（按用户 ID 作为捐赠人查询书籍）
        const donateRecordResponse = await this.$axios.post(`/bookInfo/condition/${this.donationPageSize}/${this.donationCurrentPage}`, null, {
          params: { donorId: this.currentUser.id }
        })
        
        if (donateRecordResponse.code === 20000) {
          this.donationRecords = donateRecordResponse.data.records || []
          this.donationTotal = donateRecordResponse.data.total || 0
        }
    
        // 2. 获取借阅记录（按用户 ID 查询认领记录）
        const borrowRecordResponse = await this.$axios.post(`/bookClaimRecord/condition/${this.borrowPageSize}/${this.borrowCurrentPage}`, null, {
          params: { userId: this.currentUser.id }
        })
        
        if (borrowRecordResponse.code === 20000) {
          this.borrowRecords = borrowRecordResponse.data.records || []
          this.borrowTotal = borrowRecordResponse.data.total || 0
        }
      } catch (error) {
        console.error('获取记录数据失败:', error)
        this.$message.error('获取记录数据失败')
      }
    },
    // 处理捐赠记录分页变化
    handleDonationPageChange(val) {
      this.donationCurrentPage = val
      this.fetchRecords()
    },
    // 处理借阅记录分页变化
    handleBorrowPageChange(val) {
      this.borrowCurrentPage = val
      this.fetchRecords()
    },
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      const date = new Date(dateTime)
      if (isNaN(date.getTime())) return '-'
      
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    // 获取书籍状态标签类型
    getBookStatusTagType(status) {
      switch (status) {
        case 1: return 'warning'  // 待认领
        case 2: return 'success'  // 已认领
        case 3: return 'info'     // 已归还
        default: return 'info'
      }
    },
    // 获取书籍状态文本
    getBookStatusText(status) {
      switch (status) {
        case 1: return '待认领'
        case 2: return '已认领'
        case 3: return '已归还'
        default: return '未知'
      }
    }
  }
}
</script>

<style scoped>
.records-container {
  padding: 20px;
}

h2 {
  color: #545c64;
  margin-bottom: 10px;
}

p {
  color: #909399;
  margin-bottom: 30px;
}

.record-card {
  margin-bottom: 20px;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
