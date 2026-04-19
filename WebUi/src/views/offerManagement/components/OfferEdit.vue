<template>
  <el-dialog
    :close-on-click-modal="false"
    :title="title"
    :visible.sync="dialogFormVisible"
    width="800px"
    @close="close"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="120px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="候选人姓名">
            <el-input v-model.trim="form.name" disabled/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系邮箱">
            <el-input v-model.trim="form.email" disabled/>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="职位">
            <el-input v-model.trim="form.position"/>
          </el-form-item>
          <!--          <el-form-item label="职位" prop="jobTitle">-->
          <!--            <el-input v-model.trim="form.phone" disabled/>-->
          <!--            <el-select v-model="form.jobTitle" placeholder="请选择职位" style="width: 100%">-->
          <!--              <el-option v-for="job in jobOptions" :key="job.value" :label="job.label" :value="job.value"/>-->
          <!--            </el-select>-->
          <!--          </el-form-item>-->
        </el-col>
        <el-col :span="12">
          <el-form-item label="部门" prop="department">
            <el-select v-model="form.department" placeholder="请选择部门" style="width: 100%">
              <el-option label="技术部" value="技术部"/>
              <el-option label="产品部" value="产品部"/>
              <el-option label="设计部" value="设计部"/>
              <el-option label="市场部" value="市场部"/>
              <el-option label="运营部" value="运营部"/>
              <el-option label="人事部" value="人事部"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="薪资(元)" prop="salary">
            <el-input v-model.number="form.salary" placeholder="请输入薪资"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工作地点" prop="workLocation">
            <el-input v-model.trim="form.workLocation" placeholder="请输入工作地点"/>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="入职日期" prop="onboardDate">
            <el-date-picker
              v-model="form.onboardDate"
              format="yyyy-MM-dd"
              placeholder="请选择入职日期"
              style="width: 100%"
              type="date"
              value-format="yyyy-MM-dd"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责HR" prop="hrName">
            <el-input v-model.trim="form.hrName" placeholder="请输入负责HR"/>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model.trim="form.remark"
              :autosize="{ minRows: 2, maxRows: 4 }"
              placeholder="请输入备注"
              type="textarea"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="close">取 消</el-button>
      <el-button type="primary" @click="save">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {updateOffer, getJobOptions} from '@/api/offerManagement'

export default {
  name: 'OfferEdit',
  data() {
    return {
      dialogFormVisible: false,
      title: '编辑Offer',
      form: {
        id: '',
        name: '',
        email: '',
        phone: '',
        age: null,
        jobTitle: '',
        department: '',
        salary: '',
        workLocation: '',
        startDate: '',
        hrName: '',
        status: '',
        remark: '',
      },
      rules: {
        jobTitle: [{required: true, message: '请选择职位', trigger: 'change'}],
        department: [{required: true, message: '请选择部门', trigger: 'change'}],
        salary: [
          {required: true, message: '请输入薪资', trigger: 'blur'},
          {type: 'number', message: '薪资必须为数字值', trigger: 'blur'}
        ],
        workLocation: [{required: true, message: '请输入工作地点', trigger: 'blur'}],
        startDate: [{required: true, message: '请选择入职日期', trigger: 'change'}],
        hrName: [{required: true, message: '请输入负责HR', trigger: 'blur'}],
        state: [{required: true, message: '请选择状态', trigger: 'change'}],
        age: [
          {required: true, message: '请输入年龄', trigger: 'blur'},
          {type: 'number', message: '年龄必须为数字值', trigger: 'blur'}
        ]
      },
      jobOptions: [],
    }
  },
  created() {
    // this.getJobOptions()
  },
  methods: {
    async getJobOptions() {
      try {
        const {data} = await getJobOptions()
        this.jobOptions = data
      } catch (error) {
        console.error('获取职位选项失败:', error)
      }
    },
    showEdit(row) {
      if (!row) {
        this.title = '添加Offer'
      } else {
        this.title = '编辑Offer'
        this.form = Object.assign({}, row)
      }
      this.dialogFormVisible = true
    },
    close() {
      this.$refs['form'].resetFields()
      this.form = this.$options.data().form
      this.dialogFormVisible = false
    },
    save() {
      this.$refs['form'].validate(async (valid) => {
        if (valid) {
          try {
            const {msg} = await updateOffer(this.form)
            this.$baseMessage(msg, 'success')
            this.$emit('fetch-data')
            this.close()
          } catch (error) {
            console.error('保存失败:', error)
            this.$baseMessage('保存失败', 'error')
          }
        }
      })
    },
  },
}
</script>

<style lang="scss" scoped>
.input-with-unit {
  display: flex;
  align-items: center;
  width: 100%;
}

.unit-text {
  margin-left: 8px;
  color: #999;
  white-space: nowrap;
}

::v-deep .el-input-number {
  flex: 1;
}
</style>
