package com.tzj.tuanojcodesandbox.utils;

import com.tzj.tuanojcodesandbox.model.JudgeConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CppCoreCodeGenerateUtils {
    /**
     * 生成C++的main文件代码 - 适应标准C++ Solution类格式
     */
    public String generateCppMainCode(String solutionCode, JudgeConfig judgeConfig) {
        StringBuilder codeBuilder = new StringBuilder();

        // 包含必要的头文件
        codeBuilder.append("#include <iostream>\n");
        codeBuilder.append("#include <vector>\n");
        codeBuilder.append("#include <string>\n");
        codeBuilder.append("#include <queue>\n");
        codeBuilder.append("#include <sstream>\n");
        codeBuilder.append("#include <algorithm>\n");
        codeBuilder.append("#include <unordered_map>\n");
        codeBuilder.append("using namespace std;\n");
        codeBuilder.append("\n");

        // 添加需要的类定义(如ListNode、TreeNode等)
        codeBuilder.append(generateCppClassDefinitions(judgeConfig.getParamTypes(), judgeConfig.getReturnType())).append("\n");

        // 添加用户提供的Solution类代码 - C++格式
        codeBuilder.append(solutionCode).append("\n\n");

        // 添加字符串分割函数
        codeBuilder.append("// 字符串分割函数\n");
        codeBuilder.append("std::vector<std::string> splitString(const std::string& str, char delimiter) {\n");
        codeBuilder.append("    std::vector<std::string> tokens;\n");
        codeBuilder.append("    std::stringstream ss(str);\n");
        codeBuilder.append("    std::string token;\n");
        codeBuilder.append("    while (std::getline(ss, token, delimiter)) {\n");
        codeBuilder.append("        tokens.push_back(token);\n");
        codeBuilder.append("    }\n");
        codeBuilder.append("    return tokens;\n");
        codeBuilder.append("}\n\n");

        // 添加必要的辅助函数
        Set<String> requiredMethods = new HashSet<>();
        for (String paramType : judgeConfig.getParamTypes()) {
            addRequiredMethodsWithDependencies(paramType, requiredMethods);
        }
        addRequiredMethodsWithDependencies(judgeConfig.getReturnType(), requiredMethods);

        codeBuilder.append(generateCppHelperMethods(requiredMethods)).append("\n");

        // 定义main函数
        codeBuilder.append("int main(int argc, char* argv[]) {\n");
        codeBuilder.append("    Solution solution;\n\n");

        // 获取方法信息
        String methodName = judgeConfig.getMethodName();
        List<String> paramTypes = judgeConfig.getParamTypes();
        String returnType = judgeConfig.getReturnType();

        // 解析命令行参数
        codeBuilder.append("    // 从命令行接收参数\n");
        codeBuilder.append("    std::string input = \"\";\n");
        codeBuilder.append("    if (argc > 1) {\n");
        codeBuilder.append("        input = argv[1];\n");
        codeBuilder.append("    }\n\n");

        // 处理输入参数
        codeBuilder.append("    try {\n");
        codeBuilder.append("        // 解析输入参数\n");
        codeBuilder.append("        std::vector<std::string> inputParts = splitString(input, ' ');\n\n");

        // 为每个参数生成转换代码
        for (int i = 0; i < paramTypes.size(); i++) {
            String paramType = paramTypes.get(i);
            String cppParamType = convertJavaToCppType(paramType);

            codeBuilder.append("        // 转换参数 ").append(i + 1).append("\n");

            // 判断是否是引用类型参数
            boolean isReference = isReferenceParam(paramType);

            codeBuilder.append("        ").append(cppParamType).append(" param").append(i + 1);
            codeBuilder.append(" = ");

            if (i < paramTypes.size()) {
                codeBuilder.append("inputParts.size() > ").append(i).append(" ? ");
                codeBuilder.append("parse").append(getTypeSimpleName(paramType)).append("(inputParts[").append(i).append("])");
                codeBuilder.append(" : ").append(getCppDefaultValue(paramType));
            } else {
                codeBuilder.append(getCppDefaultValue(paramType));
            }
            codeBuilder.append(";\n\n");
        }

        // 调用方法
        codeBuilder.append("        // 调用Solution方法\n");
        String cppReturnType = convertJavaToCppType(returnType);
        if (!"void".equals(returnType)) {
            codeBuilder.append("        ").append(cppReturnType).append(" result = ");
        } else {
            codeBuilder.append("        ");
        }
        codeBuilder.append("solution.").append(methodName).append("(");

        // 添加参数
        for (int i = 0; i < paramTypes.size(); i++) {
            if (i > 0) {
                codeBuilder.append(", ");
            }
            codeBuilder.append("param").append(i + 1);
        }
        codeBuilder.append(");\n\n");

        // 输出结果
        codeBuilder.append("        // 输出结果\n");
        if (!"void".equals(returnType)) {
            codeBuilder.append("        std::cout << format").append(getTypeSimpleName(returnType)).append("(result) << std::endl;\n");
        } else {
            codeBuilder.append("        std::cout << \"执行完成\" << std::endl;\n");
        }

        // 异常处理
        codeBuilder.append("    } catch (const std::exception& e) {\n");
        codeBuilder.append("        std::cerr << \"执行异常: \" << e.what() << std::endl;\n");
        codeBuilder.append("    }\n");

        // 释放动态分配的内存(如果有的话)
        if (hasPointerParam(paramTypes) || "ListNode".equals(returnType) || "TreeNode".equals(returnType)) {
            codeBuilder.append("\n    // 释放内存\n");
            for (int i = 0; i < paramTypes.size(); i++) {
                if ("ListNode".equals(paramTypes.get(i))) {
                    codeBuilder.append("    freeListNode(param").append(i + 1).append(");\n");
                } else if ("TreeNode".equals(paramTypes.get(i))) {
                    codeBuilder.append("    freeTreeNode(param").append(i + 1).append(");\n");
                }
            }
            if ("ListNode".equals(returnType)) {
                codeBuilder.append("    freeListNode(result);\n");
            } else if ("TreeNode".equals(returnType)) {
                codeBuilder.append("    freeTreeNode(result);\n");
            }
        }

        codeBuilder.append("    return 0;\n");
        codeBuilder.append("}\n");

        return codeBuilder.toString();
    }

    /**
     * 生成C++的类定义
     */
    private String generateCppClassDefinitions(List<String> paramTypes, String returnType) {
        StringBuilder defBuilder = new StringBuilder();
        Set<String> requiredClasses = new HashSet<>();

        // 检查参数和返回类型中是否包含需要单独定义的类
        for (String paramType : paramTypes) {
            addRequiredClass(paramType, requiredClasses);
        }
        addRequiredClass(returnType, requiredClasses);

        // 为所需类生成定义
        for (String className : requiredClasses) {
            if (className.equals("ListNode")) {
                defBuilder.append("// 链表节点定义\n");
                defBuilder.append("struct ListNode {\n");
                defBuilder.append("    int val;\n");
                defBuilder.append("    ListNode* next;\n");
                defBuilder.append("    ListNode() : val(0), next(nullptr) {}\n");
                defBuilder.append("    ListNode(int x) : val(x), next(nullptr) {}\n");
                defBuilder.append("    ListNode(int x, ListNode* next) : val(x), next(next) {}\n");
                defBuilder.append("};\n\n");
            } else if (className.equals("TreeNode")) {
                defBuilder.append("// 二叉树节点定义\n");
                defBuilder.append("struct TreeNode {\n");
                defBuilder.append("    int val;\n");
                defBuilder.append("    TreeNode* left;\n");
                defBuilder.append("    TreeNode* right;\n");
                defBuilder.append("    TreeNode() : val(0), left(nullptr), right(nullptr) {}\n");
                defBuilder.append("    TreeNode(int x) : val(x), left(nullptr), right(nullptr) {}\n");
                defBuilder.append("    TreeNode(int x, TreeNode* left, TreeNode* right) : val(x), left(left), right(right) {}\n");
                defBuilder.append("};\n\n");
            }
            // 可以根据需要添加更多特殊类型的定义
        }

        return defBuilder.toString();
    }

    /**
     * 将类型添加到必需类集合中
     */
    private void addRequiredClass(String type, Set<String> requiredClasses) {
        if (type.equals("ListNode")) {
            requiredClasses.add("ListNode");
        } else if (type.equals("TreeNode")) {
            requiredClasses.add("TreeNode");
        }
        // 可以根据需要添加更多类型判断
    }

    /**
     * 添加方法及其依赖方法到集合中
     */
    private void addRequiredMethodsWithDependencies(String type, Set<String> requiredMethods) {
        String methodType = getTypeSimpleName(type);
        requiredMethods.add(methodType);

        // 添加依赖方法
        if (methodType.equals("ListNode")) {
            requiredMethods.add("IntArray"); // ListNode 依赖 IntArray
        } else if (methodType.equals("TreeNode")) {
            // TreeNode的解析和格式化方法
        } else if (methodType.equals("ListOfInteger")) {
            requiredMethods.add("IntArray"); // vector<int> 依赖 IntArray
        } else if (methodType.equals("IntMatrix")) {
            requiredMethods.add("IntArray"); // vector<vector<int>> 依赖 vector<int>
        } else if (methodType.equals("ListOfListOfInteger")) {
            requiredMethods.add("ListOfInteger"); // 二维vector依赖一维vector
            requiredMethods.add("IntArray"); // 间接依赖
        }
        // 添加更多依赖关系
    }

    /**
     * 判断参数是否为引用类型
     */
    private boolean isReferenceParam(String paramType) {
        return paramType.equals("ListNode") ||
                paramType.equals("TreeNode") ||
                paramType.contains("[]") ||
                paramType.startsWith("List<") ||
                paramType.contains("vector");
    }

    /**
     * 判断是否有指针类型参数
     */
    private boolean hasPointerParam(List<String> paramTypes) {
        for (String type : paramTypes) {
            if ("ListNode".equals(type) || "TreeNode".equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取类型的简化名称（用于方法名）
     */
    private String getTypeSimpleName(String type) {
        if (type == null) {
            return "Object";
        }

        // 去除可能的前缀和空格
        String cleanType = type.trim();

        // 处理基本类型
        if (cleanType.equals("int")) {
            return "Int";
        } else if (cleanType.equals("long") || cleanType.equals("long long")) {
            return "Long";
        } else if (cleanType.equals("double")) {
            return "Double";
        } else if (cleanType.equals("boolean") || cleanType.equals("bool")) {
            return "Boolean";
        } else if (cleanType.equals("String") || cleanType.equals("string") || cleanType.equals("std::string")) {
            return "String";
        }

        // 处理数组和向量类型
        if (cleanType.equals("int[]") ||
                cleanType.equals("vector<int>") ||
                cleanType.equals("std::vector<int>") ||
                cleanType.equals("List<Integer>")) {
            return "IntArray";
        } else if (cleanType.equals("long[]") ||
                cleanType.equals("vector<long>") ||
                cleanType.equals("vector<long long>") ||
                cleanType.equals("std::vector<long long>")) {
            return "LongArray";
        } else if (cleanType.equals("double[]") ||
                cleanType.equals("vector<double>") ||
                cleanType.equals("std::vector<double>")) {
            return "DoubleArray";
        } else if (cleanType.equals("String[]") ||
                cleanType.equals("vector<string>") ||
                cleanType.equals("std::vector<string>") ||
                cleanType.equals("vector<std::string>") ||
                cleanType.equals("std::vector<std::string>") ||
                cleanType.equals("List<String>")) {
            return "StringArray";
        }

        // 处理二维数组和向量
        if (cleanType.equals("int[][]") ||
                cleanType.equals("vector<vector<int>>") ||
                cleanType.equals("std::vector<std::vector<int>>") ||
                cleanType.equals("List<List<Integer>>")) {
            return "IntMatrix";
        }

        // 处理特殊类型
        if (cleanType.equals("ListNode")) {
            return "ListNode";
        } else if (cleanType.equals("TreeNode")) {
            return "TreeNode";
        }

        // 默认情况
        return "Object";
    }

    /**
     * 将Java类型转换为C++类型
     */
    private String convertJavaToCppType(String javaType) {
        switch (javaType) {
            case "int":
                return "int";
            case "long":
                return "long long";
            case "double":
                return "double";
            case "boolean":
                return "bool";
            case "String":
                return "std::string";
            case "int[]":
                return "std::vector<int>";
            case "long[]":
                return "std::vector<long long>";
            case "double[]":
                return "std::vector<double>";
            case "String[]":
                return "std::vector<std::string>";
            case "ListNode":
                return "ListNode*";
            case "TreeNode":
                return "TreeNode*";
            case "int[][]":
                return "std::vector<std::vector<int>>";
            case "List<Integer>":
                return "std::vector<int>";
            case "List<List<Integer>>":
                return "std::vector<std::vector<int>>";
            case "List<String>":
                return "std::vector<std::string>";
            default:
                return "void*"; // 默认情况
        }
    }

    /**
     * 获取C++的默认值
     */
    private String getCppDefaultValue(String javaType) {
        switch (javaType) {
            case "int":
                return "0";
            case "long":
                return "0";
            case "double":
                return "0.0";
            case "boolean":
                return "false";
            case "String":
                return "\"\"";
            case "int[]":
            case "vector<int>":
                return "std::vector<int>()";
            case "long[]":
            case "vector<long long>":
                return "std::vector<long long>()";
            case "double[]":
            case "vector<double>":
                return "std::vector<double>()";
            case "String[]":
            case "vector<string>":
            case "vector<std::string>":
                return "std::vector<std::string>()";
            case "ListNode":
                return "nullptr";
            case "TreeNode":
                return "nullptr";
            case "int[][]":
            case "vector<vector<int>>":
                return "std::vector<std::vector<int>>()";
            case "List<Integer>":
                return "std::vector<int>()";
            case "List<List<Integer>>":
                return "std::vector<std::vector<int>>()";
            case "List<String>":
                return "std::vector<std::string>()";
            default:
                return "nullptr"; // 默认情况
        }
    }

    /**
     * 生成C++的辅助方法
     */
    private String generateCppHelperMethods(Set<String> requiredMethods) {
        StringBuilder methodsBuilder = new StringBuilder();

        // 为每种类型生成解析和格式化方法
        for (String methodType : requiredMethods) {
            if (methodType.equals("Int")) {
                methodsBuilder.append("// 解析整数\n");
                methodsBuilder.append("int parseInt(const std::string& input) {\n");
                methodsBuilder.append("    return std::stoi(input);\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化整数\n");
                methodsBuilder.append("std::string formatInt(int value) {\n");
                methodsBuilder.append("    return std::to_string(value);\n");
                methodsBuilder.append("}\n\n");
            } else if (methodType.equals("Long")) {
                methodsBuilder.append("// 解析长整数\n");
                methodsBuilder.append("long long parseLong(const std::string& input) {\n");
                methodsBuilder.append("    return std::stoll(input);\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化长整数\n");
                methodsBuilder.append("std::string formatLong(long long value) {\n");
                methodsBuilder.append("    return std::to_string(value);\n");
                methodsBuilder.append("}\n\n");
            } else if (methodType.equals("Double")) {
                methodsBuilder.append("// 解析浮点数\n");
                methodsBuilder.append("double parseDouble(const std::string& input) {\n");
                methodsBuilder.append("    return std::stod(input);\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化浮点数\n");
                methodsBuilder.append("std::string formatDouble(double value) {\n");
                methodsBuilder.append("    return std::to_string(value);\n");
                methodsBuilder.append("}\n\n");
            } else if (methodType.equals("Boolean")) {
                methodsBuilder.append("// 解析布尔值\n");
                methodsBuilder.append("bool parseBoolean(const std::string& input) {\n");
                methodsBuilder.append("    std::string lowered = input;\n");
                methodsBuilder.append("    std::transform(lowered.begin(), lowered.end(), lowered.begin(), \n");
                methodsBuilder.append("                 [](unsigned char c){ return std::tolower(c); });\n");
                methodsBuilder.append("    return lowered == \"true\" || lowered == \"1\";\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化布尔值\n");
                methodsBuilder.append("std::string formatBoolean(bool value) {\n");
                methodsBuilder.append("    return value ? \"true\" : \"false\";\n");
                methodsBuilder.append("}\n\n");
            } else if (methodType.equals("String")) {
                methodsBuilder.append("// 解析字符串\n");
                methodsBuilder.append("std::string parseString(const std::string& input) {\n");
                methodsBuilder.append("    std::string result = input;\n");
                methodsBuilder.append("    // 去除前后空白\n");
                methodsBuilder.append("    size_t start = result.find_first_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (start != std::string::npos) {\n");
                methodsBuilder.append("        result = result.substr(start);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    size_t end = result.find_last_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (end != std::string::npos) {\n");
                methodsBuilder.append("        result = result.substr(0, end + 1);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    // 去除引号\n");
                methodsBuilder.append("    if (result.size() >= 2 && result.front() == '\"' && result.back() == '\"') {\n");
                methodsBuilder.append("        result = result.substr(1, result.length() - 2);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化字符串\n");
                methodsBuilder.append("std::string formatString(const std::string& value) {\n");
                methodsBuilder.append("    return value.empty() ? \"\\\"\\\"\" : '\"' + value + '\"';\n");
                methodsBuilder.append("}\n\n");
            } else if (methodType.equals("IntArray")) {
                methodsBuilder.append("// 解析整型数组\n");
                methodsBuilder.append("std::vector<int> parseIntArray(const std::string& input) {\n");
                methodsBuilder.append("    std::vector<int> result;\n");
                methodsBuilder.append("    std::string trimmed = input;\n");
                methodsBuilder.append("    // 去除前后空白\n");
                methodsBuilder.append("    size_t start = trimmed.find_first_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (start != std::string::npos) {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(start);\n");
                methodsBuilder.append("    } else {\n");
                methodsBuilder.append("        return result; // 空串\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    size_t end = trimmed.find_last_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (end != std::string::npos) {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(0, end + 1);\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 去除方括号\n");
                methodsBuilder.append("    if (trimmed.front() == '[' && trimmed.back() == ']') {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(1, trimmed.length() - 2);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    if (trimmed.empty()) return result;\n\n");

                methodsBuilder.append("    // 分割并解析每个数字\n");
                methodsBuilder.append("    std::vector<std::string> parts = splitString(trimmed, ',');\n");
                methodsBuilder.append("    for (const auto& part : parts) {\n");
                methodsBuilder.append("        std::string p = part;\n");
                methodsBuilder.append("        // 去除部分的前后空白\n");
                methodsBuilder.append("        start = p.find_first_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("        if (start != std::string::npos) {\n");
                methodsBuilder.append("            p = p.substr(start);\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        end = p.find_last_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("        if (end != std::string::npos) {\n");
                methodsBuilder.append("            p = p.substr(0, end + 1);\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        // 解析为整数并添加到结果\n");
                methodsBuilder.append("        if (!p.empty()) {\n");
                methodsBuilder.append("            result.push_back(std::stoi(p));\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化整型数组\n");
                methodsBuilder.append("std::string formatIntArray(const std::vector<int>& array) {\n");
                methodsBuilder.append("    if (array.empty()) return \"[]\";\n");
                methodsBuilder.append("    std::string result = \"[\";\n");
                methodsBuilder.append("    for (size_t i = 0; i < array.size(); i++) {\n");
                methodsBuilder.append("        if (i > 0) result += \",\";\n");
                methodsBuilder.append("        result += std::to_string(array[i]);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    result += \"]\";\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");
            } else if (methodType.equals("ListNode")) {
                methodsBuilder.append("// 解析链表\n");
                methodsBuilder.append("ListNode* parseListNode(const std::string& input) {\n");
                methodsBuilder.append("    std::vector<int> values = parseIntArray(input);\n");
                methodsBuilder.append("    if (values.empty()) return nullptr;\n\n");
                methodsBuilder.append("    ListNode* dummy = new ListNode();\n");
                methodsBuilder.append("    ListNode* current = dummy;\n");
                methodsBuilder.append("    for (int val : values) {\n");
                methodsBuilder.append("        current->next = new ListNode(val);\n");
                methodsBuilder.append("        current = current->next;\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    ListNode* result = dummy->next;\n");
                methodsBuilder.append("    delete dummy;\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化链表\n");
                methodsBuilder.append("std::string formatListNode(ListNode* head) {\n");
                methodsBuilder.append("    if (!head) return \"null\";\n");
                methodsBuilder.append("    std::string result = \"[\";\n");
                methodsBuilder.append("    ListNode* current = head;\n");
                methodsBuilder.append("    while (current) {\n");
                methodsBuilder.append("        result += std::to_string(current->val);\n");
                methodsBuilder.append("        if (current->next) {\n");
                methodsBuilder.append("            result += \",\";\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        current = current->next;\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    result += \"]\";\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 释放链表内存\n");
                methodsBuilder.append("void freeListNode(ListNode* head) {\n");
                methodsBuilder.append("    while (head) {\n");
                methodsBuilder.append("        ListNode* temp = head;\n");
                methodsBuilder.append("        head = head->next;\n");
                methodsBuilder.append("        delete temp;\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("}\n\n");
            } else if (methodType.equals("TreeNode")) {
                methodsBuilder.append("// 解析二叉树\n");
                methodsBuilder.append("TreeNode* parseTreeNode(const std::string& input) {\n");
                methodsBuilder.append("    std::string trimmed = input;\n");
                methodsBuilder.append("    // 去除前后空白和括号\n");
                methodsBuilder.append("    size_t start = trimmed.find_first_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (start != std::string::npos) {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(start);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    size_t end = trimmed.find_last_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (end != std::string::npos) {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(0, end + 1);\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    if (trimmed.empty() || trimmed == \"null\" || trimmed == \"[]\") return nullptr;\n");
                methodsBuilder.append("    \n");
                methodsBuilder.append("    // 去除方括号\n");
                methodsBuilder.append("    if (trimmed.front() == '[' && trimmed.back() == ']') {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(1, trimmed.length() - 2);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    if (trimmed.empty()) return nullptr;\n\n");

                methodsBuilder.append("    // 分割节点值\n");
                methodsBuilder.append("    std::vector<std::string> parts = splitString(trimmed, ',');\n");
                methodsBuilder.append("    if (parts.empty()) return nullptr;\n\n");
                methodsBuilder.append("    // 解析第一个节点为根节点\n");
                methodsBuilder.append("    std::string rootVal = parts[0];\n");
                methodsBuilder.append("    rootVal.erase(0, rootVal.find_first_not_of(\" \\t\\n\\r\\f\\v\"));\n");
                methodsBuilder.append("    rootVal.erase(rootVal.find_last_not_of(\" \\t\\n\\r\\f\\v\") + 1);\n");
                methodsBuilder.append("    if (rootVal.empty() || rootVal == \"null\") return nullptr;\n\n");

                methodsBuilder.append("    TreeNode* root = new TreeNode(std::stoi(rootVal));\n");
                methodsBuilder.append("    std::queue<TreeNode*> nodeQueue;\n");
                methodsBuilder.append("    nodeQueue.push(root);\n\n");

                methodsBuilder.append("    size_t index = 1;\n");
                methodsBuilder.append("    while (!nodeQueue.empty() && index < parts.size()) {\n");
                methodsBuilder.append("        TreeNode* current = nodeQueue.front();\n");
                methodsBuilder.append("        nodeQueue.pop();\n\n");

                methodsBuilder.append("        // 左子节点\n");
                methodsBuilder.append("        if (index < parts.size()) {\n");
                methodsBuilder.append("            std::string leftVal = parts[index++];\n");
                methodsBuilder.append("            leftVal.erase(0, leftVal.find_first_not_of(\" \\t\\n\\r\\f\\v\"));\n");
                methodsBuilder.append("            leftVal.erase(leftVal.find_last_not_of(\" \\t\\n\\r\\f\\v\") + 1);\n");
                methodsBuilder.append("            if (!leftVal.empty() && leftVal != \"null\") {\n");
                methodsBuilder.append("                current->left = new TreeNode(std::stoi(leftVal));\n");
                methodsBuilder.append("                nodeQueue.push(current->left);\n");
                methodsBuilder.append("            }\n");
                methodsBuilder.append("        }\n\n");

                methodsBuilder.append("        // 右子节点\n");
                methodsBuilder.append("        if (index < parts.size()) {\n");
                methodsBuilder.append("            std::string rightVal = parts[index++];\n");
                methodsBuilder.append("            rightVal.erase(0, rightVal.find_first_not_of(\" \\t\\n\\r\\f\\v\"));\n");
                methodsBuilder.append("            rightVal.erase(rightVal.find_last_not_of(\" \\t\\n\\r\\f\\v\") + 1);\n");
                methodsBuilder.append("            if (!rightVal.empty() && rightVal != \"null\") {\n");
                methodsBuilder.append("                current->right = new TreeNode(std::stoi(rightVal));\n");
                methodsBuilder.append("                nodeQueue.push(current->right);\n");
                methodsBuilder.append("            }\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    return root;\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化二叉树\n");
                methodsBuilder.append("std::string formatTreeNode(TreeNode* root) {\n");
                methodsBuilder.append("    if (!root) return \"null\";\n");
                methodsBuilder.append("    std::vector<std::string> values;\n");
                methodsBuilder.append("    std::queue<TreeNode*> nodeQueue;\n");
                methodsBuilder.append("    nodeQueue.push(root);\n\n");

                methodsBuilder.append("    while (!nodeQueue.empty()) {\n");
                methodsBuilder.append("        TreeNode* current = nodeQueue.front();\n");
                methodsBuilder.append("        nodeQueue.pop();\n\n");

                methodsBuilder.append("        if (current) {\n");
                methodsBuilder.append("            values.push_back(std::to_string(current->val));\n");
                methodsBuilder.append("            nodeQueue.push(current->left);\n");
                methodsBuilder.append("            nodeQueue.push(current->right);\n");
                methodsBuilder.append("        } else {\n");
                methodsBuilder.append("            values.push_back(\"null\");\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 移除末尾的null\n");
                methodsBuilder.append("    while (!values.empty() && values.back() == \"null\") {\n");
                methodsBuilder.append("        values.pop_back();\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    if (values.empty()) return \"null\";\n");
                methodsBuilder.append("    \n");
                methodsBuilder.append("    std::string result = \"[\";\n");
                methodsBuilder.append("    for (size_t i = 0; i < values.size(); i++) {\n");
                methodsBuilder.append("        if (i > 0) result += \",\";\n");
                methodsBuilder.append("        result += values[i];\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    result += \"]\";\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 释放二叉树内存\n");
                methodsBuilder.append("void freeTreeNode(TreeNode* root) {\n");
                methodsBuilder.append("    if (!root) return;\n");
                methodsBuilder.append("    if (root->left) freeTreeNode(root->left);\n");
                methodsBuilder.append("    if (root->right) freeTreeNode(root->right);\n");
                methodsBuilder.append("    delete root;\n");
                methodsBuilder.append("}\n\n");
            } else if (methodType.equals("IntMatrix") || methodType.equals("ListOfListOfInteger")) {
                methodsBuilder.append("// 解析二维整型数组/列表\n");
                methodsBuilder.append("std::vector<std::vector<int>> parseIntMatrix(const std::string& input) {\n");
                methodsBuilder.append("    std::vector<std::vector<int>> result;\n");
                methodsBuilder.append("    std::string trimmed = input;\n");
                methodsBuilder.append("    // 去除前后空白\n");
                methodsBuilder.append("    size_t start = trimmed.find_first_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (start != std::string::npos) {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(start);\n");
                methodsBuilder.append("    } else {\n");
                methodsBuilder.append("        return result; // 空串\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    size_t end = trimmed.find_last_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (end != std::string::npos) {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(0, end + 1);\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 确保是矩阵格式 [[...],[...],...]]\n");
                methodsBuilder.append("    if (trimmed.size() < 2 || trimmed[0] != '[' || trimmed[trimmed.size()-1] != ']') {\n");
                methodsBuilder.append("        return result;\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 去除最外层方括号\n");
                methodsBuilder.append("    trimmed = trimmed.substr(1, trimmed.length() - 2);\n");
                methodsBuilder.append("    if (trimmed.empty()) return result;\n\n");

                methodsBuilder.append("    // 手动解析二维数组，处理嵌套括号\n");
                methodsBuilder.append("    size_t i = 0;\n");
                methodsBuilder.append("    while (i < trimmed.size()) {\n");
                methodsBuilder.append("        // 寻找子数组的开始\n");
                methodsBuilder.append("        if (trimmed[i] != '[') {\n");
                methodsBuilder.append("            i++;\n");
                methodsBuilder.append("            continue;\n");
                methodsBuilder.append("        }\n\n");

                methodsBuilder.append("        // 找到子数组的结束位置\n");
                methodsBuilder.append("        size_t j = i + 1;\n");
                methodsBuilder.append("        int bracketCount = 1;\n");
                methodsBuilder.append("        while (j < trimmed.size() && bracketCount > 0) {\n");
                methodsBuilder.append("            if (trimmed[j] == '[') bracketCount++;\n");
                methodsBuilder.append("            else if (trimmed[j] == ']') bracketCount--;\n");
                methodsBuilder.append("            j++;\n");
                methodsBuilder.append("        }\n\n");

                methodsBuilder.append("        // 解析子数组\n");
                methodsBuilder.append("        if (j > i) {\n");
                methodsBuilder.append("            std::string subArray = trimmed.substr(i, j - i);\n");
                methodsBuilder.append("            result.push_back(parseIntArray(subArray));\n");
                methodsBuilder.append("            i = j;\n");
                methodsBuilder.append("        } else {\n");
                methodsBuilder.append("            i++;\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化二维整型数组/列表\n");
                methodsBuilder.append("std::string formatIntMatrix(const std::vector<std::vector<int>>& matrix) {\n");
                methodsBuilder.append("    if (matrix.empty()) return \"[]\";\n");
                methodsBuilder.append("    std::string result = \"[\";\n");
                methodsBuilder.append("    for (size_t i = 0; i < matrix.size(); i++) {\n");
                methodsBuilder.append("        if (i > 0) result += \",\";\n");
                methodsBuilder.append("        result += formatIntArray(matrix[i]);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    result += \"]\";\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");

                // 添加一些列表类型的别名方法
                if (methodType.equals("ListOfListOfInteger")) {
                    methodsBuilder.append("// 解析整型二维列表 (vector<vector<int>>)\n");
                    methodsBuilder.append("std::vector<std::vector<int>> parseListOfListOfInteger(const std::string& input) {\n");
                    methodsBuilder.append("    return parseIntMatrix(input);\n");
                    methodsBuilder.append("}\n\n");

                    methodsBuilder.append("// 格式化整型二维列表 (vector<vector<int>>)\n");
                    methodsBuilder.append("std::string formatListOfListOfInteger(const std::vector<std::vector<int>>& list) {\n");
                    methodsBuilder.append("    return formatIntMatrix(list);\n");
                    methodsBuilder.append("}\n\n");
                }
            } else if (methodType.equals("ListOfInteger")) {
                methodsBuilder.append("// 解析整型列表 (vector<int>)\n");
                methodsBuilder.append("std::vector<int> parseListOfInteger(const std::string& input) {\n");
                methodsBuilder.append("    return parseIntArray(input);\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化整型列表 (vector<int>)\n");
                methodsBuilder.append("std::string formatListOfInteger(const std::vector<int>& list) {\n");
                methodsBuilder.append("    return formatIntArray(list);\n");
                methodsBuilder.append("}\n\n");
            } else if (methodType.equals("ListOfString")) {
                methodsBuilder.append("// 解析字符串列表 (vector<string>)\n");
                methodsBuilder.append("std::vector<std::string> parseListOfString(const std::string& input) {\n");
                methodsBuilder.append("    std::vector<std::string> result;\n");
                methodsBuilder.append("    std::string trimmed = input;\n");
                methodsBuilder.append("    // 去除前后空白\n");
                methodsBuilder.append("    size_t start = trimmed.find_first_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (start != std::string::npos) {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(start);\n");
                methodsBuilder.append("    } else {\n");
                methodsBuilder.append("        return result; // 空串\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    size_t end = trimmed.find_last_not_of(\" \\t\\n\\r\\f\\v\");\n");
                methodsBuilder.append("    if (end != std::string::npos) {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(0, end + 1);\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 去除方括号\n");
                methodsBuilder.append("    if (trimmed.size() >= 2 && trimmed.front() == '[' && trimmed.back() == ']') {\n");
                methodsBuilder.append("        trimmed = trimmed.substr(1, trimmed.length() - 2);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    if (trimmed.empty()) return result;\n\n");

                methodsBuilder.append("    // 手动解析带引号的字符串数组\n");
                methodsBuilder.append("    bool inQuotes = false;\n");
                methodsBuilder.append("    std::string current;\n");
                methodsBuilder.append("    for (size_t i = 0; i < trimmed.size(); i++) {\n");
                methodsBuilder.append("        char c = trimmed[i];\n");
                methodsBuilder.append("        if (c == '\"') {\n");
                methodsBuilder.append("            inQuotes = !inQuotes;\n");
                methodsBuilder.append("            current += c;\n");
                methodsBuilder.append("        } else if (c == ',' && !inQuotes) {\n");
                methodsBuilder.append("            // 完成一个字符串\n");
                methodsBuilder.append("            result.push_back(parseString(current));\n");
                methodsBuilder.append("            current.clear();\n");
                methodsBuilder.append("        } else {\n");
                methodsBuilder.append("            current += c;\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    // 添加最后一个字符串\n");
                methodsBuilder.append("    if (!current.empty()) {\n");
                methodsBuilder.append("        result.push_back(parseString(current));\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");

                methodsBuilder.append("// 格式化字符串列表 (vector<string>)\n");
                methodsBuilder.append("std::string formatListOfString(const std::vector<std::string>& list) {\n");
                methodsBuilder.append("    if (list.empty()) return \"[]\";\n");
                methodsBuilder.append("    std::string result = \"[\";\n");
                methodsBuilder.append("    for (size_t i = 0; i < list.size(); i++) {\n");
                methodsBuilder.append("        if (i > 0) result += \",\";\n");
                methodsBuilder.append("        result += formatString(list[i]);\n");
                methodsBuilder.append("    }\n");
                methodsBuilder.append("    result += \"]\";\n");
                methodsBuilder.append("    return result;\n");
                methodsBuilder.append("}\n\n");
            }
            // 添加其他需要的方法...
        }

        return methodsBuilder.toString();
    }
}

