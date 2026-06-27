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
            <div class="header-actions">
                <span class="admin-badge">Admin</span>
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
    background: #f0f0f5;
    position: relative;
    overflow: hidden;
    width: 100vw;
    height: 100vh;
    display: flex;
    flex-direction: column;

    /* ===== 顶部标题栏 ===== */
    .admin-header {
        height: 52px;
        background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 20px;
        flex-shrink: 0;
        border-bottom: 1px solid rgba(255, 255, 255, 0.08);

        .header-brand {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .brand-icon {
            width: 34px;
            height: 34px;
            border-radius: 10px;
            background: linear-gradient(135deg, #6366f1, #8b5cf6);
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);

            .iconfont {
                font-size: 18px;
                color: #fff;
            }
        }

        .brand-text {
            font-size: 16px;
            font-weight: 600;
            color: #e2e8f0;
            letter-spacing: 0.5px;
        }

        .admin-badge {
            padding: 4px 14px;
            border-radius: 20px;
            background: rgba(139, 92, 246, 0.2);
            color: #c4b5fd;
            font-size: 12px;
            font-weight: 600;
            letter-spacing: 1px;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }
    }

    .body-content {
        flex: 1;
        display: flex;
        overflow: hidden;

        /* ===== 侧边栏 ===== */
        .left-side {
            width: 220px;
            background: #1e1b4b;
            flex-shrink: 0;
            overflow-y: auto;
            position: relative;

            &::after {
                content: '';
                position: absolute;
                right: 0;
                top: 0;
                bottom: 0;
                width: 1px;
                background: linear-gradient(180deg, 
                    rgba(139, 92, 246, 0.3) 0%, 
                    rgba(99, 102, 241, 0.1) 50%, 
                    rgba(139, 92, 246, 0.3) 100%);
            }
        }

        .side-nav {
            padding: 12px 10px;
        }

        .nav-item {
            display: flex;
            align-items: center;
            padding: 11px 14px;
            margin-bottom: 4px;
            border-radius: 12px;
            cursor: pointer;
            position: relative;
            transition: all 0.2s ease;

            &:hover {
                background: rgba(139, 92, 246, 0.12);

                .nav-icon {
                    transform: scale(1.05);
                }
            }

            &:active {
                transform: scale(0.98);
            }

            .nav-icon {
                width: 36px;
                height: 36px;
                border-radius: 10px;
                display: flex;
                align-items: center;
                justify-content: center;
                flex-shrink: 0;
                transition: transform 0.2s ease;

                .iconfont {
                    color: #fff;
                    font-size: 18px;
                }
            }

            .nav-text {
                margin-left: 12px;
                font-size: 14px;
                color: #a5a6c4;
                font-weight: 500;
                transition: color 0.2s ease;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .nav-indicator {
                position: absolute;
                right: 0;
                top: 50%;
                transform: translateY(-50%);
                width: 3px;
                height: 24px;
                background: linear-gradient(180deg, #818cf8, #a78bfa);
                border-radius: 3px 0 0 3px;
            }
        }

        .nav-item.active {
            background: rgba(139, 92, 246, 0.18);

            .nav-text {
                color: #e2e8f0;
                font-weight: 600;
            }
        }

        /* ===== 右侧内容区 ===== */
        .right-content {
            flex: 1;
            padding: 20px;
            overflow-y: auto;
            background: #f8f7ff;
        }
    }
}
</style>
