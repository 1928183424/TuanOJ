package com.tzj.tuanojcodesandbox.controller;

import com.tzj.tuanojcodesandbox.corecodesandbox.cppcodesandbox.CppDockerCoreCodeSandbox;
import com.tzj.tuanojcodesandbox.corecodesandbox.cppcodesandbox.CppNativeCoreCodeSandbox;
import com.tzj.tuanojcodesandbox.corecodesandbox.javacodesandbox.JavaDockerCoreCodeSandbox;
import com.tzj.tuanojcodesandbox.corecodesandbox.javacodesandbox.JavaNativeCoreCodeSandbox;
import com.tzj.tuanojcodesandbox.corecodesandbox.pythoncodesandbox.PythonDockerCoreCodeSandbox;
import com.tzj.tuanojcodesandbox.corecodesandbox.pythoncodesandbox.PythonNativeCoreCodeSandbox;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@RestController("/")
public class MainController {

    /**
     * 如果想用 Docker 沙箱，就改成 JavaDockerCodeSandbox
     */
    @Resource
    private JavaNativeCoreCodeSandbox javaNativeCoreCodeSandbox;

    @Resource
    private JavaDockerCoreCodeSandbox javaDockerCoreCodeSandbox;

    @Resource
    private CppNativeCoreCodeSandbox cppNativeCoreCodeSandbox;

    @Resource
    private CppDockerCoreCodeSandbox cppDockerCoreCodeSandbox;

    @Resource
    private PythonNativeCoreCodeSandbox pythonNativeCoreCodeSandbox;

    @Resource
    private PythonDockerCoreCodeSandbox pythonDockerCoreCodeSandbox;

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";

    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response) {
        String language = executeCodeRequest.getLanguage();

        // 基本的认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }

        if(Objects.equals(language, "java")){
            return javaDockerCoreCodeSandbox.executeCode(executeCodeRequest);
        }else if(Objects.equals(language, "cpp")){
            return cppDockerCoreCodeSandbox.executeCode(executeCodeRequest);
        }
        else if(Objects.equals(language, "python")){
            return pythonDockerCoreCodeSandbox.executeCode(executeCodeRequest);
        }

        return javaDockerCoreCodeSandbox.executeCode(executeCodeRequest);
    }
}
