package com.ksyun.player.demo.ui.activity.playrelatedactivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaMeta;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.misc.KSYQosInfo;
import com.ksyun.player.demo.R;
import com.ksyun.player.demo.model.KSYFloatingPlayer;
import com.ksyun.player.demo.model.Strings;
import com.ksyun.player.demo.ui.activity.FloatingPlayingActivity;
import com.ksyun.player.demo.util.QosObject;
import com.ksyun.player.demo.util.QosThread;
import com.ksyun.player.demo.util.Settings;

import java.io.IOException;

/**
 * Created by xbc on 2017/3/13.
 */

public class FloatingVideoActivity extends Activity implements Handler.Callback {

    private static final String TAG = "FloatingVideoActivity";

    private static final int UPDATE_SEEK_BAR_STATUS = 101;
    private static final int CHANGE_PANEL_STATUS = UPDATE_SEEK_BAR_STATUS + 1;

    private SharedPreferences mSettings;

    private String chooseDecode;
    private String chooseDebug;
    private String bufferTime;
    private String bufferSize;
    private String mDataSource;

    private Handler mHandler;
    private Context mContext;
    private QosThread mQosThread;
    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private RelativeLayout mPlayerPanel;
    private RelativeLayout mQosLayout;
    private ImageView mPlayerStartBtn;
    private ImageView mFloatingPlaying;
    private SeekBar mPlayerSeekBar;
    private TextView mPlayerPosition;
    private TextView mLoadText;
    private TextView mCpu;
    private TextView mMemInfo;
    private TextView mVideoResolution;
    private TextView mVideoBitrate;
    private TextView mVideoBufferTime;
    private TextView mAudioBufferTime;
    private TextView mServerIp;
    private TextView mSdkVersion;
    private TextView mDNSTime;
    private TextView mHttpConnectionTime;
    //卡顿信息
    private TextView mBufferEmptyCnt;
    private TextView mBufferEmptyDuration;
    private TextView mDecodeFps;
    private TextView mOutputFps;

    private long mStartTime = 0;
    private long mPauseStartTime = 0;
    private long mPausedTime = 0;

    private int mVideoWidth = 0;
    private int mVideoHeight = 0;

    boolean useHwDecoder = false;

    private KSYQosInfo mQosInfo;
    private boolean mPause = false;
    private boolean mPlayerPanelShow = false;
    private boolean mPlayingCompleted = false;
    private boolean mJumpToFloatingActivity = false;

    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mVideoWidth = KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getVideoWidth();
            mVideoHeight = KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getVideoHeight();

            KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

            long videoDuration = KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getDuration();
            long currentProgress = KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getCurrentPosition();

            mPlayerSeekBar.setMax((int) videoDuration);
            mPlayerSeekBar.setProgress((int) currentProgress);

            //  get meta data
            Bundle bundle =  KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getMediaMeta();
            KSYMediaMeta meta = KSYMediaMeta.parse(bundle);
            if (meta != null) {
                if (meta.mHttpConnectTime > 0) {
                    double http_connection_time = Double.valueOf(meta.mHttpConnectTime);
                    mHttpConnectionTime.setText("HTTP Connection Time: " + (int) http_connection_time);
                }

                int dns_time = meta.mAnalyzeDnsTime;
                if (dns_time > 0) {
                    mDNSTime.setText("DNS time: " + dns_time);
                }
            }

            mSdkVersion.setText("SDK version: " +  KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getVersion());
            mServerIp.setText("ServerIP:" + KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getClientIP());

            mVideoResolution.setText("Resolution:" +  mVideoWidth + "x" + mVideoHeight);

            if (mQosThread == null) {
                mQosThread = new QosThread(mContext, mHandler);
                mQosThread.start();
            }

            if (mHandler != null)
                mHandler.obtainMessage(UPDATE_SEEK_BAR_STATUS).sendToTarget();

