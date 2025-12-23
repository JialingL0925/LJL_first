package com.example.demo.util;

/**
 * 字符串工具类
 */
public class StringUtil {
    
    /**
     * 判断字符串是否不为空
     * @param str 字符串
     * @return true不为空，false为空
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否为空
     * @param str 字符串
     * @return true为空，false不为空
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}