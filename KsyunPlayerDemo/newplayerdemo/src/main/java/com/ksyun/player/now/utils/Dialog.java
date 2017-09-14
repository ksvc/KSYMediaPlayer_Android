package com.ksyun.player.now.utils;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;


public class Dialog {
    private static AnimationDrawable animationDrawable;
    private static View view;

    public static void init(View view1) {
        view = view1;
        animationDrawable = (AnimationDrawable) view.getBackground();
    }

    public static void show() {
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
        }
        if(!animationDrawable.isRunning()){
            animationDrawable.start();
        }
    }

    public static void dismiss() {
        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        }
    }
}
