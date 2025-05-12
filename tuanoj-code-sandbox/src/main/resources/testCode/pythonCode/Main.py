#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import json
import re
from typing import List, Optional


class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        n = len(nums)
        for i in range(n):
            for j in range(i + 1, n):
                if nums[i] + nums[j] == target:
                    return [i, j]

        return []

# 解析整型数组
def parse_intarray(input_str):
    if input_str is None or input_str.strip() == "":
        return []
    input_str = input_str.strip()
    # 去除方括号
    if input_str.startswith('[') and input_str.endswith(']'):
        input_str = input_str[1:-1]
    if input_str == "":
        return []
    # 分割并解析每个数字
    parts = input_str.split(',')
    return [int(part.strip()) for part in parts if part.strip()]

# 格式化整型数组
def format_intarray(array):
    if array is None:
        return "null"
    return "[" + ",".join([str(x) for x in array]) + "]"

# 解析整数
def parse_int(input_str):
    if input_str is None or input_str.strip() == "":
        return 0
    return int(input_str.strip())

# 格式化整数
def format_int(value):
    return str(value)


def main():
    solution = Solution()

    # 从命令行接收参数
    input_str = ""
    if len(sys.argv) > 1:
        input_str = sys.argv[1]

    try:
        # 解析输入参数
        input_parts = input_str.split(' ')

        # 转换参数 1
        param1 = parse_intarray(input_parts[0]) if len(input_parts) > 0 else []

        # 转换参数 2
        param2 = parse_int(input_parts[1]) if len(input_parts) > 1 else 0

        # 调用Solution方法
        result = solution.twoSum(param1, param2)

        # 输出结果
        print(format_intarray(result))
    except Exception as e:
        print(f"执行异常: {str(e)}", file=sys.stderr)
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()