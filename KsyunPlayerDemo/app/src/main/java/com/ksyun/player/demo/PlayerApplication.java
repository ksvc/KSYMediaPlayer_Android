package com.ksyun.player.demo;

import android.app.Application;

import com.tencent.bugly.Bugly;

/**
 * Created by admin on 17/6/21.
 */

public class PlayerApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        Bugly.init(getApplicationContext(), "fa7327d922", true);
        Bugly.setAppChannel(getApplicationContext(), "public");

    }
}
