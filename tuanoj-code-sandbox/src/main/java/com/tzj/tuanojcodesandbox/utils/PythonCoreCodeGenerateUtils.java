package com.tzj.tuanojcodesandbox.utils;

import com.tzj.tuanojcodesandbox.model.JudgeConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PythonCoreCodeGenerateUtils {
    /**
     * 生成Python的main文件代码 - 适应标准Python Solution类格式
     */
    public String generatePythonMainCode(String solutionCode, JudgeConfig judgeConfig) {
        StringBuilder codeBuilder = new StringBuilder();

        // 包含必要的导入语句
        codeBuilder.append("#!/usr/bin/env python3\n");
        codeBuilder.append("# -*- coding: utf-8 -*-\n\n");
        codeBuilder.append("import sys\n");
        codeBuilder.append("import json\n");
        codeBuilder.append("import re\n");
        codeBuilder.append("from typing import List, Optional\n");
        codeBuilder.append("\n");

        // 添加需要的类定义(如ListNode、TreeNode等)
        codeBuilder.append(generatePythonClassDefinitions(judgeConfig.getParamTypes(), judgeConfig.getReturnType())).append("\n");

        // 添加用户提供的Solution类代码 - Python格式
        codeBuilder.append(solutionCode).append("\n\n");

        // 添加必要的辅助函数
        Set<String> requiredMethods = new HashSet<>();
        for (String paramType : judgeConfig.getParamTypes()) {
            addRequiredMethodsWithDependencies(paramType, requiredMethods);
        }
        addRequiredMethodsWithDependencies(judgeConfig.getReturnType(), requiredMethods);

        codeBuilder.append(generatePythonHelperMethods(requiredMethods)).append("\n");

        // 定义main函数
        codeBuilder.append("def main():\n");
        codeBuilder.append("    solution = Solution()\n\n");

        // 获取方法信息
        String methodName = judgeConfig.getMethodName();
        List<String> paramTypes = judgeConfig.getParamTypes();
        String returnType = judgeConfig.getReturnType();

        // 解析命令行参数
        codeBuilder.append("    # 从命令行接收参数\n");
        codeBuilder.append("    input_str = \"\"\n");
        codeBuilder.append("    if len(sys.argv) > 1:\n");
        codeBuilder.append("        input_str = sys.argv[1]\n\n");

        // 处理输入参数
        codeBuilder.append("    try:\n");
        codeBuilder.append("        # 解析输入参数\n");
        codeBuilder.append("        input_parts = input_str.split(' ')\n\n");

        // 为每个参数生成转换代码
        for (int i = 0; i < paramTypes.size(); i++) {
            String paramType = paramTypes.get(i);

            codeBuilder.append("        # 转换参数 ").append(i + 1).append("\n");
            codeBuilder.append("        param").append(i + 1).append(" = ");

            if (i < paramTypes.size()) {
                codeBuilder.append("parse_").append(getTypeSimpleName(paramType).toLowerCase()).append("(input_parts[").append(i).append("]) if len(input_parts) > ").append(i);
                codeBuilder.append(" else ").append(getPythonDefaultValue(paramType));
            } else {
                codeBuilder.append(getPythonDefaultValue(paramType));
            }
            codeBuilder.append("\n\n");
        }

        // 调用方法
        codeBuilder.append("        # 调用Solution方法\n");
        if (!"void".equals(returnType)) {
            codeBuilder.append("        result = ");
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
        codeBuilder.append(")\n\n");

        // 输出结果
        codeBuilder.append("        # 输出结果\n");
        if (!"void".equals(returnType)) {
            codeBuilder.append("        print(format_").append(getTypeSimpleName(returnType).toLowerCase()).append("(result))\n");
        } else {
            codeBuilder.append("        print(\"执行完成\")\n");
        }

        // 异常处理
        codeBuilder.append("    except Exception as e:\n");
        codeBuilder.append("        print(f\"执行异常: {str(e)}\", file=sys.stderr)\n");
        codeBuilder.append("        import traceback\n");
        codeBuilder.append("        traceback.print_exc()\n\n");

        // 添加main调用
        codeBuilder.append("if __name__ == \"__main__\":\n");
        codeBuilder.append("    main()\n");

        return codeBuilder.toString();
    }

    /**
     * 生成Python的类定义
     */
    private String generatePythonClassDefinitions(List<String> paramTypes, String returnType) {
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
                defBuilder.append("# 链表节点定义\n");
                defBuilder.append("class ListNode:\n");
                defBuilder.append("    def __init__(self, val=0, next=None):\n");
                defBuilder.append("        self.val = val\n");
                defBuilder.append("        self.next = next\n");
                defBuilder.append("    \n");
                defBuilder.append("    def __str__(self):\n");
                defBuilder.append("        values = []\n");
                defBuilder.append("        current = self\n");
                defBuilder.append("        while current:\n");
                defBuilder.append("            values.append(str(current.val))\n");
                defBuilder.append("            current = current.next\n");
                defBuilder.append("        return '[' + ','.join(values) + ']'\n\n");
            } else if (className.equals("TreeNode")) {
                defBuilder.append("# 二叉树节点定义\n");
                defBuilder.append("class TreeNode:\n");
                defBuilder.append("    def __init__(self, val=0, left=None, right=None):\n");
                defBuilder.append("        self.val = val\n");
                defBuilder.append("        self.left = left\n");
                defBuilder.append("        self.right = right\n\n");
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
            requiredMethods.add("IntArray"); // List[int] 依赖 IntArray
        } else if (methodType.equals("IntMatrix")) {
            requiredMethods.add("IntArray"); // List[List[int]] 依赖 List[int]
        } else if (methodType.equals("ListOfListOfInteger")) {
            requiredMethods.add("ListOfInteger"); // 二维List依赖一维List
            requiredMethods.add("IntArray"); // 间接依赖
        }
        // 添加更多依赖关系
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
        } else if (cleanType.equals("double") || cleanType.equals("float")) {
            return "Double";
        } else if (cleanType.equals("boolean") || cleanType.equals("bool")) {
            return "Boolean";
        } else if (cleanType.equals("String") || cleanType.equals("string") || cleanType.equals("str")) {
            return "String";
        }

        // 处理数组和列表类型
        if (cleanType.equals("int[]") ||
                cleanType.equals("List<Integer>") ||
                cleanType.equals("List[int]")) {
            return "IntArray";
        } else if (cleanType.equals("long[]") ||
                cleanType.equals("List<Long>") ||
                cleanType.equals("List[long]")) {
            return "LongArray";
        } else if (cleanType.equals("double[]") ||
                cleanType.equals("List<Double>") ||
                cleanType.equals("List[float]")) {
            return "DoubleArray";
        } else if (cleanType.equals("String[]") ||
                cleanType.equals("List<String>") ||
                cleanType.equals("List[str]")) {
            return "StringArray";
        }

        // 处理二维数组和列表
        if (cleanType.equals("int[][]") ||
                cleanType.equals("List<List<Integer>>") ||
                cleanType.equals("List[List[int]]")) {
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
     * 获取Python类型注解
     */
    private String getPythonTypeAnnotation(String type) {
        if (type == null) {
            return "object";
        }

        switch (type) {
            case "int":
                return "int";
            case "long":
                return "int";  // Python int可以处理任意大小的整数
            case "double":
            case "float":
                return "float";
            case "boolean":
            case "bool":
                return "bool";
            case "String":
            case "string":
                return "str";
            case "int[]":
            case "List<Integer>":
                return "List[int]";
            case "long[]":
            case "List<Long>":
                return "List[int]";
            case "double[]":
            case "List<Double>":
                return "List[float]";
            case "String[]":
            case "List<String>":
                return "List[str]";
            case "ListNode":
                return "Optional[ListNode]";
            case "TreeNode":
                return "Optional[TreeNode]";
            case "int[][]":
            case "List<List<Integer>>":
                return "List[List[int]]";
            case "List<List<String>>":
                return "List[List[str]]";
            default:
                return "object";
        }
    }

    /**
     * 获取Python的默认值
     */
    private String getPythonDefaultValue(String type) {
        if (type == null) {
            return "None";
        }

        switch (type) {
            case "int":
                return "0";
            case "long":
                return "0";
            case "double":
            case "float":
                return "0.0";
            case "boolean":
            case "bool":
                return "False";
            case "String":
            case "string":
                return "\"\"";
            case "int[]":
            case "List<Integer>":
            case "List[int]":
                return "[]";
            case "long[]":
            case "List<Long>":
            case "List[long]":
                return "[]";
            case "double[]":
            case "List<Double>":
            case "List[float]":
                return "[]";
            case "String[]":
            case "List<String>":
            case "List[str]":
                return "[]";
            case "ListNode":
                return "None";
            case "TreeNode":
                return "None";
            case "int[][]":
            case "List<List<Integer>>":
            case "List[List[int]]":
                return "[]";
            default:
                return "None";
        }
    }

    /**
     * 生成Python的辅助方法
     */
    private String generatePythonHelperMethods(Set<String> requiredMethods) {
        StringBuilder methodsBuilder = new StringBuilder();

        // 为每种类型生成解析和格式化方法
        for (String methodType : requiredMethods) {
            if (methodType.equals("Int")) {
                methodsBuilder.append("# 解析整数\n");
                methodsBuilder.append("def parse_int(input_str):\n");
                methodsBuilder.append("    if input_str is None or input_str.strip() == \"\":\n");
                methodsBuilder.append("        return 0\n");
                methodsBuilder.append("    return int(input_str.strip())\n\n");

                methodsBuilder.append("# 格式化整数\n");
                methodsBuilder.append("def format_int(value):\n");
                methodsBuilder.append("    return str(value)\n\n");
            } else if (methodType.equals("Long")) {
                methodsBuilder.append("# 解析长整数\n");
                methodsBuilder.append("def parse_long(input_str):\n");
                methodsBuilder.append("    if input_str is None or input_str.strip() == \"\":\n");
                methodsBuilder.append("        return 0\n");
                methodsBuilder.append("    return int(input_str.strip())\n\n");

                methodsBuilder.append("# 格式化长整数\n");
                methodsBuilder.append("def format_long(value):\n");
                methodsBuilder.append("    return str(value)\n\n");
            } else if (methodType.equals("Double")) {
                methodsBuilder.append("# 解析浮点数\n");
                methodsBuilder.append("def parse_double(input_str):\n");
                methodsBuilder.append("    if input_str is None or input_str.strip() == \"\":\n");
                methodsBuilder.append("        return 0.0\n");
                methodsBuilder.append("    return float(input_str.strip())\n\n");

                methodsBuilder.append("# 格式化浮点数\n");
                methodsBuilder.append("def format_double(value):\n");
                methodsBuilder.append("    return str(value)\n\n");
            } else if (methodType.equals("Boolean")) {
                methodsBuilder.append("# 解析布尔值\n");
                methodsBuilder.append("def parse_boolean(input_str):\n");
                methodsBuilder.append("    if input_str is None or input_str.strip() == \"\":\n");
                methodsBuilder.append("        return False\n");
                methodsBuilder.append("    return input_str.strip().lower() in [\"true\", \"1\"]\n\n");

                methodsBuilder.append("# 格式化布尔值\n");
                methodsBuilder.append("def format_boolean(value):\n");
                methodsBuilder.append("    return str(value).lower()\n\n");
            } else if (methodType.equals("String")) {
                methodsBuilder.append("# 解析字符串\n");
                methodsBuilder.append("def parse_string(input_str):\n");
                methodsBuilder.append("    if input_str is None:\n");
                methodsBuilder.append("        return \"\"\n");
                methodsBuilder.append("    input_str = input_str.strip()\n");
                methodsBuilder.append("    if len(input_str) >= 2 and input_str[0] == '\"' and input_str[-1] == '\"':\n");
                methodsBuilder.append("        return input_str[1:-1]\n");
                methodsBuilder.append("    return input_str\n\n");

                methodsBuilder.append("# 格式化字符串\n");
                methodsBuilder.append("def format_string(value):\n");
                methodsBuilder.append("    if value is None:\n");
                methodsBuilder.append("        return '\"\"'\n");
                methodsBuilder.append("    return f'\"{value}\"'\n\n");
            } else if (methodType.equals("IntArray")) {
                methodsBuilder.append("# 解析整型数组\n");
                methodsBuilder.append("def parse_intarray(input_str):\n");
                methodsBuilder.append("    if input_str is None or input_str.strip() == \"\":\n");
                methodsBuilder.append("        return []\n");
                methodsBuilder.append("    input_str = input_str.strip()\n");
                methodsBuilder.append("    # 去除方括号\n");
                methodsBuilder.append("    if input_str.startswith('[') and input_str.endswith(']'):\n");
                methodsBuilder.append("        input_str = input_str[1:-1]\n");
                methodsBuilder.append("    if input_str == \"\":\n");
                methodsBuilder.append("        return []\n");
                methodsBuilder.append("    # 分割并解析每个数字\n");
                methodsBuilder.append("    parts = input_str.split(',')\n");
                methodsBuilder.append("    return [int(part.strip()) for part in parts if part.strip()]\n\n");

                methodsBuilder.append("# 格式化整型数组\n");
                methodsBuilder.append("def format_intarray(array):\n");
                methodsBuilder.append("    if array is None:\n");
                methodsBuilder.append("        return \"null\"\n");
                methodsBuilder.append("    return \"[\" + \",\".join([str(x) for x in array]) + \"]\"\n\n");
            } else if (methodType.equals("ListNode")) {
                methodsBuilder.append("# 解析链表\n");
                methodsBuilder.append("def parse_listnode(input_str):\n");
                methodsBuilder.append("    values = parse_intarray(input_str)\n");
                methodsBuilder.append("    if not values:\n");
                methodsBuilder.append("        return None\n");
                methodsBuilder.append("    dummy = ListNode(0)\n");
                methodsBuilder.append("    current = dummy\n");
                methodsBuilder.append("    for val in values:\n");
                methodsBuilder.append("        current.next = ListNode(val)\n");
                methodsBuilder.append("        current = current.next\n");
                methodsBuilder.append("    return dummy.next\n\n");

                methodsBuilder.append("# 格式化链表\n");
                methodsBuilder.append("def format_listnode(head):\n");
                methodsBuilder.append("    if head is None:\n");
                methodsBuilder.append("        return \"null\"\n");
                methodsBuilder.append("    values = []\n");
                methodsBuilder.append("    current = head\n");
                methodsBuilder.append("    while current:\n");
                methodsBuilder.append("        values.append(str(current.val))\n");
                methodsBuilder.append("        current = current.next\n");
                methodsBuilder.append("    return \"[\" + \",\".join(values) + \"]\"\n\n");
            } else if (methodType.equals("TreeNode")) {
                methodsBuilder.append("# 解析二叉树\n");
                methodsBuilder.append("def parse_treenode(input_str):\n");
                methodsBuilder.append("    if input_str is None or input_str.strip() == \"\" or input_str.strip() == \"null\" or input_str.strip() == \"[]\":\n");
                methodsBuilder.append("        return None\n");
                methodsBuilder.append("    input_str = input_str.strip()\n");
                methodsBuilder.append("    # 去除方括号\n");
                methodsBuilder.append("    if input_str.startswith('[') and input_str.endswith(']'):\n");
                methodsBuilder.append("        input_str = input_str[1:-1]\n");
                methodsBuilder.append("    if input_str == \"\":\n");
                methodsBuilder.append("        return None\n");
                methodsBuilder.append("    # 分割节点值\n");
                methodsBuilder.append("    parts = input_str.split(',')\n");
                methodsBuilder.append("    if not parts:\n");
                methodsBuilder.append("        return None\n");
                methodsBuilder.append("    # 处理节点值\n");
                methodsBuilder.append("    values = []\n");
                methodsBuilder.append("    for part in parts:\n");
                methodsBuilder.append("        part = part.strip()\n");
                methodsBuilder.append("        if part == \"null\" or part == \"\":\n");
                methodsBuilder.append("            values.append(None)\n");
                methodsBuilder.append("        else:\n");
                methodsBuilder.append("            values.append(int(part))\n");
                methodsBuilder.append("    # 构建树\n");
                methodsBuilder.append("    if not values or values[0] is None:\n");
                methodsBuilder.append("        return None\n");
                methodsBuilder.append("    root = TreeNode(values[0])\n");
                methodsBuilder.append("    queue = [root]\n");
                methodsBuilder.append("    i = 1\n");
                methodsBuilder.append("    while queue and i < len(values):\n");
                methodsBuilder.append("        node = queue.pop(0)\n");
                methodsBuilder.append("        # 左子节点\n");
                methodsBuilder.append("        if i < len(values):\n");
                methodsBuilder.append("            if values[i] is not None:\n");
                methodsBuilder.append("                node.left = TreeNode(values[i])\n");
                methodsBuilder.append("                queue.append(node.left)\n");
                methodsBuilder.append("            i += 1\n");
                methodsBuilder.append("        # 右子节点\n");
                methodsBuilder.append("        if i < len(values):\n");
                methodsBuilder.append("            if values[i] is not None:\n");
                methodsBuilder.append("                node.right = TreeNode(values[i])\n");
                methodsBuilder.append("                queue.append(node.right)\n");
                methodsBuilder.append("            i += 1\n");
                methodsBuilder.append("    return root\n\n");

                methodsBuilder.append("# 格式化二叉树\n");
                methodsBuilder.append("def format_treenode(root):\n");
                methodsBuilder.append("    if root is None:\n");
                methodsBuilder.append("        return \"null\"\n");
                methodsBuilder.append("    values = []\n");
                methodsBuilder.append("    queue = [root]\n");
                methodsBuilder.append("    while queue:\n");
                methodsBuilder.append("        node = queue.pop(0)\n");
                methodsBuilder.append("        if node:\n");
                methodsBuilder.append("            values.append(str(node.val))\n");
                methodsBuilder.append("            queue.append(node.left)\n");
                methodsBuilder.append("            queue.append(node.right)\n");
                methodsBuilder.append("        else:\n");
                methodsBuilder.append("            values.append(\"null\")\n");
                methodsBuilder.append("    # 移除末尾的null\n");
                methodsBuilder.append("    while values and values[-1] == \"null\":\n");
                methodsBuilder.append("        values.pop()\n");
                methodsBuilder.append("    return \"[\" + \",\".join(values) + \"]\"\n\n");
            } else if (methodType.equals("IntMatrix") || methodType.equals("ListOfListOfInteger")) {
                methodsBuilder.append("# 解析二维整型数组/列表\n");
                methodsBuilder.append("def parse_intmatrix(input_str):\n");
                methodsBuilder.append("    if input_str is None or input_str.strip() == \"\":\n");
                methodsBuilder.append("        return []\n");
                methodsBuilder.append("    input_str = input_str.strip()\n");
                methodsBuilder.append("    # 确保是矩阵格式 [[...],[...],...]]\n");
                methodsBuilder.append("    if not (input_str.startswith('[') and input_str.endswith(']')):\n");
                methodsBuilder.append("        return []\n");
                methodsBuilder.append("    # 去除最外层方括号\n");
                methodsBuilder.append("    input_str = input_str[1:-1].strip()\n");
                methodsBuilder.append("    if input_str == \"\":\n");
                methodsBuilder.append("        return []\n");
                methodsBuilder.append("    # 使用正则表达式匹配嵌套数组\n");
                methodsBuilder.append("    result = []\n");
                methodsBuilder.append("    # 初始位置\n");
                methodsBuilder.append("    pos = 0\n");
                methodsBuilder.append("    input_len = len(input_str)\n");
                methodsBuilder.append("    while pos < input_len:\n");
                methodsBuilder.append("        # 跳过空白字符\n");
                methodsBuilder.append("        while pos < input_len and input_str[pos].isspace():\n");
                methodsBuilder.append("            pos += 1\n");
                methodsBuilder.append("        # 检查是否到达字符串末尾\n");
                methodsBuilder.append("        if pos >= input_len:\n");
                methodsBuilder.append("            break\n");
                methodsBuilder.append("        # 检查是否是子数组开始\n");
                methodsBuilder.append("        if input_str[pos] == '[':\n");
                methodsBuilder.append("            # 寻找子数组结束\n");
                methodsBuilder.append("            bracket_count = 1\n");
                methodsBuilder.append("            start = pos\n");
                methodsBuilder.append("            pos += 1\n");
                methodsBuilder.append("            while pos < input_len and bracket_count > 0:\n");
                methodsBuilder.append("                if input_str[pos] == '[':\n");
                methodsBuilder.append("                    bracket_count += 1\n");
                methodsBuilder.append("                elif input_str[pos] == ']':\n");
                methodsBuilder.append("                    bracket_count -= 1\n");
                methodsBuilder.append("                pos += 1\n");
                methodsBuilder.append("            # 解析子数组\n");
                methodsBuilder.append("            sub_array = input_str[start:pos]\n");
                methodsBuilder.append("            result.append(parse_intarray(sub_array))\n");
                methodsBuilder.append("        # 跳过逗号等分隔符\n");
                methodsBuilder.append("        if pos < input_len and input_str[pos] == ',':\n");
                methodsBuilder.append("            pos += 1\n");
                methodsBuilder.append("        else:\n");
                methodsBuilder.append("            pos += 1\n");
                methodsBuilder.append("    return result\n\n");

                methodsBuilder.append("# 格式化二维整型数组/列表\n");
                methodsBuilder.append("def format_intmatrix(matrix):\n");
                methodsBuilder.append("    if matrix is None or len(matrix) == 0:\n");
                methodsBuilder.append("        return \"[]\"\n");
                methodsBuilder.append("    rows = []\n");
                methodsBuilder.append("    for row in matrix:\n");
                methodsBuilder.append("        rows.append(format_intarray(row))\n");
                methodsBuilder.append("    return \"[\" + \",\".join(rows) + \"]\"\n\n");

                // 添加一些列表类型的别名方法
                if (methodType.equals("ListOfListOfInteger")) {
                    methodsBuilder.append("# 解析整型二维列表 (List[List[int]])\n");
                    methodsBuilder.append("def parse_listoflistofinteger(input_str):\n");
                    methodsBuilder.append("    return parse_intmatrix(input_str)\n\n");

                    methodsBuilder.append("# 格式化整型二维列表 (List[List[int]])\n");
                    methodsBuilder.append("def format_listoflistofinteger(list_of_lists):\n");
                    methodsBuilder.append("    return format_intmatrix(list_of_lists)\n\n");
                }
            } else if (methodType.equals("ListOfInteger")) {
                methodsBuilder.append("# 解析整型列表 (List[int])\n");
                methodsBuilder.append("def parse_listofinteger(input_str):\n");
                methodsBuilder.append("    return parse_intarray(input_str)\n\n");

                methodsBuilder.append("# 格式化整型列表 (List[int])\n");
                methodsBuilder.append("def format_listofinteger(list_items):\n");
                methodsBuilder.append("    return format_intarray(list_items)\n\n");
            } else if (methodType.equals("StringArray") || methodType.equals("ListOfString")) {
                methodsBuilder.append("# 解析字符串列表 (List[str])\n");
                methodsBuilder.append("def parse_stringarray(input_str):\n");
                methodsBuilder.append("    if input_str is None or input_str.strip() == \"\":\n");
                methodsBuilder.append("        return []\n");
                methodsBuilder.append("    input_str = input_str.strip()\n");
                methodsBuilder.append("    # 去除方括号\n");
                methodsBuilder.append("    if input_str.startswith('[') and input_str.endswith(']'):\n");
                methodsBuilder.append("        input_str = input_str[1:-1]\n");
                methodsBuilder.append("    if input_str == \"\":\n");
                methodsBuilder.append("        return []\n");
                methodsBuilder.append("    # 手动解析带引号的字符串列表\n");
                methodsBuilder.append("    result = []\n");
                methodsBuilder.append("    in_quotes = False\n");
                methodsBuilder.append("    current = \"\"\n");
                methodsBuilder.append("    for char in input_str:\n");
                methodsBuilder.append("        if char == '\"':\n");
                methodsBuilder.append("            in_quotes = not in_quotes\n");
                methodsBuilder.append("            current += char\n");
                methodsBuilder.append("        elif char == ',' and not in_quotes:\n");
                methodsBuilder.append("            result.append(parse_string(current))\n");
                methodsBuilder.append("            current = \"\"\n");
                methodsBuilder.append("        else:\n");
                methodsBuilder.append("            current += char\n");
                methodsBuilder.append("    if current:\n");
                methodsBuilder.append("        result.append(parse_string(current))\n");
                methodsBuilder.append("    return result\n\n");

                methodsBuilder.append("# 格式化字符串列表 (List[str])\n");
                methodsBuilder.append("def format_stringarray(array):\n");
                methodsBuilder.append("    if array is None:\n");
                methodsBuilder.append("        return \"null\"\n");
                methodsBuilder.append("    return \"[\" + \",\".join([format_string(x) for x in array]) + \"]\"\n\n");

                if (methodType.equals("ListOfString")) {
                    methodsBuilder.append("# 解析字符串列表别名 (List[str])\n");
                    methodsBuilder.append("def parse_listofstring(input_str):\n");
                    methodsBuilder.append("    return parse_stringarray(input_str)\n\n");

                    methodsBuilder.append("# 格式化字符串列表别名 (List[str])\n");
                    methodsBuilder.append("def format_listofstring(list_items):\n");
                    methodsBuilder.append("    return format_stringarray(list_items)\n\n");
                }
            }
            // 添加其他需要的方法...
        }

        return methodsBuilder.toString();
    }
}