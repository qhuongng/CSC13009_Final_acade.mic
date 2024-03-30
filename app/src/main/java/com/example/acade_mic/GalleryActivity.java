package com.example.acade_mic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

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
    private TextInputEditText searchInput;
    private MaterialToolbar toolbar;
    private View editbar;
    private ImageButton btnClose;
    private ImageButton btnSelectAll;

    private boolean allChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        toolbar = findViewById(R.id.toolbar);
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

        editbar = findViewById(R.id.editBar);
        btnClose = findViewById(R.id.btnClose);
        btnSelectAll = findViewById(R.id.btnSelectAll);


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
        searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                searchDatabase(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionBar actionBar = getSupportActionBar();
                if(actionBar != null){
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setDisplayShowHomeEnabled(true);
                }
                editbar.setVisibility(View.GONE);
                for (AudioRecord rc : records ) {
                    rc.setChecked(false);
                }
                mAdapter.setEditMode(false);
            }
        });
        btnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allChecked = !allChecked;
                for (AudioRecord rc : records ) {
                    rc.setChecked(allChecked);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void searchDatabase(String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                records.clear();
                List<AudioRecord> queryResult = db.audioRecordDao().searchDatabase(String.format("%%%s%%",query));
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

            if(mAdapter.isEditMode()){
                records.get(position).setChecked(!records.get(position).isChecked());
                mAdapter.notifyItemChanged(position);
            } else {
                Intent intent = new Intent(this, AudioPlayerActivity.class);
                intent.putExtra("filepath", audioRecord.getFilePath());
                intent.putExtra("filename", audioRecord.getFilename());
                startActivity(intent);
            }
        }catch(Exception exception){
            System.out.println(exception.fillInStackTrace());
        }
    }

    @Override
    public void onItemLongClickListener(int position) {
        mAdapter.setEditMode(true);
        records.get(position).setChecked(!records.get(position).isChecked());
        mAdapter.notifyItemChanged(position);

        if(mAdapter.isEditMode() && editbar.getVisibility() == View.GONE){
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
            }
            editbar.setVisibility(View.VISIBLE);
        }
    }
}