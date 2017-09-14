package com.ksyun.player.now.utils;

import android.app.Activity;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

/**
 * 控制亮度显示
 */

public class Display {
    private static float startLight,currentLight;
    public static void setLight(Activity context, float brightness, WindowManager.LayoutParams params){
        params.screenBrightness=brightness;
        context.getWindow().setAttributes(params);
    }
    public  static float changeLight(Activity context,float total,float brightness, WindowManager.LayoutParams params){
        currentLight=startLight+(brightness/((total/2)));
        if(currentLight<0){
            currentLight=0f;
        }
        if(currentLight>1){
            currentLight=1.0f;
        }
        Log.i("light",""+currentLight);
        params.screenBrightness=currentLight;
        context.getWindow().setAttributes(params);
        return currentLight;
    }
    public static float getCurrentLight(){
        startLight=currentLight;
        return currentLight;
    }
    public static  float getSystemBrightness(Activity context) {
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        startLight=((float)systemBrightness)/255.0f;
        Log.i("start",""+startLight+","+systemBrightness);
        return systemBrightness;
    }
}
