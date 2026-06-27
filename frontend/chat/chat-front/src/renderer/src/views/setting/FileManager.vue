<template>
  <ContentPanel v-loading="copying" element-loading-text="正在复制文件">
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" label-position="top" @submit.prevent>
      <!--input输入-->
      <el-form-item label="文件管理" class="file-manager">
        <div class="file-input" :title="formData?.sysSetting">{{ formData?.sysSetting }}</div>
        <div class="tips">文件的默认保存位置</div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="changeFolder">更改保存路径</el-button> 
        <el-button type="primary" @click="openFolder">打开文件保存位置</el-button>
      </el-form-item>
    </el-form>
  </ContentPanel>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();

const copying = ref(false);

const formData = ref({});
const formDataRef = ref();
const rules = {
  title: [{required: true, message: "请输入内容", trigger: "blur"}]
}

const changeFolder = () => { 
  window.ipcRenderer.send("changeLocalFolder");
}

const openFolder = () => { 
  window.ipcRenderer.send("openLocalFolder");
}

const getSystemSetting = () => { 
  window.ipcRenderer.send("getSystemSetting");
}

const getSystemSettingCallBack = () => { 
  window.ipcRenderer.on("getSystemSettingCallBack", (event, data) => { 
    console.log("getSystemSettingCallBack", data);
    formData.value.sysSetting = data;
    copying.value = false;
  })
}

const onCopyFolderCallBack = () => { 
  window.ipcRenderer.on("copyFolderCallBack", (event, data) => { 
    copying.value = true;
  })
}

onMounted(() => { 
  getSystemSetting();
  getSystemSettingCallBack();
  onCopyFolderCallBack();
})

onUnmounted(() => { 
  window.ipcRenderer.removeAllListeners("getSystemSettingCallBack");
  window.ipcRenderer.removeAllListeners("copyFolderCallBack");
})

</script>

<style lang="scss" scoped>
.file-manager {
  :deep(.el-form-item__content) {
    display: block;
  }

  .file-input {
    background: #fff;
    padding: 0 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 16px;
  }

  .tips {
    color: #888888;
    font-size: 13px;
  }
}
</style>
