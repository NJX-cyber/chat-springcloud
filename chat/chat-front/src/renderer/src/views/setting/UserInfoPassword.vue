<template>
  <div>
    <el-form ref="formDataRef" :model="formData" :rules="rules" label-width="80px" @submit.prevent>
      <el-form-item label="原密码" prop="password">
        <el-input v-model.trim="formData.password" clearable type="password" placeholder="请输入原密码" show-password
          @keydown.enter="saveUserInfo"></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model.trim="formData.newPassword" clearable type="password" placeholder="请输入新密码" show-password
          @keydown.enter="saveUserInfo"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model.trim="formData.confirmPassword" clearable type="password" placeholder="请输入确认密码" show-password
          @keydown.enter="saveUserInfo"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="saveUserInfo">修改密码</el-button>
        <el-button link :disabled="submitting" @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, getCurrentInstance } from 'vue'
import md5 from 'js-md5'

const { proxy } = getCurrentInstance()

const formData = reactive({
  password: '',
  newPassword: '',
  confirmPassword: ''
})
const formDataRef = ref()
const submitting = ref(false)

const validatePassword = (rule, value, callback) => {
  proxy.Verify.password(rule, value, (error) => {
    if (error) {
      callback(error)
      return
    }
    if (formData.newPassword) {
      formDataRef.value?.validateField('newPassword')
    }
    callback()
  })
}

const validateNewPasswordDiff = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  if (value === formData.password) {
    callback(new Error('新密码不能和原密码一致'))
    return
  }
  if (formData.confirmPassword) {
    formDataRef.value?.validateField('confirmPassword')
  }
  callback()
}

const validateRePassword = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  if (value !== formData.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  password: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    {
      validator: validatePassword,
      message: '密码只能是8-18位的数字、字母或下划线',
      trigger: 'blur'
    }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: proxy.Verify.password,
      message: '密码只能是8-18位的数字、字母或下划线',
      trigger: 'blur'
    },
    { validator: validateNewPasswordDiff, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请输入确认密码', trigger: 'blur' },
    {
      validator: proxy.Verify.password,
      message: '密码只能是8-18位的数字、字母或下划线',
      trigger: 'blur'
    },
    { validator: validateRePassword, trigger: 'blur' }
  ]
}

const emit = defineEmits(['editBack'])

const resetForm = () => {
  formDataRef.value?.resetFields()
}

const saveUserInfo = async () => {
  if (submitting.value) {
    return
  }
  const valid = await formDataRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  proxy.Confirm({
    message: '修改密码后需要重新登录，是否继续？',
    okfun: async () => {
      submitting.value = true
      const result = await proxy.Request({
        url: proxy.Api.updatePassword,
        method: 'put',
        params: {
          password: md5(formData.password),
          newPassword: md5(formData.newPassword)
        }
      })
      submitting.value = false
      if (!result) {
        return
      }
      resetForm()
      proxy.Message.success('修改成功，即将退出登录', () => {
        window.ipcRenderer.send('reLogin')
      })
    }
  })
}

const cancel = () => {
  resetForm()
  emit('editBack')
}
</script>

<style lang="scss" scoped></style>
