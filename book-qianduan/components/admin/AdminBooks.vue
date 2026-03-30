<template>
  <div class="admin-books">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-box slide-in">
        <h1 class="welcome-title">
          <span class="title-glow">书籍大厅管理</span>
        </h1>
        <p class="welcome-subtitle">管理系统中的所有漂流书籍</p>
      </div>
    </div>
    
    <!-- 搜索栏 -->
    <div class="section-box search-section">
      <div class="section-header">
        <h3 class="section-title">搜索书籍</h3>
      </div>
      <div class="search-bar-wrapper">
        <div class="search-box">
          <i class="el-icon-search search-icon"></i>
          <el-input
            v-model="searchKeyword"
            placeholder="请输入书籍名称、作者或标签搜索..."
            class="search-input"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-button 
            v-if="searchKeyword" 
            icon="el-icon-refresh-left" 
            @click="resetSearch"
            class="reset-btn"
          ></el-button>
          <el-button 
            type="primary" 
            @click="handleSearch"
            class="search-btn"
          >
            <i class="el-icon-search"></i>
            搜索
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 书籍列表 -->
    <div class="section-box">
      <div class="section-header">
        <h3 class="section-title">书籍列表</h3>
      </div>
      <el-table
        :data="bookList"
        border
        stripe
        class="book-list-table custom-table"
        :empty-text="`暂无书籍`"
        v-loading="loading"
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
          prop="donorName"
          label="捐赠人"
          width="100"
        >
          <template slot-scope="scope">
            {{ scope.row.donorName || '匿名捐赠者' }}
          </template>
        </el-table-column>
        <el-table-column
          label="标签"
          min-width="200"
        >
          <template slot-scope="scope">
            <el-tag
              v-for="tag in scope.row.tags"
              :key="tag.id"
              size="small"
              class="custom-tag"
              style="margin-right: 5px; margin-bottom: 5px;"
            >
              {{ tag.tagName }}
            </el-tag>
            <span v-if="!scope.row.tags || scope.row.tags.length === 0" style="color: #8a9a8a;">暂无标签</span>
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="100"
        >
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.bookStatus)" class="custom-tag">
              {{ getStatusText(scope.row.bookStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="480"
        >
          <template slot-scope="scope">
            <div style="display: flex; gap: 5px; flex-wrap: wrap;">
              <el-button
                type="primary"
                size="mini"
                @click="viewClaimRecords(scope.row)"
                class="action-btn"
              >
                查看轨迹
              </el-button>
              <el-button
                type="success"
                size="mini"
                @click="viewBookNotes(scope.row)"
                class="action-btn"
              >
                查看读书笔记
              </el-button>
              <el-button
                type="info"
                size="mini"
                @click="bindTags(scope.row)"
                class="action-btn"
              >
                绑定标签
              </el-button>
              <el-button
                type="warning"
                size="mini"
                @click="editBook(scope.row)"
                class="action-btn"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="mini"
                @click="deleteBook(scope.row.id)"
                class="action-btn"
              >
                删除
              </el-button>
            </div>
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
        class="pagination-box custom-pagination"
      >
      </el-pagination>
    </div>
    
    <!-- 编辑书籍弹窗 -->
    <el-dialog
      title="编辑书籍"
      :visible.sync="bookDialogVisible"
      width="500px"
      @close="closeBookDialog"
      class="custom-dialog"
    >
      <el-form :model="bookForm" :rules="bookRules" ref="bookFormRef" label-width="100px">
        <el-form-item label="书籍名称" prop="bookName">
          <el-input v-model="bookForm.bookName" placeholder="请输入书籍名称" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="书籍二维码" prop="bookQrcode">
          <el-input v-model="bookForm.bookQrcode" placeholder="请输入书籍二维码" disabled />
        </el-form-item>
        <el-form-item label="状态" prop="bookStatus">
          <el-select v-model="bookForm.bookStatus" placeholder="请选择状态">
            <el-option label="待认领" :value="1"></el-option>
            <el-option label="已认领" :value="2"></el-option>
            <el-option label="已归还" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveBook" class="action-btn">保存</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 查看轨迹弹窗 -->
    <el-dialog
      title="书籍漂流轨迹"
      :visible.sync="recordDialogVisible"
      width="900px"
      @close="closeRecordDialog"
      class="custom-dialog"
    >
      <div v-if="currentBook">
        <div class="book-info-header" style="margin-bottom: 20px; padding: 15px; background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15)); border-radius: 12px;">
          <h4 style="margin: 0 0 10px 0; color: #5a6a5a; font-weight: 700;">《{{ currentBook.bookName || '未知书籍' }}》</h4>
          <p style="margin: 5px 0; color: #6a7a6a;">作者：{{ currentBook.author || '未知' }}</p>
        </div>
        
        <el-table
          :data="claimRecords"
          border
          stripe
          :empty-text="'暂无借阅记录'"
          class="custom-table"
        >
          <el-table-column
            prop="claimerName"
            label="借阅人"
            width="120"
          >
            <template slot-scope="scope">
              {{ scope.row.claimerName || '-' }}
            </template>
          </el-table-column>
          <el-table-column
            prop="claimTime"
            label="借阅时间"
            width="180"
          >
            <template slot-scope="scope">
              {{ scope.row.claimTime ? formatDateTime(scope.row.claimTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column
            prop="returnTime"
            label="归还时间"
            width="180"
          >
            <template slot-scope="scope">
              {{ scope.row.returnTime ? formatDateTime(scope.row.returnTime) : '-' }}
            </template>
          </el-table-column>
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

    <!-- 查看读书笔记弹窗 -->
    <el-dialog
      title="读书笔记"
      :visible.sync="notesDialogVisible"
      width="900px"
      @close="closeNotesDialog"
      class="custom-dialog"
    >
      <el-table :data="bookNotes" border stripe class="custom-table">
        <el-table-column
          prop="userName"
          label="笔记作者"
          width="120"
        />
        <el-table-column
          prop="noteContent"
          label="笔记内容"
          min-width="300"
          show-overflow-tooltip
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
          width="180"
          align="center"
          fixed="right"
        >
          <template slot-scope="scope">
            <el-button
              type="warning"
              size="mini"
              @click="editNote(scope.row)"
              class="action-btn"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="mini"
              @click="deleteNote(scope.row.id)"
              class="action-btn"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        @current-change="handleNotesPageChange"
        :current-page="notesCurrentPage"
        :page-size="notesPageSize"
        layout="total, prev, pager, next, jumper"
        :total="notesTotal"
        style="text-align: right; margin-top: 20px;"
        class="custom-pagination"
      />
    </el-dialog>

    <!-- 编辑笔记弹窗 -->
    <el-dialog
      title="编辑读书笔记"
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
    
    <!-- 绑定标签弹窗 -->
    <el-dialog
      :title="`为《${currentBindBook?.bookName || ''}》绑定标签`"
      :visible.sync="bindTagsDialogVisible"
      width="600px"
      @close="closeBindTagsDialog"
      class="custom-dialog"
    >
      <div class="tags-select-container">
        <el-checkbox-group v-model="selectedTagIds">
          <el-checkbox
            v-for="tag in allTags"
            :key="tag.id"
            :label="tag.id"
            style="margin: 10px;"
          >
            {{ tag.tagName }}
            <span v-if="tag.description" style="color: #909399; font-size: 12px;">- {{ tag.description }}</span>
          </el-checkbox>
        </el-checkbox-group>
        <div v-if="allTags.length === 0" style="text-align: center; padding: 40px; color: #8a9a8a;">
          暂无可用标签，请先在标签管理中创建标签
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="bindTagsDialogVisible = false" class="action-btn">取 消</el-button>
        <el-button type="primary" @click="submitBindTags" :loading="bindingTags" class="action-btn">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AdminBooks',
  data() {
    return {
      bookList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchKeyword: '',
      loading: false,
      bookDialogVisible: false,
      bookForm: {
        id: null,
        bookName: '',
        author: '',
        bookQrcode: '',
        bookStatus: 1
      },
      bookRules: {
        bookName: [{ required: true, message: '请输入书籍名称', trigger: 'blur' }],
        author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
        bookQrcode: [{ required: true, message: '请输入书籍二维码', trigger: 'blur' }],
        bookStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
      },
      bookFormRef: null,
      // 轨迹记录相关
      recordDialogVisible: false,
      currentBook: null,
      claimRecords: [],
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
      noteFormDialogVisible: false,
      noteFormMode: 'edit',
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
      // 标签绑定相关
      bindTagsDialogVisible: false,
      currentBindBook: null,
      allTags: [],
      selectedTagIds: [],
      bindingTags: false
    }
  },
  mounted() {
    this.fetchBookList()
    this.fetchAllTags()
  },
  methods: {
    async fetchBookList() {
      try {
        this.loading = true
        const response = await this.$axios.post(`/bookInfo/condition/${this.pageSize}/${this.currentPage}`, null, {
          params: {
            bookName: this.searchKeyword
          }
        })
        
        if (response.code === 20000) {
          this.bookList = response.data.records || []
          this.total = response.data.total || 0
        }
      } catch (error) {
        this.$message.error('获取书籍列表失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchBookList()
    },
    resetSearch() {
      this.searchKeyword = ''
      this.currentPage = 1
      this.fetchBookList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchBookList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchBookList()
    },
    editBook(book) {
      this.bookForm = {
        id: book.id,
        bookName: book.bookName,
        author: book.author,
        bookQrcode: book.bookQrcode,
        bookStatus: book.bookStatus
      }
      this.bookDialogVisible = true
    },
    async saveBook() {
      this.$refs.bookFormRef.validate(async valid => {
        if (!valid) return
        
        try {
          this.loading = true
          const response = await this.$axios.put(`/bookInfo`, this.bookForm)
          
          if (response.code === 20000) {
            this.$message.success('书籍更新成功')
            this.bookDialogVisible = false
            this.fetchBookList()
          } else {
            this.$message.error(response.message || '更新失败')
          }
        } catch (error) {
          this.$message.error('操作失败：' + (error.response?.data?.message || error.message))
        } finally {
          this.loading = false
        }
      })
    },
    async deleteBook(bookId) {
      const book = this.bookList.find(b => b.id === bookId)
      
      // 如果书籍正在被借阅，禁止删除
      if (book && book.bookStatus === 2) {
        this.$message.warning('该书籍正在被借阅，无法删除')
        return
      }
      
      this.$confirm('确定要删除这本书吗？删除后将同时清除相关的借阅记录、读书笔记等数据，此操作不可恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          this.loading = true
          const response = await this.$axios.delete(`/bookInfo/${bookId}`)
          
          if (response.code === 20000) {
            this.$message.success('书籍删除成功')
            this.fetchBookList()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          this.$message.error('删除失败：' + (error.response?.data?.message || error.message))
        } finally {
          this.loading = false
        }
      }).catch(() => {})
    },
    closeBookDialog() {
      this.bookDialogVisible = false
      this.bookForm = {
        id: null,
        bookName: '',
        author: '',
        bookQrcode: '',
        bookStatus: 1
      }
    },
    // 查看书籍轨迹
    async viewClaimRecords(book) {
      try {
        this.currentBook = book
        const response = await this.$axios.get(`/bookClaimRecord/history/${book.id}`, {
          params: { current: this.trailCurrentPage, size: this.trailPageSize }
        })
        
        if (response.code === 20000 && response.data) {
          this.claimRecords = response.data.records || []
          this.trailTotal = response.data.total || 0
          this.recordDialogVisible = true
        } else {
          this.$message.error('获取轨迹记录失败')
        }
      } catch (error) {
        this.$message.error('获取轨迹记录失败：' + (error.response?.data?.message || error.message))
      }
    },
    // 查看读书笔记
    async viewBookNotes(book) {
      try {
        this.currentNoteBook = book
        this.notesCurrentPage = 1
        const response = await this.$axios.post(`/bookNote/condition/${this.notesPageSize}/${this.notesCurrentPage}`, null, {
          params: { bookId: book.id }
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
    // 处理轨迹分页
    async handleTrailPageChange(val) {
      this.trailCurrentPage = val
      // 重新加载当前书籍的轨迹记录
      if (this.currentBook && this.currentBook.id) {
        await this.viewClaimRecords(this.currentBook)
      }
    },
    // 处理笔记分页
    async handleNotesPageChange(val) {
      this.notesCurrentPage = val
      if (this.currentNoteBook && this.currentNoteBook.id) {
        await this.viewBookNotes(this.currentNoteBook)
      }
    },
    // 关闭轨迹弹窗
    closeRecordDialog() {
      this.recordDialogVisible = false
      this.currentBook = null
      this.claimRecords = []
      this.trailCurrentPage = 1
      this.trailTotal = 0
    },
    // 关闭笔记弹窗
    closeNotesDialog() {
      this.notesDialogVisible = false
      this.currentNoteBook = null
      this.bookNotes = []
      this.notesCurrentPage = 1
      this.notesTotal = 0
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
          await this.viewBookNotes(this.currentNoteBook)
        } else {
          this.$message.error('删除失败：' + response.message)
        }
      } catch (error) {
        if (error !== 'cancel' && error.message !== 'cancel') {
          this.$message.error('删除失败：' + error.message)
        }
      }
    },
    // 提交笔记表单
    async submitNoteForm() {
      if (!this.$refs.noteFormRef) return
      
      await this.$refs.noteFormRef.validate(async (valid) => {
        if (!valid) return
        
        this.submitting = true
        try {
          const response = await this.$axios.put('/bookNote', {
            id: this.noteForm.id,
            noteContent: this.noteForm.noteContent
          })
          
          if (response.code === 20000) {
            this.$message.success('修改成功！')
            this.noteFormDialogVisible = false
            await this.viewBookNotes(this.currentNoteBook)
          } else {
            this.$message.error('修改失败：' + response.message)
          }
        } catch (error) {
          this.$message.error('修改失败：' + error.message)
        } finally {
          this.submitting = false
        }
      })
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
    getStatusType(status) {
      switch (status) {
        case 1: return 'warning'
        case 2: return 'success'
        case 3: return 'info'
        default: return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 1: return '待认领'
        case 2: return '已认领'
        case 3: return '已归还'
        default: return '未知状态'
      }
    },
    // 格式化时间
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
    // 绑定标签
    bindTags(book) {
      this.currentBindBook = book
      this.selectedTagIds = book.tags ? book.tags.map(t => t.id) : []
      this.bindTagsDialogVisible = true
    },
    // 提交标签绑定
    async submitBindTags() {
      this.bindingTags = true
      try {
        const response = await this.$axios.post('/bookTag/bindBookTags', null, {
          params: {
            bookId: this.currentBindBook.id,
            tagIds: this.selectedTagIds.join(',')
          }
        })
        
        if (response.code === 20000) {
          this.$message.success('标签绑定成功')
          this.bindTagsDialogVisible = false
          this.fetchBookList()
        } else {
          this.$message.error(response.message || '标签绑定失败')
        }
      } catch (error) {
        this.$message.error('标签绑定失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.bindingTags = false
      }
    },
    // 关闭标签绑定弹窗
    closeBindTagsDialog() {
      this.bindTagsDialogVisible = false
      this.currentBindBook = null
      this.selectedTagIds = []
    }
  }
}
</script>

<style scoped>
.admin-books {
  position: relative;
}

.slide-in {
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 20px;
}

.welcome-box {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(171, 240, 209, 0.4);
  box-shadow: 5px 3px 10px rgba(0, 0, 0, 0.07);
  padding: 20px 28px;
  text-align: center;
  position: relative;
}

.welcome-box::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #abf0d1, #d4eea7, #fef1d1, #abf0d1);
  border-radius: 20px;
  z-index: -1;
  background-size: 400% 400%;
  animation: borderGlow 4s ease infinite;
  opacity: 0.5;
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

.welcome-title {
  font-size: 24px;
  font-weight: 800;
  margin: 0 0 6px 0;
}

.title-glow {
  background: linear-gradient(135deg, #6b9a8a 0%, #7a9d5a 50%, #c4a77a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 14px;
  color: #8a9a8a;
  margin: 0 0 15px 0;
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

/* 搜索区域 */
.search-section {
  padding: 20px 24px;
}

.search-bar-wrapper {
  width: 100%;
}

.search-box {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border: 2px solid rgba(171, 240, 209, 0.4);
  border-radius: 16px;
  padding: 8px 16px;
  gap: 12px;
  transition: all 0.3s ease;
}

.search-box:focus-within {
  border-color: rgba(171, 240, 209, 0.8);
  box-shadow: 0 0 0 4px rgba(171, 240, 209, 0.15);
}

.search-icon {
  font-size: 20px;
  color: #6a9a7a;
}

.search-input {
  flex: 1;
}

.search-input >>> .el-input__inner {
  border: none;
  background: transparent;
  font-size: 15px;
  color: #5a6a5a;
  height: 40px;
  padding: 0 12px;
}

.search-input >>> .el-input__inner::placeholder {
  color: #9aaba;
}

.search-input >>> .el-input__inner:focus {
  background: transparent;
  border-color: transparent;
  box-shadow: none;
}

.reset-btn {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(171, 240, 209, 0.4);
  color: #6a9a7a;
  border-radius: 12px;
  padding: 0 16px;
  height: 40px;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: rgba(171, 240, 209, 0.2);
  color: #4a6a5a;
  transform: translateY(-1px);
}

.search-btn {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border: none;
  color: #4a6a5a;
  border-radius: 12px;
  padding: 0 24px;
  height: 40px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(171, 240, 209, 0.4);
}

/* 表格样式 */
.book-list-table {
  margin-bottom: 20px;
}

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
.pagination-box {
  text-align: right;
}

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

/* 按钮样式 */
.action-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

/* 标签样式 */
.custom-tag {
  border-radius: 8px;
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
</style>
