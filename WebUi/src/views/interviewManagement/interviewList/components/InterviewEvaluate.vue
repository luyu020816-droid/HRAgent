<template>
  <el-dialog
    title="面试评价"
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
          <el-form-item label="候选人">
            <span>{{ form.name }}</span>
          </el-form-item>
        </el-col>
        <!--        <el-col :span="12">-->
        <!--          <el-form-item label="面试轮次">-->
        <!--            <span>{{ getRoundLabel(form.state) }}</span>-->
        <!--          </el-form-item>-->
        <!--        </el-col>-->
      </el-row>
      <el-divider></el-divider>
      <el-form-item label="专业技能" prop="professionalScore">
        <el-rate
          v-model="form.professionalScore"
          :max="5"
          :allow-half="false"
          show-score
          score-template="{value}分"
          style="margin-top: 8px;"
        />
      </el-form-item>

      <el-form-item label="沟通能力" prop="communicationScore">
        <el-rate
          v-model="form.communicationScore"
          :max="5"
          :allow-half="false"
          show-score
          score-template="{value}分"
          style="margin-top: 8px;"
        />
      </el-form-item>

      <el-form-item label="团队协作" prop="teamworkScore">
        <el-rate
          v-model="form.teamworkScore"
          :max="5"
          :allow-half="false"
          show-score
          score-template="{value}分"
          style="margin-top: 8px;"
        />
      </el-form-item>

      <el-form-item label="综合素质" prop="comprehensiveScore">
        <el-rate
          v-model="form.comprehensiveScore"
          :max="5"
          :allow-half="false"
          show-score
          score-template="{value}分"
          style="margin-top: 8px;"
        />
      </el-form-item>

      <el-form-item label="评价内容" prop="evaluate">
        <el-input
          v-model="form.evaluate"
          type="textarea"
          :rows="4"
          placeholder="请输入面试评价内容"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="面试结果" prop="result">
        <el-radio-group v-model="form.result">
          <el-radio label="PASSED">通过</el-radio>
          <el-radio label="FAILED">不通过</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        提交评价
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import {mapActions, mapGetters} from 'vuex'

export default {
  name: 'InterviewEvaluate',
  data() {
    return {
      visible: false,
      loading: false,

      // 表单数据
      form: {
        id: '',
        name: '',
        state: 2,
        interviewPerson: '',
        interviewTime: '',
        skillsScore: 0,
        communicationScore: 0,
        teamworkScore: 0,
        overallScore: 0,
        evaluationContent: '',
        result: 'PASSED'
      },

      // 表单验证规则
      rules: {
        skillsScore: [
          {required: true, message: '请评价专业技能', trigger: 'change'}
        ],
        communicationScore: [
          {required: true, message: '请评价沟通能力', trigger: 'change'}
        ],
        teamworkScore: [
          {required: true, message: '请评价团队协作', trigger: 'change'}
        ],
        overallScore: [
          {required: true, message: '请评价综合素质', trigger: 'change'}
        ],
        evaluationContent: [
          {required: true, message: '请输入评价内容', trigger: 'blur'}
        ],
        result: [
          {required: true, message: '请选择面试结果', trigger: 'change'}
        ]
      }
    }
  },
  computed: {
    ...mapGetters('interview', [
      'roundOptionsMap'
    ])
  },
  methods: {
    ...mapActions('interview', [
      'addEvaluation'
    ]),

    // 打开弹窗
    open(record) {
      this.visible = true
      this.form = {
        id: record.id,
        name: record.name,
        interviewRound: record.interviewRound,
        interviewer: record.interviewer,
        interviewDate: record.interviewDate,
        skillsScore: 0,
        communicationScore: 0,
        teamworkScore: 0,
        overallScore: 0,
        evaluationContent: '',
        result: 'PASSED'
      }

      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },

    // 关闭弹窗
    handleClose() {
      this.visible = false
      this.loading = false
    },

    // 获取轮次标签
    getRoundLabel(round) {
      const roundInfo = this.roundOptionsMap[round]
      return roundInfo?.label || round
    },

    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    },

    // 提交表单
    async handleSubmit() {
      try {
        await this.$refs.form.validate()

        this.loading = true

        const formData = {...this.form}

        await this.addEvaluation(formData)
        this.$message.success('评价提交成功')

        this.handleClose()
        this.$emit('refresh')
      } catch (error) {
        if (error !== false) { // 非表单验证错误
          console.error('提交失败:', error)
          this.$message.error('提交失败')
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

::v-deep .el-rate {
  display: flex;
  align-items: center;
}
</style>
