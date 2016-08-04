package com.ksyun.player.demo.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Path;

import com.ksyun.player.demo.model.NetbaseHelper;

import java.util.Calendar;

/**
 * Created by liubohua on 16/7/18.
 */
public class NetDbAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_PATH = "path";
    public static final String KEY_CREATED = "created";

    private NetbaseHelper databaseHelper;
    private Context context;

    private SQLiteDatabase sqliteDatabase;

    public NetDbAdapter(Context context) {
        this.context = context;
    }

    //打开数据库
    public void open() {
        databaseHelper = new NetbaseHelper(context);
        try {
            sqliteDatabase = databaseHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            sqliteDatabase = databaseHelper.getReadableDatabase();
        }
    }

    public void close() {
        sqliteDatabase.close();
    }

    public long createDate(String path) {

        ContentValues content = new ContentValues();
        content.put(KEY_PATH, path);
        Calendar calendar = Calendar.getInstance();
        String created = calendar.get(Calendar.YEAR) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.DAY_OF_MONTH) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND);
        content.put(KEY_CREATED, created);

        return sqliteDatabase.insert(databaseHelper.DATABSE_TABLE, null, content);
    }

    public Cursor getAllData()
    {
        String[] searchResult =  {KEY_ROWID,KEY_PATH, KEY_CREATED};
        return sqliteDatabase.query(databaseHelper.DATABSE_TABLE,searchResult,null,null,null,null,KEY_CREATED + " DESC");
    }

    public boolean getData(String path){
        String[] searchResult =  {KEY_ROWID, KEY_CREATED,KEY_PATH};
        String whereString = KEY_PATH + "=" + "'"+ path + "'";

        Cursor mCursor = sqliteDatabase.query(true, databaseHelper.DATABSE_TABLE, searchResult, whereString, null, null, null, null, null);
        if(mCursor.getCount()>0){
            return true;
        }else{
            return false;

        }
    }

    public boolean updateData(String path){

        ContentValues content = new ContentValues();
        content.put(KEY_PATH, path);

        Calendar calendar = Calendar.getInstance();
        String created = calendar.get(Calendar.YEAR) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.DAY_OF_MONTH) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND);
        content.put(KEY_CREATED, created);
        String whereString = KEY_PATH + "=" + "'"+ path + "'";

        return sqliteDatabase.update(databaseHelper.DATABSE_TABLE, content, whereString, null)>0;
    }


}
