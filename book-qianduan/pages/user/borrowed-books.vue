<template>
  <div class="borrowed-books-container">
    <div class="page-header">
      <h2>已借阅书籍</h2>
      <el-button @click="goBack" icon="el-icon-back">返回</el-button>
    </div>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入书籍名称搜索"
        style="width: 300px"
        clearable
        @clear="handleSearch"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
      </el-input>
      <el-select v-model="statusFilter" placeholder="书籍状态" clearable @change="handleSearch">
        <el-option label="全部" value=""></el-option>
        <el-option label="已归还" value="3"></el-option>
        <el-option label="未归还" value="2"></el-option>
      </el-select>
    </div>
    
    <!-- 借阅书籍列表 -->
    <el-table
      :data="filteredBookList"
      border
      stripe
      class="book-list-table"
      :empty-text="`暂无借阅书籍`"
    >
      <el-table-column
        label="编号"
        width="80"
      >
        <template slot-scope="scope">
          {{ scope.$index + 1 + (currentPage - 1) * pageSize }}
        </template>
      </el-table-column>
      <el-table-column
        prop="bookName"
        label="书籍名称"
        min-width="180"
      />
      <el-table-column
        prop="author"
        label="作者"
        width="120"
      />
      <el-table-column
        prop="claimTime"
        label="借阅时间"
        width="180"
      />
      <el-table-column
        prop="returnTime"
        label="归还时间"
        width="180"
      />
      <el-table-column
        label="书籍状态"
        width="100"
      >
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.bookStatus)">
            {{ getStatusText(scope.row.bookStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="150"
        fixed="right"
      >
        <template slot-scope="scope">
          <el-button
            type="success"
            size="mini"
            @click="returnBook(scope.row)"
            :disabled="scope.row.bookStatus === 3"
          >
            归还
          </el-button>
          <el-button
            type="info"
            size="mini"
            @click="viewTrail(scope.row)"
          >
            轨迹
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      class="pagination-box"
    >
    </el-pagination>

    <!-- 书籍轨迹弹窗 -->
    <el-dialog
      title="书籍漂流轨迹"
      :visible.sync="trailDialogVisible"
      width="800px"
      :before-close="closeTrailDialog"
    >
      <div v-if="currentBook" class="trail-dialog-content">
        <div class="book-info">
          <h4>《{{ currentBook.bookName }}》</h4>
          <p>作者：{{ currentBook.author || '未知' }}</p>
        </div>
        
        <!-- 轨迹记录表格 -->
        <el-table
          :data="trailRecords"
          border
          stripe
          style="width: 100%; margin-top: 15px;"
          :empty-text="'暂无借阅记录'"
        >
          <el-table-column
            prop="claimerName"
            label="借阅人"
            width="120"
          />
          <el-table-column
            prop="claimTime"
            label="借阅时间"
            width="180"
          />
          <el-table-column
            prop="returnTime"
            label="归还时间"
            width="180"
          />
          <el-table-column
            prop="dueTime"
            label="应还时间"
            width="180"
          />
          <el-table-column
            label="状态"
            width="100"
          >
            <template slot-scope="scope">
              <el-tag :type="scope.row.returnTime ? 'success' : 'warning'">
                {{ scope.row.returnTime ? '已归还' : '借阅中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="是否超期"
            width="100"
          >
            <template slot-scope="scope">
              <el-tag :type="scope.row.isOverdue === 1 ? 'danger' : 'success'">
                {{ scope.row.isOverdue === 1 ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentUser: {
        id: null
      },
      borrowedBooksList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchKeyword: '',
      statusFilter: '',
      loading: false,
      // 轨迹弹窗相关
      trailDialogVisible: false,
      currentBook: null,
      trailRecords: []
    }
  },
  computed: {
    filteredBookList() {
      let list = this.borrowedBooksList
      
      // 按书籍名称筛选
      if (this.searchKeyword) {
        list = list.filter(book => 
          book.bookName && book.bookName.toLowerCase().includes(this.searchKeyword.toLowerCase())
        )
      }
      
      // 按状态筛选
      if (this.statusFilter) {
        list = list.filter(book => book.bookStatus === parseInt(this.statusFilter))
      }
      
      return list
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchBorrowedBooks()
  },
  methods: {
    getCurrentUser() {
      const user = localStorage.getItem('user')
      if (user) {
        this.currentUser = JSON.parse(user)
      } else {
        this.$router.push('/login')
      }
    },
    async fetchBorrowedBooks() {
      try {
        this.loading = true
        
        // 调用后端 API 获取借阅记录
        const response = await this.$axios.post(`/bookClaimRecord/condition/${this.pageSize}/${this.currentPage}`, null, {
          params: { userId: this.currentUser.id }
        })
        
        if (response.code === 20000) {
          this.borrowedBooksList = (response.data.records || []).map(record => ({
            id: record.bookId,
            recordId: record.id,
            bookName: record.bookName,
            author: record.author,
            claimTime: record.claimTime,
            returnTime: record.returnTime,
            dueTime: record.dueTime,
            isOverdue: record.isOverdue,
            bookStatus: record.returnTime ? 3 : 2, // 3-已归还，2-已认领
            userName: record.userName
          }))
          this.total = response.data.total || 0
        }
      } catch (error) {
        this.$message.error('获取借阅书籍列表失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    async returnBook(book) {
      this.$confirm(`确定要归还《${book.bookName}》吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.post(`/bookClaimRecord/return/${book.recordId}`)
          
          if (response.code === 20000) {
            this.$message.success('书籍归还成功')
            this.fetchBorrowedBooks()
          } else {
            this.$message.error(response.message || '归还失败')
          }
        } catch (error) {
          this.$message.error('归还失败：' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {})
    },
    async viewTrail(book) {
      try {
        // 调用后端接口获取该书籍的历史借阅记录
        const response = await this.$axios.get(`/bookClaimRecord/history/${book.id}`, {
          params: {
            size: 100,
            current: 1
          }
        })
        
        if (response.code === 20000) {
          // 设置当前书籍信息
          this.currentBook = {
            bookName: book.bookName,
            author: book.author
          }
          // 设置轨迹记录
          this.trailRecords = response.data.records || []
          console.log('轨迹记录:', this.trailRecords)
          // 显示弹窗
          this.trailDialogVisible = true
        } else {
          this.$message.error('获取借阅记录失败')
        }
      } catch (error) {
        console.error('获取借阅记录错误:', error)
        this.$message.error('获取借阅记录失败：' + (error.response?.data?.message || error.message))
      }
    },
    closeTrailDialog() {
      this.trailDialogVisible = false
      this.currentBook = null
      this.trailRecords = []
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchBorrowedBooks()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchBorrowedBooks()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchBorrowedBooks()
    },
    getStatusTagType(status) {
      switch (status) {
        case 2: return 'success'  // 已认领
        case 3: return 'info'     // 已归还
        default: return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 2: return '已认领'
        case 3: return '已归还'
        default: return '未知状态'
      }
    },
    goBack() {
      this.$router.push('/user/home')
    }
  }
}
</script>

<style scoped>
.borrowed-books-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #545c64;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.book-list-table {
  margin-bottom: 20px;
}

.pagination-box {
  text-align: right;
}

.trail-dialog-content {
  padding: 10px 0;
}

.book-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 5px;
}

.book-info h4 {
  margin: 0 0 10px 0;
  color: #409EFF;
  font-size: 16px;
}

.book-info p {
  margin: 5px 0;
  color: #606266;
}
</style>
