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
        :total="total"
        class="pagination-box"
      >
      </el-pagination>
    </div>

    <!-- 漂流书籍列表表格 -->
    <el-table
      :data="driftBookList"
      border
      stripe
      class="book-list-table"
      :empty-text="`暂无符合条件的书籍`"
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
        label="操作"
        min-width="200"
      >
        <template slot-scope="scope">
          <!-- 判断当前用户与书籍的关系 -->
          
          <!-- 情况 1：当前用户是持有人（已认领状态） -->
          <el-button
            v-if="scope.row.bookStatus === 2 && scope.row.currentHolderId === currentUser.id"
            type="success"
            size="mini"
            @click.prevent="returnBook(scope.row.id)"
            class="oper-btn"
          >
            归还
          </el-button>
          
          <!-- 情况 2：当前用户不是持有人，但书籍是已认领状态 -->
          <span v-if="scope.row.bookStatus === 2 && scope.row.currentHolderId !== currentUser.id" class="borrowed-info">
            <el-tag type="warning" size="mini">已被他人借走</el-tag>
          </span>
          
          <!-- 情况 3：书籍待认领或已归还，且不是自己捐赠的 -->
          <el-button
            v-if="(scope.row.bookStatus === 1 || scope.row.bookStatus === 3) && scope.row.donorId !== currentUser.id"
            type="primary"
            size="mini"
            @click.prevent="addDriftCount(scope.row.id)"
            class="oper-btn"
          >
            {{ scope.row.bookStatus === 1 ? '认领' : '认领（已归还）' }}
          </el-button>
          
          <!-- 情况 4：书籍是自己捐赠的 -->
          <span v-if="scope.row.donorId === currentUser.id" class="donated-info">
            <el-tag type="info" size="mini">
              {{ scope.row.bookStatus === 1 ? '我捐赠的（待认领）' : (scope.row.bookStatus === 3 ? '我捐赠的（已归还）' : '我捐赠的（已借出）') }}
            </el-tag>
          </span>
          
          <el-button
            type="warning"
            size="mini"
            @click="viewBookNotes(scope.row)"
            class="notes-btn"
          >
            读书笔记
          </el-button>
        </template>
      </el-table-column>
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
        label="查看"
        width="180"
      >
        <template slot-scope="scope">
          <el-button
            type="primary"
            size="mini"
            @click="viewClaimRecords(scope.row)"
            class="trail-btn"
          >
            查看轨迹
          </el-button>
          <!-- 仅管理员显示上下架按钮 -->
          <el-button
            v-if="currentUser.role === 1"
            type="danger"
            size="mini"
            @click="changeBookStatus(scope.row.id, scope.row.bookStatus)"
            class="status-btn"
          >
            {{ scope.row.bookStatus === 1 ? '下架' : (scope.row.bookStatus === 2 ? '归还' : '上架') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 书籍轨迹弹窗 -->
    <el-dialog
      title="书籍漂流轨迹"
      :visible.sync="recordDialogVisible"
      width="900px"
      @close="closeDialog"
    >
      <div v-if="currentBook">
        <div class="book-info-header" style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 5px;">
          <h4 style="margin: 0 0 10px 0; color: #409EFF;">《{{ currentBook.bookName || currentBook.book_name || '未知书籍' }}》</h4>
          <p style="margin: 5px 0; color: #606266;">作者：{{ currentBook.author || '未知' }}</p>
        </div>
        
        <el-table
          :data="currentBook.claimRecords"
          border
          stripe
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
        
        <!-- 分页组件 -->
        <el-pagination
          @current-change="handleTrailPageChange"
          :current-page="trailCurrentPage"
          :page-size="trailPageSize"
          layout="total, prev, pager, next, jumper"
          :total="trailTotal"
          style="margin-top: 15px; text-align: right;"
        />
      </div>
    </el-dialog>

    <!-- 读书笔记弹窗 -->
    <el-dialog
      title="读书笔记"
      :visible.sync="notesDialogVisible"
      width="95%"
      @close="closeNotesDialog"
    >
      <div v-if="currentNoteBook">
        <div class="book-info-header" style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 5px;">
          <h4 style="margin: 0 0 10px 0; color: #409EFF;">《{{ currentNoteBook.bookName || currentNoteBook.book_name || '未知书籍' }}》</h4>
          <p style="margin: 5px 0; color: #606266;">作者：{{ currentNoteBook.author || '未知' }}</p>
        </div>
        
        <!-- 写笔记按钮 -->
        <div style="margin-bottom: 15px;">
          <el-button
            type="primary"
            @click="showAddNoteDialog"
            icon="el-icon-edit"
          >
            写笔记
          </el-button>
        </div>
        
        <!-- 笔记列表 -->
        <el-table
          :data="bookNotes"
          border
          stripe
          :empty-text="'暂无读书笔记'"
          style="margin-bottom: 20px;"
        >
          <el-table-column
            prop="noteContent"
            label="笔记内容"
            min-width="300"
            show-overflow-tooltip
          />
          <el-table-column
            prop="userName"
            label="笔记作者"
            width="120"
          />
          <el-table-column
            prop="likeCount"
            label="点赞数"
            width="80"
            align="center"
          />
          <el-table-column
            label="笔记时间"
            width="180"
            align="center"
          >
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="220"
            align="center"
            fixed="right"
          >
            <template slot-scope="scope">
              <div style="display: flex; gap: 5px; justify-content: center;">
                <el-button
                  type="primary"
                  size="mini"
                  @click="likeNote(scope.row)"
                  style="padding: 7px 10px;"
                >
                  {{ scope.row.isLiked ? '取消点赞' : '点赞' }}
                </el-button>
                <!-- 仅笔记作者可以编辑删除 -->
                <template v-if="scope.row.userId == currentUser.id">
                  <el-button
                    type="warning"
                    size="mini"
                    @click="editNote(scope.row)"
                    style="padding: 7px 10px;"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="mini"
                    @click="deleteNote(scope.row.id)"
                    style="padding: 7px 10px;"
                  >
                    删除
                  </el-button>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页组件 -->
        <el-pagination
          @current-change="handleNotesPageChange"
          :current-page="notesCurrentPage"
          :page-size="notesPageSize"
          layout="total, prev, pager, next, jumper"
          :total="notesTotal"
          style="text-align: right;"
        />
      </div>
    </el-dialog>

    <!-- 添加/编辑笔记弹窗 -->
    <el-dialog
      :title="noteFormMode === 'add' ? '添加读书笔记' : '编辑读书笔记'"
      :visible.sync="noteFormDialogVisible"
      width="600px"
      @close="closeNoteFormDialog"
    >
      <el-form
        :model="noteForm"
        :rules="noteFormRules"
        ref="noteFormRef"
        label-width="80px"
      >
        <el-form-item label="笔记内容" prop="noteContent">
          <el-input
            v-model="noteForm.noteContent"
            type="textarea"
            :rows="8"
            placeholder="请输入你的读书笔记..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="noteFormDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitNoteForm" :loading="submitting">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 捐赠书籍表单（仅普通用户显示） -->
    <el-card v-if="currentUser.role === 2" class="donate-form-container" header="捐赠闲置书籍">
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
import axios from 'axios'

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
      pageSize: 3,
      total: 0,
      recordDialogVisible: false,
      currentBook: null,
      // 轨迹记录分页
      trailCurrentPage: 1,
      trailPageSize: 10,
      trailTotal: 0,
      // 读书笔记相关
      notesDialogVisible: false,
      currentNoteBook: null,
      bookNotes: [],
      notesCurrentPage: 1,
      notesPageSize: 5,
      notesTotal: 0,
      // 笔记表单相关
      noteFormDialogVisible: false,
      noteFormMode: 'add', // 'add' 或 'edit'
      noteForm: {
        id: null,
        noteContent: '',
        bookId: null
      },
      noteFormRules: {
        noteContent: [
          { required: true, message: '请输入笔记内容', trigger: 'blur' },
          { min: 10, max: 2000, message: '笔记内容长度在 10-2000 个字符之间', trigger: 'blur' }
        ]
      },
      noteFormRef: null,
      submitting: false,
      // 当前登录用户信息
      currentUser: {
        id: null,
        student_id: '',
        name: '',
        college: ''
      },
      // API 基础 URL
      apiBaseUrl: 'http://localhost:3000/api'
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
    // 更新用户统计数据
    updateUserStats() {
      // 从本地存储获取用户统计数据
      const userStats = localStorage.getItem('userStats')
      let stats = {
        donatedBooks: 0,
        borrowedBooks: 0,
        unlockedBadges: 0
      }
      
      if (userStats) {
        stats = JSON.parse(userStats)
      }
      
      // 更新捐赠书籍数量
      stats.donatedBooks += 1
      
      // 计算已解锁的勋章数量
      const donationBadges = [
        { required: 1 },
        { required: 10 },
        { required: 20 },
        { required: 50 }
      ]
      
      const borrowingBadges = [
        { required: 1 },
        { required: 10 },
        { required: 20 },
        { required: 50 }
      ]
      
      let unlockedBadges = 0
      
      donationBadges.forEach(badge => {
        if (stats.donatedBooks >= badge.required) {
          unlockedBadges += 1
        }
      })
      
      borrowingBadges.forEach(badge => {
        if (stats.borrowedBooks >= badge.required) {
          unlockedBadges += 1
        }
      })
      
      stats.unlockedBadges = unlockedBadges
      
      // 保存更新后的统计数据到本地存储
      localStorage.setItem('userStats', JSON.stringify(stats))
    },
    // 初始化应用
    async initApp() {
      try {
        // 先获取用户信息
        await this.fetchCurrentUser()
        // 再获取书籍列表
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
        case 3: return 'danger'   // 已归还
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
    // 改变书籍状态
    async changeBookStatus(bookId, currentStatus) {
      try {
        const newStatus = currentStatus === 1 ? 3 : (currentStatus === 2 ? 3 : 1)
        await this.apiRequest('put', `/book/${bookId}/status`, { status: newStatus })
        this.$message.success('状态更新成功！')
        this.fetchBookList()
      } catch (error) {
        this.$message.error('状态更新失败：' + error.message)
      }
    },
    // 认领书籍
    async addDriftCount(bookId) {
      try {
        const book = this.driftBookList.find(item => item.id === bookId)
        if (!book) return
        
        // 检查书籍状态是否为待认领（1）或已归还（3）
        if (book.bookStatus !== 1 && book.bookStatus !== 3) {
          this.$message.error(`该书当前状态为【${this.getStatusText(book.bookStatus)}】，无法认领！`)
          return
        }

        // 调用后端认领接口
        const response = await this.$axios.post(
          `/bookClaimRecord/claim/${bookId}/${this.currentUser.id}`
        )
        
        if (response.code === 20000) {
          const statusMsg = book.bookStatus === 1 ? '成功认领' : '成功重新认领'
          this.$message.success(`${statusMsg}《${book.bookName}》！请在 7 天内归还～`)
          this.fetchBookList()
        } else {
          this.$message.error('认领失败：' + response.message)
        }
      } catch (error) {
        this.$message.error('认领失败：' + error.message)
      }
    },
    // 归还书籍
    async returnBook(bookId) {
      try {
        const book = this.driftBookList.find(item => item.id === bookId)
        if (!book) return
        
        // 查找该用户当前的借阅记录
        const recordResponse = await this.$axios.post(
          `/bookClaimRecord/condition/10/1`,
          null,
          { params: { userId: this.currentUser.id }}
        )
        
        if (recordResponse.code !== 20000) {
          this.$message.error('查询借阅记录失败')
          return
        }
        
        // 找到这本书的借阅记录
        const record = recordResponse.data.records.find(r => r.bookId === bookId && !r.returnTime)
        
        if (!record) {
          this.$message.error('未找到借阅记录')
          return
        }
        
        // 检查是否超期 - 修复时间计算问题
        const now = new Date()
        
        // 处理 dueTime 为 null 的情况
        let dueTime
        if (!record.dueTime) {
          // 如果没有 dueTime，使用 claimTime + 7 天计算
          // claimTime 格式可能是 "2026-03-19" 或 "2026-03-19T10:30:00.000Z"
          let claimDate
          if (record.claimTime && record.claimTime.includes('T')) {
            // ISO 格式，需要转换为本地时间
            claimDate = new Date(record.claimTime)
          } else if (record.claimTime) {
            // 日期格式 "2026-03-19"
            const parts = record.claimTime.split('-')
            claimDate = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
          } else {
            // 默认使用当前时间
            claimDate = new Date()
          }
          dueTime = new Date(claimDate)
          dueTime.setDate(claimDate.getDate() + 7)
          dueTime.setHours(23, 59, 59)
        } else {
          // 将 dueTime (格式："2026-04-18") 转换为日期对象，设置为当天 23:59:59
          const dueDateStr = record.dueTime
          const dueParts = dueDateStr.split('-')
          const dueYear = parseInt(dueParts[0])
          const dueMonth = parseInt(dueParts[1]) - 1 // 月份从 0 开始
          const dueDay = parseInt(dueParts[2])
          dueTime = new Date(dueYear, dueMonth, dueDay, 23, 59, 59)
        }
        
        if (now > dueTime) {
          const daysOverdue = Math.floor((now.getTime() - dueTime.getTime()) / (1000 * 60 * 60 * 24))
          this.$confirm(`您已超期 ${daysOverdue} 天，请尽快归还书籍！`, '超期提示', {
            confirmButtonText: '确定归还',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(async () => {
            await this.doReturnBook(record.id, book)
          })
        } else {
          this.$confirm(`确定要归还《${book.bookName}》吗？`, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info'
          }).then(async () => {
            await this.doReturnBook(record.id, book)
          })
        }
      } catch (error) {
        console.error('归还失败:', error)
        this.$message.error('归还失败：' + error.message)
      }
    },
    // 执行归还操作
    async doReturnBook(recordId, book) {
      try {
        const response = await this.$axios.post(`/bookClaimRecord/return/${recordId}`)
        
        if (response.code === 20000) {
          this.$message.success(`《${book.bookName}》归还成功！`)
          this.fetchBookList()
        } else {
          this.$message.error('归还失败：' + response.message)
        }
      } catch (error) {
        this.$message.error('归还失败：' + error.message)
      }
    },
    // 更新借阅统计数据
    updateBorrowStats() {
      // 从本地存储获取用户统计数据
      const userStats = localStorage.getItem('userStats')
      let stats = {
        donatedBooks: 0,
        borrowedBooks: 0,
        unlockedBadges: 0
      }
      
      if (userStats) {
        stats = JSON.parse(userStats)
      }
      
      // 更新借阅书籍数量
      stats.borrowedBooks += 1
      
      // 计算已解锁的勋章数量
      const donationBadges = [
        { required: 1 },
        { required: 10 },
        { required: 20 },
        { required: 50 }
      ]
      
      const borrowingBadges = [
        { required: 1 },
        { required: 10 },
        { required: 20 },
        { required: 50 }
      ]
      
      let unlockedBadges = 0
      
      donationBadges.forEach(badge => {
        if (stats.donatedBooks >= badge.required) {
          unlockedBadges += 1
        }
      })
      
      borrowingBadges.forEach(badge => {
        if (stats.borrowedBooks >= badge.required) {
          unlockedBadges += 1
        }
      })
      
      stats.unlockedBadges = unlockedBadges
      
      // 保存更新后的统计数据到本地存储
      localStorage.setItem('userStats', JSON.stringify(stats))
    },
    // 添加到借阅书籍列表
    addToBorrowedBooks(book) {
      // 从本地存储获取借阅书籍列表
      const borrowedBooks = localStorage.getItem('borrowedBooks')
      let books = []
    
      if (borrowedBooks) {
        books = JSON.parse(borrowedBooks)
      }
    
      // 添加新借阅的书籍
      books.unshift({
        id: book.id,
        book_name: book.book_name,
        author: book.author,
        borrow_time: new Date().toISOString(),
        return_time: null,
        status: 2, // 2表示未归还
        notes: []
      })
    
      // 保存到本地存储
      localStorage.setItem('borrowedBooks', JSON.stringify(books))
    },
    // 查看书籍轨迹（借阅和漂流记录）
    async viewClaimRecords(book) {
      try {
        // 调用后端接口获取漂流轨迹
        const response = await this.$axios.get(`/bookClaimRecord/history/${book.id}`, {
          params: { current: this.trailCurrentPage, size: this.trailPageSize }
        })
        
        if (response.code === 20000 && response.data) {
          this.currentBook = {
            ...book,
            claimRecords: response.data.records || []
          }
          this.trailTotal = response.data.total || 0
          this.recordDialogVisible = true
        } else {
          this.$message.error('获取认领记录失败')
        }
      } catch (error) {
        this.$message.error('获取认领记录失败：' + (error.response?.data?.message || error.message))
      }
    },
    // 处理轨迹记录分页变化
    async handleTrailPageChange(val) {
      this.trailCurrentPage = val
      // 重新加载当前书籍的轨迹记录
      if (this.currentBook && this.currentBook.id) {
        await this.viewClaimRecords(this.currentBook)
      }
    },
    // 关闭弹窗
    closeDialog() {
      this.recordDialogVisible = false
      this.currentBook = null
      this.trailCurrentPage = 1
      this.trailTotal = 0
    },
    // 查看读书笔记
    async viewBookNotes(book) {
      try {
        this.currentNoteBook = book
        // 调用后端接口获取读书笔记 - 使用 POST 方式
        const response = await this.$axios.post(`/bookNote/condition/${this.notesPageSize}/${this.notesCurrentPage}`, null, {
          params: { 
            bookId: book.id,
            userId: this.currentUser.id
          }
        })
        
        if (response.code === 20000 && response.data) {
          this.bookNotes = response.data.records || []
          this.notesTotal = response.data.total || 0
          this.notesDialogVisible = true
        } else {
          this.bookNotes = []
          this.notesTotal = 0
          this.notesDialogVisible = true
        }
      } catch (error) {
        this.$message.error('获取读书笔记失败：' + (error.response?.data?.message || error.message))
      }
    },
    // 处理笔记分页变化
    async handleNotesPageChange(val) {
      this.notesCurrentPage = val
      // 重新加载当前书籍的笔记
      if (this.currentNoteBook && this.currentNoteBook.id) {
        await this.viewBookNotes(this.currentNoteBook)
      }
    },
    // 关闭笔记弹窗
    closeNotesDialog() {
      this.notesDialogVisible = false
      this.currentNoteBook = null
      this.bookNotes = []
      this.notesCurrentPage = 1
      this.notesTotal = 0
    },
    // 点赞笔记
    async likeNote(note) {
      try {
        let response
        if (note.isLiked) {
          // 取消点赞
          response = await this.$axios.post(`/bookNote/unlike/${note.id}`, null, {
            params: { userId: this.currentUser.id }
          })
          if (response.code === 20000) {
            note.likeCount = Math.max(0, (note.likeCount || 0) - 1)
            note.isLiked = false
            this.$message.success('已取消点赞')
          } else {
            this.$message.error('取消点赞失败：' + response.message)
          }
        } else {
          // 点赞
          response = await this.$axios.post(`/bookNote/like/${note.id}`, null, {
            params: { userId: this.currentUser.id }
          })
          if (response.code === 20000) {
            note.likeCount = (note.likeCount || 0) + 1
            note.isLiked = true
            this.$message.success('点赞成功！')
          } else {
            this.$message.error('点赞失败：' + response.message)
          }
        }
      } catch (error) {
        this.$message.error('操作失败：' + error.message)
      }
    },
    // 显示添加笔记弹窗
    showAddNoteDialog() {
      this.noteFormMode = 'add'
      this.noteForm = {
        id: null,
        noteContent: '',
        bookId: this.currentNoteBook.id
      }
      this.noteFormDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.noteFormRef) {
          this.$refs.noteFormRef.clearValidate()
        }
      })
    },
    // 编辑笔记
    editNote(note) {
      this.noteFormMode = 'edit'
      this.noteForm = {
        id: note.id,
        noteContent: note.noteContent,
        bookId: this.currentNoteBook.id
      }
      this.noteFormDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.noteFormRef) {
          this.$refs.noteFormRef.clearValidate()
        }
      })
    },
    // 提交笔记表单
    async submitNoteForm() {
      if (!this.$refs.noteFormRef) return
      
      await this.$refs.noteFormRef.validate(async (valid) => {
        if (!valid) return
        
        this.submitting = true
        try {
          let response
          if (this.noteFormMode === 'add') {
            // 新增笔记
            response = await this.$axios.post('/bookNote', {
              noteContent: this.noteForm.noteContent,
              bookId: this.noteForm.bookId,
              userId: this.currentUser.id
            })
          } else {
            // 编辑笔记
            response = await this.$axios.put('/bookNote', {
              id: this.noteForm.id,
              noteContent: this.noteForm.noteContent
            })
          }
          
          if (response.code === 20000) {
            this.$message.success(this.noteFormMode === 'add' ? '添加成功！' : '修改成功！')
            this.noteFormDialogVisible = false
            // 重新加载笔记列表
            await this.viewBookNotes(this.currentNoteBook)
          } else {
            this.$message.error((this.noteFormMode === 'add' ? '添加' : '修改') + '失败：' + response.message)
          }
        } catch (error) {
          this.$message.error((this.noteFormMode === 'add' ? '添加' : '修改') + '失败：' + error.message)
        } finally {
          this.submitting = false
        }
      })
    },
    // 删除笔记
    async deleteNote(noteId) {
      try {
        await this.$confirm('确定要删除这条笔记吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await this.$axios.delete(`/bookNote/${noteId}`)
        
        if (response.code === 20000) {
          this.$message.success('删除成功！')
          // 重新加载笔记列表
          await this.viewBookNotes(this.currentNoteBook)
        } else {
          this.$message.error('删除失败：' + response.message)
        }
      } catch (error) {
        // 用户点击取消时不显示错误
        if (error !== 'cancel' && error.message !== 'cancel') {
          this.$message.error('删除失败：' + error.message)
        }
      }
    },
    // 关闭笔记表单弹窗
    closeNoteFormDialog() {
      this.noteFormDialogVisible = false
      this.noteForm = {
        id: null,
        noteContent: '',
        bookId: null
      }
    },
    // 捐赠书籍
    async donateBookSubmit() {
      this.$refs.donateFormRef.validate(async (valid) => {
        if (!valid) return

        try {
          // 生成唯一二维码（简单实现：时间戳 + 随机数）
          const qrcode = 'QR_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
          
          const response = await this.$axios.post('/bookInfo', {
            bookQrcode: qrcode,
            bookName: this.donateBook.book_name,
            author: this.donateBook.author,
            donorId: this.currentUser.id,
            bookStatus: 1
          })
          
          if (response.code === 20000) {
            this.$refs.donateFormRef.resetFields()
            this.$message.success('捐赠成功！书籍已加入漂流列表~')
            this.fetchBookList()
          } else {
            this.$message.error('捐赠失败：' + response.message)
          }
        } catch (error) {
          this.$message.error('捐赠失败：' + error.message)
        }
      })
    },
    // 获取书籍列表
    async fetchBookList() {
      try {
        const response = await this.$axios.post(`/bookInfo/condition/${this.pageSize}/${this.currentPage}`, null, {
          params: { bookName: this.searchKeyword }
        })
        
        if (response.code === 20000) {
          this.driftBookList = response.data.records || []
          this.total = response.data.total || 0
        } else {
          this.$message.error('获取书籍列表失败')
        }
      } catch (error) {
        console.error('获取书籍列表错误:', error)
        this.$message.error('获取书籍列表失败：' + error.message)
      }
    },
    // 获取当前用户信息
    async fetchCurrentUser() {
      // 直接从本地存储获取用户信息（登录时已保存）
      const user = localStorage.getItem('user')
      if (user) {
        this.currentUser = JSON.parse(user)
      } else {
        this.$message.warning('未找到用户信息，请重新登录')
        this.$router.push('/login')
      }
    },
    // 搜索书籍
    searchBook() {
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

/* 借阅信息提示 */
.borrowed-info {
  display: inline-block;
  margin: 0 5px;
}

/* 捐赠信息提示 */
.donated-info {
  display: inline-block;
  margin: 0 5px;
}

/* 捐赠表单容器 */
.donate-form-container {
  margin-top: 20px;
}

.donate-form {
  padding: 10px 0;
}

/* 按钮样式 */
.notes-btn {
  margin-right: 5px;
}

.trail-btn {
  margin-right: 5px;
}

.status-btn {
  margin-left: 5px;
}
</style>
