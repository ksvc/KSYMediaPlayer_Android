package com.ksyun.player.demo.ui.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ksyun.player.demo.R;
import com.ksyun.player.demo.ui.activity.player.FloatingVideoActivity;
import com.ksyun.player.demo.ui.activity.player.MediaPlayerActivity;
import com.ksyun.player.demo.ui.activity.player.TextureVideoActivity;
import com.ksyun.player.demo.ui.activity.player.TextureVodActivity;
import com.ksyun.player.demo.ui.others.JieVideoListViewAdapter;
import com.ksyun.player.demo.util.Settings;
import com.ksyun.player.demo.util.Video;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 17/6/30.
 */

public class ListFragment extends DialogFragment {
    private View view;
    private ArrayList<Video> showListVideos = new ArrayList<>();
    private ListView mListView;
    private TextView mLocalPath;
    private ArrayList<Video> batchList;
    private JieVideoListViewAdapter mAdapter;
    private File selectedFile;
    private SharedPreferences settings;

    public ListFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.SpecialEffectsDialogStyle);
        setDialogPosition(dialog);
        return dialog;
    }

    private void setDialogPosition(Dialog dialog) {
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);

        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(params);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view == null){
            view = inflater.inflate(R.layout.fragment_local_list, container, false);
        }
        initData();
        mLocalPath = (TextView) view.findViewById(R.id.local_path_frag);
        mListView = (ListView) view.findViewById(R.id.local_frag_list);
        mAdapter = new JieVideoListViewAdapter(getActivity(), showListVideos);
        mListView.setAdapter(mAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Video v = showListVideos.get(position);

                File file = new File(v.getPath());

                if (file.isDirectory()){
                    selectedFile = file;
                    mLocalPath.setText(selectedFile.getAbsolutePath());
                    showListVideos.clear();
                    List fileList = getVideoFileList(selectedFile);
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

        return view;
    }

    private void initData() {
        batchList = getVideoFileList(Environment.getExternalStorageDirectory());
        showListVideos.addAll(batchList);
    }

    private ArrayList<Video> getVideoFileList(File file) {
        final ArrayList<Video> batchList = new ArrayList<>();
        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                // 筛选出文件夹、视频文件
                if (file.isDirectory() || isVideoFile(file)) {
                    Video v = new Video();
                    v.setTitle(file.getName());
                    v.setPath(file.getAbsolutePath());
                    batchList.add(v);
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
            if (name.equalsIgnoreCase(".srt") || name.equalsIgnoreCase(".ass")) {
                return true;
            }
        }
        return false;
    }

    public void setSettings( SharedPreferences set){
        settings = set;
    }
/*
    public void onBackPressed(){
        if(selectedFile.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
            getActivity().finish();
        } else {
            showListVideos.clear();

            selectedFile = selectedFile.getParentFile();
            mLocalPath.setText(selectedFile.getAbsolutePath());

            List list = getVideoFileList(selectedFile);
            showListVideos.addAll(list);
            mAdapter.notifyDataSetChanged();
        }
    }*/

}
