package com.example.acade_mic;


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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class EditAudioActivity extends AppCompatActivity {
    private ArrayList<AudioRecord> records;
    private AppDatabase db = null;
    private ImageButton btnPlay;
    private SeekBar seekBar;
    private TextView tvFilename;
    private Button cutAudio;

    // bottomSheet
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private TextInputEditText fileNameInput;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;
    private View bottomSheetBG;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "audioRecords"
        ).build();

        records = new ArrayList<AudioRecord>();
        fetchAll();

        String filePath = getIntent().getStringExtra("filepath");
        String fileName = getIntent().getStringExtra("filename");


        btnPlay = findViewById(R.id.btnPlay);
        seekBar = findViewById(R.id.seekBar);
        tvFilename = findViewById(R.id.tvFilename);
        cutAudio = findViewById(R.id.cutAudio);
        RangeSlider rangeSlider = findViewById(R.id.rangeSlider);
        tvFilename.setText(fileName);

        // element in bottomSheet
        fileNameInput = (TextInputEditText) findViewById(R.id.filenameInput);
        btnCancel = (MaterialButton) findViewById(R.id.btnCancel);
        btnSave = (MaterialButton) findViewById(R.id.btnSave);
        View bottomSheet = findViewById(R.id.bottomSheet);
        TextView textViewTitle = bottomSheet.findViewById(R.id.textViewTitle);


        float duration = calculateAudioDuration(filePath);
        rangeSlider.setValueFrom(0);
        rangeSlider.setValueTo(duration);
        rangeSlider.setValues((float) 0, duration);

        cutAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                bottomSheetBG.setVisibility(View.VISIBLE);
                fileNameInput.setText(fileName);
                textViewTitle.setText("Enter your filename");
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
            List<Float> rangeValues = rangeSlider.getValues();

            float[] rangeArray = new float[rangeValues.size()];
            for (int i = 0; i < rangeValues.size(); i++) {
                rangeArray[i] = rangeValues.get(i);
            }

            float start = rangeArray[0];
            float end = rangeArray[1];

            String newFileName = fileNameInput.getText().toString();

            cutAudioFile(newFileName, fileName, filePath, start, end);
        });
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

//    private void cutAudioFile(String oldFileName, String filePath, int startSeconds, int endSeconds) {
//        try {
//            String[] path = filePath.split(oldFileName);
//            String newFilePath = path[0] + "output.mp3";
//
//            File sourceFile = new File(filePath);
//            File newFile = new File(newFilePath);
//            FileInputStream fis1 = new FileInputStream(filePath);
//            MediaPlayer mediaPlayer = new MediaPlayer();
//            mediaPlayer.setDataSource(fis1.getFD());
//            mediaPlayer.prepare();
//
//            int _duration = mediaPlayer.getDuration()/1000;
//
//            InputStream inputStream = new FileInputStream(filePath);
//            OutputStream outputStream = new FileOutputStream(newFile);
//
//            byte[] cutBuffer = new byte[1024];
//            int byteRead = 0;
//
////            while (byteRead != -1) {
////                outputStream.write(cutBuffer, 0, byteRead);
////                byteRead = inputStream.read(cutBuffer);
////            }
//
//            long startPosition = startSeconds * sourceFile.length() / _duration;
//            int curPosition = 0;
//            Log.d("AudioCutting", "startPosition: " + startPosition);
//
//            while (byteRead != -1) {
//                Log.d("AudioCutting", "byteRead: " + byteRead);
//                curPosition += byteRead;
//                Log.d("AudioCutting", "curPosition: " + curPosition);
//                if(curPosition >= startPosition)
//                {
//                    int num_byte_to_read = (int) (curPosition - startPosition);
//                    Log.d("AudioCutting", "num_byte_to_read: " + num_byte_to_read);
//
//                    if (num_byte_to_read <= byteRead)
//                    {
//                        int start_byte_to_read = byteRead - num_byte_to_read;
//                        Log.d("AudioCutting", "start_byte_to_read: " + start_byte_to_read);
//                        outputStream.write(cutBuffer, start_byte_to_read, num_byte_to_read);
//                        Log.d("AudioCutting", "Ghi xong 1");
//                    }
//                    else {
//                        Log.d("AudioCutting", "Ghi toàn bộ những gì đọc được");
//                        outputStream.write(cutBuffer, 0, byteRead);
//                        Log.d("AudioCutting", "Ghi xong 2");
//                    }
//                }
//                byteRead = inputStream.read(cutBuffer);
//            }
//
//            inputStream.close();
//            outputStream.close();
//
//            long timestamp = new Date().getTime();
//            int durationInSeconds = (endSeconds - startSeconds);
//
//            // Convert duration to HH:mm:ss format
//            String duration = String.format("%02d:%02d:%02d",
//                    durationInSeconds / 3600,
//                    (durationInSeconds % 3600) / 60,
//                    durationInSeconds % 60);
//            saveCutAudio(newFilePath, "output.mp3", timestamp, duration);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error reading or writing file", Toast.LENGTH_SHORT).show();
//        }
//    }

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


}