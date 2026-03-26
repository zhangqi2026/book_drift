<template>
  <div class="admin-tags">
    <div class="page-header">
      <h2>标签管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">新增标签</el-button>
    </div>
    
    <!-- 标签列表 -->
    <el-table
      :data="tagList"
      border
      stripe
      class="tag-list-table"
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
        width="200"
        fixed="right"
      >
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="text" size="small" style="color: #F56C6C;" @click="handleDelete(scope.row)">删除</el-button>
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
    
    <!-- 新增/编辑标签弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
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
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
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
  color: #303133;
}

.tag-list-table {
  margin-bottom: 20px;
}

.pagination-box {
  text-align: right;
}
</style>
