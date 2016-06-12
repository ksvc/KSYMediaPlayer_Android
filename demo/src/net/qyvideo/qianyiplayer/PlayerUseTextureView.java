package net.qyvideo.qianyiplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;
import android.content.Context;
import android.content.res.Configuration;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaMeta;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.https.KsyHttpResponse;
import com.ksyun.media.player.misc.IMediaFormat;
import com.ksyun.media.player.misc.ITrackInfo;
import com.ksyun.media.player.misc.KSYMediaFormat;


import net.qyvideo.qianyiplayer.util.QosObject;
import net.qyvideo.qianyiplayer.util.QosThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.microedition.khronos.opengles.GL10;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class PlayerUseTextureView extends Activity implements  TextureView.SurfaceTextureListener{

    private static final String TAG = "VideoPlayerActivity";

    public static final int UPDATE_SEEKBAR = 0;
    public static final int HIDDEN_SEEKBAR = 1;
    public static final int UPDATE_QOS  = 2;

    private Context mContext;
    private KSYMediaPlayer ksyMediaPlayer;
    private QosThread mQosThread;

    private Surface mSurface = null;
    private TextureView mVideoTextureView = null;
    private SurfaceTexture mSurfaceTexture = null;
    private SurfaceHolder mSurfaceHolder = null;

    private Handler mHandler;

    // UI
    private LinearLayout mPlayerPanel;
    private ImageView mPlayerStartBtn;
    private SeekBar mPlayerSeekbar;
    private TextView mPlayerPosition;
    private TextView mLoadText;
    private TextView mCpu;
    private TextView mMemInfo;
    private TextView mVideoResolution;
    private TextView mVideoBitrate;
    private TextView mFrameRate;
    private TextView mCodecType;
    private TextView mServerIp;
    private TextView mSdkVersion;
    private TextView mDNSTime;
    private TextView mHttpConnectionTime;

    private Button mPlayerScaleVideo;

    private boolean mPlayerPanelShow = false;
    private boolean mPause = false;

    private long mStartTime = 0;
    private long mPauseStartTime = 0;
    private long mPausedTime = 0;

    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private static final int MAGIC_TEXTURE_ID = 10;
    private int mVideoScaleIndex = 0;
    private int index = 0;
    boolean     useHwCodec = false;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {

            mVideoWidth = ksyMediaPlayer.getVideoWidth();
            mVideoHeight = ksyMediaPlayer.getVideoHeight();

            // Set Video Scaling Mode
            ksyMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

            //start player
            ksyMediaPlayer.start();

            //set progress
            setVideoProgress(0);

            //start hardware info thread
            if (mQosThread != null)
                mQosThread.start();

            //update UI
            mCpu.setVisibility(View.VISIBLE);
            mMemInfo.setVisibility(View.VISIBLE);

            if(ksyMediaPlayer.getServerAddress() != null)
                mServerIp.setText("ServerIP: "+ ksyMediaPlayer.getServerAddress());

            //  get meta data
            Bundle bundle = ksyMediaPlayer.getMediaMeta();
            KSYMediaMeta meta = KSYMediaMeta.parse(bundle);
            if (meta != null)
            {
                if (meta.mHttpConnectTime > 0) {
                    double http_connection_time = Double.valueOf(meta.mHttpConnectTime);
                    mHttpConnectionTime.setText("HTTP Connection Time: " + (int)http_connection_time);
                }

                int dns_time = meta.mAnalyzeDnsTime;
                if (dns_time > 0) {
                    mDNSTime.setText("DNS time: " + dns_time);
                }
            }

            mSdkVersion.setText("SDK version: " + ksyMediaPlayer.getVersion());

            mVideoResolution.setText("Resolution:" + ksyMediaPlayer.getVideoWidth() + "x" + ksyMediaPlayer.getVideoHeight());
            mVideoResolution.setVisibility(View.VISIBLE);
            mFrameRate.setVisibility(View.VISIBLE);
            mVideoBitrate.setVisibility(View.VISIBLE);
            mCodecType.setVisibility(View.VISIBLE);

            ITrackInfo trackInfos[] = ksyMediaPlayer.getTrackInfo();
            if(trackInfos != null) {
                for(ITrackInfo infos : trackInfos)
                {
                    if(infos.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_VIDEO)
                    {
                        IMediaFormat mediaFormat = infos.getFormat();
                        String codecType = mediaFormat.getString(KSYMediaFormat.KEY_IJK_CODEC_LONG_NAME_UI);
                        mFrameRate.setText("FrameRate: " + mediaFormat.getString(KSYMediaFormat.KEY_IJK_FRAME_RATE_UI));

                        if(codecType.equals("hevc"))
                            mCodecType.setText("Codec: H.265/HEVC");
                        else if(codecType.equals("h264"))
                            mCodecType.setText("Codec: H.264/AVC");
                    }
                }
            } // end of for loop

            mStartTime = System.currentTimeMillis();

        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            long duration = ksyMediaPlayer.getDuration();
            long progress = duration * percent/100;
            mPlayerSeekbar.setSecondaryProgress((int)progress);
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangeListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
            if(mVideoWidth > 0 && mVideoHeight > 0) {
                if(width != mVideoWidth || height != mVideoHeight) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();

                    if(ksyMediaPlayer != null)
                        ksyMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                }
            }
        }
    };

    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompletedListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            Log.e(TAG, "onSeekComplete...............");
        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            Toast.makeText(mContext, "OnCompletionListener, play complete.", Toast.LENGTH_LONG).show();
            videoPlayEnd();
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            switch (what)
            {
                case KSYMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    Log.e(TAG, "OnErrorListener, Error Unknown:" + what + ",extra:" + extra);
                    break;
                default:
                    Log.e(TAG, "OnErrorListener, Error:" + what + ",extra:" + extra);
            }

            videoPlayEnd();

            return false;
        }
    };

    public IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            Log.d(TAG, "onInfo, what:"+i+",extra:"+i1);
            return false;
        }
    };

    private View.OnClickListener mVideoScaleButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int mode = mVideoScaleIndex % 2;
            mVideoScaleIndex++;

            if(ksyMediaPlayer != null) {
                if(mode == 1)
                    ksyMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                else
                    ksyMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            }
        }
    };

    public static void glCheck(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + android.opengl.GLUtils.getEGLErrorString(error));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getApplicationContext();
        useHwCodec = getIntent().getBooleanExtra("HWCodec",false);
        setContentView(R.layout.player_textureview);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            Toast.makeText(this,"Android version must >= JELLY_BEAN",Toast.LENGTH_SHORT).show();
        }

        mPlayerPanel = (LinearLayout) findViewById(R.id.player_panel);
        mPlayerStartBtn = (ImageView) findViewById(R.id.player_start);
        mPlayerSeekbar = (SeekBar) findViewById(R.id.player_seekbar);
        mPlayerPosition = (TextView) findViewById(R.id.player_time);
        mLoadText = (TextView) findViewById(R.id.loading_text);
        mCpu = (TextView) findViewById(R.id.player_cpu);
        mMemInfo = (TextView) findViewById(R.id.player_mem);
        mVideoResolution = (TextView) findViewById(R.id.player_re);
        mVideoBitrate = (TextView) findViewById(R.id.player_br);
        mFrameRate = (TextView) findViewById(R.id.player_fr);
        mCodecType = (TextView) findViewById(R.id.player_codec);
        mServerIp = (TextView) findViewById(R.id.player_ip);
        mSdkVersion = (TextView) findViewById(R.id.player_sdk_version);
        mDNSTime = (TextView) findViewById(R.id.player_dns_time);
        mHttpConnectionTime = (TextView) findViewById(R.id.player_http_connection_time);

        mPlayerScaleVideo = (Button) findViewById(R.id.player_scale);
        mPlayerScaleVideo.setOnClickListener(mVideoScaleButton);

        mPlayerStartBtn.setOnClickListener(mStartBtnListener);
        mPlayerSeekbar.setOnSeekBarChangeListener(mSeekBarListener);
        mPlayerSeekbar.setEnabled(true);

        mVideoTextureView = (TextureView) findViewById(R.id.player_surface);
        mVideoTextureView.setSurfaceTextureListener(this);
        mVideoTextureView.setOnTouchListener(mTouchListener);
        mVideoTextureView.setKeepScreenOn(true);

        mLoadText.setVisibility(View.VISIBLE);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mHandler = new UIHandler(this);
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        mQosThread = new QosThread(activityManager, mHandler);

        String mrl = getIntent().getStringExtra("path");

        ksyMediaPlayer = new KSYMediaPlayer.Builder(mContext).build();
        ksyMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        ksyMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        ksyMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        ksyMediaPlayer.setOnInfoListener(mOnInfoListener);
        ksyMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangeListener);
        ksyMediaPlayer.setOnErrorListener(mOnErrorListener);
        ksyMediaPlayer.setOnSeekCompleteListener(mOnSeekCompletedListener);
        ksyMediaPlayer.setScreenOnWhilePlaying(true);
        ksyMediaPlayer.setBufferTimeMax(5);

        if (useHwCodec) {
            //硬解264&265
            ksyMediaPlayer.setCodecFlag(KSYMediaPlayer.KSY_USE_MEDIACODEC_ALL);
        }

        try {
            ksyMediaPlayer.setDataSource(mrl);
            ksyMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mSurfaceTexture != null)
            mSurfaceTexture.release();

        mVideoTextureView = null;
        mSurfaceTexture = null;

    }
    @Override
    protected void onPause() {
        super.onPause();
        if(ksyMediaPlayer != null)
        {
            ksyMediaPlayer.pause();
            mPause = true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        reinitTextureView((TextureView) findViewById(R.id.player_surface));
        if(ksyMediaPlayer != null)
        {
            ksyMediaPlayer.start();
            mPause = false;
        }
    }
    //android <=4.4 后台切回后不会再触发onSurfaceTextureAvailable，所以在此处调用setSurfaceTexture
    public void reinitTextureView(TextureView view) {
        mVideoTextureView = view;
        mVideoTextureView.setSurfaceTextureListener(this);
        //需要检测Texture是否是available，否则在某些设备上会引起崩溃
        if (!mVideoTextureView.isAvailable()) {
            if (mSurfaceTexture != null) {
                mVideoTextureView.setSurfaceTexture(mSurfaceTexture);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            videoPlayEnd();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // Maybe we could support gesture detect
    private void dealTouchEvent(View view, MotionEvent event) {
        mPlayerPanelShow = !mPlayerPanelShow;

        if(mPlayerPanelShow) {
            mPlayerPanel.setVisibility(View.VISIBLE);

            Message msg = new Message();
            msg.what = HIDDEN_SEEKBAR;
            mHandler.sendMessageDelayed(msg, 3000);
        }else{
            mPlayerPanel.setVisibility(View.GONE);
            mHandler.removeMessages(HIDDEN_SEEKBAR);
        }
    }

    public int setVideoProgress(int currentProgress) {

        if(ksyMediaPlayer == null)
            return -1;

        long time = currentProgress > 0 ? currentProgress : ksyMediaPlayer.getCurrentPosition();
        long length = ksyMediaPlayer.getDuration();

        // Update all view elements
        mPlayerSeekbar.setMax((int)length);
        mPlayerSeekbar.setProgress((int)time);

        if(time >= 0)
        {
            String progress = Strings.millisToString(time) + "/" + Strings.millisToString(length);
            mPlayerPosition.setText(progress);
        }

        Message msg = new Message();
        msg.what = UPDATE_SEEKBAR;

        if(mHandler != null)
            mHandler.sendMessageDelayed(msg, 1000);
        return (int)time;
    }

    private void updateQosInfo(QosObject obj) {
        mCpu.setText("Cpu usage:"+obj.cpuUsage);
        mMemInfo.setText("Memory:" + obj.pss + " KB");

        if(ksyMediaPlayer != null)
        {
            long bits = ksyMediaPlayer.getDecodedDataSize() * 8 / (mPause ? mPauseStartTime - mPausedTime - mStartTime : System.currentTimeMillis() - mPausedTime - mStartTime);
            mVideoBitrate.setText("Bitrate: " + bits + " kb/s");
        }
    }

    private String md5(String string)
    {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    private void videoPlayEnd() {
        if(ksyMediaPlayer != null)
        {
            ksyMediaPlayer.release();
            ksyMediaPlayer = null;
        }

        if(mQosThread != null) {
            mQosThread.stopThread();
            mQosThread = null;
        }

        mHandler = null;

        finish();
    }

    private View.OnClickListener mStartBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPause = !mPause;
            if(mPause) {
                mPlayerStartBtn.setBackgroundResource(R.drawable.qyvideo_pause_btn);
                ksyMediaPlayer.pause();
                mPauseStartTime = System.currentTimeMillis();
            }else {
                mPlayerStartBtn.setBackgroundResource(R.drawable.qyvideo_start_btn);
                ksyMediaPlayer.start();
                mPausedTime += System.currentTimeMillis() - mPauseStartTime;
                mPauseStartTime = 0;
            }
        }
    };

    private int mVideoProgress = 0;
    private SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) {
                mVideoProgress = progress;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ksyMediaPlayer.seekTo(mVideoProgress);
            setVideoProgress(mVideoProgress);
        }
    };

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            dealTouchEvent(v, event);
            return false;
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if(mSurfaceTexture == null) {
            mSurfaceTexture = surface;
        }
        if (mSurface == null){
            mSurface = new Surface(mSurfaceTexture);
        }

        if (ksyMediaPlayer != null){
            ksyMediaPlayer.setSurface(mSurface);
        }
    };

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return  false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private class UIHandler extends Handler {

        PlayerUseTextureView mActivtiy;

        public UIHandler(PlayerUseTextureView activty) {
            mActivtiy = activty;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_SEEKBAR:
                    if(mActivtiy != null)
                        mActivtiy.setVideoProgress(0);
                    break;
                case HIDDEN_SEEKBAR:
                    if(mActivtiy != null)
                    {
                        mActivtiy.mPlayerPanelShow = false;
                        mActivtiy.mPlayerPanel.setVisibility(View.GONE);
                    }
                    break;
                case UPDATE_QOS:
                    if(mActivtiy != null && msg.obj instanceof QosObject) {
                        mActivtiy.updateQosInfo((QosObject)msg.obj);
                    }
                    break;
            }
        }
    }
}
