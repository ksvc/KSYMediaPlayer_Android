package com.ksyun.player.demo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.player.demo.R;
import com.ksyun.player.demo.model.GetList;
import com.ksyun.player.demo.util.Settings;
import com.ksyun.player.demo.util.Video;

import java.io.File;
import java.util.ArrayList;


public class LocalFragment extends android.app.Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int UPDATE = 1;
    private ListView listView;
    private JieVideoListViewAdapter mAdapter;
    private ArrayList<Video> showListVideos;
    private SwipeRefreshLayout swipeLayout;
    public static Handler mHandler;
    private GetList getList;
    private boolean isUpdate = false;
    private SharedPreferences settings;
    private File currentFile;

    private TextView localPath;

    public LocalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        showListVideos = new ArrayList<Video>();
        getList = new GetList();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATE:
                        if (isUpdate) {
                            updatelist();
                            swipeLayout.setRefreshing(false);
                        } else {
                            swipeLayout.setRefreshing(false);
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
                }
            }
        };

        View view = inflater.inflate(R.layout.fragment_local, container, false);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        listView = (ListView) view.findViewById(R.id.list_local_frag);
        localPath = (TextView) view.findViewById(R.id.local_path);
        swipeLayout.setOnRefreshListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                    msg.what = UPDATE;
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
                    } else {
                        Intent intent = new Intent(getActivity(), FloatingVideoActivity.class);
                        intent.putExtra("path", v.getPath());
                        startActivity(intent);
                    }
                }
            }
        });
        getList.getFileList(showListVideos, Environment.getExternalStorageDirectory());
        currentFile = Environment.getExternalStorageDirectory();
        localPath.setText(currentFile.getAbsolutePath());
        return view;
    }

    public void updatelist() {
        mAdapter = new JieVideoListViewAdapter(getActivity(), showListVideos);
        listView.setAdapter(mAdapter);
    }

    public void setSettings( SharedPreferences set){
        settings = set;
    }

    @Override
    public void onRefresh() {
        Message msg = new Message();
        msg.what = UPDATE;
        mHandler.sendMessageDelayed(msg,3000);
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
            msg.what = UPDATE;
            mHandler.sendMessageDelayed(msg,500);
        }
    }
}

