#include <iostream>
#include <vector>
#include <string>
#include <queue>
#include <sstream>
#include <algorithm>
#include <unordered_map>
using namespace std;


class Solution {
public:
    vector<int> twoSum(vector<int>& nums, int target) {
        int n = nums.size();
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                if (nums[i] + nums[j] == target) {
                    return {i, j};
                }
            }
        }
        return {};
    }
};

// 字符串分割函数
std::vector<std::string> splitString(const std::string& str, char delimiter) {
    std::vector<std::string> tokens;
    std::stringstream ss(str);
    std::string token;
    while (std::getline(ss, token, delimiter)) {
        tokens.push_back(token);
    }
    return tokens;
}

// 解析整型数组
std::vector<int> parseIntArray(const std::string& input) {
    std::vector<int> result;
    std::string trimmed = input;
    // 去除前后空白
    size_t start = trimmed.find_first_not_of(" \t\n\r\f\v");
    if (start != std::string::npos) {
        trimmed = trimmed.substr(start);
    } else {
        return result; // 空串
    }
    size_t end = trimmed.find_last_not_of(" \t\n\r\f\v");
    if (end != std::string::npos) {
        trimmed = trimmed.substr(0, end + 1);
    }

    // 去除方括号
    if (trimmed.front() == '[' && trimmed.back() == ']') {
        trimmed = trimmed.substr(1, trimmed.length() - 2);
    }
    if (trimmed.empty()) return result;

    // 分割并解析每个数字
    std::vector<std::string> parts = splitString(trimmed, ',');
    for (const auto& part : parts) {
        std::string p = part;
        // 去除部分的前后空白
        start = p.find_first_not_of(" \t\n\r\f\v");
        if (start != std::string::npos) {
            p = p.substr(start);
        }
        end = p.find_last_not_of(" \t\n\r\f\v");
        if (end != std::string::npos) {
            p = p.substr(0, end + 1);
        }
        // 解析为整数并添加到结果
        if (!p.empty()) {
            result.push_back(std::stoi(p));
        }
    }
    return result;
}

// 格式化整型数组
std::string formatIntArray(const std::vector<int>& array) {
    if (array.empty()) return "[]";
    std::string result = "[";
    for (size_t i = 0; i < array.size(); i++) {
        if (i > 0) result += ",";
        result += std::to_string(array[i]);
    }
    result += "]";
    return result;
}

// 解析整数
int parseInt(const std::string& input) {
    return std::stoi(input);
}

// 格式化整数
std::string formatInt(int value) {
    return std::to_string(value);
}


int main(int argc, char* argv[]) {
    Solution solution;

    // 从命令行接收参数
    std::string input = "";
    if (argc > 1) {
        input = argv[1];
    }

    try {
        // 解析输入参数
        std::vector<std::string> inputParts = splitString(input, ' ');

        // 转换参数 1
        std::vector<int> param1 = inputParts.size() > 0 ? parseIntArray(inputParts[0]) : std::vector<int>();

        // 转换参数 2
        int param2 = inputParts.size() > 1 ? parseInt(inputParts[1]) : 0;

        // 调用Solution方法
        std::vector<int> result = solution.twoSum(param1, param2);

        // 输出结果
        std::cout << formatIntArray(result) << std::endl;
    } catch (const std::exception& e) {
        std::cerr << "执行异常: " << e.what() << std::endl;
    }
    return 0;
}