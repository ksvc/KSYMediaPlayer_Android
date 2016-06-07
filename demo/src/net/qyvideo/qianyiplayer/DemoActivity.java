package net.qyvideo.qianyiplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.util.Log;

import net.qyvideo.qianyiplayer.util.Extensions;
import net.qyvideo.qianyiplayer.util.AsyncCallback;
import net.qyvideo.qianyiplayer.util.PlayerPopupWindow;
import net.qyvideo.qianyiplayer.util.QyDataBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by QianYi-Xin on 2015/6/1.
 */
public class DemoActivity extends Activity implements AsyncCallback{

    public static final int GET_JSONDATA = 0x1001;
    public static final int PLAY_VIDEO = 0x1002;

    private Context mContext;
    private RelativeLayout mLoadingLayout;
    private ListView mListView;
    private ListView mHistoryListView;
    private Button mLocalPlayButton;
    private Button mInputUrlButton;
    private EditText mEditText;

    private ListAdapter mListAdapter;
    private HistoryAdapter mHistoryAdapter;
    private PlayerPopupWindow mPlayerPopupWin;
    private UIHandler mHandler;
    private boolean mIsLoading;
    private boolean mIsLocalPlayback = false;
    private int mPlayerposition = 0;
    private int mPlayType = 0; // 1:local, 2:network, 3:input url

    private QyDataBase mDatabase;
    private List<String> mPlayHistory;

    private RadioGroup mChooseSurface;
    private RadioGroup mChooseCodec;

    private boolean useHwCodec = false;
    private boolean useSurfaceView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getApplicationContext();
        mListAdapter = new ListAdapter(mContext);
        mHistoryAdapter = new HistoryAdapter(mContext);
        mDatabase = new QyDataBase(mContext);
        mHandler = new UIHandler(this);
        mPlayerPopupWin = new PlayerPopupWindow(this, mHandler);
        mPlayHistory = new ArrayList<>();
        mIsLoading = false;

        setContentView(R.layout.demo_main);

        mLoadingLayout = (RelativeLayout) findViewById(R.id.demo_lo);
        mListView = (ListView) findViewById(R.id.demo_list);
        mHistoryListView = (ListView) findViewById(R.id.play_history);
        mLocalPlayButton = (Button) findViewById(R.id.demo_localplay);
        mInputUrlButton = (Button) findViewById(R.id.demo_writein);
        mEditText = (EditText) findViewById(R.id.network_url);
        mChooseSurface = (RadioGroup) findViewById(R.id.choose_surface);
        mChooseCodec = (RadioGroup) findViewById(R.id.choose_codec);

        mLocalPlayButton.setOnClickListener(mOnLocalClickListener);
        mInputUrlButton.setOnClickListener(mOnInputClickListener);
        mListView.setOnItemClickListener(mItemListener);
        mHistoryListView.setOnItemClickListener(mItemListener);
        mEditText.setOnKeyListener(mKeyListerner);
        mEditText.setOnEditorActionListener(mOnEditorListener);
        mChooseSurface.setOnCheckedChangeListener(mCheckedChangeListener);
        mChooseCodec.setOnCheckedChangeListener(mCheckedChangeListener);

