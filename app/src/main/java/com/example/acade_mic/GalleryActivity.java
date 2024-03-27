package com.example.acade_mic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kotlinx.coroutines.GlobalScope;

public class GalleryActivity extends AppCompatActivity implements OnItemClickListener{

    private ArrayList<AudioRecord> records;
    private Adapter mAdapter;
    private AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        records = new ArrayList<>();

        db = Room.databaseBuilder(
                this,
                AppDatabase.class,
                "audioRecords"
        ).build();

        mAdapter = new Adapter(records, this);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchAll();
    }

    private void fetchAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                records.clear();
                List<AudioRecord> queryResult = db.audioRecordDao().getAll();
                records.addAll(queryResult);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onItemClickListener(int position) {
        try{
            AudioRecord audioRecord = records.get(position);
            File cacheFile = new File(getCacheDir(), audioRecord.getFilename());
            Intent intent = new Intent(this, AudioPlayerActivity.class);
            intent.putExtra("filepath", cacheFile.getAbsolutePath() );
            intent.putExtra("filename", audioRecord.getFilename());
            startActivity(intent);
        }catch(Exception exception){
            System.out.println(exception.fillInStackTrace());
        }
    }

    private byte[] readAudioFileFromCache() {
        byte[] audioData = null;
        try {
            // Example cache file path
            File cacheFile = new File(getCacheDir(), "example_audio.mp3");

            FileInputStream inputStream = new FileInputStream(cacheFile);
            audioData = new byte[(int) cacheFile.length()];
            inputStream.read(audioData);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return audioData;
    }

    @Override
    public void onItemLongClickListener(int position) {
        Toast.makeText(this,"Long click",Toast.LENGTH_SHORT).show();
    }
}