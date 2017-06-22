package com.ksyun.player.demo.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.player.demo.R;
import com.ksyun.player.demo.model.GetList;
import com.ksyun.player.demo.ui.activity.player.FloatingVideoActivity;
import com.ksyun.player.demo.ui.activity.player.MediaPlayerActivity;
import com.ksyun.player.demo.ui.activity.player.TextureVideoActivity;
import com.ksyun.player.demo.ui.activity.player.TextureVodActivity;
import com.ksyun.player.demo.ui.others.JieVideoListViewAdapter;
import com.ksyun.player.demo.ui.view.LoadMoreListView;
import com.ksyun.player.demo.util.Settings;
import com.ksyun.player.demo.util.Video;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by admin on 17/6/21.
 */

public class LocalFragment extends Fragment {

    private ArrayList<Video> showListVideos;
    private TextView localPath;
    private LoadMoreListView mListView;
    private JieVideoListViewAdapter mAdapter;
    private SharedPreferences settings;
    public static Handler mHandler;
    private GetList getList;
    private File currentFile;
    private boolean isUpdate = false;

    public LocalFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        showListVideos = new ArrayList<Video>();
        getList = new GetList();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        if (isUpdate) {
                            updatelist();
                            mAdapter.notifyDataSetChanged();
                            mListView.setLoadCompleted();
                        } else {
                            Toast.makeText(getActivity(), "更新失败,请等待加载完毕", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 2:
                        if (msg.obj instanceof ArrayList) {
                            showListVideos.clear();
                            showListVideos.addAll((ArrayList<Video>) msg.obj);
                            updatelist();
                        }
                        break;

                    case 3:
                        if (msg.obj instanceof ArrayList) {
                            isUpdate = true;
                            showListVideos.clear();
                            showListVideos.addAll((ArrayList<Video>) msg.obj);
                        }

                    default:
                        break;
                }
            }
        };

        View view = inflater.inflate(R.layout.fragment_local, container, false);
        localPath = (TextView) view.findViewById(R.id.local_path);
        mListView = (LoadMoreListView) view.findViewById(R.id.list_local_frag);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Video v = showListVideos.get(position);
                File file = new File(v.getPath());
                if (file.isDirectory()) {
                    showListVideos.clear();
                    getList.getFileList(showListVideos, file);
                    currentFile = file;
                    localPath.setText(currentFile.getAbsolutePath());
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessageDelayed(msg, 500);
                } else {
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

        mListView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onloadMore() {
                loadMore();
            }
        });

        getList.getFileList(showListVideos, Environment.getExternalStorageDirectory());
        currentFile = Environment.getExternalStorageDirectory();
        localPath.setText(currentFile.getAbsolutePath());

        return view;

    }

    private void loadMore() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.obtainMessage(1).sendToTarget();
            }
        }.start();
    }

    public void updatelist() {
        mAdapter = new JieVideoListViewAdapter(getActivity(), showListVideos);
        mListView.setAdapter(mAdapter);
    }


    public void setSettings( SharedPreferences set){
        settings = set;
    }

    public void onBackPressed(){
        if(currentFile.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
            getActivity().finish();
        } else {
            showListVideos.clear();
            getList.getFileList(showListVideos, currentFile.getParentFile());
            currentFile = currentFile.getParentFile();
            localPath.setText(currentFile.getAbsolutePath());
            Message msg = new Message();
            msg.what = 1;
            mHandler.sendMessageDelayed(msg,500);
        }
    }

}
