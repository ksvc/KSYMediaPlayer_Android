package com.ksyun.player.demo.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.ksyun.player.demo.R;


/**
 * Created by liubohua on 16/12/19.
 */
public class ProgressTextView extends View {
    private static final String TAG = ProgressTextView.class.getSimpleName();

    private int mTextColor = Color.WHITE;
    private int mHeight;
    private int mWidth;
    private double mOneProgressHeight;
    private int mCurProgress = 0;
    private String mProgressText = "";
    private int mMaxProgress = 200;
    private Paint mPaint;
    private float mThumbOffset;
    private int mTextSize = 36;

    private float mheight;

    public ProgressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (null != context && attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressTextView);
            mTextColor = array.getColor(R.styleable.ProgressTextView_ptv_textColor, Color.WHITE);
            mTextSize = array.getDimensionPixelSize(R.styleable.ProgressTextView_ptv_thumWidth, 36);
            float thumHeight = array.getDimension(R.styleable.ProgressTextView_ptv_thumWidth, 20);
            mThumbOffset = thumHeight / 2;
        }
        initObserver();
    }

    private void initObserver() {
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                mHeight = getMeasuredHeight();
                mWidth = getMeasuredWidth();
                initPaint();
                initData();
                return true;
            }
        });
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.RIGHT);
        mPaint.setColor(mTextColor);
    }

    private void initData() {
        mheight = (float) mHeight - mThumbOffset;
        mOneProgressHeight = (double) (mheight - 2 * mThumbOffset) / (mMaxProgress);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawText(canvas);
        super.onDraw(canvas);
    }

    private void drawText(Canvas canvas) {
        float y = (float) (mCurProgress * mOneProgressHeight);
        float yy = (mheight - y - mThumbOffset) + (((float) mCurProgress) / 200) * 2 * mThumbOffset;
        canvas.translate(mThumbOffset, 0);
        canvas.drawText(mProgressText, mWidth - 20, yy, mPaint);
    }

    public void setProgress(int progress, String showText) {
        mCurProgress = progress;
        mProgressText = showText;
        invalidate();
    }
}