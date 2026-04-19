import {
  getResumeSubmissionList,
  createResumeSubmission,
  updateResumeSubmission,
  deleteResumeSubmission,
  batchDeleteResumeSubmission,
  batchUpdateStatus,
  getResumeStatistics
} from '@/api/resumeSubmission'

const state = {
  // 列表数据
  list: [],
  total: 0,
  loading: false,

  // 当前操作的记录
  currentRecord: null,

  // 搜索表单
  searchForm: {
    pageNo: 1,
    pageSize: 20,
    name: '',
    status: '',
    submissionDateStart: '',
    submissionDateEnd: '',
    source: '',
    hrId: ''
  },

  // 选项数据
  statusOptions: [
    {label: '待处理', value: 1, type: 'info'},
    {label: '简历未通过', value: -1, type: 'danger'},
    {label: '面试中', value: 2, type: 'warning'}
  ],

  sourceOptions: [
    {label: '官网', value: '官网'},
    {label: '招聘网站', value: '招聘网站'},
    {label: '内推', value: '内推'},
    {label: '校园招聘', value: '校园招聘'},
    {label: '猎头推荐', value: '猎头推荐'},
    {label: '社交媒体', value: '社交媒体'}
  ],

  educationOptions: [
    {label: '专科', value: '专科'},
    {label: '本科', value: '本科'},
    {label: '硕士', value: '硕士'},
    {label: '博士', value: '博士'}
  ],

  // 统计数据
  statistics: {
    total: 0,
    pending: 0,
    rejected: 0,
    interviewScheduled: 0
  }
}

const mutations = {
  SET_LIST: (state, data) => {
    state.list = data
  },

  SET_TOTAL: (state, total) => {
    state.total = total
  },

  SET_LOADING: (state, loading) => {
    state.loading = loading
  },

  SET_CURRENT_RECORD: (state, record) => {
    state.currentRecord = record
  },

  UPDATE_SEARCH_FORM: (state, form) => {
    state.searchForm = {...state.searchForm, ...form}
  },

  RESET_SEARCH_FORM: (state) => {
    state.searchForm = {
      pageNo: 1,
      pageSize: 20,
      name: '',
      status: '',
      submissionDateStart: '',
      submissionDateEnd: '',
      source: '',
      hrId: ''
    }
  },

  SET_STATISTICS: (state, statistics) => {
    state.statistics = statistics
  },

  // 添加新记录到列表顶部
  ADD_RECORD: (state, record) => {
    state.list.unshift(record)
    state.total += 1
  },

  // 更新列表中的记录
  UPDATE_RECORD: (state, record) => {
    const index = state.list.findIndex(item => item.id === record.id)
    if (index !== -1) {
      state.list.splice(index, 1, record)
    }
  },

  // 从列表中删除记录
  REMOVE_RECORD: (state, id) => {
    const index = state.list.findIndex(item => item.id === id)
    if (index !== -1) {
      state.list.splice(index, 1)
      state.total -= 1
    }
  },

  // 批量删除记录
  REMOVE_RECORDS: (state, ids) => {
    state.list = state.list.filter(item => !ids.includes(item.id))
    state.total -= ids.length
  }
}

const actions = {
  // 获取列表数据
  async fetchList({commit, state}) {
    commit('SET_LOADING', true)
    try {
      const response = await getResumeSubmissionList(state.searchForm)
      commit('SET_LIST', response.data.records)
      commit('SET_TOTAL', response.totalCount)
      return response
    } catch (error) {
      console.error('获取简历投递列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 创建记录
  async createRecord({commit, dispatch}, data) {
    try {
      const response = await createResumeSubmission(data)
      commit('ADD_RECORD', response.data)
      return response
    } catch (error) {
      console.error('创建简历投递记录失败:', error)
      throw error
    }
  },

  // 更新记录
  async updateRecord({commit}, data) {
    try {
      const response = await updateResumeSubmission(data)
      commit('UPDATE_RECORD', response.data)
      return response
    } catch (error) {
      console.error('更新简历投递记录失败:', error)
      throw error
    }
  },

  // 删除记录
  async deleteRecord({commit}, id) {
    try {
      const response = await deleteResumeSubmission(id)
      commit('REMOVE_RECORD', id)
      return response
    } catch (error) {
      console.error('删除简历投递记录失败:', error)
      throw error
    }
  },

  // 批量删除记录
  async batchDeleteRecords({commit}, ids) {
    try {
      const response = await batchDeleteResumeSubmission(ids)
      commit('REMOVE_RECORDS', ids)
      return response
    } catch (error) {
      console.error('批量删除简历投递记录失败:', error)
      throw error
    }
  },

  // 批量更新状态
  async batchUpdateRecordStatus({dispatch}, {ids, status}) {
    try {
      const response = await batchUpdateStatus({ids, status})
      // 批量更新后重新获取列表数据
      await dispatch('fetchList')
      return response
    } catch (error) {
      console.error('批量更新状态失败:', error)
      throw error
    }
  },

  // 获取统计数据
  async fetchStatistics({commit}) {
    try {
      const response = await getResumeStatistics()
      commit('SET_STATISTICS', response.data)
      return response
    } catch (error) {
      console.error('获取统计数据失败:', error)
      throw error
    }
  },

  // 搜索
  async handleSearch({commit, dispatch}, searchParams) {
    commit('UPDATE_SEARCH_FORM', {...searchParams, pageNo: 1})
    return await dispatch('fetchList')
  },

  // 重置搜索
  async handleResetSearch({commit, dispatch}) {
    commit('RESET_SEARCH_FORM')
    return await dispatch('fetchList')
  },

  // 分页
  async handlePageChange({commit, dispatch}, {pageNo, pageSize}) {
    commit('UPDATE_SEARCH_FORM', {pageNo, pageSize})
    return await dispatch('fetchList')
  }
}

const getters = {
  // 获取状态选项映射
  statusOptionsMap: (state) => {
    const map = {}
    state.statusOptions.forEach(option => {
      map[option.value] = option
    })
    return map
  },

  // 是否有搜索条件
  hasSearchCondition: (state) => {
    const {name, status, submissionDateStart, submissionDateEnd, source, hrId} = state.searchForm
    return !!(name || status || submissionDateStart || submissionDateEnd || source || hrId)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
