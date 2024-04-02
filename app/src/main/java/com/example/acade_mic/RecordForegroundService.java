package com.example.acade_mic;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordForegroundService extends Service implements Timer.OnTimerTickListener {
    private static final String CHANNEL_ID = "myChannel";
    private static final int NOTIFICATION_ID = 169;
    private static final int NOTI_REQUEST_CODE = 269;
    public String filePath;
    public MediaRecorder recorder;
    public String path = "";
    public String fileName = "";
    public Timer timer;
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onTimerTick(String duration) {
        String[] parts = duration.split("\\.");
        if(parts[1].equals("00")){
            updateNotification(this, parts[0]);
        }
    }

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

    public void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void updateNotification(Context context, String updatedContent) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pause)
                .setContentTitle("Acade.mic")
                .setSound(null)
                .setSilent(true)
                .setContentText(updatedContent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)//to show content in lock screen
                .setOngoing(true)
                .setContentIntent(resultPendingIntent);


        startForeground(9999, builder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(getBaseContext());
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT |PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }

    public void start(String thePath, String theFileName){
        timer = new Timer(this);
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
        timer.start();
    }

    public void pause(){
        recorder.pause();
        timer.pause();
        System.out.println("Service is paused" + filePath);
    }

    public void resume(){
        recorder.resume();
        timer.start();
        System.out.println("Service is resumed" + filePath);
    }

    public void stop(){
        recorder.stop();
        timer.stop();
        recorder.release();
        recorder = null;
        System.out.println("Service is stoppped" + filePath);
    }
}
