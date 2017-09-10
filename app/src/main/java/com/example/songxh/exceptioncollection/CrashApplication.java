package com.example.songxh.exceptioncollection;

import android.app.Application;

/**
 * Created by Songxh on 2017/9/10.
 */

public class CrashApplication extends Application {
    private CrashHandler crashHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }
}
