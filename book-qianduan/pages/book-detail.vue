<template>
  <div class="book-detail-container">
    <div class="page-header">
      <h2>书籍详情</h2>
      <el-button @click="goBack" icon="el-icon-back">返回</el-button>
    </div>
    
    <el-row :gutter="20">
      <!-- 左侧：书籍基本信息 -->
      <el-col :span="8">
        <el-card class="book-info-card">
          <div slot="header">
            <span>基本信息</span>
          </div>
          <div class="book-detail" v-if="book">
            <div class="book-name">《{{ book.bookName }}》</div>
            <div class="book-author">作者：{{ book.author }}</div>
            <div class="book-donor">捐赠人：{{ book.donorName || '匿名捐赠者' }}</div>
            <div class="book-status">
              状态：
              <el-tag :type="getStatusTagType(book.bookStatus)">
                {{ getStatusText(book.bookStatus) }}
              </el-tag>
            </div>
            <div class="book-qrcode" v-if="book.bookQrcode">
              <p>书籍二维码：</p>
              <div id="qrcode"></div>
              <p class="qrcode-text">{{ book.bookQrcode }}</p>
            </div>
            
            <!-- 操作按钮 -->
            <div class="action-buttons" style="margin-top: 20px;">
              <el-button 
                type="primary" 
                @click="viewNotes"
                icon="el-icon-edit"
                style="width: 100%; margin-bottom: 10px;"
              >
                查看笔记
              </el-button>
              <el-button 
                type="success" 
                @click="claimBook"
                v-if="book.bookStatus === 1"
                style="width: 100%;"
              >
                立即认领
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧：漂流轨迹可视化 -->
      <el-col :span="16">
        <el-card class="trail-card">
          <div slot="header">
            <span>漂流轨迹</span>
          </div>
          <div ref="trailChart" style="height: 400px;"></div>
        </el-card>
        
        <!-- 漂流记录表格 -->
        <el-card class="records-card" style="margin-top: 20px;">
          <div slot="header">
            <span>详细记录</span>
          </div>
          <el-table
            :data="trailRecords"
            border
            stripe
            style="width: 100%"
          >
            <el-table-column prop="userName" label="持有者" width="120" />
            <el-table-column prop="claimTime" label="认领时间" width="180" />
            <el-table-column prop="returnTime" label="归还时间" width="180" />
            <el-table-column prop="dueTime" label="应还时间" width="180" />
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.returnTime ? 'success' : 'warning'">
                  {{ scope.row.returnTime ? '已归还' : '借阅中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="是否超期" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isOverdue === 1 ? 'danger' : 'success'">
                  {{ scope.row.isOverdue === 1 ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  data() {
    return {
      bookId: null,
      book: null,
      trailRecords: [],
      chartInstance: null
    }
  },
  mounted() {
    this.getBookIdFromQuery()
    this.fetchBookDetail()
    this.fetchTrailRecords()
  },
  methods: {
    getBookIdFromQuery() {
      this.bookId = this.$route.query.id
      if (!this.bookId) {
        this.$message.error('缺少书籍 ID')
        this.$router.push('/book-trail')
      }
    },
    async fetchBookDetail() {
      try {
        const response = await this.$axios.get(`/bookInfo/${this.bookId}`)
        if (response.code === 20000) {
          this.book = response.data
          // 生成二维码
          this.$nextTick(() => {
            this.generateQRCode()
          })
        }
      } catch (error) {
        this.$message.error('获取书籍信息失败')
      }
    },
    async fetchTrailRecords() {
      try {
        const response = await this.$axios.get(`/bookClaimRecord/history/${this.bookId}`, {
          params: {
            size: 100,
            current: 1
          }
        })
        
        if (response.code === 20000) {
          this.trailRecords = response.data.records || []
          this.$nextTick(() => {
            this.initTrailChart()
          })
        }
      } catch (error) {
        this.$message.error('获取漂流记录失败')
      }
    },
    generateQRCode() {
      // 使用 QRCode.js 生成二维码
      // 注意：需要先安装 qrcodejs 包
      const qrcodeElement = document.getElementById('qrcode')
      if (qrcodeElement && this.book.bookQrcode) {
        // 简单实现，实际应该使用 QRCode 库
        qrcodeElement.innerHTML = `<div style="text-align: center; padding: 20px; background: #f5f5f5; border: 1px solid #ddd;">${this.book.bookQrcode}</div>`
      }
    },
    initTrailChart() {
      // 使用 ECharts 绘制漂流轨迹图
      const chartDom = this.$refs.trailChart
      if (!chartDom) return
      
      this.chartInstance = echarts.init(chartDom)
      
      // 准备数据
      const data = this.trailRecords.map((record, index) => {
        return {
          name: record.userName || `用户${index + 1}`,
          value: [
            new Date(record.claimTime).getTime(),
            index + 1,
            record.returnTime ? new Date(record.returnTime).getTime() : null
          ]
        }
      })
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'line'
          },
          formatter: function(params) {
            const item = params[0]
            const record = data[item.dataIndex]
            let result = `${record.name}<br/>`
            result += `认领时间：${new Date(record.value[0]).toLocaleDateString()}<br/>`
            if (record.value[2]) {
              result += `归还时间：${new Date(record.value[2]).toLocaleDateString()}`
            } else {
              result += '状态：借阅中'
            }
            return result
          }
        },
        xAxis: {
          type: 'time',
          name: '时间',
          axisLabel: {
            formatter: '{yyyy}-{MM}-{dd}'
          }
        },
        yAxis: {
          type: 'category',
          name: '持有者',
          data: data.map(item => item.name),
          inverse: true
        },
        series: [
          {
            type: 'custom',
            renderItem: (index, opts) => {
              const point = opts.coordSys.pointToData(data[index].value)
              return {
                type: 'rect',
                shape: {
                  x: point[0],
                  y: opts.coordSys.yAxis.getBandPosition(0) - 10,
                  width: data[index].value[2] 
                    ? opts.coordSys.pointToData([data[index].value[2], 0])[0] - point[0]
                    : 50,
                  height: 20
                },
                style: opts.style
              }
            },
            itemStyle: {
              color: '#409EFF'
            },
            encode: {
              x: [0, 2],
              y: 1
            },
            data: data.map(item => ({
              value: [
                new Date(item.value[0]).getTime(),
                item.name,
                item.value[2] ? new Date(item.value[2]).getTime() : null
              ]
            }))
          }
        ]
      }
      
      this.chartInstance.setOption(option)
      
      // 响应式
      window.addEventListener('resize', () => {
        this.chartInstance.resize()
      })
    },
    getStatusTagType(status) {
      switch (status) {
        case 1: return 'warning'
        case 2: return 'success'
        case 3: return 'info'
        default: return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 1: return '待认领'
        case 2: return '已认领'
        case 3: return '已归还'
        default: return '未知状态'
      }
    },
    claimBook() {
      this.$confirm('确定要认领这本书吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const user = JSON.parse(localStorage.getItem('user'))
          const response = await this.$axios.post(`/bookClaimRecord/claim/${this.bookId}/${user.id}`)
          
          if (response.code === 20000) {
            this.$message.success('认领成功')
            this.fetchBookDetail()
            this.fetchTrailRecords()
          } else {
            this.$message.error(response.message || '认领失败')
          }
        } catch (error) {
          this.$message.error('认领失败：' + (error.response?.data?.message || error.message))
        }
      }).catch(() => {})
    },
    viewNotes() {
      this.$router.push({
        path: '/book-notes',
        query: { id: this.bookId }
      })
    },
    goBack() {
      this.$router.push('/book-trail')
    }
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
  }
}
</script>

<style scoped>
.book-detail-container {
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
  color: #545c64;
}

.book-info-card,
.trail-card,
.records-card {
  margin-bottom: 20px;
}

.book-detail {
  text-align: center;
}

.book-name {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 15px;
}

.book-author,
.book-donor {
  margin: 10px 0;
  color: #606266;
}

.book-status {
  margin: 15px 0;
}

.book-qrcode {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 5px;
}

.book-qrcode p {
  margin: 5px 0;
}

#qrcode {
  margin: 10px auto;
  width: 150px;
  height: 150px;
}

.qrcode-text {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
}

.action-buttons {
  margin-top: 20px;
}
</style>
