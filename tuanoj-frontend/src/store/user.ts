import { StoreOptions } from "vuex";
import ACCESSENUM from "@/access/accessEnum";
import { UserControllerService } from "../../generated";

export default {
  namespaced: true,

  // 修改状态初始化，加入从localStorage恢复的逻辑
  state: () => {
    // 尝试从localStorage恢复用户状态
    let savedUser = null;
    try {
      const savedUserString = localStorage.getItem("loginUser");
      if (savedUserString) {
        savedUser = JSON.parse(savedUserString);
        console.log("从localStorage恢复用户状态:", savedUser);
      }
    } catch (e) {
      console.error("从localStorage恢复用户状态失败:", e);
    }

    // 如果有保存的用户数据则使用它，否则使用默认值
    return {
      loginUser: savedUser || {
        userName: "未登录",
        userRole: ACCESSENUM.NOT_LOGIN,
      },
    };
  },

  actions: {
    async getLoginUser({ commit, state }, payload) {
      try {
        // 从远程请求获取登录信息
        const res = await UserControllerService.getLoginUserUsingGet();
        console.log("获取登录用户信息响应:", res);

        // 登录成功，更新用户信息
        if (res.code === 0 && res.data) {
          commit("updateUser", res.data);
          return res.data; // 返回用户数据供调用方使用
        } else {
          // 登录失败，设置为未登录状态
          const notLoginUser = {
            ...state.loginUser,
            userName: "未登录",
            userRole: ACCESSENUM.NOT_LOGIN,
          };
          commit("updateUser", notLoginUser);
          return notLoginUser;
        }
      } catch (error) {
        console.error("获取登录用户信息失败:", error);
        // 出错时也设置为未登录状态
        const errorUser = {
          ...state.loginUser,
          userName: "未登录",
          userRole: ACCESSENUM.NOT_LOGIN,
        };
        commit("updateUser", errorUser);
        // 将错误向上抛出，让调用方知道发生了错误
        throw error;
      }
    },

    // 可以添加一个专门的登录action
    setLoginUser({ commit }, userData) {
      commit("updateUser", userData);
    },
  },

  mutations: {
    updateUser(state, payload) {
      // 添加日志以便调试
      console.log("更新用户状态 - 旧值:", JSON.stringify(state.loginUser));
      console.log("更新用户状态 - 新值:", JSON.stringify(payload));

      // 更新state
      state.loginUser = payload;

      // 同步到localStorage
      try {
        localStorage.setItem("loginUser", JSON.stringify(payload));
        console.log("已同步用户状态到localStorage");
      } catch (e) {
        console.error("同步用户状态到localStorage失败:", e);
      }
    },

    // 添加清除用户信息的mutation，用于登出
    clearUser(state) {
      state.loginUser = {
        userName: "未登录",
        userRole: ACCESSENUM.NOT_LOGIN,
      };

      // 从localStorage中移除
      try {
        localStorage.removeItem("loginUser");
      } catch (e) {
        console.error("从localStorage移除用户状态失败:", e);
      }
    },
  },
} as StoreOptions<any>;
