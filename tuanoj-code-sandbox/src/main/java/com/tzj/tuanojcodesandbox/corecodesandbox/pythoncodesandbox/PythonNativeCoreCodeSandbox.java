package com.tzj.tuanojcodesandbox.corecodesandbox.pythoncodesandbox;

import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * python 核心代码模式本地代码沙箱
 */
@Component
public class PythonNativeCoreCodeSandbox extends PythonCoreCodeSandboxTemplate{
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
