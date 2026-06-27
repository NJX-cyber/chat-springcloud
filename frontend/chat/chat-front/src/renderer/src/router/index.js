import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  mode: "hash",
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "默认路径",
      redirect: "/login",
    },
    {
      path: "/login",
      name: "登录",
      component: () => import('@/views/Login.vue'),
    },
    {
      path: "/showMedia",
      name: "媒体信息",
      component: () => import('@/views/show/showMedia.vue'),

    },
    {
      path: "/admin",
      name: "管理员后台",
      component: () => import('@/views/admin/Admin.vue'),
      children: [
        {
          path: "/admin/userList",
          name: "用户管理",
          component: () => import('@/views/admin/UserList.vue'),
        },
        {
          path: "/admin/groupList",
          name: "群组管理",
          component: () => import('@/views/admin/GroupList.vue'),
        },
        {
          path: "/admin/beautyAccount",
          name: "靓号管理",
          component: () => import('@/views/admin/BeautyAccount.vue'),
        },
        {
          path: "/admin/systemSetting",
          name: "系统设置",
          component: () => import('@/views/admin/SysSetting.vue'),
        },
        {
          path: "/admin/update",
          name: "版本更新",
          component: () => import('@/views/admin/Update.vue'),
        }
      ]
    },
    {
      path: "/main",
      redirect: "/chat",
      name: "主界面",
      component: () => import('@/views/Main.vue'),
      children: [
        {
          path: "/chat",
          name: "聊天",
          component: () => import('@/views/chat/Chat.vue'),
          
        },
        {
          path: "/contact",
          name: "联系人",
          redirect: "/contact/blankPage",
          component: () => import('@/views/contact/Contact.vue'),
          children: [
            {
              path: "/contact/blankPage",
              name: "空白页",
              component: () => import('@/views/contact/BlankPage.vue'),
            },
            {
              path: "/contact/Search",
              name: "搜索好友",
              component: () => import('@/views/contact/Search.vue'),
            },
            {
              path: "/contact/createGroup",
              name: "创建群组",
              component: () => import('@/views/contact/GroupEdit.vue'),
            },
            {
              path: "/contact/userDetail",
              name: "用户详情",
              component: () => import('@/views/contact/UserDetail.vue'),
            },
            {
              path: "/contact/groupDetail",
              name: "群组详情",
              component: () => import('@/views/contact/GroupDetail.vue'),
            },
            {
              path: "/contact/contactApply",
              name: "好友申请",
              component: () => import('@/views/contact/ContactApply.vue'),
            }
          ]
        },
        {
          path: "/setting",
          name: "设置",
          redirect: "/setting/userInfo",
          component: () => import('@/views/setting/Setting.vue'),
          children: [
            {
              path: "/setting/userInfo",
              name: "个人信息",
              component: () => import('@/views/setting/UserInfo.vue'),
            },
            {
              path: "/setting/fileManager",
              name: "文件管理",
              component: () => import('@/views/setting/FileManager.vue')
            },
            {
              path: "/setting/about",
              name: "关于我们",
              component: () => import('@/views/setting/About.vue')
            },

          ]
        },
      ]
    }
  ]
})
export default router
