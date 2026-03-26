<template>
  <div class="admin-home">
    <h1>欢迎回来，{{ currentUser.name }}</h1>
    <p>这是您的管理控制台</p>
    <el-button type="primary" @click="collectStatistics" style="margin-bottom: 20px;">
      <i class="el-icon-refresh"></i> 更新统计数据
    </el-button>
    
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <i class="el-icon-reading stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ stats.totalBooks }}</div>
                <div class="stat-label">总书籍数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <i class="el-icon-user stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ stats.totalUsers }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <i class="el-icon-s-order stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ stats.totalBorrows }}</div>
                <div class="stat-label">总借阅次数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <i class="el-icon-present stat-icon"></i>
              <div class="stat-text">
                <div class="stat-value">{{ stats.totalDonations }}</div>
                <div class="stat-label">总捐赠次数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="charts-container">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>捐赠统计图</span>
            </div>
            <div ref="donationChart" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>借阅趋势图</span>
            </div>
            <div ref="borrowChart" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>书籍状态分布</span>
            </div>
            <div ref="statusChart" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <span>用户活跃排行</span>
              <el-select v-model="rankType" size="mini" style="float: right;" @change="fetchActiveUsers">
                <el-option label="总榜" value="total"></el-option>
                <el-option label="日榜" value="daily"></el-option>
                <el-option label="周榜" value="weekly"></el-option>
                <el-option label="月榜" value="monthly"></el-option>
              </el-select>
            </div>
            <el-table :data="activeUsers" style="width: 100%">
              <el-table-column prop="rank" label="排名" width="80">
                <template slot-scope="scope">
                  <el-tag v-if="scope.row.rank <= 3" :type="getRankTagType(scope.row.rank)" size="mini">
                    {{ scope.row.rank }}
                  </el-tag>
                  <span v-else>{{ scope.row.rank }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="用户名"></el-table-column>
              <el-table-column prop="college" label="学院"></el-table-column>
              <el-table-column prop="activityScore" label="活跃度分数" width="120"></el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'AdminHome',
  props: {
    currentUser: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      stats: {
        totalBooks: 0,
        totalUsers: 0,
        totalBorrows: 0,
        totalDonations: 0
      },
      activeUsers: [],
      rankType: 'total',
      donationChart: null,
      borrowChart: null,
      statusChart: null
    }
  },
  mounted() {
    this.fetchStats()
    this.fetchActiveUsers()
    this.$nextTick(() => {
      this.initCharts()
    })
  },
  beforeDestroy() {
    if (this.donationChart) {
      this.donationChart.dispose()
    }
    if (this.borrowChart) {
      this.borrowChart.dispose()
    }
    if (this.statusChart) {
      this.statusChart.dispose()
    }
  },
  methods: {
    async fetchStats() {
      try {
        const [booksRes, usersRes, statsRes] = await Promise.all([
          this.$axios.post('/bookInfo/condition/1/1'),
          this.$axios.post('/sysUser/condition/1/1'),
          this.$axios.post('/bookStatistics/latest')
        ])
        
        if (booksRes.code === 20000) {
          this.stats.totalBooks = booksRes.data.total || 0
        }
        
        if (usersRes.code === 20000) {
          this.stats.totalUsers = usersRes.data.total || 0
        }
        
        if (statsRes.code === 20000 && statsRes.data) {
          this.stats.totalBorrows = statsRes.data.totalBorrows || 0
          this.stats.totalDonations = statsRes.data.totalDonations || 0
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        this.$message.error('获取统计数据失败')
      }
    },
    async fetchActiveUsers() {
      try {
        const res = await this.$axios.post(`/userActivity/rank/10/1`, null, {
          params: { rankType: this.rankType }
        })
        if (res.code === 20000 && res.data && res.data.records) {
          this.activeUsers = res.data.records.map(user => ({
            rank: user.rank,
            name: user.name,
            college: user.college,
            activityScore: user.activityScore || 0
          }))
        }
      } catch (error) {
        console.error('获取活跃用户失败:', error)
      }
    },
    getRankTagType(rank) {
      switch (rank) {
        case 1: return 'danger'
        case 2: return 'warning'
        case 3: return 'success'
        default: return 'info'
      }
    },
    async initCharts() {
      try {
        const res = await this.$axios.post('/bookStatistics/recent/7')
        if (res.code === 20000 && res.data) {
          const data = res.data.reverse()
          this.initDonationChart(data)
          this.initBorrowChart(data)
          this.initStatusChart()
        }
      } catch (error) {
        console.error('获取统计图表数据失败:', error)
      }
    },
    initDonationChart(data) {
      const dates = data.map(item => {
        const date = new Date(item.statDate)
        return `${date.getMonth() + 1}/${date.getDate()}`
      })
      const donations = data.map(item => item.totalDonations || 0)
      
      this.donationChart = echarts.init(this.$refs.donationChart)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            data: donations,
            type: 'bar',
            itemStyle: {
              color: '#409EFF'
            }
          }
        ]
      }
      this.donationChart.setOption(option)
    },
    initBorrowChart(data) {
      const dates = data.map(item => {
        const date = new Date(item.statDate)
        return `${date.getMonth() + 1}/${date.getDate()}`
      })
      const borrows = data.map(item => item.totalBorrows || 0)
      
      this.borrowChart = echarts.init(this.$refs.borrowChart)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            data: borrows,
            type: 'line',
            smooth: true,
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      }
      this.borrowChart.setOption(option)
    },
    async initStatusChart() {
      try {
        // 从后端获取真实的书籍状态分布数据
        const res = await this.$axios.post('/bookInfo/condition/1/1')
        if (res.code === 20000) {
          const totalBooks = res.data.total || 0
          
          // 构建查询条件获取不同状态的书籍数量
          const [pendingRes, claimedRes, returnedRes] = await Promise.all([
            this.$axios.post('/bookInfo/condition/1/1', null, { params: { bookStatus: 1 } }),
            this.$axios.post('/bookInfo/condition/1/1', null, { params: { bookStatus: 2 } }),
            this.$axios.post('/bookInfo/condition/1/1', null, { params: { bookStatus: 3 } })
          ])
          
          const pendingCount = pendingRes.code === 20000 ? pendingRes.data.total || 0 : 0
          const claimedCount = claimedRes.code === 20000 ? claimedRes.data.total || 0 : 0
          const returnedCount = returnedRes.code === 20000 ? returnedRes.data.total || 0 : 0
          
          this.statusChart = echarts.init(this.$refs.statusChart)
          const option = {
            tooltip: {
              trigger: 'item',
              formatter: '{a} <br/>{b}: {c} ({d}%)'
            },
            legend: {
              orient: 'horizontal',
              bottom: '10%',
              left: 'center'
            },
            series: [
              {
                name: '书籍状态',
                type: 'pie',
                radius: '55%',
                center: ['50%', '45%'],
                data: [
                  { value: pendingCount, name: '待认领' },
                  { value: claimedCount, name: '已认领' },
                  { value: returnedCount, name: '已归还' }
                ],
                emphasis: {
                  itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                  }
                },
                label: {
                  formatter: '{b}: {d}%'
                }
              }
            ]
          }
          this.statusChart.setOption(option)
        }
      } catch (error) {
        console.error('获取书籍状态分布数据失败:', error)
        // 失败时使用模拟数据
        this.statusChart = echarts.init(this.$refs.statusChart)
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'horizontal',
            bottom: '10%',
            left: 'center'
          },
          series: [
            {
              name: '书籍状态',
              type: 'pie',
              radius: '55%',
              center: ['50%', '45%'],
              data: [
                { value: this.stats.totalBooks - (this.stats.totalBooks * 0.3), name: '待认领' },
                { value: this.stats.totalBooks * 0.2, name: '已认领' },
                { value: this.stats.totalBooks * 0.1, name: '已归还' }
              ],
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              },
              label: {
                formatter: '{b}: {d}%'
              }
            }
          ]
        }
        this.statusChart.setOption(option)
      }
    },
    async collectStatistics() {
      try {
        const res = await this.$axios.post('/bookStatistics/collect')
        if (res.code === 20000) {
          this.$message.success('统计数据更新成功')
          // 重新获取统计数据和图表
          await this.fetchStats()
          await this.fetchActiveUsers()
          await this.initCharts()
        }
      } catch (error) {
        console.error('更新统计数据失败:', error)
        this.$message.error('更新统计数据失败')
      }
    }
  }
}
</script>

<style scoped>
.admin-home {
  padding: 20px;
}

h1 {
  color: #303133;
  margin-bottom: 10px;
}

p {
  color: #909399;
  margin-bottom: 40px;
}

.stats-container {
  margin-bottom: 40px;
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 40px;
  color: #F56C6C;
  margin-right: 20px;
}

.stat-text {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-container {
  margin-top: 40px;
}

.chart-card {
  margin-bottom: 20px;
  height: 400px;
}

.chart-header {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.chart {
  width: 100%;
  height: 340px;
}
</style>
