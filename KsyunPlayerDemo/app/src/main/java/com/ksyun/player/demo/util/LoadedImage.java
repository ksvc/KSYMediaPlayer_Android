package com.ksyun.player.demo.util;

import android.graphics.Bitmap;

/**
 * Created by liubohua on 16/7/12.
 */
public class LoadedImage {
    Bitmap mBitmap;

    public LoadedImage(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
