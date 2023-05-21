package com.example.resumedownload;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ThreadDAOImpl implements ThreadDAO{
    private DBHelper dbHelper = null;
    private static final String INSERT_SQL = "insert into thread_info(url,start,end,now) values(?,?,?,?)";
    private static final String DELETE_SQL = "delete from thread_info where url = ?";
    private static final String UPDATE_SQL = "update thread_info set now = ?  where url = ?";
    private static final String QUERY_SQL = "select * from thread_info where url = ?";

    public ThreadDAOImpl(Context context) {
        dbHelper = new DBHelper(context);
    }

    @Override
    public void insert(FileInfo info) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL(INSERT_SQL, new Object[]{info.getUrl(), info.getStart(),
            info.getLength(), info.getNow()});
        sqLiteDatabase.close();
    }

    @Override
    public void delete(String url) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL(DELETE_SQL, new Object[]{url});
        sqLiteDatabase.close();
    }

    @Override
    public void update(String url, int now) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL(UPDATE_SQL, new Object[]{now, url});
        sqLiteDatabase.close();
    }

    @SuppressLint("Range")
    @Override
    public List<FileInfo> get(String url) {
        List<FileInfo> list = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(QUERY_SQL, new String[]{url});
        while (cursor.moveToNext()) {
            FileInfo info = new FileInfo();
            info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            info.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            info.setLength(cursor.getInt(cursor.getColumnIndex("end")));
            info.setNow(cursor.getInt(cursor.getColumnIndex("now")));
            list.add(info);
        }
        cursor.close();
        sqLiteDatabase.close();
        return list;
    }

    @Override
    public boolean isExits(String url) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(QUERY_SQL, new String[]{url});
        boolean exits = cursor.moveToNext();
        cursor.close();
        sqLiteDatabase.close();
        return exits;
    }
}
