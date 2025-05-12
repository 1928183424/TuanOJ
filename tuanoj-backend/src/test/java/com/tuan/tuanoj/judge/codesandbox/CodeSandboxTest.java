package com.tuan.tuanoj.judge.codesandbox;

import com.tuan.tuanoj.judge.codesandbox.impl.ExampleCodeSandbox;
import com.tuan.tuanoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.tuan.tuanoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.tuan.tuanoj.model.dto.question.JudgeConfig;
import com.tuan.tuanoj.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class CodeSandboxTest {

    @Value("${codesandbox.type:example}")
    private String type;

    @Test
    void executeCode(){
        CodeSandbox codeSandbox = new ExampleCodeSandbox();
        String code = "int main() { }";
        String language = "cpp";
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    @Test
    void executeCodeByValue(){
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String code = "int main() { }";
        String language = "cpp";
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    @Test
    void executeCodeByProxy(){
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String code = "class Solution:\n" +
                "    def twoSum(self, nums: List[int], target: int) -> List[int]:\n" +
                "        n = len(nums)\n" +
                "        for i in range(n):\n" +
                "            for j in range(i + 1, n):\n" +
                "                if nums[i] + nums[j] == target:\n" +
                "                    return [i, j]\n" +
                "        \n" +
                "        return []";
        String language = QuestionSubmitLanguageEnum.PYTHON.getValue();
        List<String> inputList = Arrays.asList("[2,7,11,15] 9", "[3,2,4] 6");
        JudgeConfig judgeConfig = new JudgeConfig();
        judgeConfig.setTimeLimit(1000L);
        judgeConfig.setMemoryLimit(262144L);
        judgeConfig.setMethodName("twoSum");
        judgeConfig.setParamTypes(Arrays.asList("int[]", "int"));
        judgeConfig.setReturnType("int[]");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).judgeConfig(judgeConfig).build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        Assertions.assertNotNull(executeCodeResponse);
    }
}