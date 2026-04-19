/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description router全局配置，如有必要可分文件抽离，其中asyncRoutes只有在intelligence模式下才会用到，vip文档中已提供路由的基础图标与小清新图标的配置方案，请仔细阅读
 */

import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/layouts'
import EmptyLayout from '@/layouts/EmptyLayout'
import {publicPath, routerMode} from '@/config'

Vue.use(VueRouter)
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true,
  },
  {
    path: '/register',
    component: () => import('@/views/register/index'),
    hidden: true,
  },
  {
    path: '/401',
    name: '401',
    component: () => import('@/views/401'),
    hidden: true,
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/404'),
    hidden: true,
  },
]

export const asyncRoutes = [
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: 'index',
        name: 'Index',
        component: () => import('@/views/index/index'),
        meta: {
          title: '首页',
          icon: 'home',
          affix: true,
        },
      },
    ],
  },

  {
    path: '/ai-chat',
    component: Layout,
    redirect: 'noRedirect',
    children: [
      {
        path: 'AiChat',
        name: 'AiChat',
        component: () => import('@/views/aiChat/index'),
        meta: {
          title: 'AI助手',
          icon: 'robot',
          badge: 'Hot',
        },
      },
    ],
  },

  {
    path: '/vab',
    component: Layout,
    redirect: 'noRedirect',
    name: 'Vab',
    alwaysShow: true,
    hidden: true,
    meta: {title: '组件', icon: 'box-open'},
    children: [
      {
        path: 'permissions',
        name: 'Permission',
        component: () => import('@/views/vab/permissions/index'),
        meta: {
          title: '角色权限',
          permissions: ['admin', 'editor'],
        },
      },
      {
        path: 'icon',
        component: EmptyLayout,
        redirect: 'noRedirect',
        name: 'Icon',
        meta: {
          title: '图标',
          permissions: ['admin'],
        },
        children: [
          {
            path: 'awesomeIcon',
            name: 'AwesomeIcon',
            component: () => import('@/views/vab/icon/index'),
            meta: {title: '常规图标'},
          },
          {
            path: 'colorfulIcon',
            name: 'ColorfulIcon',
            component: () => import('@/views/vab/icon/colorfulIcon'),
            meta: {title: '多彩图标'},
          },
        ],
      },
      {
        path: 'table',
        component: () => import('@/views/vab/table/index'),
        name: 'Table',
        meta: {
          title: '表格',
          permissions: ['admin'],
        },
      },

      {
        path: 'webSocket',
        name: 'WebSocket',
        component: () => import('@/views/vab/webSocket/index'),
        meta: {title: 'webSocket', permissions: ['admin']},
      },
      {
        path: 'form',
        name: 'Form',
        component: () => import('@/views/vab/form/index'),
        meta: {title: '表单', permissions: ['admin']},
      },
      {
        path: 'element',
        name: 'Element',
        component: () => import('@/views/vab/element/index'),
        meta: {title: '常用组件', permissions: ['admin']},
      },
      {
        path: 'tree',
        name: 'Tree',
        component: () => import('@/views/vab/tree/index'),
        meta: {title: '树', permissions: ['admin']},
      },
      {
        path: 'menu1',
        component: () => import('@/views/vab/nested/menu1/index'),
        name: 'Menu1',
        alwaysShow: true,
        meta: {
          title: '嵌套路由 1',
          permissions: ['admin'],
        },
        children: [
          {
            path: 'menu1-1',
            name: 'Menu1-1',
            alwaysShow: true,
            meta: {title: '嵌套路由 1-1'},
            component: () => import('@/views/vab/nested/menu1/menu1-1/index'),

            children: [
              {
                path: 'menu1-1-1',
                name: 'Menu1-1-1',
                meta: {title: '嵌套路由 1-1-1'},
                component: () => import('@/views/vab/nested/menu1/menu1-1/menu1-1-1/index'),
              },
            ],
          },
        ],
      },
      {
        path: 'loading',
        name: 'Loading',
        component: () => import('@/views/vab/loading/index'),
        meta: {title: 'loading', permissions: ['admin']},
      },
      {
        path: 'backToTop',
        name: 'BackToTop',
        component: () => import('@/views/vab/backToTop/index'),
        meta: {title: '返回顶部', permissions: ['admin']},
      },
      {
        path: 'lodash',
        name: 'Lodash',
        component: () => import('@/views/vab/lodash/index'),
        meta: {title: 'lodash', permissions: ['admin']},
      },

      {
        path: 'upload',
        name: 'Upload',
        component: () => import('@/views/vab/upload/index'),
        meta: {title: '上传', permissions: ['admin']},
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/vab/errorLog/index'),
        meta: {title: '错误日志模拟', permissions: ['admin']},
      },
      {
        path: 'external-link',
        component: EmptyLayout,
        redirect: 'noRedirect',
        meta: {
          title: '外链',
        },
        children: [
          {
            path: 'https://github.com/zxwk1998/vue-admin-better/',
            name: 'ExternalLink',
            meta: {
              title: '外链',
              target: '_blank',
              permissions: ['admin', 'editor'],
              badge: 'New',
            },
          },
        ],
      },
      {
        path: 'more',
        name: 'More',
        component: () => import('@/views/vab/more/index'),
        meta: {title: '关于', permissions: ['admin']},
      },
      {
        path: 'chart',
        name: 'Chart',
        component: () => import('@/views/vab/chart/index'),
        meta: {title: '图表', permissions: ['admin']},
      },
      {
        path: 'tab',
        name: 'Tab',
        component: () => import('@/views/vab/tab/index'),
        meta: {title: '选项卡', permissions: ['admin']},
      },
      {
        path: 'editor',
        name: 'Editor',
        component: () => import('@/views/vab/editor/index'),
        meta: {title: '编辑器', permissions: ['admin']},
      },
      {
        path: 'qrCode',
        name: 'QrCode',
        component: () => import('@/views/vab/qrCode/index'),
        meta: {title: '二维码', permissions: ['admin']},
      },
    ],
  },
  {
    path: '/resumeManagement',
    component: Layout,
    redirect: 'noRedirect',
    name: 'ResumeManagement',
    meta: {title: '简历管理', icon: 'user-tie', permissions: ['admin']},
    children: [
      {
        path: 'resumeSubmissionList',
        name: 'ResumeSubmissionList',
        component: () => import('@/views/resumeManagement/resumeSubmissionList/index'),
        meta: {title: '简历筛选'},
      },
    ],
  },
  {
    path: '/interviewManagement',
    component: Layout,
    redirect: 'noRedirect',
    name: 'InterviewManagement',
    meta: {title: '面试流转', icon: 'user-tie', permissions: ['admin']},
    children: [
      {
        path: 'interviewList',
        name: 'InterviewList',
        component: () => import('@/views/interviewManagement/interviewList/index'),
        meta: {title: '面试流转'},
      },
    ],
  },
  {
    path: '/offerManagement',
    component: Layout,
    redirect: 'noRedirect',
    children: [
      {
        path: 'index',
        name: 'OfferManagement',
        component: () => import('@/views/offerManagement/index'),
        meta: {
          title: 'Offer管理',
          // icon: 'handshake',
          permissions: ['admin'],
        },
      },
    ],
  },
  {
    path: '/progress',
    component: Layout,
    redirect: 'noRedirect',
    name: 'Progress',
    meta: {title: '面试进度查询', icon: 'search', permissions: ['admin']},
    children: [
      {
        path: 'index',
        name: 'ProgressIndex',
        component: () => import('@/views/progress/index'),
        meta: {title: '面试进度查询'},
      },
    ],
  },
  {
    path: '/personnelManagement',
    component: Layout,
    redirect: 'noRedirect',
    name: 'PersonnelManagement',
    meta: {
      title: '系统管理',
      // icon: 'users-cog',
      permissions: ['admin'], hidden: true
    },
    children: [
      {
        path: 'userManagement',
        name: 'UserManagement',
        component: () => import('@/views/personnelManagement/userManagement/index'),
        meta: {title: '用户管理'},
      },
      {
        path: 'roleManagement',
        name: 'RoleManagement',
        component: () => import('@/views/personnelManagement/roleManagement/index'),
        meta: {title: '角色管理'},
      },
      {
        path: 'menuManagement',
        name: 'MenuManagement',
        component: () => import('@/views/personnelManagement/menuManagement/index'),
        meta: {title: '菜单管理', badge: 'New'},
      },
    ],
  },
  {
    path: '/mall',
    component: Layout,
    redirect: 'noRedirect',
    name: 'Mall',
    hidden: true,
    meta: {
      title: '商城',
      icon: 'shopping-cart',
      permissions: ['admin']
    },
    children: [
      {
        path: 'pay',
        name: 'Pay',
        component: () => import('@/views/mall/pay/index'),
        meta: {
          title: '支付',
          noKeepAlive: true,
        },
        children: null,
      },
      {
        path: 'goodsList',
        name: 'GoodsList',
        component: () => import('@/views/mall/goodsList/index'),
        meta: {
          title: '商品列表',
        },
      },
    ],
  },
  {
    path: '/error',
    component: EmptyLayout,
    redirect: 'noRedirect',
    name: 'Error',
    hidden: true,
    meta: {title: '错误页', icon: 'bug'},
    children: [
      {
        path: '401',
        name: 'Error401',
        component: () => import('@/views/401'),
        meta: {title: '401'},
      },
      {
        path: '404',
        name: 'Error404',
        component: () => import('@/views/404'),
        meta: {title: '404'},
      },
    ],
  },
  {
    path: '/store',
    component: Layout,
    meta: {
      title: '',
      icon: '',
      hidden: true,
    },
    children: [
      {
        path: 'https://vuejs-core.cn/store',
        hidden: true,
        meta: {
          title: '物料市场',
          target: '_blank',
          icon: 'mortar-pestle',
          badge: 'Hot',
        },
      },
    ],
  },
  {
    path: '*',
    redirect: '/404',
    hidden: true,
  },
]

const router = new VueRouter({
  base: publicPath,
  mode: routerMode,
  scrollBehavior: () => ({
    y: 0,
  }),
  routes: constantRoutes,
})

export function resetRouter() {
  location.reload()
}

export default router
