<template>
    <div class="admin-window">
        <!-- 顶部标题栏 -->
        <div class="admin-header drag">
            <div class="header-brand">
                <div class="brand-icon">
                    <span class="iconfont icon-setting"></span>
                </div>
                <span class="brand-text">管理控制台</span>
            </div>
        </div>

        <div class="body-content">
            <!-- 侧边导航 -->
            <div class="left-side">
                <nav class="side-nav">
                    <div
                        :class="['nav-item', item.path === route.path ? 'active' : '']"
                        @click="menuJump(item)"
                        v-for="item in menuList"
                        :key="item.path"
                    >
                        <div class="nav-icon" :style="{ background: item.iconBgColor }">
                            <span :class="['iconfont', item.icon]"></span>
                        </div>
                        <span class="nav-text">{{ item.name }}</span>
                        <div class="nav-indicator" v-if="item.path === route.path"></div>
                    </div>
                </nav>
            </div>

            <!-- 内容区 -->
            <div class="right-content">
                <router-view v-slot="{ Component }">
                    <component :is="Component" ref="componentRef"></component>
                </router-view>
            </div>
        </div>

        <WinOP :showSetTop="false" :darkBg="true"></WinOP>
    </div>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRouter, useRoute } from "vue-router";
const route = useRoute();
const router = useRouter();

import { useGlobalInfoStore } from "@/stores/GlobalInfoStore";
const globalInfoStore = useGlobalInfoStore();

const menuList = ref([
    {
        name: "用户管理",
        icon: "icon-user",
        path: "/admin/userList",
        iconBgColor: "#fa9d3b"
    },
    {
        name: "靓号管理",
        icon: "icon-beauty-beauty",
        path: "/admin/beautyAccount",
        iconBgColor: "#fe90b3"
    },
    {
        name: "群组管理",
        icon: "icon-group",
        path: "/admin/groupList",
        iconBgColor: "#1485ee"
    },
    {
        name: "系统设置",
        icon: "icon-setting",
        path: "/admin/systemSetting",
        iconBgColor: "#fa5151"
    },
    {
        name: "版本管理",
        icon: "icon-refresh",
        path: "/admin/update",
        iconBgColor: "#6366f1"
    }
])

const menuJump = (item) => {
    router.push(item.path)
}

onMounted(() => {
    window.ipcRenderer.on("pageInitData", (e, data) => {
        globalInfoStore.setGlobalInfo("token", data.token);
        globalInfoStore.setGlobalInfo("localServerPort", data.localServerPort);
        // 写入 localStorage，否则 request.js 中的 axios 拦截器取不到 token
        localStorage.setItem("token", data.token);
    })
})

onUnmounted(() => {
    window.ipcRenderer.removeAllListeners("pageInitData");
})

</script>

<style lang="scss" scoped>
.admin-window {
    padding: 0;
    border: 1px solid #ddd;
    background: #fff;
    position: relative;
    overflow: hidden;
    width: 100vw;
    height: 100vh;

    .admin-header {
        height: 40px;
        background: #f5f5f5;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 15px;
        flex-shrink: 0;
        border-bottom: 1px solid #e0e0e0;

        .header-brand {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .brand-icon {
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;

            .iconfont {
                font-size: 16px;
                color: #333;
            }
        }

        .brand-text {
            font-size: 14px;
            font-weight: 500;
            color: #333;
        }

        .admin-badge {
            font-size: 12px;
            color: #999;
        }
    }

    .body-content {
        height: calc(100% - 40px);
        display: flex;

        .left-side {
            width: 200px;
            border-right: 1px solid #e0e0e0;
            background: #f5f5f5;
        }

        .side-nav {
            padding: 8px 0;
        }

        .nav-item {
            display: flex;
            align-items: center;
            padding: 12px 15px;
            cursor: pointer;

            &:hover {
                background: #e8e8e8;
            }

            .nav-icon {
                width: 32px;
                height: 32px;
                display: flex;
                align-items: center;
                justify-content: center;
                flex-shrink: 0;

                .iconfont {
                    color: #fff;
                    font-size: 16px;
                }
            }

            .nav-text {
                margin-left: 10px;
                font-size: 14px;
                color: #333;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
        }

        .nav-item.active {
            background: #ddd;
        }

        .right-content {
            flex: 1;
            padding: 15px;
            overflow-y: auto;
            background: #fff;
        }
    }
}
</style>
