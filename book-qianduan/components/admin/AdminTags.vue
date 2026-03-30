<template>
  <div class="admin-tags">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-box slide-in">
        <h1 class="welcome-title">
          <span class="title-glow">标签管理</span>
        </h1>
        <p class="welcome-subtitle">管理书籍分类标签</p>
      </div>
    </div>
    
    <!-- 标签列表 -->
    <div class="section-box">
      <div class="section-header">
        <h3 class="section-title">标签列表</h3>
        <el-button type="primary" icon="el-icon-plus" @click="handleCreate" class="action-btn">新增标签</el-button>
      </div>
      <el-table
        :data="tagList"
        border
        stripe
        class="tag-list-table custom-table"
        :empty-text="`暂无标签`"
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
          prop="tagName"
          label="标签名称"
          width="150"
        />
        <el-table-column
          prop="description"
          label="描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="bookCount"
          label="关联书籍数"
          width="120"
        />
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
        >
          <template slot-scope="scope">
            {{ scope.row.createTime ? formatDateTime(scope.row.createTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="220"
          fixed="right"
        >
          <template slot-scope="scope">
            <div class="action-buttons">
              <el-button type="primary" size="small" icon="el-icon-edit" @click="handleEdit(scope.row)" class="action-btn">编辑</el-button>
              <el-button type="danger" size="small" icon="el-icon-delete" @click="handleDelete(scope.row)" class="action-btn">删除</el-button>
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
    
    <!-- 新增/编辑标签弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
      class="custom-dialog"
    >
      <el-form :model="tagForm" :rules="tagRules" ref="tagForm" label-width="80px">
        <el-form-item label="标签名称" prop="tagName">
          <el-input v-model="tagForm.tagName" placeholder="请输入标签名称" maxlength="50" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="tagForm.description" type="textarea" :rows="4" placeholder="请输入标签描述" maxlength="200" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" class="action-btn">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" class="action-btn">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AdminTags',
  data() {
    return {
      tagList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      dialogVisible: false,
      dialogTitle: '新增标签',
      tagForm: {
        id: null,
        tagName: '',
        description: ''
      },
      tagRules: {
        tagName: [
          { required: true, message: '请输入标签名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.fetchTagList()
  },
  methods: {
    async fetchTagList() {
      try {
        this.loading = true
        const response = await this.$axios.post(`/bookTag/condition/${this.pageSize}/${this.currentPage}`)
        
        if (response.code === 20000) {
          this.tagList = response.data.records || []
          this.total = response.data.total || 0
        }
      } catch (error) {
        this.$message.error('获取标签列表失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    handleCreate() {
      this.dialogTitle = '新增标签'
      this.tagForm = {
        id: null,
        tagName: '',
        description: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑标签'
      this.tagForm = {
        id: row.id,
        tagName: row.tagName,
        description: row.description
      }
      this.dialogVisible = true
    },
    async handleSubmit() {
      this.$refs.tagForm.validate(async (valid) => {
        if (valid) {
          try {
            let response
            if (this.tagForm.id) {
              response = await this.$axios.post('/bookTag/update', this.tagForm)
            } else {
              response = await this.$axios.post('/bookTag/add', this.tagForm)
            }
            
            if (response.code === 20000) {
              this.$message.success(this.tagForm.id ? '编辑成功' : '新增成功')
              this.dialogVisible = false
              this.fetchTagList()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          } catch (error) {
            this.$message.error('操作失败：' + (error.response?.data?.message || error.message))
          }
        }
      })
    },
    handleDelete(row) {
      this.$confirm(`确定要删除标签「${row.tagName}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.post('/bookTag/delete', null, {
            params: { id: row.id }
          })
          
          if (response.code === 20000) {
            this.$message.success('删除成功')
            this.fetchTagList()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          this.$message.error('删除失败：' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {})
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchTagList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchTagList()
    },
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
    }
  }
}
</script>

<style scoped>
.admin-tags {
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

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
}

/* 表格样式 */
.tag-list-table {
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
