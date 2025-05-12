package com.tzj.tuanojcodesandbox;

import com.tzj.tuanojcodesandbox.corecodesandbox.cppcodesandbox.CppNativeCoreCodeSandbox;
import com.tzj.tuanojcodesandbox.corecodesandbox.javacodesandbox.JavaCoreCodeSandboxTemplate;
import com.tzj.tuanojcodesandbox.corecodesandbox.javacodesandbox.JavaNativeCoreCodeSandbox;
import com.tzj.tuanojcodesandbox.corecodesandbox.pythoncodesandbox.PythonNativeCoreCodeSandbox;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeRequest;
import com.tzj.tuanojcodesandbox.model.ExecuteCodeResponse;
import com.tzj.tuanojcodesandbox.model.JudgeConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class TuanojCodeSandboxApplicationTests {

    @Test
    void testSandbox() {
        PythonNativeCoreCodeSandbox pythonNativeCoreCodeSandbox = new PythonNativeCoreCodeSandbox();
        JudgeConfig judgeConfig = new JudgeConfig();
        judgeConfig.setTimeLimit(1000L);
        judgeConfig.setMemoryLimit(262144L);
        judgeConfig.setMethodName("twoSum");
        judgeConfig.setParamTypes(Arrays.asList("int[]", "int"));
        judgeConfig.setReturnType("int[]");

        List<String> inputList = Arrays.asList("[2,7,11,15] 9", "[3,2,4] 6");
        
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code("class Solution:\n" +
                        "    def twoSum(self, nums: List[int], target: int) -> List[int]:\n" +
                        "        n = len(nums)\n" +
                        "        for i in range(n):\n" +
                        "            for j in range(i + 1, n):\n" +
                        "                if nums[i] + nums[j] == target:\n" +
                        "                    return [i, j]\n" +
                        "        \n" +
                        "        return []")
                .language("cpp")
                .inputList(inputList)
                .judgeConfig(judgeConfig).build();

        ExecuteCodeResponse executeCodeResponse = pythonNativeCoreCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

}
