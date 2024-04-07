package com.example.acade_mic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class AudioPlayerActivity extends AppCompatActivity implements OnItemClickListener, AsyncAudioTranscriptor.TranscriptionCallback {
    private MediaPlayer mediaPlayer;
    private MaterialToolbar toolbar;
    private TextView tvFilename;
    private TextView tvTrackProgress;
    private TextView tvTrackDuration;
    private TextView transcriptTxt;

    private ImageButton btnTranscribe;
    private ImageButton btnPlay;
    private ImageButton btnBackward;
    private ImageButton btnForward;
    private ImageButton btnLoop;
    private Chip speedChip;
    private SeekBar seekBar;
    private Runnable runnable;
    private Handler handler;
    private boolean checkLoop;
    private ArrayList<Bookmark> bookmarks;
    private AppDatabase db;
    private BookmarkAdapter bAdapter;
    ImageButton flatBtn;

    private SpeechCredentialsProvider credentialsProvider;

    private final long delay = 100L;
    private final int jumvalue = 5000;
    private float playBackSpeed = 1.0f;

    // audio file's id
    private int id;

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

        credentialsProvider = new SpeechCredentialsProvider(this);

        mediaPlayer = new MediaPlayer();
        bookmarks = new ArrayList<Bookmark>();
        String filePath = getIntent().getStringExtra("filepath");
        String fileName = getIntent().getStringExtra("filename");
        id =  getIntent().getIntExtra("id",0);

        toolbar = findViewById(R.id.toolBar);
        tvFilename = findViewById(R.id.tvFilename);
        tvTrackProgress = findViewById(R.id.tvTrackProgess);
        tvTrackDuration = findViewById(R.id.tvTrackDuration);
        btnBackward = findViewById(R.id.btnBackward);
        btnForward = findViewById(R.id.btnForward);
        btnLoop = findViewById(R.id.btnLoop);
        btnPlay = findViewById(R.id.btnPlay);
        speedChip = findViewById(R.id.chip);
        seekBar = findViewById(R.id.seekBar);

        btnTranscribe = findViewById(R.id.btnTranscribe);
        transcriptTxt = findViewById(R.id.transcriptTxt);
        fetchTranscript();

        bookmarks = new ArrayList<>();
        db = AppDatabase.getInstance(this);
        bAdapter = new BookmarkAdapter(bookmarks,this,this);
        RecyclerView recyclerView = findViewById(R.id.bookmarkRecyclerView);
        recyclerView.setAdapter(bAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchAll(id);
        //
        flatBtn = findViewById(R.id.btnBookmark);
        flatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int check = 0;
                for(Bookmark bm : bookmarks){
                    if(dateFormat(bm.getPosition()).equals(dateFormat(mediaPlayer.getCurrentPosition()))) check++;
                }
                if(check == 0){
                    mediaPlayer.pause();
                    // setup for note
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AudioPlayerActivity.this);
                    View popupView = getLayoutInflater().inflate(R.layout.popup_insert_note, null);
                    EditText editTextNote = popupView.findViewById(R.id.editTextNote);

                    ImageButton saveBtn = popupView.findViewById(R.id.btnSaveNote);
                    ImageButton cancelBtn = popupView.findViewById(R.id.btnCancel);
                    alertDialog.setView(popupView);
                    AlertDialog dialog = alertDialog.create();
                    dialog.show();
                    final boolean[] saveNote = {false};
                    saveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String note = editTextNote.getText().toString().trim();
                            if(!note.isEmpty()){
                                Bookmark newBm = new Bookmark(id,mediaPlayer.getCurrentPosition(), note);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        db.bookmarkDao().insert(newBm);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                bookmarks.add(newBm);
                                                bAdapter.notifyDataSetChanged();
                                                saveNote[0] = true;
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                }).start();
                                recyclerView.smoothScrollToPosition(bookmarks.size());

                                mediaPlayer.start();
                            }  else {
                            // Người dùng không nhập ghi chú, bạn có thể xử lý ở đây
                            Toast.makeText(AudioPlayerActivity.this, "Please enter a note", Toast.LENGTH_SHORT).show();
                                mediaPlayer.start();
                            }
                        }
                    });
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            String note = editTextNote.getText().toString().trim();
                            if (!note.isEmpty() && !saveNote[0]) {
                                Bookmark newBm = new Bookmark(id, mediaPlayer.getCurrentPosition(),note);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        db.bookmarkDao().insert(newBm);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                bookmarks.add(newBm);
                                                bAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }).start();
                                recyclerView.smoothScrollToPosition(bookmarks.size());
                            }
                            mediaPlayer.start();
                        }
                    });
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss(); // Đóng dialog mà không lưu ghi chú
                            mediaPlayer.start();
                        }
                    });
                    //
                }
            }
        });

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
                mediaPlayer.seekTo(0);
                if(!checkLoop){
                    btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_circle, getTheme()));
                    handler.removeCallbacks(runnable);
                } else {
                    mediaPlayer.start();
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
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                    mediaPlayer.start();
                }
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

        btnTranscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!transcriptTxt.getText().equals(String.valueOf(R.string.transcript_placeholder))) {
                    transcriptTxt.setText(R.string.transcript_executing);
                    transcribeAudio(filePath);
                }
            }
        });
    }

    public void transcribeAudio(String filePath) {
        new AsyncAudioTranscriptor(credentialsProvider, this).execute(filePath);
    }

    @Override
    public void onTranscriptionCompleted(String transcript) {
        // Update UI with the transcript
        transcriptTxt.setText(transcript);

        // save the transcript to the database for future reference
        TranscriptionFile newTranscript = new TranscriptionFile(id, transcript);
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.transcriptionFileDao().insert(newTranscript);
            }
        }).start();
    }

    @Override
    public void onTranscriptionFailed() {
        transcriptTxt.setText(R.string.transcript_failure);
    }

    @Override
    public void onItemClickListener(int position) {
        try {
            Bookmark bookmark = bookmarks.get(position);
            if(mediaPlayer.isPlaying()){
                mediaPlayer.seekTo(bookmark.getPosition());
            }
            else{
                btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_pause_circle, getTheme()));
                mediaPlayer.seekTo(bookmark.getPosition());
                mediaPlayer.start();
                handler.postDelayed(runnable, delay);
            }

        } catch (Exception exception){
            System.out.println(exception.fillInStackTrace());
        }
    }

    @Override
    public void onItemLongClickListener(int position) {
        Bookmark bookmark = bookmarks.get(position);
        mediaPlayer.seekTo(bookmark.getPosition());
        if(mediaPlayer.isPlaying()) mediaPlayer.pause();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AudioPlayerActivity.this);
        View popupView = getLayoutInflater().inflate(R.layout.show_note, null);
        TextView textNote = popupView.findViewById(R.id.noteTV);
        textNote.setText(bookmark.getNote());
        alertDialog.setView(popupView);
        AlertDialog dialog = alertDialog.create();
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                mediaPlayer.start();
            }
        });
    }
    private void fetchAll(int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                bookmarks.clear();
                List<Bookmark> queryResult = db.bookmarkDao().getBookmarksByAudioId(id);
                bookmarks.addAll(queryResult);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void fetchTranscript() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TranscriptionFile queryResult = db.transcriptionFileDao().getTranscript(id);

                if (queryResult != null) {
                    transcriptTxt.setText(queryResult.getContent());
                }
                else {
                    transcriptTxt.setText(R.string.transcript_placeholder);
                }
            }
        }).start();
    }
}