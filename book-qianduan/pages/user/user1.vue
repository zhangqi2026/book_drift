<template>
  <div class="book-drift-container">
    <!-- 粒子背景 -->
    <div class="particles-bg">
      <div v-for="i in 30" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    
    <!-- 装饰性背景元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="grid-lines"></div>
    </div>
    
    <div class="content-wrapper">
      <!-- 页面标题 -->
      <div class="page-title-section">
        <h2 class="page-title">校园闲置书籍漂流管理系统</h2>
      </div>

      <!-- 搜索栏 + 标签筛选 + 分页控制区 -->
      <div class="search-pagination-bar">
        <!-- 书籍搜索和标签筛选 -->
        <div class="search-tags-box">
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
          <div class="tags-filter-box">
            <span class="tags-label">标签筛选：</span>
            <el-select
              v-model="selectedTagIds"
              multiple
              placeholder="选择标签筛选"
              style="width: 300px;"
              @change="handleTagFilterChange"
            >
              <el-option
                v-for="tag in allTags"
                :key="tag.id"
                :label="tag.tagName"
                :value="tag.id"
              >
                <span>{{ tag.tagName }}</span>
                <span v-if="tag.description" style="color: #909399; font-size: 12px; margin-left: 8px;">- {{ tag.description }}</span>
              </el-option>
            </el-select>
          </div>
        </div>

        <!-- 分页控制 -->
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[4, 8, 12]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          class="pagination-box custom-pagination"
        >
        </el-pagination>
      </div>

      <!-- 漂流书籍卡片列表 -->
      <div class="book-card-list">
        <el-row :gutter="24">
          <el-col 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6" 
            v-for="book in driftBookList" 
            :key="book.id"
          >
            <div class="book-card">
              <div class="book-cover">
                <i class="el-icon-notebook-2"></i>
              </div>
              <div class="book-info">
                <h3 class="book-title">{{ book.bookName }}</h3>
                <div class="book-tags">
                  <el-tag
                    v-for="tag in book.tags"
                    :key="tag.id"
                    size="mini"
                    class="book-tag"
                  >
                    {{ tag.tagName }}
                  </el-tag>
                </div>
                <p class="book-author">作者：{{ book.author }}</p>
                <div class="book-status-section">
                  <el-tag :type="getStatusTagType(book.bookStatus)" size="small" class="custom-tag">
                    {{ getStatusText(book.bookStatus) }}
                  </el-tag>
                  <!-- 特殊状态提示 -->
                  <span v-if="book.donorId === currentUser.id" class="special-tag">
                    <el-tag type="info" size="small" class="custom-tag">我捐赠的</el-tag>
                  </span>
                  <span v-if="book.bookStatus === 2 && book.currentHolderId !== currentUser.id" class="special-tag">
                    <el-tag type="warning" size="small" class="custom-tag">已被他人借走</el-tag>
                  </span>
                </div>
                <div class="book-actions">
                  <!-- 归还按钮（当前用户是持有人） -->
                  <el-button
                    v-if="book.bookStatus === 2 && book.currentHolderId === currentUser.id"
                    type="success"
                    size="small"
                    @click="returnBook(book.id)"
                    class="action-btn"
                  >
                    归还
                  </el-button>
                  <!-- 认领按钮（待认领或已归还，且不是自己捐赠的） -->
                  <el-button
                    v-if="(book.bookStatus === 1 || book.bookStatus === 3) && book.donorId !== currentUser.id"
                    type="primary"
                    size="small"
                    @click="addDriftCount(book.id)"
                    class="action-btn"
                  >
                    {{ book.bookStatus === 1 ? '认领' : '认领（已归还）' }}
                  </el-button>
                  <!-- 读书笔记按钮 -->
                  <el-button
                    type="warning"
                    size="small"
                    @click="viewBookNotes(book)"
                    class="action-btn"
                  >
                    读书笔记
                  </el-button>
                  <!-- 查看轨迹按钮 -->
                  <el-button
                    type="info"
                    size="small"
                    @click="viewClaimRecords(book)"
                    class="action-btn"
                  >
                    查看轨迹
                  </el-button>
                  <!-- 仅管理员显示上下架按钮 -->
                  <el-button
                    v-if="currentUser.role === 1"
                    type="danger"
                    size="small"
                    @click="changeBookStatus(book.id, book.bookStatus)"
                    class="action-btn"
                  >
                    {{ book.bookStatus === 1 ? '下架' : (book.bookStatus === 2 ? '归还' : '上架') }}
                  </el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        <div v-if="driftBookList.length === 0" class="empty-data">暂无符合条件的书籍</div>
      </div>

      <!-- 书籍轨迹弹窗 -->
      <el-dialog
        title="书籍漂流轨迹"
        :visible.sync="recordDialogVisible"
        width="900px"
        @close="closeDialog"
        class="custom-dialog"
      >
        <div v-if="currentBook">
          <div class="book-info-header" style="margin-bottom: 20px; padding: 15px; background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15)); border-radius: 12px;">
            <h4 style="margin: 0 0 10px 0; color: #5a6a5a; font-weight: 700;">《{{ currentBook.bookName || currentBook.book_name || '未知书籍' }}》</h4>
            <p style="margin: 5px 0; color: #6a7a6a;">作者：{{ currentBook.author || '未知' }}</p>
          </div>
          
          <el-table
            :data="currentBook.claimRecords"
            border
            stripe
            :empty-text="'暂无借阅记录'"
            class="custom-table"
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
                <el-tag :type="scope.row.returnTime ? 'success' : 'warning'" class="custom-tag">
                  {{ scope.row.returnTime ? '已归还' : '借阅中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              label="是否超期"
              width="100"
            >
              <template slot-scope="scope">
                <el-tag :type="scope.row.isOverdue === 1 ? 'danger' : 'success'" class="custom-tag">
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
            class="custom-pagination"
          />
        </div>
      </el-dialog>

      <!-- 读书笔记弹窗 -->
      <el-dialog
        title="读书笔记"
        :visible.sync="notesDialogVisible"
        width="95%"
        @close="closeNotesDialog"
        class="custom-dialog"
      >
        <div v-if="currentNoteBook">
          <div class="book-info-header" style="margin-bottom: 20px; padding: 15px; background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15)); border-radius: 12px;">
            <h4 style="margin: 0 0 10px 0; color: #5a6a5a; font-weight: 700;">《{{ currentNoteBook.bookName || currentNoteBook.book_name || '未知书籍' }}》</h4>
            <p style="margin: 5px 0; color: #6a7a6a;">作者：{{ currentNoteBook.author || '未知' }}</p>
          </div>
          
          <!-- 写笔记按钮 -->
          <div style="margin-bottom: 15px;">
            <el-button
              type="primary"
              @click="showAddNoteDialog"
              icon="el-icon-edit"
              class="action-btn"
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
            class="custom-table"
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
                    class="action-btn"
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
                      class="action-btn"
                      style="padding: 7px 10px;"
                    >
                      编辑
                    </el-button>
                    <el-button
                      type="danger"
                      size="mini"
                      @click="deleteNote(scope.row.id)"
                      class="action-btn"
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
            class="custom-pagination"
          />
        </div>
      </el-dialog>

      <!-- 添加/编辑笔记弹窗 -->
      <el-dialog
        :title="noteFormMode === 'add' ? '添加读书笔记' : '编辑读书笔记'"
        :visible.sync="noteFormDialogVisible"
        width="600px"
        @close="closeNoteFormDialog"
        class="custom-dialog"
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
          <el-button @click="noteFormDialogVisible = false" class="action-btn">取 消</el-button>
          <el-button type="primary" @click="submitNoteForm" :loading="submitting" class="action-btn">确 定</el-button>
        </div>
      </el-dialog>

      <!-- 捐赠书籍表单（仅普通用户显示） -->
      <div v-if="currentUser.role === 2" class="donate-section">
        <div class="section-box">
          <div class="section-header">
            <h3 class="section-title">捐赠闲置书籍</h3>
          </div>
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
            <el-form-item label="选择标签">
              <el-select
                v-model="donateBook.tagIds"
                multiple
                placeholder="请选择书籍标签"
                style="width: 100%;"
              >
                <el-option
                  v-for="tag in allTags"
                  :key="tag.id"
                  :label="tag.tagName"
                  :value="tag.id"
                >
                  <span>{{ tag.tagName }}</span>
                  <span v-if="tag.description" style="color: #909399; font-size: 12px; margin-left: 8px;">- {{ tag.description }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="donateBookSubmit" class="action-btn donate-btn">确认捐赠</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
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
        author: '',
        tagIds: []
      },
      // 捐赠表单校验规则
      donateRules: {
        book_name: [{ required: true, message: '请输入书籍名称', trigger: 'blur' }],
        author: [{ required: true, message: '请输入作者', trigger: 'blur' }]
      },
      donateFormRef: null,
      searchKeyword: '',
      currentPage: 1,
      pageSize: 4,
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
      apiBaseUrl: 'http://localhost:3000/api',
      // 标签相关
      allTags: [],
      selectedTagIds: []
    }
  },
  mounted() {
    this.checkLoginStatus()
    this.initApp()
    this.fetchAllTags()
  },
  methods: {
    getParticleStyle(index) {
      const size = Math.random() * 4 + 2
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
          let message = `${statusMsg}《${book.bookName}》！请在 7 天内归还～`
          if (response.other && response.other.score) {
            message += ` +${response.other.score}分`
          }
          this.$message.success(message)
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
          let message = `《${book.bookName}》归还成功！`
          if (response.other && response.other.score) {
            message += ` +${response.other.score}分`
          }
          this.$message.success(message)
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
            let message = '已取消点赞'
            if (response.other && response.other.score) {
              message += '（笔记作者减少 -' + response.other.score + '分）'
            }
            this.$message.success(message)
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
              let message = '点赞成功！'
              if (response.other && response.other.score) {
                // 这是笔记作者获得的积分，不显示给当前点赞的用户
                // 或者我们可以提示笔记作者会获得积分
                message += '（笔记作者获得 +' + response.other.score + '分）'
              }
              this.$message.success(message)
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
            let message = this.noteFormMode === 'add' ? '添加成功！' : '修改成功！'
            if (this.noteFormMode === 'add' && response.other && response.other.score) {
              message += ` +${response.other.score}分`
            }
            this.$message.success(message)
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
            // 获取新创建书籍的ID
            const bookId = response.data
            
            // 如果选择了标签，绑定标签
            if (this.donateBook.tagIds && this.donateBook.tagIds.length > 0) {
              await this.$axios.post('/bookTag/bindBookTags', null, {
                params: {
                  bookId: bookId,
                  tagIds: this.donateBook.tagIds.join(',')
                }
              })
            }
            
            // 重置表单
            this.$refs.donateFormRef.resetFields()
            this.donateBook.tagIds = []
            
            let message = '捐赠成功！书籍已加入漂流列表~'
            if (response.other && response.other.score) {
              message += ` +${response.other.score}分`
            }
            this.$message.success(message)
            this.fetchBookList()
          } else {
            this.$message.error('捐赠失败：' + response.message)
          }
        } catch (error) {
          this.$message.error('捐赠失败：' + error.message)
        }
      })
    },
    // 获取所有标签
    async fetchAllTags() {
      try {
        const response = await this.$axios.post('/bookTag/listAll')
        if (response.code === 20000) {
          this.allTags = response.data || []
        }
      } catch (error) {
        console.error('获取标签列表失败：', error)
      }
    },
    // 标签筛选变化
    handleTagFilterChange() {
      this.currentPage = 1
      this.fetchBookList()
    },
    // 获取书籍列表（支持标签筛选）
    async fetchBookList() {
      try {
        const params = {
          bookName: this.searchKeyword
        }
        if (this.selectedTagIds && this.selectedTagIds.length > 0) {
          params.tagIds = this.selectedTagIds.join(',')
        }
        const response = await this.$axios.post(`/bookInfo/conditionWithTags/${this.pageSize}/${this.currentPage}`, null, {
          params
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
      this.selectedTagIds = []
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
.book-drift-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #fef9f0 0%, #f5f0e6 50%, #e8f5e2 100%);
  position: relative;
  overflow-x: hidden;
  overflow-y: auto;
}

* {
  box-sizing: border-box;
}

/* 粒子背景 */
.particles-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
  pointer-events: none;
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
    opacity: 0.6;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-100px) rotate(720deg);
    opacity: 0;
  }
}

