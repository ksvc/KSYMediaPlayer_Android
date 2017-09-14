package com.ksyun.player.now.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ksyun.player.now.R;
import com.ksyun.player.now.bean.VodBean;

import java.util.List;

/**
 * Created by zhangyunlong on 2017/8/4.
 */

public class VodDiaplayAdapter extends BaseAdapter {
    private Context context;
    private List<VodBean.DataBean.DetailBean> videoList;
    public VodDiaplayAdapter(Context context,List<VodBean.DataBean.DetailBean> videoList){
        this.context=context;
        this.videoList=videoList;
    }
    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int i) {
        return videoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view==null){
            view= LayoutInflater.from(context).inflate(R.layout.item_vod_display,viewGroup,false);
            holder=new ViewHolder();
            holder.imageView=(ImageView)view.findViewById(R.id.imageView);
            holder.textView=(TextView)view.findViewById(R.id.textView);
            view.setTag(holder);
        }else{
            holder=(ViewHolder)view.getTag();
        }
        holder.textView.setText(videoList.get(i).getVideoTitle());
        Glide.with(context)
                .load(videoList.get(i).getCoverURL().get(1))
                .error(R.mipmap.cover)
                .into(holder.imageView);
        return view;
    }

    class ViewHolder
    {
        TextView textView;
        ImageView imageView;
    }
}
