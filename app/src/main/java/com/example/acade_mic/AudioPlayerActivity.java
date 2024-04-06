package com.example.acade_mic;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class AudioPlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private MaterialToolbar toolbar;
    private TextView tvFilename;
    private TextView tvTrackProgress;
    private TextView tvTrackDuration;
    private ImageButton btnPlay;
    private ImageButton btnBackward;
    private ImageButton btnForward;
    private ImageButton btnBookMark;
    private ImageButton btnLoop;
    private Chip speedChip;
    private SeekBar seekBar;
    private Runnable runnable;
    private Handler handler;
    private boolean checkLoop;
    private ArrayList<Bookmark> bookmarks;
    private AppDatabase db;


    private final long delay = 100L;
    private final int jumvalue = 5000;
    private float playBackSpeed = 1.0f;

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        mediaPlayer.stop();
        mediaPlayer.release();
        handler.removeCallbacks(runnable);
    }
    public String dateFormat(int duration) {
        int d = duration / 1000;
        int s = d % 60;
        int m = (d / 60) % 60;
        int h = (d - m * 60) / 360;

        NumberFormat f = new DecimalFormat("00");
        String str = m + ":" + f.format(s);
        if (h > 0) {
            str = h + ":" + str;
        }
        return str;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        mediaPlayer = new MediaPlayer();
        bookmarks = new ArrayList<Bookmark>();
        String filePath = getIntent().getStringExtra("filepath");
        String fileName = getIntent().getStringExtra("filename");

        toolbar = findViewById(R.id.toolBar);
        tvFilename = findViewById(R.id.tvFilename);
        tvTrackProgress = findViewById(R.id.tvTrackProgess);
        tvTrackDuration = findViewById(R.id.tvTrackDuration);
        btnBackward = findViewById(R.id.btnBackward);
        btnForward = findViewById(R.id.btnForward);
        btnBookMark = findViewById(R.id.btnBookmark);
        btnLoop = findViewById(R.id.btnLoop);
        btnPlay = findViewById(R.id.btnPlay);
        speedChip = findViewById(R.id.chip);
        seekBar = findViewById(R.id.seekBar);

        //setup for toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //tvFilename
        tvFilename.setText(fileName);

        FileInputStream fis;

        try {
            fis = new FileInputStream(filePath);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                tvTrackProgress.setText(dateFormat(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(runnable,delay);
            }
        };

        tvTrackDuration.setText(dateFormat(mediaPlayer.getDuration()));

        seekBar.setMax(mediaPlayer.getDuration());
        //thay đổi icon play khi phát xong
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(!checkLoop){
                    btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_circle, getTheme()));
                    handler.removeCallbacks(runnable);
                } else {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    handler.postDelayed(runnable, delay);
                }

            }
        });
        //tua thêm
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + jumvalue);
                seekBar.setProgress(seekBar.getProgress() + jumvalue);
            }
        });
        //lùi lại
        btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - jumvalue);
                seekBar.setProgress(seekBar.getProgress() - jumvalue);
            }
        });
        speedChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playBackSpeed != 2.0f)
                    playBackSpeed += 0.5f;
                else
                    playBackSpeed = 0.5f;
                PlaybackParams params = new PlaybackParams();
                params.setSpeed(playBackSpeed);
                mediaPlayer.setPlaybackParams(params);
                speedChip.setText("x " + playBackSpeed);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                    mediaPlayer.seekTo(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_pause_circle, getTheme()));
                    handler.postDelayed(runnable,0);
                } else {
                    mediaPlayer.pause();
                    btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_circle, getTheme()));
                    handler.removeCallbacks(runnable);
                }
            }
        });
        btnLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkLoop){
                    checkLoop = true;
                    btnLoop.setImageResource(R.drawable.ic_noloop);
                } else{
                    checkLoop = false;
                    btnLoop.setImageResource(R.drawable.ic_loop);
                }
            }
        });
    }
}