package com.example.acade_mic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.acade_mic.adapter.BookmarkAdapter;
import com.example.acade_mic.model.AudioRecord;
import com.example.acade_mic.model.Bookmark;
import com.example.acade_mic.model.ReviewAlarm;
import com.example.acade_mic.model.TranscriptionFile;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class AudioPlayerActivity extends AppCompatActivity implements OnItemClickListener, AsyncAudioTranscriptor.TranscriptionCallback {
    private MediaPlayer mediaPlayer;
    private MaterialToolbar toolbar;
    private View editbar;
    private ImageButton btnClose;
    private View bottomSheetBG;
    private ImageButton btnReviewAlarm;
    final Calendar myCalendar = Calendar.getInstance();
    private MaterialButton addBtn;
    private EditText startDate;
    private TextView tvFilename;
    private TextView tvTrackProgress;
    private TextView tvTrackDuration;
    private TextView transcriptTxt;

    private ImageButton btnTranscribe;
    private ImageButton btnSummarize;
    private ImageButton btnExport;
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
    private ImageButton flatBtn;
    private RecyclerView recyclerView;
    private BottomSheetBehavior<LinearLayout> addBookmarkBehavior;
    private BottomSheetBehavior<LinearLayout> addReviewRecordBehavior;
    private TextInputEditText addTextNote;
    private MaterialButton btnCancelAddNote;
    private MaterialButton btnSaveNote;


    private CloudCredentialsProvider credentialsProvider;
    private Spinner spLang;

    private final long delay = 100L;
    private final int jumvalue = 5000;
    private float playBackSpeed = 1.0f;

    // audio file's id
    private int id;
    public static boolean storagePermissionGranted;
    ReviewAlarm check = null;

    String[] langSpinner = { "Tiếng Việt (default)", "English (US)", "日本語", "한국어", "中文", "Français", "Español", "Deutsch" };
    String[] langCodes = { "vi", "en", "ja", "ko", "zh", "fr", "es", "de"};

    TranscriptionFile curTranscript;

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

        credentialsProvider = new CloudCredentialsProvider(this);

        mediaPlayer = new MediaPlayer();
        bookmarks = new ArrayList<Bookmark>();
        String filePath = getIntent().getStringExtra("filepath");
        String fileName = getIntent().getStringExtra("filename");
        id =  getIntent().getIntExtra("id",0);

        //int id = Integer.parseInt();
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
        btnSummarize = findViewById(R.id.btnSummarize);
        btnExport = findViewById(R.id.btnExport);
        transcriptTxt = findViewById(R.id.transcriptTxt);

        spLang = findViewById(R.id.spLang);

        bottomSheetBG =findViewById(R.id.bottomSheetBG);
        bottomSheetBG.setOnClickListener((View v) -> {
            dismiss();
            leaveAlarmReview();
        });
        // cài báo thức
        addReviewRecordBehavior = BottomSheetBehavior.from(findViewById(R.id.add_review_audio));
        addReviewRecordBehavior.setPeekHeight(0);
        addReviewRecordBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        // thêm bookmarl
        addBookmarkBehavior = BottomSheetBehavior.from((findViewById(R.id.popup_insert_note)));
        addBookmarkBehavior.setPeekHeight(0);
        addBookmarkBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnCancelAddNote = findViewById(R.id.btnCancelAddNote);
        addTextNote = findViewById(R.id.addTextNote);
        // khai báo các button cho add bookmark

        editbar = findViewById(R.id.editBar);
        startDate = findViewById(R.id.startDate);
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AudioPlayerActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        NumberPicker startHour = (NumberPicker) findViewById(R.id.startHour);
        String[] hourData = new String[24];
        for (int i = 0; i <= 23; i++) {
            hourData[i] = String.valueOf(i).length() < 2 ? ("0" + String.valueOf(i)) : String.valueOf(i);
        }
        startHour.setMinValue(0);
        startHour.setMaxValue(hourData.length - 1);
        startHour.setDisplayedValues(hourData);

        NumberPicker startMin = (NumberPicker) findViewById(R.id.startMin);
        String[] minData = new String[60];
        for (int i = 0; i <= 59; i++) {
            minData[i] = String.valueOf(i).length() < 2 ? ("0" + String.valueOf(i)) : String.valueOf(i);
        }
        startMin.setMinValue(0);
        startMin.setMaxValue(minData.length - 1);
        startMin.setDisplayedValues(minData);

        NumberPicker startSec = (NumberPicker) findViewById(R.id.startSec);
        String[] secData = new String[60];
        for (int i = 0; i <= 59; i++) {
            secData[i] = String.valueOf(i).length() < 2 ? ("0" + String.valueOf(i)) : String.valueOf(i);
        }
        startSec.setMinValue(0);
        startSec.setMaxValue(secData.length - 1);
        startSec.setDisplayedValues(secData);

        addBtn = findViewById(R.id.btnCreate);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startDateTime = startDate.getText().toString() + " " + formatTime(startHour.getValue(), startMin.getValue(), startSec.getValue());
                LocalDateTime localDateTime = LocalDateTime.parse(startDateTime,
                        DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss"));

                long startTimeMillis = localDateTime
                        .atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        check = db.reviewAlarmDao().getReviewAlarm(id);
                    }
                }).start();
                if(check!= null && check.getStartTime() == startTimeMillis){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(),"This Alarm have already exists",Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Intent intent = new Intent(getBaseContext(),ReviewAlarmService.class);
                    intent.putExtra("filename", fileName);
                    PendingIntent pendingIntent = PendingIntent.getService(getBaseContext(),0,intent,PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, startTimeMillis, pendingIntent);
                    ReviewAlarm rv = new ReviewAlarm(id, startTimeMillis);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            db.reviewAlarmDao().deleteAlarmByRecordId(id);
                            db.reviewAlarmDao().insert(rv);
                        }
                    }).start();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(),"Create ReviewAlarm success",Toast.LENGTH_SHORT).show();
                            leaveAlarmReview();
                        }
                    });
                }
            }
        });
        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveAlarmReview();
            }
        });

        btnReviewAlarm = findViewById(R.id.reviewAlarm);
        btnReviewAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ReviewAlarm reviewAlarm = db.reviewAlarmDao().getReviewAlarm(id);
                        if(reviewAlarm != null){
                            long time = reviewAlarm.getStartTime();
                            if(time + 120L*1000L > System.currentTimeMillis()){
                                Instant instant = Instant.ofEpochMilli(time);
                                LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
                                // Lấy giá trị ngày và giờ từ LocalDateTime
                                LocalDate checkDay = localDateTime.toLocalDate();
                                LocalTime checkTime = localDateTime.toLocalTime();
                                String month = (checkDay.getMonthValue() < 10) ? "0" + checkDay.getMonthValue() : String.valueOf(checkDay.getMonthValue());
                                String day = (checkDay.getDayOfMonth() < 10) ? "0" + checkDay.getDayOfMonth() : String.valueOf(checkDay.getDayOfMonth());
                                String date = day + "/" + month + "/" + checkDay.getYear();
                                startDate.setText(date);
                                startHour.setValue(checkTime.getHour());
                                startMin.setValue(checkTime.getMinute());
                                startSec.setValue(checkTime.getSecond());
                            } else {
                                db.reviewAlarmDao().deleteAlarmByRecordId(id);
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addReviewRecordBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                                bottomSheetBG.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }).start();
            }
        });

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, langSpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLang.setAdapter(adapter);
        spLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (curTranscript != null) {
                    transcriptTxt.setText(R.string.translate_executing);
                    fetchTranslatedTranscript(langCodes[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fetchTranscript();

        bookmarks = new ArrayList<>();
        db = AppDatabase.getInstance(this);
        bAdapter = new BookmarkAdapter(bookmarks,this,this);
        recyclerView = findViewById(R.id.bookmarkRecyclerView);
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
                    if(mediaPlayer.isPlaying()) mediaPlayer.pause();
                    addTextNote.setText("");
                    // setup for note
                    addBookmarkBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    bottomSheetBG.setVisibility(View.VISIBLE);
                }
            }
        });
        btnSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note = addTextNote.getText().toString().trim();
                if(!note.isEmpty()){
                    dismiss();
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
                                }
                            });
                        }
                    }).start();
                    recyclerView.smoothScrollToPosition(bookmarks.size());
                }  else {
                    // Người dùng không nhập ghi chú
                    Toast.makeText(AudioPlayerActivity.this, "Please enter a note", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnCancelAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Đóng dialog mà không lưu ghi chú
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
                if (curTranscript == null) {
                    transcriptTxt.setText(R.string.transcript_executing);
                    toggleTranscriptButtons(3);

                    transcribeAudio(filePath);
                }
            }
        });

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curTranscript != null) {
                    exportCurrentTranscript();
                }
            }
        });

        btnSummarize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curTranscript != null) {
                    runOnUiThread(ToastExecutingSummary);
                    exportTranscriptSummary();
                }
            }
        });
    }

    private String formatTime(int hour, int min, int sec) {
        String hourStr = String.valueOf(hour).length() < 2 ? ("0" + hour) : String.valueOf(hour);
        String minStr = String.valueOf(min).length() < 2 ? ("0" + min) : String.valueOf(min);
        String secStr = String.valueOf(sec).length() < 2 ? ("0" + sec) : String.valueOf(sec);
        return hourStr + ":" + minStr + ":" + secStr;
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        startDate.setText(dateFormat.format(myCalendar.getTime()));
    }

    private void leaveAlarmReview() {
        bottomSheetBG.setVisibility(View.GONE);
        addReviewRecordBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            addReviewRecordBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }, 500);
    }
    public void dismiss() {
        bottomSheetBG.setVisibility(View.GONE);
        hideKeyBoard(addTextNote);
        if(!mediaPlayer.isPlaying()) mediaPlayer.start();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            addBookmarkBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }, 50);
    }
    public void hideKeyBoard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private Runnable ToastExecutingSummary = new Runnable()
    {
        public void run()
        {
            Toast.makeText(AudioPlayerActivity.this, "Summarizing transcript...", Toast.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    public void onItemClickListener(int position) {
        try {
            Bookmark bookmark = bookmarks.get(position);
            if(mediaPlayer.isPlaying()){
                mediaPlayer.seekTo(bookmark.getPosition());
            }
            else {
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

    public void transcribeAudio(String filePath) {
        new AsyncAudioTranscriptor(credentialsProvider, this).execute(filePath);
    }

    @Override
    public void onTranscriptionCompleted(String transcript) {
        transcriptTxt.setText(transcript);

        // save the transcript to the database for future reference
        TranscriptionFile newTranscript = new TranscriptionFile(id, transcript, "vi");
        curTranscript = newTranscript;

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.transcriptionFileDao().insert(newTranscript);
            }
        }).start();

        toggleTranscriptButtons(2);
    }

    @Override
    public void onTranscriptionFailed() {
        transcriptTxt.setText(R.string.transcript_failure);
        toggleTranscriptButtons(1);
    }

    /**
     Toggles the buttons in the transcription view depending on the state of
     the audio file's transcript.

     States:
        1 - No transcript found
        2 - Found at least 1 transcript
        3 - Executing transcription
     */
    private void toggleTranscriptButtons(int state) {
        if (state == 1) {
            btnTranscribe.setEnabled(true);
            btnTranscribe.setImageTintList(ContextCompat.getColorStateList(AudioPlayerActivity.this, R.color.midnightGreen));

            btnSummarize.setEnabled(false);
            btnSummarize.setImageTintList(ContextCompat.getColorStateList(AudioPlayerActivity.this, R.color.disabledDarkGray));

            btnExport.setEnabled(false);
            btnExport.setImageTintList(ContextCompat.getColorStateList(AudioPlayerActivity.this, R.color.disabledDarkGray));

            spLang.setEnabled(false);
        }
        else if (state == 2) {
            btnTranscribe.setEnabled(false);
            btnTranscribe.setImageTintList(ContextCompat.getColorStateList(AudioPlayerActivity.this, R.color.disabledDarkGray));

            btnSummarize.setEnabled(true);
            btnSummarize.setImageTintList(ContextCompat.getColorStateList(AudioPlayerActivity.this, R.color.midnightGreen));

            btnExport.setEnabled(true);
            btnExport.setImageTintList(ContextCompat.getColorStateList(AudioPlayerActivity.this, R.color.midnightGreen));

            spLang.setEnabled(true);
        }
        else if (state == 3) {
            btnTranscribe.setEnabled(false);
            btnTranscribe.setImageTintList(ContextCompat.getColorStateList(AudioPlayerActivity.this, R.color.disabledDarkGray));

            btnSummarize.setEnabled(false);
            btnSummarize.setImageTintList(ContextCompat.getColorStateList(AudioPlayerActivity.this, R.color.disabledDarkGray));

            btnExport.setEnabled(false);
            btnExport.setImageTintList(ContextCompat.getColorStateList(AudioPlayerActivity.this, R.color.disabledDarkGray));

            spLang.setEnabled(false);
        }
    }

    private void fetchTranscript() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TranscriptionFile queryResult = db.transcriptionFileDao().getTranscript(id, "vi");

                if (queryResult != null) {
                    curTranscript = queryResult;
                    transcriptTxt.setText(curTranscript.getContent());
                    toggleTranscriptButtons(2);
                }
                else {
                    transcriptTxt.setText(R.string.transcript_placeholder);
                    toggleTranscriptButtons(1);
                }
            }
        }).start();
    }

    private Runnable ToastSuccessfulSummary = new Runnable()
    {
        public void run()
        {
            Toast.makeText(AudioPlayerActivity.this, "Summary saved to device's Downloads folder", Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private Runnable ToastFailedSummary = new Runnable()
    {
        public void run()
        {
            Toast.makeText(AudioPlayerActivity.this, "Failed to summarize transcript, please try again later", Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private void summaryExportHelper() {
        if (curTranscript.getSummary().length() > 0) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                storagePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                if (!storagePermissionGranted) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 203);
                }
            }

            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "acade.mic Summaries");

            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    System.out.println("mkdirs failed");
                    return;
                }
            }

            int num = 1;
            String fname = tvFilename.getText().toString().split("\\.")[0];
            File file = new File(directory, fname + "_summary_" + curTranscript.getLangCode() + ".txt");

            while(file.exists()) {
                file = new File(directory, fname + "_summary_" + curTranscript.getLangCode() + "_" + (num++) + ".txt");
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(curTranscript.getSummary().getBytes());
                this.runOnUiThread(ToastSuccessfulSummary);
            } catch (IOException e) {
                e.printStackTrace();
                this.runOnUiThread(ToastFailedSummary);
            }
        }
        else {
            summarizeCurrentTranscript();
        }
    }

    private void summarizeCurrentTranscript() {
        final boolean[] summarizeSuccess = {false};

        GenerativeModel gm = new GenerativeModel("gemini-pro", getString(R.string.cloud_api_key));
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText("Summarize this text into bullet points, making sure that: THE SUMMARY IS IN THE SAME LANGUAGE AS THE TEXT PROVIDED AND NOT THE PROMPT, the wording is concise and easy to understand, and no numerical data point is left out: " + curTranscript.getContent().replace("\"", "\\\"").replace("\n", "\\n"))
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String summary = result.getText();

                TranscriptionFile summarizedTranscript = new TranscriptionFile(id, curTranscript.getContent(), summary, curTranscript.getLangCode());
                curTranscript = summarizedTranscript;
                summaryExportHelper();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        db.transcriptionFileDao().update(summarizedTranscript);
                    }
                }).start();

                summarizeSuccess[0] = true;
                toggleTranscriptButtons(2);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, this.getMainExecutor());
    }

    private void exportTranscriptSummary() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (curTranscript.getSummary().length() > 0) {
                    summaryExportHelper();
                }
                else {
                    summarizeCurrentTranscript();
                }
            }
        }).start();
    }

    private void fetchTranslatedTranscript(String targetLang) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TranscriptionFile queryResult = db.transcriptionFileDao().getTranscript(id, targetLang);

                if (queryResult != null) {
                    curTranscript = queryResult;
                    transcriptTxt.setText(curTranscript.getContent());
                    toggleTranscriptButtons(2);
                }
                else {
                    translateTranscript(targetLang);
                }
            }
        }).start();
    }

    private void translateTranscript(String targetLang) {
        // call the translator
        AsyncTranslator translator = new AsyncTranslator();

        try {
            String result = translator.execute(getString(R.string.cloud_api_key), curTranscript.getContent(), curTranscript.getLangCode(), targetLang).get();

            if (!result.isEmpty()) {
                // Update UI element on the main UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        transcriptTxt.setText(Html.fromHtml(result).toString());
                    }
                });

                TranscriptionFile newTranscript = new TranscriptionFile(id, Html.fromHtml(result).toString(), targetLang);
                curTranscript = newTranscript;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        db.transcriptionFileDao().insert(newTranscript);
                    }
                }).start();

                toggleTranscriptButtons(2);
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        transcriptTxt.setText(R.string.translate_failure);
                    }
                });
                toggleTranscriptButtons(1);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void exportCurrentTranscript() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            storagePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (!storagePermissionGranted) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 203);
            }
        }

        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "acade.mic Transcripts");

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int num = 1;
        String fname = tvFilename.getText().toString().split("\\.")[0];
        File file = new File(directory, fname + "_transcript_" + curTranscript.getLangCode() + ".txt");

        while(file.exists()) {
            file = new File(directory, fname + "_transcript_" + curTranscript.getLangCode() + "_" + (num++) + ".txt");
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(curTranscript.getContent().getBytes());
            Toast.makeText(this, "Transcript saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }
}