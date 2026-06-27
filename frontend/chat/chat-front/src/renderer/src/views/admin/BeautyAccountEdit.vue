<template>
    <Dialogue :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" :width="'440px'"
        @close="dialogConfig.show = false">
        <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="60px">
            <el-form-item label="邮箱" prop="email" style="margin-bottom: 15px">
                <el-input :maxLength="50" placeholder="请输入邮箱" v-model.trim="formData.email" />
            </el-form-item>
            <el-form-item label="靓号" prop="userId">
                <el-input :maxLength="11" placeholder="请输入靓号" v-model.trim="formData.userId" />
            </el-form-item>
        </el-form>
    </Dialogue>
</template>

<script setup>
import { ref, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()

const dialogConfig = ref({
    show: false,
    title: '修改靓号',
    buttons: [
        {
            text: '确定',
            type: 'primary',
            click: (e) => {
                submitForm()
            }
        }
    ]
})

const formData = ref({ updateDescList: [] })
const formDataRef = ref()
const rules = ref({
    email: [
        { required: true, message: '请输入邮箱' },
        { validator: proxy.Verify.email, message: '邮箱格式不正确', }
    ],
    userId: [
        { required: true, message: '请输入靓号' },
        { min: 11, max: 11, message: '靓号必须是11位数字' },
        { validator: proxy.Verify.number, message: '靓号格式不正确' }
    ]
})

const showEdit = (data = {}) => {
    if (!data.id) {
        dialogConfig.value.title = '新增靓号'
    }
    dialogConfig.value.show = true
    nextTick(() => {
        formDataRef.value.resetFields()
        formData.value = Object.assign({}, data)
    })
}

const emit = defineEmits(['reload'])
const submitForm = () => {
    formDataRef.value.validate(async (valid) => {
        if (valid) {
            let res = await proxy.Request({
                url: proxy.Api.saveBeautyAccount,
                method: 'post',
                params: formData.value
            })
            if (!res) {
                return
            }
            dialogConfig.value.show = false
            proxy.Message.success('保存成功')
            emit('reload')
        }
    })
}

defineExpose({
    showEdit
})

</script>

<style lang="scss" scoped></style>
