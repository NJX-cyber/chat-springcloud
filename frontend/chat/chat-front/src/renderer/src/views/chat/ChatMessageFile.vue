<template>
    <div class="file-panel" :style="{ cursor: data.status === 1 ? 'pointer' : '' }">
        <div class="file-info" :title="data.fileName">
            <div class="file-name">{{ data.fileName }}</div>
            <div class="file-size">大小: {{ proxy.Utils.size2Str(data.fileSize) }}</div>
            <div class="process">
                <span class="iconfont icon-ok" v-if="data.status === 1"></span>
                <span class="iconfont icon-error" v-else-if="data.status === -1"></span>
                <div class="info">{{ statusText }}</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed, getCurrentInstance } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
    data: {
        type: Object,
        default: () => ({})
    },
})

const statusText = computed(() => {
    if (props.data.status === 0) {
        return "正在处理";
    }
    if (props.data.status === -1) {
        return "处理失败";
    }
    return "处理完成";
})
</script>

<style lang="scss" scoped>
.file-panel {
    display: flex;
    padding: 10px 5px;
    background: #fff;
    align-items: center;

    .file-info {
        width: 150px;
        display: flex;
        flex-direction: column;
        max-width: 100px;

        .file-name {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .file-size {
            margin-top: 2px;
            font-size: 12px;
            color: #6e6e6e;
        }

        .process {
            margin-top: 2px;
            display: flex;
            align-items: center;

            .loading {
                width: 16px;
            }

            .info {
                margin-left: 5px;
                font-size: 12px;
                color: #545454;
            }

            .icon-ok {
                color: #2fc25b;
                font-size: 16px;
                width: 16px;
            }

            .icon-error {
                color: #f56c6c;
                font-size: 16px;
                width: 16px;
            }
        }
    }

    .iconfont {
        width: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 40px;
        color: #6e6e6e;
    }
}
</style>
