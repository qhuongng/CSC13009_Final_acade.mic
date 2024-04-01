package com.example.acade_mic;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class RecordForegroundService extends Service {
    String filePath;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        RecordForegroundService getService() {
            return RecordForegroundService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        filePath = intent.getStringExtra("filePath");
        System.out.println("Service created " + filePath);
        return START_STICKY;
    }

    public void start(){
        System.out.println("Service is started" + filePath);
    }

    public void pause(){
        System.out.println("Service is paused" + filePath);
    }
}
