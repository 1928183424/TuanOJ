package com.tzj.tuanojcodesandbox.corecodesandbox.javacodesandbox;

import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * java 核心代码模式本地代码沙箱
 */
@Component
public class JavaNativeCoreCodeSandbox extends JavaCoreCodeSandboxTemplate {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
