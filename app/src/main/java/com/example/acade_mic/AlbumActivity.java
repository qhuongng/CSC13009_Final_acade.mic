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
    private LinearLayout editSheet;
    private View bottomSheetBG;
    private MaterialButton btnCancel;
    private MaterialButton btnCreate;
    private TextInputEditText fileNameInput;


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

        btnRename = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);



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
        btnCancel = (MaterialButton) findViewById(R.id.btnCancel);
        btnCreate = (MaterialButton) findViewById(R.id.btnCreate);
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
                if(name.isEmpty()){
                    Album allRec = new Album("All Records");
                    Album delRec = new Album("Delete");
                    albumNames.add(allRec.getAlbumName());
                    albumNames.add(delRec.getAlbumName());
                    db.albumDao().insert(allRec);
                    db.albumDao().insert(delRec);
                }
                else albumNames.addAll(name);
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
    }
}