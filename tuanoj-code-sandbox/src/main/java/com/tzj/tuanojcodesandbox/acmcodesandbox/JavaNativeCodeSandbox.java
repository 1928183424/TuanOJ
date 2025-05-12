package com.tzj.tuanojcodesandbox.acmcodesandbox;

import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * 使用模板方法的 Java 原生代码沙箱
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate{
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
