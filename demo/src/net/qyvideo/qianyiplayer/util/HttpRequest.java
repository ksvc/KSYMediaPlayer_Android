package net.qyvideo.qianyiplayer.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by QianYi-Xin on 2015/6/2.
 */
public class HttpRequest extends Thread{
    private String reqeustUrl = "http://120.132.71.196:8958/android_demo?method=query";
    private AsyncCallback mCallback;

    public HttpRequest(AsyncCallback callback){
        mCallback = callback;
    }

    public HttpRequest(Runnable runnable) {
        super(runnable);
    }

    @Override
    public void run() {
        executeGetRequest();
    }

    private void executeGetRequest() {
        BufferedReader reader = null;

        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(reqeustUrl);

        try {
            HttpResponse response = client.execute(httpGet);
            if(response.getStatusLine().getStatusCode() == 200)
            {
                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer buffer = new StringBuffer("");
                String line = null;
                while((line = reader.readLine()) != null)
                    buffer.append(line);

                if(mCallback != null)
                    mCallback.onDataCallback(buffer.toString());
            }
            else
            {
                if(mCallback != null)
                    mCallback.onErrorCallback(response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
