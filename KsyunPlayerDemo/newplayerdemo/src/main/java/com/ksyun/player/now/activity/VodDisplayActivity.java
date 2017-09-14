package com.ksyun.player.now.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYHardwareDecodeWhiteList;
import com.ksyun.media.player.KSYMediaMeta;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYMediaRecorder;
import com.ksyun.media.player.KSYTextureView;
import com.ksyun.media.player.misc.KSYQosInfo;
import com.ksyun.media.player.recorder.KSYMediaRecorderConfig;
import com.ksyun.player.now.R;
import com.ksyun.player.now.adapter.VodDiaplayAdapter;
import com.ksyun.player.now.bean.QosBean;
import com.ksyun.player.now.bean.VodBean;
import com.ksyun.player.now.model.FloatingPlayer;
import com.ksyun.player.now.model.NetState;
import com.ksyun.player.now.utils.Dialog;
import com.ksyun.player.now.utils.Display;
import com.ksyun.player.now.utils.Ids;
import com.ksyun.player.now.utils.NetStateUtil;
import com.ksyun.player.now.utils.QosInfoThread;
import com.ksyun.player.now.utils.Setting;
import com.ksyun.player.now.utils.Strings;
import com.ksyun.player.now.view.DisplayProgressBar;
import com.ksyun.player.now.view.RecordingProgressBar;
import com.ksyun.player.now.view.VideoLoadMoreListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VodDisplayActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback, RadioGroup.OnCheckedChangeListener {
    private static final int UPDATE_SEEK_BAR_STATUS = 101;
    public static final int UPDATE_QOSMESS = 102;
    public static final int UPADTE_QOSVIEW = 103;
    public static final int REMOVE_TIPS = 104;
    public static final int CAP_FINISHED = 105;
    private ImageView back, pause, full_screen;
    private TextView currentTime, totalTime;
    private RelativeLayout panel, content;
    private RelativeLayout video;
    private SeekBar mPlayerSeekBar;
    private Boolean isPause = false;
    private int video_height, video_width;
    private int full_screen_width = 0, full_screen_height = 0;
    private RelativeLayout landscape_content, portrait_content;
    private RelativeLayout landscape_top_panel, landscape_bottom_panel;
    private ImageView back_landscape, more_landscape, pause_landscape, next_landscape;
    private TextView current_time_landscape, total_time_landscape, video_name_landscape;
    private TextView clarity_landscape;
    private SeekBar seekBar_landscape;
    private ImageView screen_shot, screen_cap;
    private float startX, startY;
    private Boolean toFloatingWindow = false;
    private Boolean comeBackFromRestart = false;
    private Boolean isPalying = true;
    private long quit_time;
    private int playingId;
    private int clarityId;
    //0 横屏播放 1 竖屏播放
    private int currentState;

    //加载进度条
    private ImageView loadingProgress;

    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private RelativeLayout more_content, clarity_content, screen_cap_content;

    private VideoLoadMoreListView listView;
    private VodDiaplayAdapter adapter;
    private List<VodBean.DataBean.DetailBean> videoList;

    private Boolean isPanelShowing_Portrait = true;
    private Boolean isPanelShowing_Landscape = true;
    private Boolean mTouching;
    private Boolean isLooping;
    private Boolean isControling;
    private Context context;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    private String chooseDecode;
    private String chooseDebug;
    private int bufferTime;
    private int bufferSize;
    private int prepareTimeout;
    private int readTimeout;
    boolean useHwDecoder = false;
    private String mDataSource;
    private String playingTitle = "";
    private int leftPosition, rightPosition;
    private float centerPointX;
    private float centerPointY;
    private float lastMoveX = -1;
    private float lastMoveY = -1;
    private float movedDeltaX;
    private float movedDeltaY;
    private float totalRatio;
    private float deltaRatio;
    private double lastSpan;
    private Timer timer;
    private TimerTask timerTask;
    private Handler mHandler;
    private int mVideoProgress = 0;
    //调试信息
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
    private TextView mBufferEmptyCnt;
    private TextView mBufferEmptyDuration;
    private TextView mDecodeFps;
    private TextView mOutputFps;

    private QosInfoThread mQosThread;
    private long bits;
    private KSYQosInfo info;
    private String cpuUsage;
    private int pss;
    private long mStartTime = 0;
    private long mPauseStartTime = 0;
    private long mPausedTime = 0;

    //调试开启信息，倍数信息
    private RadioGroup debug, times;
    private RadioButton debug_on, debug_off, times_10, times_125, times_15, times_20;

    //清晰度
    private RadioGroup clarity;
    private RadioButton clarity_super, clarity_high, clarity_normal;
    private Boolean isChangingClarity = false;
    private int changing_clarity_time;
    //录屏
    private TextView cap_save, cap_delete, cap_text;
    private RecordingProgressBar cap_progress;
    private ImageView cap_pause;
    private KSYMediaRecorder mMediaRecorder;
    private int progress;
    private Boolean cap_stop = false;
    private Boolean toEnd = false;
    //声音和亮度
    private float currentBrighrness;
    private float startVol, currentVol;
    //音量和亮度调节进度条
    private DisplayProgressBar displaySeekbar_brightness, displaySeekbar_volumn;
    private RelativeLayout displayDialog_brightness, displayDialog_volumn;
    private TextView volumn_text;
    private ImageView volumn_image;
    //本地保存路径
    private File imageFile, videoFile;
    private String videoPath;
    //保存图片和小视频弹窗
    private RelativeLayout save_bitmap, save_video;
    private TextView video_log,image_log;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mVideoWidth = FloatingPlayer.getInstance().getKSYTextureView().getVideoWidth();
            mVideoHeight = FloatingPlayer.getInstance().getKSYTextureView().getVideoHeight();
            FloatingPlayer.getInstance().getKSYTextureView().setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            Log.i("buffer", "视频加载结束");
            setVideoProgress(0);
            startQosInfo();
            if (comeBackFromRestart) {
                FloatingPlayer.getInstance().getKSYTextureView().seekTo(quit_time);
                FloatingPlayer.getInstance().getKSYTextureView().start();
                if (!isPalying) {
                    FloatingPlayer.getInstance().getKSYTextureView().pause();
                }
                isPalying = true;
                comeBackFromRestart = false;
            } else if (isChangingClarity) {
                FloatingPlayer.getInstance().getKSYTextureView().seekTo(changing_clarity_time);
                FloatingPlayer.getInstance().getKSYTextureView().start();
                pause_landscape.setImageResource(R.mipmap.stop_full_screen);
                pause.setImageResource(R.mipmap.stop_full_screen);
                isChangingClarity = false;

            } else {
                FloatingPlayer.getInstance().getKSYTextureView().start();
            }
            Dialog.dismiss();

        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            Toast.makeText(VodDisplayActivity.this, "播放器遇到错误，播放已退出，错误码:"+i, Toast.LENGTH_SHORT).show();
            videoPlayEnd();
            return false;
        }
    };

    private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            switch (i) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.i("buffer", "卡顿了");
                    Dialog.show();
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Dialog.dismiss();
                    Log.i("buffer", "卡顿结束了");
                    break;
            }
            return false;
        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            toEnd = true;
            playNextVideo();
        }
    };
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (portrait_content.getVisibility() == View.VISIBLE) {
                mVideoProgress = seekBar.getProgress();
            } else {
                mVideoProgress = seekBar_landscape.getProgress();
            }
            if (FloatingPlayer.getInstance().getKSYTextureView() != null)
                FloatingPlayer.getInstance().getKSYTextureView().seekTo(mVideoProgress);
        }
    };


    private void videoPlayEnd() {
        if (FloatingPlayer.getInstance().getKSYTextureView() != null) {
            FloatingPlayer.getInstance().destroy();
        }

        if (mHandler != null) {
            mHandler.removeMessages(UPDATE_SEEK_BAR_STATUS);
            mHandler = null;
        }
        if (mQosThread != null) {
            mQosThread.stopThread();
            mQosThread = null;
        }
        editor.putBoolean("isPlaying", false);
        editor.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (landscape_content.getVisibility() == View.VISIBLE) {
                        landscape_content.setVisibility(View.GONE);
                        hideStatusBar();
                        isPanelShowing_Landscape = false;
                    }
                }
            }, 5000);
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(params);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            showLandscapePanel();
            ViewTreeObserver vto2 = content.getViewTreeObserver();
            vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    full_screen_height = content.getHeight();
                    full_screen_width = content.getWidth();
                }
            });
            currentState = 1;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(params);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            showPortraitPanel();
            Display.setLight(this, currentBrighrness, this.getWindow().getAttributes());
            full_screen_height = 0;
            full_screen_width = 0;
            currentState = 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_display);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        currentBrighrness = Display.getSystemBrightness(this);
        mHandler = new Handler(getMainLooper(), this);
        context = this.getApplicationContext();
        mSettings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        editor = mSettings.edit();
        currentState = 0;
        initData();
        initView();
        initQosInfo();
        initFile();
        if (FloatingPlayer.getInstance().getKSYTextureView() == null) {
            startToPlay();
        } else {
            resumeToPlay();
        }

    }

    private void initData() {
        videoList = (List<VodBean.DataBean.DetailBean>) getIntent().getSerializableExtra("videoList");
        playingId = getIntent().getIntExtra("playingId", 0);
        if (playingId >= 0)
            playingTitle = videoList.get(playingId).getVideoTitle();
        else
            mDataSource = getIntent().getStringExtra(Ids.PLAY_URL);
    }

    private void initView() {
        mPlayerSeekBar = (SeekBar) findViewById(R.id.vod_display_seekbar);
        back = (ImageView) findViewById(R.id.vod_display_back_portrait);
        back.setOnClickListener(this);
        pause = (ImageView) findViewById(R.id.vod_display_pause);
        pause.setOnClickListener(this);
        full_screen = (ImageView) findViewById(R.id.vod_display_full_screen);
        full_screen.setOnClickListener(this);
        currentTime = (TextView) findViewById(R.id.vod_display_current_time);
        totalTime = (TextView) findViewById(R.id.vod_display_total_time);
        panel = (RelativeLayout) findViewById(R.id.vod_controller_bar);
        panel.setOnClickListener(this);
        video = (RelativeLayout) findViewById(R.id.vod_main_video);
        listView = (VideoLoadMoreListView) findViewById(R.id.vod_display_list);
        adapter = new VodDiaplayAdapter(this, videoList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int position = i - 1;
                if (position >= 0) {
                    if (FloatingPlayer.getInstance().getKSYTextureView().getDuration() >= 0) {
                        String name = videoList.get(position).getVideoTitle();
                        listView.updateHeader(name);
                        video_name_landscape.setText(name);
                        playingId = position;
                        Ids.playingId = position;
                        changeClarityId();
                        mDataSource = videoList.get(playingId).getPlayURL().get(clarityId).trim();
                        FloatingPlayer.getInstance().getKSYTextureView().reset();
                        try {
                            FloatingPlayer.getInstance().getKSYTextureView().setDataSource(mDataSource);
                            FloatingPlayer.getInstance().getKSYTextureView().shouldAutoPlay(false);
                            FloatingPlayer.getInstance().getKSYTextureView().prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Dialog.show();
                        pause_landscape.setImageResource(R.mipmap.stop_full_screen);
                        pause.setImageResource(R.mipmap.stop_full_screen);
                    }
                }
            }
        });
        listView.updateHeader(playingTitle);
        mPlayerSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mPlayerSeekBar.setEnabled(true);
        content = (RelativeLayout) findViewById(R.id.vod_content);
        //保存横屏是播放器的高度
        ViewTreeObserver vto2 = content.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                content.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                video_height = content.getHeight();
                video_width = content.getWidth();
            }
        });

        landscape_content = (RelativeLayout) findViewById(R.id.landscape_controller);
        portrait_content = (RelativeLayout) findViewById(R.id.portrait_controller);
        landscape_top_panel = (RelativeLayout) findViewById(R.id.landscape_top_panel);
        landscape_top_panel.setOnClickListener(this);
        landscape_bottom_panel = (RelativeLayout) findViewById(R.id.landscape_bottom_panel);
        landscape_bottom_panel.setOnClickListener(this);
        back_landscape = (ImageView) findViewById(R.id.vod_display_back_landscape);
        back_landscape.setOnClickListener(this);
        more_landscape = (ImageView) findViewById(R.id.more_landscape);
        more_landscape.setOnClickListener(this);
        pause_landscape = (ImageView) findViewById(R.id.pause_landscape);
        pause_landscape.setOnClickListener(this);
        next_landscape = (ImageView) findViewById(R.id.next_landscape);
        next_landscape.setOnClickListener(this);
        screen_cap = (ImageView) findViewById(R.id.screen_cap);
        screen_cap.setOnClickListener(this);
        screen_shot = (ImageView) findViewById(R.id.screen_shot);
        screen_shot.setOnClickListener(this);
        seekBar_landscape = (SeekBar) findViewById(R.id.seekbar_landscape);
        seekBar_landscape.setOnSeekBarChangeListener(mSeekBarChangeListener);
        total_time_landscape = (TextView) findViewById(R.id.total_time_landscape);
        current_time_landscape = (TextView) findViewById(R.id.current_time_landscape);
        video_name_landscape = (TextView) findViewById(R.id.video_name_landscape);
        video_name_landscape.setText(playingTitle);
        clarity_landscape = (TextView) findViewById(R.id.clarity_landscape);
        clarity_landscape.setOnClickListener(this);

        more_content = (RelativeLayout) findViewById(R.id.content_more);
        clarity_content = (RelativeLayout) findViewById(R.id.content_clarity);
        screen_cap_content = (RelativeLayout) findViewById(R.id.screen_cap_content);

        //调试信息
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

        //更多设置
        times = (RadioGroup) findViewById(R.id.times);
        times.setOnCheckedChangeListener(this);
        debug = (RadioGroup) findViewById(R.id.debug);
        debug.setOnCheckedChangeListener(this);
        times_10 = (RadioButton) findViewById(R.id.times_1);
        times_125 = (RadioButton) findViewById(R.id.times_1_25);
        times_15 = (RadioButton) findViewById(R.id.times_1_5);
        times_20 = (RadioButton) findViewById(R.id.times_2);
        debug_off = (RadioButton) findViewById(R.id.debug_off);
        debug_on = (RadioButton) findViewById(R.id.debug_on);
        switch (mSettings.getString("debug", "")) {
            case Setting.DEBUGON:
                debug.check(debug_on.getId());
                break;
            case Setting.DEBUGOFF:
                debug.check(debug_off.getId());
                break;
        }
        if (mSettings.getFloat("times", 1.0f) == 1.0) {
            times.check(times_10.getId());
        } else if (mSettings.getFloat("times", 1.0f) == 1.25) {
            times.check(times_125.getId());
        } else if (mSettings.getFloat("times", 1.0f) == 1.5) {
            times.check(times_15.getId());
        } else if (mSettings.getFloat("times", 1.0f) == 2.0) {
            times.check(times_20.getId());
        }

        //清晰度
        clarity = (RadioGroup) findViewById(R.id.clarity);
        clarity_high = (RadioButton) findViewById(R.id.clarity_high);
        clarity_super = (RadioButton) findViewById(R.id.clarity_super);
        clarity_normal = (RadioButton) findViewById(R.id.clarity_normal);
        clarity.setOnCheckedChangeListener(this);
        changeClarityId();
        //屏幕录制
        cap_save = (TextView) findViewById(R.id.save_cap);
        cap_save.setOnClickListener(this);
        cap_delete = (TextView) findViewById(R.id.delete_cap);
        cap_delete.setOnClickListener(this);
        cap_text = (TextView) findViewById(R.id.cap_text);
        cap_pause = (ImageView) findViewById(R.id.cap_pause);
        cap_pause.setOnClickListener(this);
        cap_progress = (RecordingProgressBar) findViewById(R.id.progressBar);

        //小视频和图片保存
        save_bitmap = (RelativeLayout) findViewById(R.id.save_picture_dialog);
        save_video = (RelativeLayout) findViewById(R.id.save_video_dialog);
        video_log=(TextView)findViewById(R.id.video_log);
        image_log=(TextView)findViewById(R.id.image_log);
        video_log.setText("小视频已保存至DCIM/video");
        image_log.setText("截图已保存至DCIM/image");
        //音量亮度调节进度条
        displaySeekbar_brightness = (DisplayProgressBar) findViewById(R.id.diaplay_progress_brightness);
        displayDialog_brightness = (RelativeLayout) findViewById(R.id.display_dialog_brightness);
        displaySeekbar_volumn = (DisplayProgressBar) findViewById(R.id.diaplay_progress_volumn);
        displayDialog_volumn = (RelativeLayout) findViewById(R.id.display_dialog_volumn);
        volumn_text = (TextView) findViewById(R.id.volumn_text);
        volumn_image = (ImageView) findViewById(R.id.volumn_image);
        //进度条
        loadingProgress = (ImageView) findViewById(R.id.iv_loading);
        Dialog.init(loadingProgress);
    }

    private void startToPlay() {
        Dialog.show();
        if (playingId >= 0)
            mDataSource = videoList.get(playingId).getPlayURL().get(clarityId).trim();
        FloatingPlayer.getInstance().init(context);
        video.addView(FloatingPlayer.getInstance().getKSYTextureView());
        FloatingPlayer.getInstance().getKSYTextureView().setOnTouchListener(mTouchListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnPreparedListener(mOnPreparedListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnErrorListener(mOnErrorListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnInfoListener(mOnInfoListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnCompletionListener(mOnCompletionListener);
        FloatingPlayer.getInstance().getKSYTextureView().setVolume(1.0f, 1.0f);
        startVol = 1.0f;
        changeSettings();
        try {
            FloatingPlayer.getInstance().getKSYTextureView().setDataSource(mDataSource);
            FloatingPlayer.getInstance().getKSYTextureView().shouldAutoPlay(false);
            FloatingPlayer.getInstance().getKSYTextureView().prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.putBoolean("isPlaying", true);
        editor.commit();
    }

    private void resumeToPlay() {
        mSettings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        editor = mSettings.edit();
        changeSettings();
        video.addView(FloatingPlayer.getInstance().getKSYTextureView());
        FloatingPlayer.getInstance().getKSYTextureView().setOnTouchListener(mTouchListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnPreparedListener(mOnPreparedListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnErrorListener(mOnErrorListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnInfoListener(mOnInfoListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnCompletionListener(mOnCompletionListener);
        FloatingPlayer.getInstance().getKSYTextureView().setVisibility(View.VISIBLE);
        FloatingPlayer.getInstance().getKSYTextureView().setComeBackFromShare(true);
        setVideoProgress(0);
        editor.putBoolean("isPlaying", true);
        editor.commit();
        startQosInfo();
    }
    private void changeSettings(){
        chooseDecode = mSettings.getString("choose_decode", "undefined");
        bufferTime = mSettings.getInt("buffertime", 2);
        bufferSize = mSettings.getInt("buffersize", 15);
        prepareTimeout = mSettings.getInt("preparetimeout", 5);
        readTimeout = mSettings.getInt("readtimeout", 30);
        isLooping = mSettings.getBoolean("isLooping", false);
        if (bufferTime > 0) {
            FloatingPlayer.getInstance().getKSYTextureView().setBufferTimeMax(bufferTime);
        }

        if (bufferSize > 0) {
            FloatingPlayer.getInstance().getKSYTextureView().setBufferSize(bufferSize);
        }
        if (prepareTimeout > 0 && readTimeout > 0) {
            FloatingPlayer.getInstance().getKSYTextureView().setTimeout(prepareTimeout, readTimeout);
        }
        if (chooseDecode.equals(Setting.USEHARD)) {
            useHwDecoder = true;
        } else {
            useHwDecoder = false;
        }
        FloatingPlayer.getInstance().getKSYTextureView().setLooping(isLooping);
        if (useHwDecoder) {
            if (KSYHardwareDecodeWhiteList.getInstance().getCurrentStatus() == KSYHardwareDecodeWhiteList.KSY_STATUS_OK) {
                if (KSYHardwareDecodeWhiteList.getInstance().supportHardwareDecodeH264()
                        || KSYHardwareDecodeWhiteList.getInstance().supportHardwareDecodeH265())
                    FloatingPlayer.getInstance().getKSYTextureView().setDecodeMode(KSYMediaPlayer.KSYDecodeMode.KSY_DECODE_MODE_AUTO);
            }
        }
        FloatingPlayer.getInstance().getKSYTextureView().setSpeed(mSettings.getFloat("times", 1.0f));
        if (mSettings.getString("debug", Setting.DEBUGOFF).equals(Setting.DEBUGON)) {
            openDebug();
        } else if (mSettings.getString("debug", Setting.DEBUGOFF).equals(Setting.DEBUGOFF)) {
            closeDebug();
        }
    }
    private void playNextVideo() {
        if (playingId == 4) {
            playingId = 0;
        } else {
            playingId++;
        }
        Ids.playingId = playingId;
        changeClarityId();
        String name = videoList.get(playingId).getVideoTitle();
        listView.updateHeader(name);
        video_name_landscape.setText(name);
        mDataSource = videoList.get(playingId).getPlayURL().get(clarityId).trim();
        FloatingPlayer.getInstance().getKSYTextureView().reset();
        try {
            FloatingPlayer.getInstance().getKSYTextureView().setDataSource(mDataSource);
            FloatingPlayer.getInstance().getKSYTextureView().shouldAutoPlay(false);
            FloatingPlayer.getInstance().getKSYTextureView().prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Dialog.show();
        pause_landscape.setImageResource(R.mipmap.stop_full_screen);
        pause.setImageResource(R.mipmap.stop_full_screen);
        changeSettings();
    }

    //选择不同码率的视频源
    private void changeClarityId() {
        if (playingId==1||playingId==3||playingId==4) {
            clarity_super.setVisibility(View.GONE);
            if (mSettings.getString("clarity", Setting.CLARITY_HIGH).equals(Setting.CLARITY_SUPER)) {
                editor.putString("clarity", Setting.CLARITY_HIGH);
                editor.commit();
            }
        } else {
            clarity_super.setVisibility(View.VISIBLE);
        }
        switch (mSettings.getString("clarity", Setting.CLARITY_HIGH)) {
            case Setting.CLARITY_NORMAL:
                clarityId = 2;
                clarity.check(clarity_normal.getId());
                clarity_landscape.setText("标清");
                if(playingId==1||playingId==3||playingId==4){
                    clarityId--;
                }
                break;
            case Setting.CLARITY_HIGH:
                clarityId = 1;
                clarity.check(clarity_high.getId());
                clarity_landscape.setText("高清");
                if(playingId==1||playingId==3||playingId==4){
                    clarityId--;
                }
                break;
            case Setting.CLARITY_SUPER:
                clarityId = 0;
                clarity.check(clarity_super.getId());
                clarity_landscape.setText("超清");
                break;
        }
    }

    //改变清晰度
    private void changeClarity() {
        isChangingClarity = true;
        changeClarityId();
        if (FloatingPlayer.getInstance().getKSYTextureView() != null) {
            mDataSource = videoList.get(playingId).getPlayURL().get(clarityId).trim();
            editor.commit();
            changing_clarity_time = seekBar_landscape.getProgress();
            FloatingPlayer.getInstance().getKSYTextureView().softReset();
            changeSettings();
            try {
                FloatingPlayer.getInstance().getKSYTextureView().setDataSource(mDataSource);
                FloatingPlayer.getInstance().getKSYTextureView().shouldAutoPlay(false);
                FloatingPlayer.getInstance().getKSYTextureView().prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Dialog.show();
        }
    }

    private void initQosInfo() {
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        Message message = new Message();
                        message.what = VodDisplayActivity.UPADTE_QOSVIEW;
                        if (mHandler != null && message != null) {
                            mHandler.sendMessage(message);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                }
            };
        }

        if (timer == null) {
            timer = new Timer(true);
        }

        timer.schedule(timerTask, 2000, 5000);

        mQosThread = new QosInfoThread(context, mHandler);
    }

    private void initFile() {
        videoFile = new File(Environment.getExternalStorageDirectory(), "DCIM/video");
        imageFile = new File(Environment.getExternalStorageDirectory(), "DCIM/image");
        if (!videoFile.exists()) {
            videoFile.mkdir();
        }
        if (!imageFile.exists()) {
            imageFile.mkdir();
        }
    }

    @Override
    public boolean handleMessage(Message message) {

        switch (message.what) {
            case UPDATE_SEEK_BAR_STATUS:
                setVideoProgress(0);
                break;
            case UPDATE_QOSMESS:
                if (message.obj instanceof QosBean) {
                    updateQosInfo((QosBean) message.obj);
                }
                break;
            case UPADTE_QOSVIEW:
                updateQosView();
                break;
            case REMOVE_TIPS:
                cap_text.setVisibility(View.GONE);
                break;
            case CAP_FINISHED:
                cap_save.setVisibility(View.VISIBLE);
                break;
        }

        return false;
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            KSYTextureView mVideoView = FloatingPlayer.getInstance().getKSYTextureView();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mTouching = false;
                    startX = event.getX();
                    startY = event.getY();
                    leftPosition = full_screen_width / 3;
                    rightPosition = leftPosition * 2;
                    isControling = (startX < leftPosition || startX > rightPosition) && (full_screen_width > video_width);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mTouching = true;
                    if (event.getPointerCount() == 2) {
                        lastSpan = getCurrentSpan(event);
                        centerPointX = getFocusX(event);
                        centerPointY = getFocusY(event);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 1) {
                        float posX = event.getX();
                        float posY = event.getY();
                        if (lastMoveX == -1 && lastMoveX == -1) {
                            lastMoveX = posX;
                            lastMoveY = posY;
                        }
                        movedDeltaX = posX - lastMoveX;
                        movedDeltaY = posY - lastMoveY;

                        if (Math.abs(movedDeltaX) > 5 || Math.abs(movedDeltaY) > 5) {
                            //判断调节音量和亮度 还是缩放画面
                            if (isControling && (Math.abs(movedDeltaY) > 2 * Math.abs(movedDeltaX))) {

                                if (startX < leftPosition) {
                                    //调节亮度
                                    float position = Display.changeLight(VodDisplayActivity.this, full_screen_height, (startY - posY), getWindow().getAttributes());
                                    displaySeekbar_brightness.setMax(16);
                                    displaySeekbar_brightness.setProgress((int) (position * 16));
                                    displayDialog_brightness.setVisibility(View.VISIBLE);
                                } else if (startX > rightPosition) {
                                    //调节音量
                                    currentVol = startVol + (startY - posY) / (full_screen_height / 8);
                                    if (currentVol <= 0) {
                                        currentVol = 0;
                                        volumn_text.setText("静音");
                                        volumn_image.setImageResource(R.mipmap.novolumn);
                                    } else {
                                        volumn_text.setText("音量");
                                        volumn_image.setImageResource(R.mipmap.volumn1);
                                    }
                                    if (currentVol >= 2.0f) {
                                        currentVol = 2.0f;
                                    }
                                    displaySeekbar_volumn.setMax(16);
                                    displaySeekbar_volumn.setProgress((int) (currentVol * 8));
                                    displayDialog_volumn.setVisibility(View.VISIBLE);
                                    mVideoView.setVolume(currentVol, currentVol);
                                }
                            } else {
                                if (mVideoView != null) {
                                    mVideoView.moveVideo(movedDeltaX, movedDeltaY);
                                }
                            }
                            mTouching = true;
                        }
                        lastMoveX = posX;
                        lastMoveY = posY;

                    } else if (event.getPointerCount() == 2) {
                        double spans = getCurrentSpan(event);
                        if (spans > 5) {
                            deltaRatio = (float) (spans / lastSpan);
                            totalRatio = mVideoView.getVideoScaleRatio() * deltaRatio;
                            if (mVideoView != null) {
                                mVideoView.setVideoScaleRatio(totalRatio, centerPointX, centerPointY);
                            }
                            lastSpan = spans;
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    if (event.getPointerCount() == 2) {
                        lastMoveX = -1;
                        lastMoveY = -1;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    lastMoveX = -1;
                    lastMoveY = -1;
                    Display.getCurrentLight();
                    if (displayDialog_brightness.getVisibility() == View.VISIBLE) {
                        displayDialog_brightness.setVisibility(View.GONE);
                    }
                    if (displayDialog_volumn.getVisibility() == View.VISIBLE) {
                        displayDialog_volumn.setVisibility(View.GONE);
                    }
                    startVol = currentVol;
                    if (!mTouching) {
                        dealTouchEvent();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private double getCurrentSpan(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return Math.sqrt(disX * disX + disY * disY);
    }

    private float getFocusX(MotionEvent event) {
        float xPoint0 = event.getX(0);
        float xPoint1 = event.getX(1);
        return (xPoint0 + xPoint1) / 2;
    }

    private float getFocusY(MotionEvent event) {
        float yPoint0 = event.getY(0);
        float yPoint1 = event.getY(1);
        return (yPoint0 + yPoint1) / 2;
    }

    private void dealTouchEvent() {
        if (portrait_content.getVisibility() == View.VISIBLE) {
            if (isPanelShowing_Portrait) {
                panel.setVisibility(View.GONE);
            } else {
                panel.setVisibility(View.VISIBLE);
            }
        } else {
            hideShade();
            showLandscapePanel();
            if (isPanelShowing_Landscape) {
                landscape_content.setVisibility(View.GONE);
                hideStatusBar();
            } else {
                landscape_content.setVisibility(View.VISIBLE);
                showStatusBar();
            }
        }
        isPanelShowing_Landscape = !isPanelShowing_Landscape;
        isPanelShowing_Portrait = !isPanelShowing_Portrait;
    }

    public int setVideoProgress(int currentProgress) {

        if (FloatingPlayer.getInstance().getKSYTextureView() == null)
            return -1;

        long time = currentProgress > 0 ? currentProgress : FloatingPlayer.getInstance().getKSYTextureView().getCurrentPosition();
        long length = FloatingPlayer.getInstance().getKSYTextureView().getDuration();

        // Update all view elements
        if (portrait_content.getVisibility() == View.VISIBLE) {
            mPlayerSeekBar.setMax((int) length);
            mPlayerSeekBar.setProgress((int) time);
        } else {
            seekBar_landscape.setMax((int) length);
            seekBar_landscape.setProgress((int) time);
        }

        if (time >= 0) {
            if (portrait_content.getVisibility() == View.VISIBLE) {
                currentTime.setText(Strings.millisToString(time));
                totalTime.setText(Strings.millisToString(length));
            } else {
                current_time_landscape.setText(Strings.millisToString(time));
                total_time_landscape.setText(Strings.millisToString(length));
            }
        }

        Message msg = new Message();
        msg.what = UPDATE_SEEK_BAR_STATUS;

        if (mHandler != null)
            mHandler.sendMessageDelayed(msg, 1000);
        return (int) time;
    }

    private void startQosInfo() {
        KSYTextureView mVideoView = FloatingPlayer.getInstance().getKSYTextureView();
        if (mQosThread != null && !mQosThread.isAlive())
            mQosThread.start();


        if (mVideoView.getServerAddress() != null)
            mServerIp.setText("ServerIP: " + mVideoView.getServerAddress());

        //  get meta data
        Bundle bundle = mVideoView.getMediaMeta();
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

        mSdkVersion.setText("SDK version: " + mVideoView.getVersion());

        mVideoResolution.setText("Resolution:" + mVideoView.getVideoWidth() + "x" + mVideoView.getVideoHeight());

        mStartTime = System.currentTimeMillis();
        chooseDebug = mSettings.getString("debug", "debugoff");
        if (chooseDebug.isEmpty() || chooseDebug.equals(Setting.DEBUGOFF)) {
            closeDebug();
        } else {
            openDebug();
        }
    }

    private void updateQosInfo(QosBean obj) {
        cpuUsage = obj.cpuUsage;
        pss = obj.pss;


        if (FloatingPlayer.getInstance().getKSYTextureView() != null) {
            bits = FloatingPlayer.getInstance().getKSYTextureView().getDecodedDataSize() * 8 / (isPause ? mPauseStartTime - mPausedTime - mStartTime : System.currentTimeMillis() - mPausedTime - mStartTime);

            info = FloatingPlayer.getInstance().getKSYTextureView().getStreamQosInfo();

        }
    }

    private void updateQosView() {
        mCpu.setText("Cpu usage:" + cpuUsage);
        mMemInfo.setText("Memory:" + pss + " KB");

        if (FloatingPlayer.getInstance().getKSYTextureView() != null) {
            mVideoBitrate.setText("Bitrate: " + bits + " kb/s");

            mBufferEmptyCnt.setText("BufferEmptyCount:" + FloatingPlayer.getInstance().getKSYTextureView().bufferEmptyCount());
            mBufferEmptyDuration.setText("BufferEmptyDuration:" + FloatingPlayer.getInstance().getKSYTextureView().bufferEmptyDuration());
            mDecodeFps.setText("DecodeFps:" + FloatingPlayer.getInstance().getKSYTextureView().getVideoDecodeFramesPerSecond());
            mOutputFps.setText("OutputFps:" + FloatingPlayer.getInstance().getKSYTextureView().getVideoOutputFramesPerSecond());
            if (info != null) {
                mVideoBufferTime.setText("VideoBufferTime:" + info.videoBufferTimeLength + "(ms)");
                mAudioBufferTime.setText("AudioBufferTime:" + info.audioBufferTimeLength + "(ms)");
            }
        }
    }

    private void openDebug() {
        mSdkVersion.setVisibility(View.VISIBLE);
        mVideoResolution.setVisibility(View.VISIBLE);
        mVideoBitrate.setVisibility(View.VISIBLE);
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

    private void closeDebug() {
        mSdkVersion.setVisibility(View.GONE);
        mVideoResolution.setVisibility(View.GONE);
        mVideoBitrate.setVisibility(View.GONE);
        mCpu.setVisibility(View.GONE);
        mMemInfo.setVisibility(View.GONE);
        mVideoBufferTime.setVisibility(View.GONE);
        mAudioBufferTime.setVisibility(View.GONE);
        mServerIp.setVisibility(View.GONE);
        mDNSTime.setVisibility(View.GONE);
        mHttpConnectionTime.setVisibility(View.GONE);
        mBufferEmptyCnt.setVisibility(View.GONE);
        mBufferEmptyDuration.setVisibility(View.GONE);
        mDecodeFps.setVisibility(View.GONE);
        mOutputFps.setVisibility(View.GONE);
    }

    private void showLandscapePanel() {
        showStatusBar();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        content.setLayoutParams(layoutParams);
        portrait_content.setVisibility(View.GONE);
        landscape_content.setVisibility(View.VISIBLE);
    }

    private void hideLandscapePanel() {
        hideStatusBar();
        landscape_content.setVisibility(View.GONE);
        isPanelShowing_Landscape = false;
    }

    private void showPortraitPanel() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, video_height);
        content.setLayoutParams(layoutParams);
        landscape_content.setVisibility(View.GONE);
        portrait_content.setVisibility(View.VISIBLE);
    }

    private void showStatusBar() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(params);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void hideStatusBar() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(params);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void hideShade() {
        more_content.setVisibility(View.GONE);
        clarity_content.setVisibility(View.GONE);
        screen_cap_content.setVisibility(View.GONE);
    }

    private void saveBitmap() {
        String imageName = System.currentTimeMillis() + ".jpg";
        File file = new File(imageFile, imageName);
        FileOutputStream outputStream;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Bitmap bitmap = FloatingPlayer.getInstance().getKSYTextureView().getScreenShot();
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            //图片保存到相册
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), imageName, null);
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        save_bitmap.setVisibility(View.VISIBLE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                save_bitmap.setVisibility(View.GONE);
            }
        }, 2000);

    }

    private void saveVideo() {
        cap_text.setVisibility(View.VISIBLE);
        cap_save.setVisibility(View.GONE);
        cap_progress.setProgress(0);
        cap_stop = false;
        progress = 0;
        KSYMediaRecorderConfig recorderConfig = new KSYMediaRecorderConfig();
        String videoName = System.currentTimeMillis() + ".mp4";
        File file = new File(videoFile, videoName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String outputPath = videoFile.getAbsolutePath() + "/" + videoName;
        videoPath = outputPath;
        recorderConfig.setVideoBitrate(800 * 1000); //码率设置为 800kbps
        recorderConfig.setKeyFrameIntervalSecond(3); //关键帧间隔为 3s
        recorderConfig.setAudioBitrate(64 * 1000); // 音频编码码率设置为 64kbps

        mMediaRecorder = new KSYMediaRecorder(recorderConfig, outputPath);
        try {
            mMediaRecorder.init(FloatingPlayer.getInstance().getKSYTextureView().getMediaPlayer()); // 初始化
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder.start(); // 开始录制
        final Timer cap_timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isLooping) {
                    toEnd = FloatingPlayer.getInstance().getKSYTextureView().getCurrentPosition() + 1000 >= FloatingPlayer.getInstance().getKSYTextureView().getDuration();
                }
                if ((!cap_stop) && (progress <= 15000) && !toEnd) {
                    progress += 10;
                    cap_progress.setProgress(progress);
                    if (progress > 3000) {
                        Message message = new Message();
                        message.what = REMOVE_TIPS;
                        mHandler.sendMessageAtTime(message, 0);
                    }
                } else {
                    mMediaRecorder.stop();
                    if (progress >= 3000) {
                        Message message = new Message();
                        message.what = CAP_FINISHED;
                        mHandler.sendMessageAtTime(message, 0);
                    }
                    toEnd = false;
                    cap_timer.cancel();
                }
            }
        };
        cap_timer.schedule(timerTask, 0, 10);

    }

    private void deleteVideo() {
        File file = new File(videoPath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.vod_display_back_portrait) {
            toFloatingWindow = true;
            finish();
        } else if (id == R.id.vod_display_back_landscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (id == R.id.vod_display_pause || id == R.id.pause_landscape) {
            if (isPause) {
                pause.setImageResource(R.mipmap.stop_full_screen);
                pause_landscape.setImageResource(R.mipmap.stop_full_screen);
                FloatingPlayer.getInstance().getKSYTextureView().start();
                editor.putBoolean("isPlaying", true);
            } else {
                pause.setImageResource(R.mipmap.start);
                pause_landscape.setImageResource(R.mipmap.start);
                FloatingPlayer.getInstance().getKSYTextureView().pause();
                editor.putBoolean("isPlaying", false);
                if (loadingProgress.getVisibility() == View.VISIBLE) {
                    Dialog.dismiss();
                }
            }
            editor.commit();
            isPause = !isPause;
        } else if (id == R.id.vod_display_full_screen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (id == R.id.next_landscape) {
            playNextVideo();
        } else if (id == R.id.more_landscape) {
            hideLandscapePanel();
            more_content.setVisibility(View.VISIBLE);
        } else if (id == R.id.clarity_landscape) {
            hideLandscapePanel();
            clarity_content.setVisibility(View.VISIBLE);
        } else if (id == R.id.screen_shot) {
            saveBitmap();
        } else if (id == R.id.screen_cap) {
            if (useHwDecoder) {
                Toast.makeText(this, "录制视频请切换至软解", Toast.LENGTH_LONG).show();
            } else if(!mSettings.getString("clarity", Setting.CLARITY_HIGH).equals(Setting.CLARITY_NORMAL)){
                Toast.makeText(this, "当前视频清晰度过高，请切换至标清", Toast.LENGTH_LONG).show();
            }
            else{
                hideLandscapePanel();
                showStatusBar();
                screen_cap_content.setVisibility(View.VISIBLE);
                saveVideo();
                screen_cap_content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        } else if (id == R.id.vod_controller_bar || id == R.id.landscape_bottom_panel || id == R.id.landscape_top_panel) {
            return;
        } else if (id == R.id.cap_pause) {
            cap_stop = true;
        } else if (id == R.id.delete_cap) {
            cap_stop = true;
            landscape_content.setVisibility(View.VISIBLE);
            deleteVideo();
            screen_cap_content.setVisibility(View.GONE);
        } else if (id == R.id.save_cap) {
            save_video.setVisibility(View.VISIBLE);
            File file = new File(videoPath);
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    save_video.setVisibility(View.GONE);
                    screen_cap_content.setVisibility(View.GONE);
                    landscape_content.setVisibility(View.VISIBLE);
                }
            }, 2000);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        //倍数
        if (i == R.id.times_1) {
            editor.putFloat("times", 1.0f);
        } else if (i == R.id.times_1_25) {
            editor.putFloat("times", 1.25f);
        } else if (i == R.id.times_1_5) {
            editor.putFloat("times", 1.5f);
        } else if (i == R.id.times_2) {
            editor.putFloat("times", 2.0f);
        }
        //调试信息是否开启
        if (i == R.id.debug_on) {
            editor.putString("debug", Setting.DEBUGON);
            openDebug();
        } else if (i == R.id.debug_off) {
            editor.putString("debug", Setting.DEBUGOFF);
            closeDebug();
        }
        //清晰度
        if (i == R.id.clarity_normal) {
            editor.putString("clarity", Setting.CLARITY_NORMAL);
            clarity_landscape.setText("标清");
        } else if (i == R.id.clarity_high) {
            editor.putString("clarity", Setting.CLARITY_HIGH);
            clarity_landscape.setText("高清");
        } else if (i == R.id.clarity_super) {
            editor.putString("clarity", Setting.CLARITY_SUPER);
            clarity_landscape.setText("超清");
        }
        editor.commit();
        if (clarity_content.getVisibility() == View.VISIBLE) {
            if (playingId >= 0)
                changeClarity();
        }
        if (FloatingPlayer.getInstance().getKSYTextureView() != null) {
            FloatingPlayer.getInstance().getKSYTextureView().setSpeed(mSettings.getFloat("times", 1.0f));
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideShade();
            }
        }, 1000);
    }
    private NetStateUtil.NetChangeListener netChangeListener = new NetStateUtil.NetChangeListener() {
        @Override
        public void onNetStateChange(int netWorkState) {
            switch (netWorkState) {
                case NetState.NETWORK_MOBILE:
                    break;
                case NetState.NETWORK_WIFI:
                    break;
                case NetState.NETWORK_NONE:
                    Toast.makeText(VodDisplayActivity.this, "没有监测到网络,请检查网络连接", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public void onBackPressed() {
        if (currentState == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            toFloatingWindow = true;
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        comeBackFromRestart = true;
        startToPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mQosThread != null) {
            mQosThread.resume2();
        }
        NetStateUtil.registerNetState(this, netChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (toFloatingWindow) {
            video.removeView(FloatingPlayer.getInstance().getKSYTextureView());
            FloatingPlayer.getInstance().getKSYTextureView().setOnTouchListener(null);
            FloatingPlayer.getInstance().getKSYTextureView().setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            if (!FloatingPlayer.getInstance().getKSYTextureView().isPlaying() && toFloatingWindow) {
                videoPlayEnd();
            }
        } else {
            if (FloatingPlayer.getInstance().getKSYTextureView().isPlaying()) {
                isPalying = true;
            } else {
                isPalying = false;
            }
            quit_time = FloatingPlayer.getInstance().getKSYTextureView().getCurrentPosition();
            FloatingPlayer.getInstance().getKSYTextureView().pause();
        }
        if (mQosThread != null) {
            mQosThread.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetStateUtil.unregisterNetState(this);
        if (mQosThread != null) {
            mQosThread.stopThread();
            try {
                mQosThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mQosThread = null;
    }
}
