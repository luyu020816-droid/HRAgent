import {
  getInterviewList,
  createInterview,
  updateInterview,
  getInterviewStatistics,
  addInterviewEvaluation
} from '@/api/interview'

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
    state: '',
    interviewDateStart: '',
    interviewDateEnd: ''
  },

  // 选项数据
  statusOptions: [
    {label: '一面', value: 2, type: 'success'},
    {label: '二面', value: 3, type: 'success'},
    {label: '三面', value: 4, type: 'success'},
    {label: '未通过', value: -2, type: 'info'}
  ],

  roundOptions: [
    {label: '一面', value: 2},
    {label: '二面', value: 3},
    {label: '三面', value: 4}
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
    firstRound: 0,
    secondRound: 0,
    thirdRound: 0
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
      state: '',
      interviewDateStart: '',
      interviewDateEnd: ''
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
      const response = await getInterviewList(state.searchForm)
      commit('SET_LIST', response.data.records)
      commit('SET_TOTAL', response.totalCount)
      return response
    } catch (error) {
      console.error('获取面试列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 创建记录
  async createRecord({commit, dispatch}, data) {
    try {
      const response = await createInterview(data)
      commit('ADD_RECORD', response.data)
      return response
    } catch (error) {
      console.error('创建面试记录失败:', error)
      throw error
    }
  },

  // 更新记录
  async updateRecord({commit}, data) {
    try {
      const response = await updateInterview(data)
      commit('UPDATE_RECORD', response.data)
      return response
    } catch (error) {
      console.error('更新面试记录失败:', error)
      throw error
    }
  },

  // 添加面试评价
  async addEvaluation({commit}, data) {
    try {
      const response = await addInterviewEvaluation(data)
      commit('UPDATE_RECORD', response.data)
      return response
    } catch (error) {
      console.error('添加面试评价失败:', error)
      throw error
    }
  },

  // 删除记录
  async deleteRecord({commit}, id) {
    try {
      const response = await deleteInterview(id)
      commit('REMOVE_RECORD', id)
      return response
    } catch (error) {
      console.error('删除面试记录失败:', error)
      throw error
    }
  },

  // 批量删除记录
  async batchDeleteRecords({commit}, ids) {
    try {
      const response = await batchDeleteInterview(ids)
      commit('REMOVE_RECORDS', ids)
      return response
    } catch (error) {
      console.error('批量删除面试记录失败:', error)
      throw error
    }
  },

  // 获取统计数据
  async fetchStatistics({commit}) {
    try {
      const response = await getInterviewStatistics()
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

  // 获取轮次选项映射
  roundOptionsMap: (state) => {
    const map = {}
    state.roundOptions.forEach(option => {
      map[option.value] = option
    })
    return map
  },

  // // 是否有搜索条件
  // hasSearchCondition: (state) => {
  //   const {name, state} = state.searchForm
  //   return !!(name || state)
  // }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
