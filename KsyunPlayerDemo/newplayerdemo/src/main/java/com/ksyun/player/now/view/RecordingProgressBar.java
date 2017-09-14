package com.ksyun.player.now.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.ksyun.player.now.R;

/**
 * 视频录制进度条
 */

public class RecordingProgressBar extends ProgressBar {
    private int height,width;
    private int startIndex;
    public RecordingProgressBar(Context context) {
        super(context);
    }

    public RecordingProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height=getMeasuredHeight();
        width=getMeasuredWidth();
        startIndex=width/5;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint=new Paint();
        paint.setColor(getResources().getColor(R.color.white));
        canvas.drawRect(startIndex,0,startIndex+10,height,paint);
    }
}
