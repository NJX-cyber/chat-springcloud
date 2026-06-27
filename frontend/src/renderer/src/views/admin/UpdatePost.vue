<template>
  <Dialogue :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" :width="'600px'" @close="dialogConfig.show = false">
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="100px" @submit.prevent>
      <el-form-item label="版本号">
        {{ formData.version }}
      </el-form-item>
      <el-form-item label="发布状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio-button :value="0">取消发布</el-radio-button>
          <el-radio-button :value="1">灰度发布</el-radio-button>
          <el-radio-button :value="2">全网发布</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="灰度ID" prop="garyscaleUid" v-if="formData.status == 1">
        <div class="tag-panel">
          <el-tag v-for="(item, index) in formData.garyscaleUid" :key="item" closable @close="closeTag(index)">{{ item
            }}</el-tag>
        </div>
        <div class="tag input" v-if="showInput">
          <el-input size="small" clearable placeholder="请输入UID" v-model="tagInput" @keyup.enter="addDeviceId"
            @blur="addDeviceId"></el-input>
        </div>
        <div class="tag" v-if="!showInput">
          <el-button type="primary" size="small" @click="showInputHandler">增加</el-button>
        </div>
      </el-form-item>
    </el-form>
  </Dialogue>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted, nextTick } from "vue";
const { proxy } = getCurrentInstance();

const dialogConfig = ref({
  show: false,
  title: "发布更新",
  buttons: [
    {
      type: "primary",
      text: "确定",
      click: (e) => {
        submitForm();
      },
    }
  ]
})

const formData = ref({});
const formDataRef = ref(null);
const rules = ref({
  status: [
    { required: true, message: "请选择发布状态" },
  ],
});
const emit = defineEmits(["reload"]);

const submitForm = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {}
    Object.assign(params, formData.value)
    let result = await proxy.Request({
      url: proxy.Api.postUpdate,
      params,
    });
    if (!result) {
      return;
    }
    dialogConfig.value.show = false;
    emit("reload");
    proxy.Message.success("发布成功");
  })
};

const showEdit = (data) => {
  dialogConfig.value.show = true;
  nextTick(() => {
    formDataRef.value.resetFields()
    formData.value = Object.assign({}, {
      id: data.id,
      version: data.version,
      status: data.status,
      garyscaleUid: data.garyscaleUid ? data.garyscaleUid.split(",") : [],

    })
  })
};

defineExpose({
  showEdit,
});

const tagInput = ref();
const showInput = ref(false);
const addDeviceId = () => {
  if (tagInput.value) {
    formData.value.garyscaleUid.push(tagInput.value);
  }
  tagInput.value = "";
  showInput.value = false;
};

const showInputHandler = () => {
  showInput.value = true;
};

const closeTag = (index) => {
  formData.value.garyscaleUid.splice(index, 1);
};

</script>

<style lang="scss" scoped>
.tag-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: center;

  .tag {
    margin: 0 5px 5px 0;
  }

  .input {
    width: 150px;
  }
}
</style>
