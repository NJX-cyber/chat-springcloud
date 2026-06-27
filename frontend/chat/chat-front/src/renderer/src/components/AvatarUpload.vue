<template>
    <div class="avatar-upload">
        <div class="avatar-show">
            <template v-if="modelValue">
                <el-image v-if="preview" :src="localFile" fit="scale-down"></el-image>
                <ShowLocalImage v-else :fileId="modelValue" :forceGet="forceGet" partType="avatar" :width="40">
                </ShowLocalImage>
            </template>
            <template v-else>
                <el-upload name="file" :show-file-list="false" :multiple="false"
                    accept=".jpg,.jpeg,.png,.JPG,.JPEG,.PNG,.gif,.bmp,.GIF,.BMP" class="select-btn"
                    :http-request="uploadImage">
                    <span class="icon-add iconfont"></span>
                </el-upload>
            </template>
        </div>
        <div class="select-btn">
            <el-upload name="file" :show-file-list="false" :multiple="false"
                accept=".jpg,.jpeg,.png,.JPG,.JPEG,.PNG,.gif,.bmp,.GIF,.BMP" class="select-btn"
                :http-request="uploadImage">
                <el-button type="primary" size="small" @click="preview = true">选择</el-button>
            </el-upload>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
    modelValue: {
        type: [String, Object],
        default: null,
    },
    forceGet: {
        type: Boolean,
        default: false
    }
})

const preview = computed(() => {
    return props.modelValue instanceof File;
});

const localFile = ref(null);

const emit = defineEmits(["coverFile"]);

const uploadImage = async (file) => {
    file = file.file;
    window.ipcRenderer.send("createCover", { filePath: file.path, fileName: props.modelValue });
}

const createCoverCallBack = () => {
    window.ipcRenderer.on("createCoverCallBack", (event, payload) => {
        if (!payload?.success) {
            proxy.Message.alertError(payload?.errorInfo || { msg: "头像处理失败" }, "头像处理失败");
            return;
        }
        const { avatarStream, coverStream } = payload;
        const coverBlob = new Blob([coverStream], { type: "image/jpeg" });
        const coverFile = new File([coverBlob], "cover.jpg");
        let img = new FileReader();
        img.readAsDataURL(coverFile);
        img.onload = ({ target }) => {
            localFile.value = target.result;
        }
        const avatarBlob = new Blob([avatarStream], { type: "image/jpeg" });
        const avatarFile = new File([avatarBlob], "avatar.jpg");
        emit("coverFile", { avatarFile, coverFile })
    })
}

onMounted(() => {
    createCoverCallBack();
})

onUnmounted(() => {
    window.ipcRenderer.removeAllListeners("createCoverCallBack");
})

</script>

<style lang="scss" scoped>
.avatar-upload {
    display: flex;
    justify-content: center;
    align-items: end;
    line-height: normal;

    .avatar-show {
        background: #ededed;
        width: 60px;
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;
        position: relative;

        .icon-add {
            font-size: 30px;
            color: #b9b9b9;
            width: 60px;
            height: 60px;
            text-align: center;
            line-height: 60px;
        }

        img {
            width: 100%;

        }

        .op {
            position: absolute;
            color: #0e8aef;
            top: 80px;
        }
    }

    .select-btn {
        margin-left: 5px;
        vertical-align: bottom;
    }


}
</style>
