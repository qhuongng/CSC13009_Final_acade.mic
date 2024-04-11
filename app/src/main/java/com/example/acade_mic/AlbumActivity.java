package com.example.acade_mic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity implements OnItemClickListener {

    private ArrayList<String> albumNames;
    private AlbumAdapter mAdapter;
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

    }
}