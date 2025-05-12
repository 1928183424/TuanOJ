package com.tuan.tuanoj.judge.codesandbox;

import com.tuan.tuanoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.tuan.tuanoj.judge.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
