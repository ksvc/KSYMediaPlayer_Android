package com.ksyun.player.now.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class MyRequest {
    private static OkHttpClient client;

    public synchronized static OkHttpClient init() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(5000, TimeUnit.SECONDS)
                    .readTimeout(5000, TimeUnit.SECONDS)
                    .writeTimeout(5000, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }


    public static void doGet(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}
