package com.tzj.tuanojcodesandbox.corecodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.tzj.tuanojcodesandbox.CodeSandbox;
import com.tzj.tuanojcodesandbox.enums.JudgeInfoMessageEnum;
import com.tzj.tuanojcodesandbox.model.*;
import com.tzj.tuanojcodesandbox.utils.JavaCoreCodeGenerateUtils;
import com.tzj.tuanojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装通用的核心代码模式沙箱模板（以Java为默认语言）
 */
@Slf4j
public abstract class CoreCodeSandboxTemplate implements CodeSandbox {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final Long TIME_OUT = 5000L;

//    private static final String SECURITY_MANAGER_PATH = "C:\\Users\\19281\\Desktop\\项目源码\\tuanoj\\tuanoj-code-sandbox\\src\\main\\resources\\security";
//
//    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        // 1.将用户的代码保存为文件
        File mainFile = saveToFile(code, executeCodeRequest.getJudgeConfig());

        try {
            // 2.编译代码，得到class文件
            ExecuteMessage compileFileExecuteMessage = compileFile(mainFile);
        }
        catch (RuntimeException e){
            return getErrorResponse(e);
        }

        // 3.执行程序
        List<ExecuteMessage> executeMessageList = runFile(mainFile, inputList);

        // 4.整理输出结果
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);


        // 5.文件清理
        boolean b = deleteFile(mainFile);
        if(!b){
            log.error("文件清理失败， 文件路径：{}", mainFile.getAbsolutePath());
        }

        return outputResponse;
    }

    /**
     * 1、生成 LeetCode 模式（核心代码模式）的文件
     */
    public File saveToFile(String solutionCode, JudgeConfig judgeConfig) {
        // 获取项目根目录
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        JavaCoreCodeGenerateUtils coreCodeGenerateUtils = new JavaCoreCodeGenerateUtils();

        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 创建用户代码目录
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        FileUtil.mkdir(userCodeParentPath);

        // 生成Main.java - 接收命令行参数方式
        String mainCode = coreCodeGenerateUtils.generateMainCode(solutionCode, judgeConfig);
        String mainPath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File mainFile = FileUtil.writeString(mainCode, mainPath, StandardCharsets.UTF_8);

        return mainFile;
    }

    /**
     * 2、编译代码，得到 class 文件
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile){
        // 获取父目录路径，以便编译整个目录下的Java文件
        String parentPath = userCodeFile.getParentFile().getAbsolutePath();

        /// 通过shell解释器执行命令，这样可以处理通配符
        String[] shellCmd = {"/bin/sh", "-c", "javac -encoding utf-8 " + parentPath + "/*.java"};
        try {
            Process compileProcess = Runtime.getRuntime().exec(shellCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            if(executeMessage.getExitValue() != 0){
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            // return getErrorResponse(e);
            throw new  RuntimeException(e);
        }
    }

    /**
     * 3.执行程序
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList){
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for(String inputArgs : inputList){
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main \"%s\"", userCodeParentPath, inputArgs);
            // 使用Shell解释器执行命令
            String[] shellCmd = {"/bin/sh", "-c", runCmd};
            try {
                Process runProcess = Runtime.getRuntime().exec(shellCmd);
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
                throw new  RuntimeException("程序执行错误", e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4、整理输出结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0L;
        long maxMemory = 0L;
        for (ExecuteMessage executeMessage : executeMessageList) {

            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            Long memory = executeMessage.getMemory();
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }
            String errorMessage = executeMessage.getErrorMessage();
            if(StrUtil.isNotBlank(errorMessage)){
                executeCodeResponse.setMessage(errorMessage);
                //执行中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
        }
        //正常运行完成
        if(outputList.size() == executeMessageList.size()){
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);

        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5、删除文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile){
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    /**
     * 6、编译错误处理，提升程序健壮性
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getValue());
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }



}

