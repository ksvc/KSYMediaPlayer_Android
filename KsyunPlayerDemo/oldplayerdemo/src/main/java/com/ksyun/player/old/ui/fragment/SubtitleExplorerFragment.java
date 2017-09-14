package com.ksyun.player.old.ui.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ksyun.player.old.R;
import com.ksyun.player.old.ui.others.JieVideoListViewAdapter;
import com.ksyun.player.old.util.Video;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Created by xbc on 2017/6/29.
 */

public class SubtitleExplorerFragment extends DialogFragment {

    private TextView mExplorerPath;
    private ListView mSubListView;

    private JieVideoListViewAdapter mAdapter;
    private ArrayList<Video> mSubItemList = new ArrayList<>();
    private String mCurrentPath;

    private OnSubtitleSelectedListener mSubtitleSelectedListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.SubtitleFileExplorerStyle);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_subtitle_explorer, null);

        mCurrentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        updateSubtitleList(mCurrentPath);

        mExplorerPath = (TextView) view.findViewById(R.id.explorer_path);
        mSubListView = (ListView) view.findViewById(R.id.sub_list);

        mExplorerPath.setText(mCurrentPath);

        mAdapter = new JieVideoListViewAdapter(getActivity(), mSubItemList);
        mSubListView.setAdapter(mAdapter);
        mSubListView.setOnItemClickListener(mOnItemClickListener);

        setDialogPosition(dialog);
        dialog.setContentView(view);

        return dialog;
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Video v = mSubItemList.get(position);
            File file = new File(v.getPath());

            if (file.isDirectory()) {
                mCurrentPath = file.getAbsolutePath();
                mExplorerPath.setText(mCurrentPath);

                updateSubtitleList(mCurrentPath);
                mAdapter.notifyDataSetChanged();
            } else {
                if (mSubtitleSelectedListener != null)
                    mSubtitleSelectedListener.onSubtitleSelected(file.getAbsolutePath());
                dismiss();
            }
        }
    };

    private void setDialogPosition(Dialog dialog) {
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

    private void updateSubtitleList(String path) {
        mSubItemList.clear();

        File files = new File(path);
        files.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory() ||  isSubtitleFile(file)) {
                    Video v = new Video();
                    v.setTitle(file.getName());
                    v.setPath(file.getPath());
                    mSubItemList.add(v);
                    return true;
                }
                return false;
            }
        });
    }

    private boolean isSubtitleFile(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".ass") || name.endsWith(".srt"))
            return true;
        return false;
    }

    public interface OnSubtitleSelectedListener {
        void onSubtitleSelected(String path);
    }

    public void setOnSubtitleSelectedListener(OnSubtitleSelectedListener listener) {
        mSubtitleSelectedListener = listener;
    }
}
