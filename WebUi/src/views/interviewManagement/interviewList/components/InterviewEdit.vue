<template>
  <el-dialog
    title="添加面试信息"
    :visible.sync="visible"
    width="600px"
    :before-close="handleClose"
    @close="handleClose"
  >
    <el-form
      ref="form"
      :model="form"
      :rules="rules"
      label-width="100px"
      size="small"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="候选人姓名">
            <span>{{ form.name }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话">
            <span>{{ form.phone }}</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="邮箱地址">
            <span>{{ form.email }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="面试轮次">
            <span>{{ getRoundLabel(form.state) }}</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="年龄">
            <span>{{ form.age }} 岁</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工作经验">
            <span>{{ form.workAge }} 年</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="学历">
            <span>{{ form.edu }}</span>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider></el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="面试官" prop="interviewPerson">
            <el-input
              v-model="form.interviewPerson"
              placeholder="请输入面试官姓名"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="面试时间" prop="interviewTime">
            <el-date-picker
              v-model="form.interviewTime"
              type="datetime"
              placeholder="请选择面试时间"
              style="width: 100%"
              value-format="yyyy-MM-dd HH:mm:ss"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注信息" prop="desc">
        <el-input
          v-model="form.desc"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        保存
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import {mapState, mapActions, mapGetters} from 'vuex'

export default {
  name: 'InterviewEdit',
  data() {
    return {
      visible: false,
      loading: false,

      // 表单数据
      form: {
        id: '',
        name: '',
        phone: '',
        email: '',
        age: null,
        interviewRound: 1,
        experience: 0,
        edu: '',
        interviewerPerson: '',
        interviewerTime: '',
        state: 2,
        desc: ''
      },

      // 表单验证规则
      rules: {
        interviewerPerson: [
          {required: true, message: '请输入面试官姓名', trigger: 'blur'}
        ],
        interviewerTime: [
          {required: true, message: '请选择面试时间', trigger: 'change'}
        ]
      }
    }
  },
  computed: {
    ...mapState('interview', [
      'stateOptions',
      'roundOptions',
      'eduOptions'
    ]),
    ...mapGetters('interview', [
      'roundOptionsMap'
    ])
  },
  methods: {
    ...mapActions('interview', [
      'createRecord'
    ]),

    // 获取轮次标签
    getRoundLabel(round) {
      const roundInfo = this.roundOptionsMap[round]
      return roundInfo?.label || round
    },

    // 打开弹窗
    open(mode, record = null) {
      this.visible = true

      if (record) {
        this.form = {...record}
      } else {
        this.resetForm()
      }

      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },

    // 重置表单
    resetForm() {
      this.form = {
        id: '',
        name: '',
        phone: '',
        email: '',
        age: null,
        interviewRound: 1,
        experience: 0,
        edu: '',
        interviewerPerson: '',
        interviewerTime: '',
        state: 2,
        desc: ''
      }
    },

    // 关闭弹窗
    handleClose() {
      this.visible = false
      this.loading = false
      this.resetForm()
    },

    // 提交表单
    async handleSubmit() {
      try {
        await this.$refs.form.validate()

        this.loading = true

        // 只提交面试官、面试时间和备注信息
        const formData = {
          rid: this.form.id,
          interviewPerson: this.form.interviewPerson,
          interviewTime: this.form.interviewTime,
          desc: this.form.desc
        }

        await this.createRecord(formData)
        this.$message.success('保存成功')

        this.handleClose()
        this.$emit('refresh')
      } catch (error) {
        if (error !== false) { // 非表单验证错误
          console.error('提交失败:', error)
          this.$message.error('保存失败')
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.dialog-footer {
  text-align: right;
}
</style>
