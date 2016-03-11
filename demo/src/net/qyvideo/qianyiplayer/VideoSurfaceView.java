package net.qyvideo.qianyiplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by xbc on 16/3/2.
 * @author xinbaicheng@kingsoft.com
 */
public class VideoSurfaceView extends SurfaceView {

    private int mVideoWidth;
    private int mVideoHeight;

    private int mMeasuredWidth;
    private int mMeasuredHeight;

    public VideoSurfaceView(Context context) {
        super(context);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasuredWidth, mMeasuredHeight);
    }

    public void setVideoDimension(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    private void doMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = View.getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = View.getDefaultSize(mVideoHeight, heightMeasureSpec);

        if(mVideoWidth > 0 && mVideoHeight > 0)
        {
            int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
            float specAspectRatio = (float) widthSpecSize / (float) heightSpecSize;
            float displayAspectRatio = (float) mVideoWidth / (float) mVideoHeight;
            boolean shouldBeWider = displayAspectRatio > specAspectRatio;

            if (shouldBeWider) {
                // not high enough, fix height
                height = heightSpecSize;
                width = (int) (height * displayAspectRatio);
            } else {
                // not wide enough, fix width
                width = widthSpecSize;
                height = (int) (width / displayAspectRatio);
            }
        }

        mMeasuredWidth = width;
        mMeasuredHeight = height;
    }
}
