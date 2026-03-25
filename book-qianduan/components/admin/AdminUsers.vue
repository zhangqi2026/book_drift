<template>
  <div class="admin-users">
    <div class="page-header">
      <h2>信息管理</h2>
      <div>
        <el-button type="primary" icon="el-icon-plus" @click="showAddUserDialog">添加学生</el-button>
      </div>
    </div>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="请输入学生姓名搜索"
        style="width: 300px"
        clearable
        @clear="handleSearch"
      >
        <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
      </el-input>
    </div>
    
    <!-- 学生列表 -->
    <el-table
      :data="userList"
      border
      stripe
      class="user-list-table"
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
          <el-tag :type="scope.row.role === 1 ? 'danger' : 'primary'" size="small">
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
          >
            修改
          </el-button>
          <el-button
            type="danger"
            size="mini"
            @click="deleteUser(scope.row.id)"
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
      class="pagination-box"
    >
    </el-pagination>
    
    <!-- 添加/编辑学生弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="userDialogVisible"
      width="500px"
      @close="closeUserDialog"
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
          <el-button type="primary" @click="saveUser">保存</el-button>
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

.user-list-table {
  margin-bottom: 20px;
}

.pagination-box {
  text-align: right;
}
</style>
