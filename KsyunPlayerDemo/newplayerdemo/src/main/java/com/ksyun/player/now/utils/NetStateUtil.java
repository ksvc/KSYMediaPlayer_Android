package com.ksyun.player.now.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.ksyun.player.now.model.NetState;

public class NetStateUtil {

    private static NetStateBroadcastReceiver netStateBroadcastReceiver;

    public static void  registerNetState(Context context, NetChangeListener netChangeListener) {
        if (netStateBroadcastReceiver == null) {
            netStateBroadcastReceiver = new NetStateBroadcastReceiver();
        }
        netStateBroadcastReceiver.addListener(netChangeListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(netStateBroadcastReceiver, filter);
    }

    public static void unregisterNetState(Context context) {
        context.unregisterReceiver(netStateBroadcastReceiver);
    }


    public static class NetStateBroadcastReceiver extends BroadcastReceiver {
        private NetChangeListener netChangeListener;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                int netWorkState = NetState.getNetWorkState(context);
                if(netChangeListener!=null){
                    netChangeListener.onNetStateChange(netWorkState);
                }
            }
        }

        public void addListener(NetChangeListener netChangeListener) {
            this.netChangeListener = netChangeListener;
        }
    }

    public interface NetChangeListener {
        void onNetStateChange(int netWorkState);
    }
}
