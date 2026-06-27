<template>
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
        <el-form-item label="群名称" prop="groupName">
            <el-input v-model.trim="formData.groupName" maxlength="150" clearable placeholder="请输入群名称"></el-input>
        </el-form-item>
        <el-form-item label="头像" prop="avatarFile">
            <AvatarUpload v-model="formData.avatarFile" :forceGet="true" ref="avatarUploadRef" @coverFile="saveCover" />
        </el-form-item>
        <el-form-item label="加入权限" prop="joinType">
            <el-radio-group v-model="formData.joinType">
                <el-radio :label="0">无需管理员同意</el-radio>
                <el-radio :label="1">需要管理员同意</el-radio>
            </el-radio-group>
        </el-form-item>
        <el-form-item label="公告" prop="groupNotice">
            <el-input v-model.trim="formData.groupNotice" type="textarea" rows="5" maxlength="300"
                :show-word-limit="true" clearable placeholder="请输入群公告"></el-input>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" @click="submitForm">{{ formData.groupId ? '修 改' : '创 建' }}</el-button>
        </el-form-item>
    </el-form>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useContactStateStore } from "../../stores/ContactStateStore";
const contactStateStore = useContactStateStore();

import { useAvatarInfoStore } from "../../stores/AvatarUpdateStore";
const avatarStore = useAvatarInfoStore();

const initFormData = () => ({
    groupId: "",
    groupName: "",
    joinType: "",
    groupNotice: "",
    avatarFile: ""
})

const formData = ref(initFormData())
const formDataRef = ref()
const rules = {
    groupName: [{ required: true, message: "请输入群名称" }],
    joinType: [{ required: true, message: "请选择加入权限" }],
    avatarFile: [{ required: true, message: "请上传群头像"}],
}

const emit = defineEmits(["editBack"]);

const show = (data) => {
    console.log("show:", data);
    if (data) {
        formData.value = {
            groupId: data.groupId,
            groupName: data.groupName,
            joinType: data.joinType,
            groupNotice: data.groupNotice,
            avatarFile: data.groupId
        }
    } else {
        formData.value = initFormData()
    }
}

defineExpose({
    show
})


//TODO 保存封面
const saveCover = ({ avatarFile, coverFile }) => {
    formData.value.avatarFile = avatarFile;
    formData.value.avatarCover = coverFile;
    
}

const submitForm = async () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {};
        //TODO 重新加载头像
        avatarStore.setForceReload(params.groupId,false);
        Object.assign(params, formData.value);
        let result = await proxy.Request({
            url: proxy.Api.saveGroup,
            method: "post",
            params,
        });
        if (!result) {
            return;
        }
        if (params.groupId) {
            proxy.Message.success("修改成功");
            emit("editBack");
        } else {
            proxy.Message.success("创建成功");
        }
        formDataRef.value.resetFields();
        contactStateStore.setContactReload(null);
        contactStateStore.setContactReload("MY_GROUP");
        if (params.groupId) {
            avatarStore.setForceReload(params.groupId,true);
        }
    });
}

</script>

<style lang="scss" scoped></style>
