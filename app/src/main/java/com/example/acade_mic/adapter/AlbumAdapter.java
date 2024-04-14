package com.example.acade_mic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acade_mic.AppDatabase;
import com.example.acade_mic.OnItemClickListener;
import com.example.acade_mic.R;
import com.example.acade_mic.model.Album;
import com.example.acade_mic.model.Bookmark;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    private ArrayList<String> albumNames;
    private OnItemClickListener listener;
    private Context bContext;
    private AppDatabase db;
    public AlbumAdapter(ArrayList<String> albumname, OnItemClickListener listener, Context context){
        this.albumNames = albumname;
        this.listener = listener;
        this.bContext = context;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        TextView tvAlbumName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlbumName= itemView.findViewById(R.id.tvAlbumName);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClickListener(position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                listener.onItemLongClickListener(position);
            }
            return true;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.albumview_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position != RecyclerView.NO_POSITION) {
            String albumName = albumNames.get(position);


            holder.tvAlbumName.setText(albumName);

        }
    }

    @Override
    public int getItemCount() {
        return albumNames.size();
    }
}
