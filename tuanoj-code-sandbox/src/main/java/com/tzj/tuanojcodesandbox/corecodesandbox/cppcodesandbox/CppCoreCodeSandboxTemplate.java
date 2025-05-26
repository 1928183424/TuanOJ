package com.tzj.tuanojcodesandbox.corecodesandbox.cppcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.tzj.tuanojcodesandbox.corecodesandbox.CoreCodeSandboxTemplate;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import com.tzj.tuanojcodesandbox.model.ExecuteMessage;
import com.tzj.tuanojcodesandbox.model.JudgeConfig;
import com.tzj.tuanojcodesandbox.utils.CppCoreCodeGenerateUtils;
import com.tzj.tuanojcodesandbox.utils.JavaCoreCodeGenerateUtils;
import com.tzj.tuanojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * cpp 核心代码模式沙箱模板
 * 重写 saveToFile、compileFile、runFile
 */
public abstract class CppCoreCodeSandboxTemplate extends CoreCodeSandboxTemplate {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.cpp";

    private static final Long TIME_OUT = 5000L;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }

    @Override
    public File saveToFile(String solutionCode, JudgeConfig judgeConfig) {
        // 获取项目根目录
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        CppCoreCodeGenerateUtils coreCodeGenerateUtils = new CppCoreCodeGenerateUtils();

        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 创建用户代码目录
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        FileUtil.mkdir(userCodeParentPath);

        // 生成Main.java - 接收命令行参数方式
        String mainCode = coreCodeGenerateUtils.generateCppMainCode(solutionCode, judgeConfig);
        String mainPath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File mainFile = FileUtil.writeString(mainCode, mainPath, StandardCharsets.UTF_8);

        return mainFile;
    }

    @Override
    public ExecuteMessage compileFile(File userCodeFile) {
        // 获取父目录路径，以便编译整个目录下的C++文件
        String parentPath = userCodeFile.getParentFile().getAbsolutePath();
        String executablePath = parentPath + File.separator + "main";

        // 修改编译命令，使用g++编译C++文件
        // 通过shell解释器执行命令
        String[] shellCmd = {"/bin/sh", "-c", "g++ -std=c++11 -o " + executablePath +" " + parentPath + "/*.cpp"};
        try {
            Process compileProcess = Runtime.getRuntime().exec(shellCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            if(executeMessage.getExitValue() != 0){
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        String executablePath = userCodeParentPath + File.separator + "main";

        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for(String inputArgs : inputList){
            // C++直接执行编译好的可执行文件
            String runCmd = String.format("%s \"%s\"", executablePath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "执行");
                executeMessageList.add(executeMessage);
                System.out.println(executeMessage);
            } catch (Exception e) {
                throw new RuntimeException("程序执行错误", e);
            }
        }
        return executeMessageList;
    }
}
