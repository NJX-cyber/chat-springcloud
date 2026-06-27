<template>
    <div class="image-panel" @click="showImageHandel">
        <el-image :src="serverUrl" fit="scale-down" :width="width">
        </el-image>
        <div class="play-panel" v-if="showPlay">
            <span class="iconfont icon-video-play"></span>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue"
const { proxy } = getCurrentInstance();

import { useGlobalInfoStore } from "../stores/GlobalInfoStore";
const globalInfoStore = useGlobalInfoStore();

const props = defineProps({
    width: {
        type: Number,
        default: 170
    },
    showPlay: {
        type: Boolean,
        default: false
    },
    fileId: {
        type: [String, Number],
        default: 0
    },
    partType: {
        type: String,
        default: "avatar"
    },
    fileType: {
        type: Number,
        default: 0
    },
    forceGet: {
        type: Boolean,
        default: false
    }
})

const serverUrl = computed(() => {
    // console.log("partType:", props.partType)
    // console.log("fileId:", props.fileId)
    if (!props.fileId) {
        return
    }
    const serverPort = globalInfoStore.getGlobalInfo().localServerPort;
    return `http://127.0.0.1:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&fileType=${props.fileType}&showCover=true&forceGet=${props.forceGet}&t=${Date.now()}`;
})



</script>

<style lang="scss" scoped>
.image-panel {
    position: relative;
    display: flex;
    overflow: hidden;
    cursor: pointer;
    max-width: 170px;
    max-height: 170px;
    background: #ddd;

    .icon-image-error {
        margin: 0px auto;
        font-size: 30px;
        color: #838383;
    }

    .play-panel {
        z-index: 2;
        position: absolute;
        top: 0px;
        left: 0px;
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;

        .icon-video-play {
            font-size: 35px;
            color: #fff;
        }

        &:hover {
            opacity: 0.8;
        }
    }
}
</style>
