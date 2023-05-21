package com.example.resumedownload;

import static com.example.resumedownload.DownloadService.ACTION_UPDATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ProgressBar mProgressBar;
    private Button start;
    private Button stop;
    private String url = "https://mp-bef8678e-0f74-4985-b8a7-43982f366dac.cdn.bspapp.com/cloudstorage/3e0fd743-6a1b-4fa1-9601-f1cb7c8937d9.mp4";
    public static final String LOG_TAG = "lmz  ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setMax(100);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        final FileInfo info = new FileInfo(url, 0, 0, 0);
        Log.i(LOG_TAG, "FileInfo: " + info);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "start click");
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileUrl", info);
                startService(intent);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "stop click");
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileUrl", info);
                startService(intent);
            }
        });

        // 注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_UPDATE)) {
                int finish = intent.getIntExtra("now", 0);
                mProgressBar.setProgress(finish);
            }
        }
    };
}
