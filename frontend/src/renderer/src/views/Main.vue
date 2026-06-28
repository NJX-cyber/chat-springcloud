<template>
  <div class="main">
    <div class="left-side">
      <!-- 用户头像区域 -->
      <div class="avatar-section">
        <Avatar :userId="userInfoStore.getInfo()?.userId" :width="38" :showDetail="false"></Avatar>
      </div>

      <!-- 上部导航菜单 -->
      <div class="menu-list menu-top">
        <template v-for="item in menuList" :key="item.path">
          <div v-if="item.position === 'top'"
            :class="['tab-item iconfont', item.icon, activeMenu.path === item.path ? 'active' : '']"
            @click="changeMenu(item)"
            :title="item.name">
            <div class="tab-indicator" v-if="activeMenu.path === item.path"></div>
            <template v-if="item.name === 'chat' || item.name === 'contact'">
              <Badge :count="messageCountStore.getCount(item.countKey)" :top="2" :left="16"></Badge>
            </template>
          </div>
        </template>
      </div>

      <!-- 下部设置菜单 -->
      <div class="menu-list menu-bottom">
        <template v-for="item in menuList" :key="item.path">
          <div v-if="item.position === 'bottom'"
            :class="['tab-item iconfont', item.icon, activeMenu.path === item.path ? 'active' : '']"
            @click="changeMenu(item)"
            :title="item.name">
            <div class="tab-indicator" v-if="activeMenu.path === item.path"></div>
          </div>
        </template>
      </div>
    </div>

    <div class="right-container">
      <router-view v-slot="{ Component }">
        <KeepAlive include="chat">
          <component :is="Component" ref="componentRef"> </component>
        </KeepAlive>
      </router-view>
    </div>
  </div>
  <WinOP></WinOP>
  <Update></Update>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, watch } from "vue"
const { proxy } = getCurrentInstance();
import { useRouter, useRoute } from "vue-router";
import WinOP from "../components/WinOP.vue";
const router = useRouter();
const route = useRoute();
import { useGlobalInfoStore } from "../stores/GlobalInfoStore";
const globalInfoStore = useGlobalInfoStore();

import { useUserInfoStore } from "../stores/UserInfoStore";
import Avatar from "../components/Avatar.vue";
import Update from "@/views/setting/Update.vue";
const userInfoStore = useUserInfoStore();

import { useSysSettingStore } from "../stores/SysSettingStore";
const sysSettingStore = useSysSettingStore();

import { useMessageCountStore } from "@/stores/MessageCountStore";
const messageCountStore = useMessageCountStore();

const menuList = ref([
  { name: 'chat', icon: 'icon-chat', path: '/chat', countKey: 'chatCount', position: 'top' },
  { name: 'contact', icon: 'icon-user', path: '/contact', countKey: 'contactApplyCount', position: 'top' },
  { name: 'mySetting', icon: 'icon-more2', path: '/setting', position: 'bottom' },
])

const activeMenu = ref(menuList.value[0])
const changeMenu = (item) => {
  activeMenu.value = item;
  router.push(item.path)
}

const getLocalServerPort = () => {
  window.ipcRenderer.once('getLocalStoreCallBack', (event, serverPort) => {
    globalInfoStore.setGlobalInfo('localServerPort', serverPort);
  })
  window.ipcRenderer.send('getLocalStore', userInfoStore.getInfo().userId + 'localServerPort');
}

const getSystemSetting = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getSystemSettings,
    method: 'get'
  })
  if (!result) {
    return;
  }
  sysSettingStore.setSysSetting(result.data);
}

const onReLogin = () => {
  window.ipcRenderer.once('reLoginCallBack', (event) => {
    router.push('/login');
  })
}
watch(
  () => route.path,
  (newValue, oldValue) => {
    if (newValue) {
      switch (newValue) {
        case '/chat':
          changeMenu(menuList.value[0]);
          break;
        case '/contact':
          break;
        case '/setting':
          break;
        default:
          break;
      }
    }
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  getSystemSetting();
  getLocalServerPort();
  onReLogin();
})

</script>

<style lang="scss" scoped>
.main {
  background: #ddd;
  display: flex;
  border-radius: 0 3px 3px 0;
  overflow: hidden;
  height: 100%;

  .left-side {
    width: 60px;
    background: #2e2e2e;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 30px;
    border-right: none;
    padding-bottom: 10px;
  }

  .avatar-section {
    margin-bottom: 15px;

    :deep(.avatar) {
      border-radius: 3px;
    }
  }

  .menu-list {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;

    .tab-item {
      color: #999;
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 2px 0;
      cursor: pointer;
      font-size: 22px;
      position: relative;

      &:hover {
        color: #ccc;
      }
    }

    .active {
      color: #07c160 !important;
    }
  }

  .menu-top {
    flex: 1;
    padding-top: 5px;
  }

  .menu-bottom {
    justify-content: flex-end;
  }

  .right-container {
    flex: 1;
    overflow: hidden;
    background: #f5f5f5;
  }
}
</style>
