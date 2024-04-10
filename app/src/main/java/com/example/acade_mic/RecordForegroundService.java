package com.example.acade_mic;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.example.acade_mic.model.AudioRecord;

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
    public MediaRecorder recorder;

    public String path = "";
    public String fileName = "";
    public Timer timer;
    String currentTime = "00:00.00";
    private final IBinder mBinder = new LocalBinder();

    private final int FROM_WIDGET = 0;
    private final int FROM_ACTIVITY = 1;

    public boolean isRecording = false;
    public boolean isPaused = false;
    long startTimeMillis = 0;

    long totalTimeMillis = 0;

    long pauseTimeMillis = 0;

    long totalPauseTime = 0;

    public long AlarmStopRecordingMillis = 0;

    public boolean isAlarmRecording = false;

    public String format(long duration) {
        long millis = duration % 1000;
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        long hours = (duration / (1000 * 60 * 60));

        String formatted;
        if (hours > 0)
            formatted = String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, millis / 10);
        else
            formatted = String.format("%02d:%02d.%02d", minutes, seconds, millis / 10);

        return formatted;
    }

    @Override
    public void onTimerTick(String duration) {
        if(isRecording && !isPaused){
            long currentTimeMillis = System.currentTimeMillis();
            totalTimeMillis = currentTimeMillis - startTimeMillis - totalPauseTime;
            duration = format(totalTimeMillis);
            currentTime = duration;
            updateNotification(this, duration.split("//.")[0]);
            if(isAlarmRecording && currentTimeMillis >= AlarmStopRecordingMillis){
                stop(true);
            }
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
        if (intent != null && intent.getStringExtra("message") != null && intent.getStringExtra("message").equals("PLAY")) {
            if (isRecording) {
                resume();
                System.out.println("RESUME HERE");
            } else {
                start();
                System.out.println("START HERE");
            }
        }
        if (intent != null && intent.getStringExtra("message") != null && intent.getStringExtra("message").equals("PAUSE")) {
            pause();
            System.out.println("PAUSE HERE");
        }
        if(intent != null && intent.getAction() != null && intent.getAction().equals("START_ALARM")){
            long totalDuration = intent.getLongExtra("totalDuration", 0L);
            System.out.println("ALARM START and ends in: " + totalDuration);
            if(isRecording == true){
                stop(false);
            }
            AlarmStopRecordingMillis = totalDuration + System.currentTimeMillis();
            isAlarmRecording = true;
            start();
        }
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

        Intent intent2 = new Intent(getBaseContext(), RecorderWidget.class);
        intent2.setAction("PLAY_BUTTON_SWITCH");
        getBaseContext().sendBroadcast(intent2);
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
        Thread thread = new Thread(()->{
            while(true){
                try {
                    Thread.sleep(500);
                    if(isRecording == false && isPaused == false){
                        Intent widgetIntent = new Intent(getBaseContext(), RecorderWidget.class);
                        widgetIntent.setAction("TIME_UPDATE");
                        widgetIntent.putExtra("message", "00:00");
                        Intent intent = new Intent(getBaseContext(), RecorderWidget.class);
                        intent.setAction("PAUSE_BUTTON_SWITCH");
                        getBaseContext().sendBroadcast(intent);
                        getBaseContext().sendBroadcast(widgetIntent);
                    }else if(isRecording == true && isPaused == true){
                        Intent widgetIntent = new Intent(getBaseContext(), RecorderWidget.class);
                        widgetIntent.setAction("TIME_PAUSED");
                        widgetIntent.putExtra("message", currentTime);
                        getBaseContext().sendBroadcast(widgetIntent);

                        Intent intent = new Intent(getBaseContext(), RecorderWidget.class);
                        intent.setAction("PAUSE_BUTTON_SWITCH");
                        getBaseContext().sendBroadcast(intent);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
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
        Intent intent = new Intent(getBaseContext(), RecorderWidget.class);
        intent.setAction("PAUSE_BUTTON");
        getBaseContext().sendBroadcast(intent);
        getBaseContext().sendBroadcast(widgetIntent);
    }

    public void startFromActivity(String thePath, String theFileName) {
        Intent intent = new Intent(getBaseContext(), RecorderWidget.class);
        intent.setAction("PLAY_BUTTON_SWITCH");
        getBaseContext().sendBroadcast(intent);

        timer = new Timer(this);
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        isRecording = true;
        isPaused = false;
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
        startTimeMillis = System.currentTimeMillis();
        timer.start();
    }

    public void start() {
        Intent intent = new Intent(getBaseContext(), RecorderWidget.class);
        intent.setAction("PLAY_BUTTON_SWITCH");
        getBaseContext().sendBroadcast(intent);
        timer = new Timer(this);
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        isRecording = true;
        isPaused = false;

        if (getExternalFilesDir(null) != null) {
            path = getExternalFilesDir(null).getAbsolutePath() + "/";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", Locale.ENGLISH);
        String date = sdf.format(new Date());
        fileName = "recording_" + date + ".mp3";
        if(isAlarmRecording){
            fileName = "alarm_" + fileName;
        }
        isRecording = true;
        isPaused = false;

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
        startTimeMillis = System.currentTimeMillis();
        totalPauseTime = 0;
        totalTimeMillis = 0;
        timer.start();
    }

    public void pause() {
        System.out.println("SERVICE PAUSED");
        recorder.pause();
        pauseTimeMillis = System.currentTimeMillis();
        timer.pause();
        isPaused = true;
        Intent widgetIntent = new Intent(getBaseContext(), RecorderWidget.class);
        widgetIntent.setAction("TIME_PAUSED");
        widgetIntent.putExtra("message", currentTime);
        getBaseContext().sendBroadcast(widgetIntent);

        Intent intent = new Intent(getBaseContext(), RecorderWidget.class);
        intent.setAction("PAUSE_BUTTON_SWITCH");
        getBaseContext().sendBroadcast(intent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    public void resume() {
        System.out.println("SERVICE RESUMED");

        isPaused = false;
        recorder.resume();
        totalPauseTime += System.currentTimeMillis() - pauseTimeMillis;
        timer.start();
        Intent intent = new Intent(getBaseContext(), RecorderWidget.class);
        intent.setAction("PLAY_BUTTON_SWITCH");
        getBaseContext().sendBroadcast(intent);
    }

    public void stop(boolean isAlarm) {
        isPaused = false;
        isRecording = false;
        totalTimeMillis = 0;
        totalPauseTime = 0;
        Intent widgetIntent = new Intent(getBaseContext(), RecorderWidget.class);
        widgetIntent.setAction("TIME_UPDATE");
        widgetIntent.putExtra("message", "00:00");
        getBaseContext().sendBroadcast(widgetIntent);
        Intent intent = new Intent(getBaseContext(), RecorderWidget.class);
        intent.setAction("PAUSE_BUTTON_SWITCH");
        getBaseContext().sendBroadcast(intent);
        recorder.stop();
        timer.stop();
        recorder.release();
        recorder = null;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        if(isAlarm){
            AppDatabase db = Room.databaseBuilder(
                    getApplicationContext(),
                    AppDatabase.class,
                    "audioRecords"
            ).build();

            AudioRecord record = new AudioRecord("alarm_" + fileName, path + fileName,new Date().getTime(), currentTime, path + fileName);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    db.audioRecordDao().insert(record);
                }
            }).start();
            isAlarmRecording = false;
            AlarmStopRecordingMillis = 0;
        }
    }
}
