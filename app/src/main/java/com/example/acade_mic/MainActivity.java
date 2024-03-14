package com.example.acade_mic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_CODE = 200;
    private boolean permissionGranted;
    private MediaRecorder recorder;
    private String path = "";
    private String fileName = "";
    private ImageButton btnRec;
    private boolean isRecording = false;
    private boolean isPaused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_CODE);
        }

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
            }
        });
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

        // change the button
        btnRec.setImageResource(R.drawable.ic_pause);
        btnRec.setBackgroundResource(R.drawable.ic_stop_ripple);
    }

    private void resumeRec() {
        recorder.resume();
        isPaused = false;

        // change the button
        btnRec.setImageResource(R.drawable.ic_pause);
        btnRec.setBackgroundResource(R.drawable.ic_stop_ripple);
    }

    private void pauseRec() {
        recorder.pause();
        isPaused = true;

        // change the button
        btnRec.setImageResource(R.drawable.ic_rec);
        btnRec.setBackgroundResource(R.drawable.ic_record_ripple);
    }
}