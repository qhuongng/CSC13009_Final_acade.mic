package com.example.acade_mic;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.acade_mic.model.AudioRecord;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class EditAudioActivity extends AppCompatActivity {
    private ArrayList<AudioRecord> records;
    private AppDatabase db = null;
    private ImageButton btnPlay;
    private SeekBar seekBar;
    MediaPlayer mediaPlayer;
    private TextView tvFilename;
    private Button cutAudio;
    private Button mergeAudio;
    String filePath = null;
    String fileName = null;
    private TextView tvTrackProgress;
    private TextView tvTrackDuration;
    private Spinner spinner;
    private ArrayList<String> selectedItems = new ArrayList<>();

    private RecyclerView selectedItemsRecyclerView;
    private SelectedItemsAdapter selectedItemsAdapter;

    private boolean isCuttingAudio = false;

    // bottomSheet
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private TextInputEditText fileNameInput;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;
    private View bottomSheetBG;
    private float begin;
    private float end;
    private MaterialToolbar toolbar;


//    @Override
//    public void onBackPressed(){
//        super.onBackPressed();
//        mediaPlayer.stop();
//        mediaPlayer.release();
//        //handler.removeCallbacks(runnable);
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        mediaPlayer = new MediaPlayer();
        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "audioRecords"
        ).build();

        records = new ArrayList<AudioRecord>();
        fetchAll();

        ArrayList<String> filePaths = getIntent().getStringArrayListExtra("filepaths");
        ArrayList<String> filenames = getIntent().getStringArrayListExtra("filenames");

        HashMap<String, String> filenamePathMap = new HashMap<>();
        for (int i = 0; i < filePaths.size(); i++) {
            String filename = filenames.get(i);
            String filepath = filePaths.get(i);
            filenamePathMap.put(filename, filepath);
        }

        toolbar = findViewById(R.id.toolBarEdit);
        btnPlay = findViewById(R.id.btnPlay);
        seekBar = findViewById(R.id.seekBar);
        tvFilename = findViewById(R.id.tvFilename);
        cutAudio = findViewById(R.id.cutAudio);
        mergeAudio = findViewById(R.id.mergeAudio);
        tvTrackProgress = findViewById(R.id.tvTrackProgress);
        tvTrackDuration = findViewById(R.id.tvTrackDuration);
        RangeSlider rangeSlider = findViewById(R.id.rangeSlider);
        selectedItemsRecyclerView = findViewById(R.id.selectedItemsRecyclerView);
        selectedItemsAdapter = new SelectedItemsAdapter(selectedItems);
        selectedItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedItemsRecyclerView.setAdapter(selectedItemsAdapter);


        spinner = findViewById(R.id.fileSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filenames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                if (!selectedItems.contains(selectedItem)) {
                    selectedItems.add(selectedItem);
                    selectedItemsAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // element in bottomSheet
        fileNameInput = (TextInputEditText) findViewById(R.id.filenameInput);
        btnCancel = (MaterialButton) findViewById(R.id.btnCancel);
        btnSave = (MaterialButton) findViewById(R.id.btnSave);
        View bottomSheet = findViewById(R.id.bottomSheet);
        TextView textViewTitle = bottomSheet.findViewById(R.id.textViewTitle);



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


        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                if(fromUser){
                    begin = slider.getValues().get(0);
                    end = slider.getValues().get(1);

                    int durationInSeconds = (int) (end - begin);
                    String duration = String.format("%02d:%02d", (durationInSeconds % 3600) / 60, durationInSeconds % 60);
                    tvTrackDuration.setText(duration);
                }
            }
        });
        cutAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                bottomSheetBG.setVisibility(View.VISIBLE);
                fileNameInput.setText(fileName);
                textViewTitle.setText("Enter your filename");
            }
        });

        mergeAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItems.size() < 2) {
                    Toast.makeText(EditAudioActivity.this, "Select at least two audio files for merging", Toast.LENGTH_SHORT).show();
                    return;
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                bottomSheetBG.setVisibility(View.VISIBLE);
                fileNameInput.setText("MergedAudio.mp3");
                textViewTitle.setText("Enter your filename for merged audio");
            }
        });

        btnCancel.setOnClickListener((View v) -> {
            dismiss();
        });

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet));

        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetBG = (View) findViewById(R.id.bottomSheetBG);

        btnSave.setOnClickListener((View v) -> {
            dismiss();

            if (isCuttingAudio)
            {
                float start = rangeSlider.getValues().get(0);
                float end = rangeSlider.getValues().get(1);
                String newFileName = fileNameInput.getText().toString();
                cutAudioFile(newFileName, fileName, filePath, start, end);
            }
            else
            {
                String newFileName = fileNameInput.getText().toString();
                //mergeAudioFiles(selectedItems, newFileName, filenamePathMap);

                appendToExistingFile(selectedItems, newFileName, filenamePathMap);

            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the start and end values from the range slider
                float start = rangeSlider.getValues().get(0);
                float end = rangeSlider.getValues().get(1);

                // Play the audio file with the specified start and end values
                playAudio(filePath, start, end);
            }
        });

        if (filePaths != null && !filePaths.isEmpty() && filenames != null && !filenames.isEmpty()) {
            if (filePaths.size() > 1) {
                isCuttingAudio = false;
                cutAudio.setEnabled(false);
                tvFilename.setText("AUDIO MERGING");
                disableCut();
                disablePlay();
                rangeSlider.setEnabled(false);
                enableMerge();
            } else {
                isCuttingAudio = true;
                filePath = filePaths.get(0);
                fileName = filenames.get(0);

                tvFilename.setText(fileName);
                enableCut();
                enablePlay();
                rangeSlider.setEnabled(true);
                disableMerge();
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

                tvTrackProgress.setText("00:00");
                tvTrackDuration.setText(dateFormat(mediaPlayer.getDuration()));
                seekBar.setProgress(0);

                float duration = calculateAudioDuration(filePath);
                rangeSlider.setValueFrom(0);
                rangeSlider.setValueTo(duration);
                rangeSlider.setValues((float) 0, duration);
            }
        }
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

    private float calculateAudioDuration(String filePath){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            return mediaPlayer.getDuration()/1000;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void playAudio(String filePath, float startSeconds, float endSeconds) {
        try {
            // Reset the MediaPlayer before initializing it
            if (mediaPlayer != null && (mediaPlayer.isPlaying() || mediaPlayer.isLooping())) {
                mediaPlayer.reset();
            }
            // Set the data source and prepare the MediaPlayer
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Calculate start and end time in milliseconds
                    int startTimeMs = (int) (startSeconds * 1000);
                    int endTimeMs = (int) (endSeconds * 1000);

                    // Set the start time and duration for playback
                    mp.setLooping(false);
                    mp.seekTo(startTimeMs);
                    mp.start();
                    btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_pause_circle, getTheme()));
                    // Update the seekbar progress while playing
                    final int duration = endTimeMs - startTimeMs;
                    seekBar.setMax(duration);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int currentPosition = startTimeMs;
                            while (mediaPlayer != null && currentPosition <= endTimeMs) {
                                try {
                                    Thread.sleep(100);
                                    currentPosition = mediaPlayer.getCurrentPosition();
                                    if(currentPosition >= endTimeMs){
                                        mp.stop();
                                        btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_circle, getTheme()));
                                        mp.release();
                                    }
                                    seekBar.setProgress(currentPosition - startTimeMs);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
            });
            mediaPlayer.prepareAsync(); // Prepare asynchronously
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("WrongConstant")
    private void cutAudioFile(String newFileName, String oldFileName, String filePath, float startSeconds, float endSeconds){
        String[] path = filePath.split(oldFileName);
        String newFilePath = path[0] + newFileName;


        MediaExtractor extractor = new MediaExtractor();
        MediaMuxer muxer = null;

        long startTimeUs = (long)startSeconds * 1000000L;
        long endTimeUs = (long)endSeconds * 1000000L;

        try {
            extractor.setDataSource(filePath);

            int trackCount = extractor.getTrackCount();
            int audioTrackIndex = -1;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }

            if (audioTrackIndex < 0) {
                Log.e("AudioCutter", "Không tìm thấy track âm thanh trong tệp đầu vào");
                return;
            }

            extractor.selectTrack(audioTrackIndex);

            MediaFormat inputFormat = extractor.getTrackFormat(audioTrackIndex);
            muxer = new MediaMuxer(newFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int outputTrackIndex = muxer.addTrack(inputFormat);
            muxer.start();

            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            extractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            while (true) {
                int sampleSize = extractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }

                long presentationTimeUs = extractor.getSampleTime();
                if (presentationTimeUs > endTimeUs) {
                    break;
                }

                info.offset = 0;
                info.size = sampleSize;
                info.presentationTimeUs = presentationTimeUs;
                info.flags = extractor.getSampleFlags();

                muxer.writeSampleData(outputTrackIndex, buffer, info);
                extractor.advance();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (muxer != null) {
                try {
                    muxer.stop();
                    muxer.release();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
            extractor.release();
        }

        long timestamp = new Date().getTime();
        int durationInSeconds = (int)(endSeconds - startSeconds);

        String duration = String.format("%02d:%02d", (durationInSeconds % 3600) / 60, durationInSeconds % 60);
        saveCutAudio(newFilePath, newFileName, timestamp, duration);
    }

    private static final String TAG = "AudioMerger";
    @SuppressLint("WrongConstant")
    private void mergeAudioFiles(ArrayList<String> filenames, String newFileName, HashMap<String, String> filenamePathMap) {
        String oldFileName = filenames.get(0);
        String oldFilePath = filenamePathMap.get(oldFileName);
        String[] path = oldFilePath.split(oldFileName);
        String newFilePath = path[0] + newFileName;
        MediaMuxer muxer = null;

        Log.d("MERGE", "file1: " + filenames.get(0) + " " + "file2: " + filenames.get(1));
        try {
            muxer = new MediaMuxer(newFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            int numTracks = filenames.size();
            Log.d("MERGE", "numTracks: " + numTracks);
            ArrayList<MediaExtractor> extractors = new ArrayList<>(numTracks);
            // Initialize extractors and add tracks
            for (String inputFile : filenames) {
                String filePath = filenamePathMap.get(inputFile);
                MediaExtractor extractor = new MediaExtractor();
                extractor.setDataSource(filePath);

                Log.d(inputFile, "extractor: " + extractor);
                int trackIndex = selectTrack(extractor);
                Log.d(inputFile, "trackIndex: " + trackIndex);
                if (trackIndex >= 0) {
                    extractor.selectTrack(trackIndex);
                    MediaFormat format = extractor.getTrackFormat(trackIndex);
                    int muxerTrackIndex = muxer.addTrack(format);
                    Log.d(String.valueOf(trackIndex), "muxerTrackIndex: " + muxerTrackIndex);
                    if (muxerTrackIndex >= 0) {
                        extractors.add(extractor);
                    }
                }
            }

            // Start muxer after adding all tracks
            muxer.start();

            // Write sample data

            for (int i = 0; i < extractors.size(); i++) {
                MediaExtractor extractor = extractors.get(i);
                ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

                long presentationTimeUs = 0;
                while (true) {
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        break;
                    }

                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    info.offset = 0;
                    info.size = sampleSize;
                    info.presentationTimeUs = presentationTimeUs;
                    info.flags = extractor.getSampleFlags();

                    muxer.writeSampleData(extractors.indexOf(extractor), buffer, info);
                    Log.d(String.valueOf(extractor), String.valueOf(extractors.indexOf(extractor)));
                    extractor.advance();
                    presentationTimeUs = extractor.getSampleTime();
                    Log.d(String.valueOf(extractor), String.valueOf(presentationTimeUs));
                }
                extractor.release();
                buffer.clear();
            }

            // Stop and release muxer
            muxer.stop();
            muxer.release();

        } catch (IOException e) {
            Log.e(TAG, "Error merging audio files", e);
        }
    }

    private void appendToExistingFile(ArrayList<String> filenames, String newFileName, HashMap<String, String> filenamePathMap) {
        try {
            String oldFileName = filenames.get(0);
            String oldFilePath = filenamePathMap.get(oldFileName);
            String[] path = oldFilePath.split(oldFileName);
            String newFilePath = path[0] + newFileName;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < filenames.size(); i++) {
                File file = new File(filenamePathMap.get(filenames.get(i)));
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                fis.close();
            }
            baos.close();
            byte[] outputFileBytes = baos.toByteArray();
            FileOutputStream fos = new FileOutputStream(newFilePath);
            fos.write(outputFileBytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static int selectTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    private void saveCutAudio(String filePath, String newFileName, long timestamp, String duration) {
        File cutAudioFile = new File(filePath);

        if (cutAudioFile.exists()) {
            int check = 0;
            for (AudioRecord record : records) {
                if (newFileName.equals(record.getFilename())) {
                    check++;
                }
            }
            if (check > 0) {
                Toast.makeText(this, "File name already exists", Toast.LENGTH_SHORT).show();
            } else {

                AudioRecord record = new AudioRecord(newFileName, filePath, timestamp, duration, filePath);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        db.audioRecordDao().insert(record);
                    }
                }).start();
                Toast.makeText(this, "Audio file saved successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, GalleryActivity.class);
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "Cut audio file not found", Toast.LENGTH_SHORT).show();
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

    private void disableCut(){
        cutAudio.setEnabled(false);
        cutAudio.setClickable(false);
        cutAudio.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
    }
    private void enableCut(){
        cutAudio.setEnabled(true);
        cutAudio.setClickable(true);
        cutAudio.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.white, getTheme()));
    }
    private void disableMerge(){
        mergeAudio.setEnabled(false);
        mergeAudio.setClickable(false);
        mergeAudio.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
    }
    private void enableMerge(){
        mergeAudio.setEnabled(true);
        mergeAudio.setClickable(true);
        mergeAudio.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.white, getTheme()));
    }
    private void disablePlay(){
        btnPlay.setEnabled(false);
        btnPlay.setClickable(false);
        btnPlay.setImageTintList(ContextCompat.getColorStateList(this, R.color.darkGray));
    }
    private void enablePlay() {
        btnPlay.setEnabled(true);
        btnPlay.setClickable(true);
        btnPlay.setImageTintList(ContextCompat.getColorStateList(this, R.color.darkGray));
    }

}