/* 装饰性背景元素 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border: 1px solid rgba(171, 240, 209, 0.2);
  box-shadow: 0 0 40px rgba(171, 240, 209, 0.15), inset 0 0 40px rgba(171, 240, 209, 0.08);
  animation: float 12s ease-in-out infinite;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg) scale(1);
  }
  50% {
    transform: translateY(-30px) rotate(180deg) scale(1.03);
  }
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(171, 240, 209, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(171, 240, 209, 0.04) 1px, transparent 1px);
  background-size: 60px 60px;
}

.content-wrapper {
  position: relative;
  z-index: 10;
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  overflow-x: hidden;
}

/* 页面标题 */
.page-title-section {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #6b9a8a 0%, #7a9d5a 50%, #c4a77a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 搜索栏布局 */
.search-pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.25), rgba(212, 238, 167, 0.25));
  border-radius: 16px;
  border: 1px solid rgba(171, 240, 209, 0.3);
  flex-wrap: wrap;
  gap: 15px;
}

.search-tags-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tags-filter-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tags-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  font-weight: 500;
}

/* 书籍卡片列表 */
.book-card-list {
  margin-bottom: 20px;
}

.book-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(171, 240, 209, 0.3);
  box-shadow: 5px 2px 10px rgba(0, 0, 0, 0.06);
  transition: all 0.35s ease;
  margin-bottom: 24px;
  overflow: hidden;
}

