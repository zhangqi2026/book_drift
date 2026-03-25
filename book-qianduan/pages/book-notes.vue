<template>
  <div class="book-notes-container">
    <div class="page-header">
      <h2>读书笔记</h2>
      <el-button @click="goBack" icon="el-icon-back">返回</el-button>
    </div>
    
    <!-- 书籍信息 -->
    <el-card class="book-info-card" v-if="currentBook">
      <div class="book-info">
        <h3>《{{ currentBook.bookName }}》</h3>
        <p>作者：{{ currentBook.author }}</p>
      </div>
    </el-card>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入笔记内容搜索"
        style="width: 300px"
        clearable
        @clear="handleSearch"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
      </el-input>
    </div>
    
    <!-- 笔记列表 -->
    <el-row :gutter="20">
      <el-col :span="24" v-for="note in notesList" :key="note.id">
        <el-card class="note-card">
          <div class="note-header">
            <div class="note-author">
              <i class="el-icon-user"></i>
              <span>{{ note.userName || '未知用户' }}</span>
            </div>
            <div class="note-time">
              <i class="el-icon-time"></i>
              <span>{{ note.createTime }}</span>
            </div>
          </div>
          
          <div class="note-content">
            <p>{{ note.noteContent }}</p>
          </div>
          
          <div class="note-mark" v-if="note.markParagraph">
            <strong>重点标注：</strong>
            <p>{{ note.markParagraph }}</p>
          </div>
          
          <div class="note-footer">
            <el-button 
              type="text" 
              size="small" 
              @click="likeNote(note)"
              :class="{ 'liked': note.isLiked }"
            >
              <i :class="note.isLiked ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
              {{ note.likeCount || 0 }}
            </el-button>
            
            <el-button 
              type="text" 
              size="small" 
              v-if="canDelete(note)"
              @click="deleteNote(note.id)"
            >
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
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
    
    <!-- 添加笔记按钮（仅当前持有者可以添加） -->
    <el-button 
      type="primary" 
      class="add-note-btn"
      v-if="canAddNote"
      @click="showAddNoteDialog = true"
      icon="el-icon-edit"
    >
      写笔记
    </el-button>
    
    <!-- 添加笔记对话框 -->
    <el-dialog
      title="添加读书笔记"
      :visible.sync="showAddNoteDialog"
      width="600px"
    >
      <el-form :model="newNote" label-width="80px">
        <el-form-item label="笔记内容" required>
          <el-input
            v-model="newNote.noteContent"
            type="textarea"
            :rows="6"
            placeholder="请输入你的读书心得..."
          ></el-input>
        </el-form-item>
        <el-form-item label="重点标注">
          <el-input
            v-model="newNote.markParagraph"
            type="textarea"
            :rows="3"
            placeholder="可选：标注重点段落"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <span slot="footer">
        <el-button @click="showAddNoteDialog = false">取消</el-button>
        <el-button type="primary" @click="submitNote" :loading="submitting">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      bookId: null,
      currentBook: null,
      notesList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchKeyword: '',
      showAddNoteDialog: false,
      submitting: false,
      newNote: {
        noteContent: '',
        markParagraph: ''
      },
      currentUser: {
        id: null,
        role: 2
      },
      canAddNote: false // 是否可以添加笔记（仅当前持有者）
    }
  },
  mounted() {
    this.getCurrentUser()
    this.getBookIdFromQuery()
    this.fetchNotes()
    this.fetchBookInfo()
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
    getBookIdFromQuery() {
      this.bookId = this.$route.query.id
      if (!this.bookId) {
        this.$message.error('缺少书籍 ID')
        this.$router.push('/book-trail')
      }
    },
    async fetchBookInfo() {
      try {
        const response = await this.$axios.get(`/bookInfo/${this.bookId}`)
        if (response.code === 20000) {
          this.currentBook = response.data
          // 检查当前用户是否为书籍持有者
          this.canAddNote = this.currentBook.currentHolderId === this.currentUser.id
        }
      } catch (error) {
        console.error('获取书籍信息失败:', error)
      }
    },
    async fetchNotes() {
      try {
        const response = await this.$axios.post(`/bookNote/condition/${this.pageSize}/${this.currentPage}`, null, {
          params: {
            bookId: this.bookId
          }
        })
        
        if (response.code === 20000) {
          this.notesList = response.data.records || []
          this.total = response.data.total || 0
        }
      } catch (error) {
        this.$message.error('获取笔记列表失败：' + (error.response?.data?.message || error.message))
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchNotes()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchNotes()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchNotes()
    },
    async likeNote(note) {
      if (note.isLiked) {
        this.$message.warning('已经点过赞了')
        return
      }
      
      try {
        const response = await this.$axios.put(`/bookNote`, {
          id: note.id,
          likeCount: (note.likeCount || 0) + 1
        })
        
        if (response.code === 20000) {
          note.likeCount = (note.likeCount || 0) + 1
          note.isLiked = true
          this.$message.success('点赞成功')
        }
      } catch (error) {
        this.$message.error('点赞失败')
      }
    },
    async deleteNote(noteId) {
      this.$confirm('确定要删除这篇笔记吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/bookNote/${noteId}`)
          if (response.code === 20000) {
            this.$message.success('删除成功')
            this.fetchNotes()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          this.$message.error('删除失败：' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {})
    },
    async submitNote() {
      if (!this.newNote.noteContent.trim()) {
        this.$message.error('请输入笔记内容')
        return
      }
      
      this.submitting = true
      
      try {
        const response = await this.$axios.post(`/bookNote`, {
          bookId: this.bookId,
          userId: this.currentUser.id,
          noteContent: this.newNote.noteContent,
          markParagraph: this.newNote.markParagraph
        })
        
        if (response.code === 20000) {
          this.$message.success('笔记发布成功')
          this.showAddNoteDialog = false
          this.newNote = {
            noteContent: '',
            markParagraph: ''
          }
          this.fetchNotes()
        } else {
          this.$message.error(response.message || '发布失败')
        }
      } catch (error) {
        this.$message.error('发布失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.submitting = false
      }
    },
    canDelete(note) {
      // 只有笔记作者或管理员可以删除
      return note.userId === this.currentUser.id || this.currentUser.role === 1
    },
    goBack() {
      this.$router.push('/book-trail')
    }
  }
}
</script>

<style scoped>
.book-notes-container {
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

.book-info-card {
  margin-bottom: 20px;
}

.book-info h3 {
  margin: 0 0 10px 0;
  color: #409EFF;
}

.book-info p {
  margin: 0;
  color: #909399;
}

.search-bar {
  margin-bottom: 20px;
}

.note-card {
  margin-bottom: 20px;
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.note-author {
  color: #606266;
  font-size: 14px;
}

.note-author i {
  margin-right: 5px;
}

.note-time {
  color: #909399;
  font-size: 13px;
}

.note-time i {
  margin-right: 5px;
}

.note-content {
  margin-bottom: 15px;
  line-height: 1.8;
  color: #303133;
}

.note-mark {
  background-color: #f6f7f9;
  padding: 10px;
  border-left: 3px solid #409EFF;
  margin-bottom: 15px;
}

.note-mark p {
  margin: 5px 0 0 0;
  color: #606266;
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.liked {
  color: #E6A23C;
}

.pagination-box {
  margin-top: 20px;
  text-align: right;
}

.add-note-btn {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
