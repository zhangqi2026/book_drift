<template>
  <div class="announcements-container">
    <div class="page-header">
      <h1>公告通知</h1>
      <p>最新公告信息</p>
    </div>
    
    <!-- 公告列表 -->
    <div class="announcement-list" v-loading="loading">
      <el-card 
        v-for="announcement in announcementList" 
        :key="announcement.id" 
        class="announcement-card"
        :class="{ 'unread': !isRead(announcement.id) }"
        shadow="hover"
        @click.native="handleView(announcement)"
      >
        <div class="card-header">
          <h3 class="announcement-title">
            <span v-if="!isRead(announcement.id)" class="unread-dot"></span>
            {{ announcement.title }}
          </h3>
          <el-tag :type="isRead(announcement.id) ? 'info' : 'danger'" size="small">
            {{ isRead(announcement.id) ? '已查看' : '未读' }}
          </el-tag>
        </div>
        <div class="card-meta">
          <span><i class="el-icon-user"></i> {{ announcement.publisherName || '管理员' }}</span>
          <span><i class="el-icon-time"></i> {{ formatDateTime(announcement.publishTime) }}</span>
        </div>
        <div class="card-content">
          {{ announcement.content.length > 150 ? announcement.content.substring(0, 150) + '...' : announcement.content }}
        </div>
        <div class="card-footer">
          <el-button type="text" size="small">查看详情 <i class="el-icon-arrow-right"></i></el-button>
        </div>
      </el-card>
      
      <div v-if="announcementList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-document"></i>
        <p>暂无公告</p>
      </div>
    </div>
    
    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
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
    
    <!-- 查看公告详情弹窗 -->
    <el-dialog
      title="公告详情"
      :visible.sync="viewDialogVisible"
      width="700px"
    >
      <div class="announcement-detail" v-if="viewAnnouncement">
        <h3>{{ viewAnnouncement.title }}</h3>
        <div class="detail-meta">
          <span><i class="el-icon-user"></i> {{ viewAnnouncement.publisherName || '管理员' }}</span>
          <span><i class="el-icon-time"></i> {{ formatDateTime(viewAnnouncement.publishTime) }}</span>
        </div>
        <div class="detail-content">
          {{ viewAnnouncement.content }}
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="viewDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'UserAnnouncements',
  data() {
    return {
      announcementList: [],
      currentPage: 1,
      pageSize: 5,
      total: 0,
      loading: false,
      viewDialogVisible: false,
      viewAnnouncement: null,
      currentUser: null,
      readAnnouncementIds: []
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchAnnouncements()
  },
  methods: {
    getCurrentUser() {
      const user = localStorage.getItem('user')
      if (user) {
        this.currentUser = JSON.parse(user)
        this.fetchReadAnnouncementIds()
      }
    },
    async fetchReadAnnouncementIds() {
      if (!this.currentUser?.id) return
      try {
        const response = await this.$axios.get(`/announcementRead/readIds/${this.currentUser.id}`)
        if (response.code === 20000) {
          this.readAnnouncementIds = response.data || []
        }
      } catch (error) {
        console.error('获取已读公告失败:', error)
      }
    },
    isRead(announcementId) {
      return this.readAnnouncementIds.includes(announcementId)
    },
    async fetchAnnouncements() {
      try {
        this.loading = true
        const response = await this.$axios.post(`/announcement/condition/${this.pageSize}/${this.currentPage}`, null, {
          params: { isPublished: 1 }
        })
        
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
    async handleView(announcement) {
      this.viewAnnouncement = announcement
      this.viewDialogVisible = true
      
      if (!this.isRead(announcement.id) && this.currentUser?.id) {
        try {
          await this.$axios.post('/announcementRead/markRead', null, {
            params: {
              announcementId: announcement.id,
              userId: this.currentUser.id
            }
          })
          this.readAnnouncementIds.push(announcement.id)
        } catch (error) {
          console.error('标记已读失败:', error)
        }
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchAnnouncements()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchAnnouncements()
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
      
      return `${year}-${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.announcements-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.announcement-list {
  margin-bottom: 30px;
}

.announcement-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.announcement-card.unread {
  border-left: 4px solid #F56C6C;
}

.announcement-card:hover {
  transform: translateY(-3px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.announcement-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
  flex: 1;
  display: flex;
  align-items: center;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background-color: #F56C6C;
  border-radius: 50%;
  margin-right: 10px;
  flex-shrink: 0;
}

.card-meta {
  font-size: 14px;
  color: #909399;
  margin-bottom: 15px;
}

.card-meta span {
  margin-right: 20px;
}

.card-meta i {
  margin-right: 5px;
}

.card-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 15px;
  white-space: pre-wrap;
}

.card-footer {
  text-align: right;
  padding-top: 10px;
  border-top: 1px solid #EBEEF5;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  display: block;
}

.pagination-box {
  text-align: center;
}

.announcement-detail h3 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 20px;
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

.detail-meta i {
  margin-right: 5px;
}

.detail-content {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}
</style>
