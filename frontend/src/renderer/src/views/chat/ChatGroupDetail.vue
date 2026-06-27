<template>
    <div class="group-panel">
        <el-drawer v-model="showDrawer" modal-class="mask-style" :size="300" ref="drawerRef">
            <div class="group-panel-body">
                <div class="member-list">
                    <div class="member-item" v-for="item in memberList" :key="item.userId">
                        <Avatar :userId="item.userId" :width="30" @closeDrawer="closeDrawerHandler"></Avatar>
                        <div class="nick-name" :title="item.contactName">{{ item.contactName }}</div>
                        <div class="owner-tag" v-if="item.userId === groupInfo.groupOwnerId">群主</div>
                    </div>
                    <template v-if="userInfoStore.getInfo().userId === groupInfo.groupOwnerId">
                        <div class="member-item" @click="addMemberHandler('USER')">
                            <div class="iconfont icon-add icon-op"></div>
                            <div class="nick-name">添加</div>
                        </div>
                        <div class="member-item" @click="removeMemberHandler">
                            <div class="iconfont icon-min icon-op"></div>
                            <div class="nick-name">移除</div>
                        </div>
                    </template>
                </div>
                <div class="line"></div>
                <div class="part-content">
                    <AvatarBase :userId="groupInfo.groupId" :width="60" :borderRadius="5" :showDetail="true">
                    </AvatarBase>
                </div>
                <div class="part-title">群号</div>
                <div class="part-content">{{ groupInfo.groupId }}</div>
                <div class="part-title">群名称</div>
                <div class="part-content">{{ groupInfo.groupName }}</div>
                <div class="part-title">群公告</div>
                <div class="part-content">{{ groupInfo.groupNotice || '-' }}</div>
                <div class="line"></div>
                <a href="javascript:void(0)" class="leave-btn" @click="dissolutionGroup"
                    v-if="userInfoStore.getInfo().userId === groupInfo.groupOwnerId">解散群聊</a>
                <a href="javascript:void(0)" class="leave-btn" @click="leaveGroup" v-else>退出群聊</a>
            </div>
        </el-drawer>
        <UserSelect ref="userSelectRef" @callback="addOrRemoveMemberCallBack"></UserSelect>
    </div>
</template>

<script setup>
import AvatarBase from "@/components/AvatarBase.vue"
import UserSelect from "./UserSelect.vue"
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

import { useUserInfoStore } from "../../stores/UserInfoStore";
const userInfoStore = useUserInfoStore();


const showDrawer = ref(false);
const memberList = ref([])
const groupInfo = ref({})
const drawerRef = ref(null)

const show = async (groupId) => {
    let result = await proxy.Request({
        url: proxy.Api.getGroupInfo4Chat,
        method: 'get',
        params: {
            groupId: groupId
        },
        showError: false,
        errorCallback: (response) => {
            proxy.Confirm({ message: response.info, showCancelButton: false })
        }
    })
    if (!result) {
        return;
    }
    showDrawer.value = true;
    memberList.value = result.data.userContactList;
    groupInfo.value = result.data.groupInfo;
}

const userSelectRef = ref()
const addMemberHandler = async (contactType) => {
    let result = await proxy.Request({
        url: proxy.Api.loadContact,
        method: 'get',
        params: {
            contactType: contactType === "GROUP" ? 1 : 0
        }
    })
    if (!result) {
        return;
    }
    const contactIds = memberList.value.map(item => {
        return item.userId;
    });
    let contactList = result.data;
    contactList.forEach(item => {
        item.disabled = contactIds.includes(item.contactId);
    });
    console.log("con:", contactList);
    userSelectRef.value.show({ contactList, groupId: groupInfo.value.groupId, opType: 1 })
}

const removeMemberHandler = async () => {
    let contactIds = memberList.value.map(item => {
        return item;
    });
    contactIds.forEach(item => {
        item.contactId = item.userId;
    });
    contactIds.splice(0, 1);
    userSelectRef.value.show({ contactList: contactIds, groupId: groupInfo.value.groupId, opType: 0 })
}

const addOrRemoveMemberCallBack = async (contactList) => {
    showDrawer.value = false;
}

const leaveGroup = async () => {
    proxy.Confirm({
        message: "确定要退出群聊吗？", okfun: async () => {
            let result = await proxy.Request({
                url: proxy.Api.leaveGroup,
                method: 'post',
                params: {
                    groupId: groupInfo.value.groupId
                }
            })
            if (!result) {
                return;
            }
            showDrawer.value = false;
            proxy.Message.success("退出成功");
            emit("delChatSessionCallBack",groupInfo.value.groupId)
        }
    })
}

const dissolutionGroup = async () => { 
    proxy.Confirm({
        message: "确定要解散群聊吗？", okfun: async () => {
            let result = await proxy.Request({
                url: proxy.Api.dissolutionGroup,
                method: 'put',
                params: {
                    groupId: groupInfo.value.groupId
                }
            })
            if (!result) {
                return;
            }
            showDrawer.value = false;
            proxy.Message.success("操作成功");
        }
    })
}

const closeDrawerHandler = () => {
    showDrawer.value = false;
}

defineExpose({
    show
})

const emit = defineEmits(["delChatSessionCallBack"])

</script>

<style lang="scss" scoped>
.group-panel {
    color: #000000;

    :deep(.mask-style) {
        top: 1px;
        right: 1px;
        height: calc(100vh - 2px);
    }

    :deep(.el-drawer) {
        -webkit-app-region: no-drag;
    }

    :deep(.el-drawer__header) {
        margin-bottom: 10px;
    }

    :deep(.el-drawer__body) {
        padding: 10px;
    }

    .group-panel-body {
        .member-list {
            display: flex;
            flex-wrap: wrap;

            .member-item {
                width: 20%;
                display: flex;
                flex-direction: column;
                align-items: center;
                margin-bottom: 5px;
                padding: 5px;
                position: relative;

                .owner-tag {
                    position: absolute;
                    left: 0;
                    top: 0;
                    font-size: 12px;
                    background: #6366f1;
                    color: #ffffff;
                    border-radius: 3px;
                }

                .nick-name {
                    margin-top: 3px;
                    width: 100%;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    font-size: 13px;
                    text-align: center;
                }

                .icon-op {
                    cursor: pointer;
                    width: 30px;
                    height: 30px;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    border: 1px solid #dbdbdb;
                    color: #6e6e6e;
                }
            }
        }

        .line {
            margin-bottom: 10px;
            border-top: 1px solid #ddd;
            height: 1px;
        }

        .part-title {
            margin-top: 10px;
        }

        .part-content {
            color: #757575;
            margin-bottom: 10px;
        }

        .leave-btn {
            color: #f45454;
            text-decoration: none;
            text-align: center;
            display: block;
            margin-top: 10px;
        }
    }
}
</style>
