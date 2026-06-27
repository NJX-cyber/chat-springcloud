<template>
    <ContentPanel>
        <div class="show-info" v-if="showType === 0">
            <div class="user-info">
                <UserBaseInfo :userInfo="userInfo"></UserBaseInfo>
                <div class="more-op">
                    <el-dropdown placement="bottom-end" trigger="click">
                        <span class="el-dropdown-link iconfont icon-more"></span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item @click="changePart(1)">修改个人信息</el-dropdown-item>
                                <el-dropdown-item @click="changePart(2)">修改密码</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </div>
            <div class="part-item">
                <div class="part-title">朋友权限</div>
                <div class="part-content">
                    {{ userInfo?.connectType === 0 ? '不需要验证' : '需要验证' }}
                </div>
            </div>
            <div class="part-item">
                <div class="part-title">个性签名</div>
                <div class="part-content">{{ userInfo?.personalSignature || '-' }}</div>
            </div>
            <div class="logout">
                <el-button type="primary" @click="logout">退出登录</el-button>
            </div>
        </div>
        <div v-else-if="showType === 1">
            <UserInfoEdit :data="userInfo" @editBack="editBack" @updateUserInfo="updateUserInfo"></UserInfoEdit>
        </div>
        <div v-else-if="showType === 2">
            <UserInfoPassword @editBack="editBack"></UserInfoPassword>
        </div>
    </ContentPanel>
</template>

<script setup>
import UserInfoEdit from './UserInfoEdit.vue';
import UserInfoPassword from './UserInfoPassword.vue';
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue"
const { proxy } = getCurrentInstance();

const userInfo = ref();

const getUserInfo = async () => {
    let result = await proxy.Request({
        url: proxy.Api.getUserInfo,
        method: 'get',
        params: {}
    })
    if (!result) {
        return;
    }
    userInfo.value = result.data;
}

const showType = ref(0);
const changePart = (type) => {
    showType.value = type;
}

const updateUserInfo = () => {
    editBack();
}
const editBack = () => {
    showType.value = 0;
    getUserInfo();
}

const logout = () => {
    proxy.Confirm({
        message: '确定退出登录？',
        okfun: async () => {
            window.ipcRenderer.send("reLogin");
            let result = await proxy.Request({
                url: proxy.Api.logout,
                method: 'post'
            })
            if (!result) {
                return;
            }
        }
    })
}

onMounted(() => {
    getUserInfo();
})
</script>

<style lang="scss" scoped>
.show-info {
    .user-info {
        position: relative;

        .more-op {
            position: absolute;
            right: 0;
            top: 20px;

            .icon-more {
                color: #9e9e9e;

                &:hover {
                    background: #dddddd;
                }
            }
        }
    }

    .part-item {
        display: flex;
        border-bottom: 1px solid #eaeaea;
        padding: 20px 0;

        .part-title {
            width: 60px;
            color: #9e9e9e;
        }

        .part-content {
            flex: 1;
            margin-left: 15px;
            color: #161616;
        }
    }

    .logout {
        text-align: center;
        margin-top: 20px;
    }
}
</style>
