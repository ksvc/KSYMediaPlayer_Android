package com.ksyun.player.demo.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xbc on 15/8/21.
 */
public class QyDataBaseHelper extends SQLiteOpenHelper {

    private static String HISTORY_ID = "history_id";
    private static String HISTORY_URL = "history_url";
    private static String TABLE_NAME = "history";

    public QyDataBaseHelper(Context context, String name, CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + "(" + HISTORY_ID + " INTEGER primary key autoincrement, " + HISTORY_URL + " varchar)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long Insert(String url)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        /* ContentValues */
        ContentValues cv = new ContentValues();
        cv.put(HISTORY_URL, url);

        Log.d("XBC", "Insert the url:" + url);

        return db.insert(TABLE_NAME, null, cv);
    }

    public List<String> getPlayHistory()
    {
        List<String> list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if(cursor != null)
        {
            cursor.moveToFirst();
            while (!cursor.isAfterLast() && (cursor.getString(cursor.getColumnIndex(HISTORY_URL)) != null))
            {
                list.add(cursor.getString(cursor.getColumnIndex(HISTORY_URL)));
                cursor.moveToNext();
            }
            cursor.close();
        }

        return list;
    }
}
