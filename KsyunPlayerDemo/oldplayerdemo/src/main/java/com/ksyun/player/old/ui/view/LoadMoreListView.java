package com.ksyun.player.old.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.ksyun.player.old.R;


/**
 * Created by admin on 17/6/22.
 */

public class LoadMoreListView extends ListView implements AbsListView.OnScrollListener{
    private Context mContext;
    private View mFootView;
    private int mTotalItemCount;
    private OnLoadMoreListener mLoadMoreListener;
    private boolean mIsLoading=false;

    public LoadMoreListView(Context context) {
        super(context);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.mContext=context;
        mFootView= LayoutInflater.from(context).inflate(R.layout.listview_footer_view,null);
        setOnScrollListener(this);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(mLoadMoreEnabled) {
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
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mTotalItemCount = totalItemCount;
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
