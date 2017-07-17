package com.ksyun.player.demo.ui.others;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ksyun.media.player.misc.KSYTrackInfo;
import com.ksyun.media.player.misc.ITrackInfo;
import com.ksyun.player.demo.R;

import java.util.ArrayList;

/**
 * Created by xbc on 2017/7/3.
 */

public class StreamInfoAdapter extends BaseAdapter {

    private ArrayList<KSYTrackInfo> mStreamInfoList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;

    public StreamInfoAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void updateStreamInfoList(ArrayList<KSYTrackInfo> list) {
        mStreamInfoList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mStreamInfoList != null)
            return mStreamInfoList.size()+1;

        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (mStreamInfoList != null)
            return mStreamInfoList.get(i);
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mLayoutInflater.inflate(R.layout.stream_info_item, null);
            holder.mStreamIndex = (TextView) view.findViewById(R.id.stream_index);
            holder.mStreamType = (TextView) view.findViewById(R.id.stream_type);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (i == 0)
            return view;

        KSYTrackInfo info = mStreamInfoList.get(i-1);
        String type = "N/A";
        switch (info.getTrackType()) {
            case ITrackInfo.MEDIA_TRACK_TYPE_AUDIO:
                type = "Audio";
                break;
            case ITrackInfo.MEDIA_TRACK_TYPE_VIDEO:
                type = "Video";
                break;
            case ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT:
                type = "InternalSubtitle";
                break;
            case ITrackInfo.MEDIA_TRACK_TYPE_EXTERNAL_TIMEDTEXT:
                type = "ExternalSubtitle";
                break;
        }

        // MiUI 的bug, 不能直接setText(0)
        String index = String.valueOf(info.getTrackIndex());
        holder.mStreamType.setText(type);
        holder.mStreamIndex.setText(index);

        return view;
    }

    private class ViewHolder {
        TextView mStreamIndex;
        TextView mStreamType;
    }
}
