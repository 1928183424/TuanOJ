package com.tzj.tuanojcodesandbox.corecodesandbox.pythoncodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.tzj.tuanojcodesandbox.corecodesandbox.javacodesandbox.JavaCoreCodeSandboxTemplate;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import com.tzj.tuanojcodesandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * python 核心代码模式 Docker 代码沙箱
 */
@Component
public class PythonDockerCoreCodeSandbox extends PythonCoreCodeSandboxTemplate {

    // 使用volatile确保多线程可见性，并改为AtomicBoolean更合适
    private static volatile Boolean FIRST_INIT = true;

    // 超时时间常量
    private static final long TIME_OUT = 5000L;

    // 内存限制常量
    private static final long MEMORY_LIMIT = 100 * 1000 * 1000L;

    // Docker镜像名称常量
    private static final String DOCKER_IMAGE = "python:3.9-slim";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }

    /**
     * 创建容器，把文件复制到容器内并运行
     * @param userCodeFile 用户代码文件
     * @param inputList 输入参数列表
     * @return 执行结果列表
     */
    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        DockerClient dockerClient = null;
        String containerId = null;
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        try {
            // 获取Docker客户端
            dockerClient = DockerClientBuilder.getInstance().build();

//            // 初始化Docker镜像（仅第一次）
//            initDockerImage(dockerClient);

            // 创建并启动容器
            containerId = createAndStartContainer(dockerClient, userCodeParentPath);

            // 执行代码并收集结果
            executePythonCodeInContainer(dockerClient, containerId, inputList, executeMessageList);

        } catch (Exception e) {
            System.err.println("代码执行异常: " + e.getMessage());
            e.printStackTrace();
            // 添加执行异常信息到结果
            ExecuteMessage errorMessage = new ExecuteMessage();
            errorMessage.setErrorMessage("系统错误：" + e.getMessage());
            executeMessageList.add(errorMessage);
        } finally {
            // 清理资源
            cleanupResources(dockerClient, containerId);
        }

        return executeMessageList;
    }

    /**
     * 初始化Docker镜像
     */
    private void initDockerImage(DockerClient dockerClient) {
        // 使用synchronized块确保线程安全
        if (FIRST_INIT) {
            synchronized (PythonDockerCoreCodeSandbox.class) {
                if (FIRST_INIT) {
                    System.out.println("开始拉取镜像：" + DOCKER_IMAGE);
                    PullImageCmd pullImageCmd = dockerClient.pullImageCmd(DOCKER_IMAGE);
                    PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                        @Override
                        public void onNext(PullResponseItem item) {
                            System.out.println("下载镜像：" + item.getStatus());
                            super.onNext(item);
                        }
                    };
                    try {
                        pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
                        System.out.println("下载镜像完成");
                        FIRST_INIT = false;
                    } catch (InterruptedException e) {
                        System.err.println("拉取镜像异常: " + e.getMessage());
                        throw new RuntimeException("拉取Docker镜像失败", e);
                    }
                }
            }
        }
    }

    /**
     * 创建并启动Docker容器
     */
    private String createAndStartContainer(DockerClient dockerClient, String userCodeParentPath) {
        // 创建容器配置
        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        hostConfig.withMemory(MEMORY_LIMIT); // 限制内存
        hostConfig.withCpuCount(1L); // 限制CPU个数

        // 创建容器
        CreateContainerResponse container = dockerClient.createContainerCmd(DOCKER_IMAGE)
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)      // 禁用网络
                .withReadonlyRootfs(true)       // 只读文件系统
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(true)
                .exec();

        String containerId = container.getId();
        System.out.println("容器创建成功，ID: " + containerId);

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        System.out.println("容器启动成功");

        return containerId;
    }

    /**
     * 在容器中执行Python代码
     */
    private void executePythonCodeInContainer(
            DockerClient dockerClient,
            String containerId,
            List<String> inputList,
            List<ExecuteMessage> executeMessageList) {

        StopWatch stopWatch = new StopWatch();

        for (String inputArgs : inputList) {
            // 每次执行前创建新的计时器实例
            stopWatch = new StopWatch();

            String[] cmdArray = ArrayUtil.append(new String[]{"python3","-u","/app/Main.py"}, inputArgs);

            // 创建执行命令
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();

            String execId = execCreateCmdResponse.getId();
            System.out.println("创建执行命令: " + execId);

            // 准备结果容器
            ExecuteMessage executeMessage = new ExecuteMessage();
            final StringBuilder messageBuilder = new StringBuilder();
            final StringBuilder errorMessageBuilder = new StringBuilder();
            final boolean[] memoryExceeded = {false};
            final long[] maxMemory = {0L};

            // 创建内存统计回调
            final ResultCallback<Statistics>[] statsCallbackRef = new ResultCallback[1];
            ResultCallback<Statistics> statisticsResultCallback = new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    if (statistics != null && statistics.getMemoryStats() != null) {
                        Long usage = statistics.getMemoryStats().getUsage();
                        if (usage != null) {
                            System.out.println("内存占用：" + usage);
                            maxMemory[0] = Math.max(usage, maxMemory[0]);
                        }

                        // 如果内存使用接近限制值(例如90%)，则提前终止
                        if (usage > MEMORY_LIMIT * 0.9) {
                            System.out.println("内存即将耗尽，终止执行");
                            // 标记内存超限
                            memoryExceeded[0] = true;
                            try {
                                // 关闭回调
                                close();
                            } catch (Exception e) {
                                System.err.println("关闭内存监控回调异常: " + e.getMessage());
                            }
                        }
                    }
                }

                @Override
                public void close() {
                    System.out.println("内存统计回调关闭");
                }

                @Override
                public void onStart(Closeable closeable) {
                    // 保存引用以便后续关闭
                    statsCallbackRef[0] = this;
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("获取统计信息错误: " + throwable.getMessage());
                    try {
                        close();
                    } catch (Exception e) {
                        System.err.println("关闭统计回调出错: " + e.getMessage());
                    }
                }

                @Override
                public void onComplete() {
                    System.out.println("内存统计完成");
                    try {
                        close();
                    } catch (Exception e) {
                        System.err.println("关闭统计回调出错: " + e.getMessage());
                    }
                }
            };

            // 创建输出回调
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    String payload = new String(frame.getPayload()).trim();

                    if (StreamType.STDERR.equals(streamType)) {
                        System.out.println("输出错误结果：" + payload);
                        errorMessageBuilder.append(payload);
                    } else if (StreamType.STDOUT.equals(streamType)) {
                        System.out.println("输出结果：" + payload);
                        messageBuilder.append(payload);
                    }
                    super.onNext(frame);
                }
            };

            // 启动内存统计
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            statsCmd.exec(statisticsResultCallback);

            // 监控线程
            final Thread monitorThread = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                while (!Thread.currentThread().isInterrupted()) {
                    // 检查是否超时或内存超限
                    if (System.currentTimeMillis() - startTime > 0.9 * TIME_OUT ) {
                        try {
                            System.out.println("检测到超时，强制终止容器");
                            // 强制停止容器
                            dockerClient.stopContainerCmd(containerId).withTimeout(1).exec();
                            executeMessage.setTime(10000L);
                            executeMessage.setMemory(maxMemory[0]);
                            executeMessage.setErrorMessage("超时");
                            executeMessageList.add(executeMessage);
                            break;
                        } catch (Exception e) {
                            System.err.println("强制终止容器异常: " + e.getMessage());
                        }
                    }
                    if(memoryExceeded[0]){
                        try {
                            System.out.println("检测到内存超限，强制终止容器");
                            // 强制停止容器
                            dockerClient.stopContainerCmd(containerId).withTimeout(1).exec();
                            executeMessage.setTime(System.currentTimeMillis() - startTime);
                            executeMessage.setMemory(200 * 1000 * 1000L);
                            executeMessage.setErrorMessage("内存超限");
                            executeMessageList.add(executeMessage);
                            break;
                        } catch (Exception e) {
                            System.err.println("强制终止容器异常: " + e.getMessage());
                        }
                    }

                    try {
                        Thread.sleep(100); // 每100ms检查一次
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });

            // 启动监控线程
            monitorThread.start();

            try {
                // 执行命令并计时
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                stopWatch.stop();

                executeMessage.setMemory(maxMemory[0]);
                executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
                String messageOutput = messageBuilder.toString();
                // 限制输出结果长度，防止数据库存不下
                if(messageOutput.length() > 100){
                    messageOutput = messageOutput.substring(0, 100);
                }
                executeMessage.setMessage(messageOutput);
                executeMessage.setErrorMessage(errorMessageBuilder.toString());
                executeMessageList.add(executeMessage);

            } catch (InterruptedException e) {
                System.err.println("程序执行中断: " + e.getMessage());
                executeMessage.setErrorMessage("程序执行异常：" + e.getMessage());
                executeMessageList.add(executeMessage);
            } finally {
                // 终止监控线程
                monitorThread.interrupt();
                try {
                    monitorThread.join(1000); // 等待监控线程终止
                } catch (InterruptedException e) {
                    System.err.println("等待监控线程终止异常: " + e.getMessage());
                }

                // 确保关闭内存统计回调
                if (statsCallbackRef[0] != null) {
                    try {
                        statsCallbackRef[0].close();
                    } catch (IOException e) {
                        System.err.println("关闭统计回调出错: " + e.getMessage());
                    }
                }

                // 关闭统计命令
                try {
                    statsCmd.close();
                } catch (Exception e) {
                    System.err.println("关闭统计命令异常: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 清理资源
     */
    private void cleanupResources(DockerClient dockerClient, String containerId) {
        if (containerId != null && dockerClient != null) {
            // 停止容器（先检查容器状态）
            try {
                // 获取容器信息
                InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
                // 检查容器是否已经停止
                if (containerInfo != null && containerInfo.getState() != null && containerInfo.getState().getRunning()) {
                    System.out.println("停止容器: " + containerId);
                    dockerClient.stopContainerCmd(containerId).withTimeout(10).exec();
                } else {
                    System.out.println("容器已停止，跳过停止操作: " + containerId);
                }

                // 删除容器
                System.out.println("删除容器: " + containerId);
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                System.out.println("删除成功");
            } catch (Exception e) {
                System.err.println("容器停止或删除过程中出现异常: " + e.getMessage());
                // 如果普通删除失败，尝试强制删除
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception ex) {
                    System.err.println("强制删除容器失败: " + ex.getMessage());
                }
            }

            try {
                // 关闭Docker客户端
                dockerClient.close();
                System.out.println("Docker客户端关闭");
            } catch (IOException e) {
                System.err.println("关闭Docker客户端异常: " + e.getMessage());
            }
        }
    }
}
