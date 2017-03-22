package com.ksyun.player.demo.model;

import android.content.Context;

import com.ksyun.media.player.KSYMediaPlayer;

/**
 * Created by xbc on 2017/3/14.
 */

public class KSYFloatingPlayer {

    private KSYMediaPlayer  mKsyMediaPlayer;

    private static KSYFloatingPlayer _instance;

    private KSYFloatingPlayer() {}

    public static KSYFloatingPlayer getInstance() {
        if (_instance == null) {
            synchronized (KSYFloatingPlayer.class) {
                if (_instance == null)
                    _instance = new KSYFloatingPlayer();
            }
        }

        return _instance;
    }

    public void init(Context context) {
        if (mKsyMediaPlayer != null) {
            mKsyMediaPlayer.release();
            mKsyMediaPlayer = null;
        }

        mKsyMediaPlayer = new KSYMediaPlayer.Builder(context).build();
    }
    public KSYMediaPlayer getKSYMediaPlayer() {
        return mKsyMediaPlayer;
    }

    public void destroy() {
        if (mKsyMediaPlayer != null)
            mKsyMediaPlayer.release();

        mKsyMediaPlayer = null;
    }
}
