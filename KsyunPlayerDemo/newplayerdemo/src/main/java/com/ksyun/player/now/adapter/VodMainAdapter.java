package com.ksyun.player.now.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ksyun.player.now.R;
import com.ksyun.player.now.activity.VodDisplayActivity;
import com.ksyun.player.now.activity.VodMainActivity;
import com.ksyun.player.now.bean.VodBean;
import com.ksyun.player.now.model.FloatingPlayer;
import com.ksyun.player.now.utils.Ids;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhangyunlong on 2017/8/3.
 */

public class VodMainAdapter extends RecyclerView.Adapter<VodMainAdapter.VodMainHolder> {
    private static final int ITEM_VIEW_TYPE_HEADER=0;
    private static final int ITEM_VIEW_TYPE_ITEM=1;
    private Context context;
    private VodMainHolder vodMainHolder;
    private List<VodBean.DataBean.DetailBean> videoList;
    private Handler handler;
    public VodMainAdapter(Context context, List<VodBean.DataBean.DetailBean> videoList) {
        this.context = context;
        this.videoList=videoList;
    }

    @Override
    public VodMainHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType==ITEM_VIEW_TYPE_HEADER){
            view = LayoutInflater.from(context).inflate(R.layout.item_vod_main_header, parent, false);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.item_vod_main, parent, false);
        }
        vodMainHolder = new VodMainHolder(view);
        return vodMainHolder;
    }

    @Override
    public void onBindViewHolder(VodMainHolder holder, final int position) {
        holder.textView.setText(videoList.get(position).getVideoTitle());
        Glide.with(context)
                .load(videoList.get(position).getCoverURL().get(0))
                .error(R.mipmap.cover)
                .into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FloatingPlayer.getInstance().getKSYTextureView() != null) {
                    FloatingPlayer.getInstance().destroy();
                    handler.obtainMessage(VodMainActivity.ONLY_REMOVE_WINDOW).sendToTarget();
                }
                Intent intent=new Intent(context, VodDisplayActivity.class);
                intent.putExtra(Ids.PLAY_ID,position);
                Ids.playingId=position;
                intent.putExtra(Ids.VIDEO_LIST, (Serializable)videoList);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return ITEM_VIEW_TYPE_HEADER;
        }else{
            return ITEM_VIEW_TYPE_ITEM;
        }
    }
    public void setHandler(Handler handler){
        this.handler=handler;
    }
    class VodMainHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public VodMainHolder(View view) {
            super(view);
            imageView=(ImageView)view.findViewById(R.id.vod_main_image);
            textView=(TextView)view.findViewById(R.id.item_main_text);
        }
    }
}
