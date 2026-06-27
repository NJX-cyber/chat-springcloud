<template>
  <div class="main">
    <div class="left-side">
      <div>
        <Avatar :userId="userInfoStore.getInfo()?.userId" :width="35" :showDetail="false"></Avatar>
      </div>
      <div class="menu-list">
        <template v-for="item in menuList" :key="item.path">
          <div :class="['tab-item iconfont', item.icon, activeMenu.path === item.path ? 'active' : '']"
            v-if="item.position === 'top'" @click="changeMenu(item)">
            <template v-if="item.name === 'chat' || item.name === 'contact'">
              <Badge :count="messageCountStore.getCount(item.countKey)" :top="3" :left="15"></Badge>
            </template>
          </div>
        </template>
      </div>
      <div class="menu-list menu-button">
        <template v-for="item in menuList" :key="item.path">
          <div :class="['tab-item iconfont', item.icon, activeMenu.path === item.path ? 'active' : '']"
            v-if="item.position === 'bottom'" @click="changeMenu(item)"> </div>
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

  .left-side {
    width: 55px;
    background: #2e2e2e;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 35px;
    border: 1px solid #2e2e2e;
    border-right: none;
    padding-bottom: 10px;

    .menu-list {
      width: 100%;
      flex: 1;

      .tab-item {
        color: #d3d3d3;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: 10px;
        cursor: pointer;
        font-size: 22px;
        position: relative;
      }

      .active {
        color: #07c160;
      }
    }

    .menu-button {
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
    }
  }

  .right-container {
    flex: 1;
    overflow: hidden;
    border: 1px solid #ddd;
    border-left: none;
  }
}

.popover-user-panel {
  padding: 10px;

  .popover-user {
    display: flex;
    border-bottom: 1px solid #ddd;
    padding-bottom: 20px;
  }

  .send-message {
    margin-top: 10px;
    text-align: center;
    padding: 20px 0 0 0;
  }
}
</style>
