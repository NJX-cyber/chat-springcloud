<template>
    <div class="win-op no-drag">
        <div v-if="showSetTop" :class="['iconfont icon-top', isTop ? 'win-top' : '']" @click="top"
            :title="isTop ? '取消置顶' : '置顶'"></div>
        <div v-if="showMin" class="iconfont icon-min" @click="minimize" title="最小化"></div>
        <div v-if="showMax" :class="['iconfont', isMax ? 'icon-maximize' : 'icon-max']" :title="isMax ? '向下还原' : '最大化'" @click="maximize"></div>
        <div v-if="showClose" class="iconfont icon-close" @click="close" title="关闭"></div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue"

const props = defineProps({
    showSetTop: { type: Boolean, default: true },
    showMin: { type: Boolean, default: true },
    showMax: { type: Boolean, default: true },
    showClose: { type: Boolean, default: true },
    closeType: { type: Number, default: 1 } //关闭页面 0：关闭，1：隐藏
})

const emit = defineEmits(['closeCallback'])

const isMax = ref(false)
const isTop = ref(false)

const winOp = (action, data) => {
    if (window.ipcRenderer) {
        window.ipcRenderer.send('winTitleOp', { action, data })
    } else {
        console.warn('ipcRenderer is not available')
    }
}

const close = () => {
    winOp('close', { closeType: props.closeType })
    emit('closeCallback')
}

const minimize = () => {
    winOp('minimize')
}

const maximize = () => {
    if (isMax.value) {
        winOp('unmaximize')
        isMax.value = false
    } else {
        winOp('maximize')
        isMax.value = true
    }
}

const top = () => {
    isTop.value = !isTop.value
    winOp('top', { isTop: isTop.value })
}

onMounted(() => {
    isMax.value = false
    isTop.value = false
})
</script>

<style lang="scss" scoped>
.win-op {
    top: 0px;
    right: 0px;
    position: absolute;
    z-index: 1;
    overflow: hidden;
    border-radius: 0px 3px 3px 0px;
    -webkit-app-region: no-drag; 

    .iconfont {
        float: left;
        font-size: 12px;
        color: #101010;
        text-align: center;
        display: flex;
        justify-content: center;
        cursor: pointer;
        height: 25px;
        align-items: center;
        padding: 0px 10px;
        transition: background-color 0.2s; /* 加个小动画提升体验 */

        &:hover {
            background: #ddd;
        }
    }

    .icon-close {
        &:hover {
            background: #fb7373;
            color: #fff;
        }
    }

    .win-top {
        background: #ddd; /* 选一个淡淡的蓝色背景 */
        color: #07c160;      /* 图标变成高亮蓝色！ */
        
        &:hover {
            background: #d9ecff; /* 置顶状态下悬停稍微加深一点 */
        }
    }
}
</style>