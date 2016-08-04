package com.ksyun.player.demo.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by liubohua on 16/7/18.
 */
public class NetbaseHelper extends SQLiteOpenHelper {
    //数据库名
    private final static String DATABSE_NAME = "Net";
    //版本号
    private final static int DATABASE_VERSION = 1;
    //表名
    public final static String DATABSE_TABLE = "history";

    //创建表sql语句
    private final static String DATABASE_CREATE = "create table " + DATABSE_TABLE + " (_id integer primary key autoincrement,"
            + "path text not null,created time not null);";

    public NetbaseHelper(Context context)  {
        super(context, DATABSE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABSE_TABLE);
        onCreate(db);
    }
}
