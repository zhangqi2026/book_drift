<template>
  <div class="admin-announcements">
    <div class="page-header">
      <h2>公告管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">发布公告</el-button>
    </div>
    
    <!-- 公告列表 -->
    <el-table
      :data="announcementList"
      border
      stripe
      class="announcement-list-table"
      :empty-text="`暂无公告`"
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
        prop="title"
        label="公告标题"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column
        prop="publisherName"
        label="发布人"
        width="120"
      />
      <el-table-column
        label="状态"
        width="100"
      >
        <template slot-scope="scope">
          <el-tag :type="scope.row.isPublished === 1 ? 'success' : 'info'">
            {{ scope.row.isPublished === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="publishTime"
        label="发布时间"
        width="180"
      >
        <template slot-scope="scope">
          {{ scope.row.publishTime ? formatDateTime(scope.row.publishTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="300"
        fixed="right"
      >
        <template slot-scope="scope">
          <div class="action-buttons">
            <el-button type="info" size="small" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
            <el-button type="primary" size="small" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[5, 10, 20]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      class="pagination-box"
    >
    </el-pagination>
    
    <!-- 新增/编辑公告弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="announcementForm" :rules="announcementRules" ref="announcementForm" label-width="80px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" maxlength="200" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="announcementForm.content" type="textarea" :rows="10" placeholder="请输入公告内容"></el-input>
        </el-form-item>
        <el-form-item label="发布状态" prop="isPublished">
          <el-radio-group v-model="announcementForm.isPublished">
            <el-radio :label="1">立即发布</el-radio>
            <el-radio :label="0">保存草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </span>
    </el-dialog>
    
    <!-- 查看公告详情弹窗 -->
    <el-dialog
      title="公告详情"
      :visible.sync="viewDialogVisible"
      width="700px"
    >
      <div class="announcement-detail">
        <h3>{{ viewAnnouncement.title }}</h3>
        <div class="detail-meta">
          <span>发布人：{{ viewAnnouncement.publisherName || '-' }}</span>
          <span>发布时间：{{ viewAnnouncement.publishTime ? formatDateTime(viewAnnouncement.publishTime) : '-' }}</span>
        </div>
        <div class="detail-content" v-html="viewAnnouncement.content"></div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="viewDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AdminAnnouncements',
  props: {
    currentUser: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      announcementList: [],
      currentPage: 1,
      pageSize: 5,
      total: 0,
      loading: false,
      dialogVisible: false,
      viewDialogVisible: false,
      dialogTitle: '发布公告',
      announcementForm: {
        id: null,
        title: '',
        content: '',
        publisherId: null,
        publisherName: '',
        isPublished: 1
      },
      viewAnnouncement: {},
      announcementRules: {
        title: [
          { required: true, message: '请输入公告标题', trigger: 'blur' },
          { min: 1, max: 200, message: '长度在 1 到 200 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入公告内容', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.fetchAnnouncementList()
  },
  methods: {
    async fetchAnnouncementList() {
      try {
        this.loading = true
        const response = await this.$axios.post(`/announcement/condition/${this.pageSize}/${this.currentPage}`)
        
        if (response.code === 20000) {
          this.announcementList = response.data.records || []
          this.total = response.data.total || 0
        }
      } catch (error) {
        this.$message.error('获取公告列表失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    handleCreate() {
      this.dialogTitle = '发布公告'
      this.announcementForm = {
        id: null,
        title: '',
        content: '',
        publisherId: this.currentUser.id,
        publisherName: this.currentUser.name,
        isPublished: 1
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑公告'
      this.announcementForm = {
        id: row.id,
        title: row.title,
        content: row.content,
        publisherId: row.publisherId,
        publisherName: row.publisherName,
        isPublished: row.isPublished
      }
      this.dialogVisible = true
    },
    handleView(row) {
      this.viewAnnouncement = { ...row }
      this.viewDialogVisible = true
    },
    async handleSubmit() {
      this.$refs.announcementForm.validate(async (valid) => {
        if (valid) {
          try {
            let response
            if (this.announcementForm.id) {
              response = await this.$axios.put('/announcement', this.announcementForm)
            } else {
              response = await this.$axios.post('/announcement', this.announcementForm)
            }
            
            if (response.code === 20000) {
              this.$message.success(this.announcementForm.id ? '编辑成功' : '发布成功')
              this.dialogVisible = false
              this.fetchAnnouncementList()
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
      this.$confirm(`确定要删除公告「${row.title}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.delete(`/announcement/${row.id}`)
          
          if (response.code === 20000) {
            this.$message.success('删除成功')
            this.fetchAnnouncementList()
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
      this.fetchAnnouncementList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchAnnouncementList()
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
.admin-announcements {
  padding: 20px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
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

.announcement-list-table {
  margin-bottom: 20px;
}

.pagination-box {
  text-align: right;
}

.announcement-detail h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.detail-meta {
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
}

.detail-meta span {
  margin-right: 20px;
}

.detail-content {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}
</style>
