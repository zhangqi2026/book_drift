<template>
  <div class="admin-users">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-box slide-in">
        <h1 class="welcome-title">
          <span class="title-glow">信息管理</span>
        </h1>
        <p class="welcome-subtitle">管理系统中的所有用户信息</p>
      </div>
    </div>
    
    <!-- 搜索栏和添加按钮 -->
    <div class="section-box search-section">
      <div class="section-header">
        <h3 class="section-title">搜索与操作</h3>
        <el-button type="primary" icon="el-icon-plus" @click="showAddUserDialog" class="action-btn">添加学生</el-button>
      </div>
      <div class="search-bar-wrapper">
        <div class="search-box">
          <i class="el-icon-search search-icon"></i>
          <el-input
            v-model="searchKeyword"
            placeholder="请输入学生姓名、学号或学院搜索..."
            class="search-input"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-button 
            v-if="searchKeyword" 
            icon="el-icon-refresh-left" 
            @click="resetSearch"
            class="reset-btn"
          ></el-button>
          <el-button 
            type="primary" 
            @click="handleSearch"
            class="search-btn"
          >
            <i class="el-icon-search"></i>
            搜索
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 学生列表 -->
    <div class="section-box">
      <div class="section-header">
        <h3 class="section-title">用户列表</h3>
      </div>
      <el-table
        :data="userList"
        border
        stripe
        class="user-list-table custom-table"
        :empty-text="`暂无学生`"
        v-loading="loading"
      >
        <el-table-column
          label="编号"
          width="70"
          align="center"
        >
          <template slot-scope="scope">
            {{ scope.$index + 1 + (currentPage - 1) * pageSize }}
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          label="姓名"
          width="100"
          align="center"
        />
        <el-table-column
          prop="studentId"
          label="学号"
          width="140"
          align="center"
        />
        <el-table-column
          prop="college"
          label="学院"
          min-width="150"
          align="center"
        />
        <el-table-column
          prop="borrowCount"
          label="借阅次数"
          width="90"
          align="center"
        >
          <template slot-scope="scope">
            {{ scope.row.borrowCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column
          label="用户角色"
          width="100"
          align="center"
        >
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 1 ? 'danger' : 'primary'" size="small" class="custom-tag">
              {{ scope.row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="捐赠次数"
          width="90"
          align="center"
        >
          <template slot-scope="scope">
            {{ getDonationCount(scope.row.id) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="160"
          align="center"
          fixed="right"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              size="mini"
              @click="editUser(scope.row)"
              class="action-btn"
            >
              修改
            </el-button>
            <el-button
              type="danger"
              size="mini"
              @click="deleteUser(scope.row.id)"
              class="action-btn"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
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
    
    <!-- 添加/编辑学生弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="userDialogVisible"
      width="500px"
      @close="closeUserDialog"
      class="custom-dialog"
    >
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="userForm.studentId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-input v-model="userForm.college" placeholder="请输入学院" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="userForm.password" 
            type="password" 
            :placeholder="userForm.id ? '请输入新密码，不修改请留空' : '请输入密码'" 
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveUser" class="action-btn">保存</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AdminUsers',
  data() {
    return {
      userList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchKeyword: '',
      loading: false,
      userDialogVisible: false,
      dialogTitle: '添加学生',
      userForm: {
        id: null,
        name: '',
        studentId: '',
        college: '',
        password: ''
      },
      userRules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
        college: [{ required: true, message: '请输入学院', trigger: 'blur' }]
      },
      userFormRef: null,
      // 捐赠次数映射表
      donationCountMap: {}
    }
  },
  mounted() {
    this.fetchUserList()
  },
  methods: {
    async fetchUserList() {
      try {
        this.loading = true
        const response = await this.$axios.post(`/sysUser/condition/${this.pageSize}/${this.currentPage}`, null, {
          params: {
            name: this.searchKeyword
          }
        })
        
        if (response.code === 20000) {
          this.userList = response.data.records || []
          this.total = response.data.total || 0
          
          // 获取每个用户的借阅次数和捐赠次数
          for (const user of this.userList) {
            // 获取借阅次数（从 BookClaimRecord 表查询所有记录，包括已归还和未归还）
            const borrowResponse = await this.$axios.post(`/bookClaimRecord/condition/1000/1`, null, {
              params: { userId: user.id }
            })
            if (borrowResponse.code === 20000) {
              // 统计所有借阅记录数量
              user.borrowCount = borrowResponse.data.total || 0
            }
            
            // 获取捐赠次数（从 BookInfo 表根据 userId 查询）
            const donateResponse = await this.$axios.post(`/bookInfo/condition/1000/1`, null, {
              params: { donorId: user.id }
            })
            if (donateResponse.code === 20000) {
              const donationCount = donateResponse.data.total || 0
              this.$set(this.donationCountMap, user.id, donationCount)
            }
          }
        }
      } catch (error) {
        this.$message.error('获取用户列表失败：' + (error.response?.data?.message || error.message))
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchUserList()
    },
    resetSearch() {
      this.searchKeyword = ''
      this.currentPage = 1
      this.fetchUserList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchUserList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchUserList()
    },
    showAddUserDialog() {
      this.dialogTitle = '添加学生'
      this.userForm = {
        id: null,
        name: '',
        studentId: '',
        college: '',
        password: ''
      }
      this.userDialogVisible = true
    },
    editUser(user) {
      this.dialogTitle = '修改学生'
      this.userForm = {
        id: user.id,
        name: user.name,
        studentId: user.studentId,
        college: user.college,
        password: ''
      }
      this.userDialogVisible = true
    },
    async saveUser() {
      this.$refs.userFormRef.validate(async valid => {
        if (!valid) return
        
        try {
          this.loading = true
          let response
          
          // 准备提交的数据
          const submitData = {
            id: this.userForm.id,
            name: this.userForm.name,
            studentId: this.userForm.studentId,
            college: this.userForm.college
          }
          
          // 如果是新增用户，必须输入密码
          if (!this.userForm.id) {
            if (!this.userForm.password || this.userForm.password.trim() === '') {
              this.$message.error('新增用户必须设置密码')
              return
            }
            submitData.password = this.userForm.password
            // 添加学生
            response = await this.$axios.post(`/sysUser`, submitData)
            if (response.code === 20000) {
              this.$message.success('学生添加成功')
            }
          } else {
            // 编辑用户 - 只有输入了新密码才更新
            if (this.userForm.password && this.userForm.password.trim() !== '') {
              submitData.password = this.userForm.password
            }
            // 编辑学生
            response = await this.$axios.put(`/sysUser`, submitData)
            if (response.code === 20000) {
              this.$message.success('学生信息更新成功')
            }
          }
          
          this.userDialogVisible = false
          this.fetchUserList()
        } catch (error) {
          this.$message.error('操作失败：' + (error.response?.data?.message || error.message))
        } finally {
          this.loading = false
        }
      })
    },
    async deleteUser(userId) {
      const user = this.userList.find(u => u.id === userId)
      
      this.$confirm(`确定要删除学生"${user ? user.name : ''}"吗？
删除后将同时清除：
- 所有借阅记录
- 所有勋章记录
- 所有读书笔记
- 所有点赞记录
此操作不可恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }).then(async () => {
        try {
          this.loading = true
          const response = await this.$axios.delete(`/sysUser/${userId}`)
          
          if (response.code === 20000) {
            this.$message.success('学生删除成功')
            this.fetchUserList()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          this.$message.error('删除失败：' + (error.response?.data?.message || error.message))
        } finally {
          this.loading = false
        }
      }).catch(() => {})
    },
    closeUserDialog() {
      this.userDialogVisible = false
      this.userForm = {
        id: null,
        name: '',
        studentId: '',
        college: '',
        password: ''
      }
    },
    // 获取用户的捐赠次数
    getDonationCount(userId) {
      return this.donationCountMap[userId] || 0
    }
  }
}
</script>

<style scoped>
.admin-users {
  position: relative;
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
  padding: 20px 24px;
}

.search-bar-wrapper {
  width: 100%;
}

.search-box {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border: 2px solid rgba(171, 240, 209, 0.4);
  border-radius: 16px;
  padding: 8px 16px;
  gap: 12px;
  transition: all 0.3s ease;
}

.search-box:focus-within {
  border-color: rgba(171, 240, 209, 0.8);
  box-shadow: 0 0 0 4px rgba(171, 240, 209, 0.15);
}

.search-icon {
  font-size: 20px;
  color: #6a9a7a;
}

.search-input {
  flex: 1;
}

.search-input >>> .el-input__inner {
  border: none;
  background: transparent;
  font-size: 15px;
  color: #5a6a5a;
  height: 40px;
  padding: 0 12px;
}

.search-input >>> .el-input__inner::placeholder {
  color: #9aaba;
}

.search-input >>> .el-input__inner:focus {
  background: transparent;
  border-color: transparent;
  box-shadow: none;
}

.reset-btn {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(171, 240, 209, 0.4);
  color: #6a9a7a;
  border-radius: 12px;
  padding: 0 16px;
  height: 40px;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: rgba(171, 240, 209, 0.2);
  color: #4a6a5a;
  transform: translateY(-1px);
}

.search-btn {
  background: linear-gradient(135deg, #abf0d1 0%, #d4eea7 100%);
  border: none;
  color: #4a6a5a;
  border-radius: 12px;
  padding: 0 24px;
  height: 40px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(171, 240, 209, 0.4);
}

/* 表格样式 */
.user-list-table {
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

/* 按钮样式 */
.action-btn {
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

/* 标签样式 */
.custom-tag {
  border-radius: 8px;
  font-weight: 500;
}

/* 弹窗样式 */
.custom-dialog >>> .el-dialog__header {
  background: linear-gradient(135deg, rgba(171, 240, 209, 0.15), rgba(212, 238, 167, 0.15));
  border-radius: 4px 4px 0 0;
}

.custom-dialog >>> .el-dialog__title {
  color: #5a6a5a;
  font-weight: 700;
}
</style>
