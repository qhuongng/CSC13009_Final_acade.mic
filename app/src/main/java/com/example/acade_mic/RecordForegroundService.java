package com.example.acade_mic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordForegroundService extends Service {
    public String filePath;
    public MediaRecorder recorder;
    public String path = "";
    public String fileName = "";
    public Timer timer;
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
        return START_STICKY;
    }

    public void start(String thePath, String theFileName){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        path = thePath;
        fileName = theFileName;

        try {
            recorder.setOutputFile(new FileOutputStream(path + fileName).getFD());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
    }

    public void pause(){
        recorder.pause();
        System.out.println("Service is paused" + filePath);
    }

    public void resume(){
        recorder.resume();
        System.out.println("Service is resumed" + filePath);
    }

    public void stop(){
        recorder.stop();
        recorder.release();
        recorder = null;
        System.out.println("Service is stoppped" + filePath);
    }
}
