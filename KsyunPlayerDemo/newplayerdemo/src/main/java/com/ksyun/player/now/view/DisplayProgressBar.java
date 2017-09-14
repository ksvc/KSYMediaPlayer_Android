package com.ksyun.player.now.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ksyun.player.now.R;

/**
 * 音量和亮度调节的进度条
 */

public class DisplayProgressBar extends View {
    private int total,progress;
    private int width,height;
    public DisplayProgressBar(Context context) {
        super(context);
    }

    public DisplayProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DisplayProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width=getMeasuredWidth();
        height=getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint=new Paint();
        paint.setColor(getResources().getColor(R.color.content_background));
        canvas.drawRect(0,0,width,height,paint);
        paint.setColor(getResources().getColor(R.color.white));
        float bar_width=(float) width/(16*16)*15;
        float total_width=(float)width/16;

        for(int i=0;i<progress;i++){
            float left=i*(total_width);
            canvas.drawRect(left,0,left+bar_width,height,paint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
    public void setMax(int total){
        this.total=total;
    }
    public void setProgress(int progress){
        this.progress=progress;
        invalidate();
    }
}
