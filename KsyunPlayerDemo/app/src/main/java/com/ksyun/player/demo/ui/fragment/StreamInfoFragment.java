package com.ksyun.player.demo.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.ksyun.media.player.misc.KSYTrackInfo;
import com.ksyun.player.demo.R;
import com.ksyun.player.demo.ui.others.StreamInfoAdapter;

import java.util.ArrayList;

/**
 * Created by xbc on 2017/7/3.
 */

public class StreamInfoFragment extends DialogFragment {

    private ListView mStreamInfoList;
    private StreamInfoAdapter mAdapter;
    private ArrayList<KSYTrackInfo> mTrackList = new ArrayList<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity(), R.style.SubtitleFileExplorerStyle);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_stream_info, null);

        mStreamInfoList = (ListView) view.findViewById(R.id.frag_stream_info);

        mAdapter = new StreamInfoAdapter(getActivity());
        if (mTrackList.size() > 0)
            mAdapter.updateStreamInfoList(mTrackList);
        mStreamInfoList.setAdapter(mAdapter);

        setDialogPosition(dialog);

        dialog.setContentView(view);

        return dialog;
    }

    public void updateStreamInfo(KSYTrackInfo[] infos) {
        if (infos == null || infos.length < 1)
            return;

        mTrackList.clear();
        for (KSYTrackInfo info : infos) {
            mTrackList.add(info);
        }

        if (mAdapter != null)
            mAdapter.updateStreamInfoList(mTrackList);
    }

    private void setDialogPosition(Dialog dialog) {
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }
}
