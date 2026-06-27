<template>
  <div class="user-panel">
    <AvatarBase
      :userId="userInfo.userId || userInfo.contactId || userInfo.groupId"
      :width="60"
      :borderRadius="5"
      :showDetail="true"
    />
    <div class="user-info">
      <div class="nickname">
        {{ userInfo.nickname }}
        <span class="iconfont icon-woman" v-if="userInfo.sex === 1"></span>
        <span class="iconfont icon-man" v-if="userInfo.sex === 0"></span>
      </div>
      <div class="info">ID：{{ userInfo.userId || userInfo.contactId || userInfo.groupId }}</div>
      <div class="info" v-if="showArea">地区：{{ proxy.Utils.getAreaInfo(userInfo.areaName) }}</div>
    </div>
  </div>
</template>

<script setup>
import AvatarBase from './AvatarBase.vue';
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
const props = defineProps({
  userInfo: {
    type: Object,
    default: {}
  },
  showArea: {
    type: Boolean,
    default: true
  }
});

</script>

<style lang="scss" scoped>
.user-panel { 
    display: flex;
    padding-bottom: 20px;
    .user-info { 
        flex: 1;
        margin-left: 10px;
        .nickname { 
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            color: #000;
            font-size: 16px;
            .iconfont { 
                font-size: 13px;
            }
            .icon-man {
                color: #2cb6fe;
            }
            .icon-woman { 
                color: #fb7373;
            }
        }
        .info {
            font-size: 13px;
            color: #9e9e9e;
            margin-top: 3px;
        }
        
    }
}
</style>
