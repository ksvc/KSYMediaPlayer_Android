package com.ksyun.player.demo.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.ksyun.player.demo.R;
import com.ksyun.player.demo.ui.activity.player.FloatingVideoActivity;
import com.ksyun.player.demo.ui.activity.player.MediaPlayerActivity;
import com.ksyun.player.demo.ui.activity.player.TextureVideoActivity;
import com.ksyun.player.demo.ui.activity.player.TextureVodActivity;
import com.ksyun.player.demo.ui.others.JieVideoListViewAdapter;
import com.ksyun.player.demo.ui.view.LoadMoreListView;
import com.ksyun.player.demo.util.Settings;
import com.ksyun.player.demo.util.Video;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by admin on 17/6/21.
 */

public class LocalFragment extends Fragment {

    private ArrayList<Video> showListVideos = new ArrayList<>();
    private TextView localPath;
    private LoadMoreListView mListView;
    private JieVideoListViewAdapter mAdapter;
    private File currentFile;
    private File selectedFile;
    private SharedPreferences settings;
    int tempBatch = 0;
    int tempIndex = 0;

    public LocalFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_local, container, false);

            init();

        }

        return mRootView;

    }

    private void init() {

        localPath = (TextView) mRootView.findViewById(R.id.local_path);
        mListView = (LoadMoreListView) mRootView.findViewById(R.id.list_local_frag);
        mAdapter = new JieVideoListViewAdapter(getActivity(), showListVideos);
        mListView.setAdapter(mAdapter);

        mListView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onloadMore() {
                // 延迟500毫秒后再加载数据，否则因加载过快看不到"正在加载"的ProgressBar
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadMore(currentFile);
                    }
                }, 500);

            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Video v = showListVideos.get(position);

                File file = new File(v.getPath());

                if (file.isDirectory()){
                    selectedFile = file;
                    localPath.setText(selectedFile.getAbsolutePath());
                    showListVideos.clear();
                    List fileList = doGetData(0, BATCH, selectedFile);
                    showListVideos.addAll(fileList);
                    mAdapter.notifyDataSetChanged();
                }else{
                    if (settings == null){
                        Log.e("WSC", "find setting is null");
                    }
                    String playerType = settings.getString("choose_type", Settings.LIVE);
                    if (playerType.equals(Settings.VOD)) {
                        Intent intent = new Intent(getActivity(), TextureVodActivity.class);
                        intent.putExtra("path", v.getPath());
                        startActivity(intent);
                    } else if (playerType.equals(Settings.LIVE)){
                        Intent intent = new Intent(getActivity(), TextureVideoActivity.class);
                        intent.putExtra("path", v.getPath());
                        startActivity(intent);
                    } else if (playerType.equals(Settings.MEDIA_PLAYER)){
                        Intent intent = new Intent(getActivity(), MediaPlayerActivity.class);
                        intent.putExtra("path", v.getPath());
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), FloatingVideoActivity.class);
                        intent.putExtra("path", v.getPath());
                        startActivity(intent);
                    }
                }

            }
        });

        currentFile = Environment.getExternalStorageDirectory();

        fetchNew(currentFile);

    }

    // 批量，即每次最多加载多少条数据
    private static final int BATCH = 15;
    // 当前加载数据的索引
    private int mCurrentIndex;

    /**
     * 下拉刷新数据
     */
    private void fetchNew(File file) {
        mCurrentIndex = 0;

        List<Video> batchList = doGetData(mCurrentIndex, BATCH, file);

        showListVideos.addAll(batchList);

        mAdapter.notifyDataSetChanged();

    }

    /**
     * 上拉加载更多
     */
    private void loadMore(File file) {

        mCurrentIndex = showListVideos.size();

        List<Video> batchList = doGetData(mCurrentIndex, BATCH, file);

        showListVideos.addAll(batchList);

        mAdapter.notifyDataSetChanged();

        mListView.setLoadCompleted();

       /* if (batchList != null && batchList.size() < BATCH) {
            mListView.setLoadMoreEnabled(false);
        }*/
    }

    /**
     * 模拟分页加载的算法
     *
     * @param index 从哪条数据开始加载
     * @param batch 加载多少条数据
     */
    private List<Video> doGetData(final int index, final int batch, File file) {
        tempIndex = 0;
        tempBatch = 0;
        final List<Video> batchList = new ArrayList<>();
        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                // 筛选出文件夹、视频文件
                if (file.isDirectory() || isVideoFile(file)) {
                    if (tempBatch < batch) {
                        if (index == tempIndex) {
                            Video v = new Video();
                            v.setTitle(file.getName());
                            v.setPath(file.getAbsolutePath());
                            batchList.add(v);

                            tempBatch++;
                        } else {
                            tempIndex++;
                        }
                    }

                    return true;
                }
                return false;
            }
        });

        return batchList;
    }

    /**
     * 判断是否为视频文件
     */
    private boolean isVideoFile(File file) {
        // sdCard找到视频名称
        String name = new String(file.getName());

        int i = name.lastIndexOf('.');
        if (i != -1) {
            name = name.substring(i);
            if (name.equalsIgnoreCase(".mp4")
                    || name.equalsIgnoreCase(".3gp")
                    || name.equalsIgnoreCase(".wmv")
                    || name.equalsIgnoreCase(".ts")
                    || name.equalsIgnoreCase(".rmvb")
                    || name.equalsIgnoreCase(".mov")
                    || name.equalsIgnoreCase(".m4v")
                    || name.equalsIgnoreCase(".avi")
                    || name.equalsIgnoreCase(".m3u8")
                    || name.equalsIgnoreCase(".3gpp")
                    || name.equalsIgnoreCase(".3gpp2")
                    || name.equalsIgnoreCase(".mkv")
                    || name.equalsIgnoreCase(".flv")
                    || name.equalsIgnoreCase(".divx")
                    || name.equalsIgnoreCase(".f4v")
                    || name.equalsIgnoreCase(".rm")
                    || name.equalsIgnoreCase(".asf")
                    || name.equalsIgnoreCase(".ram")
                    || name.equalsIgnoreCase(".mpg")
                    || name.equalsIgnoreCase(".v8")
                    || name.equalsIgnoreCase(".swf")
                    || name.equalsIgnoreCase(".m2v")
                    || name.equalsIgnoreCase(".asx")
                    || name.equalsIgnoreCase(".ra")
                    || name.equalsIgnoreCase(".ndivx")
                    || name.equalsIgnoreCase(".xvid")) {
                return true;
            }
        }
        return false;
    }

    public void setSettings( SharedPreferences set){
        settings = set;
    }

    public void onBackPressed(){
        if(selectedFile.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
            getActivity().finish();
        } else {
            showListVideos.clear();
            List list = doGetData(0, BATCH, selectedFile.getParentFile());
            showListVideos.addAll(list);

            selectedFile = selectedFile.getParentFile();
            localPath.setText(selectedFile.getAbsolutePath());
            loadMore(selectedFile);

        }
    }

}