        mPlayerPopupWin.init();
        initPlayConfig();
    }

    @Override
    public void onDataCallback(String data) {
        if(data != null) {
            mHandler.obtainMessage(GET_JSONDATA, data).sendToTarget();
        }
    }

    @Override
    public void onErrorCallback(int errorCode) {
        mIsLoading = false;
        mLoadingLayout.setVisibility(View.GONE);
        Toast.makeText(this, "ErrorCode:" + errorCode, Toast.LENGTH_LONG).show();
    }

    private void initLocalPlayList()
    {
        String path = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            path = Environment.getExternalStorageDirectory().getPath();
        else
        {
            Toast.makeText(this, "Fail to get the external storage, so quit.", Toast.LENGTH_LONG).show();
            finish();
        }

        if(mHistoryListView.getAdapter() != null)
        {
            mHistoryAdapter.clearFileList();
            mHistoryListView.setVisibility(View.GONE);
        }

        if(mListView.getAdapter() != null)
        {
            mListAdapter.clearFileList();
        }

        mListAdapter.setFileList(getVideoFiles(path));
        mListView.setAdapter(mListAdapter);
        mIsLoading = false;
        mLoadingLayout.setVisibility(View.GONE);
    }

    private void initPlayConfig()
    {
        int default_codec  = mChooseCodec.getCheckedRadioButtonId();
        if (default_codec == R.id.use_hw) {
            useHwCodec = true;
        }
        else{
            useHwCodec = false;
        }

        int default_surface = mChooseSurface.getCheckedRadioButtonId();
        if (default_surface == R.id.use_surfaceview){
            useSurfaceView = true;
        }
        else {
            useSurfaceView = true;
        }
    }

    private ArrayList<String> getVideoFiles(String root) {
        if(root == null || root == "")
            return null;

        ArrayList<String> list = new ArrayList<>();
        File file = new File(root);
        File[] fileList = file.listFiles();

        for(File f : fileList)
        {
            if(f.getName().contains(".")) {
                int i = f.getName().lastIndexOf(".");
                if (i > 0) {
                    if (Extensions.VIDEO.contains(f.getName().substring(i))
                            || Extensions.AUDIO.contains(f.getName().substring(i))) {
                        list.add(f.getPath());
                    }
                }
            }
        }

        return list;
    }

    private void handleJsonData(String data) {
        mIsLoading = false;
        mLoadingLayout.setVisibility(View.GONE);

        if(mHistoryListView.getAdapter() != null)
        {
            mHistoryAdapter.clearFileList();
            mHistoryListView.setVisibility(View.GONE);
        }

        if(mListView.getAdapter() != null)
        {
            mListAdapter.clearFileList();
        }
    }

    private void startPlayerActivity(int codec) {
        Intent intent;
        if (useSurfaceView) {
            intent = new Intent(mContext, VideoPlayerActivity.class);
        }
        else {
            intent = new Intent(mContext, PlayerUseTextureView.class);
        }

        if(mPlayType == 1) {
            String path = (String) mListAdapter.getItem(mPlayerposition);

            intent.putExtra("path", path);
        }else if(mPlayType == 2) {

        } else if(mPlayType == 3) {
            String path = (String) mHistoryAdapter.getItem(mPlayerposition);

            intent.putExtra("path", path);
        }

        intent.putExtra("HWCodec",useHwCodec);
        startActivity(intent);
    }

    private void dealItemClick() {
        if(mPlayType == 1 || mPlayType == 3) {
            startPlayerActivity(0);
        } else {
            if(mPlayerPopupWin != null)
                mPlayerPopupWin.showPopWindow(mListView);
        }
    }

    View.OnKeyListener mKeyListerner = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                    keyCode == EditorInfo.IME_ACTION_GO ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String uri = mEditText.getText().toString();
                if(uri == null || uri.length() <= 0)
                    return false;

                Log.d("XBC", "get the file path:" + uri);
                if(mDatabase != null)
                    mPlayHistory = mDatabase.getPlayHistory();

                if(!mPlayHistory.contains(uri) && mDatabase != null)
                {
                    mDatabase.Insert(uri);

                    mPlayHistory = mDatabase.getPlayHistory();
                    mHistoryAdapter.clearFileList();
                    mHistoryAdapter.setFileList(mPlayHistory);
                }
                Intent intent;
                if (useSurfaceView) {
                    intent = new Intent(mContext, VideoPlayerActivity.class);
                }
                else {
                    intent = new Intent(mContext, PlayerUseTextureView.class);
                }
                intent.putExtra("path", uri);
                intent.putExtra("HWCodec",useHwCodec);

                startActivity(intent);
            }
            return false;
        }
    };

    TextView.OnEditorActionListener mOnEditorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return false;
        }
    };

    private View.OnClickListener mOnLocalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mPlayType = 1;
            mIsLocalPlayback = true;
            mIsLoading = true;
            mLoadingLayout.setVisibility(View.VISIBLE);
            initLocalPlayList();
            mListView.setVisibility(View.VISIBLE);
            mEditText.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener mOnInputClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPlayType = 3;

            if(mListView.getAdapter() != null)
            {
                mListAdapter.clearFileList();
            }

            if(mHistoryListView.getAdapter() != null)
                mHistoryAdapter.clearFileList();

            if(mPlayHistory != null && mPlayHistory.size() > 0)
                mPlayHistory.clear();

            if(mDatabase != null)
            {
                mPlayHistory = mDatabase.getPlayHistory();
                mHistoryAdapter.setFileList(mPlayHistory);
                mHistoryListView.setAdapter(mHistoryAdapter);
            }

            mListView.setVisibility(View.GONE);
            mHistoryListView.setVisibility(View.VISIBLE);
            mEditText.setVisibility(View.VISIBLE);
        }
    };

    AdapterView.OnItemClickListener mItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mPlayerposition = position;
            dealItemClick();
        }
    };

    RadioGroup.OnCheckedChangeListener mCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.use_surfaceview:
                    useSurfaceView = true;
                    break;
                case R.id.use_textureview:
                    useSurfaceView = false;
                    break;
                case R.id.use_hw:
                    useHwCodec = true;
                    break;
                case  R.id.use_sw:
                    useHwCodec = false;
                    break;
            }
        }
    };

    private class UIHandler extends Handler {
        private DemoActivity demoActivity;

        public UIHandler(DemoActivity activity) {
            demoActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_JSONDATA:
                    String data = (String) msg.obj;
                    if (demoActivity != null)
                        demoActivity.handleJsonData(data);
                    break;
                case PLAY_VIDEO:
                    if(demoActivity != null)
                        demoActivity.startPlayerActivity(msg.arg1);
                    break;
            }
        }
    }
}
