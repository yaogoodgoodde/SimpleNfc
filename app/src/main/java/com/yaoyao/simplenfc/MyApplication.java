package com.yaoyao.simplenfc;

import android.app.Application;
import android.content.Context;

/**
 * @author wangyao
 * @description:
 * @date: 2019/7/16 22:02
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=this.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}
