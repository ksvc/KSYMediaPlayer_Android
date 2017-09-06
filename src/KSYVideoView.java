package com.ksyun.media.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.ksyun.media.player.misc.KSYQosInfo;
import com.ksyun.media.player.misc.KSYTrackInfo;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * 金山云提供的播放控件,对SurfaceView和KSYMediaPlayer进行封装,接口定义与KSYMediaPlayer保持一直,支持控件{@link IMediaController},具体接口定义可参见{@link KSYMediaPlayer}
 */

public class KSYVideoView extends FrameLayout implements IMediaController.MediaPlayerControl , SurfaceHolder.Callback{
    private  String TAG =    "KSYVideoView";

    private IMediaController mMediaController;
    private KSYMediaPlayer   ksyMediaPlayer;
    private int              mVideoWidth;
    private int              mVideoHeight;
    private int              mVideoSarNum;
    private int              mVideoSarDen;
    protected MediaInfo      mMediaInfo;
    protected int            mCurrentBufferPercentage;
    private  boolean        mShouldAutoPlay = true;
    private  boolean        mCanPause;
    private  boolean        mCanForward;
    private  boolean        mCanBackward;
    private  boolean        mHwDecode = false;
    private  int            mScaleMode = -1;
    private  int            mDegree = 0;
    private  KSYSurfaceView mSurfaceView;
    private  SurfaceHolder  mSurfaceHolder;

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


    public KSYVideoView(Context context) {
        super(context);
        initVideoView(context);
        initMediaPlayer(context);
    }

