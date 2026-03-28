<template>
  <div class="admin-home">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-box slide-in">
        <h1 class="welcome-title">
          <span class="title-glow">欢迎回来，{{ currentUser.name }}</span>
        </h1>
        <p class="welcome-subtitle">这是您的管理控制台</p>
        <el-button type="primary" @click="collectStatistics" class="refresh-btn">
          <i class="el-icon-refresh"></i> 更新统计数据
        </el-button>
      </div>
    </div>
    
    <!-- 数据统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="24">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-text" style="text-align: center; width: 100%;">
              <div class="stat-value">{{ stats.totalBooks }}</div>
              <div class="stat-label">总书籍数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-text" style="text-align: center; width: 100%;">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-text" style="text-align: center; width: 100%;">
              <div class="stat-value">{{ stats.totalBorrows }}</div>
              <div class="stat-label">总借阅次数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-text" style="text-align: center; width: 100%;">
              <div class="stat-value">{{ stats.totalDonations }}</div>
              <div class="stat-label">总捐赠次数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表容器 -->
    <div class="charts-container">
      <el-row :gutter="24">
        <el-col :span="12">
          <div class="section-box chart-card">
            <div class="section-header">
              <h3 class="section-title">捐赠统计图</h3>
            </div>
            <div ref="donationChart" class="chart"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="section-box chart-card">
            <div class="section-header">
              <h3 class="section-title">借阅趋势图</h3>
            </div>
            <div ref="borrowChart" class="chart"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="section-box chart-card">
            <div class="section-header">
              <h3 class="section-title">书籍状态分布</h3>
            </div>
            <div ref="statusChart" class="chart"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="section-box chart-card">
            <div class="section-header">
              <h3 class="section-title">用户活跃排行</h3>
            </div>
            <el-table :data="activeUsers" style="width: 100%" class="custom-table">
              <el-table-column prop="rank" label="排名" width="80"></el-table-column>
              <el-table-column prop="name" label="用户名"></el-table-column>
              <el-table-column prop="college" label="学院"></el-table-column>
              <el-table-column prop="activityScore" label="活跃度分数"></el-table-column>
            </el-table>
          </div>
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
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: '#abf0d1' },
                  { offset: 1, color: '#d4eea7' }
                ]
              },
              borderRadius: [8, 8, 0, 0]
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
              color: '#7a9d5a'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(171, 240, 209, 0.3)' },
                  { offset: 1, color: 'rgba(171, 240, 209, 0.05)' }
                ]
              }
            },
            lineStyle: {
              width: 3
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
          left: 'left',
          textStyle: {
            color: '#5a6a5a'
          }
        },
        series: [
          {
            name: '书籍状态',
            type: 'pie',
            radius: ['40%', '60%'],
            center: ['60%', '55%'],
            data: [
              { value: statusData.availableBooks || 0, name: '待认领', itemStyle: { color: '#f5d089' } },
              { value: statusData.borrowedBooks || 0, name: '已认领', itemStyle: { color: '#abf0d1' } },
              { value: statusData.returnedBooks || 0, name: '已归还', itemStyle: { color: '#d4eea7' } }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.2)'
              }
            },
            label: {
              color: '#5a6a5a'
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

.refresh-btn {
  border-radius: 8px;
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border: none;
  color: #4a6a5a;
  font-weight: 600;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(171, 240, 209, 0.4);
}

/* 统计卡片区域 */
.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(171, 240, 209, 0.3);
  box-shadow: 5px 2px 10px rgba(0, 0, 0, 0.06);
  padding: 20px 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.35s ease;
  height: 110px;
}

.stat-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
}

.stat-text {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #5a6a5a;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #8a9a8a;
  font-weight: 500;
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

/* 图表容器 */
.charts-container {
  margin-top: 0;
}

.chart-card {
  height: 420px;
  box-sizing: border-box;
}

.chart {
  width: 100%;
  height: 340px;
}

/* 表格样式 */
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
</style>
