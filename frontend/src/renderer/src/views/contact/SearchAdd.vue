<template>
  <div>
    <Dialogue 
        :show="dialogConfig.show" 
        :title="dialogConfig.title" 
        :buttons="dialogConfig.buttons"
        width="400px" 
        :showCancel="false" 
        @close="dialogConfig.show = false"
        >
        <el-form :model="formData" ref="formDataRef" @submit.prevent="">
            <el-form-item label="" prop="title">
                <el-input type="textarea" :rows="5" clearable placeholder="申请信息" v-model.trim="formData.applyInfo" resize="none" show-word-limit maxlength="100"></el-input>
            </el-form-item>
        </el-form>

    </Dialogue>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import {useUserInfoStore} from "@/stores/UserInfoStore";
const useUserInfo = useUserInfoStore();
import { useContactStateStore } from "../../stores/ContactStateStore";
const contactStateStore = useContactStateStore();

const dialogConfig = reactive({
  show: false,
  title: "提交申请",
  buttons: [
   {
    type: "primary",
    text: "确定",
    click: (e) => {
      submitApply();
    },
  },
 ],
});

const formData = ref({});
const formDataRef = ref();
const emit = defineEmits(["reload"]);
const submitApply = async () => {
  const { contactId, applyInfo, contactType } = formData.value;
  let result = await proxy.Request({
     url: proxy.Api.applyAdd,
     method: 'post',
      params:{
        contactId,
        applyInfo,
        contactType
      }
  })
  if(!result){
    return;
  }
  if(result.data === 0){
    proxy.Message.success("加入成功");
    contactStateStore.setContactReload(null);
    contactStateStore.setContactReload(contactType);
  }else{
    proxy.Message.success("申请成功, 等待对方处理");
  }
  dialogConfig.show = false;
  emit("reload");
};

const show = (data) => {
  dialogConfig.show = true;
  nextTick(() => {
    formDataRef.value.resetFields();
    formData.value = Object.assign({}, data);
    console.log(formData.value);
    formData.value.applyInfo = `我是${useUserInfo.getInfo().nickname}，想加${String(formData.value.contactId).startsWith('U') ? '你为好友' : '入群聊'}。`;
  });
};

defineExpose({
  show,
});

</script>

<style lang="scss" scoped>
</style>