    public KSYVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KSYVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
        initMediaPlayer(context);
    }

    private void initVideoView(Context context){
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;

        mSurfaceView = new KSYSurfaceView(context);
        mSurfaceView.getHolder().addCallback(this);
        addView(mSurfaceView, layoutParams);
        mVideoWidth = mVideoHeight = mVideoSarNum = mVideoSarDen = 0;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    private void initMediaPlayer(Context context){
        ksyMediaPlayer = new KSYMediaPlayer.Builder(context).build();
        ksyMediaPlayer.setOnPreparedListener(mPreparedListener);
        ksyMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
        ksyMediaPlayer.setOnCompletionListener(mCompletionListener);
        ksyMediaPlayer.setOnErrorListener(mErrorListener);
        ksyMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
        ksyMediaPlayer.setOnInfoListener(mInfoListener);
        ksyMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
        ksyMediaPlayer.setOnLogEventListener(mLogEventListener);
        ksyMediaPlayer.setOnMessageListener(mInternalMessageListener);
        ksyMediaPlayer.shouldAutoPlay(false);
    }


    public void setMediaController(IMediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (ksyMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                    (View)this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(false);
            mMediaController.hide();
        }
    }


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
        return (ksyMediaPlayer != null);
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
                if (ksyMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!ksyMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (ksyMediaPlayer.isPlaying()) {
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

    public void setOnMessageListener(IMediaPlayer.OnMessageListener l) {
        mOnMessageListener = l;
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {

        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height,
                                       int sarNum, int sarDen) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoSarNum = sarNum;
            mVideoSarDen = sarDen;

            if (mCurrentState == STATE_PLAYING) {
                mSurfaceView.setMeasureSize(mVideoWidth, mVideoHeight);
                mSurfaceView.setSarSize(mVideoSarNum, mVideoSarDen);
                requestLayout();
            }
            //mSurfaceView.setMeasureSize(mVideoWidth, mVideoHeight);
            //mSurfaceView.setSarSize(mVideoSarNum,mVideoSarDen);

            if (mOnVideoSizeChangedListener != null)
                mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height, sarNum, sarDen);
        }
    };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(IMediaPlayer mp) {

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            if (mVideoWidth > 0 && mVideoHeight > 0) {
                //Log.e(TAG, "mVideoWidth: "+mVideoWidth + "mVideoHeight" + mVideoHeight);
                mSurfaceView.setMeasureSize(mVideoWidth, mVideoHeight);
            }

            if(mHwDecode) {
                mDegree = 0;
                mSurfaceView.setRotateDegree(mDegree);
            }
            else {
                if (mSurfaceView != null )
                    mSurfaceView.setRotateDegree(mDegree);
            }

            if (mOnPreparedListener != null)
                mOnPreparedListener.onPrepared(mp);

            mCurrentState = STATE_PREPARED;

            if (mSurfaceView != null  && !mSurfaceView.isShown()){
                mSurfaceView.setVisibility(View.VISIBLE);
            }
            mSurfaceView.requestLayout();
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
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            if (mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(mp);

            if (mMediaController != null) {
                mMediaController.hide();
            }
        }
    };

    protected final IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            if (mMediaController != null) {
                mMediaController.hide();
            }
            if (mOnErrorListener != null) {
                mCurrentState = STATE_ERROR;
                if (mOnErrorListener.onError(mp, framework_err, impl_err)) {
                    return true;
                }
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
                    setRotateDegree(extra);
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    break;
                case IMediaPlayer.MEDIA_INFO_RELOADED:
                    mCurrentBufferPercentage = 0;
                    if (mShouldAutoPlay && ksyMediaPlayer != null){
                        ksyMediaPlayer.start();
                    }
                    if (mSurfaceView != null){
                        mSurfaceView.setVisibility(VISIBLE);
                    }
                    if (mMediaController != null) {
                        mMediaController.setEnabled(true);
                        if (mShouldAutoPlay)
                            mMediaController.onStart();
                        else
                            mMediaController.onPause();
                    }

                    if (mShouldAutoPlay){
                        mCurrentState = STATE_PLAYING;
                    }
                    else {
                        mCurrentState = STATE_RELOADED;
                    }

                    setVideoScalingMode(mScaleMode);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCurrentState < STATE_PREPARED){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if(mVideoWidth == 0 || mVideoHeight == 0){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            //just for audio
            if (mCurrentState == STATE_PREPARED && mShouldAutoPlay) {
                start();
            }
            return;
        }
        if (mSurfaceView == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        measureChild(mSurfaceView,widthMeasureSpec, heightMeasureSpec);
        //measureChildren(widthMeasureSpec, heightMeasureSpec);
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
            height = mSurfaceView.getMeasureHeight();
            if (height > heightSpecSize){
                height = heightSpecSize;
            }
        } else if (heightSpecMode == MeasureSpec.EXACTLY) {
            height = heightSpecSize;
            width = mSurfaceView.getMeasureWidth();
            if (width > widthSpecSize){
                width = widthSpecSize;
            }

        } else {
            width = mSurfaceView.getMeasureWidth();
            height = mSurfaceView.getMeasureHeight();

            if (width > widthSpecSize){
                width = widthSpecSize;
            }
            if (height > heightSpecSize){
                height = heightSpecSize;
            }
        }
        setMeasuredDimension(width, height);

        if (mCurrentState == STATE_PREPARED && mShouldAutoPlay) {
            start();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mSurfaceView != null)
            mSurfaceView.getHolder().setFixedSize(width, height);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(ksyMediaPlayer != null) {
            ksyMediaPlayer.setDisplay(holder);
            mSurfaceHolder = holder;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(ksyMediaPlayer != null) {
            ksyMediaPlayer.setDisplay(null);
            mSurfaceHolder = null;
        }

        if (mMediaController != null) {
            mMediaController.hide();
        }
    }
    public void setDataSource(Context context, Uri uri) throws IOException {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setDataSource(context,uri);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setDataSource(context, uri, headers);
        }
    }
    public void setDataSource(String path) throws IOException {

        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setDataSource(path);
        }
    }

    public void setDataSource(String path, Map<String, String> headers) throws IOException {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setDataSource(path, headers);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void setDataSource(FileDescriptor fd) throws IOException {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setDataSource(fd);
        }
    }

    public void setDataSource(FileDescriptor fd, long offset, long length)
            throws IOException, IllegalArgumentException, IllegalStateException{
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setDataSource(fd, offset, length);
        }
    }

    public void setDataSource(List<String> sourceList, Map<String, String> headers)
            throws IOException, IllegalArgumentException, IllegalStateException {
        if (ksyMediaPlayer != null)
            ksyMediaPlayer.setDataSource(sourceList, headers);
    }


    public void prepareAsync(){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
        }
    }

    @Override
    public void start() {
        if (ksyMediaPlayer != null && mCurrentState >= STATE_PREPARED){
            ksyMediaPlayer.start();
            mCurrentState = STATE_PLAYING;

            if (mMediaController != null)
                mMediaController.onStart();
        }
    }

    @Override
    public void pause() {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;

            if (mMediaController != null)
                mMediaController.onPause();
        }
    }

    @Override
    public long getDuration() {
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(long l) {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.seekTo(l);
        }
    }

    public void seekTo(long pos, boolean accurate) {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.seekTo(pos,accurate);
        }
    }

    @Override
    public boolean isPlaying() {
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.isPlaying();
        }
        return  false;
    }

    @Override
    public int getBufferPercentage() {
        if (ksyMediaPlayer != null){
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

    public void setScreenOnWhilePlaying(boolean screenOn){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setScreenOnWhilePlaying(screenOn);
        }
    }

    public void stop(){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.stop();
        }
        mCurrentState = STATE_STOPPED;
        mCanBackward = mCanBackward = mCanPause = false;
    }

    public void release(){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.release();
            ksyMediaPlayer = null;
            mCurrentState = STATE_IDLE;
        }
    }

    private void  internalReset(){
        mMediaInfo = null;
        mHwDecode  = false;
        mScaleMode = -1;
        mDegree = 0;
        mCurrentBufferPercentage = 0;
        mVideoWidth = mVideoHeight = mVideoSarNum = mVideoSarDen = 0;
        mCanPause = mCanForward = mCanBackward = false;
        mCurrentState = STATE_IDLE;

        mShouldAutoPlay = true;

        if (mSurfaceView != null){
            mSurfaceView.resetView();;
            mSurfaceView.setVisibility(View.INVISIBLE);
        }

        //should set surface
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.shouldAutoPlay(false);
            ksyMediaPlayer.setDisplay(mSurfaceHolder);
        }

        if (mMediaController != null){
            mMediaController.setEnabled(false);
        }
    }

    public void reset(){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.reset();
            internalReset();
        }
    }

    public MediaInfo getMediaInfo() {

        if (ksyMediaPlayer != null){
            if (mMediaInfo == null) {
                mMediaInfo = ksyMediaPlayer.getMediaInfo();
            }
            return mMediaInfo;
        }

        mMediaInfo = null;
        return mMediaInfo;
    }

    public boolean isPlayable(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.isPlayable();
        }
        return  false;
    }

    public  void reload(String url, boolean flushBuffer){
        mCanPause = mCanForward = mCanBackward = false;
        mCurrentBufferPercentage = 0;
        mCurrentState = STATE_RELOADING;
        if (ksyMediaPlayer != null)
            ksyMediaPlayer.reload(url, flushBuffer);

        if (flushBuffer && (mSurfaceView != null)){
            mSurfaceView.setVisibility(INVISIBLE);
        }

        if (mMediaController != null) {
            mMediaController.setEnabled(false);
        }
    }

    public void reload(String path, boolean flushBuffer, KSYMediaPlayer.KSYReloadMode mode){
        mCanPause = mCanForward = mCanBackward = false;
        mCurrentBufferPercentage = 0;
        mCurrentState = STATE_RELOADING;

        if (flushBuffer && (mSurfaceView != null)){
            mSurfaceView.setVisibility(INVISIBLE);
        }

        if (mMediaController != null) {
            mMediaController.setEnabled(false);
        }
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.reload(path,flushBuffer, mode);
        }
    }

    public int getAudioSessionId() {
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getAudioSessionId();
        }

        return  -1;
    }


    public void setVideoScalingMode(int mode){
        mScaleMode = mode;
        if (mSurfaceView != null) {
            mSurfaceView.setVideoScalingMode(mode);
        }
    }

    public  boolean setRotateDegree(int degree){
        if (mHwDecode)
            return false;

        mDegree =  degree;
        if (mCurrentState > STATE_PREPARED)
            mSurfaceView.setRotateDegree(degree);

        if (ksyMediaPlayer != null)
            ksyMediaPlayer.setRotateDegree(degree);

        return true;
    }

    public void setRotation(float rotation){
        setRotateDegree((int)rotation);
    }


    public void setDecodeMode(KSYMediaPlayer.KSYDecodeMode mode){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setDecodeMode(mode);
        }
    }

    public void setMirror(boolean  mirror){
        if (mHwDecode )
            return;

        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setMirror(mirror);
        }
    }

    public void setVideoOffset(float horizontal, float vertical){
        if (mHwDecode )
            return ;

        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setVideoOffset(horizontal, vertical);
        }
    }

    public void setDeinterlaceMode(KSYMediaPlayer.KSYDeinterlaceMode mode){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setDeinterlaceMode(mode);
        }
    }

    public String getVersion() {
        if (ksyMediaPlayer != null) {
            return ksyMediaPlayer.getVersion();
        }
        return "N/A";
    }

    public void shouldAutoPlay(boolean state){
        mShouldAutoPlay = state;
    }

    public String getDataSource(){

        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getDataSource();
        }

        return  null;
    }

    public void setWakeMode(Context context, int mode){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setWakeMode(context,mode);
        }
    }

    public KSYTrackInfo[] getTrackInfo(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getTrackInfo();
        }
        return null;
    }

    public int getSelectedTrack(int trackType){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getSelectedTrack(trackType);
        }
        return 0;
    }

    public void selectTrack(int track){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.selectTrack(track);
        }
    }

    public void deselectTrack(int track){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.deselectTrack(track);
        }
    }
    public int getVideoSarNum(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getVideoSarNum();
        }
        return 0;
    }

    public int getVideoSarDen(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getVideoSarDen();
        }
        return 0;
    }

    public void setLooping(boolean looping){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setLooping(looping);
        }
    }

    public boolean isLooping(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.isLooping();
        }
        return false;
    }

    public void softReset(){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.softReset();
            internalReset();
        }
    }

    public void setVideoRenderingState(int mode){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setVideoRenderingState(mode);
        }
    }

    public void setSpeed(float speed){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setSpeed(speed);
        }
    }

    public float getSpeed(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getSpeed();
        }

        return  1.0f;
    }

    public int getVideoDecoder(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getVideoDecoder();
        }
        return  0;
    }

    public float getVideoOutputFramesPerSecond(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getVideoOutputFramesPerSecond();
        }
        return 0.0f;
    }

    public float getVideoDecodeFramesPerSecond(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getVideoDecodeFramesPerSecond();
        }
        return 0.0f;
    }

    public long getDecodedDataSize(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getDecodedDataSize();
        }

        return 0;
    }

    public long getDownloadDataSize(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getDownloadDataSize();
        }
        return  0;
    }

    public String getServerAddress(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getServerAddress();
        }
        return "N/A";
    }

    public int bufferEmptyCount(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.bufferEmptyCount();
        }

        return  0;
    }

    public float bufferEmptyDuration(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.bufferEmptyDuration();
        }

        return 0.0f;
    }

    public long getVideoCachedDuration(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getVideoCachedDuration();
        }
        return  0;
    }

    public long getAudioCachedDuration(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getAudioCachedDuration();
        }
        return  0;
    }

    public long getVideoCachedBytes(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getVideoCachedBytes();
        }

        return 0;
    }

    public long getAudioCachedBytes(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getAudioCachedBytes();
        }
        return  0;
    }

    public long getVideoCachedPackets(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getVideoCachedPackets();
        }
        return  0;
    }

    public long getAudioCachedPackets(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getAudioCachedPackets();
        }
        return  0;
    }

    public long getCurrentPts(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getCurrentPts();
        }
        return  0;
    }

    public long getStreamStartTime(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getStreamStartTime();
        }
        return 0;
    }

    public  void setVolume(float leftVolume, float rightVolume){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setVolume(leftVolume,rightVolume);
        }
    }

    public Bundle getMediaMeta(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getMediaMeta();
        }
        return  null;
    }

    public void setTimeout(int prepareTimeout, int readTimeout){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setTimeout(prepareTimeout,readTimeout);
        }
    }

    public void setBufferSize(int size){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setBufferSize(size);
        }
    }

    public KSYQosInfo getStreamQosInfo(){
        if (ksyMediaPlayer != null){
            return ksyMediaPlayer.getStreamQosInfo();
        }
        return null;
    }

    public void setBufferTimeMax(float timeSecond){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setBufferTimeMax(timeSecond);
        }
    }

    public float getBufferTimeMax(){
        if (ksyMediaPlayer != null){
            return  ksyMediaPlayer.getBufferTimeMax();
        }

        return 0.0f;
    }

    public Bitmap getScreenShot(){
        if (ksyMediaPlayer != null && !mHwDecode) {
            return ksyMediaPlayer.getScreenShot();
        }
        return null;
    }

    public void setPlayerMute(int mute){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setPlayerMute(mute);
        }
    }
    public void setOnAudioPCMAvailableListener (KSYMediaPlayer.OnAudioPCMListener listener){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setOnAudioPCMAvailableListener(listener);
        }
    }
    public void setVideoRawDataListener(KSYMediaPlayer.OnVideoRawDataListener listener){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setVideoRawDataListener(listener);
        }
    }

    public void addVideoRawBuffer(byte[] rawBuffer){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.addVideoRawBuffer(rawBuffer);
        }
    }
    /** @hide */
    public void setOption(int category, String name, String value){
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setOption(category,name,value);
        }
    }
    /** @hide */
    public void setOption(int category, String name, long value) {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setOption(category, name, value);
        }
    }

    public String getLocalDnsIP() {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.getLocalDnsIP();
        }
        return "N/A";
    }

    public String getClientIP() {
        if (ksyMediaPlayer != null){
            ksyMediaPlayer.getClientIP();
        }
        return "N/A";
    }

    public void setPlayableRanges(long startTime, long endTime) {
        if (ksyMediaPlayer != null)
            ksyMediaPlayer.setPlayableRanges(startTime, endTime);
    }


    class KSYSurfaceView extends SurfaceView {
        private  int    mVideoWidth;
        private  int    mVideoHeight;
        private  int    mVideoSarNum;
        private  int    mVideoSarDen;
        private  int    mMeasureWidth;
        private  int    mMeasureHeight;
        private  int    mDegree = 0;
        private  int    mScaleMode =  -1;

        public KSYSurfaceView(Context context) {
            super(context);
        }

        public KSYSurfaceView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public KSYSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        private void Measure(int widthMeasureSpec , int heightMeasureSpec){
            if(mVideoWidth == 0 || mVideoHeight == 0) {
                mMeasureWidth = MeasureSpec.getSize(widthMeasureSpec);
                mMeasureHeight = MeasureSpec.getSize(heightMeasureSpec);
                return;
            }

            float scaleX = 1.0f;
            float scaleY = 1.0f;
            int height = 0;
            int width = 0;
            int videoWidth  = mVideoWidth;
            int videoHeight = mVideoHeight;
            float ratio = 1.0f;

            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            width = widthSpecSize;
            height = heightSpecSize;

            if (mVideoSarNum > 0 && mVideoSarDen > 0) {
                videoWidth = videoWidth * mVideoSarNum / mVideoSarDen;
            }

            scaleX = (float)videoWidth/width;
            scaleY = (float)videoHeight/height;

            if ((mDegree / 90) %2 != 0){
                videoHeight = mVideoWidth;
                videoWidth = mVideoHeight;

                if (mVideoSarNum > 0 && mVideoSarDen > 0) {
                    videoHeight = videoHeight * mVideoSarNum / mVideoSarDen;
                }
            }

            switch (mScaleMode) {
                case KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:
                    ratio = Math.max((float) width / videoWidth, (float) height / videoHeight);
                    break;

                case KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT:
                    ratio = Math.min((float) width / videoWidth, (float) height / videoHeight);
                    break;
                case KSYMediaPlayer.VIDEO_SCALING_MODE_NOSCALE_TO_FIT:
                    if ((mDegree / 90) %2 != 0){
                        scaleX = (float) height/width;
                        scaleY = (float) width/height;
                    }
                    else {
                        scaleX = 1.0f;
                        scaleY = 1.0f;
                    }
                    break;
            }

            if ((mDegree / 90) %2 != 0){
                mMeasureHeight = (int) (width * ratio * scaleX);
                mMeasureWidth = (int) (height * ratio * scaleY);
            }
            else {
                mMeasureWidth = (int) (width * ratio * scaleX);
                mMeasureHeight = (int) (height * ratio * scaleY);
            }
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            Measure(widthMeasureSpec,heightMeasureSpec);
            if (mMeasureWidth > 0 && mMeasureHeight > 0)
                setMeasuredDimension(mMeasureWidth,mMeasureHeight);
            else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        public void setMeasureSize(int width, int height){
            if (mVideoWidth != width || mVideoHeight != height) {
                mVideoWidth = width;
                mVideoHeight = height;
            }
            this.getHolder().setFixedSize(0, 0);
        }

        public void setSarSize(int sarNum,int sarDen){
            mVideoSarNum = sarNum;
            mVideoSarDen = sarDen;
        }


        public int getMeasureWidth(){
            return  mMeasureWidth;
        }

        public int getMeasureHeight(){
            return  mMeasureHeight;
        }

        public void setVideoScalingMode(int mode){
            mScaleMode = mode;
            requestLayout();
        }

        public  boolean setRotateDegree(int degree){
            mDegree = degree;
            requestLayout();
            return true;
        }

        public void resetView(){
            mVideoWidth = 0;
            mVideoHeight = 0;
            mVideoSarNum = 0;
            mVideoSarDen = 0;
            mMeasureWidth = 0;
            mMeasureHeight = 0;
            mDegree = 0;
            mScaleMode =  -1;
        }
    }
}