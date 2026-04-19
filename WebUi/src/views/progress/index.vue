<template>
  <div class="recruitment-progress-container">
    <vab-page-header
      description="面试进度查询：根据姓名、手机、邮箱查询候选人面试进度"
      title="面试进度查询"
    />

    <div class="query-form">
      <div class="query-form-right">
        <el-form :model="queryForm" inline @submit.native.prevent="queryData">
          <el-form-item label="候选人姓名">
            <el-input
              v-model.trim="queryForm.name"
              clearable
              placeholder="请输入候选人姓名"
              style="width: 150px"
            />
          </el-form-item>

          <el-form-item label="联系电话">
            <el-input
              v-model.trim="queryForm.phone"
              clearable
              placeholder="请输入联系电话"
              style="width: 150px"
            />
          </el-form-item>

          <el-form-item label="邮箱">
            <el-input
              v-model.trim="queryForm.email"
              clearable
              placeholder="请输入邮箱"
              style="width: 200px"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" native-type="submit">
              查询
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      :element-loading-text="elementLoadingText"
      class="result-table"
      style="width: 100%"
    >
      <el-table-column label="候选人姓名" prop="name" show-overflow-tooltip min-width="120"/>
      <el-table-column label="联系电话" prop="phone" show-overflow-tooltip min-width="120"/>
      <el-table-column label="邮箱" prop="email" show-overflow-tooltip min-width="200"/>
      <el-table-column label="年龄" prop="age" show-overflow-tooltip min-width="80" align="center"/>
      <el-table-column label="工作经验" prop="workAge" show-overflow-tooltip min-width="100" align="center">
        <template #default="{ row }">
          <span>{{ row.workAge }}年</span>
        </template>
      </el-table-column>
      <el-table-column label="学历" prop="edu" show-overflow-tooltip min-width="120" align="center"/>
      <el-table-column label="面试状态" show-overflow-tooltip min-width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.state)" size="small">
            {{ getStatusLabel(row.state) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" show-overflow-tooltip min-width="150" align="center"/>
    </el-table>

    <el-pagination
      background
      :current-page="queryForm.pageNo"
      :layout="layout"
      :page-size="queryForm.pageSize"
      :total="total"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script>
import {getList} from '@/api/progress'
import VabPageHeader from '@/components/VabPageHeader'

export default {
  name: 'Progress',
  components: {
    VabPageHeader,
  },
  data() {
    return {
      list: [],
      listLoading: true,
      layout: 'total, sizes, prev, pager, next, jumper',
      total: 0,
      elementLoadingText: '正在加载...',
      queryForm: {
        pageNo: 1,
        pageSize: 10,
        name: '',
        phone: '',
        email: '',
      },
      timeOutID: null,
    }
  },
  created() {
    this.fetchData()
  },
  beforeDestroy() {
    clearTimeout(this.timeOutID)
  },
  methods: {
    // 获取状态类型
    getStatusType(state) {
      const statusMap = {
        1: '',
        2: 'success',
        3: 'success',
        4: 'success',
        5: 'success',
        6: 'success'
      }
      return statusMap[state] || 'info'
    },

    // 获取状态标签
    getStatusLabel(state) {
      const statusLabelMap = {
        1: '待面试',
        2: '一面',
        3: '二面',
        4: '三面',
        5: '待发Offer',
        6: '已发Offer',
        '-1': '简历未通过筛选',
        '-2': '面试未通过',
        '-3': '拒绝Offer',
      }
      return statusLabelMap[state] || state
    },

    // 获取面试轮次标签
    getRoundLabel(round) {
      const roundLabelMap = {
        2: '一面',
        3: '二面',
        4: '三面'
      }
      return roundLabelMap[round] || round
    },

    handleSizeChange(val) {
      this.queryForm.pageSize = val
      this.fetchData()
    },

    handleCurrentChange(val) {
      this.queryForm.pageNo = val
      this.fetchData()
    },

    queryData() {
      this.queryForm.pageNo = 1
      this.fetchData()
    },

    async fetchData() {
      this.listLoading = true
      try {
        const result = await getList(this.queryForm)
        this.list = result.data.records
        this.total = result.totalCount
      } catch (error) {
        this.$baseMessage('获取数据失败', 'error')
      }
      this.timeOutID = setTimeout(() => {
        this.listLoading = false
      }, 300)
    },
  },
}
</script>

<style lang="scss" scoped>
.recruitment-progress-container {
  padding: 20px;

  .query-form {
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

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

  .result-table {
    margin-top: 20px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    ::v-deep .el-table__header th {
      background-color: #f5f7fa;
      color: #606266;
      font-weight: 600;
    }

    ::v-deep .el-table__row:hover {
      background-color: #f0f9ff;
    }

    ::v-deep .el-table__body td {
      padding: 8px 0;
    }
  }

  ::v-deep .el-pagination {
    margin-top: 20px;
    text-align: center;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .recruitment-progress-container {
    .query-form {
      .query-form-right {
        .el-form {
          flex-direction: column;
          align-items: flex-start;
        }

        .el-form-item {
          margin-right: 0;
          margin-bottom: 10px;
          width: 100%;

          &:last-child {
            margin-bottom: 0;
            align-self: flex-end;
          }
        }
      }
    }
  }
}
</style>
