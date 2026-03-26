<template>
  <div class="ranking-container">
    <h1>用户活跃度排行榜</h1>
    
    <!-- 排行榜类型选择 -->
    <div class="rank-type-selector">
      <el-radio-group v-model="rankType" @change="handleRankTypeChange">
        <el-radio-button label="total">总榜</el-radio-button>
        <el-radio-button label="monthly">月榜</el-radio-button>
        <el-radio-button label="weekly">周榜</el-radio-button>
        <el-radio-button label="daily">日榜</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 展示数量选择 -->
    <div class="rank-size-selector">
      <span>展示：</span>
      <el-radio-group v-model="rankSize" @change="handleRankSizeChange">
        <el-radio-button label="10">TOP 10</el-radio-button>
        <el-radio-button label="50">TOP 50</el-radio-button>
        <el-radio-button label="100">全部</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 排行榜列表 -->
    <div class="ranking-list">
      <el-table :data="rankingList" stripe style="width: 100%">
        <el-table-column prop="rank" label="排名" width="100">
          <template slot-scope="scope">
            <div class="rank-badge" :class="'rank-' + scope.row.rank">
              <i v-if="scope.row.rank === 1" class="el-icon-crown"></i>
              <i v-else-if="scope.row.rank === 2" class="el-icon-medal"></i>
              <i v-else-if="scope.row.rank === 3" class="el-icon-trophy"></i>
              <span v-else>{{ scope.row.rank }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="180">
          <template slot-scope="scope">
            <div class="user-info">
              <el-avatar :size="40" :src="getUserAvatar(scope.row.name)">
                {{ scope.row.name.charAt(0) }}
              </el-avatar>
              <div class="user-details">
                <div class="user-name">{{ scope.row.name }}</div>
                <div class="user-college">{{ scope.row.college || '未知学院' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="活跃度" width="150">
          <template slot-scope="scope">
            <div class="activity-score">
              <span class="score-number">{{ getActivityScore(scope.row) }}</span>
              <span class="score-label">分</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="变化" width="100">
          <template slot-scope="scope">
            <div class="change-badge" :class="getChangeClass(scope.row)">
              <i :class="getChangeIcon(scope.row)"></i>
              <span>{{ getChangeText(scope.row) }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="rankingList.length === 0" class="empty-data">暂无排行榜数据</div>
      
      <!-- 分页 -->
      <el-pagination
        v-if="rankingList.length > 0"
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        layout="total, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: right;"
      />
    </div>
    
    <!-- 我的排名 -->
    <div class="my-rank" v-if="currentUser">
      <el-card>
        <div slot="header">
          <span>我的排名</span>
        </div>
        <div class="my-rank-content">
          <div class="my-rank-number">
            <span class="rank-value">{{ myRank ? (myRank.rank > 100 ? '100+' : myRank.rank) : '未上榜' }}</span>
          </div>
          <div class="my-rank-info">
            <div class="my-activity-score">
              <span class="score-label">我的活跃度：</span>
              <span class="score-value">{{ myRank ? getActivityScore(myRank) : 0 }} 分</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentUser: null,
      rankType: 'total',
      rankSize: '10',
      rankingList: [],
      myRank: null,
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  mounted() {
    this.getCurrentUser()
    this.fetchRankingList()
    this.fetchMyRank()
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
    async fetchRankingList() {
      try {
        const res = await this.$axios.post(`/userActivity/rank/${this.pageSize}/${this.currentPage}`, null, {
          params: { rankType: this.rankType }
        })
        if (res.code === 20000 && res.data) {
          this.rankingList = res.data.records || []
          this.total = res.data.total || 0
        }
      } catch (error) {
        console.error('获取排行榜失败:', error)
        this.$message.error('获取排行榜失败')
      }
    },
    async fetchMyRank() {
      try {
        const res = await this.$axios.get(`/userActivity/userRank/${this.currentUser.id}`, {
          params: { rankType: this.rankType }
        })
        if (res.code === 20000 && res.data) {
          this.myRank = res.data
        }
      } catch (error) {
        console.error('获取我的排名失败:', error)
      }
    },
    handleRankTypeChange() {
      this.currentPage = 1
      this.fetchRankingList()
      this.fetchMyRank()
    },
    handleRankSizeChange() {
      this.pageSize = parseInt(this.rankSize)
      this.currentPage = 1
      this.fetchRankingList()
    },
    handlePageChange(val) {
      this.currentPage = val
      this.fetchRankingList()
    },
    getUserAvatar(name) {
      return ''
    },
    getActivityScore(user) {
      switch (this.rankType) {
        case 'daily':
          return user.dailyActivityScore || 0
        case 'weekly':
          return user.weeklyActivityScore || 0
        case 'monthly':
          return user.monthlyActivityScore || 0
        case 'total':
        default:
          return user.activityScore || 0
      }
    },
    getChangeClass(user) {
      return 'no-change'
    },
    getChangeIcon(user) {
      return 'el-icon-minus'
    },
    getChangeText(user) {
      return '-'
    }
  }
}
</script>

<style scoped>
.ranking-container {
  padding: 20px;
}

h1 {
  color: #303133;
  margin-bottom: 30px;
}

.rank-type-selector,
.rank-size-selector {
  margin-bottom: 20px;
}

.rank-size-selector span {
  margin-right: 10px;
  color: #606266;
}

.ranking-list {
  margin-top: 30px;
}

.rank-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-weight: bold;
  font-size: 16px;
}

.rank-1 {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
}

.rank-2 {
  background: linear-gradient(135deg, #C0C0C0, #A9A9A9);
  color: white;
}

.rank-3 {
  background: linear-gradient(135deg, #CD7F32, #8B4513);
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-details {
  margin-left: 12px;
}

.user-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.user-college {
  font-size: 12px;
  color: #909399;
}

.activity-score {
  display: flex;
  align-items: center;
}

.score-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-right: 4px;
}

.score-label {
  font-size: 14px;
  color: #909399;
}

.change-badge {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.no-change {
  color: #909399;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.my-rank {
  margin-top: 40px;
}

.my-rank-content {
  display: flex;
  align-items: center;
}

.my-rank-number {
  flex: 0 0 120px;
  text-align: center;
}

.rank-value {
  font-size: 48px;
  font-weight: bold;
  color: #E6A23C;
}

.my-rank-info {
  flex: 1;
}

.my-activity-score {
  font-size: 18px;
  color: #606266;
}

.score-label {
  margin-right: 10px;
}

.score-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
</style>
