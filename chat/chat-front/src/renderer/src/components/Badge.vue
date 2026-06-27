<template>
  <div class="badge" :style="{ top: top + 'px', left: left + 'px'}" v-if="count > 0">{{ count > 99 ? '99+' : count }}</div>
</template>

<script setup>
import { ref, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue";
const { proxy } = getCurrentInstance();

const props = defineProps({
  top: {
    type: Number,
    default: 0
  },
  left: {
    type: Number,
    default: 0
  },
  count: {
    type: Number,
    default: 0
  }
})

</script>

<style lang="scss" scoped>
.badge {
    position: absolute;
    padding: 1px 6px;
    border-radius: 10px;
    background: #fa5151;
    color: #fff;
    z-index: 1;
    font-size: 12px;
    zoom: 0.85;
}
</style>
