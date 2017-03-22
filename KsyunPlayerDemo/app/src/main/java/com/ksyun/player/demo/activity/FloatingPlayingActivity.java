package com.ksyun.player.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ksyun.player.demo.R;
import com.ksyun.player.demo.view.KSYFloatingWindowView;

/**
 * Created by xbc on 2017/3/14.
 */

public class FloatingPlayingActivity extends Activity implements Handler.Callback {

    public static final int LEAVE_FLOATING_WINDOW_PLAYING = 6789;
    public static final int REMOVE_FLOATING_WINDOW = LEAVE_FLOATING_WINDOW_PLAYING + 1;

    private ImageView mFloatingLeave;

    private KSYFloatingWindowView mFloatingView;
    private WindowManager.LayoutParams mFloatingViewParams;

    private WindowManager mWindowManager;

    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_floating_playing);

        mFloatingLeave = (ImageView) findViewById(R.id.floating_playing_back);
        mFloatingLeave.setOnClickListener(mOnClickListener);

        mHandler = new Handler(getMainLooper(), this);
        createFloatingWindow(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        removeFloatingWindow(getApplicationContext());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getAction()) {
            case KeyEvent.KEYCODE_BACK:
                FloatingPlayingActivity.this.finish();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FloatingPlayingActivity.this.finish();
        }
    };

    private void createFloatingWindow(Context context) {
        if (context == null)
            return;

        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (mFloatingView == null) {
            mFloatingView = new KSYFloatingWindowView(context);
            mFloatingView.setHandler(mHandler);
            if (mFloatingViewParams == null) {
                mFloatingViewParams = new WindowManager.LayoutParams();
                mFloatingViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                mFloatingViewParams.format = PixelFormat.RGBA_8888;
                mFloatingViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                mFloatingViewParams.gravity = Gravity.LEFT | Gravity.TOP;
                mFloatingViewParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mFloatingViewParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mFloatingViewParams.x = screenWidth;
                mFloatingViewParams.y = screenHeight;
            }

            mFloatingView.updateViewLayoutParams(mFloatingViewParams);
            windowManager.addView(mFloatingView, mFloatingViewParams);
        }
    }

    public void removeFloatingWindow(Context context) {
        if (mFloatingView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mFloatingView);
            mFloatingView = null;
        }
    }

    private WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case LEAVE_FLOATING_WINDOW_PLAYING:
                FloatingPlayingActivity.this.finish();
                break;
            case REMOVE_FLOATING_WINDOW:
                removeFloatingWindow(getApplicationContext());
                break;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
