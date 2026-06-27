<template>
    <div class="user-avatar" @click="showDetailHandler"
        :style="{ 'width': width + 'px', 'height': width + 'px', 'borderRadius': borderRadius + 'px' }">
        <ShowLocalImage :width="width" :fileId="userId" :partType="partType" :fileType="fileType"
            :forceGet="avatarStore.getForceReload(userId)"></ShowLocalImage>
    </div>
</template>

<script setup>
import { useAvatarInfoStore } from "@/stores/AvatarUpdateStore";
const avatarStore = useAvatarInfoStore();


const props = defineProps({
    userId: {
        type: String,
        default: ""
    },
    width: {
        type: Number,
        default: 40
    },
    partType: {
        type: String,
        default: "avatar"
    },
    fileType: {
        type: Number,
        default: 0
    },
    borderRadius: {
        type: Number,
        default: 0
    },
    showDetail: {
        type: Boolean,
        default: false
    }
})

const showDetailHandler = () => {
    if (!props.showDetail) {
        return
    }
    window.ipcRenderer.send("newWindow", { windowId: "media", title: "图片查看", path: '/showMedia', 
    data: { fileList: [{ fileId: props.userId, fileType: 0, partType: "avatar", status: 1, forceGet: true }]} });
}

</script>

<style lang="scss" scoped>
.user-avatar {
    background: #d3d3d3;
    display: flex;
    overflow: hidden;
    cursor: pointer;
    align-items: center;
    justify-content: center;
}
</style>
