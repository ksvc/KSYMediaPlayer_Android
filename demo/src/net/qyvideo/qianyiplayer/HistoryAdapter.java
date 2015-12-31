package net.qyvideo.qianyiplayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.qyvideo.qianyiplayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by QianYi-Xin on 2015/5/18.
 */
public class HistoryAdapter extends BaseAdapter {

    private ArrayList<String> mFiles;
    private Context mContext;

    public HistoryAdapter(Context context) {
        mContext = context;
        mFiles = new ArrayList<String>();
    }

    public void setFileList(List<String> input) {
        if(input != null)
            mFiles.addAll(input);

        if(mFiles.size() > 0)
            notifyDataSetChanged();
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

        holder.name.setText(videoPath);

        return convertView;
    }

    private class ViewHolder {
        ImageView icon;
        TextView name;
    }
}
