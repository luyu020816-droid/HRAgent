<template>
  <el-dialog
    title="面试详情"
    :visible.sync="visible"
    width="800px"
    :before-close="handleClose"
  >
    <el-card v-if="record" class="preview-card">
      <div slot="header" class="clearfix">
        <span>基本信息</span>
      </div>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">候选人姓名：</span>
            <span class="info-value">{{ record.name }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">联系电话：</span>
            <span class="info-value">{{ record.phone }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">邮箱地址：</span>
            <span class="info-value">{{ record.email }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">年龄：</span>
            <span class="info-value">{{ record.age }} 岁</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">工作经验：</span>
            <span class="info-value">{{ record.workAge }} 年</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">学历：</span>
            <span class="info-value">{{ record.edu }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">面试轮次：</span>
            <span class="info-value">{{ getRoundLabel(record.state) }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">面试官：</span>
            <span class="info-value">{{ record.interviewPerson }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-item">
            <span class="info-label">面试时间：</span>
            <span class="info-value">{{ formatDateTime(record.interviewTime) }}</span>
          </div>
        </el-col>
        <!--        <el-col :span="12">-->
        <!--          <div class="info-item">-->
        <!--            <span class="info-label">面试状态：</span>-->
        <!--            <span class="info-value">-->
        <!--              <el-tag :type="getStatusType(record.status)">-->
        <!--                {{ getStatusLabel(record.status) }}-->
        <!--              </el-tag>-->
        <!--            </span>-->
        <!--          </div>-->
        <!--        </el-col>-->
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <div class="info-item">
            <span class="info-label">备注信息：</span>
            <span class="info-value">{{ record.desc || '-' }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card v-if="record && record.interviews && record.interviews.length > 0" class="preview-card"
             style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span>历史面试评价</span>
      </div>

      <div v-for="(evaluation, index) in record.interviews" :key="index"
           style="margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #eee;"
           :style="{ 'border-bottom': index === record.interviews.length - 1 ? 'none' : '1px solid #eee' }">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">面试轮次：</span>
              <span class="info-value">{{ index + 1 }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">面试时间：</span>
              <span class="info-value">{{ formatDateTime(evaluation.interviewTime) }}</span>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">专业技能：</span>
              <span class="info-value">
                <el-rate
                  :value="evaluation.professionalScore"
                  :max="5"
                  disabled
                  show-score
                  score-template="{value}分"
                />
              </span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">沟通能力：</span>
              <span class="info-value">
                <el-rate
                  :value="evaluation.communicationScore"
                  :max="5"
                  disabled
                  show-score
                  score-template="{value}分"
                />
              </span>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">团队协作：</span>
              <span class="info-value">
                <el-rate
                  :value="evaluation.teamworkScore"
                  :max="5"
                  disabled
                  show-score
                  score-template="{value}分"
                />
              </span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="info-label">综合素质：</span>
              <span class="info-value">
                <el-rate
                  :value="evaluation.comprehensiveScore"
                  :max="5"
                  disabled
                  show-score
                  score-template="{value}分"
                />
              </span>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <div class="info-item">
              <span class="info-label">评价内容：</span>
              <span class="info-value">{{ evaluation.evaluate }}</span>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <div class="info-item">
              <span class="info-label">面试结果：</span>
              <span class="info-value">
                <el-tag :type="evaluation.result === 'PASSED' ? 'success' : 'danger'">
                  {{ evaluation.result === 'PASSED' ? '通过' : '不通过' }}
                </el-tag>
              </span>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {mapGetters} from 'vuex'

export default {
  name: 'InterviewPreview',
  data() {
    return {
      visible: false,
      record: null
    }
  },
  computed: {
    ...mapGetters('interview', [
      'statusOptionsMap',
      'roundOptionsMap'
    ])
  },
  methods: {
    // 打开弹窗
    open(record) {
      this.visible = true
      this.record = {...record}
    },

    // 关闭弹窗
    handleClose() {
      this.visible = false
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

    // 获取轮次标签
    getRoundLabel(round) {
      const roundInfo = this.roundOptionsMap[round]
      return roundInfo?.label || round
    },

    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN')
    }
  }
}
</script>

<style lang="scss" scoped>
.preview-card {
  .info-item {
    display: flex;
    margin-bottom: 15px;

    .info-label {
      width: 100px;
      font-weight: bold;
      color: #666;
    }

    .info-value {
      flex: 1;
      color: #333;
    }
  }
}

.dialog-footer {
  text-align: right;
}

::v-deep .el-rate {
  display: inline-block;
  margin-top: 2px;
}
</style>
