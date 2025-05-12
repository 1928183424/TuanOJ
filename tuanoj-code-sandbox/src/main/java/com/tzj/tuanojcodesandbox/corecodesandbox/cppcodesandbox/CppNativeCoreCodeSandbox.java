package com.tzj.tuanojcodesandbox.corecodesandbox.cppcodesandbox;

import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * cpp 核心代码模式本地代码沙箱
 */
@Component
public class CppNativeCoreCodeSandbox extends CppCoreCodeSandboxTemplate{
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
