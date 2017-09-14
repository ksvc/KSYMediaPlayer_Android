package com.ksyun.player.now.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ksyun.media.player.KSYTextureView;
import com.ksyun.player.now.R;
import com.ksyun.player.now.activity.VodMainActivity;
import com.ksyun.player.now.model.FloatingPlayer;

import java.lang.reflect.Field;

/**
 * 悬浮窗
 */

public class FloatWindowView extends RelativeLayout {
    private static final String TAG = "KSYFloatingWindowView";

    private static final int JUST_CLICK = 5;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private RelativeLayout video_content;
    private ImageView quit;

    private Handler mHandler;

    private int statusBarHeight;

    private float xInScreen, yInScreen; //当前手指位置

    private float xDownInScreen, yDownInScreen; //手指按下位置

    private float xInView, yInView;  //手指相对于悬浮窗位置


    public FloatWindowView(Context context) {
        super(context);
        init(context);
    }

    public FloatWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FloatWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.floating_player_window, this);

        video_content=(RelativeLayout)findViewById(R.id.floating_content);
        quit = (ImageView) findViewById(R.id.close_floating_window);

        quit.setOnClickListener(mOnClickListener);
    }

    public void updateViewLayoutParams(WindowManager.LayoutParams params) {
        mLayoutParams = params;
    }
    public void addFloatingWindow(KSYTextureView ksyTextureView){
        video_content.addView(ksyTextureView);
        FloatingPlayer.getInstance().getKSYTextureView().setVisibility(View.VISIBLE);
        FloatingPlayer.getInstance().getKSYTextureView().setComeBackFromShare(true);

    }
    public void removeFloatingWindow(KSYTextureView ksyTextureView){
        video_content.removeView(ksyTextureView);
    }
    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    // 在此处重写 onTouchEvent 处理相应的事件
    // 必须返回 true 表示在此已处理相应事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (Math.abs(xDownInScreen - xInScreen) < JUST_CLICK && Math.abs(yDownInScreen - yInScreen) < JUST_CLICK) {
                    if (mHandler != null)
                        mHandler.obtainMessage(VodMainActivity.REMOVE_FLOATING_WINDOW).sendToTarget();
                }
                break;
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX(); //相对于view的坐标
                yInView = event.getY();

                //getRaw()返回相对于屏幕左上角坐标
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();

                xInScreen = xDownInScreen;
                yInScreen = yDownInScreen;
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                updateViewPosition();
                break;
        }
        return true;
    }

    private void updateViewPosition() {
        mLayoutParams.x = (int) (xInScreen - xInView);
        mLayoutParams.y = (int) (yInScreen - yInView);
        mWindowManager.updateViewLayout(this, mLayoutParams);
    }

    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
           if (mHandler != null)
                mHandler.obtainMessage(VodMainActivity.STOP_FLOATING_WINDOW).sendToTarget();
        }
    };

}
