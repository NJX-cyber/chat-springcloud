<template>
  <div class="login-panel">
    <div class="title drag">chat</div>
    <div v-if="showLoading" class="loading-panel">
      <img src="../assets/img/loading.gif" alt="Loading...">
    </div>
    <div class="login-form">
      <div class="error-msg">{{ errorMessage }}</div>
      <el-form :model="formData" ref="formDataRef" @submit.prevent>
        <!-- 邮箱输入 -->
        <el-form-item prop="email">
          <div class="email-panel">
            <el-input size="large" clearable placeholder="请输入邮箱" v-model.trim="formData.email" maxlength="30"
              @focus="cleanVerify">
              <template #prefix>
                <span class="iconfont icon-email"></span>
              </template>
            </el-input>
            <el-dropdown v-if="isLogin && localUserList?.length > 0" trigger="click">
              <span class="iconfont icon-down"></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="item in localUserList" :key="item.userId">
                    <div class="email-select" @click="selectLocalUser(item.email)">{{ item.email }}</div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-form-item>

        <!-- 昵称输入 -->
        <el-form-item prop="nickname" v-if="!isLogin">
          <el-input size="large" clearable placeholder="请输入昵称" v-model.trim="formData.nickname" maxlength="15"
            @focus="cleanVerify">
            <template #prefix>
              <span class="iconfont icon-user-nick"></span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 密码输入 -->
        <el-form-item prop="password">
          <el-input size="large" show-password clearable placeholder="请输入密码" v-model.trim="formData.password"
            type="password" @focus="cleanVerify">
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 确认密码输入 -->
        <el-form-item prop="confirmPassword" v-if="!isLogin">
          <el-input size="large" show-password clearable placeholder="请确认密码" v-model.trim="formData.confirmPassword"
            type="password" @focus="cleanVerify">
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 验证码输入 -->
        <el-form-item prop="checkCode">
          <div class="check-code-panel">
            <el-input size="large" clearable placeholder="请输入验证码" v-model.trim="formData.checkCode" @focus="cleanVerify"
              @keydown.enter="handleLogin">
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img :src="checkCodeUrl" class="check-code" @click="changeCheckCode">
          </div>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin">{{ isLogin ? '登录' : '注册' }}</el-button>
        </el-form-item>

        <div class="bottom-link">
          <span class="a-link" @click="changeState">{{ isLogin ? '注册账号' : '立即登录' }}</span>
        </div>

      </el-form>
    </div>
  </div>
  <win-o-p :show-set-top="false" :show-max="false" :show-min="false" :close-type="0"></win-o-p>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue";
const { proxy } = getCurrentInstance();
import md5 from "js-md5";
import { useUserInfoStore } from "../stores/UserInfoStore";
import { useRouter } from "vue-router";
import WinOP from "../components/WinOP.vue";
const userInfoStore = useUserInfoStore()
const router = useRouter()

const showLoading = ref(false)

const getInitialFormData = () => ({
  email: '',
  password: '',
  checkCode: '',
  nickname: '',
  confirmPassword: ''
})

const formData = ref(getInitialFormData())

// 表单的 DOM 引用
const formDataRef = ref()

const isLogin = ref(true)

const errorMessage = ref('')

// 获取验证码
const checkCodeUrl = ref(null)
const changeCheckCode = async () => {
  let result = await proxy.Request({
    url: proxy.Api.checkCode,
    method: 'get',
    params: {
    }
  })
  if (!result) {
    return;
  }
  checkCodeUrl.value = result.data.checkCode;
  localStorage.setItem('checkCodeKey', result.data.checkCodeKey)
}

