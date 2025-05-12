import router from "@/router";
import store from "@/store";
import ACCESSENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";
import { getManuallyLoggedIn } from "@/access/loginState";

// 在访问页面之前进行判断
router.beforeEach(async (to, from, next) => {
  console.log("路由守卫 - 当前登录用户信息", store.state.user.loginUser);
  let loginUser = store.state.user.loginUser;

  // 只有在未手动登录且未登录状态时才尝试自动登录
  if (
    !getManuallyLoggedIn() &&
    (!loginUser ||
      !loginUser.userRole ||
      loginUser.userRole === ACCESSENUM.NOT_LOGIN)
  ) {
    // 尝试从localStorage恢复
    try {
      const savedUserString = localStorage.getItem("loginUser");
      if (savedUserString) {
        const savedUser = JSON.parse(savedUserString);
        if (
          savedUser &&
          savedUser.userRole &&
          savedUser.userRole !== ACCESSENUM.NOT_LOGIN
        ) {
          store.commit("user/updateUser", savedUser);
          loginUser = savedUser;
          console.log("从localStorage恢复用户状态:", loginUser);
        } else {
          console.log("localStorage中的用户状态无效");
        }
      } else {
        console.log("localStorage中没有保存的用户数据");
      }
    } catch (e) {
      console.error("恢复用户状态出错:", e);
    }
  }

  const needAccess = (to.meta?.access as string) ?? ACCESSENUM.NOT_LOGIN;

  // 要跳转的页面必须要登录
  if (needAccess !== ACCESSENUM.NOT_LOGIN) {
    // 但是没有登录
    if (
      !loginUser ||
      !loginUser.userRole ||
      loginUser.userRole === ACCESSENUM.NOT_LOGIN
    ) {
      console.log("需要登录权限，但用户未登录，跳转到登录页");
      // 跳转到登录页
      next(`/user/login?redirect=${to.fullPath}`);
      return;
    }
    // 如果已经登录了，但是权限不足，那么跳转到无权限页面
    if (!checkAccess(loginUser, needAccess)) {
      console.log("用户权限不足，跳转到无权限页面");
      next("/noAuth");
      return;
    }
  }

  next();
});
