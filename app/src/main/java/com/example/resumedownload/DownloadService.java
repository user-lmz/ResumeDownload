package com.example.resumedownload;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {
    public static final String FILE_NAME = "test";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory()
        .getAbsolutePath() + "/download/";
    public static final String LOG_TAG = "lmz  ";

    private DownLoadUtil downLoadUtil = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 判断Intent的值，获得Activity传来的参数
        if (intent.getAction().equals(ACTION_START)) {
            Log.i(LOG_TAG, "ACTION_START");
            // 开始命令
            FileInfo info = (FileInfo) intent.getSerializableExtra("fileUrl");
            // 启动初始化线程
            new MyThread(info).start();
        } else if (intent.getAction().equals(ACTION_STOP)) {
            Log.i(LOG_TAG, "ACTION_STOP");
            // 停止命令
            FileInfo info = (FileInfo) intent.getSerializableExtra("fileUrl");
            if (downLoadUtil != null) {
                downLoadUtil.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 用于Service和MyThread之间的通信
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0:
                    FileInfo info = (FileInfo) msg.obj;
                    Log.i("test", "Handler" + info.toString());
                    // 启动一个下载任务
                    downLoadUtil = new DownLoadUtil(DownloadService.this, info);
                    downLoadUtil.download();
                    break;
            }
        }
    };

    private class MyThread extends Thread{
        private FileInfo fileInfo = null;
        public MyThread(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        public void run() {
            HttpURLConnection urlConnection = null;
            RandomAccessFile randomFile = null;
            try {
                //连接网络
                URL url = new URL(fileInfo.getUrl());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("GET");
                int length = -1;
                Log.i(LOG_TAG, "ResponseCode: " + urlConnection.getResponseCode());
                if (urlConnection.getResponseCode() == HttpStatus.SC_OK) {
                    //获得文件长度
                    length = urlConnection.getContentLength();
                    Log.i(LOG_TAG, "length: " + length);
                }
                if (length <= 0) {
                    return;
                }
                //本地文件
                File dir = new File(DOWNLOAD_PATH);
                Log.i("test", "Path" + DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, FILE_NAME);
                //可以在文件的任意位置进行操作
                randomFile = new RandomAccessFile(file, "rwd");
                //设置本地文件长度
                randomFile.setLength(length);
                //长度给fileInfo对象
                fileInfo.setLength(length);
                //传递给Service
                mHandler.obtainMessage(0, fileInfo).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
                try {
                    randomFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
