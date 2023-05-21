package com.example.resumedownload;

import static com.example.resumedownload.DownloadService.ACTION_UPDATE;
import static com.example.resumedownload.DownloadService.DOWNLOAD_PATH;
import static com.example.resumedownload.DownloadService.FILE_NAME;

import android.content.Context;
import android.content.Intent;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DownLoadUtil {
    private Context context;
    private FileInfo fileInfo;
    private ThreadDAO threadDAO;
    private int finished = 0;
    public boolean isPause = false;

    public DownLoadUtil(Context context, FileInfo fileInfo) {
        this.context = context;
        this.fileInfo = fileInfo;
        threadDAO = new ThreadDAOImpl(context);
    }

    public void download() {
        List<FileInfo> lists = threadDAO.get(fileInfo.getUrl());
        FileInfo info = null;
        if (lists.size() == 0) {
            new MyThread(fileInfo).start();
        } else {
            // 中间开始的
            info = lists.get(0);
            new MyThread(info).start();
        }
    }

    private class MyThread extends Thread{
        private FileInfo info = null;

        public MyThread(FileInfo threadInfo) {
            this.info = threadInfo;
        }

        @Override
        public void run() {
            // 向数据库添加线程信息
            if (!threadDAO.isExits(info.getUrl())) {
                threadDAO.insert(info);
            }
            HttpURLConnection urlConnection = null;
            RandomAccessFile randomFile = null;
            InputStream inputStream = null;
            try {
                //连接网络
                URL url = new URL(fileInfo.getUrl());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("GET");
                // 设置下载位置
                int start = info.getStart() + info.getNow();
                urlConnection.setRequestProperty("Range", "bytes=" + start + "-" + info.getLength());

                // 设置文件写入位置
                // 创建一个本地文件
                File file = new File(DOWNLOAD_PATH, FILE_NAME);
                randomFile = new RandomAccessFile(file, "rwd");
                randomFile.seek(start);

                // 向Activity发送广播
                Intent intent = new Intent(ACTION_UPDATE);
                finished += info.getNow();

                // 下载任务
                // 读取数据
                if (urlConnection.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
                    // 获得文件流
                    inputStream = urlConnection.getInputStream();
                    byte[] buffer = new byte[512];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buffer)) != -1) {
                        // 写入文件
                        randomFile.write(buffer, 0, len);

                        // 把进度发给Activity
                        finished += len;

                        // 看时间间隔，时间间隔大于500ms再发
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            intent.putExtra("now", finished * 100 / fileInfo.getLength());
                            context.sendBroadcast(intent);
                        }

                        // 判断是否有暂停状态
                        if (isPause) {
                            threadDAO.update(info.getUrl(), finished);
                            return; // 结束循环
                        }

                        // 删除线程信息
                        threadDAO.delete(info.getUrl());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
                try {
                    if (randomFile != null)
                        randomFile.close();
                    if (inputStream != null)
                        inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
