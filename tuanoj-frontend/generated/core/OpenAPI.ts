/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ApiRequestOptions } from "./ApiRequestOptions";

type Resolver<T> = (options: ApiRequestOptions) => Promise<T>;
type Headers = Record<string, string>;

export type Middleware = {
  pre?: (config: any) => Promise<any>;
  post?: (response: any) => Promise<any>;
};

export type OpenAPIConfig = {
  BASE: string;
  VERSION: string;
  WITH_CREDENTIALS: boolean;
  CREDENTIALS: "include" | "omit" | "same-origin";
  TOKEN?: string | Resolver<string> | undefined;
  USERNAME?: string | Resolver<string> | undefined;
  PASSWORD?: string | Resolver<string> | undefined;
  HEADERS?: Headers | Resolver<Headers> | undefined;
  ENCODE_PATH?: ((path: string) => string) | undefined;
  MIDDLEWARES?: Middleware[];
};

// 自定义请求中间件
const requestMiddleware = async (config: any) => {
  // 获取认证信息
  const loginUserStr = localStorage.getItem("loginUser");

  console.log("发送API请求:", config.url);
  console.log("请求方法:", config.method);
  console.log("请求头(处理前):", JSON.stringify(config.headers));

  // 确保headers对象存在
  config.headers = config.headers || {};

  // 添加认证信息到请求头
  if (loginUserStr) {
    try {
      const loginUser = JSON.parse(loginUserStr);
      console.log("当前登录用户:", loginUser);

      // 特别关注用户角色，确认是否为已登录状态
      if (loginUser.userRole && loginUser.userRole !== "notLogin") {
        console.log("用户已登录，角色:", loginUser.userRole);

        // 根据你后端的认证方式选择适当的方法
        // 方法1: 如果后端使用JWT认证
        if (loginUser.token) {
          config.headers.Authorization = `Bearer ${loginUser.token}`;
          console.log("已添加JWT认证头");
        }
        // 方法2: 如果后端直接使用用户ID作为认证
        else if (loginUser.id) {
          config.headers["X-User-Id"] = loginUser.id;
          console.log("已添加用户ID头");
        }

        // 方法3: 传递完整用户信息（如果你的后端支持）
        config.headers["X-User-Info"] = loginUserStr;
        console.log("已添加用户信息头");
      } else {
        console.log("用户未登录或角色无效:", loginUser.userRole);
      }
    } catch (e) {
      console.error("解析loginUser出错:", e);
    }
  } else {
    console.log("未找到保存的用户信息");
  }

  // 确保总是启用凭证（对于Cookie认证）
  config.credentials = "include";

  console.log("请求头(处理后):", JSON.stringify(config.headers));
  return config;
};

// 响应中间件
const responseMiddleware = async (response: any) => {
  console.log("API响应状态:", response.status);

  if (response.data) {
    console.log("API响应数据:", response.data);

    // 检查未登录状态
    if (
      response.data.code === 40100 ||
      (response.data.message && response.data.message.includes("未登录"))
    ) {
      console.error("检测到未登录状态:", response.data);

      // 尝试重新从localStorage获取用户信息并打印，以便对比
      try {
        const loginUserStr = localStorage.getItem("loginUser");
        console.log(
          "localStorage中的用户信息:",
          loginUserStr ? JSON.parse(loginUserStr) : null
        );
      } catch (e) {
        console.error("获取localStorage用户信息出错:", e);
      }

      // 你可以选择在这里添加其他逻辑，如提示用户重新登录
      // window.location.href = '/user/login'; // 重定向到登录页
    }
  }

  return response;
};

export const OpenAPI: OpenAPIConfig = {
  BASE: "http://47.94.249.3:8121",
  VERSION: "1.0",
  WITH_CREDENTIALS: true,
  CREDENTIALS: "include",
  TOKEN: undefined,
  USERNAME: undefined,
  PASSWORD: undefined,
  HEADERS: {
    "Content-Type": "application/json",
  },
  MIDDLEWARES: [
    {
      pre: requestMiddleware,
      post: responseMiddleware,
    },
  ],
};
