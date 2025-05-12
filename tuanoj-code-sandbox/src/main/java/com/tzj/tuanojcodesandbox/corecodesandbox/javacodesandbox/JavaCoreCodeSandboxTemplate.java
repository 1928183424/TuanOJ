package com.tzj.tuanojcodesandbox.corecodesandbox.javacodesandbox;

import com.tzj.tuanojcodesandbox.corecodesandbox.CoreCodeSandboxTemplate;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * Java 核心代码模式沙箱模板
 */
public abstract class JavaCoreCodeSandboxTemplate extends CoreCodeSandboxTemplate {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
