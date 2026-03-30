<template>
  <div class="about-container">
    <!-- 粒子背景 -->
    <div class="particles-bg">
      <div v-for="i in 30" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>
    
    <!-- 装饰性背景元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="grid-lines"></div>
    </div>
    
    <div class="about-content">
      <!-- 标题区域 -->
      <div class="welcome-section">
        <div class="welcome-box slide-in">
          <h1 class="welcome-title">
            <span class="title-glow">关于我们</span>
          </h1>
          <p class="welcome-subtitle">校园闲置书籍漂流系统</p>
        </div>
      </div>
      
      <!-- 系统简介 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">系统简介</h3>
        </div>
        <div class="card-content">
          <p>校园闲置书籍漂流系统是一个旨在促进校园内书籍资源共享的平台。我们相信每一本书都有其价值，通过这个平台，同学们可以将自己闲置的书籍捐赠出来，让它们在校园内继续"漂流"，发挥更大的价值。</p>
          <p>我们的目标是建立一个可持续发展的书籍共享生态系统，让知识在校园内自由流动，帮助同学们节省购书成本，同时培养大家的分享精神和环保意识。</p>
        </div>
      </div>
      
      <!-- 功能介绍 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">功能介绍</h3>
        </div>
        <div class="card-content">
          <el-row :gutter="24">
            <el-col :span="8">
              <div class="feature-item">
                <div class="feature-text">
                  <h4>书籍捐赠</h4>
                  <p>将闲置书籍捐赠到系统，让它们继续发挥价值</p>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <div class="feature-text">
                  <h4>书籍借阅</h4>
                  <p>浏览并借阅系统中的书籍，满足学习和阅读需求</p>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <div class="feature-text">
                  <h4>成就系统</h4>
                  <p>通过捐赠和借阅书籍获得成就勋章，展示你的贡献</p>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <!-- 联系我们 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">联系我们</h3>
        </div>
        <div class="card-content">
          <el-row :gutter="24">
            <el-col :span="12">
              <div class="contact-item">
                <div class="contact-info">
                  <h4>电子邮件</h4>
                  <p>support@bookdrift.edu</p>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="contact-item">
                <div class="contact-info">
                  <h4>联系电话</h4>
                  <p>400-123-4567</p>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="contact-item">
                <div class="contact-info">
                  <h4>办公地址</h4>
                  <p>武汉工程大学图书馆一楼服务台</p>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="contact-item">
                <div class="contact-info">
                  <h4>服务时间</h4>
                  <p>周一至周五 9:00-18:00</p>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <!-- 团队介绍 -->
      <div class="section-box">
        <div class="section-header">
          <h3 class="section-title">开发团队</h3>
        </div>
        <div class="card-content">
          <el-row :gutter="24">
            <el-col :span="6" v-for="(member, index) in teamMembers" :key="index">
              <div class="team-member">
                <div class="member-avatar">
                  <div class="avatar-placeholder">{{ member.name.charAt(0) }}</div>
                </div>
                <h4>{{ member.name }}</h4>
                <p>{{ member.role }}</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      teamMembers: [
        {
          name: '张琦',
          role: '项目经理'
        },
        {
          name: '李锦运',
          role: '前端开发'
        },
        {
          name: '杨灿',
          role: '后端开发'
        },
        {
          name: '高钰',
          role: 'UI设计'
        }
      ]
    }
  },
  methods: {
    getParticleStyle(index) {
      const size = Math.random() * 4 + 2
      const duration = Math.random() * 20 + 10
      const delay = Math.random() * 10
      return {
        width: `${size}px`,
        height: `${size}px`,
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animationDuration: `${duration}s`,
        animationDelay: `${delay}s`
      }
    }
  }
}
</script>

<style scoped>
.about-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #fef9f0 0%, #f5f0e6 50%, #e8f5e2 100%);
  position: relative;
  overflow-x: hidden;
}

* {
  box-sizing: border-box;
}

/* 粒子背景 */
.particles-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: radial-gradient(circle, #abf0d1, #d4eea7);
  border-radius: 50%;
  box-shadow: 0 0 10px #abf0d1, 0 0 20px #d4eea7;
  animation: particleFloat linear infinite;
  pointer-events: none;
}

@keyframes particleFloat {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-100px) rotate(720deg);
    opacity: 0;
  }
}

/* 装饰性背景元素 */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border: 1px solid rgba(171, 240, 209, 0.2);
  box-shadow: 0 0 40px rgba(171, 240, 209, 0.15), inset 0 0 40px rgba(171, 240, 209, 0.08);
  animation: float 12s ease-in-out infinite;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg) scale(1);
  }
  50% {
    transform: translateY(-30px) rotate(180deg) scale(1.03);
  }
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(171, 240, 209, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(171, 240, 209, 0.04) 1px, transparent 1px);
  background-size: 60px 60px;
}

.about-content {
  position: relative;
  z-index: 10;
  padding: 20px;
  width: 100%;
  overflow-x: hidden;
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
  margin: 0;
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
  transform: translateY(-2px);
  box-shadow: 6px 4px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
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

.card-content p {
  color: #6a7a6a;
  line-height: 1.8;
  margin-bottom: 15px;
  font-size: 14px;
}

/* 功能介绍样式 */
.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  border: 1px solid rgba(171, 240, 209, 0.25);
  transition: all 0.35s ease;
}

.feature-item:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
}

.feature-text {
  text-align: center;
}

.feature-text h4 {
  color: #5a6a5a;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
}

.feature-text p {
  color: #8a9a8a;
  margin: 0;
  font-size: 13px;
}

/* 联系我们样式 */
.contact-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  border: 1px solid rgba(171, 240, 209, 0.25);
  margin-bottom: 15px;
  transition: all 0.35s ease;
}

.contact-item:hover {
  transform: translateY(-2px);
  box-shadow: 5px 2px 10px rgba(0, 0, 0, 0.08);
  border-color: rgba(171, 240, 209, 0.6);
}

.contact-info h4 {
  color: #5a6a5a;
  margin: 0 0 5px 0;
  font-size: 15px;
  font-weight: 600;
}

.contact-info p {
  color: #8a9a8a;
  margin: 0;
  font-size: 13px;
}

/* 团队介绍样式 */
.team-member {
  text-align: center;
  padding: 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.12), rgba(212, 238, 167, 0.12));
  border: 1px solid rgba(171, 240, 209, 0.25);
  transition: all 0.35s ease;
}

.team-member:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 6px 3px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(171, 240, 209, 0.6);
}

.member-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #abf0d1, #d4eea7);
  margin: 0 auto 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder {
  color: #4a6a5a;
  font-size: 28px;
  font-weight: bold;
}

.team-member h4 {
  color: #5a6a5a;
  margin-bottom: 5px;
  font-size: 15px;
  font-weight: 600;
}

.team-member p {
  color: #8a9a8a;
  margin: 0;
  font-size: 13px;
}
</style>
