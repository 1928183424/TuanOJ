<template>
  <div id="userLayout">
    <a-layout style="height: 400px">
      <a-layout-header class="header">
        <a-space>
          <img src="../assets/sunflower.svg" class="logo" />
          <div>团OJ</div>
        </a-space>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">TuanOJ by 唐振嘉</a-layout-footer>
    </a-layout>
  </div>
</template>

<style scoped>
#userLayout {
}
#userLayout .logo {
  width: 64px;
  height: 64px;
}
#userLayout .header {
}

#userLayout .content {
  margin-bottom: 16px;
  text-align: center;
}

#userLayout .footer {
  background: #efefef;
  padding: 16px;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
<script setup lang="ts"></script>
