package com.example.acade_mic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kotlinx.coroutines.GlobalScope;

public class GalleryActivity extends AppCompatActivity implements OnItemClickListener{
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
    private TextView tvRename;
    private TextView tvDelete;
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

        btnRename = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        tvRename = findViewById(R.id.tvEdit);
        tvDelete = findViewById(R.id.tvDelete);


        editbar = findViewById(R.id.editBar);
        btnClose = findViewById(R.id.btnClose);
        btnSelectAll = findViewById(R.id.btnSelectAll);

        bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

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

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                File delFile = new File(record.getFilePath());
                                if(delFile != null)  delFile.delete();
                            }
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                db.audioRecordDao().delete(toDelete);
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
                        Toast.makeText(GalleryActivity.this, "Delete File successfully", Toast.LENGTH_SHORT).show();
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
        btnRename.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
        tvRename.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
    }

    private void disableDelete() {
        btnDelete.setClickable(false);
        btnDelete.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.disabledDarkGray, getTheme()));
    }

    private void enableRename() {
        btnRename.setClickable(true);
        btnRename.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.darkGray, getTheme()));
        tvRename.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.darkGray, getTheme()));
    }

    private void enableDelete() {
        btnDelete.setClickable(true);
        btnDelete.setBackgroundTintList(ResourcesCompat.getColorStateList(getResources(), R.color.darkGray, getTheme()));
        tvDelete.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.darkGray, getTheme()));
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
                        break;
                    case 1:
                        enableDelete();
                        enableRename();
                        break;
                    default:
                        disableRename();
                        enableDelete();
                }

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
        }
    }
}