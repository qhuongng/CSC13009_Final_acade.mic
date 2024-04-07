package com.example.acade_mic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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

public class MainActivity extends AppCompatActivity implements Timer.OnTimerTickListener, ServiceConnection {
    public final int REQUEST_CODE = 200;
    public final int REQUEST_CODE2 = 201;
    public final int REQUEST_CODE3 = 202;
    public final int REQUEST_CODE4 = 203;


    public static boolean permissionGranted;
    public static boolean permissionGranted2;
    public static boolean permissionGranted3;
    public static boolean permissionGranted4;

    public ImageButton btnRec;
    public ImageButton btnDel;
    public ImageButton btnOk;
    public ImageButton btnRecList;
    public ArrayList<Float> amplitudes;
    public RecordForegroundService recordService = null;
    public BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    public Timer timer;
    public String duration;
    public AppDatabase db = null;
    public TextView tvTimer;
    public WaveformView waveformView;
    public String path;
    public String fileName;
    public Vibrator vibrator;
    public View bottomSheetBG;
    public TextInputEditText fileNameInput;
    public MaterialButton btnCancel;
    public MaterialButton btnSave;
    public ArrayList<AudioRecord> records;

    private final int FROM_WIDGET = 0;
    private final int FROM_ACTIVITY = 1;
    @Override
    protected void onResume() {
        super.onResume();
        if(recordService != null && recordService.isRecording && recordService.isPaused){
            path = recordService.path;
            fileName = recordService.fileName;
            pauseRec(FROM_WIDGET);
            syncPauseTime();
        }else if(recordService != null && recordService.isRecording && !recordService.isPaused){
            path = recordService.path;
            fileName = recordService.fileName;
            resumeRec(FROM_WIDGET);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Override
    protected void onDestroy() {
        if (db.isOpen()) {
            db.close();
        }

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        records = new ArrayList<AudioRecord>();
        setContentView(R.layout.activity_main);
        permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        permissionGranted2 = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED;
        permissionGranted3 = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_MICROPHONE) == PackageManager.PERMISSION_GRANTED;
        permissionGranted4 = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
        }

        if (!permissionGranted2) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, REQUEST_CODE2);
        }

        if (!permissionGranted3) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE_MICROPHONE}, REQUEST_CODE3);
        }

        if (!permissionGranted4) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_CODE4);
        }


        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "audioRecords"
        ).build();
        db = AppDatabase.getInstance(this);
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

        Intent recordIntent = new Intent(this, RecordForegroundService.class);
        bindService(recordIntent, this, BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(recordIntent);
        } else {
            startService(recordIntent);
        }


        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( recordService.isPaused) {
                    resumeRec(FROM_ACTIVITY);
                } else if (recordService.isRecording) {
                    pauseRec(FROM_ACTIVITY);
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
            Toast.makeText(this, "Delete record complete", Toast.LENGTH_SHORT).show();
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

        Button schedulerBtn = (Button) findViewById(R.id.schedulerBtn);
        schedulerBtn.setOnClickListener((View v)->{
            Intent startSchedulerIntent = new Intent(this, AlarmActivity.class);
            startActivity(startSchedulerIntent);
        });
    }

    public void save() {
        String newFileName = fileNameInput.getText().toString();

        File oldFile = new File(recordService.path + recordService.fileName);
        if (oldFile.exists()) {
            int check = 0;
            for (AudioRecord record:records) {
                if(newFileName.equals(record.getFilename())) check++;
            }
            if(check > 0) {
                Toast.makeText(this, "File name has been exists", Toast.LENGTH_SHORT).show();
            } else {
            String newFilePath = recordService.path + newFileName;
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
    public void fetchAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AudioRecord> queryResult = db.audioRecordDao().getAll();
                records.addAll(queryResult);
            }
        }).start();
    }
    public void dismiss() {
        bottomSheetBG.setVisibility(View.GONE);
        hideKeyBoard(fileNameInput);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }, 500);
    }

    public void hideKeyBoard(View v) {
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

    public void startRec() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
            return;
        }

        if (getExternalFilesDir(null) != null) {
            path = getExternalFilesDir(null).getAbsolutePath() + "/";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.ENGLISH);
        String date = sdf.format(new Date());
        fileName = "recording_" + date + ".mp3";
        recordService.startFromActivity(path, fileName);
        timer = new Timer(this);
        timer.start();

        // change the button
        btnRec.setImageResource(R.drawable.ic_pause);
        btnRec.setBackgroundResource(R.drawable.ic_stop_ripple);

        btnDel.setClickable(true);

        btnRecList.setVisibility(View.GONE);
        btnOk.setVisibility(View.VISIBLE);
    }

    public void resumeRec(int from) {
        btnDel.setClickable(true);

        btnRecList.setVisibility(View.GONE);
        btnOk.setVisibility(View.VISIBLE);
        btnDel.setClickable(true);
        if(from != FROM_WIDGET){
            recordService.resume();
        }
        timer.start();
        // change the button
        btnRec.setImageResource(R.drawable.ic_pause);
        btnRec.setBackgroundResource(R.drawable.ic_stop_ripple);
    }

    public void pauseRec(int from) {
        if(from != FROM_WIDGET){
            recordService.pause();
        }

        btnRecList.setVisibility(View.GONE);
        btnOk.setVisibility(View.VISIBLE);

        btnDel.setClickable(true);
        timer.pause();
        // change the button
        btnRec.setImageResource(R.drawable.ic_rec);
        btnRec.setBackgroundResource(R.drawable.ic_record_ripple);
    }

    public void stopRec() {
        timer.stop();

        recordService.stop(false);

        btnRecList.setVisibility(View.VISIBLE);
        btnOk.setVisibility(View.GONE);

        btnDel.setClickable(false);

        btnRec.setImageResource(R.drawable.ic_rec);
        tvTimer.setText("00:00:00");
        amplitudes = waveformView.clear();
    }

    public void syncPauseTime(){
        tvTimer.setText(recordService.currentTime);
        this.duration = recordService.currentTime.substring(0, recordService.currentTime.length() - 3);
        waveformView.addAmplitude((float) recordService.recorder.getMaxAmplitude());
    }

    @Override
    public void onTimerTick(String duration) {
        if(recordService != null && recordService.isRecording && !recordService.isPaused){
            tvTimer.setText(recordService.currentTime);
            this.duration = recordService.currentTime.substring(0, recordService.currentTime.length() - 3);
            waveformView.addAmplitude((float) recordService.recorder.getMaxAmplitude());
        }else{
            tvTimer.setText("00:00.00");
        }
    }

    boolean mBound = false;

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
        RecordForegroundService.LocalBinder binder = (RecordForegroundService.LocalBinder) service;
        recordService = binder.getService();
        mBound = true;
        if(recordService.isRecording){
            if(recordService.isPaused){
                path = recordService.path;
                fileName = recordService.fileName;
                pauseRec(FROM_WIDGET);
                syncPauseTime();
            }else{
                path = recordService.path;
                fileName = recordService.fileName;
                resumeRec(FROM_WIDGET);
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        boolean mBound = false;
        recordService = null;
    }

}