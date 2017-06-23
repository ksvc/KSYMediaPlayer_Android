package com.ksyun.player.demo.ui.others;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksyun.player.demo.R;
import com.ksyun.player.demo.model.MyVideoThumbLoader;
import com.ksyun.player.demo.util.Video;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by liubohua on 16/7/12.
 */
public class JieVideoListViewAdapter extends BaseAdapter {

    private ArrayList<Video> listVideos;
    private LayoutInflater mLayoutInflater;
    private MyVideoThumbLoader mVideoThumbLoader;

    public JieVideoListViewAdapter(Context context, ArrayList<Video> listVideos){
        mLayoutInflater = LayoutInflater.from(context);
        this.listVideos = listVideos;
        mVideoThumbLoader = new MyVideoThumbLoader();// 初始化缩略图载入方法
    }


    @Override
    public int getCount() {
        return listVideos != null ? listVideos.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = mLayoutInflater.inflate(R.layout.list_local, null);
            holder.img = (ImageView)view.findViewById(R.id.img_list_local);
            holder.title = (TextView)view.findViewById(R.id.tittle_list_local);
            holder.path = (TextView)view.findViewById(R.id.content_list_local);
            view.setTag(holder);
        }else {
            holder = (ViewHolder)view.getTag();
        }
        String path = listVideos.get(i).getPath();
        File file = new File(path);
        if(file.isDirectory()){
            holder.img.setImageResource(R.drawable.file);
        }else{
            holder.img.setTag(path);
            mVideoThumbLoader.showThumbByAsyncTask(path, holder.img);
        }
        holder.title.setText(listVideos.get(i).getTitle());
        holder.path.setText(listVideos.get(i).getPath());
        return view;

    }

    public final class ViewHolder{
        public ImageView img;
        public TextView title;
        public TextView path;
    }
}
