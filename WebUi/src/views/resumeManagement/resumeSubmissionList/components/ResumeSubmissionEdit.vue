<template>
  <el-dialog
    :title="dialogTitle"
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
          <el-form-item label="候选人姓名" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入候选人姓名"
              maxlength="50"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入联系电话"
              maxlength="11"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="邮箱地址" prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱地址"
              maxlength="100"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="投递状态" prop="state">
            <el-select
              v-model="form.state"
              placeholder="请选择投递状态"
              style="width: 100%"
            >
              <el-option
                v-for="status in statusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="年龄" prop="age">
            <div class="input-with-unit">
              <el-input-number
                v-model="form.age"
                :min="18"
                :max="65"
                style="width: 100%"
                controls-position="right"
              />
              <span class="unit-text">岁</span>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工作经验" prop="workAge">
            <div class="input-with-unit">
              <el-input-number
                v-model="form.workAge"
                :min="0"
                :max="30"
                style="width: 100%"
                controls-position="right"
              />
              <span class="unit-text">年</span>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="学历" prop="edu">
            <el-select
              v-model="form.edu"
              placeholder="请选择学历"
              style="width: 100%"
            >
              <el-option
                v-for="education in educationOptions"
                :key="education.value"
                :label="education.label"
                :value="education.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="期望薪资" prop="expectedSalary">
            <div class="input-with-unit">
              <el-input-number
                v-model="form.expectedSalary"
                :min="0"
                :max="100000"
                :step="1000"
                style="width: 100%"
                controls-position="right"
              />
              <span class="unit-text">元/月</span>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="当前薪资" prop="currentSalary">
            <div class="input-with-unit">
              <el-input-number
                v-model="form.currentSalary"
                :min="0"
                :max="100000"
                :step="1000"
                style="width: 100%"
                controls-position="right"
              />
              <span class="unit-text">元/月</span>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="简历文件" prop="resumeFile">
        <el-upload
          ref="upload"
          :file-list="fileList"
          :limit="1"
          accept=".pdf"
          :auto-upload="false"
          list-type="text"
          action=""
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
        >
          <el-button size="small" type="primary">
            <i class="el-icon-upload el-icon--left"></i>
            上传简历
          </el-button>
          <div slot="tip" class="el-upload__tip">
            支持 PDF、Word 格式，文件大小不超过 10MB
          </div>
        </el-upload>
      </el-form-item>


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
        {{ mode === 'create' ? '创建' : '更新' }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import {mapState, mapActions} from 'vuex'

export default {
  name: 'ResumeSubmissionEdit',
  data() {
    return {
      visible: false,
      loading: false,
      mode: 'create', // create | edit

      // 表单数据
      form: {
        id: '',
        name: '',
        phone: '',
        email: '',
        age: null,
        state: 1,
        workAge: 0,
        edu: '',
        expectedSalary: 0,
        currentSalary: 0,
        resumeFile: null,
        resumeUrl: '',
        resumeFileName: '',
        desc: ''
      },

      // 文件列表
      fileList: [],

      // 表单验证规则
      rules: {
        name: [
          {required: true, message: '请输入候选人姓名', trigger: 'blur'},
          {min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur'}
        ],
        phone: [
          {required: true, message: '请输入联系电话', trigger: 'blur'},
          {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur'}
        ],
        email: [
          {required: true, message: '请输入邮箱地址', trigger: 'blur'},
          {type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur'}
        ],
        state: [
          {required: true, message: '请选择投递状态', trigger: 'change'}
        ],
        age: [
          {required: true, message: '请输入年龄', trigger: 'blur'},
          {type: 'number', message: '年龄必须为数字值', trigger: 'blur'}
        ],
        workAge: [
          {type: 'number', message: '工作经验必须为数字值', trigger: 'blur'}
        ],
        expectedSalary: [
          {type: 'number', message: '期望薪资必须为数字值', trigger: 'blur'}
        ],
        currentSalary: [
          {type: 'number', message: '当前薪资必须为数字值', trigger: 'blur'}
        ]
      }
    }
  },
  computed: {
    ...mapState('resumeSubmission', [
      'statusOptions',
      'educationOptions'
    ]),

    dialogTitle() {
      return this.mode === 'create' ? '新增投递记录' : '编辑投递记录'
    }
  },
  methods: {
    ...mapActions('resumeSubmission', [
      'createRecord',
      'updateRecord'
    ]),

    // 打开弹窗
    open(mode, record = null) {
      this.mode = mode
      this.visible = true

      if (mode === 'edit' && record) {
        this.form = {...record}
        // 设置文件列表
        if (record.resumeUrl) {
          this.fileList = [{
            name: record.resumeFileName || '简历文件',
            url: record.resumeUrl
          }]
        }
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
        state: 1,
        workAge: 0,
        edu: '',
        expectedSalary: 0,
        currentSalary: 0,
        resumeFile: null,
        resumeUrl: '',
        resumeFileName: '',
        desc: ''
      }
      this.fileList = []
    },

    // 关闭弹窗
    handleClose() {
      this.visible = false
      this.loading = false
      this.resetForm()
    },

    // 职位改变
    handleJobChange(jobId) {
      const job = this.jobOptions.find(item => item.value === jobId)
      if (job) {
        this.form.jobTitle = job.label
      }
    },


    // 文件上传改变
    handleFileChange(file, fileList) {
      this.form.resumeUrl = URL.createObjectURL(file.raw)
      this.form.resumeFileName = file.name
      this.form.resumeFile = file.raw
    },

    // 文件移除
    handleFileRemove(file, fileList) {
      this.form.resumeUrl = ''
      this.form.resumeFileName = ''
      this.form.resumeFile = null
    },

    // 提交表单
    async handleSubmit() {
      try {
        await this.$refs.form.validate()

        this.loading = true

        const formData = {...this.form}

        if (this.mode === 'create') {
          await this.createRecord(formData)
          this.$message.success('创建成功')
        } else {
          await this.updateRecord(formData)
          this.$message.success('更新成功')
        }

        this.handleClose()
        this.$emit('refresh')
      } catch (error) {
        if (error !== false) { // 非表单验证错误
          console.error('提交失败:', error)
          this.$message.error(this.mode === 'create' ? '创建失败' : '更新失败')
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

::v-deep .el-upload__tip {
  margin-top: 5px;
  color: #999;
  font-size: 12px;
}

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
