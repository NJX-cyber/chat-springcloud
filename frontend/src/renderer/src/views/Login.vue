<template>
  <div class="login-wrapper">
    <div v-if="showLoading" class="loading-mask">
      <p>正在登录...</p>
    </div>

    <div class="login-panel">
      <div class="panel-header drag">
        <span class="panel-title">Chat</span>
      </div>

      <div class="panel-body">
        <div class="error-tip" v-if="errorMessage">{{ errorMessage }}</div>

        <el-form :model="formData" ref="formDataRef" @submit.prevent>
          <!-- 邮箱 -->
          <el-form-item prop="email">
            <div class="email-panel">
              <el-input size="large" clearable placeholder="邮箱" v-model.trim="formData.email" maxlength="30"
                @focus="cleanVerify">
              </el-input>
              <el-dropdown v-if="isLogin && localUserList?.length > 0" trigger="click">
                <span class="dropdown-btn iconfont icon-down"></span>
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

          <!-- 昵称 -->
          <el-form-item prop="nickname" v-if="!isLogin">
            <el-input size="large" clearable placeholder="昵称" v-model.trim="formData.nickname" maxlength="15"
              @focus="cleanVerify">
            </el-input>
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input size="large" show-password clearable placeholder="密码" v-model.trim="formData.password"
              type="password" @focus="cleanVerify">
            </el-input>
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item prop="confirmPassword" v-if="!isLogin">
            <el-input size="large" show-password clearable placeholder="确认密码" v-model.trim="formData.confirmPassword"
              type="password" @focus="cleanVerify">
            </el-input>
          </el-form-item>

          <!-- 验证码 -->
          <el-form-item prop="checkCode">
            <div class="check-code-panel">
              <el-input size="large" clearable placeholder="验证码" v-model.trim="formData.checkCode" @focus="cleanVerify"
                @keydown.enter="handleLogin">
              </el-input>
              <img :src="checkCodeUrl" class="captcha-img" @click="changeCheckCode" alt="验证码">
            </div>
          </el-form-item>

          <!-- 按钮 -->
          <el-button type="primary" class="submit-btn" @click="handleLogin" :loading="showLoading">
            {{ isLogin ? '登录' : '注册' }}
          </el-button>

          <div class="bottom-link">
            <span class="a-link" @click="changeState">
              {{ isLogin ? '注册账号' : '立即登录' }}
            </span>
          </div>
        </el-form>
      </div>
    </div>

    <win-o-p :show-set-top="false" :show-max="false" :show-min="false" :close-type="0"></win-o-p>
  </div>
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

.login-wrapper {
  width: 100vw;
  height: 100vh;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.loading-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;

  p {
    color: #fff;
    font-size: 14px;
  }
}

.login-panel {
  width: 100%;
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 30px 0 20px;
  text-align: center;

  .panel-title {
    font-size: 22px;
    font-weight: 500;
    color: #333;
  }
}

.panel-body {
  flex: 1;
  padding: 0 30px 25px;
  overflow-y: auto;
}

.error-tip {
  color: #fa5151;
  font-size: 13px;
  margin-bottom: 10px;
}

:deep(.el-form-item) {
  margin-bottom: 14px;
}

:deep(.el-input__wrapper) {
  box-shadow: none;
  border-radius: 0;
}

.email-panel {
  display: flex;
  align-items: center;

  .el-input {
    flex: 1;
  }

  .dropdown-btn {
    margin-left: 4px;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #999;
    font-size: 14px;

    &:hover {
      color: #07c160;
    }
  }
}

.check-code-panel {
  display: flex;
  gap: 8px;
  align-items: center;

  .el-input {
    flex: 1;
  }

  .captcha-img {
    height: 40px;
    cursor: pointer;
  }
}

.submit-btn {
  width: 100%;
  height: 40px;
  margin-top: 10px;
  font-size: 15px;
}

.bottom-link {
  text-align: center;
  margin-top: 12px;
}
</style>