.book-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
}

.book-cover {
  width: 100%;
  height: 120px;
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.book-cover i {
  font-size: 48px;
  color: #4a6a5a;
}

.book-info {
  text-align: center;
  padding: 16px 18px;
}

.book-title {
  font-size: 16px;
  font-weight: 700;
  color: #5a6a5a;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-tags {
  margin: 10px 0;
  min-height: 24px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
}

.book-tag {
  border-radius: 8px;
  font-weight: 500;
}

.book-author {
  font-size: 13px;
  color: #8a9a8a;
  margin: 5px 0;
  font-weight: 500;
}

.book-status-section {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin: 12px 0;
}

.special-tag {
  display: inline-block;
}

.book-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 15px;
}

.action-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

.custom-tag {
  border-radius: 8px;
  font-weight: 500;
}

/* 区域通用样式 */
.section-box {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(171, 240, 209, 0.3);
  box-shadow: 5px 3px 10px rgba(0, 0, 0, 0.07);
  padding: 20px 24px;
  margin-bottom: 20px;
  transition: all 0.35s ease;
}

.section-box:hover {
  box-shadow: 6px 4px 12px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #5a6a5a;
  margin: 0;
}

.donate-form {
  padding: 10px 0;
}

/* 表格样式 */
.custom-table {
  border-radius: 12px;
  overflow: hidden;
}

