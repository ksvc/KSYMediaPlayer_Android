package com.ksyun.player.now.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYHardwareDecodeWhiteList;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYMediaRecorder;
import com.ksyun.media.player.KSYTextureView;
import com.ksyun.media.player.recorder.KSYMediaRecorderConfig;
import com.ksyun.player.now.R;
import com.ksyun.player.now.bean.LiveBean;
import com.ksyun.player.now.model.FloatingPlayer;
import com.ksyun.player.now.model.NetState;
import com.ksyun.player.now.utils.Dialog;
import com.ksyun.player.now.utils.Ids;
import com.ksyun.player.now.utils.NetStateUtil;
import com.ksyun.player.now.utils.Setting;
import com.ksyun.player.now.view.HeartLayout;
import com.ksyun.player.now.view.RecordingProgressBar;
import com.ksyun.player.now.view.VerticalSeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LiveDisplayActivity extends AppCompatActivity implements Handler.Callback, View.OnClickListener {
    public static final int REMOVE_TIPS = 104;
    public static final int CAP_FINISHED = 105;
    public static final int DELETE_CAP = 106;
    private Handler mHandler;
    private Context context;
    private int playingId;
    private String playingTitle = "";
    private List<LiveBean.DataBean.DetailBean> videoList;
    private RelativeLayout video;
    private RelativeLayout content;
    private ImageView back;
    private ImageView screen_shot, screen_cap;
    private ImageView mirror, rotation;
    private ImageView like, volumn;
    private TextView title;

    private Boolean toFloatingWindow = false;
    //音量设置
    private float startVol;
    //设置信息
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    private String chooseDecode;
    private int bufferTime;
    private int bufferSize;
    private int prepareTimeout;
    private int readTimeout;
    boolean useHwDecoder = false;
    private String url;

    //视频缩放设置
    private Boolean mTouching;
    private float centerPointX;
    private float centerPointY;
    private float lastMoveX = -1;
    private float lastMoveY = -1;
    private float movedDeltaX;
    private float movedDeltaY;
    private float totalRatio;
    private float deltaRatio;
    private double lastSpan;
    //视频加载进度条
    private ImageView loadingProgress;
    //录屏
    private TextView cap_save, cap_delete, cap_text;
    private RecordingProgressBar cap_progress;
    private ImageView cap_pause;
    private KSYMediaRecorder mMediaRecorder;
    private int progress;
    private Boolean cap_stop = false;
    private RelativeLayout screen_cap_content;
    //保存图片和小视频弹窗
    private RelativeLayout save_bitmap, save_video;
    private TextView video_log,image_log;
    //图片截取视频截取
    private File videoFile, imageFile;
    private String videoPath;
    //视频镜像
    private Boolean isMirror = false;
    //视频旋转
    private int degree = 0;
    //音量
    private VerticalSeekBar verticalSeekBar;
    //点赞
    private HeartLayout heartLayout;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            FloatingPlayer.getInstance().getKSYTextureView().setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            FloatingPlayer.getInstance().getKSYTextureView().start();
            Dialog.dismiss();
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            Toast.makeText(LiveDisplayActivity.this, "播放器遇到错误，播放已退出，错误码:"+i, Toast.LENGTH_SHORT).show();
            videoPlayEnd();
            return false;
        }
    };

    private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            switch (i) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Dialog.show();
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Dialog.dismiss();
                    break;
            }
            return false;
        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        playingId=getIntent().getIntExtra("playingId",0);
        videoList=(List<LiveBean.DataBean.DetailBean>)getIntent().getSerializableExtra("videoList");
        if (playingId >= 0) {
            url = videoList.get(playingId).getPlayURL().get(0);
            playingTitle = videoList.get(playingId).getVideoTitle();
        } else {
            url = getIntent().getStringExtra(Ids.PLAY_URL);
        }
        setContentView(R.layout.activity_live_display_portrait);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (playingId==1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.activity_live_display_landscape);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.activity_live_display_portrait);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        mHandler = new Handler(getMainLooper(), this);
        context = this.getApplicationContext();
        mSettings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        editor = mSettings.edit();
        initView();
        initFile();
        if (FloatingPlayer.getInstance().getKSYTextureView() == null) {
            startToPlay();
        } else {
            resumeToPlay();
        }
    }

    private void initView() {
        video = (RelativeLayout) findViewById(R.id.live_video);
        content = (RelativeLayout) findViewById(R.id.content);
        back = (ImageView) findViewById(R.id.live_back);
        back.setOnClickListener(this);
        screen_cap = (ImageView) findViewById(R.id.screen_cap);
        screen_cap.setOnClickListener(this);
        screen_shot = (ImageView) findViewById(R.id.screen_shot);
        screen_shot.setOnClickListener(this);
        mirror = (ImageView) findViewById(R.id.mirror);
        mirror.setOnClickListener(this);
        rotation = (ImageView) findViewById(R.id.rotation);
        rotation.setOnClickListener(this);
        volumn = (ImageView) findViewById(R.id.volumn);
        volumn.setOnClickListener(this);
        like = (ImageView) findViewById(R.id.like);
        like.setOnClickListener(this);
        title = (TextView) findViewById(R.id.live_title);
        title.setText(playingTitle);
        //屏幕录制
        cap_save = (TextView) findViewById(R.id.save_cap);
        cap_save.setOnClickListener(this);
        cap_delete = (TextView) findViewById(R.id.delete_cap);
        cap_delete.setOnClickListener(this);
        cap_text = (TextView) findViewById(R.id.cap_text);
        cap_pause = (ImageView) findViewById(R.id.cap_pause);
        cap_pause.setOnClickListener(this);
        cap_progress = (RecordingProgressBar) findViewById(R.id.progressBar);
        screen_cap_content = (RelativeLayout) findViewById(R.id.screen_cap_content);
        //小视频和图片保存
        save_bitmap = (RelativeLayout) findViewById(R.id.save_picture_dialog);
        save_video = (RelativeLayout) findViewById(R.id.save_video_dialog);
        video_log=(TextView)findViewById(R.id.video_log);
        image_log=(TextView)findViewById(R.id.image_log);
        video_log.setText("小视频已保存至DCIM/video");
        image_log.setText("截图已保存至DCIM/image");
        //音量
        verticalSeekBar = (VerticalSeekBar) findViewById(R.id.volumn_seek_bar);
        verticalSeekBar.setVisibility(View.GONE);
        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FloatingPlayer.getInstance().getKSYTextureView().setVolume((float) i / 100, (float) i / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //点赞
        heartLayout = (HeartLayout) findViewById(R.id.heart_layout);
        //视频加载进度条
        loadingProgress=(ImageView)findViewById(R.id.iv_loading);
        Dialog.init(loadingProgress);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                verticalSeekBar.setVisibility(View.GONE);
                if (content.getVisibility() == View.VISIBLE) {
                    content.setVisibility(View.GONE);
                    hideStatusBar();
                }
            }
        },5000);
    }

    private void startToPlay() {
        Log.i("buffer", "视频加载开始了");
        Dialog.show();
        FloatingPlayer.getInstance().init(context);
        video.addView(FloatingPlayer.getInstance().getKSYTextureView());
        FloatingPlayer.getInstance().getKSYTextureView().setOnTouchListener(mTouchListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnPreparedListener(mOnPreparedListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnErrorListener(mOnErrorListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnInfoListener(mOnInfoListener);
        FloatingPlayer.getInstance().getKSYTextureView().setOnCompletionListener(mOnCompletionListener);
        FloatingPlayer.getInstance().getKSYTextureView().setVolume(1.0f, 1.0f);
        startVol = 1.0f;
        chooseDecode = mSettings.getString("choose_decode", "undefined");
        bufferTime = mSettings.getInt("buffertime", 2);
        bufferSize = mSettings.getInt("buffersize", 15);
        prepareTimeout = mSettings.getInt("preparetimeout", 5);
        readTimeout = mSettings.getInt("readtimeout", 30);
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
        if (useHwDecoder) {
            if (KSYHardwareDecodeWhiteList.getInstance().getCurrentStatus() == KSYHardwareDecodeWhiteList.KSY_STATUS_OK) {
                if (KSYHardwareDecodeWhiteList.getInstance().supportHardwareDecodeH264()
                        || KSYHardwareDecodeWhiteList.getInstance().supportHardwareDecodeH265())
                    FloatingPlayer.getInstance().getKSYTextureView().setDecodeMode(KSYMediaPlayer.KSYDecodeMode.KSY_DECODE_MODE_AUTO);
            }
        }

        try {
            FloatingPlayer.getInstance().getKSYTextureView().setDataSource(url);
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
        video.addView(FloatingPlayer.getInstance().getKSYTextureView());
        FloatingPlayer.getInstance().getKSYTextureView().setVisibility(View.VISIBLE);
        FloatingPlayer.getInstance().getKSYTextureView().setComeBackFromShare(true);
        editor.putBoolean("isPlaying", true);
        editor.commit();
        FloatingPlayer.getInstance().getKSYTextureView().setOnTouchListener(mTouchListener);
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

    private void saveBitmap() {
        String imageName = System.currentTimeMillis() + ".jpg";
        File file = new File(imageFile, imageName);
        FileOutputStream outputStream;
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
        cap_stop = false;
        progress = 0;
        cap_text.setVisibility(View.VISIBLE);
        cap_save.setVisibility(View.GONE);
        cap_progress.setProgress(0);
        KSYMediaRecorderConfig recorderConfig = new KSYMediaRecorderConfig();
        String videoName = System.currentTimeMillis() + ".mp4";
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
                if ((!cap_stop) && (progress <= 15000)) {
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

    private void videoPlayEnd() {
        if (FloatingPlayer.getInstance().getKSYTextureView() != null) {
            FloatingPlayer.getInstance().destroy();
        }

        editor.putBoolean("isPlaying", false);
        editor.commit();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case REMOVE_TIPS:
                cap_text.setVisibility(View.GONE);
                break;
            case CAP_FINISHED:
                cap_save.setVisibility(View.VISIBLE);
                break;
            case DELETE_CAP:
                content.setVisibility(View.VISIBLE);
                deleteVideo();
                screen_cap_content.setVisibility(View.GONE);
                break;
        }
        return true;
    }
    //事件监听
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            KSYTextureView mVideoView = FloatingPlayer.getInstance().getKSYTextureView();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mTouching = false;
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
                            if (mVideoView != null) {
                                mVideoView.moveVideo(movedDeltaX, movedDeltaY);
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
        verticalSeekBar.setVisibility(View.GONE);
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
            hideStatusBar();
        } else if (content.getVisibility() == View.GONE) {
            content.setVisibility(View.VISIBLE);
            showStatusBar();
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        toFloatingWindow = true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.live_back) {
            toFloatingWindow = true;
            finish();
        } else if (id == R.id.rotation) {
            degree += 90;
            if (degree > 270)
                degree = 0;
            FloatingPlayer.getInstance().getKSYTextureView().setRotateDegree(degree);
        } else if (id == R.id.mirror) {
            isMirror = !isMirror;
            FloatingPlayer.getInstance().getKSYTextureView().setMirror(isMirror);
        } else if (id == R.id.screen_shot) {
            saveBitmap();
        } else if (id == R.id.screen_cap) {
            if(useHwDecoder){
                Toast.makeText(this,"录制视频请切换至软解",Toast.LENGTH_LONG).show();
            } else{
                content.setVisibility(View.GONE);
                screen_cap_content.setVisibility(View.VISIBLE);
                saveVideo();
                screen_cap_content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        } else if (id == R.id.volumn) {
            if (verticalSeekBar.getVisibility() == View.VISIBLE) {
                verticalSeekBar.setVisibility(View.GONE);
            } else if (verticalSeekBar.getVisibility() == View.GONE) {
                verticalSeekBar.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.like) {
            heartLayout.addFavor();
        } else if (id == R.id.vod_controller_bar || id == R.id.landscape_bottom_panel || id == R.id.landscape_top_panel) {
            return;
        } else if (id == R.id.cap_pause) {
            cap_stop = true;
        } else if (id == R.id.delete_cap) {
            cap_stop = true;
            Message message = new Message();
            message.what = DELETE_CAP;
            mHandler.sendMessageAtTime(message, 0);
        } else if (id == R.id.save_cap) {
            save_video.setVisibility(View.VISIBLE);
            File file = new File(videoPath);
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    save_video.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                    screen_cap_content.setVisibility(View.GONE);
                }
            }, 2000);
        }

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
                    Toast.makeText(LiveDisplayActivity.this, "没有监测到网络,请检查网络连接", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onRestart() {
        super.onRestart();
        startToPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetStateUtil.registerNetState(this, netChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(toFloatingWindow){
            video.removeView(FloatingPlayer.getInstance().getKSYTextureView());
            FloatingPlayer.getInstance().getKSYTextureView().setOnTouchListener(null);
            FloatingPlayer.getInstance().getKSYTextureView().setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        }else{
            FloatingPlayer.getInstance().getKSYTextureView().pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetStateUtil.unregisterNetState(this);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
    }
}
