<template>
    <div v-if="showUpdate">
        <!-- 遮罩层 -->
        <div class="update-container drag"></div>
        <div class="update-panel nodrag">
            <div class="update-inner">
                <!-- 头部区域，去掉了 SVG，增加了副标题让排版更丰满 -->
                <div class="update-header">
                    <div class="header-bg-circle circle-1"></div>
                    <div class="header-bg-circle circle-2"></div>
                    <div class="header-title">发现新版本</div>
                    <div class="header-subtitle">体验全新功能与体验优化</div>
                </div>

                <div class="update-content">
                    <div class="update-content-title">更新提示</div>
                    <div class="update-list">
                        <div v-for="(item, index) in updateInfo?.updateList" :key="item.id || index">
                            {{ index + 1 }}、{{ item }}
                        </div>
                    </div>

                    <div class="download-progress" v-if="downloading">
                        <div v-if="downloadPercent.progress !== 100">
                            <el-progress :percentage="downloadPercent.progress" :stroke-width="10" color="#6366f1" />
                            <div class="download-tips">
                                下载中... ({{ proxy.Utils?.size2Str(downloadPercent.loaded) || downloadPercent.loaded }} /
                                {{ proxy.Utils?.size2Str(downloadPercent.total) || downloadPercent.total }})
                            </div>
                        </div>
                        <div v-else class="download-complete">下载完成，准备安装...</div>
                    </div>

                    <!-- 优化后的按钮组 -->
                    <div class="op-btn" v-else>
                        <div class="btn cancel" @click.stop="handleCancel">稍后再说</div>
                        <div class="btn update" @click.stop="handleUpdate">立即更新</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted, onUnmounted } from 'vue';
import config from "../../../../../package.json";

const { proxy } = getCurrentInstance();

import { useUserInfoStore } from "@/stores/userInfoStore";
const userInfoStore = useUserInfoStore();

const props = defineProps({
    autoUpdate: {
        type: Boolean,
        default: false
    }
});

const showUpdate = ref(false);

const updateInfo = ref({
    size: 0,
    updateList: [
        "优化了系统整体性能",
        "修复了已知的一些Bug",
        "提升了用户界面的交互体验"
    ]
});

const handleCancel = () => {
    showUpdate.value = false;
};

const handleUpdate = () => {
    downloading.value = true;
    let progress = 0;
    const timer = setInterval(() => {
        progress += 10;
        downloadPercent.value = {
            progress: progress,
            loaded: progress * 1024 * 1024,
            total: 100 * 1024 * 1024
        };
        if (progress >= 100) clearInterval(timer);
    }, 500);
};

const checkUpdateAuto = async (auto) => {
    let result = await proxy.Request({
        url: proxy.Api.checkVersion,
        method: 'get',
        params: {
            appVersion: config.version,
            userId: userInfoStore.getInfo().userId,
            token: localStorage.getItem('token')
        },
    })
    if (!result) {
        return;
    }
    if (result.data === null) {
        if (!auto) {
            proxy.Confirm({
                message: "当前已经是最新版本",
                showCancelButton: false
            })
        }
    }
    showUpdate.value = true;
    updateInfo.value = result.data;
};

const downloading = ref(false);
const downloadPercent = ref({ progress: 0, loaded: 0, total: updateInfo.value.size });
const startDownload = () => {
    if (updateInfo.value.fileType === 0) {
        downloading.value = true;
        window.ipcRenderer.send("downloadUpdate", { id: updateInfo.value.id, fileName: updateInfo.value.fileName });
    } else if (updateInfo.value.fileType === 1) {
        window.ipcRenderer.send("openUrl", { url: updateInfo.value.outerLink });
    }
}

const updateDownloadCallBack = () => {
    window.ipcRenderer.on("updateDownloadCallBack", (e, loaded) => {
        downloadPercent.value.loaded = loaded;
        downloading.value.progress = Math.floor((loaded / updateInfo.value.size) * 100);
    });
}

const checkUpdate = () => { 
    checkUpdateAuto(false);
}

defineExpose({
    checkUpdate
})

onMounted(() => {
    if (props.autoUpdate) {
        checkUpdateAuto(true);
    }
    updateDownloadCallBack();
})

