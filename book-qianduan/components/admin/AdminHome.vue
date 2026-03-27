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
            </div>
            <el-table :data="activeUsers" style="width: 100%">
              <el-table-column prop="rank" label="排名" width="80"></el-table-column>
              <el-table-column prop="name" label="用户名"></el-table-column>
              <el-table-column prop="college" label="学院"></el-table-column>
              <el-table-column prop="activityScore" label="活跃度分数"></el-table-column>
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
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
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
    handleResize() {
      if (this.donationChart) {
        this.donationChart.resize()
      }
      if (this.borrowChart) {
        this.borrowChart.resize()
      }
      if (this.statusChart) {
        this.statusChart.resize()
      }
    },
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
        const res = await this.$axios.post('/userActivity/rank/10/1', null, {
          params: { rankType: 'total' }
        })
        if (res.code === 20000 && res.data) {
          this.activeUsers = res.data.records || []
        }
      } catch (error) {
        console.error('获取活跃用户失败:', error)
      }
    },
    async initCharts() {
      try {
        const [recentRes, statusRes] = await Promise.all([
          this.$axios.post('/bookStatistics/recent/7'),
          this.$axios.post('/bookStatistics/status')
        ])
        if (recentRes.code === 20000 && recentRes.data) {
          const data = recentRes.data.reverse()
          this.initDonationChart(data)
          this.initBorrowChart(data)
        }
        if (statusRes.code === 20000 && statusRes.data) {
          this.initStatusChart(statusRes.data)
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
      
      if (this.donationChart) {
        this.donationChart.dispose()
      }
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
          bottom: '15%',
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
      this.donationChart.resize()
    },
    initBorrowChart(data) {
      const dates = data.map(item => {
        const date = new Date(item.statDate)
        return `${date.getMonth() + 1}/${date.getDate()}`
      })
      const borrows = data.map(item => item.totalBorrows || 0)
      
      if (this.borrowChart) {
        this.borrowChart.dispose()
      }
      this.borrowChart = echarts.init(this.$refs.borrowChart)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
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
      this.borrowChart.resize()
    },
    initStatusChart(statusData) {
      if (this.statusChart) {
        this.statusChart.dispose()
      }
      this.statusChart = echarts.init(this.$refs.statusChart)
      const option = {
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '书籍状态',
            type: 'pie',
            radius: '60%',
            center: ['50%', '55%'],
            data: [
              { value: statusData.availableBooks || 0, name: '待认领' },
              { value: statusData.borrowedBooks || 0, name: '已认领' },
              { value: statusData.returnedBooks || 0, name: '已归还' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      this.statusChart.setOption(option)
      this.statusChart.resize()
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
  height: 450px;
  padding: 20px;
  box-sizing: border-box;
}

.chart-header {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.chart {
  width: 100%;
  height: 380px;
}
</style>
