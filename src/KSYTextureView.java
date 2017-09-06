package com.ksyun.media.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import com.ksyun.media.player.misc.KSYQosInfo;
import com.ksyun.media.player.misc.KSYTrackInfo;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 金山云提供的播放控件,对TextureView和KSYMediaPlayer进行封装,接口定义与KSYMediaPlayer保持一直,支持控件{@link IMediaController},具体接口定义可参见{@link KSYMediaPlayer}
 * @author xinbaicheng@kingsoft.com
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class KSYTextureView extends FrameLayout implements IMediaController.MediaPlayerControl , TextureView.SurfaceTextureListener{
    private RenderTextureView mTextureView;
    private IMediaController mMediaController;
    private KSYMediaPlayer   mMediaPlayer;
    private SurfaceTexture   mSurfaceTexture = null;
    private int              mVideoWidth;
    private int              mVideoHeight;
    private int              mVideoSarNum;
    private int              mVideoSarDen;
    private int              mDegree = 0;
    protected MediaInfo      mMediaInfo;
    private  boolean         mPause = false;
    protected int            mCurrentBufferPercentage;
    private  boolean        mShouldAutoPlay = true;
    private  boolean        mIsComeBackFromShare = false;
    private  boolean        mCanPause;
    private  boolean        mCanForward;
    private  boolean        mCanBackward;
    private  boolean        mHwDecode = true;
    private  int            mScaleMode = KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_RELOADING = 5;
    private static final int STATE_RELOADED = 6;
    private static final int STATE_STOPPED = 7;
    protected static final int STATE_PLAYBACK_COMPLETED = 8;
    public int mCurrentState = STATE_IDLE;

    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private IMediaPlayer.OnLogEventListener mOnLogEventListener;
    private IMediaPlayer.OnMessageListener mOnMessageListener;
    private IMediaPlayer.OnTimedTextListener mOnTimedTextListener;

    public KSYTextureView(Context context) {
        super(context);
        initVideoView(context);
        initMediaPlayer(context);
    }

    public KSYTextureView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public KSYTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
        initMediaPlayer(context);
    }

    private void initVideoView(Context context){
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;

        mTextureView = new RenderTextureView(context);
        mTextureView.setLayoutParams(layoutParams);
        mTextureView.setSurfaceTextureListener(this);

        this.addView(mTextureView);

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            if (mTextureView != null)
                mTextureView.setVerticalOrientation(false);
        }
        else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mTextureView != null)
                mTextureView.setVerticalOrientation(true);
        }

        mVideoWidth = mVideoHeight = mVideoSarNum = mVideoSarDen = 0;
        mCanPause = mCanForward = mCanBackward = false;
        mShouldAutoPlay = true;

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    private void initMediaPlayer(Context context){
        mMediaPlayer = new KSYMediaPlayer.Builder(context).build();
        mMediaPlayer.setOnPreparedListener(mPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
        mMediaPlayer.setOnErrorListener(mErrorListener);
        mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
        mMediaPlayer.setOnInfoListener(mInfoListener);
        mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
        mMediaPlayer.setOnLogEventListener(mLogEventListener);
        mMediaPlayer.setOnMessageListener(mInternalMessageListener);
        mMediaPlayer.setOnTimedTextListener(mInternalOnTimedTextListener);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            if (mTextureView != null)
                mTextureView.setVerticalOrientation(false);
        }
        else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mTextureView != null)
                mTextureView.setVerticalOrientation(true);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mVideoWidth == 0 || mVideoHeight == 0){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (mTextureView == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        //measureChild(mTextureView,widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int width = 0;int height = 0;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
            width = widthSpecSize;
            height = heightSpecSize;
        } else if (widthSpecMode == MeasureSpec.EXACTLY) {
            width = widthSpecSize;
            height = mTextureView.getMeasureHeight();
            if ((mDegree / 90) %2 != 0){
                height = mTextureView.getMeasureWidth();
            }
            if (height > heightSpecSize){
                height = heightSpecSize;
            }
        } else if (heightSpecMode == MeasureSpec.EXACTLY) {
            height = heightSpecSize;
            width = mTextureView.getMeasureWidth();
            if ((mDegree / 90) %2 != 0){
                width = mTextureView.getMeasureHeight();
            }
            if (width > widthSpecSize){
                width = widthSpecSize;
            }

        } else {
            width = mTextureView.getMeasureWidth();
            height = mTextureView.getMeasureHeight();
            if ((mDegree / 90) %2 != 0){
                height = mTextureView.getMeasureWidth();
                width = mTextureView.getMeasureHeight();
            }
            if (width > widthSpecSize){
                width = widthSpecSize;
            }
            if (height > heightSpecSize){
                height = heightSpecSize;
            }
        }

        setMeasuredDimension(width, height);

    }

    public void setMediaController(IMediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                    (View)this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(false);
            mMediaController.hide();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        if (mSurfaceTexture != null && isComeBackFromShare()){
            mSurfaceTexture.release();
            mSurfaceTexture = surfaceTexture;
        }

        if (mSurfaceTexture == null)
            mSurfaceTexture = surfaceTexture;

        if (mMediaPlayer != null){
            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        if (mTextureView != null){
            mTextureView.setScaleMode(mScaleMode);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {

        if (mMediaController != null) {
            mMediaController.hide();
        }

        return (mSurfaceTexture == null);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    //============================

    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {

        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {

        mOnCompletionListener = l;
    }

    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {

        mOnErrorListener = l;
    }

    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener l) {

        mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener l) {

        mOnSeekCompleteListener = l;
    }

    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {

        mOnInfoListener = l;
    }

    public void setOnVideoSizeChangedListener(IMediaPlayer.OnVideoSizeChangedListener l){
        mOnVideoSizeChangedListener = l;
    }

    public void setOnLogEventListener(IMediaPlayer.OnLogEventListener l){
        mOnLogEventListener = l;
    }

    public void setOnMessageListener(IMediaPlayer.OnMessageListener listener) {
        mOnMessageListener = listener;
    }

    public void setOnTimedTextListener(IMediaPlayer.OnTimedTextListener listener) {
        mOnTimedTextListener = listener;
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {

        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height,
                                       int sarNum, int sarDen) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoSarNum = sarNum;
            mVideoSarDen = sarDen;

            boolean canUpdateSize = ((mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSED));
            if (mTextureView != null && canUpdateSize) {
                mTextureView.setMeasureSize(mVideoWidth , mVideoHeight);
                mTextureView.setSarSize(mVideoSarNum, mVideoSarDen);
                setVideoScalingMode(mScaleMode);
            }
            if (mOnVideoSizeChangedListener != null)
                mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height, sarNum, sarDen);
        }
    };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(IMediaPlayer mp) {
            mCanPause = mCanForward = mCanBackward = true;

            if (mOnPreparedListener != null)
                mOnPreparedListener.onPrepared(mp);

            if (mShouldAutoPlay){
                mCurrentState = STATE_PLAYING;
            }
            else {
                mCurrentState = STATE_PREPARED;
            }
				
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
                if (mShouldAutoPlay) {
                    mMediaController.onStart();
                }
                else {
                    mMediaController.onPause();
                }
            }
        }
    };

    protected final IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(IMediaPlayer mp) {
            if (mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(mp);

            mCurrentState = STATE_PLAYBACK_COMPLETED;
            if (mMediaController != null) {
                mMediaController.hide();
            }
        }
    };

    protected final IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mp, framework_err, impl_err)) {
                    return true;
                }
            }
            mCurrentState = STATE_ERROR;
            if (mMediaController != null) {
                mMediaController.hide();
            }

            return true;
        }
    };

    protected final IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {

        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null) {
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
            }
        }
    };

    protected final IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            switch (what){
                case IMediaPlayer.MEDIA_INFO_SOFTWARE_DECODE:
                    mHwDecode = false;
                    break;
                case IMediaPlayer.MEDIA_INFO_HARDWARE_DECODE:
                    mHwDecode = true;
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    if (mMediaPlayer != null && !mHwDecode )
                        mMediaPlayer.setRotateDegree(0);
                    if (mCurrentState == STATE_RELOADING){
                        setRotateDegree(mDegree);
                    }
                    else {
                        setRotateDegree(extra);
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    if (mTextureView != null){
                        mTextureView.setMeasureSize(mVideoWidth , mVideoHeight);
                        mTextureView.setSarSize(mVideoSarNum, mVideoSarDen);
                    }
					setVideoScalingMode(mScaleMode);
                    mTextureView.setVisibility(VISIBLE);
                    break;
                case IMediaPlayer.MEDIA_INFO_RELOADED:
                    mTextureView.setVisibility(INVISIBLE);
                    mCanPause = mCanForward = mCanBackward = true;
                    mCurrentBufferPercentage = 0;

                    if (mShouldAutoPlay){
                        mCurrentState = STATE_PLAYING;
                    }
                    else {
                        mCurrentState = STATE_RELOADED;
                    }
                    if (mMediaController != null) {
                        mMediaController.setEnabled(true);
                        if (mShouldAutoPlay)
                            mMediaController.onStart();
                        else
                            mMediaController.onPause();
                    }
                    
                    break;
            }
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            }
            return true;
        }

    };

    protected final IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {

            if (mOnSeekCompleteListener != null)
                mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };

    protected  final  IMediaPlayer.OnLogEventListener mLogEventListener = new IMediaPlayer.OnLogEventListener() {
        @Override
        public void onLogEvent(IMediaPlayer mp, String log) {
            if (mOnLogEventListener != null)
                mOnLogEventListener.onLogEvent(mp,log);
        }
    };

    protected final IMediaPlayer.OnMessageListener mInternalMessageListener = new IMediaPlayer.OnMessageListener() {
        @Override
        public void onMessage(IMediaPlayer mp, String name, String info, double number) {
            if (mOnMessageListener != null)
                mOnMessageListener.onMessage(mp, name, info, number);
        }
    };

    protected IMediaPlayer.OnTimedTextListener mInternalOnTimedTextListener = new IMediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(IMediaPlayer mp, String text) {
            if (mOnTimedTextListener != null)
                mOnTimedTextListener.onTimedText(mp, text);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    public void setDataSource(Context context, Uri uri) throws IOException {
        if (mMediaPlayer != null){
            mMediaPlayer.setDataSource(context,uri);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException {
        if (mMediaPlayer != null){
            mMediaPlayer.setDataSource(context, uri, headers);
        }
    }
    public void setDataSource(String path) throws IOException {

        if (mMediaPlayer != null){
            mMediaPlayer.setDataSource(path);
        }
    }

    public void setDataSource(String path, Map<String, String> headers) throws IOException {
        if (mMediaPlayer != null){
            mMediaPlayer.setDataSource(path, headers);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void setDataSource(FileDescriptor fd) throws IOException {
        if (mMediaPlayer != null){
            mMediaPlayer.setDataSource(fd);
        }
    }

    public void setDataSource(FileDescriptor fd, long offset, long length)
            throws IOException, IllegalArgumentException, IllegalStateException{
        if (mMediaPlayer != null){
            mMediaPlayer.setDataSource(fd, offset, length);
        }
    }

    public void setDataSource(List<String> sourceList, Map<String, String> headers)
            throws IOException, IllegalArgumentException, IllegalStateException {
        if (mMediaPlayer != null)
            mMediaPlayer.setDataSource(sourceList, headers);
    }

    public void prepareAsync(){
        if (mMediaPlayer != null){
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
        }
    }

    public void setScreenOnWhilePlaying(boolean screenOn){
        if (mMediaPlayer != null){
            mMediaPlayer.setScreenOnWhilePlaying(screenOn);
        }
    }

    public void stop(){
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
        }
        mCurrentState = STATE_STOPPED;
        mPause = false;
        mCanBackward = mCanBackward = mCanPause = false;
    }

    public void release(){
        if (mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mCurrentState = STATE_IDLE;
        mSurfaceTexture = null;
    }

    private void  internalReset(){
        mDegree  = 0;
        mMediaInfo = null;
        mPause = false;
        mIsComeBackFromShare = false;
        mScaleMode = KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;
        mVideoWidth = mVideoHeight = mVideoSarNum = mVideoSarDen = 0;
        mCanPause = mCanForward = mCanBackward = false;
        mShouldAutoPlay = true;
        mCurrentState = STATE_IDLE;

        if (mTextureView != null){
            mTextureView.setVideoOffset(0.0f, 0.0f);
            mTextureView.setMeasureSize(0,0);
            mTextureView.setSarSize(0,0);
        }

        //should set surface
        if (mSurfaceTexture != null && mMediaPlayer != null) {
            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
        }

        if (mMediaController != null){
            mMediaController.setEnabled(false);
        }
    }

    public void reset(){
        if (mMediaPlayer != null){
            mMediaPlayer.reset();
            internalReset();
        }
    }

    public boolean isPlayable(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.isPlayable();
        }
        return  false;
    }

    @Override
    public void start() {
        if (mMediaPlayer != null){
            mMediaPlayer.start();
        }
        mPause = false;
        mCurrentState = STATE_PLAYING;
        if (mMediaController != null)
            mMediaController.onStart();
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null){
            mMediaPlayer.pause();
        }

        mPause = true;
        mCurrentState = STATE_PAUSED;
        if (mMediaController != null)
            mMediaController.onPause();
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer != null){
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer != null){
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(long pos) {
        if (mMediaPlayer != null){
            mMediaPlayer.seekTo(pos);
        }
    }

    public void seekTo(long pos, boolean accurate) {
        if (mMediaPlayer != null){
            mMediaPlayer.seekTo(pos,accurate);
        }
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null){
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public MediaInfo getMediaInfo() {

        if (mMediaPlayer != null){
            if (mMediaInfo == null) {
                mMediaInfo = mMediaPlayer.getMediaInfo();
            }
            return mMediaInfo;
        }

        mMediaInfo = null;
        return mMediaInfo;
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    public int getVideoWidth() {

        return mVideoWidth;
    }

    public int getVideoHeight() {

        return mVideoHeight;
    }

    public  void reload(String url, boolean flushBuffer){
        mIsComeBackFromShare = false;
        mPause = mCanPause = mCanForward = mCanBackward = false;
        mCurrentBufferPercentage = 0;

        mCurrentState = STATE_RELOADING;
        if (mMediaPlayer != null)
            mMediaPlayer.reload(url, flushBuffer);

        if (mMediaController != null) {
            mMediaController.setEnabled(false);
        }
    }

    public void reload(String path, boolean flushBuffer, KSYMediaPlayer.KSYReloadMode mode){
        mIsComeBackFromShare = false;
        mPause = mCanPause = mCanForward = mCanBackward = false;

        mCurrentBufferPercentage = 0;
        mCurrentState = STATE_RELOADING;
        if (mMediaController != null) {
            mMediaController.setEnabled(false);
        }
        if (mMediaPlayer != null){
            mMediaPlayer.reload(path,flushBuffer, mode);
        }
    }

    public int getAudioSessionId() {
        if (mMediaPlayer != null){
            return  mMediaPlayer.getAudioSessionId();
        }

        return  -1;
    }

    public void runInBackground(boolean audioPlay){
        if(mMediaPlayer != null)
        {
            if (!mPause) {
                if (!audioPlay) {
                    mMediaPlayer.pause();
                }
            }
        }
        if (mTextureView != null)
            mTextureView.setVisibility(View.INVISIBLE);
    }

    public  void runInForeground(){

        if (mTextureView != null && !mTextureView.isAvailable()) {
            if (mSurfaceTexture != null) {
                mTextureView.setSurfaceTexture(mSurfaceTexture);
            }
        }
        setComeBackFromShare(false);

        if (mTextureView != null)
            mTextureView.setVisibility(View.VISIBLE);
    }

    public boolean isComeBackFromShare() {
        return mIsComeBackFromShare;
    }

    public void setComeBackFromShare(boolean comeBackFromShare) {
        mIsComeBackFromShare = comeBackFromShare;
    }

    public void setVideoScalingMode(int mode){
        if (mTextureView != null){
            mScaleMode = mode;
            mTextureView.setScaleMode(mode);
        }
    }

    /**
     * 以设置的坐标为中心点对显示的画面进行缩放。缩放时视频画面宽高比保持不变,为原画宽高比例。此函数不可与setVideoOffset同时使用
     * 若视频宽高小于屏幕宽高则以屏幕中心点进行缩放
     *
     * @param ratio 视频画面的缩放比例,设置有效范围为[0.25,100]
     * @param x     缩放中心点横坐标
     * @param y     缩放中心点纵坐标
     */
    public void setVideoScaleRatio(float ratio, float x, float y){
        if (mTextureView != null){
            if (ratio>=0.25f && ratio<=100.0f)
                mTextureView.setVideoScaleRatio(ratio, x, y);
        }
    }

    public void setPlayableRanges(long startTime, long endTime) {
        if (mMediaPlayer != null)
            mMediaPlayer.setPlayableRanges(startTime, endTime);
    }

    /**
     * 获取但前视频的缩放比例值
     */
    public float getVideoScaleRatio(){
        if (mTextureView != null){
            return mTextureView.getVideoScaleRatio();
        }
        return  1.0f;
    }

    public  boolean setRotateDegree(int degree){
        if(degree % 90 != 0)
            return false;
        mDegree = degree;
        if (mTextureView != null)
            mTextureView.setDegree(-degree);

        return true;
    }

    public void setRotation(float rotation){
        setRotateDegree((int)rotation);
    }


    public void setDecodeMode(KSYMediaPlayer.KSYDecodeMode mode){
        if (mMediaPlayer != null){
            mMediaPlayer.setDecodeMode(mode);
        }
    }

    public void setMirror(boolean  mirror){
        if (mTextureView != null){
            mTextureView.setMirror(mirror);
        }
    }

    public void setVideoOffset(float horizontal, float vertical){
        if (mTextureView != null){
            mTextureView.setVideoOffset(horizontal, vertical);
        }
    }

    /**
     * 移动播放视频画面,画面超出屏幕可通过次函数移动画面将隐藏的画面显示出来.此函数一般结合setVideoScaleRatio使用,此函数不可与setVideoOffset同时使用
     * 若视频宽高小于屏幕宽高则无法进行移动
     *
     * @param deltaX     移动横向的相对位移
     * @param deltaY     移动纵向的相对位移
     */

    public void moveVideo(float deltaX, float deltaY){
        if (mTextureView != null){
            mTextureView.moveVideo(deltaX, deltaY);
        }
    }

    public void setDeinterlaceMode(KSYMediaPlayer.KSYDeinterlaceMode mode){
        if (mMediaPlayer != null){
            mMediaPlayer.setDeinterlaceMode(mode);
        }
    }

    private Bitmap getBitmap(IMediaPlayer mediaPlayer) {

        float scaleX = 1.0f;
        float scaleY = 1.0f;
        int frame_width = 0;
        int frame_height = 0;

        if (mTextureView == null)
            return null;

        if (mediaPlayer != null) {
            frame_width = mediaPlayer.getVideoWidth();
            frame_height = mediaPlayer.getVideoHeight();
        }

        if (frame_height == 0 || frame_width == 0)
            return null;

        Bitmap origin = mTextureView.getBitmap();
        if (origin == null)
            return null;

        int width = origin.getWidth();
        int height = origin.getHeight();

        int pivotPointX = (int) (frame_width / 2);
        int pivotPointY = (int) (frame_height / 2);

        scaleX = (float)frame_width/width;
        scaleY = (float)frame_height/height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY, pivotPointX, pivotPointY);

        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public String getVersion() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getVersion();
        }
        return "N/A";
    }

    public void shouldAutoPlay(boolean state){
        if (mMediaPlayer != null){
            mMediaPlayer.shouldAutoPlay(state);
            mShouldAutoPlay = state;
        }
    }

    public String getDataSource(){

        if (mMediaPlayer != null){
            return  mMediaPlayer.getDataSource();
        }

        return  null;
    }

    public void setWakeMode(Context context, int mode){
        if (mMediaPlayer != null){
            mMediaPlayer.setWakeMode(context,mode);
        }
    }

    public KSYTrackInfo[] getTrackInfo(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getTrackInfo();
        }
        return null;
    }

    public int getSelectedTrack(int trackType){
        if (mMediaPlayer != null){
            return mMediaPlayer.getSelectedTrack(trackType);
        }
        return 0;
    }

    public void selectTrack(int track){
        if (mMediaPlayer != null){
            mMediaPlayer.selectTrack(track);
        }
    }

    public void deselectTrack(int track){
        if (mMediaPlayer != null){
            mMediaPlayer.deselectTrack(track);
        }
    }
    public int getVideoSarNum(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getVideoSarNum();
        }
        return 0;
    }

    public int getVideoSarDen(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getVideoSarDen();
        }
        return 0;
    }

    public void setLooping(boolean looping){
        if (mMediaPlayer != null){
            mMediaPlayer.setLooping(looping);
        }
    }

    public boolean isLooping(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.isLooping();
        }
        return false;
    }

    public void softReset(){
        if (mMediaPlayer != null){
            mMediaPlayer.softReset();
            internalReset();
        }
    }

    public void setVideoRenderingState(int mode){
        if (mMediaPlayer != null){
            mMediaPlayer.setVideoRenderingState(mode);
        }
    }

    public void setSpeed(float speed){
        if (mMediaPlayer != null){
            mMediaPlayer.setSpeed(speed);
        }
    }

    public float getSpeed(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getSpeed();
        }

        return  1.0f;
    }

    public int getVideoDecoder(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.getVideoDecoder();
        }
        return  0;
    }

    public float getVideoOutputFramesPerSecond(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getVideoOutputFramesPerSecond();
        }
        return 0.0f;
    }

    public float getVideoDecodeFramesPerSecond(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getVideoDecodeFramesPerSecond();
        }
        return 0.0f;
    }

    public long getDecodedDataSize(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getDecodedDataSize();
        }

        return 0;
    }

    public long getDownloadDataSize(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getDownloadDataSize();
        }
        return  0;
    }

    public String getServerAddress(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getServerAddress();
        }
        return "N/A";
    }

    public int bufferEmptyCount(){
        if (mMediaPlayer != null){
            return mMediaPlayer.bufferEmptyCount();
        }

        return  0;
    }

    public float bufferEmptyDuration(){
        if (mMediaPlayer != null){
            return mMediaPlayer.bufferEmptyDuration();
        }

        return 0.0f;
    }

    public long getVideoCachedDuration(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.getVideoCachedDuration();
        }
        return  0;
    }

    public long getAudioCachedDuration(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.getAudioCachedDuration();
        }
        return  0;
    }

    public long getVideoCachedBytes(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.getVideoCachedBytes();
        }

        return 0;
    }

    public long getAudioCachedBytes(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.getAudioCachedBytes();
        }
        return  0;
    }

    public long getVideoCachedPackets(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getVideoCachedPackets();
        }
        return  0;
    }

    public long getAudioCachedPackets(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getAudioCachedPackets();
        }
        return  0;
    }

    public long getCurrentPts(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getCurrentPts();
        }
        return  0;
    }

    public long getStreamStartTime(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.getStreamStartTime();
        }
        return 0;
    }

    public  void setVolume(float leftVolume, float rightVolume){
        if (mMediaPlayer != null){
            mMediaPlayer.setVolume(leftVolume,rightVolume);
        }
    }

    public Bundle getMediaMeta(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.getMediaMeta();
        }
        return  null;
    }

    public void setTimeout(int prepareTimeout, int readTimeout){
        if (mMediaPlayer != null){
            mMediaPlayer.setTimeout(prepareTimeout,readTimeout);
        }
    }

    public void addTimedTextSource(String path) {
        if (mMediaPlayer != null)
            mMediaPlayer.addTimedTextSource(path);
    }

    public void setBufferSize(int size){
        if (mMediaPlayer != null){
            mMediaPlayer.setBufferSize(size);
        }
    }

    public KSYQosInfo getStreamQosInfo(){
        if (mMediaPlayer != null){
            return mMediaPlayer.getStreamQosInfo();
        }
        return null;
    }

    public void setBufferTimeMax(float timeSecond){
        if (mMediaPlayer != null){
            mMediaPlayer.setBufferTimeMax(timeSecond);
        }
    }

    public float getBufferTimeMax(){
        if (mMediaPlayer != null){
            return  mMediaPlayer.getBufferTimeMax();
        }

        return 0.0f;
    }

    public Bitmap getScreenShot(){
        if (mMediaPlayer != null) {
            return getBitmap(mMediaPlayer);
        }
        return null;
    }

    public void setPlayerMute(int mute){
        if (mMediaPlayer != null){
            mMediaPlayer.setPlayerMute(mute);
        }
    }
    public void setOnAudioPCMAvailableListener (KSYMediaPlayer.OnAudioPCMListener listener){
        if (mMediaPlayer != null){
            mMediaPlayer.setOnAudioPCMAvailableListener(listener);
        }
    }
    public void setVideoRawDataListener(KSYMediaPlayer.OnVideoRawDataListener listener){
        if (mMediaPlayer != null){
            mMediaPlayer.setVideoRawDataListener(listener);
        }
    }

    public void addVideoRawBuffer(byte[] rawBuffer){
        if (mMediaPlayer != null){
            mMediaPlayer.addVideoRawBuffer(rawBuffer);
        }
    }
    /** @hide */
    public void setOption(int category, String name, String value){
        if (mMediaPlayer != null){
            mMediaPlayer.setOption(category,name,value);
        }
    }
    /** @hide */
    public void setOption(int category, String name, long value) {
        if (mMediaPlayer != null){
            mMediaPlayer.setOption(category, name, value);
        }
    }

    public String getLocalDnsIP() {
        if (mMediaPlayer != null){
            mMediaPlayer.getLocalDnsIP();
        }
        return "N/A";
    }

    public String getClientIP() {
        if (mMediaPlayer != null){
            mMediaPlayer.getClientIP();
        }
        return "N/A";
    }

    public KSYMediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    class RenderTextureView extends TextureView implements  TextureView.SurfaceTextureListener{
        private  SurfaceTextureListener mListener;
        private  int    mVideoWidth;
        private  int    mVideoHeight;
        private  int    mVideoSarNum;
        private  int    mVideoSarDen;
        private  int    mMeasureWidth;
        private  int    mMeasureHeight;
        private  int    mScaleMode =  KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;
        private  int    mDegree;
        private  boolean mMirror = false;
        private  boolean mVerticalOrientation;
        boolean  mUseSettingRatio = false;
        private  float  mHOffset = 0.0f;
        private  float  mVOffset = 0.0f;
        private  Matrix mMatrix = new Matrix();

        private  int  mLayoutWidth;
        private  int  mLayoutHeight;
        private float mCenterPointX;
        private float mCenterPointY;
        private float mDeltaX;
        private float mDeltaY;
        private float mCurrentVideoWidth;
        private float mCurrentVideoHeight;
        private float mTotalTranslateX;
        private float mTotalTranslateY;
        private float mTotalRatio = 1.0f;
        private float mScaledRatio;
        private float mInitRatio;

        public static final int STATUS_NORMAL = 1;
        public static final int STATUS_ZOOM = 2;
        public static final int STATUS_MOVE = 3;
        private int mCurrentDispStatus = STATUS_NORMAL;

        public RenderTextureView(Context context) {
            super(context);
            super.setSurfaceTextureListener(this);
        }

        public RenderTextureView(Context context, AttributeSet attrs) {
            this(context, attrs , 0);

        }

        public RenderTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            super.setSurfaceTextureListener(this);
        }

        private void Zoom() {
            Matrix matrix   = mMatrix;
            int videoWidth  = mVideoWidth;
            int videoHeight = mVideoHeight;

            if (mMirror){
                mCenterPointX = mLayoutWidth-mCenterPointX;
            }
            if (mVideoSarNum > 0 && mVideoSarDen > 0) {
                videoWidth = videoWidth * mVideoSarNum / mVideoSarDen;
            }

            float scaleX = (float)videoWidth/mLayoutWidth;
            float scaleY = (float)videoHeight/mLayoutHeight;
            if (mScaleMode ==  KSYMediaPlayer.VIDEO_SCALING_MODE_NOSCALE_TO_FIT) {
                if ((mDegree / 90) % 2 != 0) {
                    scaleX = (float) mLayoutHeight / mLayoutWidth;
                    scaleY = (float) mLayoutWidth / mLayoutHeight;
                } else {
                    scaleX = 1.0f;
                    scaleY = 1.0f;
                }
            }

            matrix.reset();
            matrix.postScale(mTotalRatio*scaleX, mTotalRatio*scaleY);
            matrix.postRotate(mDegree);

            float scaledWidth = mLayoutWidth * mTotalRatio * scaleX;
            float scaledHeight = mLayoutHeight * mTotalRatio * scaleY;
            if ((mDegree / 90) %2 != 0){
                scaledWidth = mLayoutHeight * mTotalRatio * scaleY;
                scaledHeight = mLayoutWidth * mTotalRatio * scaleX;
            }
            float translateX = 0f;
            float translateY = 0f;

            translateX = mTotalTranslateX * mScaledRatio + mCenterPointX * (1 - mScaledRatio);
            translateY = mTotalTranslateY  * mScaledRatio + mCenterPointY * (1 - mScaledRatio);

            switch (mDegree){
                case 0:
                    if( scaledWidth < mLayoutWidth){
                        translateX = ((mLayoutWidth - scaledWidth) / 2f);
                    }
                    else {
                        if (translateX > 0) {
                            translateX = 0;
                        } else if (scaledWidth + translateX < mLayoutWidth) {
                            translateX = mLayoutWidth - scaledWidth;
                        }
                    }

                    if (scaledHeight < mLayoutHeight){
                        translateY = ((mLayoutHeight - scaledHeight) / 2f);
                    }
                    else {
                        if (translateY > 0) {
                            translateY = 0;
                        }
                        else if (scaledHeight + translateY < mLayoutHeight){
                            translateY = mLayoutHeight - scaledHeight;
                        }
                    }
                    break;
                case -90:
                    if( scaledWidth < mLayoutWidth){
                        translateX = ((mLayoutWidth - scaledWidth) / 2f);
                    }
                    else {
                        if (translateX > 0) {
                            translateX = 0;
                        } else if (scaledWidth + translateX < mLayoutWidth) {
                            translateX = mLayoutWidth - scaledWidth;
                        }
                    }

                    if (scaledHeight < mLayoutHeight){
                        translateY = ((mLayoutHeight + scaledHeight) / 2f);
                    }
                    else {
                        if (translateY > scaledHeight) {
                            translateY = scaledHeight;
                        }
                        else if ( translateY < mLayoutHeight){
                            translateY = mLayoutHeight ;
                        }
                    }
                    break;
                case -180:
                    if( scaledWidth < mLayoutWidth){
                        translateX = ((mLayoutWidth + scaledWidth) / 2f);
                    }
                    else {
                        if (translateX > scaledWidth) {
                            translateX = scaledWidth;
                        } else if (translateX < mLayoutWidth) {
                            translateX = mLayoutWidth;
                        }
                    }

                    if (scaledHeight < mLayoutHeight){
                        translateY = ((mLayoutHeight + scaledHeight) / 2f);
                    }
                    else {
                        if (translateY > scaledHeight) {
                            translateY = scaledHeight;
                        }
                        else if ( translateY < mLayoutHeight){
                            translateY = mLayoutHeight ;
                        }
                    }
                    break;
                case -270:
                    if( scaledWidth < mLayoutWidth){
                        translateX = ((mLayoutWidth + scaledWidth) / 2f);
                    }
                    else {
                        if (translateX > scaledWidth) {
                            translateX = scaledWidth;
                        } else if (translateX < mLayoutWidth) {
                            translateX = mLayoutWidth;
                        }
                    }

                    if (scaledHeight < mLayoutHeight){
                        translateY = ((mLayoutHeight - scaledHeight) / 2f);
                    }
                    else {
                        if( translateY > 0){
                            translateY = 0;
                        }
                        else if (scaledHeight + translateY < mLayoutHeight){
                            translateY = mLayoutHeight - scaledHeight;
                        }
                    }
                    break;
            }

//            translateX += mHOffset * mLayoutWidth/2;
//            translateY +=  - mVOffset* mLayoutHeight/2;
            matrix.postTranslate(translateX , translateY);

            mTotalTranslateX = translateX;
            mTotalTranslateY = translateY;
            mCurrentVideoWidth = scaledWidth;
            mCurrentVideoHeight = scaledHeight;

        }

        private void Move() {
            Matrix matrix   = mMatrix;
            int videoWidth  = mVideoWidth;
            int videoHeight = mVideoHeight;

            if (mMirror){
                mDeltaX = -mDeltaX;
            }
            if (mVideoSarNum > 0 && mVideoSarDen > 0) {
                videoWidth = videoWidth * mVideoSarNum / mVideoSarDen;
            }

            float scaleX = (float)videoWidth/mLayoutWidth;
            float scaleY = (float)videoHeight/mLayoutHeight;
            if (mScaleMode ==  KSYMediaPlayer.VIDEO_SCALING_MODE_NOSCALE_TO_FIT) {
                if ((mDegree / 90) % 2 != 0) {
                    scaleX = (float) mLayoutHeight / mLayoutWidth;
                    scaleY = (float) mLayoutWidth / mLayoutHeight;
                } else {
                    scaleX = 1.0f;
                    scaleY = 1.0f;
                }
            }

            matrix.reset();
            matrix.postScale(mTotalRatio*scaleX, mTotalRatio*scaleY);
            matrix.postRotate(mDegree);

            float xoffset = 0.f;
            float yoffset = 0.f;
            switch (mDegree){
                case 0:
                    xoffset = ((mLayoutWidth - mCurrentVideoWidth) / 2f);
                    yoffset = ((mLayoutHeight - mCurrentVideoHeight) / 2f);
                    break;
                case -90:
                    xoffset = (mLayoutWidth - mCurrentVideoWidth)/2;
                    yoffset = (mLayoutHeight + mCurrentVideoHeight)/2;
                    break;
                case -180:
                    xoffset = (mLayoutWidth + mCurrentVideoWidth)/2;
                    yoffset = (mLayoutHeight + mCurrentVideoHeight)/2;
                    break;
                case -270:
                    xoffset = ((mLayoutWidth + mCurrentVideoWidth) / 2f);
                    yoffset = ((mLayoutHeight - mCurrentVideoHeight) / 2f);
                    break;
            }

            if (mTotalTranslateX + mDeltaX  > xoffset + (mCurrentVideoWidth - mLayoutWidth)/2) {
                mDeltaX = 0;
            } else if ( mTotalTranslateX + mDeltaX < xoffset - (mCurrentVideoWidth - mLayoutWidth)/2) {
                mDeltaX = 0;
            }

            if (mTotalTranslateY + mDeltaY > yoffset + (mCurrentVideoHeight - mLayoutHeight)/2){
                mDeltaY = 0;
            } else if ( mTotalTranslateY + mDeltaY < yoffset - (mCurrentVideoHeight - mLayoutHeight)/2){
                mDeltaY = 0;
            }

            float translateX = mTotalTranslateX + mDeltaX;
            float translateY = mTotalTranslateY + mDeltaY;

            matrix.postTranslate(translateX , translateY );
            mTotalTranslateX = translateX;
            mTotalTranslateY = translateY;
        }

        private void Normal(int widthSpecMode, int heightSpecMode){
            float ratio = 1.0f;
            float hOffset = 0.0f;
            float vOffset = 0.0f;
            int videoWidth  = mVideoWidth;
            int videoHeight = mVideoHeight;
            int width       = mLayoutWidth;
            int height      = mLayoutHeight;
            Matrix matrix   = mMatrix;

            if (mVideoSarNum > 0 && mVideoSarDen > 0) {
                videoWidth = videoWidth * mVideoSarNum / mVideoSarDen;
            }

            float scaleX = (float)videoWidth/mLayoutWidth;
            float scaleY = (float)videoHeight/mLayoutHeight;

            if ((mDegree / 90) %2 != 0){
                videoHeight = mVideoWidth;
                videoWidth = mVideoHeight;

                if (mVideoSarNum > 0 && mVideoSarDen > 0) {
                    videoHeight = videoHeight * mVideoSarNum / mVideoSarDen;
                }
            }

            mInitRatio =  Math.min((float) width / videoWidth, (float) height / videoHeight);

            switch (mScaleMode) {
                case KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:
                    ratio = Math.max((float) width / videoWidth, (float) height / videoHeight);
                    hOffset = vOffset = 0.0f;
                    mTotalRatio  = ratio;
                    break;

                case KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT:
                    ratio = Math.min((float) width / videoWidth, (float) height / videoHeight);
                    hOffset = mHOffset;
                    vOffset = mVOffset;
                    mTotalRatio  = ratio;
                    break;
                case KSYMediaPlayer.VIDEO_SCALING_MODE_NOSCALE_TO_FIT:
                    if ((mDegree / 90) %2 != 0) {
                        scaleX = (float) height/width;
                        scaleY = (float) width/height;
                    }
                    else {
                        scaleX = 1.0f;
                        scaleY = 1.0f;
                    }
                    mTotalRatio = mInitRatio = ratio;
                    break;
            }


            if ((mDegree / 90) %2 != 0){
                mCurrentVideoWidth = height * scaleY * ratio;
                mCurrentVideoHeight = width * scaleX * ratio;
            }
            else
            {
                mCurrentVideoWidth = width * scaleX * ratio;
                mCurrentVideoHeight = height * scaleY * ratio;
            }

            matrix.reset();

            matrix.postScale(ratio*scaleX, ratio*scaleY);
            matrix.postRotate(mDegree);

            float translateX = 0.0f;
            float translateY = 0.0f;


            switch (mDegree){
                case 0:
                    translateX = ((width - mCurrentVideoWidth) / 2f);
                    translateY = ((height - mCurrentVideoHeight) / 2f);
                    break;
                case -90:
                    translateX = (width - mCurrentVideoWidth)/2;
                    translateY = (height + mCurrentVideoHeight)/2;
                    break;
                case -180:
                    translateX = (width + mCurrentVideoWidth)/2;
                    translateY = (height + mCurrentVideoHeight)/2;
                    break;
                case -270:
                    translateX = ((width + mCurrentVideoWidth) / 2f);
                    translateY = ((height - mCurrentVideoHeight) / 2f);
                    break;
            }
            mTotalTranslateX = translateX + hOffset*width/2;
            mTotalTranslateY = translateY - vOffset* height/2;

            matrix.postTranslate(mTotalTranslateX, mTotalTranslateY);
//            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY)
//                matrix.postTranslate(mHOffset*width/2,-mVOffset* height/2);
//            else if (widthSpecMode == MeasureSpec.EXACTLY){
//                matrix.postTranslate(mHOffset*width/2,0);
//            }
//            else if (heightSpecMode == MeasureSpec.EXACTLY){
//                matrix.postTranslate(0,-mVOffset* height/2);
//            }

            mMeasureWidth = (int) (width * ratio * scaleX );
            mMeasureHeight =(int) (height * ratio * scaleY );

        }

        private void Measure(int widthMeasureSpec, int heightMeasureSpec){
            if(mVideoWidth == 0 || mVideoHeight == 0) return;

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            mLayoutWidth = widthSpecSize;
            mLayoutHeight = heightSpecSize;

            if (mCurrentDispStatus == STATUS_NORMAL){
                Normal(widthSpecMode, heightSpecMode);
            }
            setTransform(mMatrix);
        }

        @Override
        protected void
        onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            Measure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            if (mListener != null){
                mListener.onSurfaceTextureAvailable(surfaceTexture,i , i1);
            }
            requestLayout();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            if (mListener != null){
                mListener.onSurfaceTextureSizeChanged(surfaceTexture,i , i1);
            }
            requestLayout();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (mListener != null){
                return mListener.onSurfaceTextureDestroyed(surfaceTexture);
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            if (mListener != null){
                mListener.onSurfaceTextureUpdated(surfaceTexture);
            }
        }

        public void setSurfaceTextureListener(SurfaceTextureListener listener) {
            mListener =  listener;
        }

        public void setMeasureSize(int width, int height){
            mVideoWidth = width;
            mVideoHeight = height;
        }

        public void setSarSize(int sarNum,int sarDen){
            mVideoSarNum = sarNum;
            mVideoSarDen = sarDen;
        }

        public void setMirror(boolean mirror){
            mMirror = mirror;
            setScaleX(mirror ? -1.0F : 1.0F);
        }

        public void setDegree(int degree){
            mDegree =  degree;
            mCurrentDispStatus = STATUS_NORMAL;
            requestLayout();
        }

        public void setScaleMode(int mode){
            mScaleMode = mode;
            mUseSettingRatio = false;
            mCurrentDispStatus = STATUS_NORMAL;

            requestLayout();
        }


        public void setVideoScaleRatio(float ratio, float x ,float y){
            //if ( (ratio < mTotalRatio && ratio < mInitRatio) || (ratio > mTotalRatio && ratio > 100*mInitRatio))
            if (( ratio < 0.25) || ( ratio > 100 ))
                return ;

            if (( mScaleMode == KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)&&
                    (mHOffset > 0.0f  ||
                            mHOffset < 0.0f ||
                            mVOffset > 0.0f ||
                            mVOffset < 0.0f))
                return ;

            mScaledRatio = ratio/mTotalRatio;
            mTotalRatio = ratio;
            mCenterPointX = x;
            mCenterPointY = y;
            mCurrentDispStatus = STATUS_ZOOM;
            Zoom();
            requestLayout();
        }

        public float getVideoScaleRatio(){
            return mTotalRatio;
        }

        public  void setVerticalOrientation(boolean vertical){
            mVerticalOrientation = vertical;
            mCurrentDispStatus = STATUS_NORMAL;
            requestLayout();
        }

        void setVideoOffset(float horizontal, float vertical){
            mHOffset = horizontal;
            mVOffset = vertical;
            mCurrentDispStatus = STATUS_NORMAL;
            requestLayout();
        }

        void moveVideo(float deltaX, float deltaY){
            if (( mScaleMode == KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)&&
                   (mHOffset > 0.0f  ||
                    mHOffset < 0.0f ||
                    mVOffset > 0.0f ||
                    mVOffset < 0.0f))
                return ;

            mDeltaX = deltaX;
            mDeltaY = deltaY;
            mCurrentDispStatus = STATUS_MOVE;
            Move();
            requestLayout();
        }

        public int getMeasureWidth(){
            return  mMeasureWidth;
        }

        public int getMeasureHeight(){
            return  mMeasureHeight;
        }

    }
}