onUnmounted(() => {
    window.ipcRenderer.removeAllListeners("updateDownloadCallBack");
})

</script>

<style lang="scss" scoped>
.drag {
    -webkit-app-region: drag;
}

.nodrag {
    -webkit-app-region: no-drag;
}

.update-container {
    opacity: 0.5;
    background: #000;
    z-index: 1;
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100vh;
    pointer-events: none;
}

.update-panel {
    top: 100px;
    left: 0;
    width: 100%;
    position: absolute;
    z-index: 200;
    display: flex;
    align-items: center;
    justify-content: center;
    pointer-events: auto;

    .update-inner {
        background: #fff;
        width: 360px;
        /* 稍微加宽了一点，让胶囊按钮有呼吸感 */
        min-height: 400px;
        border-radius: 16px;
        /* 更圆润的边角 */
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
        overflow: hidden;
        display: flex;
        flex-direction: column;

        .update-header {
            position: relative;
            height: 140px;
            /* 去掉图标后，头部高度可以稍微变矮 */
            background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            overflow: hidden;

            .circle-1 {
                position: absolute;
                width: 180px;
                height: 180px;
                background: rgba(255, 255, 255, 0.08);
                border-radius: 50%;
                top: -60px;
                left: -40px;
            }

            .circle-2 {
                position: absolute;
                width: 120px;
                height: 120px;
                background: rgba(255, 255, 255, 0.08);
                border-radius: 50%;
                bottom: -30px;
                right: -20px;
            }

            .header-title {
                color: #fff;
                font-size: 24px;
                font-weight: bold;
                letter-spacing: 2px;
                z-index: 2;
                margin-bottom: 8px;
            }

            .header-subtitle {
                color: rgba(255, 255, 255, 0.8);
                font-size: 13px;
                z-index: 2;
            }
        }

        .update-content {
            background: #fff;
            padding: 25px;
            flex: 1;
            display: flex;
            flex-direction: column;

            .update-content-title {
                font-size: 16px;
                font-weight: bold;
                color: #333;
                margin-bottom: 12px;
                display: flex;
                align-items: center;

                &::before {
                    content: '';
                    display: inline-block;
                    width: 4px;
                    height: 16px;
                    background: #6366f1;
                    border-radius: 2px;
                    margin-right: 8px;
                }
            }

            .update-list {
                flex: 1;
                max-height: 160px;
                overflow-y: auto;
                font-size: 14px;
                color: #555;
                line-height: 1.8;

                &::-webkit-scrollbar {
                    width: 4px;
                }

                &::-webkit-scrollbar-thumb {
                    background: #e0e0e0;
                    border-radius: 4px;
                }
            }
        }

        .download-progress {
            margin-top: 20px;
            text-align: center;

            .download-tips {
                margin-top: 12px;
                font-size: 13px;
                color: #888;
            }

            .download-complete {
                color: #6366f1;
                font-weight: bold;
                padding: 15px 0;
            }
        }

        /* 重新设计的现代按钮样式 */
        .op-btn {
            margin-top: 25px;
            display: flex;
            justify-content: space-between;
            gap: 15px;
            /* 按钮之间的间距 */
            -webkit-app-region: no-drag;

            .btn {
                flex: 1;
                height: 44px;
                line-height: 44px;
                text-align: center;
                border-radius: 22px;
                /* 胶囊圆角 */
                font-size: 15px;
                font-weight: 500;
                cursor: pointer;
                pointer-events: auto;
                transition: all 0.2s ease;
                user-select: none;
                /* 防止文字被意外选中 */

                /* 按压时的动效，提升点击手感 */
                &:active {
                    transform: scale(0.96);
                }
            }

            .cancel {
                background: #f5f5f5;
                color: #666;
                border: 1px solid #e8e8e8;

                &:hover {
                    background: #e8e8e8;
                    color: #333;
                }
            }

            .update {
                background: #6366f1;
                color: #fff;
                /* 增加主题色同色系的发光阴影 */
                box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);

                &:hover {
                    background: #4f46e5;
                    box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
                }
            }
        }
    }
}
</style>