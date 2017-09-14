package com.ksyun.player.old.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by liubohua on 2017/1/6.
 */
public class NetState {

    public static final int NETWORK_NONE = 997;

    public static final int NETWORK_WIFI = NETWORK_NONE + 1;

    public static final int NETWORK_MOBILE = NETWORK_WIFI + 1;

    public static int getNetWorkState(Context context) {

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
       if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
            return NETWORK_WIFI;
        } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
            return NETWORK_MOBILE;
        } else {
            return NETWORK_NONE;
        }
    }
}
