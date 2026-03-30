<template>
  <div class="qr-scanner">
    <el-dialog
      title="扫码操作"
      :visible.sync="dialogVisible"
      width="500px"
      @close="closeDialog"
      class="custom-dialog"
    >
      <div style="text-align: center;">
        <div v-if="!scanning && !scanResult" style="padding: 20px;">
          <el-button type="primary" @click="startScanning" icon="el-icon-camera" size="large">
            开始扫码
          </el-button>
          <div style="margin-top: 20px;">
            <el-input
              v-model="manualInput"
              placeholder="或者手动输入二维码编号"
              clearable
              style="width: 300px;"
            />
            <el-button type="success" @click="handleManualInput" style="margin-left: 10px;">确认</el-button>
          </div>
        </div>
        
        <div v-if="scanning" style="padding: 20px;">
          <div style="color: #6a7a6a; margin-bottom: 15px;">请将书籍二维码对准摄像头</div>
          <video
            ref="video"
            id="qr-video"
            style="width: 100%; max-width: 350px; border-radius: 8px; border: 2px solid #abf0d1;"
          ></video>
          <div style="margin-top: 15px;">
            <el-button type="warning" @click="stopScanning">取消扫码</el-button>
          </div>
        </div>
        
        <div v-if="scanResult && !scanning" style="padding: 20px;">
          <div v-if="bookInfo" style="text-align: left; background: rgba(171, 240, 209, 0.15); border-radius: 12px; padding: 20px;">
            <h4 style="margin: 0 0 15px 0; color: #5a6a5a;">书籍信息</h4>
            <p style="margin: 8px 0;"><strong>书名：</strong>{{ bookInfo.bookName }}</p>
            <p style="margin: 8px 0;"><strong>作者：</strong>{{ bookInfo.author || '未知' }}</p>
            <p style="margin: 8px 0;"><strong>状态：</strong>
              <el-tag :type="getStatusType(bookInfo.bookStatus)" size="small">{{ getStatusText(bookInfo.bookStatus) }}</el-tag>
            </p>
            <div v-if="bookInfo.tags && bookInfo.tags.length > 0" style="margin: 12px 0;">
              <strong>标签：</strong>
              <el-tag v-for="tag in bookInfo.tags" :key="tag.id" size="small" style="margin: 3px;">{{ tag.tagName }}</el-tag>
            </div>
          </div>
          
          <div v-else style="color: #f56c6c; padding: 20px;">
            <i class="el-icon-warning" style="margin-right: 8px;"></i>未找到对应书籍
          </div>
          
          <div style="margin-top: 20px;">
            <el-button
              v-if="bookInfo && (bookInfo.bookStatus === 1 || bookInfo.bookStatus === 3)"
              type="success"
              @click="handleBorrow"
              :loading="processing"
              icon="el-icon-download"
            >
              借阅书籍
            </el-button>
            <el-button
              v-if="bookInfo && bookInfo.bookStatus === 2 && bookInfo.currentHolderId === currentUserId"
              type="primary"
              @click="handleReturn"
              :loading="processing"
              icon="el-icon-upload2"
            >
              归还书籍
            </el-button>
            <el-button type="info" @click="resetScanner" style="margin-left: 10px;">继续扫码</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'QrScanner',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    currentUserId: {
      type: Number,
      default: null
    }
  },
  data() {
    return {
      dialogVisible: false,
      scanning: false,
      scanResult: null,
      bookInfo: null,
      manualInput: '',
      processing: false,
      videoElement: null,
      canvasElement: null,
      canvasContext: null,
      animationId: null
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
    },
    dialogVisible(val) {
      if (!val) {
        this.$emit('close')
      }
    }
  },
  mounted() {
    this.dialogVisible = this.visible
  },
  methods: {
    async startScanning() {
      try {
        this.scanning = true
        this.scanResult = null
        this.bookInfo = null
        
        await this.$nextTick()
        
        this.videoElement = this.$refs.video
        this.canvasElement = document.createElement('canvas')
        this.canvasContext = this.canvasElement.getContext('2d')
        
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: 'environment' }
        })
        
        this.videoElement.srcObject = stream
        await this.videoElement.play()
        
        this.canvasElement.width = this.videoElement.videoWidth
        this.canvasElement.height = this.videoElement.videoHeight
        
        this.scanFrame()
      } catch (error) {
        console.error('启动摄像头失败:', error)
        this.$message.error('无法访问摄像头，请使用手动输入方式')
        this.scanning = false
      }
    },
    
    scanFrame() {
      if (!this.scanning) return
      
      if (this.videoElement.readyState === this.videoElement.HAVE_ENOUGH_DATA) {
        this.canvasContext.drawImage(
          this.videoElement,
          0, 0,
          this.canvasElement.width,
          this.canvasElement.height
        )
        
        const imageData = this.canvasContext.getImageData(
          0, 0,
          this.canvasElement.width,
          this.canvasElement.height
        )
        
        if (window.jsQR) {
          const code = window.jsQR(imageData.data, imageData.width, imageData.height)
          if (code) {
            this.handleScanResult(code.data)
            return
          }
        }
      }
      
      this.animationId = requestAnimationFrame(() => this.scanFrame())
    },
    
    async handleScanResult(qrcode) {
      this.stopScanning()
      this.scanResult = qrcode
      await this.fetchBookInfo(qrcode)
    },
    
    async handleManualInput() {
      if (!this.manualInput.trim()) {
        this.$message.warning('请输入二维码编号')
        return
      }
      this.scanResult = this.manualInput.trim()
      await this.fetchBookInfo(this.manualInput.trim())
    },
    
    async fetchBookInfo(qrcode) {
      try {
        const response = await this.$axios.get(`/bookInfo/qrcode/${qrcode}`)
        if (response.code === 20000) {
          this.bookInfo = response.data
        } else {
          this.bookInfo = null
          this.$message.error(response.message || '未找到书籍')
        }
      } catch (error) {
        this.bookInfo = null
        this.$message.error('查询失败：' + (error.response?.data?.message || error.message))
      }
    },
    
    async handleBorrow() {
      if (!this.currentUserId) {
        this.$message.warning('请先登录')
        return
      }
      this.processing = true
      try {
        const response = await this.$axios.post(`/bookClaimRecord/scan/claim/${this.scanResult}/${this.currentUserId}`)
        if (response.code === 20000) {
          this.$message.success('借阅成功！')
          if (response.score !== undefined && response.score !== null) {
            this.$message.info(`获得活跃度：${response.score}`)
          }
          this.$emit('success', 'borrow', this.bookInfo)
          this.resetScanner()
        } else {
          this.$message.error(response.message || '借阅失败')
        }
      } catch (error) {
        this.$message.error('借阅失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.processing = false
      }
    },
    
    async handleReturn() {
      if (!this.currentUserId) {
        this.$message.warning('请先登录')
        return
      }
      this.processing = true
      try {
        const response = await this.$axios.post(`/bookClaimRecord/scan/return/${this.scanResult}/${this.currentUserId}`)
        if (response.code === 20000) {
          this.$message.success('归还成功！')
          if (response.score !== undefined && response.score !== null) {
            this.$message.info(`获得活跃度：${response.score}`)
          }
          this.$emit('success', 'return', this.bookInfo)
          this.resetScanner()
        } else {
          this.$message.error(response.message || '归还失败')
        }
      } catch (error) {
        this.$message.error('归还失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.processing = false
      }
    },
    
    stopScanning() {
      this.scanning = false
      if (this.animationId) {
        cancelAnimationFrame(this.animationId)
        this.animationId = null
      }
      if (this.videoElement && this.videoElement.srcObject) {
        const tracks = this.videoElement.srcObject.getTracks()
        tracks.forEach(track => track.stop())
        this.videoElement.srcObject = null
      }
    },
    
    resetScanner() {
      this.scanResult = null
      this.bookInfo = null
      this.manualInput = ''
    },
    
    closeDialog() {
      this.stopScanning()
      this.resetScanner()
      this.dialogVisible = false
      this.$emit('close')
    },
    
    getStatusType(status) {
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
    }
  }
}
</script>

<style scoped>
.qr-scanner {
  position: relative;
  z-index: 1000;
}

.custom-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border-radius: 4px 4px 0 0;
}

.custom-dialog >>> .el-dialog__title {
  color: #5a6a5a;
  font-weight: 700;
}
</style>
