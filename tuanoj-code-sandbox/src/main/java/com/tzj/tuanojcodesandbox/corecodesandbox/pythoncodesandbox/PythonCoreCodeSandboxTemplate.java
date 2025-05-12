package com.tzj.tuanojcodesandbox.corecodesandbox.pythoncodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.tzj.tuanojcodesandbox.corecodesandbox.CoreCodeSandboxTemplate;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import com.tzj.tuanojcodesandbox.model.ExecuteMessage;
import com.tzj.tuanojcodesandbox.model.JudgeConfig;
import com.tzj.tuanojcodesandbox.utils.ProcessUtils;
import com.tzj.tuanojcodesandbox.utils.PythonCoreCodeGenerateUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * python 核心代码模式沙箱模板
 * 重写 saveToFile、compileFile、runFile
 */
public abstract class PythonCoreCodeSandboxTemplate extends CoreCodeSandboxTemplate {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_PYTHON_FILE_NAME = "Main.py";

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
        PythonCoreCodeGenerateUtils coreCodeGenerateUtils = new PythonCoreCodeGenerateUtils();

        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 创建用户代码目录
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        FileUtil.mkdir(userCodeParentPath);

        // 生成main.py - 接收命令行参数方式
        String mainCode = coreCodeGenerateUtils.generatePythonMainCode(solutionCode, judgeConfig);
        String mainPath = userCodeParentPath + File.separator + GLOBAL_PYTHON_FILE_NAME;
        File mainFile = FileUtil.writeString(mainCode, mainPath, StandardCharsets.UTF_8);

        return mainFile;
    }

    /**
     * python代码不需要编译
     * @param userCodeFile
     * @return
     */
    @Override
    public ExecuteMessage compileFile(File userCodeFile) {
        return new ExecuteMessage();
    }

    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {

        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for(String inputArgs : inputList){
            // Python执行命令，调整内存限制和编码
            String runCmd = String.format("python -u %s \"%s\"", userCodeFile.getAbsolutePath(), inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        // System.out.println("超时了，中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "执行");
                executeMessageList.add(executeMessage);
                System.out.println(executeMessage);
            } catch (Exception e) {
                // return getErrorResponse(e);
                throw new RuntimeException("程序执行错误", e);
            }
        }
        return executeMessageList;
    }
}
