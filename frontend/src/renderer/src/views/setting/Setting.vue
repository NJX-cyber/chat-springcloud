<template>
  <Layout>
    <template #left-content>
      <div class="title-panel drag">设置</div>
      <div class="menu-list">
        <div v-for="item in settingMenuList" :class="['menu-item', route.path === item.path ? 'menu-activate' : '']" @click="jump(item)">
          <div :class="['iconfont', item.icon]" :style="{background: item.bgColor}"></div>
          <div class="menu-name">{{ item.name }}</div>
        </div>
      </div>
    </template>
    <template #right-content>
      <div class="title-panel drag">{{ rightTitle }}</div>
      <RouterView v-slot="{Component}">
        <component :is="Component" ref="componentRef"/>
      </RouterView>
    </template>
  </Layout>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

import { useRoute, useRouter } from 'vue-router'
const route = useRoute();
const router = useRouter()

const settingMenuList = ref([
  {
    name: '账号设置',
    icon: 'icon-user-setting',
    path: '/setting/userInfo',
    bgColor: '#0294f5'
  },
  {
    name: '文件管理',
    icon: 'icon-folder1',
    path: '/setting/fileManager',
    bgColor: '#ffd04f'
  },
  {
    name: '关于我们',
    icon: 'icon-about',
    path: '/setting/about',
    bgColor: '#8b5cf6'
  }
])

const rightTitle = ref();
const jump = (item) => {
  router.push(item.path);
}
</script>

<style lang="scss" scoped>
.title-panel {
  height: 58.2px;
  background: #f7f7f7;
  text-align: center;
  line-height: 60px;
}

.menu-list {
  border-top: 1px solid #ddd;

  .menu-item {
    display: flex;
    align-items: center;
    padding: 10px;

    &:hover {
      cursor: pointer;
      background: #d6d6d6;
    }

    .iconfont {
      font-size: 20px;
      width: 35px;
      height: 35px;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .menu-name {
      color: #000000;
      margin-left: 10px;
      flex: 1;
    }
  }

  .menu-activate {
    background: #c4c4c4;

    &:hover {
      background: #c4c4c4;
    }
  }
}
</style>
