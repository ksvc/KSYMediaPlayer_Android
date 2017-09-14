package com.ksyun.player.demo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.github.mzule.activityrouter.annotation.Module;
import com.github.mzule.activityrouter.annotation.Modules;

import java.util.Timer;
import java.util.TimerTask;



@Modules({"app", "oldplayerdemo", "newplayerdemo"})
@Module("app")
public class SplashActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;
    private Timer timer;
    private TimerTask timerTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        button=(Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpToNext();

            }
        });
        textView=(TextView)findViewById(R.id.sdk_version);
        String version=getVersion();
        if(!version.equals("error")){
            textView.setText("SDK V"+version+"版本");
        }
        setTimer();
    }
    private void setTimer(){
        timerTask=new MyTimerTask();
        timer=new Timer();
        timer.schedule(timerTask,3000);
    }
    private void jumpToNext(){
        Intent intent=new Intent(SplashActivity.this, ChooseActivity.class);
        startActivity(intent);
        timer.cancel();
        finish();
    }
    private String getVersion(){
        try {
            PackageManager manager=getPackageManager();
            PackageInfo packageInfo=manager.getPackageInfo(getPackageName(),0);
            String version=packageInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "error";
        }

    }
    private class MyTimerTask extends TimerTask
    {
        @Override
        public void run() {
            jumpToNext();
        }
    }
}