.custom-table >>> th {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.2), rgba(212, 238, 167, 0.2));
  color: #5a6a5a;
  font-weight: 600;
  border: none;
}

.custom-table >>> td {
  color: #6a7a6a;
  border-color: rgba(171, 240, 209, 0.15);
}

.custom-table >>> .el-table__row:hover > td {
  background: rgba(171, 240, 209, 0.08);
}

/* 分页器样式 */
.custom-pagination >>> .el-pager li {
  border-radius: 8px;
  margin: 0 4px;
  font-weight: 500;
}

.custom-pagination >>> .el-pager li.active {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  color: #4a6a5a;
}

.custom-pagination >>> .btn-prev,
.custom-pagination >>> .btn-next {
  border-radius: 8px;
}

/* 空数据 */
.empty-data {
  text-align: center;
  padding: 60px 20px;
  color: #8a9a8a;
  font-size: 16px;
  font-weight: 500;
}

/* 弹窗样式 */
.custom-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border-radius: 4px 4px 0 0;
}

.custom-dialog >>> .el-dialog__title {
  color: #5a6a5a;
  font-weight: 700;
}

/* 优化滚动条 */
.book-drift-container::-webkit-scrollbar {
  width: 8px;
}

.book-drift-container::-webkit-scrollbar-track {
  background: rgba(171, 240, 209, 0.1);
  border-radius: 4px;
}

.book-drift-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border-radius: 4px;
}

.book-drift-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #8ce0c0 0%, #c4e897 100%);
}
</style>
