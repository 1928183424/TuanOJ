<template>
  <div id="userRegisterView">
    <h1>用户注册</h1>
    <a-form :model="form" :style="{ width: '550px' }" @submit="handleSubmit">
      <a-form-item field="userName" label="昵称">
        <a-input v-model="form.userName" placeholder="请输入昵称" />
      </a-form-item>
      <a-form-item field="userAccount" label="账号">
        <a-input v-model="form.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不少于8位" label="密码">
        <a-input-password
          v-model="form.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item
        field="checkPassword"
        tooltip="请再次输入密码"
        label="确认密码"
      >
        <a-input-password
          v-model="form.checkPassword"
          placeholder="请再次输入密码"
        />
      </a-form-item>
      <a-form-item class="submit-btn">
        <a-button type="primary" html-type="submit">注册</a-button>
        <a-button style="margin-left: 10px" @click="toLogin">返回登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { reactive } from "vue";
import { UserControllerService, UserRegisterRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";

const router = useRouter();
/**
 * 表单信息
 */
const form = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
  userName: "",
} as UserRegisterRequest);

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 表单验证
  if (!form.userAccount) {
    message.error("账号不能为空");
    return;
  }
  if (form.userAccount.length < 4) {
    message.error("账号长度不能小于4位");
    return;
  }
  if (!form.userPassword) {
    message.error("密码不能为空");
    return;
  }
  if (form.userPassword.length < 8) {
    message.error("密码长度不能小于8位");
    return;
  }
  if (form.userPassword !== form.checkPassword) {
    message.error("两次输入的密码不一致");
    return;
  }

  try {
    const res = await UserControllerService.userRegisterUsingPost(form);
    if (res.code === 0) {
      message.success("注册成功");
      // 注册成功，跳转到登录页
      router.push("/user/login");
    } else {
      message.error("注册失败," + res.message);
    }
  } catch (error) {
    message.error("注册失败，请稍后再试");
    console.error(error);
  }
};

/**
 * 跳转到登录页
 */
const toLogin = () => {
  router.push("/user/login");
};
</script>

<style scoped>
#userRegisterView {
  position: absolute;
  top: 30%; /* 从页面顶部的 30% 处开始 */
  left: 50%;
  transform: translate(-50%, -30%); /* 水平居中，垂直调整 */
  width: 100%;
  max-width: 600px; /* 限制最大宽度 */
  padding: 20px;
}

h1 {
  text-align: center;
  margin-bottom: 20px;
}
</style>
