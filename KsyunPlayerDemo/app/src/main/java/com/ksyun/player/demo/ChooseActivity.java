package com.ksyun.player.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.mzule.activityrouter.router.Routers;
import com.ksyun.player.now.utils.Setting;


public class ChooseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int OVERLAY_PERMISSION_RESULT_CODE = 170;

    private LinearLayout toOldVersion;

    private ImageView vodImagView,liveImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        initView();
        checkPermission();
    }
    private void initView(){
        toOldVersion=(LinearLayout)findViewById(R.id.to_old_version);
        vodImagView=(ImageView)findViewById(R.id.vod);
        liveImageView=(ImageView)findViewById(R.id.live);
        toOldVersion.setOnClickListener(this);
        liveImageView.setOnClickListener(this);
        vodImagView.setOnClickListener(this);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_RESULT_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_RESULT_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (!Settings.canDrawOverlays(this))
                    Toast.makeText(this, "Permission SYSTEM_ALERT_WINDOW not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.to_old_version:
                Routers.open(ChooseActivity.this, "oldVersionMain://oldMain");
                break;
            case R.id.vod:
                Routers.open(ChooseActivity.this, "vod://vodMain");
                break;
            case R.id.live:
                Routers.open(ChooseActivity.this, "live://liveMain");
                break;
            default:
                break;
        }
    }
}
