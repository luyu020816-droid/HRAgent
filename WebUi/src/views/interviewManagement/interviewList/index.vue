<template>
  <div class="interview-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-content">
        <h1>面试流转</h1>
        <p>管理和查看面试流程信息</p>
      </div>
    </div>

    <!-- 查询表单 -->
    <div class="query-form">
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

          <el-form-item label="面试状态">
            <el-select
              v-model="queryForm.state"
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
              <div class="stat-number">{{ statistics.oneIt + statistics.twoIt + statistics.threeIt }}</div>
              <div class="stat-label">总面试数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ statistics.oneIt }}</div>
              <div class="stat-label">一面中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ statistics.twoIt }}</div>
              <div class="stat-label">二面中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ statistics.threeIt }}</div>
              <div class="stat-label">三面中</div>
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

      <el-table-column label="年龄" prop="age" width="60"/>

      <el-table-column label="工作经验(年)" prop="workAge" width="100"/>

      <el-table-column label="联系电话" prop="phone" width="130"/>

      <el-table-column label="面试状态" width="80">
        <template #default="{ row }">
          <el-tag
            :type="getStatusType(row.state)"
            size="small"
          >
            {{ getStatusLabel(row.state) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="面试官" prop="interviewPerson" width="120"/>

      <el-table-column label="面试时间" width="150">
        <template #default="{ row }">
          {{ formatDateTime(row.interviewTime) }}
        </template>
      </el-table-column>


      <el-table-column label="操作" width="380" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button
              type="text"
              size="small"
              icon="el-icon-view"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              type="text"
              size="small"
              icon="el-icon-edit"
              @click="handleEdit(row)"
            >
              添加面试信息
            </el-button>
            <el-button
              type="text"
              size="small"
              icon="el-icon-edit-outline"
              @click="handleEvaluate(row)"
            >
              面试评价
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

    <!-- 新增/编辑弹窗 -->
    <interview-edit
      ref="editDialog"
      @refresh="fetchData"
    />

    <!-- 面试评价弹窗 -->
    <interview-evaluate
      ref="evaluateDialog"
      @refresh="fetchData"
    />

    <!-- 查看详情弹窗 -->
    <interview-preview
      ref="previewDialog"
    />
  </div>
</template>

<script>
import {mapState, mapActions, mapGetters} from 'vuex'
import InterviewEdit from './components/InterviewEdit'
import InterviewEvaluate from './components/InterviewEvaluate'
import InterviewPreview from './components/InterviewPreview'
import {getInterviewListByRId} from '@/api/interview'

export default {
  name: 'InterviewList',
  components: {
    InterviewEdit,
    InterviewEvaluate,
    InterviewPreview
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
    ...mapState('interview', [
      'list',
      'total',
      'loading',
      'searchForm',
      'statusOptions',
      'statistics'
    ]),
    ...mapGetters('interview', [
      'statusOptionsMap',
      'roundOptionsMap'
    ])
  },
  async created() {
    await this.initializeData()
  },
  methods: {
    ...mapActions('interview', [
      'fetchList',
      'fetchStatistics',
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

    getInterviewListByRId,

    // 查看详情
    async handleView(row) {
      // 根据 rid 查询面试列表
      const result = await this.getInterviewListByRId(row.id);
      row.interviews = result.data;
      this.$refs.previewDialog.open(row)
    },

    // 编辑
    handleEdit(row) {
      this.$refs.editDialog.open('edit', row)
    },

    // 面试评价
    handleEvaluate(row) {
      this.$refs.evaluateDialog.open(row)
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
    },

    // 获取面试轮次标签
    getRoundLabel(round) {
      const roundInfo = this.roundOptionsMap[round]
      return roundInfo?.label || round
    }
  }
}
</script>

<style lang="scss" scoped>
.interview-list {
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
