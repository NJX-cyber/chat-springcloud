<template>
    <Dialogue :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" :width="'500px'"
        @close="dialogConfig.show = false">
        <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="100px">
            <el-form-item label="版本号" prop="version">
                <el-input :maxLength="10" v-model.trim="formData.version" placeholder="eg:1.0.0" />
            </el-form-item>
            <el-form-item label="文件类型" prop="fileType">
                <el-radio-group v-model="formData.fileType">
                    <el-radio :value="0">本地文件</el-radio>
                    <el-radio :value="1">外链</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="文件" prop="fileName" class="file-select" v-if="formData.fileType == 0">
                <div class="file-name">{{ formData.fileName }}</div>
                <el-upload name="file" :show-file-list="false" accept=".exe" :multiple="false"
                    :http-request="selectFile">
                    <el-button size="small" type="primary">选择文件</el-button>
                </el-upload>
            </el-form-item>
            <el-form-item label="外链地址" prop="outLink" v-if="formData.fileType == 1">
                <el-input :maxLength="200" v-model.trim="formData.outLink" placeholder="请输入完整的外链地址" />
            </el-form-item>
            <el-form-item label="更新内容" class="update-form-item" style="margin-top: 15px;">
                <div class="update-desc-item" v-for="(item, index) in formData.updateDescList">
                    <el-form-item :prop="'updateDescList.' + index + '.title'"
                        :rules="{ required: true, message: '更新内容不能为空' }">
                        <div class="update-desc">
                            <div class="num">{{ index + 1 }}</div>
                            <div class="input">
                                <el-input :maxLength="11" v-model.trim="item.title" placeholder="请输入更新内容" />
                            </div>
                            <el-icon v-if="index === 0" @click="addLine">
                                <Plus />
                            </el-icon>
                            <el-icon v-if="index > 0" @click="delLine(index)">
                                <Minus />
                            </el-icon>
                        </div>
                    </el-form-item>
                </div>
            </el-form-item>
        </el-form>
    </Dialogue>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted, nextTick } from "vue";
const { proxy } = getCurrentInstance();
import { Plus, Minus } from '@element-plus/icons-vue'

const dialogConfig = ref({
    show: false,
    title: "发布更新",
    buttons: [
        {
            text: "确定",
            type: "primary",
            click: () => {
                submitForm();
            },
        }
    ]
})

const formData = ref({
    updateDescList: []
})

const formDataRef = ref(null)

const rules = {
    version: [
        { required: true, message: "请输入版本号" },
        { validator: proxy.Verify.version, message: "版本号只能为数字和点" },
    ],
    fileType: [
        { required: true, message: "请选择文件类型" },
    ],
    fileName: [
        { required: true, message: "请选择文件" },
    ],
    outLink: [
        { required: true, message: "请输入外链地址" },
    ],
    updateType: [
        { required: true, message: "请选择更新类型" },
    ],
}

const selectFile = (file) => {
    file = file.file;
    formData.value.file = file;
    formData.value.fileName = file.name;
}

const addLine = () => {
    formData.value.updateDescList.push({
        title: ""
    })
}

const delLine = (index) => {
    formData.value.updateDescList.splice(index, 1)
}

const emit = defineEmits(["reload"]);
const submitForm = () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {}
        Object.assign(params, formData.value);
        const updateDescArray = params.updateDescList.map(item => {
            return item.title
        })
        params.updateDesc = updateDescArray.join("|")
        delete params.updateDescList;
        let result = await proxy.Request({
            url: proxy.Api.saveUpdate,
            method: "post",
            params: params
        })
        if (!result) {
            return;
        }
        dialogConfig.value.show = false;
        proxy.Message.success("保存成功");
        emit("reload");
    })
}

const showEdit = (data) => {
    dialogConfig.value.show = true;
    nextTick(() => {
        formDataRef.value.resetFields()
        if (data) {
            data.updateDescList = (data.updateDesc || "").split("|").map(item => {
                return { title: item }
            })
            data.fileName = "chat." + data.version + ".exe"
        }
        formData.value = Object.assign({}, data || { updateDescList: [] })
    })
}

defineExpose({
    showEdit
})

</script>

<style lang="scss" scoped>
.file-select {
    display: flex;

    .file-name {
        color: #409eff;
        margin-right: 10px;
    }
}
.el-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 28px;
    height: 28px;
    font-size: 18px;
    cursor: pointer;
}

.update-form-item {
    margin-bottom: 0;

    .update-desc-item {
        width: 100%;
        margin-bottom: 15px;

        .update-desc {
            width: 100%;
            display: flex;

            .num {
                width: 15px;
                margin-right: 2px;
            }

            .input {
                flex: 1;
            }

            .iconfont {
                text-align: center;
                margin-left: 10px;
                cursor: pointer;
            }
        }
    }
}
</style>
