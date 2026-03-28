<template>
  <div class="admin-medals">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-box slide-in">
        <h1 class="welcome-title">
          <span class="title-glow">勋章管理</span>
        </h1>
        <p class="welcome-subtitle">查看和管理用户获得的所有勋章</p>
      </div>
    </div>
    
    <!-- 搜索栏 -->
    <div class="section-box search-section">
      <div class="section-header">
        <h3 class="section-title">搜索勋章</h3>
      </div>
      <div class="search-bar">
        <el-input
          v-model="searchUserName"
          placeholder="请输入用户名查询该用户的勋章"
          class="search-input"
          style="width: 350px;"
          clearable
          @clear="handleSearch"
        >
          <el-button slot="append" icon="el-icon-user" @click="handleSearch" class="search-btn"></el-button>
        </el-input>
      </div>
    </div>
    
    <!-- 勋章列表 -->
    <div class="section-box">
      <div class="section-header">
        <h3 class="section-title">勋章列表</h3>
      </div>
      <el-table
        :data="medalList"
        border
        stripe
        class="medal-list-table custom-table"
        :empty-text="`暂无勋章`"
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
          prop="medalName"
          label="勋章名称"
          width="150"
        />
        <el-table-column
          prop="userName"
          label="用户名"
          width="120"
        />
        <el-table-column
          label="勋章类型"
          width="120"
        >
          <template slot-scope="scope">
            <el-tag :type="getMedalTypeTag(scope.row.medalType)" class="custom-tag">
              {{ getMedalTypeText(scope.row.medalType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="requiredCount"
          label="所需数量"
          width="100"
        />
        <el-table-column
          prop="unlockTime"
          label="解锁时间"
          width="180"
        >
          <template slot-scope="scope">
            {{ scope.row.unlockTime ? formatDateTime(scope.row.unlockTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="说明"
          min-width="200"
          show-overflow-tooltip
        />
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
  </div>
</template>

<script>
export default {
  name: 'AdminMedals',
  data() {
    return {
      medalList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchUserName: '',
      loading: false
    }
  },
  mounted() {
    this.fetchMedalList()
  },
  methods: {
    async fetchMedalList() {
      try {
        this.loading = true
        // 调用后端 API 查询勋章列表
        const response = await this.$axios.post(`/userMedal/condition/${this.pageSize}/${this.currentPage}`, null, {
          params: {
            userName: this.searchUserName
          }
        })
        
        if (response.code === 20000) {
          this.medalList = response.data.records || []
          this.total = response.data.total || 0
        }
      } catch (error) {
        this.$message.error('获取勋章列表失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchMedalList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchMedalList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchMedalList()
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
    // 获取勋章类型标签颜色
    getMedalTypeTag(type) {
      const tagMap = {
        1: 'success',
        2: 'primary',
        3: 'warning'
      }
      return tagMap[type] || 'info'
    },
    // 获取勋章类型文本
    getMedalTypeText(type) {
      const textMap = {
        1: '捐赠',
        2: '借阅',
        3: '分享笔记'
      }
      return textMap[type] || '未知'
    }
  }
}
</script>

<style scoped>
.admin-medals {
  position: relative;
  z-index: 10;
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
  padding: 16px 24px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input >>> .el-input__inner {
  border-radius: 8px 0 0 8px;
  border-color: rgba(171, 240, 209, 0.5);
}

.search-btn {
  border-radius: 0 8px 8px 0;
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border: none;
  color: #4a6a5a;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(171, 240, 209, 0.4);
}

/* 表格样式 */
.medal-list-table {
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

/* 标签样式 */
.custom-tag {
  border-radius: 8px;
  font-weight: 500;
}
</style>
