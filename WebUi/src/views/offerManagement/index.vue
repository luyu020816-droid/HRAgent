<template>
  <div class="offerManagement-container">
    <vab-page-header description="Offer管理：支持发送、撤回Offer"
                     title="Offer管理"/>

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

          <!--          <el-form-item label="职位">-->
          <!--            <el-select-->
          <!--              v-model="queryForm.jobTitle"-->
          <!--              clearable-->
          <!--              placeholder="请选择职位"-->
          <!--              style="width: 120px"-->
          <!--            >-->
          <!--              <el-option v-for="job in jobOptions" :key="job.value" :label="job.label" :value="job.value"/>-->
          <!--            </el-select>-->
          <!--          </el-form-item>-->

          <el-form-item label="状态">
            <el-select
              v-model="queryForm.status"
              clearable
              placeholder="请选择状态"
              style="width: 120px"
            >
              <el-option label="待发Offer" value="5"/>
              <el-option label="已发Offer" value="6"/>
              <el-option label="已拒绝" value="-3"/>
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" native-type="submit">
              查询
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <el-table v-loading="listLoading" :data="list" :element-loading-text="elementLoadingText"
              @selection-change="setSelectRows">
      <el-table-column show-overflow-tooltip type="selection"/>
      <el-table-column label="候选人姓名" prop="name" show-overflow-tooltip width="120"/>
      <el-table-column label="邮箱" prop="email" show-overflow-tooltip width="180"/>
      <el-table-column label="职位" prop="position" show-overflow-tooltip width="120"/>
      <el-table-column label="部门" prop="department" show-overflow-tooltip width="100"/>
      <el-table-column label="薪资(元)" show-overflow-tooltip width="100">
        <template #default="{ row }">
          {{ row.salary }}
        </template>
      </el-table-column>
      <el-table-column label="工作地点" prop="workLocation" show-overflow-tooltip width="100"/>
      <el-table-column label="入职日期" prop="onboardDate" show-overflow-tooltip width="110"/>
      <el-table-column label="HR" prop="hrName" show-overflow-tooltip width="100"/>
      <el-table-column label="状态" show-overflow-tooltip width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatus(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createdTime" show-overflow-tooltip width="150"/>
      <el-table-column label="操作" fixed="right" show-overflow-tooltip width="200">
        <template #default="{ row }">
          <el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 5" size="mini" type="text" @click="handleSendEmail(row)">发送
          </el-button>
          <el-button v-if="row.status === 6" size="mini" type="text" @click="handleWithdraw(row)">撤回
          </el-button>
          <!--          <el-button size="mini" type="text" @click="handleDelete(row)">删除</el-button>-->
        </template>
      </el-table-column>
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
    <edit ref="edit" @fetch-data="fetchData"/>
  </div>
</template>

<script>
import {
  getOfferList,
  deleteOffer,
  batchDeleteOffer,
  sendOfferEmail,
  withdrawOffer
} from '@/api/offerManagement'
import Edit from './components/OfferEdit'
import VabPageHeader from '@/components/VabPageHeader'

export default {
  name: 'OfferManagement',
  components: {
    Edit,
    VabPageHeader,
  },
  data() {
    return {
      list: null,
      listLoading: true,
      layout: 'total, sizes, prev, pager, next, jumper',
      total: 0,
      selectRows: '',
      elementLoadingText: '正在加载...',
      queryForm: {
        pageNo: 1,
        pageSize: 10,
        name: '',
        jobTitle: '',
        status: '',
      },
      jobOptions: [],
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
    getStatus(state) {
      const statusMap = {
        '-3': '已拒绝',
        '5': '待发Offer',
        '6': '已发Offer'
      }
      return statusMap[state] || ''
    },
    getStatusType(state) {
      const statusMap = {
        '-3': '',
        '5': 'warning',
        '6': 'success'
      }
      return statusMap[state] || ''
    },
    setSelectRows(val) {
      this.selectRows = val
    },
    handleEdit(row) {
      this.$refs['edit'].showEdit(row)
    },
    handleDelete(row) {
      if (row.id) {
        this.$baseConfirm('你确定要删除当前Offer吗', null, async () => {
          const {msg} = await deleteOffer(row.id)
          this.$baseMessage(msg, 'success')
          this.fetchData()
        })
      } else {
        if (this.selectRows.length > 0) {
          const ids = this.selectRows.map((item) => item.id)
          this.$baseConfirm('你确定要删除选中的Offer吗', null, async () => {
            const {msg} = await batchDeleteOffer(ids)
            this.$baseMessage(msg, 'success')
            this.fetchData()
          })
        } else {
          this.$baseMessage('未选中任何行', 'error')
          return false
        }
      }
    },
    handleSendEmail(row) {
      this.$baseConfirm('确定要发送此Offer邮件吗？', null, async () => {
        const {msg} = await sendOfferEmail(row.id)
        this.$baseMessage(msg, 'success')
        this.fetchData()
      })
    },
    handleWithdraw(row) {
      this.$baseConfirm('确定要撤回此Offer吗？', null, async () => {
        const {msg} = await withdrawOffer(row.id)
        this.$baseMessage(msg, 'success')
        this.fetchData()
      })
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
        const result = await getOfferList(this.queryForm)
        this.list = result.data.records
        this.total = result.data.totalCount
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
.offerManagement-container {
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
}
</style>
