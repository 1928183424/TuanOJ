// 添加一个标志来防止getLoginUser覆盖已登录状态
let hasManuallyLoggedIn = false;

// 导出设置手动登录状态的方法，供登录组件使用
export function setManuallyLoggedIn(value: boolean) {
  hasManuallyLoggedIn = value;
}

// 获取当前登录状态
export function getManuallyLoggedIn() {
  return hasManuallyLoggedIn;
}
