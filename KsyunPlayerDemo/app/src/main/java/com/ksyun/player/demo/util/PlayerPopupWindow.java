package com.ksyun.player.demo.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ksyun.player.demo.R;
import com.ksyun.player.demo.activity.SettingActivity;


/**
 * Created by QianYi-Xin on 2015/6/3.
 */
public class PlayerPopupWindow {

    private Activity mActivity;
    private Handler mHandler;

    private PopupWindow mPopupWin;
    private TextView mTextViewLeft;
    private TextView mTextViewRight;

    public PlayerPopupWindow(Activity activity, Handler handler) {
        mActivity = activity;
        mHandler = handler;
    }

    public void init() {
        View popUpView = mActivity.getLayoutInflater().inflate(R.layout.popup_layout, null);
        mTextViewLeft = (TextView) popUpView.findViewById(R.id.popup_left);
        mTextViewRight = (TextView) popUpView.findViewById(R.id.popup_right);
        mTextViewLeft.setOnClickListener(mLeftListener);
        mTextViewRight.setOnClickListener(mRightlistener);

        mPopupWin = new PopupWindow(popUpView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        mPopupWin.setTouchable(true);
        mPopupWin.setOutsideTouchable(true);
        mPopupWin.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), (Bitmap) null));

        mPopupWin.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if(mPopupWin != null && !mPopupWin.isShowing())
                        mPopupWin.dismiss();
                }
                return false;
            }
        });
    }

    public void showPopWindow(View view) {
        if(mPopupWin != null && !mPopupWin.isShowing())
            mPopupWin.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    private View.OnClickListener mLeftListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPopupWin.dismiss();{
                mHandler.obtainMessage(SettingActivity.PLAY_VIDEO, 0, 0).sendToTarget();
            }
        }
    };

    private View.OnClickListener mRightlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPopupWin.dismiss();
            if(mHandler != null)
                mHandler.obtainMessage(SettingActivity.PLAY_VIDEO, 1, 0).sendToTarget();
        }
    };
}
