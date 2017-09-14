package com.ksyun.player.now.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.ksyun.player.now.R;


public class VideoLoadMoreListView extends ListView implements AbsListView.OnScrollListener{
    private Context mContext;
    private View mFootView,mHeaderView;
    private int mTotalItemCount;
    private OnLoadMoreListener mLoadMoreListener;
    private boolean mIsLoading=false;

    private TextView textView_video_name;
    public VideoLoadMoreListView(Context context) {
        super(context);
        init(context);
    }

    public VideoLoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoLoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.mContext=context;
        mFootView= LayoutInflater.from(context).inflate(R.layout.footer_view,null);
        mHeaderView=LayoutInflater.from(context).inflate(R.layout.header_view,null);
        textView_video_name=(TextView)mHeaderView.findViewById(R.id.video_name);
        addHeaderView(mHeaderView);
        setOnScrollListener(this);
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        /*if(mLoadMoreEnabled) {
            // 滑到底部后自动加载，判断listview已经停止滚动并且最后可视的条目等于adapter的条目
            int lastVisibleIndex = view.getLastVisiblePosition();
            if (!mIsLoading && scrollState == OnScrollListener.SCROLL_STATE_IDLE
                    && lastVisibleIndex == mTotalItemCount - 1) {
                mIsLoading = true;
                addFooterView(mFootView);
                if (mLoadMoreListener != null) {
                    mLoadMoreListener.onloadMore();
                }
            }
        }*/

    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mTotalItemCount = totalItemCount;
    }
    public void updateHeader(String videoName){
        textView_video_name.setText(videoName);
    }
    public void setOnLoadMoreListener(OnLoadMoreListener listener){
        mLoadMoreListener = listener;
    }

    public interface OnLoadMoreListener{
        void onloadMore();
    }
    public void setLoadCompleted(){
        if(mLoadMoreEnabled) {
            mIsLoading = false;
            removeFooterView(mFootView);
        }
    }

    private boolean mLoadMoreEnabled = true;
    public void setLoadMoreEnabled(boolean enabled){
        mLoadMoreEnabled = enabled;
    }
}
