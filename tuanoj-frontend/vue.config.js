const { defineConfig } = require("@vue/cli-service");
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");

module.exports = defineConfig({
  transpileDependencies: true,
  outputDir: "dist",
  assetsDir: "static",

  //配置 MonacoWebpack 插件
  chainWebpack(config) {
    config.plugin("monaco").use(new MonacoWebpackPlugin());
  },

  //忽略 ResizeObserver 的警告
  devServer: {
    client: {
      overlay: false,
    },
  },
});
