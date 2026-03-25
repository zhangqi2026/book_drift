<template>
  <div class="book-drift-container">
    <!-- 页面标题 -->
    <h2>校园闲置书籍漂流管理系统</h2>

    <!-- 搜索栏 + 分页控制区 -->
    <div class="search-pagination-bar">
      <!-- 书籍搜索 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入书籍名称搜索"
          class="search-input"
          style="width: 250px"
          @keyup.enter.native="searchBook"
        />
        <el-button type="primary" @click="searchBook" class="search-btn">搜索</el-button>
        <el-button @click="resetSearch" class="reset-btn">重置</el-button>
      </div>

      <!-- 分页控制 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[3, 5, 10]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredBookList.length"
        class="pagination-box"
      >
      </el-pagination>
    </div>

    <!-- 漂流书籍列表表格 -->
    <el-table
      :data="paginatedBookList"
      border
      stripe
      class="book-list-table"
      :empty-text="`暂无符合条件的书籍`"
      v-loading="loading"
    >
      <!-- 表格列定义 -->
      <el-table-column
        label="编号"
        width="80"
      >
        <template slot-scope="scope">
          {{ scope.$index + 1 + (currentPage - 1) * pageSize }}
        </template>
      </el-table-column>
      <el-table-column
        prop="book_name"
        label="书籍名称"
        min-width="180"
      />
      <el-table-column
        prop="author"
        label="作者"
        width="120"
      />
      <el-table-column
        prop="donor_name"
        label="捐赠人"
        width="100"
      >
        <template slot-scope="scope">
          {{ scope.row.donor_name || '匿名捐赠者' }}
        </template>
      </el-table-column>
      <el-table-column
        label="漂流次数/操作"
        min-width="200"
      >
        <template slot-scope="scope">
          <span class="drift-count">{{ scope.row.claim_count || scope.row.claimCount || 0 }}</span>
          <el-button
            type="primary"
            size="mini"
            @click.prevent="addDriftCount(scope.row.id)"
            :disabled="scope.row.book_status !== 1"
            class="oper-btn"
          >
            认领
          </el-button>
          <el-button
            type="success"
            size="mini"
            @click.prevent="viewClaimRecords(scope.row)"
            :disabled="!scope.row.has_records"
            class="record-btn"
          >
            认领记录
          </el-button>
        </template>
      </el-table-column>
      <el-table-column
        label="书籍状态"
        width="100"
      >
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.book_status)">
            {{ getStatusText(scope.row.book_status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="180"
      >
        <template slot-scope="scope">
          <el-button
            type="info"
            size="mini"
            @click.native.prevent="viewBookTrail(scope.row, $event)"
            @click.prevent="viewBookTrail(scope.row, $event)"
            class="trail-btn"
          >
            查看轨迹
          </el-button>
          <el-button
            type="danger"
            size="mini"
            @click="changeBookStatus(scope.row.id, scope.row.book_status)"
            class="status-btn"
          >
            {{ scope.row.book_status === 1 ? '下架' : (scope.row.book_status === 2 ? '归还' : '上架') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 总漂流次数统计 -->
    <div class="total-row" v-if="filteredBookList.length > 0">
      <el-tag type="info" size="medium">总漂流次数：{{ totalDriftCount }}</el-tag>
    </div>

    <!-- 认领记录弹窗 -->
    <el-dialog
      title="书籍认领记录"
      :visible.sync="recordDialogVisible"
      width="700px"
      @close="closeDialog"
    >
      <div v-if="currentBook">
        <h4 style="margin: 0 0 10px 0">《{{ currentBook.book_name || currentBook.bookName }}》借阅漂流记录</h4>
        <el-table
          :data="currentBook.claimRecords"
          border
          stripe
          :empty-text="`暂无借阅记录`"
        >
          <el-table-column prop="claimerName" label="认领人" width="120" />
          <el-table-column prop="claimTime" label="借书时间" width="180">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.claimTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="returnTime" label="还书时间" width="180">
            <template slot-scope="scope">
              {{ scope.row.returnTime ? formatDateTime(scope.row.returnTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="借阅天数" width="100">
            <template slot-scope="scope">
              {{ calculateDays(scope.row.claimTime, scope.row.returnTime) }}
            </template>
          </el-table-column>
          <el-table-column label="是否超期" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.isOverdue === 1 ? 'danger' : 'success'" size="small">
                {{ scope.row.isOverdue === 1 ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="书籍状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getBookStatusTagType(scope.row.bookStatus)" size="small">
                {{ getBookStatusText(scope.row.bookStatus) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 捐赠书籍表单 -->
    <el-card class="donate-form-container" header="捐赠闲置书籍">
      <el-form
        :model="donateBook"
        :rules="donateRules"
        ref="donateFormRef"
        label-width="100px"
        class="donate-form"
      >
        <el-form-item label="书籍名称" prop="book_name">
          <el-input v-model="donateBook.book_name" placeholder="请输入书籍名称" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="donateBook.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="donateBookSubmit" class="donate-btn">确认捐赠</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      // 漂流书籍列表
      driftBookList: [],
      // 捐赠表单数据
      donateBook: {
        book_name: '',
        author: ''
      },
      // 捐赠表单校验规则
      donateRules: {
        book_name: [{ required: true, message: '请输入书籍名称', trigger: 'blur' }],
        author: [{ required: true, message: '请输入作者', trigger: 'blur' }]
      },
      donateFormRef: null,
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      recordDialogVisible: false,
      currentBook: null,
      loading: false,
      // 当前登录用户信息
      currentUser: {
        id: null,
        student_id: '',
        name: '',
        college: '',
        role: 2
      }
    }
  },
  computed: {
    // 总漂流次数
    totalDriftCount() {
      return this.driftBookList.reduce((sum, book) => sum + (book.claimCount || 0), 0)
    },
    // 过滤后的书籍列表（根据搜索关键词）
    filteredBookList() {
      if (!this.searchKeyword) {
        return this.driftBookList
      }
      return this.driftBookList.filter(book => 
        book.bookName && book.bookName.toLowerCase().includes(this.searchKeyword.toLowerCase())
      )
    },
    // 分页后的书籍列表
    paginatedBookList() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredBookList.slice(start, end)
    }
  },
  mounted() {
    this.checkLoginStatus()
    this.initApp()
  },
  methods: {
    // 检查登录状态
    checkLoginStatus() {
      const isLoggedIn = localStorage.getItem('isLoggedIn')
      if (isLoggedIn !== 'true') {
        this.$router.push('/login')
      } else {
        // 从本地存储获取用户信息
        const user = localStorage.getItem('user')
        if (user) {
          this.currentUser = JSON.parse(user)
        }
      }
    },
    // 查看书籍轨迹（借阅和漂流记录）
    async viewBookTrail(book, event) {
      // 阻止默认行为
      if (event) {
        event.preventDefault()
        event.stopPropagation()
      }
      
      try {
        this.loading = true
        // 查询该书籍的所有历史记录
        const response = await this.$axios.get(`/bookClaimRecord/history/${book.id}`, {
          params: {
            size: 100,
            current: 1
          }
        })
        
        if (response.code === 20000) {
          this.currentBook = {
            ...book,
            claimRecords: response.data.records || []
          }
          this.recordDialogVisible = true
        } else {
          this.$message.error('获取借阅记录失败')
        }
      } catch (error) {
        this.$message.error('获取借阅记录失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    // 初始化应用
    async initApp() {
      try {
        await this.fetchBookList()
      } catch (error) {
        console.error('初始化应用失败:', error)
        this.$message.error('初始化应用失败，请刷新页面重试')
      }
    },
    // 获取书籍状态标签类型
    getStatusTagType(status) {
      switch (status) {
        case 1: return 'warning'  // 待认领
        case 2: return 'success'  // 已认领
        case 3: return 'info'   // 已归还
        default: return 'info'
      }
    },
    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 1: return '待认领'
        case 2: return '已认领'
        case 3: return '已归还'
        default: return '未知状态'
      }
    },
    // 改变书籍状态（下架/归还/上架）
    async changeBookStatus(bookId, currentStatus) {
      const newStatus = currentStatus === 1 ? 3 : (currentStatus === 2 ? 3 : 1)
      const statusNames = { 1: '待认领', 2: '已认领', 3: '已归还' }
      
      this.$confirm(`确定要将此书${newStatus === 1 ? '上架' : (newStatus === 3 ? '下架' : '归还')}吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.put(`/bookInfo`, {
            id: bookId,
            bookStatus: newStatus
          })
          
          if (response.code === 20000) {
            this.$message.success('状态更新成功！')
            this.fetchBookList()
          } else {
            this.$message.error(response.message || '状态更新失败')
          }
        } catch (error) {
          this.$message.error('状态更新失败：' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {})
    },
    // 认领书籍
    async addDriftCount(bookId) {
      const book = this.driftBookList.find(item => item.id === bookId)
      if (!book) return
      
      if (book.bookStatus !== 1) {
        this.$message.error(`该书当前状态为【${this.getStatusText(book.bookStatus)}】，无法认领！`)
        return
      }

      this.$confirm(`确定要认领《${book.bookName}》吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端 API 认领书籍
          const response = await this.$axios.post(`/bookClaimRecord/claim/${bookId}/${this.currentUser.id}`)
          
          if (response.code === 20000) {
            this.$message.success(`成功认领《${book.bookName}》！`)
            
            // 更新借阅统计数据
            await this.updateBorrowStats()
            
            this.fetchBookList()
          } else {
            this.$message.error(response.message || '认领失败')
          }
        } catch (error) {
          this.$message.error('认领失败：' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {})
    },
    // 更新借阅统计数据
    async updateBorrowStats() {
      try {
        // 更新用户的借书次数
        await this.$axios.put(`/sysUser`, {
          id: this.currentUser.id,
          borrowCount: (this.currentUser.borrowCount || 0) + 1
        })
      } catch (error) {
        console.error('更新借阅统计失败:', error)
      }
    },
    // 查看认领记录
    async viewClaimRecords(book) {
      try {
        this.loading = true
        // 查询该书籍的所有历史记录
        const response = await this.$axios.get(`/bookClaimRecord/history/${book.id}`, {
          params: {
            size: 100,
            current: 1
          }
        })
        
        if (response.code === 20000) {
          this.currentBook = {
            ...book,
            claimRecords: response.data.records || []
          }
          this.recordDialogVisible = true
        } else {
          this.$message.error('获取认领记录失败')
        }
      } catch (error) {
        this.$message.error('获取认领记录失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    // 关闭弹窗
    closeDialog() {
      this.recordDialogVisible = false
      this.currentBook = null
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
    // 计算借阅天数
    calculateDays(claimTime, returnTime) {
      if (!claimTime) return '-'
      const claimDate = new Date(claimTime)
      const returnDate = returnTime ? new Date(returnTime) : new Date()
      const diffTime = returnDate.getTime() - claimDate.getTime()
      const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
      return diffDays + '天'
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
    },
    // 捐赠书籍
    async donateBookSubmit() {
      this.$refs.donateFormRef.validate(async (valid) => {
        if (!valid) return

        try {
          this.loading = true
          
          // 生成唯一二维码（简单实现，实际应该用 QRCode.js）
          const qrcode = 'QR_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
          
          const response = await this.$axios.post(`/bookInfo`, {
            bookName: this.donateBook.book_name,
            author: this.donateBook.author,
            bookQrcode: qrcode,
            donorId: this.currentUser.id,
            bookStatus: 1
          })
          
          if (response.code === 20000) {
            this.$message.success('捐赠成功！')
            this.$refs.donateFormRef.resetFields()
            
            // 更新用户统计数据
            await this.updateUserStats()
            
            this.fetchBookList()
          } else {
            this.$message.error(response.message || '捐赠失败')
          }
        } catch (error) {
          this.$message.error('捐赠失败：' + (error.response?.data?.message || error.message))
        } finally {
          this.loading = false
        }
      })
    },
    // 更新用户统计数据
    async updateUserStats() {
      try {
        // 这里可以调用后端 API 更新统计
        // 暂时只更新本地缓存
        const userStats = localStorage.getItem('userStats')
        let stats = userStats ? JSON.parse(userStats) : { donatedBooks: 0, borrowedBooks: 0 }
        stats.donatedBooks += 1
        localStorage.setItem('userStats', JSON.stringify(stats))
      } catch (error) {
        console.error('更新用户统计失败:', error)
      }
    },
    // 获取书籍列表
    async fetchBookList() {
      try {
        this.loading = true
        const response = await this.$axios.post(`/bookInfo/condition/${this.pageSize}/${this.currentPage}`, null, {
          params: {
            bookName: this.searchKeyword
          }
        })
        
        if (response.code === 20000) {
          this.driftBookList = response.data.records || []
          this.total = response.data.total || 0
        } else {
          this.$message.error('获取书籍列表失败')
        }
      } catch (error) {
        this.$message.error('获取书籍列表失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    // 搜索书籍
    async searchBook() {
      this.currentPage = 1
      this.fetchBookList()
    },
    // 重置搜索
    resetSearch() {
      this.searchKeyword = ''
      this.currentPage = 1
      this.fetchBookList()
    },
    // 分页事件处理
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchBookList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchBookList()
    }
  }
}
</script>

<style scoped>
/* 仅保留少量自定义样式，Element组件自带大部分样式 */
.book-drift-container {
  width: 95%;
  margin: 20px auto;
  font-family: "Microsoft YaHei", sans-serif;
}

/* 搜索栏布局 */
.search-pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 5px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 漂流次数显示 */
.drift-count {
  display: inline-block;
  width: 30px;
  margin: 0 5px;
  text-align: center;
}

/* 总漂流次数 */
.total-row {
  margin: 10px 0;
  text-align: right;
}

/* 捐赠表单容器 */
.donate-form-container {
  margin-top: 20px;
}

.donate-form {
  padding: 10px 0;
}

/* 按钮样式 */
.trail-btn {
  margin-right: 5px;
}

.status-btn {
  margin-left: 5px;
}
</style>
