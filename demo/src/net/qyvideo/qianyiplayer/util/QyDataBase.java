package net.qyvideo.qianyiplayer.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xbc on 15/8/21.
 */
public class QyDataBase {

    private static final String DB_NAME = "history_url.db";

    private QyDataBaseHelper mDataHelper;

    public QyDataBase(Context context){
        mDataHelper = new QyDataBaseHelper(context, DB_NAME, null, 1);
    }

    public List<String> getPlayHistory()
    {
        if(mDataHelper != null)
            return mDataHelper.getPlayHistory();

        return null;
    }

    public long Insert(String url)
    {
        if(mDataHelper != null)
            return mDataHelper.Insert(url);

        return -1;
    }
}
