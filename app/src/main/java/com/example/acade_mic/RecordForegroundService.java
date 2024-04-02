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
    String currentTime = "00:00";
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onTimerTick(String duration) {
        String[] parts = duration.split("\\.");
        if(parts[1].equals("00")){
            currentTime = parts[0];
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
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setContentIntent(resultPendingIntent);
        startForeground(9999, builder.build());

        Intent widgetIntent = new Intent(getBaseContext(), RecorderWidget.class);
        widgetIntent.setAction("TIME_UPDATE");
        widgetIntent.putExtra("message", updatedContent);
        getBaseContext().sendBroadcast(widgetIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(getBaseContext());
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pause)
                .setContentTitle("Acade.mic")
                .setSound(null)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)//to show content in lock screen
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);
        startForeground(NOTIFICATION_ID, mBuilder.build());
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.stop();
        recorder.stop();
        stopSelf();
        Intent widgetIntent = new Intent(getBaseContext(), RecorderWidget.class);
        widgetIntent.setAction("TIME_UPDATE");
        widgetIntent.putExtra("message", "00:00");
        getBaseContext().sendBroadcast(widgetIntent);
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

        Intent widgetIntent = new Intent(getBaseContext(), RecorderWidget.class);
        widgetIntent.setAction("TIME_PAUSED");
        widgetIntent.putExtra("message", currentTime);
        getBaseContext().sendBroadcast(widgetIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    public void resume(){
        recorder.resume();
        timer.start();
    }

    public void stop(){
        Intent widgetIntent = new Intent(getBaseContext(), RecorderWidget.class);
        widgetIntent.setAction("TIME_UPDATE");
        widgetIntent.putExtra("message", "00:00");
        getBaseContext().sendBroadcast(widgetIntent);
        recorder.stop();
        timer.stop();
        recorder.release();
        recorder = null;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }
}
