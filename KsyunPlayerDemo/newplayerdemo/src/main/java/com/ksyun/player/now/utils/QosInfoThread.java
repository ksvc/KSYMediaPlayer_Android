package com.ksyun.player.now.utils;

import android.content.Context;
import android.os.Debug;
import android.os.Handler;

import com.ksyun.player.now.activity.VodDisplayActivity;
import com.ksyun.player.now.bean.QosBean;

import java.util.concurrent.TimeUnit;


public class QosInfoThread extends Thread {
    private Context mContext;
    private Handler mHandler;
    private CpuInfo mCpuStats;
    private Debug.MemoryInfo mi;
    private QosBean mQosObject;
    private String mPackageName;

    private boolean mRunning;
    private boolean mPausing;

    public QosInfoThread(Context context, Handler handler) {
        mHandler = handler;
        mCpuStats = new CpuInfo();
        mi = new Debug.MemoryInfo();
        mRunning = true;
        mQosObject = new QosBean();
        if(context != null)
            mPackageName = context.getPackageName();
    }

    @Override
    public void run() {
        while(mRunning) {

            while (mPausing && mRunning) {
                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mCpuStats.parseTopResults(mPackageName);

            Debug.getMemoryInfo(mi);

            if(mHandler != null) {
                mQosObject.cpuUsage = mCpuStats.getProcessCpuUsage();
                mQosObject.pss = mi.getTotalPss();
                mQosObject.vss = mi.getTotalPrivateDirty();
                mHandler.obtainMessage(VodDisplayActivity.UPDATE_QOSMESS, mQosObject).sendToTarget();
            }
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        mRunning = false;
    }

    public void resume2() {
        mPausing = false;
    }

    public void pause() {
        mPausing = true;
    }
}
