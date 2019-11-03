package com.yaoyao.simplenfc.utils;

import android.util.Log;

import com.yaoyao.simplenfc.BuildConfig;

/**
 * @author wangyao
 * @description: 自定义Log
 * @date: 2019/7/16
 */
public class Logger {


    public static void e(String TAG, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, StringUtils.getString(msg));
        }
    }

    public static void d(String TAG, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, StringUtils.getString(msg));
        }
    }

    public static void e(String TAG, Exception exception) {
        if (BuildConfig.DEBUG && exception != null) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }
    }

    public static void e(String TAG, Throwable throwable) {
        if (BuildConfig.DEBUG && throwable != null) {
            Log.e(TAG, Log.getStackTraceString(throwable));
        }
    }

    /**
     * 返回类名和代码行数
     *
     * @return
     */
    public static String getLineInfo() {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return ste.getFileName() + ": Line " + ste.getLineNumber();
    }
}
