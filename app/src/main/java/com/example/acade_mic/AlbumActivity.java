package com.example.acade_mic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acade_mic.adapter.Adapter;
import com.example.acade_mic.adapter.AlbumAdapter;
import com.example.acade_mic.model.Album;
import com.example.acade_mic.model.AudioRecord;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity implements OnItemClickListener {
    private String selectedAlbum;

    private ArrayList<String> albumNames;
    private AlbumAdapter mAdapter;
    private AppDatabase db;
    private TextInputEditText searchInput;
    private MaterialToolbar toolbar;
    private View editbar;
    private ImageButton btnClose;
    private ImageButton btnRename;
    private ImageButton btnDelete;

    private FloatingActionButton btnCreateAlbum;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private BottomSheetBehavior<LinearLayout> editSheetBehavior;
    private BottomSheetBehavior<LinearLayout> renameSheetBehavior;


    private LinearLayout editSheet;
    private View bottomSheetBG;
    private MaterialButton btnCancel;
    private MaterialButton btnCreate;
    private MaterialButton btnRenameAlbum;
    private MaterialButton btnCancelRename;
    private TextInputEditText fileNameInput;
    private TextInputEditText renameAlbInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

        renameAlbInput= findViewById(R.id.renameAlbInput);
        renameSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.renameAlbumSheet));
        renameSheetBehavior.setPeekHeight(0);

        renameSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBG = (View) findViewById(R.id.bottomSheetBG);
        btnRename = findViewById(R.id.btnEdit);
        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedAlbum.equals("") && !selectedAlbum.isEmpty()) {
                    if (selectedAlbum.equals("All Records") || selectedAlbum.equals("Delete")) {
                        Toast.makeText(AlbumActivity.this, "Unable to rename default album!", Toast.LENGTH_SHORT).show();
                    } else {

                        renameAlbInput.setText(selectedAlbum);
                        renameSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                        bottomSheetBG.setVisibility(View.VISIBLE);
                        ActionBar actionBar = getSupportActionBar();
                        if(actionBar != null){
                            actionBar.setDisplayHomeAsUpEnabled(true);
                            actionBar.setDisplayShowHomeEnabled(true);
                        }
                        editbar.setVisibility(View.GONE);
                        editSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                }
            }
        });

        btnRenameAlbum = findViewById(R.id.btnRenameAlb);
        btnRenameAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBG.setVisibility(View.GONE);
                hideKeyBoard(renameAlbInput);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    renameSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }, 500);

                rename();
                Toast.makeText(AlbumActivity.this, "Rename album successfully", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelRename=findViewById(R.id.btnCancelRename);
        btnCancelRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBG.setVisibility(View.GONE);
                hideKeyBoard(fileNameInput);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    renameSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }, 500);
            }
        });


        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AlbumActivity.this);
                builder.setTitle("Delete album?");
                builder.setMessage("Are you sure you want to delete this album?(All records in this album will be deleted!");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!selectedAlbum.equals("") && !selectedAlbum.isEmpty()) {
                            if (selectedAlbum.equals("All Records") || selectedAlbum.equals("Delete")) {
                                Toast.makeText(AlbumActivity.this, "Unable to delete default album!", Toast.LENGTH_SHORT).show();
                            } else {
                                ArrayList<Integer> recID = new ArrayList<>();

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        List<Integer>listID = db.albumDao().getAllrecordIDbyAlbumName(selectedAlbum);
                                        if(!listID.isEmpty()) {
                                            recID.addAll(listID);
                                        }
                                    }
                                }).start();

                                if(!recID.isEmpty()){
                                    ArrayList<AudioRecord> toDelList = new ArrayList<>();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for(int id:recID){
                                                System.out.println(id);
                                                AudioRecord toDelete = db.audioRecordDao().getRecbyID(id);
                                                toDelList.add(toDelete);
                                            }
                                        }
                                    }).start();

                                    if(!toDelList.isEmpty()) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (AudioRecord tmp : toDelList) {
                                                    File delFile = new File(tmp.getFilePath());
                                                    if (delFile != null) delFile.delete();
                                                    db.audioRecordDao().delete(tmp);
                                                    db.bookmarkDao().deleteBookmarksByRecordId(tmp.getId());
                                                }
                                            }
                                        }).start();

                                    }
                                }


                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        db.albumDao().deletebyAlbumName(selectedAlbum);

                                    }
                                }).start();


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        albumNames.remove(selectedAlbum);
                                        mAdapter.notifyDataSetChanged();
                                        ActionBar actionBar = getSupportActionBar();
                                        if(actionBar != null){
                                            actionBar.setDisplayHomeAsUpEnabled(true);
                                            actionBar.setDisplayShowHomeEnabled(true);
                                        }
                                        editbar.setVisibility(View.GONE);
                                        editSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                        Toast.makeText(AlbumActivity.this,"Delete album successfully!",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }


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
        });


        fileNameInput = findViewById(R.id.filenameInput);

        editbar = findViewById(R.id.editBar);
        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionBar actionBar = getSupportActionBar();
                if(actionBar != null){
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setDisplayShowHomeEnabled(true);
                }
                editbar.setVisibility(View.GONE);
                editSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });


        btnCreateAlbum = (FloatingActionButton) findViewById(R.id.btnCreateAlbum);
        btnCreateAlbum.setOnClickListener((View v) -> {
            fileNameInput.setText("");
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            bottomSheetBG.setVisibility(View.VISIBLE);

        });

        editSheet = findViewById(R.id.editSheet);
        editSheetBehavior = BottomSheetBehavior.from(editSheet);
        editSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet));
        bottomSheetBehavior.setPeekHeight(0);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBG = (View) findViewById(R.id.bottomSheetBG);
        btnCancel = findViewById(R.id.btnCancel);
        btnCreate = findViewById(R.id.btnCreate);

        btnCancel.setOnClickListener((View v) -> {
            // delete file
            //
            dismiss();
        });

        btnCreate.setOnClickListener((View v) -> {
            dismiss();
            create();
        });

        bottomSheetBG.setOnClickListener((View v) -> {
            // delete file
            //
            dismiss();
        });

        albumNames = new ArrayList<>();
        db = AppDatabase.getInstance(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> name = db.albumDao().getAllAlbumName();
                if(!name.contains("All Records")){
                        Album allRec = new Album("All Records");
                        albumNames.add(allRec.getAlbumName());
                        db.albumDao().insert(allRec);
                }
                albumNames.addAll(name);
                if(!name.contains("Delete")){
                        Album delRec = new Album("Delete");
                        albumNames.add(delRec.getAlbumName());
                        db.albumDao().insert(delRec);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();


        mAdapter = new AlbumAdapter(albumNames, this, this);


        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        recyclerView.setAdapter(mAdapter);



        searchInput = findViewById(R.id.searchInput);
    }

    private void searchDatabase(String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                albumNames.clear();
                List<String> queryResult = db.albumDao().searchDatabase(String.format("%%%s%%",query));
                albumNames.addAll(queryResult);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public void create(){
        String newAlbumName = fileNameInput.getText().toString();
        if(!newAlbumName.equals("")) {
            int check = 0;
            for (String s : albumNames) {
                if (s.equals(newAlbumName)) check++;
            }
            if (check > 0)
                Toast.makeText(this, "Album name has existed", Toast.LENGTH_SHORT).show();
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Album tmp = new Album(newAlbumName);
                        db.albumDao().insert(tmp);
                        albumNames.add(newAlbumName);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }

                        });
                    }

                }).start();
                Toast.makeText(this, "Create album successfully", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Album name can not be empty", Toast.LENGTH_SHORT).show();
        }
    }
    public void dismiss() {
        bottomSheetBG.setVisibility(View.GONE);
        hideKeyBoard(fileNameInput);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }, 500);
    }

    public void hideKeyBoard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
    @Override
    public void onItemClickListener(int position) {
        try {
            String albumName = albumNames.get(position);
            Intent intent = new Intent(this, GalleryActivity.class);
            intent.putExtra("albumName", albumName);

            startActivity(intent);

        }catch(Exception exception){
            System.out.println(exception.fillInStackTrace());
        }
    }


    @Override
    public void onItemLongClickListener(int position) {
        editSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        editbar.setVisibility(View.VISIBLE);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        selectedAlbum = albumNames.get(position);
    }
    public void rename(){

        String newAlbumName = renameAlbInput.getText().toString();
        if (!newAlbumName.equals("")) {
            int check = 0;
            for (String s : albumNames) {
                if (s.equals(newAlbumName)) check++;
            }
            if (check > 0)
                Toast.makeText(AlbumActivity.this, "Album name has existed", Toast.LENGTH_SHORT).show();
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int tmp = albumNames.indexOf(selectedAlbum);
                        albumNames.set(tmp, newAlbumName);
                        db.albumDao().updateAlbumName(selectedAlbum, newAlbumName);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                }).start();
            }
        }

    }
}