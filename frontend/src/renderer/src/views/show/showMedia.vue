<template>
    <div class="media-window">
        <div class="win-title drag"></div>
        <div class="media-op no-drag">
            <div :class="['iconfont icon-left', currentIndex === 0 ? 'not-allow' : '']" @dblclick.stop title="上一张"
                @click="next(-1)">
            </div>
            <div :class="['iconfont icon-right', currentIndex >= allFileList.length - 1 ? 'not-allow' : '']"
                @dblclick.stop title="下一张" @click="next(1)">
            </div>
            <template v-if="fileList[0].fileType === 0">
                <el-divider direction="vertical" />
                <div class="iconfont icon-enlarge" @click.stop="changeSize(0.1)" @dblclick.stop title="放大"></div>
                <div class="iconfont icon-narrow" @click.stop="changeSize(-0.1)" @dblclick.stop title="缩小"></div>
                <div :class="['iconfont', isOne2One ? 'icon-resize' : 'icon-source-size']" @click="resize"
                    @dblclick.stop :title="isOne2One ? '图片适应窗口大小' : '图片原始大小'"></div>
                <div class="iconfont icon-rotate" @click="rotate" @dblclick.stop title="旋转"></div>
                <el-divider direction="vertical" />
            </template>
            <div class="iconfont icon-download" @click="download" @dblclick.stop title="另存为..."></div>
        </div>
        <div class="media-panel">
            <viewer :options="options" @inited="inited" :images="fileList"
                v-if="fileList[0].fileType === 0 && fileList[0].status === 1">
                <img :src="fileList[0].url" />
            </viewer>
            <div ref="player" id="player" v-show="fileList[0].fileType === 1 && fileList[0].status === 1"
                style="width: 100%; height: 100%;"></div>
            <div v-if="fileList[0].fileType === 2" class="file-panel">
                <div class="file-item">{{ fileList[0].fileName }}</div>
                <div class="file-item">{{ Utils.size2Str(fileList[0].fileSize) }}</div>
                <div class="file-item download">
                    <el-button type="primary" @click="download">下载</el-button>
                </div>
            </div>
            <div class="loading" v-if="fileList[0].status !== 1">加载中...</div>
        </div>
        <WinOP @closeCallback="closeWin"></WinOP>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();
import axios from "axios";

import DPlayer from "dplayer";
import 'viewerjs/dist/viewer.css'
import { component as Viewer } from 'v-viewer'

const options = ref({
    inline: true,
    toolbar: false,
    navbar: false,
    button: false,
    title: false,
    zoomRatio: 0.1,
    zoomOnWheel: false,
})

const viewerMy = ref(null);

const inited = (viewer) => {
    viewerMy.value = viewer;
}

const changeSize = (zoomRatio) => {
    if (!viewerMy.value) return;
    viewerMy.value.zoom(zoomRatio);
}

const rotate = () => {
    viewerMy.value.rotate(90, true);
}

const isOne2One = ref(false);

const resize = () => {
    isOne2One.value = !isOne2One.value;
    if (!isOne2One.value) {
        viewerMy.value.zoomTo(viewerMy.value.initialImageData.radio, true)
    } else {
        viewerMy.value.zoomTo(1, true)
    }
}

const onWheel = (e) => {
    if (fileList.value[0].fileType !== 0) {
        return;
    } else if (e.deltaY < 0) {
        changeSize(0.1);
    } else {
        changeSize(-0.1);
    }
}

const player = ref();
const dPlayer = ref();
const initPlayer = () => {
    dPlayer.value = new DPlayer({
        element: player.value,
        theme: '#b7daff',
        screenshot: true,
        video: {
            url: '',
        },
    });
}

const currentIndex = ref(0);
const allFileList = ref([]);
const fileList = ref([{ fileType: 0, status: 0 }])
const localServerPort = ref(0);

const closeWin = () => {
    if (dPlayer.value) {
        dPlayer.value.pause();
    }
}

const next = (index) => {
    if (currentIndex.value + index < 0 || currentIndex.value + index >= allFileList.value.length) {
        return;
    }
    currentIndex.value += index;
    getCurrentFile();
}

const getUrl = (file) => {
    return `http://127.0.0.1:${localServerPort.value}/file?fileId=${file.fileId}&partType=${file.partType}&fileType=${file.fileType}&forceGet=${file.forceGet}&t=${new Date().getTime()}`;
}

const getCurrentFile = () => {
    if (dPlayer.value) {
        dPlayer.value.pause();
    }
    const currentFile = allFileList.value[currentIndex.value];
    const url = getUrl(currentFile);
    fileList.value.splice(0, 1, { url: url, fileType: currentFile.fileType, status: 1, fileSize: currentFile.fileSize, fileName: currentFile.fileName })
    if (dPlayer.value && currentFile.fileType === 1) {
        dPlayer.value.switchVideo({
            url: url,
        });
        dPlayer.value.seek(0);
    }
}

const download = () => { 
    const currentFile = allFileList.value[currentIndex.value];
    window.ipcRenderer.send("saveFile", {
        partType: currentFile.partType,
        fileId: currentFile.fileId
    });
}

onMounted(() => {
    initPlayer();
    window.addEventListener("wheel", onWheel);

    window.ipcRenderer.on("pageInitData", (event, data) => {
        localServerPort.value = data.localServerPort;
        allFileList.value = data.fileList;
        currentIndex.value = data.fileList.findIndex(
            item => item.fileId === data.currentFileId
        );
        currentIndex.value = currentIndex.value !== -1 ? currentIndex.value : 0;
        getCurrentFile();
    });
})

onUnmounted(() => {
    window.ipcRenderer.removeAllListeners("pageInitData");
    window.removeEventListener("wheel", onWheel);
})

</script>

<style lang="scss" scoped>
.media-window {
    padding: 0px;
    height: calc(100vh);
    border: 1px solid #ddd;
    background: #fff;
    position: relative;
    overflow: hidden;

    .win-title {
        height: 37px;
    }

    .media-op {
        position: absolute;
        left: 0;
        top: 0;
        height: 35px;
        line-height: 35px;
        display: flex;
        align-items: center;

        .iconfont {
            font-size: 18px;
            padding: 0 10px;

            &:hover {
                background: #f3f3f3;
                cursor: pointer;
            }
        }

        .not-allow {
            cursor: not-allowed;
            color: #ddd;
            text-decoration: none;

            &:hover {
                color: #ddd;
                cursor: not-allowed;
                background: none;
            }
        }
    }

    .media-panel {
        height: calc(100vh - 37px);
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;

        :deep(.viewer-backdrop) {
            background: #fff !important;
        }


        .file-panel {
            .file-item {
                margin-top: 5px;
            }

            .download {
                margin-top: 20px;
                text-align: center;
            }
        }
    }
}
</style>
