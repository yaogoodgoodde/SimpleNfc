package com.yaoyao.simplenfc.utils;

/**
 * @author wangyao
 * @description:
 * @date: 2019/7/16
 */

public class StringUtils {

    /**
     * 获取非空字符串
     *
     * @param str
     * @return
     */
    public static String getString(String str) {
        return str == null ? "" : str;
    }
}
