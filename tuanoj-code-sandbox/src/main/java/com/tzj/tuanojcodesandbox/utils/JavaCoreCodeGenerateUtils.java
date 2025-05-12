package com.tzj.tuanojcodesandbox.utils;

import com.tzj.tuanojcodesandbox.model.JudgeConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaCoreCodeGenerateUtils {
    /**
     * 生成Main类代码 - 只包含必要的转换函数
     */
    public String generateMainCode(String solutionCode, JudgeConfig judgeConfig) {
        StringBuilder codeBuilder = new StringBuilder();

        // 导入包
        codeBuilder.append("import java.util.*;\n\n");

        // 首先添加需要的类定义(如ListNode、TreeNode等)
        codeBuilder.append(generateClassDefinitions(judgeConfig.getParamTypes(), judgeConfig.getReturnType())).append("\n");

        // 包含Solution类
        codeBuilder.append(solutionCode).append("\n\n");

        // 定义Main类
        codeBuilder.append("public class Main {\n");
        codeBuilder.append("    public static void main(String[] args) {\n");
        codeBuilder.append("        Solution solution = new Solution();\n\n");

        // 获取方法信息
        String methodName = judgeConfig.getMethodName();
        List<String> paramTypes = judgeConfig.getParamTypes();
        String returnType = judgeConfig.getReturnType();

        // 解析命令行参数
        codeBuilder.append("        // 从命令行接收参数\n");
        codeBuilder.append("        String input = \"\";\n");
        codeBuilder.append("        if (args.length > 0) {\n");
        codeBuilder.append("            input = args[0];\n");
        codeBuilder.append("        }\n\n");

        // 处理输入参数
        codeBuilder.append("        try {\n");
        codeBuilder.append("            // 解析输入参数\n");
        codeBuilder.append("            String[] inputParts = input.split(\" \");\n\n");

        // 为每个参数生成转换代码
        for (int i = 0; i < paramTypes.size(); i++) {
            String paramType = paramTypes.get(i);
            codeBuilder.append("            // 转换参数 ").append(i + 1).append("\n");
            codeBuilder.append("            ").append(paramType).append(" param").append(i + 1);
            codeBuilder.append(" = ");
            if (i < paramTypes.size()) {
                codeBuilder.append("inputParts.length > ").append(i).append(" ? ");
                codeBuilder.append("parse").append(getTypeSimpleName(paramType)).append("(inputParts[").append(i).append("])");
                codeBuilder.append(" : ").append(getDefaultValue(paramType));
            } else {
                codeBuilder.append(getDefaultValue(paramType));
            }
            codeBuilder.append(";\n\n");
        }

        // 调用方法
        codeBuilder.append("            // 调用Solution方法\n");
        codeBuilder.append("            ");
        if (!"void".equals(returnType)) {
            codeBuilder.append(returnType).append(" result = ");
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
        codeBuilder.append("            // 输出结果\n");
        if (!"void".equals(returnType)) {
            codeBuilder.append("            System.out.println(format").append(getTypeSimpleName(returnType)).append("(result));\n");
        } else {
            codeBuilder.append("            System.out.println(\"执行完成\");\n");
        }

        // 异常处理
        codeBuilder.append("        } catch (Exception e) {\n");
        codeBuilder.append("            System.err.println(\"执行异常: \" + e.getMessage());\n");
        codeBuilder.append("            e.printStackTrace();\n");
        codeBuilder.append("        }\n");

        codeBuilder.append("    }\n\n");

        // 仅添加需要的转换方法（使用扩展的方法集合）
        Set<String> requiredMethods = new HashSet<>();
        // 确定需要哪些转换方法
        for (String paramType : paramTypes) {
            addRequiredMethodsWithDependencies(paramType, requiredMethods);
        }
        // 添加结果格式化方法
        addRequiredMethodsWithDependencies(returnType, requiredMethods);

        codeBuilder.append(generateRequiredHelperMethods(requiredMethods));

        codeBuilder.append("}\n");

        return codeBuilder.toString();
    }

    /**
     * 生成所需的类定义，放在Main类之外
     */
    private String generateClassDefinitions(List<String> paramTypes, String returnType) {
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
                defBuilder.append("class ListNode {\n");
                defBuilder.append("    int val;\n");
                defBuilder.append("    ListNode next;\n");
                defBuilder.append("    ListNode() {}\n");
                defBuilder.append("    ListNode(int val) { this.val = val; }\n");
                defBuilder.append("    ListNode(int val, ListNode next) { this.val = val; this.next = next; }\n");
                defBuilder.append("}\n\n");
            } else if (className.equals("TreeNode")) {
                defBuilder.append("// 二叉树节点定义\n");
                defBuilder.append("class TreeNode {\n");
                defBuilder.append("    int val;\n");
                defBuilder.append("    TreeNode left;\n");
                defBuilder.append("    TreeNode right;\n");
                defBuilder.append("    TreeNode() {}\n");
                defBuilder.append("    TreeNode(int val) { this.val = val; }\n");
                defBuilder.append("    TreeNode(int val, TreeNode left, TreeNode right) {\n");
                defBuilder.append("        this.val = val;\n");
                defBuilder.append("        this.left = left;\n");
                defBuilder.append("        this.right = right;\n");
                defBuilder.append("    }\n");
                defBuilder.append("}\n\n");
            }
            // 可以根据需要添加更多类定义
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
            // TreeNode的parseTreeNode方法不依赖其他方法
        } else if (methodType.equals("ListOfInteger")) {
            requiredMethods.add("IntArray"); // List<Integer> 依赖 IntArray
        } else if (methodType.equals("IntMatrix")) {
            requiredMethods.add("IntArray"); // 二维数组依赖一维数组
        } else if (methodType.equals("ListOfListOfInteger")) {
            requiredMethods.add("ListOfInteger"); // 二维List依赖一维List
            requiredMethods.add("IntArray"); // 间接依赖
        }
        // 添加更多依赖关系
    }

    /**
     * 只生成指定方法集合中的辅助方法
     */
    public String generateRequiredHelperMethods(Set<String> requiredMethods) {
        StringBuilder methodsBuilder = new StringBuilder();

        // 生成所有需要的方法
        for (String methodType : requiredMethods) {
            if (methodType.equals("Int")) {
                methodsBuilder.append("    // 解析整数\n");
                methodsBuilder.append("    private static int parseInt(String input) {\n");
                methodsBuilder.append("        return Integer.parseInt(input.trim());\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化整数\n");
                methodsBuilder.append("    private static String formatInt(int value) {\n");
                methodsBuilder.append("        return Integer.toString(value);\n");
                methodsBuilder.append("    }\n\n");
            } else if (methodType.equals("Long")) {
                methodsBuilder.append("    // 解析长整数\n");
                methodsBuilder.append("    private static long parseLong(String input) {\n");
                methodsBuilder.append("        return Long.parseLong(input.trim());\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化长整数\n");
                methodsBuilder.append("    private static String formatLong(long value) {\n");
                methodsBuilder.append("        return Long.toString(value);\n");
                methodsBuilder.append("    }\n\n");
            } else if (methodType.equals("Double")) {
                methodsBuilder.append("    // 解析浮点数\n");
                methodsBuilder.append("    private static double parseDouble(String input) {\n");
                methodsBuilder.append("        return Double.parseDouble(input.trim());\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化浮点数\n");
                methodsBuilder.append("    private static String formatDouble(double value) {\n");
                methodsBuilder.append("        return Double.toString(value);\n");
                methodsBuilder.append("    }\n\n");
            } else if (methodType.equals("Boolean")) {
                methodsBuilder.append("    // 解析布尔值\n");
                methodsBuilder.append("    private static boolean parseBoolean(String input) {\n");
                methodsBuilder.append("        return Boolean.parseBoolean(input.trim());\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化布尔值\n");
                methodsBuilder.append("    private static String formatBoolean(boolean value) {\n");
                methodsBuilder.append("        return Boolean.toString(value);\n");
                methodsBuilder.append("    }\n\n");
            } else if (methodType.equals("String")) {
                methodsBuilder.append("    // 解析字符串\n");
                methodsBuilder.append("    private static String parseString(String input) {\n");
                methodsBuilder.append("        if (input == null) return null;\n");
                methodsBuilder.append("        input = input.trim();\n");
                methodsBuilder.append("        if (input.startsWith(\"\\\"\") && input.endsWith(\"\\\"\")) {\n");
                methodsBuilder.append("            input = input.substring(1, input.length() - 1);\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        return input;\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化字符串\n");
                methodsBuilder.append("    private static String formatString(String value) {\n");
                methodsBuilder.append("        return value == null ? \"null\" : '\"' + value + '\"';\n");
                methodsBuilder.append("    }\n\n");
            } else if (methodType.equals("IntArray")) {
                methodsBuilder.append("    // 解析整型数组\n");
                methodsBuilder.append("    private static int[] parseIntArray(String input) {\n");
                methodsBuilder.append("        if (input == null || input.trim().isEmpty()) return new int[0];\n");
                methodsBuilder.append("        input = input.trim();\n");
                methodsBuilder.append("        if (input.startsWith(\"[\") && input.endsWith(\"]\")) {\n");
                methodsBuilder.append("            input = input.substring(1, input.length() - 1);\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        if (input.isEmpty()) return new int[0];\n");
                methodsBuilder.append("        String[] parts = input.split(\",\");\n");
                methodsBuilder.append("        int[] result = new int[parts.length];\n");
                methodsBuilder.append("        for (int i = 0; i < parts.length; i++) {\n");
                methodsBuilder.append("            result[i] = Integer.parseInt(parts[i].trim());\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        return result;\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化整型数组\n");
                methodsBuilder.append("    private static String formatIntArray(int[] array) {\n");
                methodsBuilder.append("        if (array == null) return \"null\";\n");
                methodsBuilder.append("        if (array.length == 0) return \"[]\";\n");
                methodsBuilder.append("        StringBuilder sb = new StringBuilder(\"[\");\n");
                methodsBuilder.append("        for (int i = 0; i < array.length; i++) {\n");
                methodsBuilder.append("            if (i > 0) sb.append(\", \");\n");
                methodsBuilder.append("            sb.append(array[i]);\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        sb.append(\"]\");\n");
                methodsBuilder.append("        return sb.toString();\n");
                methodsBuilder.append("    }\n\n");
            } else if (methodType.equals("LongArray")) {
                methodsBuilder.append("    // 解析长整型数组\n");
                methodsBuilder.append("    private static long[] parseLongArray(String input) {\n");
                methodsBuilder.append("        if (input == null || input.trim().isEmpty()) return new long[0];\n");
                methodsBuilder.append("        input = input.trim();\n");
                methodsBuilder.append("        if (input.startsWith(\"[\") && input.endsWith(\"]\")) {\n");
                methodsBuilder.append("            input = input.substring(1, input.length() - 1);\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        if (input.isEmpty()) return new long[0];\n");
                methodsBuilder.append("        String[] parts = input.split(\",\");\n");
                methodsBuilder.append("        long[] result = new long[parts.length];\n");
                methodsBuilder.append("        for (int i = 0; i < parts.length; i++) {\n");
                methodsBuilder.append("            result[i] = Long.parseLong(parts[i].trim());\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        return result;\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化长整型数组\n");
                methodsBuilder.append("    private static String formatLongArray(long[] array) {\n");
                methodsBuilder.append("        if (array == null) return \"null\";\n");
                methodsBuilder.append("        if (array.length == 0) return \"[]\";\n");
                methodsBuilder.append("        StringBuilder sb = new StringBuilder(\"[\");\n");
                methodsBuilder.append("        for (int i = 0; i < array.length; i++) {\n");
                methodsBuilder.append("            if (i > 0) sb.append(\", \");\n");
                methodsBuilder.append("            sb.append(array[i]);\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        sb.append(\"]\");\n");
                methodsBuilder.append("        return sb.toString();\n");
                methodsBuilder.append("    }\n\n");
            } else if (methodType.equals("StringArray")) {
                methodsBuilder.append("    // 解析字符串数组\n");
                methodsBuilder.append("    private static String[] parseStringArray(String input) {\n");
                methodsBuilder.append("        if (input == null || input.trim().isEmpty()) return new String[0];\n");
                methodsBuilder.append("        input = input.trim();\n");
                methodsBuilder.append("        if (input.startsWith(\"[\") && input.endsWith(\"]\")) {\n");
                methodsBuilder.append("            input = input.substring(1, input.length() - 1);\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        if (input.isEmpty()) return new String[0];\n");
                methodsBuilder.append("        List<String> resultList = new ArrayList<>();\n");
                methodsBuilder.append("        StringBuilder current = new StringBuilder();\n");
                methodsBuilder.append("        boolean inQuotes = false;\n\n");
                methodsBuilder.append("        for (char c : input.toCharArray()) {\n");
                methodsBuilder.append("            if (c == '\"') {\n");
                methodsBuilder.append("                inQuotes = !inQuotes;\n");
                methodsBuilder.append("                current.append(c);\n");
                methodsBuilder.append("            } else if (c == ',' && !inQuotes) {\n");
                methodsBuilder.append("                resultList.add(parseString(current.toString()));\n");
                methodsBuilder.append("                current = new StringBuilder();\n");
                methodsBuilder.append("            } else {\n");
                methodsBuilder.append("                current.append(c);\n");
                methodsBuilder.append("            }\n");
                methodsBuilder.append("        }\n\n");
                methodsBuilder.append("        if (current.length() > 0) {\n");
                methodsBuilder.append("            resultList.add(parseString(current.toString()));\n");
                methodsBuilder.append("        }\n\n");
                methodsBuilder.append("        return resultList.toArray(new String[0]);\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化字符串数组\n");
                methodsBuilder.append("    private static String formatStringArray(String[] array) {\n");
                methodsBuilder.append("        if (array == null) return \"null\";\n");
                methodsBuilder.append("        if (array.length == 0) return \"[]\";\n");
                methodsBuilder.append("        StringBuilder sb = new StringBuilder(\"[\");\n");
                methodsBuilder.append("        for (int i = 0; i < array.length; i++) {\n");
                methodsBuilder.append("            if (i > 0) sb.append(\", \");\n");
                methodsBuilder.append("            sb.append(formatString(array[i]));\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        sb.append(\"]\");\n");
                methodsBuilder.append("        return sb.toString();\n");
                methodsBuilder.append("    }\n\n");
            } else if (methodType.equals("ListNode")) {
                methodsBuilder.append("    // 解析链表\n");
                methodsBuilder.append("    private static ListNode parseListNode(String input) {\n");
                methodsBuilder.append("        if (input == null || input.trim().isEmpty() || input.equals(\"null\")) return null;\n");
                methodsBuilder.append("        int[] values = parseIntArray(input);\n");
                methodsBuilder.append("        if (values.length == 0) return null;\n");
                methodsBuilder.append("        ListNode dummy = new ListNode(0);\n");
                methodsBuilder.append("        ListNode current = dummy;\n");
                methodsBuilder.append("        for (int val : values) {\n");
                methodsBuilder.append("            current.next = new ListNode(val);\n");
                methodsBuilder.append("            current = current.next;\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        return dummy.next;\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化链表\n");
                methodsBuilder.append("    private static String formatListNode(ListNode head) {\n");
                methodsBuilder.append("        if (head == null) return \"null\";\n");
                methodsBuilder.append("        StringBuilder sb = new StringBuilder(\"[\");\n");
                methodsBuilder.append("        ListNode current = head;\n");
                methodsBuilder.append("        while (current != null) {\n");
                methodsBuilder.append("            sb.append(current.val);\n");
                methodsBuilder.append("            if (current.next != null) {\n");
                methodsBuilder.append("                sb.append(\",\");\n");
                methodsBuilder.append("            }\n");
                methodsBuilder.append("            current = current.next;\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        sb.append(\"]\");\n");
                methodsBuilder.append("        return sb.toString();\n");
                methodsBuilder.append("    }\n\n");
            } else if (methodType.equals("TreeNode")) {
                methodsBuilder.append("    // 解析二叉树\n");
                methodsBuilder.append("    private static TreeNode parseTreeNode(String input) {\n");
                methodsBuilder.append("        if (input == null || input.trim().isEmpty() || input.equals(\"null\")) return null;\n");
                methodsBuilder.append("        input = input.trim();\n");
                methodsBuilder.append("        if (input.startsWith(\"[\") && input.endsWith(\"]\")) {\n");
                methodsBuilder.append("            input = input.substring(1, input.length() - 1);\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        if (input.isEmpty()) return null;\n\n");

                methodsBuilder.append("        String[] parts = input.split(\",\");\n");
                methodsBuilder.append("        String firstVal = parts[0].trim();\n");
                methodsBuilder.append("        if (firstVal.equals(\"null\")) return null;\n\n");

                methodsBuilder.append("        TreeNode root = new TreeNode(Integer.parseInt(firstVal));\n");
                methodsBuilder.append("        Queue<TreeNode> queue = new LinkedList<>();\n");
                methodsBuilder.append("        queue.add(root);\n\n");

                methodsBuilder.append("        int index = 1;\n");
                methodsBuilder.append("        while (!queue.isEmpty() && index < parts.length) {\n");
                methodsBuilder.append("            TreeNode node = queue.poll();\n\n");

                methodsBuilder.append("            // 左子节点\n");
                methodsBuilder.append("            if (index < parts.length) {\n");
                methodsBuilder.append("                String leftVal = parts[index++].trim();\n");
                methodsBuilder.append("                if (!leftVal.equals(\"null\")) {\n");
                methodsBuilder.append("                    node.left = new TreeNode(Integer.parseInt(leftVal));\n");
                methodsBuilder.append("                    queue.add(node.left);\n");
                methodsBuilder.append("                }\n");
                methodsBuilder.append("            }\n\n");

                methodsBuilder.append("            // 右子节点\n");
                methodsBuilder.append("            if (index < parts.length) {\n");
                methodsBuilder.append("                String rightVal = parts[index++].trim();\n");
                methodsBuilder.append("                if (!rightVal.equals(\"null\")) {\n");
                methodsBuilder.append("                    node.right = new TreeNode(Integer.parseInt(rightVal));\n");
                methodsBuilder.append("                    queue.add(node.right);\n");
                methodsBuilder.append("                }\n");
                methodsBuilder.append("            }\n");
                methodsBuilder.append("        }\n");
                methodsBuilder.append("        return root;\n");
                methodsBuilder.append("    }\n\n");

                methodsBuilder.append("    // 格式化二叉树\n");
                methodsBuilder.append("    private static String formatTreeNode(TreeNode root) {\n");
                methodsBuilder.append("        if (root == null) return \"null\";\n");
                methodsBuilder.append("        List<String> result = new ArrayList<>();\n");
                methodsBuilder.append("        Queue<TreeNode> queue = new LinkedList<>();\n");
                methodsBuilder.append("        queue.add(root);\n\n");

                methodsBuilder.append("        while (!queue.isEmpty()) {\n");
                methodsBuilder.append("            TreeNode node = queue.poll();\n");
                methodsBuilder.append("            if (node == null) {\n");
                methodsBuilder.append("                result.add(\"null\");\n");
                methodsBuilder.append("            } else {\n");
                methodsBuilder.append("                result.add(String.valueOf(node.val));\n");
                methodsBuilder.append("                queue.add(node.left);\n");
                methodsBuilder.append("                queue.add(node.right);\n");
                methodsBuilder.append("            }\n");
                methodsBuilder.append("        }\n\n");

                methodsBuilder.append("        // 移除末尾的null值\n");
                methodsBuilder.append("        while (result.size() > 0 && result.get(result.size() - 1).equals(\"null\")) {\n");
                methodsBuilder.append("            result.remove(result.size() - 1);\n");
                methodsBuilder.append("        }\n\n");

                methodsBuilder.append("        return \"[\" + String.join(\",\", result) + \"]\";\n");
                methodsBuilder.append("    }\n\n");
            }
            // 可以根据需要添加更多类型的处理
        }

        return methodsBuilder.toString();
    }

    /**
     * 获取类型的简化名称（用于方法名）
     */
    public String getTypeSimpleName(String type) {
        switch (type) {
            case "int":
                return "Int";
            case "long":
                return "Long";
            case "double":
                return "Double";
            case "boolean":
                return "Boolean";
            case "String":
                return "String";
            case "int[]":
                return "IntArray";
            case "long[]":
                return "LongArray";
            case "double[]":
                return "DoubleArray";
            case "String[]":
                return "StringArray";
            case "ListNode":
                return "ListNode";
            case "TreeNode":
                return "TreeNode";
            case "int[][]":
                return "IntMatrix";
            case "List<Integer>":
                return "ListOfInteger";
            case "List<List<Integer>>":
                return "ListOfListOfInteger";
            case "List<String>":
                return "ListOfString";
            // 添加更多复杂类型
            default:
                return "Object"; // 默认情况
        }
    }

    /**
     * 获取类型的默认值
     */
    public String getDefaultValue(String type) {
        switch (type) {
            case "int":
                return "0";
            case "long":
                return "0L";
            case "double":
                return "0.0";
            case "boolean":
                return "false";
            case "String":
                return "null";
            case "int[]":
                return "new int[0]";
            case "long[]":
                return "new long[0]";
            case "double[]":
                return "new double[0]";
            case "String[]":
                return "new String[0]";
            case "ListNode":
                return "null";
            case "TreeNode":
                return "null";
            case "int[][]":
                return "new int[0][0]";
            case "List<Integer>":
                return "new ArrayList<>()";
            case "List<List<Integer>>":
                return "new ArrayList<>()";
            case "List<String>":
                return "new ArrayList<>()";
            // 添加更多复杂类型的默认值
            default:
                return "null"; // 默认情况
        }
    }
}
