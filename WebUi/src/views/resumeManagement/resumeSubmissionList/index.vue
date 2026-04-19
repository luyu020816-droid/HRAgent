<template>
  <div class="resume-submission-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-content">
        <h1>简历筛选</h1>
        <p>管理和查看求职者的简历投递信息</p>
      </div>
      <div class="page-header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleCreate">
          新增投递记录
        </el-button>
      </div>
    </div>

    <!-- 查询表单 -->
    <div class="query-form">
      <div class="query-form-left">
        <el-button
          type="primary"
          icon="el-icon-plus"
          @click="handleCreate"
        >
          添加记录
        </el-button>
        <el-button
          type="danger"
          icon="el-icon-delete"
          :disabled="!selectedRows.length"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </div>

      <div class="query-form-right">
        <el-form :model="queryForm" inline @submit.native.prevent="handleQuery">
          <el-form-item label="候选人姓名">
            <el-input
              v-model="queryForm.name"
              placeholder="请输入候选人姓名"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="投递状态">
            <el-select
              v-model="queryForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="status in statusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" native-type="submit">
              查询
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ statistics.total }}</div>
              <div class="stat-label">总投递数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ statistics.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ statistics.rejected }}</div>
              <div class="stat-label">简历未通过</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ statistics.interviewScheduled }}</div>
              <div class="stat-label">面试中</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <el-table
      ref="table"
      v-loading="loading"
      :data="list"
      border
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50"/>

      <el-table-column label="候选人姓名" prop="name" width="120"/>

      <el-table-column label="联系电话" prop="phone" width="150"/>

      <el-table-column label="邮箱地址" prop="email" width="250"/>

      <el-table-column label="工作经验" prop="workAge" width="120"/>

      <el-table-column label="年龄" prop="age" width="80">
        <template #default="{ row }">
          <!-- 年龄字段目前mock数据中没有，暂时显示为 '-' -->
          {{ row.age || '-' }}
        </template>
      </el-table-column>

      <el-table-column label="投递时间" prop="createTime" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>


      <el-table-column label="投递状态" prop="status" width="120">
        <template #default="{ row }">
          <el-tag
            :type="getStatusType(row.state)"
            size="small"
          >
            {{ getStatusLabel(row.state) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="简历文件" width="100">
        <template #default="{ row }">
          <el-button
            type="text"
            size="small"
            icon="el-icon-download"
            @click="handleDownloadResume(row)"
          >
            下载
          </el-button>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button
              type="text"
              size="small"
              icon="el-icon-edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-dropdown trigger="click" @command="(command) => handleStatusChange(row, command)">
              <el-button type="text" size="small">
                状态<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  v-for="status in statusOptions"
                  :key="status.value"
                  :command="status.value"
                  :disabled="row.status === status.value"
                >
                  {{ status.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <el-button
              type="text"
              size="small"
              icon="el-icon-delete"
              style="color: #f56c6c"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      style="margin-top: 20px; text-align: center"
      :current-page="searchForm.pageNo"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="searchForm.pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 编辑弹窗 -->
    <resume-submission-edit
      ref="editDialog"
      @refresh="fetchData"
    />
  </div>
</template>

<script>
import {mapState, mapActions, mapGetters} from 'vuex'
import ResumeSubmissionEdit from './components/ResumeSubmissionEdit'
import {downloadResume} from '@/api/resumeSubmission'

export default {
  name: 'ResumeSubmissionList',
  components: {
    ResumeSubmissionEdit
  },
  data() {
    return {
      // 查询表单
      queryForm: {
        name: '',
        status: ''
      },

      // 选中的行
      selectedRows: []
    }
  },
  computed: {
    ...mapState('resumeSubmission', [
      'list',
      'total',
      'loading',
      'searchForm',
      'statusOptions',
      'statistics'
    ]),
    ...mapGetters('resumeSubmission', [
      'statusOptionsMap'
    ])
  },
  async created() {
    await this.initializeData()
  },
  methods: {
    ...mapActions('resumeSubmission', [
      'fetchList',
      'fetchStatistics',
      'deleteRecord',
      'batchDeleteRecords',
      'updateRecord',
      'handleSearch',
      'handleResetSearch',
      'handlePageChange'
    ]),

    // 初始化页面数据
    async initializeData() {
      try {
        // 加载列表数据和统计数据
        await this.fetchData()
      } catch (error) {
        console.error('初始化数据失败:', error)
        this.$message.error('初始化数据失败')
      }
    },

    // 获取列表数据
    async fetchData() {
      try {
        await this.fetchList()
        await this.fetchStatistics()
      } catch (error) {
        console.error('获取数据失败:', error)
        this.$message.error('获取数据失败')
      }
    },

    // 查询
    async handleQuery() {
      try {
        await this.handleSearch(this.queryForm)
      } catch (error) {
        this.$message.error('查询失败')
      }
    },

    // 重置查询条件
    async handleReset() {
      this.queryForm = {
        name: '',
        status: ''
      }
      try {
        await this.handleResetSearch()
      } catch (error) {
        this.$message.error('重置失败')
      }
    },

    // 分页大小改变
    async handleSizeChange(pageSize) {
      try {
        await this.handlePageChange({
          pageNo: 1,
          pageSize
        })
      } catch (error) {
        this.$message.error('分页操作失败')
      }
    },

    // 当前页改变
    async handleCurrentChange(pageNo) {
      try {
        await this.handlePageChange({
          pageNo,
          pageSize: this.searchForm.pageSize
        })
      } catch (error) {
        this.$message.error('分页操作失败')
      }
    },

    // 表格选择改变
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    // 新增
    handleCreate() {
      this.$refs.editDialog.open('create')
    },

    // 编辑
    handleEdit(row) {
      this.$refs.editDialog.open('edit', row)
    },

    // 删除
    handleDelete(row) {
      this.$confirm(`确认删除候选人"${row.name}"的投递记录吗？`, '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await this.deleteRecord(row.id)
          this.$message.success('删除成功')
          await this.fetchStatistics()
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {
      })
    },

    // 批量删除
    handleBatchDelete() {
      if (!this.selectedRows.length) {
        this.$message.warning('请选择要删除的记录')
        return
      }

      this.$confirm(`确认删除选中的${this.selectedRows.length}条记录吗？`, '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const ids = this.selectedRows.map(row => row.id)
          await this.batchDeleteRecords(ids)
          this.$message.success('批量删除成功')
          this.selectedRows = []
          await this.fetchStatistics()
        } catch (error) {
          this.$message.error('批量删除失败')
        }
      }).catch(() => {
      })
    },

    // 状态改变
    async handleStatusChange(row, state) {
      try {
        await this.updateRecord({
          ...row,
          state
        })
        this.$message.success('状态更新成功')
        await this.fetchStatistics()
        await this.fetchList()
      } catch (error) {
        this.$message.error('状态更新失败')
      }
    },

    // 下载简历
    async handleDownloadResume(row) {
      try {
        const response = await downloadResume(row.id)
        const url = window.URL.createObjectURL(new Blob([response]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', row.resumeFileName || `${row.name}_简历.pdf`)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success('下载成功')
      } catch (error) {
        this.$message.error('下载失败')
      }
    },


    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    // 获取状态类型
    getStatusType(status) {
      const statusInfo = this.statusOptionsMap[status]
      return statusInfo?.type || 'info'
    },

    // 获取状态标签
    getStatusLabel(status) {
      const statusInfo = this.statusOptionsMap[status]
      return statusInfo?.label || status
    }
  }
}
</script>

<style lang="scss" scoped>
.resume-submission-list {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .page-header-content {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        font-weight: 600;
        color: #fff;
      }

      p {
        margin: 0;
        color: #fff;
        font-size: 14px;
      }
    }
  }

  .query-form {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .query-form-left {
      display: flex;
      gap: 12px;
    }

    .query-form-right {
      .el-form {
        margin: 0;
        display: flex;
        align-items: center;
      }

      .el-form-item {
        margin-bottom: 0;
        margin-right: 16px;

        &:last-child {
          margin-right: 0;
        }
      }
    }
  }

  .statistics-cards {
    .stat-card {
      text-align: center;

      .stat-item {
        .stat-number {
          font-size: 24px;
          font-weight: bold;
          color: #409eff;
          margin-bottom: 8px;
        }

        .stat-label {
          font-size: 14px;
          color: #666;
        }
      }
    }
  }

  .action-buttons {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    gap: 5px;
  }
}
</style>
