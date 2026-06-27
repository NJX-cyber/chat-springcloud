<template>
  <Dialogue
  :show="dialogConfig.show"
  :title="dialogConfig.title"
  :buttons="dialogConfig.buttons"
  width="400px"
  :showCancel="false"
  @close="dialogConfig.show = false"
  >
    <GroupEditForm ref="groupEditRef" @editBack="editBack"></GroupEditForm>
  </Dialogue>
</template>

<script setup>
import GroupEditForm from './GroupEditForm.vue';
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const dialogConfig = reactive({
    show: false,
    title: '修改群信息',
    buttons: []
})

const groupEditRef = ref();

const show = (data) => {
    dialogConfig.show = true;
    nextTick(() => {
      groupEditRef.value.show(data);
    });
}

const emit = defineEmits(['reloadGroupInfo','show'])

defineExpose({
    show
})
const editBack = () => {
    dialogConfig.show = false;
    emit('reloadGroupInfo');
}

</script>

<style lang="scss" scoped>
</style>
