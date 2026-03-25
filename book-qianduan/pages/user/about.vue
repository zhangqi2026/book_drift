<template>
  <div class="about-container">
    <h2>关于我们</h2>
    
    <!-- 系统简介 -->
    <el-card class="about-card">
      <div slot="header" class="card-header">
        <span>系统简介</span>
      </div>
      <div class="card-content">
        <p>校园闲置书籍漂流系统是一个旨在促进校园内书籍资源共享的平台。我们相信每一本书都有其价值，通过这个平台，同学们可以将自己闲置的书籍捐赠出来，让它们在校园内继续"漂流"，发挥更大的价值。</p>
        <p>我们的目标是建立一个可持续发展的书籍共享生态系统，让知识在校园内自由流动，帮助同学们节省购书成本，同时培养大家的分享精神和环保意识。</p>
      </div>
    </el-card>
    
    <!-- 功能介绍 -->
    <el-card class="about-card">
      <div slot="header" class="card-header">
        <span>功能介绍</span>
      </div>
      <div class="card-content">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="feature-item">
              <div class="feature-icon">
                <i class="el-icon-document"></i>
              </div>
              <div class="feature-text">
                <h4>书籍捐赠</h4>
                <p>将闲置书籍捐赠到系统，让它们继续发挥价值</p>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="feature-item">
              <div class="feature-icon">
                <i class="el-icon-reading"></i>
              </div>
              <div class="feature-text">
                <h4>书籍借阅</h4>
                <p>浏览并借阅系统中的书籍，满足学习和阅读需求</p>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="feature-item">
              <div class="feature-icon">
                <i class="el-icon-medal"></i>
              </div>
              <div class="feature-text">
                <h4>成就系统</h4>
                <p>通过捐赠和借阅书籍获得成就勋章，展示你的贡献</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 联系我们 -->
    <el-card class="about-card">
      <div slot="header" class="card-header">
        <span>联系我们</span>
      </div>
      <div class="card-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="contact-item">
              <i class="el-icon-message contact-icon"></i>
              <div class="contact-info">
                <h4>电子邮件</h4>
                <p>support@bookdrift.edu</p>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="contact-item">
              <i class="el-icon-phone contact-icon"></i>
              <div class="contact-info">
                <h4>联系电话</h4>
                <p>400-123-4567</p>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="contact-item">
              <i class="el-icon-location contact-icon"></i>
              <div class="contact-info">
                <h4>办公地址</h4>
                <p>武汉工程大学图书馆一楼服务台</p>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="contact-item">
              <i class="el-icon-time contact-icon"></i>
              <div class="contact-info">
                <h4>服务时间</h4>
                <p>周一至周五 9:00-18:00</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 团队介绍 -->
    <el-card class="about-card">
      <div slot="header" class="card-header">
        <span>开发团队</span>
      </div>
      <div class="card-content">
        <el-row :gutter="20">
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
    </el-card>
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
  }
}
</script>

<style scoped>
.about-container {
  padding: 20px;
}

h2 {
  color: #545c64;
  margin-bottom: 30px;
  text-align: center;
}

.about-card {
  margin-bottom: 30px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  color: #545c64;
}

.card-content p {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 15px;
}

/* 功能介绍样式 */
.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  background-color: #f5f7fa;
  transition: all 0.3s;
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

.feature-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
}

.feature-icon i {
  font-size: 30px;
  color: white;
}

.feature-text {
  text-align: center;
}

.feature-text h4 {
  color: #545c64;
  margin-bottom: 10px;
}

.feature-text p {
  color: #909399;
  margin: 0;
}

/* 联系我们样式 */
.contact-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  background-color: #f5f7fa;
  margin-bottom: 15px;
}

.contact-icon {
  font-size: 32px;
  color: #409EFF;
  margin-right: 15px;
}

.contact-info h4 {
  color: #545c64;
  margin: 0 0 5px 0;
}

.contact-info p {
  color: #909399;
  margin: 0;
}

/* 团队介绍样式 */
.team-member {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background-color: #f5f7fa;
  transition: all 0.3s;
}

.team-member:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

.member-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #409EFF;
  margin: 0 auto 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder {
  color: white;
  font-size: 36px;
  font-weight: bold;
}

.team-member h4 {
  color: #545c64;
  margin-bottom: 5px;
}

.team-member p {
  color: #909399;
  margin: 0;
}
</style>
