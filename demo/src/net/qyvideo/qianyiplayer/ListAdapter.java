package net.qyvideo.qianyiplayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.qyvideo.qianyiplayer.R;

import java.util.ArrayList;

/**
 * Created by QianYi-Xin on 2015/5/18.
 */
public class ListAdapter extends BaseAdapter {

    private ArrayList<String> mFiles;
    private Context mContext;

    public ListAdapter(Context context) {
        mContext = context;
        mFiles = new ArrayList<String>();
    }

    public void setFileList(ArrayList<String> input) {
        if(input != null)
            mFiles.addAll(input);
    }

    public void clearFileList() {
        if(mFiles != null)
        {
            mFiles.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean isEmpty() {
        return mFiles.isEmpty();
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return super.areAllItemsEnabled();
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.list_item, null);

            holder.icon = (ImageView) convertView.findViewById(R.id.item_image);
            holder.name = (TextView) convertView.findViewById(R.id.item_text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String videoPath = mFiles.get(position);
        String videoName = videoPath.substring(videoPath.lastIndexOf("/") + 1, videoPath.lastIndexOf("."));

        holder.name.setText(videoName);

        return convertView;
    }

    private class ViewHolder {
        ImageView icon;
        TextView name;
    }
}
