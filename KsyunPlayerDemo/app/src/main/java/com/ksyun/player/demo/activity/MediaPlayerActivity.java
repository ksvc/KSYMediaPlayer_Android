package com.ksyun.player.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.ksyun.player.demo.R;

import java.io.IOException;

/**
 * Created by xbc on 2017/4/27.
 */

public class MediaPlayerActivity extends Activity {

    private Context mContext;
    private MediaPlayer mMediaPlayer;

    private TextureView mTextureView;
    private Surface mSurface;
    private String mDataSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        setContentView(R.layout.activity_media_player);

        mDataSource = getIntent().getStringExtra("path");

        mTextureView = (TextureView) findViewById(R.id.media_player_view);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);

        try {
            mMediaPlayer.setDataSource(mDataSource);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null)
            mMediaPlayer.release();

        mMediaPlayer = null;
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            if (mMediaPlayer != null) {
                mMediaPlayer.start();
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            }
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            Toast.makeText(mContext, "MediaPlayer OnError:"+i, Toast.LENGTH_SHORT).show();
            MediaPlayerActivity.this.finish();
            return false;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Toast.makeText(mContext, "MediaPlayer Play Complete!", Toast.LENGTH_SHORT).show();
            MediaPlayerActivity.this.finish();
        }
    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            if (mSurface != null)
                mSurface.release();

            mSurface = new Surface(surfaceTexture);
            if (mMediaPlayer != null)
                mMediaPlayer.setSurface(mSurface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
            }

            if (mMediaPlayer != null)
                mMediaPlayer.setSurface(null);

            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
}