            KSYFloatingPlayer.getInstance().getKSYMediaPlayer().start();
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            return false;
        }
    };

    private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            return false;
        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            mPlayingCompleted = true;
            if (!mJumpToFloatingActivity)
                FloatingVideoActivity.this.finish();
        }
    };

    private View.OnClickListener mPlayerListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mPause = !mPause;
            mHandler.removeMessages(CHANGE_PANEL_STATUS);
            Message msg = new Message();
            msg.what = CHANGE_PANEL_STATUS;
            mHandler.sendMessageDelayed(msg, 3000);
            if (mPause) {
                mPlayerStartBtn.setBackgroundResource(R.drawable.ksy_pause_btn);
                KSYFloatingPlayer.getInstance().getKSYMediaPlayer().pause();
                mPauseStartTime = System.currentTimeMillis();
            } else {
                mPlayerStartBtn.setBackgroundResource(R.drawable.ksy_playing_btn);
                KSYFloatingPlayer.getInstance().getKSYMediaPlayer().start();
                mPausedTime += System.currentTimeMillis() - mPauseStartTime;
                mPauseStartTime = 0;
            }
        }
    };

    private View.OnClickListener mFloatingPlayingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mJumpToFloatingActivity = true;
            Intent intent = new Intent(FloatingVideoActivity.this, FloatingPlayingActivity.class);
            startActivity(intent);
        }
    };

    private int mVideoProgress = 0;
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mVideoProgress = seekBar.getProgress();
            if (KSYFloatingPlayer.getInstance().getKSYMediaPlayer() != null)
                KSYFloatingPlayer.getInstance().getKSYMediaPlayer().seekTo(mVideoProgress);
        }
    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            mSurfaceTexture = surfaceTexture;
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);

                if (KSYFloatingPlayer.getInstance().getKSYMediaPlayer() != null)
                    KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setSurface(mSurface);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (KSYFloatingPlayer.getInstance().getKSYMediaPlayer() != null)
                KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setSurface(null);
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
            }

            mSurfaceTexture = null;

            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private void updateQosInfoAndView(QosObject qosObject) {
        if (qosObject != null) {
            mCpu.setText("Cpu usage:" + qosObject.cpuUsage);
            mMemInfo.setText("Memory:" + qosObject.pss + " KB");
        }

        if (KSYFloatingPlayer.getInstance().getKSYMediaPlayer() != null) {
            long bits = KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getDecodedDataSize() * 8 / (mPause ? mPauseStartTime - mPausedTime - mStartTime : System.currentTimeMillis() - mPausedTime - mStartTime);
            mVideoBitrate.setText("Bitrate: " + bits + " kb/s");
            mBufferEmptyCnt.setText("BufferEmptyCount:" + KSYFloatingPlayer.getInstance().getKSYMediaPlayer().bufferEmptyCount());
            mBufferEmptyDuration.setText("BufferEmptyDuration:" + KSYFloatingPlayer.getInstance().getKSYMediaPlayer().bufferEmptyDuration());
            mDecodeFps.setText("DecodeFps:" + KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getVideoDecodeFramesPerSecond());
            mOutputFps.setText("OutputFps:" + KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getVideoOutputFramesPerSecond());

            mQosInfo = KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getStreamQosInfo();
            if (mQosInfo != null) {
                mVideoBufferTime.setText("VideoBufferTime:" + mQosInfo.videoBufferTimeLength + "(ms)");
                mAudioBufferTime.setText("AudioBufferTime:" + mQosInfo.audioBufferTimeLength + "(ms)");
            }
        }
    }

    private void displayQosView() {
        mSdkVersion.setVisibility(View.VISIBLE);
        mVideoResolution.setVisibility(View.VISIBLE);
        mVideoBitrate.setVisibility(View.VISIBLE);
        mLoadText.setVisibility(View.VISIBLE);
        mCpu.setVisibility(View.VISIBLE);
        mMemInfo.setVisibility(View.VISIBLE);
        mVideoBufferTime.setVisibility(View.VISIBLE);
        mAudioBufferTime.setVisibility(View.VISIBLE);
        mServerIp.setVisibility(View.VISIBLE);
        mDNSTime.setVisibility(View.VISIBLE);
        mHttpConnectionTime.setVisibility(View.VISIBLE);
        mBufferEmptyCnt.setVisibility(View.VISIBLE);
        mBufferEmptyDuration.setVisibility(View.VISIBLE);
        mDecodeFps.setVisibility(View.VISIBLE);
        mOutputFps.setVisibility(View.VISIBLE);
    }

    private void videoPlayEnd() {
        if (KSYFloatingPlayer.getInstance().getKSYMediaPlayer() != null) {
            KSYFloatingPlayer.getInstance().destroy();
        }

        if (mQosThread != null) {
            mQosThread.stopThread();
            mQosThread = null;
        }

        if (mHandler != null) {
            mHandler.removeMessages(UPDATE_SEEK_BAR_STATUS);
            mHandler = null;
        }
    }

    private void updateSeekBarStatus() {
        if (KSYFloatingPlayer.getInstance().getKSYMediaPlayer() == null)
            return;

        int position = (int) KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getCurrentPosition();
        long length = KSYFloatingPlayer.getInstance().getKSYMediaPlayer().getDuration();

        if (mPlayerSeekBar != null)
            mPlayerSeekBar.setProgress(position);

        if (position >= 0) {
            String progress = Strings.millisToString(position) + "/" + Strings.millisToString(length);
            mPlayerPosition.setText(progress);
        }

        if (mHandler != null) {
            Message msg = new Message();
            msg.what = UPDATE_SEEK_BAR_STATUS;

            mHandler.sendMessageDelayed(msg, 1000);
        }
    }

    private void dealTouchEvent(View view, MotionEvent event) {
        mPlayerPanelShow = !mPlayerPanelShow;

        if (mPlayerPanelShow) {
            mPlayerPanel.setVisibility(View.VISIBLE);

            Message msg = new Message();
            msg.what = CHANGE_PANEL_STATUS;
            mHandler.sendMessageDelayed(msg, 3000);
        } else {
            mPlayerPanel.setVisibility(View.GONE);
            mHandler.removeMessages(CHANGE_PANEL_STATUS);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getApplicationContext();
        mHandler = new Handler(getMainLooper(), this);

        KSYFloatingPlayer.getInstance().init(mContext);

        mDataSource = getIntent().getStringExtra("path");

        setContentView(R.layout.activity_floating);

        mQosLayout = (RelativeLayout) findViewById(R.id.player_qos);
        mPlayerPanel = (RelativeLayout) findViewById(R.id.player_panel);
        mTextureView = (TextureView) findViewById(R.id.player_view);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mPlayerStartBtn = (ImageView) findViewById(R.id.player_start);
        mFloatingPlaying = (ImageView) findViewById(R.id.floating_playing);
        mPlayerSeekBar = (SeekBar) findViewById(R.id.player_seekbar);
        mPlayerPosition = (TextView) findViewById(R.id.player_time);
        mLoadText = (TextView) findViewById(R.id.loading_text);
        mCpu = (TextView) findViewById(R.id.player_cpu);
        mMemInfo = (TextView) findViewById(R.id.player_mem);
        mVideoResolution = (TextView) findViewById(R.id.player_re);
        mVideoBitrate = (TextView) findViewById(R.id.player_br);
        mVideoBufferTime = (TextView) findViewById(R.id.player_video_time);
        mAudioBufferTime = (TextView) findViewById(R.id.player_audio_time);
        mServerIp = (TextView) findViewById(R.id.player_ip);
        mSdkVersion = (TextView) findViewById(R.id.player_sdk_version);
        mDNSTime = (TextView) findViewById(R.id.player_dns_time);
        mHttpConnectionTime = (TextView) findViewById(R.id.player_http_connection_time);
        mBufferEmptyCnt = (TextView) findViewById(R.id.player_buffer_empty_count);
        mBufferEmptyDuration = (TextView) findViewById(R.id.player_buffer_empty_duration);
        mDecodeFps = (TextView) findViewById(R.id.player_decode_fps);
        mOutputFps = (TextView) findViewById(R.id.player_output_fps);

        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dealTouchEvent(view, motionEvent);
                return false;
            }
        });

        mPlayerStartBtn.setOnClickListener(mPlayerListener);
        mFloatingPlaying.setOnClickListener(mFloatingPlayingListener);

        mPlayerSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mPlayerSeekBar.setEnabled(true);
        mPlayerSeekBar.bringToFront();

        startToPlay();
    }

    private void startToPlay() {
        KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setOnPreparedListener(mOnPreparedListener);
        KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setOnErrorListener(mOnErrorListener);
        KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setOnInfoListener(mOnInfoListener);
        KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setOnCompletionListener(mOnCompletionListener);

        mSettings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        chooseDecode = mSettings.getString("choose_decode", "undefined");
        chooseDebug = mSettings.getString("choose_debug", "undefined");
        bufferTime = mSettings.getString("buffertime", "2");
        bufferSize = mSettings.getString("buffersize", "15");

        if (!TextUtils.isEmpty(bufferTime)) {
            KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setBufferTimeMax(Integer.parseInt(bufferTime));
        }

        if (!TextUtils.isEmpty(bufferSize)) {
            KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setBufferSize(Integer.parseInt(bufferSize));
        }

        if (chooseDecode.equals(Settings.USEHARD)) {
            useHwDecoder = true;
        } else {
            useHwDecoder = false;
        }

        if (!chooseDebug.isEmpty() && chooseDebug.equals(Settings.DEBUGON)) {
            mQosLayout.setVisibility(View.VISIBLE);
            displayQosView();
        }

        if (useHwDecoder) {
            KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setDecodeMode(KSYMediaPlayer.KSYDecodeMode.KSY_DECODE_MODE_AUTO);
        }

        try {
            KSYFloatingPlayer.getInstance().getKSYMediaPlayer().setDataSource(mDataSource);
            KSYFloatingPlayer.getInstance().getKSYMediaPlayer().prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPlayingCompleted)
            this.finish();

        if (KSYFloatingPlayer.getInstance().getKSYMediaPlayer() != null)
            KSYFloatingPlayer.getInstance().getKSYMediaPlayer().start();
        else {
            KSYFloatingPlayer.getInstance().init(getApplicationContext());
            startToPlay();
        }

        if (mQosThread != null) {
            mQosThread.resume2();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mQosThread != null) {
            mQosThread.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        videoPlayEnd();
    }

    @Override
    public boolean handleMessage(Message message) {

        switch (message.what) {
            case UPDATE_SEEK_BAR_STATUS:
                updateSeekBarStatus();
                break;
            case TextureVideoActivity.UPDATE_QOSMESS:
                updateQosInfoAndView((QosObject) message.obj);
                break;
            case CHANGE_PANEL_STATUS:
                mPlayerPanel.setVisibility(View.GONE);
                break;
        }

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                FloatingVideoActivity.this.finish();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
