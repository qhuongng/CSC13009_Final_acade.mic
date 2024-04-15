package com.example.acade_mic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.acade_mic.adapter.Adapter;
import com.example.acade_mic.model.Album;
import com.example.acade_mic.model.AudioRecord;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class GalleryActivity extends AppCompatActivity implements OnItemClickListener{
    private String albName;
    private ArrayList<AudioRecord> records;
    private Adapter mAdapter;
    private AppDatabase db;
    private TextInputEditText searchInput;
    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private MaterialToolbar toolbar;
    private View editbar;
    private ImageButton btnClose;
    private ImageButton btnSelectAll;
    private boolean allChecked = false;
    private ImageButton btnRename;
    private ImageButton btnDelete;
    private ImageButton btnShare;
    private ImageButton btnEditAudio;
    private TextView tvRename;
    private TextView tvDelete;
    private TextView tvShare;
    private TextView tvEditAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        albName = getIntent().getStringExtra("albumName");

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(albName);
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

        btnRename = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnShare = findViewById(R.id.btnShare);

        btnEditAudio = findViewById(R.id.btnEditAudio);
        tvRename = findViewById(R.id.tvEdit);
        tvDelete = findViewById(R.id.tvDelete);
        tvShare = findViewById(R.id.tvShare);
        tvEditAudio = findViewById(R.id.tvEditAudio);


        editbar = findViewById(R.id.editBar);
        btnClose = findViewById(R.id.btnClose);
        btnSelectAll = findViewById(R.id.btnSelectAll);

        bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        records = new ArrayList<>();
        fetchAll();

        db = AppDatabase.getInstance(this);

        mAdapter = new Adapter(records, this);


        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


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
                leaveEditMode();
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

                if(allChecked){
                    disableRename();
                    enableDelete();
                }else {
                    disableRename();
                    disableDelete();
                }
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioRecord toBeShared = null;
                for (AudioRecord r : records) {
                    if (r.isChecked()) {
                        toBeShared = r;
                        break;
                    }
                }
                if(toBeShared != null){
                    File file = new File(toBeShared.getFilePath());
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", file);

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("audio/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivity(Intent.createChooser(shareIntent, "Share via"));
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!albName.equals("Delete")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                    builder.setTitle("Delete record?");
                    final int nbRecords = countSelectedRecords(records);
                    builder.setMessage("Are you sure you want to delete " + nbRecords + " record(s)?");

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<AudioRecord> toDelete = new ArrayList<>();
                            for (AudioRecord record : records) {
                                if (record.isChecked()) {
                                    toDelete.add(record);
                                    PersistableBundle extras = new PersistableBundle();
                                    extras.putString("filePath",record.getFilePath());
                                    extras.putInt("id", record.getId());
                                    JobInfo.Builder builder = new JobInfo.Builder(record.getId(),new ComponentName(getBaseContext(), FileDeleteJobService.class));
                                    //builder.setMinimumLatency(60 * 1000);
                                    builder.setExtras(extras);
                                    builder.setMinimumLatency(30 * 24 * 60 * 60 * 1000);
                                    JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                                    jobScheduler.schedule(builder.build());
                                }
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for(AudioRecord ar : toDelete){
                                        db.albumDao().deleteByIdRecord(ar.getId());
                                        db.bookmarkDao().deleteBookmarksByRecordId(ar.getId());
                                        db.albumDao().insert(new Album("Delete",ar.getId()));
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            records.removeAll(toDelete);
                                            mAdapter.notifyDataSetChanged();
                                            leaveEditMode();
                                        }
                                    });
                                }
                            }).start();
                            Toast.makeText(GalleryActivity.this, "Delete Records successfully", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // it does nothing
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                    builder.setTitle("Delete record permanently?");
                    final int nbRecords = countSelectedRecords(records);
                    builder.setMessage("Are you sure you want to permanently delete " + nbRecords + " record(s)? They cannot be recovered later!");

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ArrayList<AudioRecord> toDelete = new ArrayList<>();
                            for (AudioRecord record : records) {
                                if (record.isChecked()) {
                                    toDelete.add(record);
                                    File delFile = new File(record.getFilePath());
                                    if(delFile != null)  delFile.delete();
                                }
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    db.audioRecordDao().delete(toDelete);
                                    for(AudioRecord ar : toDelete){
                                        db.albumDao().deleteByIdRecord(ar.getId());
                                        db.bookmarkDao().deleteBookmarksByRecordId(ar.getId());
                                        db.transcriptionFileDao().delete(ar.getId());

                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            records.removeAll(toDelete);
                                            mAdapter.notifyDataSetChanged();
                                            leaveEditMode();
                                        }
                                    });
                                }
                            }).start();
                            Toast.makeText(GalleryActivity.this, "Delete Records successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // it does nothing
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
            private int countSelectedRecords(ArrayList<AudioRecord> records) {
                int count = 0;
                for (AudioRecord record : records) {
                    if (record.isChecked()) {
                        count++;
                    }
                }
                return count;
            }
        });

        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);

                View dialogView = getLayoutInflater().inflate(R.layout.rename_layout, null);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                AudioRecord record = null;
                for (AudioRecord r : records) {
                    if (r.isChecked()) {
                        record = r;
                        break;
                    }
                }
                if (record != null) {
                    TextInputEditText textInput = dialogView.findViewById(R.id.filenameInput);
                    textInput.setText(record.getFilename());
                    AudioRecord finalRecord = record;
                    dialogView.findViewById(R.id.btnSaveRename).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideKeyBoard(dialogView);
                            String newFileName  = textInput.getText().toString();
                            if (newFileName.isEmpty()) {
                                Toast.makeText(GalleryActivity.this, "A name is required", Toast.LENGTH_LONG).show();
                            } else if (!newFileName.contains(".mp3")) {
                                Toast.makeText(GalleryActivity.this, "Cannot change the type of file", Toast.LENGTH_LONG).show();
                            } else {
                                String oldFileName = finalRecord.getFilename();
                                int check = 0;
                                for (AudioRecord record:records) {
                                    if(newFileName.equals(record.getFilename())) check++;
                                }
                                if(check > 0) {
                                    Toast.makeText(GalleryActivity.this, "File name has been exists", Toast.LENGTH_SHORT).show();
                                } else {
                                    File oldFile = new File(finalRecord.getFilePath());
                                    if (oldFile.exists()) {
                                        String[] path = finalRecord.getFilePath().split(finalRecord.getFilename());
                                        String newFilePath = path[0] + newFileName;
                                        File newFile = new File(newFilePath);
                                        if (oldFile.renameTo(newFile)) {
                                            finalRecord.setFilename(newFileName);
                                            finalRecord.setFilePath(newFilePath);
                                            finalRecord.setAmpsPath(newFilePath);
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    db.audioRecordDao().update(finalRecord);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mAdapter.notifyItemChanged(records.indexOf(finalRecord));
                                                            dialog.dismiss();
                                                            leaveEditMode();
                                                        }
                                                    });
                                                }
                                            }).start();
                                            Toast.makeText(GalleryActivity.this, "File renamed successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(GalleryActivity.this, "Failed to rename file", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        }
                    });
                }

                dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        btnEditAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Find all selected audio records
                ArrayList<AudioRecord> selectedRecords = new ArrayList<>();
                for (AudioRecord record : records) {
                    if (record.isChecked()) {
                        selectedRecords.add(record);
                    }
                }
                // Check if any audio record is selected
                if (!selectedRecords.isEmpty()) {
                    // Start EditAudioActivity with the list of file paths and filenames as extras
                    Intent intent = new Intent(GalleryActivity.this, EditAudioActivity.class);
                    ArrayList<String> filepaths = new ArrayList<>();
                    ArrayList<String> filenames = new ArrayList<>();
                    for (AudioRecord record : selectedRecords) {
                        filepaths.add(record.getFilePath());
                        filenames.add(record.getFilename());
                    }
                    intent.putStringArrayListExtra("filepaths", filepaths);
                    intent.putStringArrayListExtra("filenames", filenames);
                    intent.putExtra("albName", albName);

                    startActivity(intent);
                    startActivityForResult(intent,1);
                } else {
                    // If no audio record is selected, display a message
                    Toast.makeText(GalleryActivity.this, "No audio record selected", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    @Override
    public void onResume(Bundle savedInstanceState){
        super.onResume();
        fetchAll();
        leaveEditMode();
    }
    private void hideKeyBoard(View v){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
    private void leaveEditMode() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        editbar.setVisibility(View.GONE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        for (AudioRecord rc : records ) {
            rc.setChecked(false);
        }
        mAdapter.setEditMode(false);

    }



    private void disableRename() {
        btnRename.setClickable(false);
        btnRename.setImageTintList(ContextCompat.getColorStateList(this, R.color.disabledDarkGray));
        tvRename.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
    }

    private void disableDelete() {
        btnDelete.setClickable(false);
        btnDelete.setImageTintList(ContextCompat.getColorStateList(this, R.color.disabledDarkGray));
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
    }
    private void disableShare(){
        btnShare.setClickable(false);
        btnShare.setImageTintList(ContextCompat.getColorStateList(this, R.color.disabledDarkGray));
        tvShare.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
    }
    private void disableEdit(){
        btnEditAudio.setClickable(false);
        btnEditAudio.setImageTintList(ContextCompat.getColorStateList(this, R.color.disabledDarkGray));
        tvEditAudio.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
    }
    private void enableRename() {
        btnRename.setClickable(true);
        btnRename.setImageTintList(ContextCompat.getColorStateList(this, R.color.darkGray));
        tvRename.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.darkGray, getTheme()));
    }

    private void enableDelete() {
        btnDelete.setClickable(true);
        btnDelete.setImageTintList(ContextCompat.getColorStateList(this, R.color.darkGray));
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.darkGray, getTheme()));
    }
    private void enableShare() {
        btnShare.setClickable(true);
        btnShare.setImageTintList(ContextCompat.getColorStateList(this, R.color.darkGray));
        tvShare.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.darkGray, getTheme()));
    }
    private void enableEdit() {
        btnEditAudio.setClickable(true);
        btnEditAudio.setImageTintList(ContextCompat.getColorStateList(this, R.color.darkGray));
        tvEditAudio.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.darkGray, getTheme()));
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
                List<Integer> listRecID = db.albumDao().getAllrecordIDbyAlbumName(albName);
                records.clear();
                if(listRecID != null){
                    for (int id: listRecID)
                    {
                        AudioRecord temp = new AudioRecord();
                        temp = db.audioRecordDao().getRecbyID(id);
                        if(temp != null) records.add(temp);
                    }

//                List<AudioRecord> queryResult = db.audioRecordDao().getAll();
//                records.addAll(queryResult);


                }
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

                int nbSelected = 0;
                for (AudioRecord record : records) {
                    if (record.isChecked()) {
                        nbSelected++;
                    }
                }

                switch (nbSelected) {
                    case 0:
                        disableRename();
                        disableDelete();
                        disableShare();
                        disableEdit();
                        break;
                    case 1:
                        enableDelete();
                        enableRename();
                        enableShare();
                        enableEdit();
                        break;
                    default:
                        disableRename();
                        enableDelete();
                        disableShare();
                        enableEdit();
                }

            } else {
                Intent intent = new Intent(this, AudioPlayerActivity.class);
                intent.putExtra("filepath", audioRecord.getFilePath());
                intent.putExtra("filename", audioRecord.getFilename());
                intent.putExtra("id", audioRecord.getId());

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

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        if(mAdapter.isEditMode() && editbar.getVisibility() == View.GONE){
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
            }
            editbar.setVisibility(View.VISIBLE);
            enableDelete();
            enableRename();
            enableShare();
            enableEdit();
        }
    }


}