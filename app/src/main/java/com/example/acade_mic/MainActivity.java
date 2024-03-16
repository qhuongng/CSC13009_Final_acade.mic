package com.example.acade_mic;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
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

    private Timer timer;
    private TextView tvTimer;
    private WaveformView waveformView;
    private Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_CODE);
        }
        timer= new Timer(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tvTimer = findViewById(R.id.tvTimer);
        waveformView = findViewById(R.id.waveformView);
        btnRec = (ImageButton) findViewById(R.id.btnRec);
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
            Toast.makeText(this, "List btn", Toast.LENGTH_SHORT).show();
        });

        btnDel = (ImageButton)findViewById(R.id.btnDel);
        btnDel.setOnClickListener((View v) ->{
            stopRec();

            Toast.makeText(this, "Del btn", Toast.LENGTH_SHORT).show();
        });

        btnOk = (ImageButton)findViewById(R.id.btnOk);
        btnOk.setOnClickListener((View v) -> {
            stopRec();
            Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show();
        });

        btnDel.setClickable(false);
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

        if (getExternalCacheDir() != null) {
            path = getExternalCacheDir().getAbsolutePath() + "/";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.ENGLISH);
        String date = sdf.format(new Date());
        fileName = path + "recording_" + date + ".mp3";

        recorder.setOutputFile(fileName);

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
        btnDel.setImageResource(R.drawable.ic_delete);

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
        isPaused = false;
        isRecording = false;

        btnRecList.setVisibility(View.VISIBLE);
        btnOk.setVisibility(View.GONE);

        btnDel.setClickable(false);
        btnDel.setImageResource(R.drawable.ic_delete);

        btnRec.setImageResource(R.drawable.ic_rec);
        tvTimer.setText("00:00:00");
        amplitudes = waveformView.clear();
    }
    @Override
    public void onTimerTick(String duration) {
        tvTimer.setText(duration);
        waveformView.addAmplitude((float) recorder.getMaxAmplitude());
    }
}