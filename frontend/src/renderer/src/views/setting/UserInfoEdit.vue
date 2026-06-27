<template>
    <div>
        <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
            <el-form-item label="头像" prop="avatarFile">
                <AvatarUpload v-model="formData.avatarFile" :forceGet="true" @coverFile="saveCover"></AvatarUpload>
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
                <el-input v-model.trim="formData.nickname" placeholder="请输入昵称" maxlength="150" clearable></el-input>
            </el-form-item>
            <el-form-item label="性别" prop="sex">
                <el-radio-group v-model="formData.sex">
                    <el-radio :label="0">男</el-radio>
                    <el-radio :label="1">女</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="朋友权限" prop="connectType">
                <el-switch v-model="formData.connectType" :active-value="0" :inactive-value="1" />
                <div class="info">好友申请无需验证</div>
            </el-form-item>
            <el-form-item label="地区" prop="area">
                <AreaSelect v-model="formData.area"></AreaSelect>
            </el-form-item>
            <el-form-item label="个性签名" prop="personalSignature">
                <el-input clearable type="textarea" :rows="5" placeholder="请输入个性签名"
                    v-model.trim="formData.personalSignature" resize="none" :show-word-limit="true"
                    maxlength="30"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="saveUserInfo">保存</el-button>
                <el-button @click="cancel">取消</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup>
import AreaSelect from "@/components/AreaSelect.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted, computed } from "vue"
const { proxy } = getCurrentInstance();

import { useUserInfoStore } from "@/stores/UserInfoStore";
const userInfoStore = useUserInfoStore();

import { useAvatarInfoStore } from "@/stores/AvatarUpdateStore";
const avatarStore = useAvatarInfoStore();

const props = defineProps({
    data: {
        type: Object,
    }
})

const formData = reactive({
    ...props.data,
    avatarFile: props.data.userId,
    area: {
        areaCode: props.data.areaCode ? props.data.areaCode.split(',') : [],
        areaName: props.data.areaName ? props.data.areaName.split(',') : []
    }
});
const formDataRef = ref();
const rules = {
    avatarFile: [{ required: true, message: '请选择头像' }],
    nickname: [{ required: true, message: '请输入昵称' }],

}

const saveCover = ({ avatarFile, coverFile }) => {
    formData.avatarFile = avatarFile;
    formData.avatarCover = coverFile;
}

const emit = defineEmits(['editBack', 'updateUserInfo']);

const saveUserInfo = () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return
        }
        let params = {}
        Object.assign(params, formData);
        params.areaName = '';
        params.areaCode = '';
        if (params.area) {
            params.areaName = params.area.areaName.join(',');
            params.areaCode = params.area.areaCode.join(',');
            delete params.area;
        }

        avatarStore.setForceReload(userInfoStore.getInfo().userId, false);

        let result = await proxy.Request({
            url: proxy.Api.saveUserInfo,
            method: "put",
            params
        })
        if (!result) {
            return;
        }
        proxy.Message.success("保存成功");

        avatarStore.setForceReload(userInfoStore.getInfo().userId, true);
        emit("updateUserInfo");
    })
}

const cancel = () => {
    emit("editBack");
}

</script>

<style lang="scss" scoped>
.info {
    margin-left: 5px;
    color: #949494;
    font-size: 12px;
}
</style>
