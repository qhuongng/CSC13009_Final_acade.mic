package com.example.acade_mic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Timer.OnTimerTickListener {
    private final int REQUEST_CODE = 200;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_CODE);
        }

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "audioRecords"
        ).build();

        timer= new Timer(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tvTimer = findViewById(R.id.tvTimer);
        waveformView = findViewById(R.id.waveformView);
        btnRec = (ImageButton) findViewById(R.id.btnRec);
        fileNameInput = (TextInputEditText) findViewById(R.id.filenameInput);
        // cancel saving file
        btnCancel = (MaterialButton) findViewById(R.id.btnCancel);
        btnSave = (MaterialButton) findViewById(R.id.btnSave);
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaused) {
                    resumeRec();
                }
                else if (isRecording) {
                    pauseRec();
                }
                else {
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

        btnDel = (ImageButton)findViewById(R.id.btnDel);
        btnDel.setOnClickListener((View v) ->{
            stopRec();
            Toast.makeText(this, "Del btn", Toast.LENGTH_SHORT).show();
        });

        btnOk = (ImageButton)findViewById(R.id.btnOk);
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

        btnSave.setOnClickListener((View v)->{
            dismiss();
            save();
        });

        bottomSheetBG.setOnClickListener((View v)->{
            // delete file
            //
            dismiss();
        });

    }

    private void save(){
        String newFileName = fileNameInput.getText().toString();

        File oldFile = new File(path + fileName);
        if(oldFile.exists()){
            String newFilePath = path + newFileName;
            long timestamp = new Date().getTime();
            File newFile = new File(newFilePath);
            if(oldFile.renameTo(newFile)){
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

    private void dismiss(){
        bottomSheetBG.setVisibility(View.GONE);
        hideKeyBoard(fileNameInput);

        new Handler(Looper.getMainLooper()).postDelayed(()->{
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }, 500);
    }

    private void hideKeyBoard(View v){
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
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_CODE);
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

    private void stopRec(){
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
    @Override
    public void onTimerTick(String duration) {
        tvTimer.setText(duration);
        this.duration = duration.substring(0, duration.length() - 3);
        waveformView.addAmplitude((float) recorder.getMaxAmplitude());
    }
}