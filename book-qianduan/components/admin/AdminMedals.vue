<template>
  <div class="admin-medals">
    <div class="page-header">
      <h2>勋章管理</h2>
    </div>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchUserName"
        placeholder="请输入用户名查询该用户的勋章"
        style="width: 250px; margin-right: 10px;"
        clearable
        @clear="handleSearch"
      >
        <el-button slot="append" icon="el-icon-user" @click="handleSearch"></el-button>
      </el-input>
    </div>
    
    <!-- 勋章列表 -->
    <el-table
      :data="medalList"
      border
      stripe
      class="medal-list-table"
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
          <el-tag :type="getMedalTypeTag(scope.row.medalType)">
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
      class="pagination-box"
    >
    </el-pagination>
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

.search-bar {
  margin-bottom: 20px;
}

.medal-list-table {
  margin-bottom: 20px;
}

.pagination-box {
  text-align: right;
}
</style>
