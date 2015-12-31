package net.qyvideo.qianyiplayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.qyvideo.qianyiplayer.util.JObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by QianYi-Xin on 2015/6/1.
 */
public class DemoAdapter extends BaseAdapter {

    private Context mContext;
    private List<JObject> mSourceUrls;

    public DemoAdapter(Context context) {
        mContext = context;
        mSourceUrls = new ArrayList<>();
    }

    public  void setSourceUrls(ArrayList<JObject> src) {
        if(src == null || src.size() <= 0)
            return;

        if(mSourceUrls.size() > 0)
            mSourceUrls.clear();

        mSourceUrls.addAll(src);

        notifyDataSetInvalidated();
    }

    public void clearFileList() {
        if(mSourceUrls != null)
        {
            mSourceUrls.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean isEmpty() {
        return mSourceUrls.isEmpty();
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
        return mSourceUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return mSourceUrls.get(position);
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

        holder.name.setText(mSourceUrls.get(position).name);

        return convertView;
    }

    private class ViewHolder {
        ImageView icon;
        TextView name;
    }
}
