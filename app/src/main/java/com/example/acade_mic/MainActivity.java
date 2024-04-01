package com.example.acade_mic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Timer.OnTimerTickListener {
    private final int REQUEST_CODE = 200;
    private final int NOTI_REQUEST_CODE = 100;
    private static final String CHANNEL_ID = "my_channel_id";
    private static final int NOTIFICATION_ID = 1;
    private boolean permissionGranted;
    private MediaRecorder recorder;
    private String path = "";
    private String fileName = "";
    private ImageButton btnRec;
    private ImageButton btnDel;
    private ImageButton btnOk;
    private ImageButton btnRecList;
    private boolean isRecording = false;
    private boolean isPaused = false;
    private ArrayList<Float> amplitudes;

    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    private Timer timer;
    private String duration;
    private AppDatabase db = null;
    private TextView tvTimer;
    private WaveformView waveformView;
    private Vibrator vibrator;
    private View bottomSheetBG;
    private TextInputEditText fileNameInput;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;
    private ArrayList<AudioRecord> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        records = new ArrayList<AudioRecord>();
        setContentView(R.layout.activity_main);
        createNotificationChannel(this);
        permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
        }

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "audioRecords"
        ).build();

        timer = new Timer(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tvTimer = findViewById(R.id.tvTimer);
        waveformView = findViewById(R.id.waveformView);
        btnRec = (ImageButton) findViewById(R.id.btnRec);
        fileNameInput = (TextInputEditText) findViewById(R.id.filenameInput);
        // cancel saving file
        btnCancel = (MaterialButton) findViewById(R.id.btnCancel);
        btnSave = (MaterialButton) findViewById(R.id.btnSave);
        fetchAll();
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaused) {
                    resumeRec();
                } else if (isRecording) {
                    pauseRec();
                } else {
                    startRec();
                }
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        });

        btnRecList = findViewById(R.id.btnRecList);
        btnRecList.setOnClickListener((View v) -> {
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);

        });

        btnDel = (ImageButton) findViewById(R.id.btnDel);
        btnDel.setOnClickListener((View v) -> {
            stopRec();
            Toast.makeText(this, "Del btn", Toast.LENGTH_SHORT).show();
        });

        btnOk = (ImageButton) findViewById(R.id.btnOk);
        btnOk.setOnClickListener((View v) -> {
            stopRec();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            bottomSheetBG.setVisibility(View.VISIBLE);
            fileNameInput.setText(fileName);
        });

        btnDel.setClickable(false);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet));

        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetBG = (View) findViewById(R.id.bottomSheetBG);

        btnCancel.setOnClickListener((View v) -> {
            // delete file
            //
            dismiss();
        });

        btnSave.setOnClickListener((View v) -> {
            dismiss();
            save();
        });

        bottomSheetBG.setOnClickListener((View v) -> {
            // delete file
            //
            dismiss();
        });

    }

    private void save() {
        String newFileName = fileNameInput.getText().toString();

        File oldFile = new File(path + fileName);
        if (oldFile.exists()) {
            int check = 0;
            for (AudioRecord record:records) {
                if(newFileName.equals(record.getFilename())) check++;
            }
            if(check > 0) {
                Toast.makeText(this, "File name has been exists", Toast.LENGTH_SHORT).show();
            } else {
            String newFilePath = path + newFileName;
            long timestamp = new Date().getTime();
            File newFile = new File(newFilePath);
            if (oldFile.renameTo(newFile)) {
                AudioRecord record = new AudioRecord(newFileName, newFilePath, timestamp, duration, newFilePath);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        db.audioRecordDao().insert(record);
                    }
                }).start();
                Toast.makeText(this, "Save record file successfully", Toast.LENGTH_SHORT).show();
            }
            }
        }
    }
    private void fetchAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AudioRecord> queryResult = db.audioRecordDao().getAll();
                records.addAll(queryResult);
            }
        }).start();
    }
    private void dismiss() {
        bottomSheetBG.setVisibility(View.GONE);
        hideKeyBoard(fileNameInput);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }, 500);
    }

    private void hideKeyBoard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void startRec() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
            return;
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        if (getExternalFilesDir(null) != null) {
            path = getExternalFilesDir(null).getAbsolutePath() + "/";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.ENGLISH);
        String date = sdf.format(new Date());
        fileName = "recording_" + date + ".mp3";

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
        System.out.println("NOTI HEREEEEEEEEEEEEEEEE");
        showNotification(this, "Acade.mic", "Audio started recording");
        recorder.start();
        isRecording = true;
        isPaused = false;
        timer.start();

        // change the button
        btnRec.setImageResource(R.drawable.ic_pause);
        btnRec.setBackgroundResource(R.drawable.ic_stop_ripple);

        btnDel.setClickable(true);

        btnRecList.setVisibility(View.GONE);
        btnOk.setVisibility(View.VISIBLE);
    }

    private void resumeRec() {
        recorder.resume();
        isPaused = false;
        timer.start();
        // change the button
        btnRec.setImageResource(R.drawable.ic_pause);
        btnRec.setBackgroundResource(R.drawable.ic_stop_ripple);
    }

    private void pauseRec() {
        recorder.pause();
        isPaused = true;
        timer.pause();
        // change the button
        btnRec.setImageResource(R.drawable.ic_rec);
        btnRec.setBackgroundResource(R.drawable.ic_record_ripple);
    }

    private void stopRec() {
        timer.stop();

        recorder.stop();
        recorder.release();
        recorder = null;
        isPaused = false;
        isRecording = false;

        btnRecList.setVisibility(View.VISIBLE);
        btnOk.setVisibility(View.GONE);

        btnDel.setClickable(false);

        btnRec.setImageResource(R.drawable.ic_rec);
        tvTimer.setText("00:00:00");
        amplitudes = waveformView.clear();
    }


    public static void createNotificationChannel(Context context) {
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

    public void showNotification(Context context, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pause)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(null)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)//to show content in lock screen
                .setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTI_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    public void updateNotification(Context context, String updatedContent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pause)
                .setContentTitle("Acade.mic")
                .setSound(null)
                .setSilent(true)
                .setContentText(updatedContent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)//to show content in lock screen
                .setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTI_REQUEST_CODE);
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("PLAY_PAUSE_ACTION");
        PendingIntent pi = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pi);

        Intent buttonIntent = new Intent(context, MainActivity.class);
        PendingIntent buttonPendingIntent = PendingIntent.getActivity(context, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_play, "Play", buttonPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    @Override
    public void onTimerTick(String duration) {
        updateNotification(this, duration);
        tvTimer.setText(duration);
        this.duration = duration.substring(0, duration.length() - 3);
        waveformView.addAmplitude((float) recorder.getMaxAmplitude());
    }
}