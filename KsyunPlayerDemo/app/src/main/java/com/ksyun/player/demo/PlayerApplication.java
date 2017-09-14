package com.ksyun.player.demo;

import android.app.Application;

import com.ksyun.media.player.KSYHardwareDecodeWhiteList;
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

        // 初始化金山云的Android硬解白名单功能, 此功能需与金山云服务器通信，建议尽早调用
        // 若不想使用该功能，请勿调用此接口
        KSYHardwareDecodeWhiteList.getInstance().init(this);
    }
}