changeCheckCode()
// 登录按钮点击事件
const handleLogin = async () => {
  cleanVerify()
  if (!checkValue('checkEmail', formData.value.email, '请输入正确的邮箱地址')) {
    return
  }
  if (!isLogin.value && !checkValue(null, formData.value.nickname, '请输入昵称')) {
    return
  }
  if (!checkValue('checkPassword', formData.value.password, '密码只能是8-18位的数字、字母或下划线')) {
    return
  }
  if (!isLogin.value && formData.value.password !== formData.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }
  if (!checkValue(null, formData.value.checkCode, '验证码错误')) {
    return
  }

  if (isLogin.value) {
    showLoading.value = true
  }
  let result = await proxy.Request({
    url: isLogin.value ? proxy.Api.login : proxy.Api.register,
    showLoading: isLogin.value ? true : false,
    method: 'post',
    showError: false,
    params: {
      email: formData.value.email,
      password: md5(formData.value.password),
      nickname: formData.value.nickname,
      checkCode: formData.value.checkCode,
      checkCodeKey: localStorage.getItem('checkCodeKey')
    },
    errorCallback: (response) => {
      changeCheckCode()
      showLoading.value = false
      errorMessage.value = response.info
    },
  })

  if (!result) {
    showLoading.value = false
    return
  }
  if (isLogin.value) {
    userInfoStore.setUserInfo(result.data)
    localStorage.setItem('token', result.data.token)
    window.ipcRenderer.send('setLocalStore', { key: "devDomain", value: proxy.Api.devDomain })
    const screenWidth = window.screen.width
    const screenHeight = window.screen.height

    window.ipcRenderer.once('loginSuccessCallBack', async () => {
      await router.replace('/main')
      showLoading.value = false
    })

    window.ipcRenderer.send('loginSuccess',
      {
        userId: result.data.userId,
        nickname: result.data.nickname,
        token: result.data.token,
        email: formData.value.email,
        admin: result.data.admin,
        screenHeight: screenHeight,
        screenWidth: screenWidth
      }
    )
    return

  } else {
    proxy.Message.success('注册成功，请登录')
    changeState()
  }
  if (showLoading.value) {
    showLoading.value = false
  }
}


const cleanVerify = () => {
  errorMessage.value = ''
}

const checkValue = (type, value, msg) => {
  if (proxy.Utils.isEmpty(value)) {
    errorMessage.value = msg
    return false
  }
  if (type !== null && !proxy.Verify[type](value)) {
    errorMessage.value = msg
    return false
  }

  return true
}

const changeState = () => {
  window.ipcRenderer.send('loginOrRegister', !isLogin.value)
  isLogin.value = !isLogin.value
  nextTick(() => {
    formData.value = getInitialFormData()
    formDataRef.value.resetFields()
    cleanVerify()
    changeCheckCode()
  })
}

const init = () => {
  window.ipcRenderer.send('setLocalStore', { key: "devDomain", value: proxy.Api.devDomain })
  window.ipcRenderer.send('setLocalStore', { key: "prodDomain", value: proxy.Api.prodDomain })
  window.ipcRenderer.send('setLocalStore', { key: "prodWsDomain", value: proxy.Api.prodWsDomain })
  window.ipcRenderer.send('setLocalStore', { key: "devWsDomain", value: proxy.Api.devWsDomain })
  window.ipcRenderer.send('loadLocalUser');
  window.ipcRenderer.on('loadLocalUserCallBack', (event, userList) => {
    localUserList.value = userList
  })
}

//本地用户列表
const localUserList = ref()

const selectLocalUser = (email) => {
  formData.value.email = email
}

onMounted(() => {
  init()
})

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('loadLocalUserCallBack')
})

</script>

<style lang="scss" scoped>
.email-select {
  width: 250px;
}

.loading-panel {
  height: calc(100vh - 32px);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;

  img {
    width: 300px;
  }
}

.login-panel {
  background: #fff;
  border-radius: 3px;
  border: 1px solid #ddd;

  .title {
    height: 30px;
    padding: 5px 0 0 10px;
  }

  .login-form {
    padding: 0 15px 29px 15px;

    :deep(.el-input__wrapper) {
      box-shadow: none;
      border-radius: 0;
    }

    .el-form-item {
      border-bottom: 1px solid #ddd;
    }
    .email-panel { 
      align-items: center;
      width: 100%;
      display: flex;
      .input {
        flex: 1;
      }
      icon-down {
        margin-left: 3px;
        width: 16px;
        cursor: pointer;
        border: none;
      }
    }

    .error-msg {
      color: #fb7373;
      line-height: 30px;
      height: 30px;
    }

    .login-btn {
      margin-top: 20px;
      width: 100%;
      background: #07c160;
      height: 36px;
      font-size: 16px;
      border: none;
    }

    .bottom-link {
      text-align: right;
    }

  }

  .check-code-panel {
    display: flex;
  }
}
</style>
