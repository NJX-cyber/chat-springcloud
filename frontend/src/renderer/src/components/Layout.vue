<template>
  <div class="layout-container">
    <div class="left-side-inner">
        <slot name="left-content"></slot>
    </div>
    <div class="right-content">
        <slot name="right-content"></slot>
    </div>
  </div>
</template>

<script setup>

</script>

<style lang="scss" scoped>
.layout-container {
    display: flex;
    height: 100%;

    .left-side-inner { 
        width: 250px;
        background: #f0eeff;
        border-right: 1px solid #e5e7eb;
        overflow-y: auto;
    }

    .right-content {
        flex: 1;
        height: 100%;
        background: #f8f7ff;
        width: 100%;
        overflow: hidden;
    }
}
</style>
