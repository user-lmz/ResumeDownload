package com.example.resumedownload;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "file.db";
    public static final int DB_VERSION = 1;
    public static final String SQL_COM_CREATE = "create table thread_info(_id integer primary key autoincrement," +
        "url text,start integer,end integer,now integer)";
    public static final String SQL_COM_DELETE = "drop table if exists thread_info";

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_COM_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_COM_DELETE);
    }
}
