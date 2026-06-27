<template>
  <div>
    <ShowLocalImage :fileId="data.messageId" partType="chat" :fileType="data.fileType" />
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  },
  currentChatSession: {
    type: Object,
    default: () => ({})
  },
})

// console.log("ChatMessageImage:", props.data);

</script>

<style lang="scss" scoped></style>
