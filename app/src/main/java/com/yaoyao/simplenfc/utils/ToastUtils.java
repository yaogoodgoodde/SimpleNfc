package com.yaoyao.simplenfc.utils;

import android.content.Context;
import android.widget.Toast;
import com.yaoyao.simplenfc.MyApplication;

/**
 * @author wangyao
 * @description:
 * @date: 2019/7/16 22:02
 */
public class ToastUtils {
    private static Toast toast;

    private static Context getContext() {
        return MyApplication.getContext();
    }

    public static void showShort(int resId) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT);
        toast.show();
    }


    public static void showShort(String msg) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showLong(int resId) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(getContext(), resId, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void showLong(String msg) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(getContext(), msg, Toast.LENGTH_LONG);
        toast.show();
    }


}
