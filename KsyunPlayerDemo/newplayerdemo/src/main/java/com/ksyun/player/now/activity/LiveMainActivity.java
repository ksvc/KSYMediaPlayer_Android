package com.ksyun.player.now.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.github.mzule.activityrouter.annotation.Module;
import com.github.mzule.activityrouter.annotation.Router;
import com.ksyun.player.now.R;
import com.ksyun.player.now.adapter.LiveMainAdapter;
import com.ksyun.player.now.bean.LiveBean;
import com.ksyun.player.now.model.FloatingPlayer;
import com.ksyun.player.now.utils.Ids;
import com.ksyun.player.now.utils.MyRequest;
import com.ksyun.player.now.utils.Urls;
import com.ksyun.player.now.view.FloatWindowView;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@Module("newplayerdemo")
@Router("liveMain")
public class LiveMainActivity extends AppCompatActivity implements Handler.Callback,View.OnClickListener{
    public static final int REMOVE_FLOATING_WINDOW = 1111;
    public static final int STOP_FLOATING_WINDOW = REMOVE_FLOATING_WINDOW + 1;
    public static final int ONLY_REMOVE_WINDOW=REMOVE_FLOATING_WINDOW+2;
    public static final int DATA_RESPONSE=REMOVE_FLOATING_WINDOW+3;
    public static final int NETWORK_ERROR=REMOVE_FLOATING_WINDOW+4;
    private ImageView config;
    private ImageView qRcode;
    private Boolean toDisplay=false;

    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private LiveMainAdapter liveMainAdapter;


    private FloatWindowView mFloatingView;
    private WindowManager.LayoutParams mFloatingViewParams;
    private WindowManager mWindowManager;

    private List<LiveBean.DataBean.DetailBean> videoList;
    private RelativeLayout title;

    private Handler mHandler;

    private SharedPreferences mSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_main);
        mHandler = new Handler(getMainLooper(), this);
        mSettings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        MyRequest.init();
        initData();
        initView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mSettings = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        if (FloatingPlayer.getInstance().getKSYTextureView() != null&&mSettings.getBoolean("isPlaying",true)) {
            createFloatingWindow(getApplicationContext());
            FloatingPlayer.getInstance().getKSYTextureView().start();
        }
        toDisplay=false;
    }

    private void initView() {
        qRcode = (ImageView) findViewById(R.id.QRcode);
        qRcode.setOnClickListener(this);
        config = (ImageView) findViewById(R.id.config);
        config.setOnClickListener(this);
        title = (RelativeLayout) findViewById(R.id.title);
        recyclerView = (RecyclerView) findViewById(R.id.vod_main_recycler);
        layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    return layoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
    }

    private void initData() {
        MyRequest.doGet(Urls.LIVE,new liveCallBack());
    }

    private void createFloatingWindow(Context context) {
        if (context == null)
            return;

        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (mFloatingView == null) {
            mFloatingView = new FloatWindowView(context);
            mFloatingView.addFloatingWindow(FloatingPlayer.getInstance().getKSYTextureView());
            mFloatingView.setHandler(mHandler);
            if (mFloatingViewParams == null) {
                mFloatingViewParams = new WindowManager.LayoutParams();
                mFloatingViewParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                mFloatingViewParams.format = PixelFormat.RGBA_8888;
                mFloatingViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                mFloatingViewParams.gravity = Gravity.LEFT | Gravity.TOP;
                mFloatingViewParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mFloatingViewParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mFloatingViewParams.x = screenWidth;
                mFloatingViewParams.y = screenHeight;
            }

            mFloatingView.updateViewLayoutParams(mFloatingViewParams);
            windowManager.addView(mFloatingView, mFloatingViewParams);
        }
    }

    public void removeFloatingWindow(Context context) {
        if (mFloatingView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mFloatingView);
            mFloatingView.removeFloatingWindow(FloatingPlayer.getInstance().getKSYTextureView());
            mFloatingView = null;
        }
    }

    private WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    private void videoPlayEnd() {
        if (FloatingPlayer.getInstance().getKSYTextureView() != null) {
            FloatingPlayer.getInstance().destroy();
        }
    }
    @Override
    public void onClick(View view) {
        Intent intent;
        int id = view.getId();
        if (id == R.id.QRcode) {
            intent = new Intent(this, CaptureNewActivity.class);
            startActivityForResult(intent, 0);
        } else if (id == R.id.config) {
            intent = new Intent(this, SettingNewActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case REMOVE_FLOATING_WINDOW:
                removeFloatingWindow(getApplicationContext());
                Intent intent = new Intent(this, LiveDisplayActivity.class);
                intent.putExtra("videoList",(Serializable)videoList);
                intent.putExtra("playingId", Ids.playingId);
                startActivity(intent);
                toDisplay=true;
                break;
            case STOP_FLOATING_WINDOW:
                removeFloatingWindow(getApplicationContext());
                videoPlayEnd();
                toDisplay=false;
                break;
            case ONLY_REMOVE_WINDOW:
                removeFloatingWindow(getApplicationContext());
                toDisplay=true;
                break;
            case DATA_RESPONSE:
                liveMainAdapter = new LiveMainAdapter(LiveMainActivity.this,videoList);
                liveMainAdapter.setHandler(mHandler);
                recyclerView.setAdapter(liveMainAdapter);
                break;
            case NETWORK_ERROR:
                Toast.makeText(LiveMainActivity.this,"网络连接失败",Toast.LENGTH_LONG).show();
                break;

        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(FloatingPlayer.getInstance().getKSYTextureView()!=null&&!toDisplay){
            FloatingPlayer.getInstance().getKSYTextureView().pause();
        }
        removeFloatingWindow(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayEnd();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            if (TextUtils.isEmpty(scanResult))
            {
                Toast.makeText(this, "Scan Content is Null", Toast.LENGTH_SHORT).show();
                return;
            }

            FloatingPlayer.getInstance().destroy();
            Intent intent = new Intent(this, LiveDisplayActivity.class);
            intent.putExtra(Ids.PLAY_ID, -1);
            Ids.playingId = -1;
            intent.putExtra(Ids.VIDEO_LIST, (Serializable) videoList);
            intent.putExtra(Ids.PLAY_URL, scanResult);
            startActivity(intent);
        }
    }
    private class liveCallBack implements Callback
    {
        @Override
        public void onFailure(Call call, IOException e) {
            mHandler.obtainMessage(LiveMainActivity.NETWORK_ERROR).sendToTarget();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                LiveBean liveBean= JSON.parseObject(response.body().string(),LiveBean.class);
                videoList=liveBean.getData().getDetail();
                mHandler.obtainMessage(LiveMainActivity.DATA_RESPONSE).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
