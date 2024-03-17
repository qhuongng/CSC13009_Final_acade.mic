package com.example.acade_mic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioPlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private ImageButton btnPlay;
    private ImageButton btnBackward;
    private ImageButton btnForward;
    private Chip speedChip;
    private SeekBar seekBar;

    boolean permissionGranted;
    int REQUEST_CODE = 200;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        mediaPlayer = new MediaPlayer();

        permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_MEDIA_AUDIO }, REQUEST_CODE);
        }

        String filePath = getIntent().getStringExtra("filepath");
        String fileName = getIntent().getStringExtra("filename");

        btnBackward = findViewById(R.id.btnBackward);
        btnForward = findViewById(R.id.btnForward);
        btnPlay = findViewById(R.id.btnPlay);
        speedChip = findViewById(R.id.chip);
        seekBar = findViewById(R.id.seekBar);

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream("/storage/emulated/0/Android/data/com.example.acade_mic/cache/recording_2024-03-17_12:21:04.mp3");
            if(fileInputStream != null){
                try {
                    mediaPlayer.setDataSource(getExternalFilesDir(null).getAbsolutePath() + "/" + fileName);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(this,"lỗi rồi má",Toast.LENGTH_LONG).show();
            }
        } catch (FileNotFoundException e) {

            throw new RuntimeException(e);
        }





        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_pause_circle, getTheme()));
                } else {
                    mediaPlayer.pause();
                    btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_circle, getTheme()));
                }
            }
        });
    }
}