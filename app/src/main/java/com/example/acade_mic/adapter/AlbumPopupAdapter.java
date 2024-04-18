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

import java.util.ArrayList;

public class AlbumPopupAdapter extends RecyclerView.Adapter<AlbumPopupAdapter.ViewHolder> {
    private ArrayList<String> albumNames;
    private OnAlbumItemClickListener listener;
    public AlbumPopupAdapter(ArrayList<String> albumname, OnAlbumItemClickListener listener){
        this.albumNames = albumname;
        this.listener = listener;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvAlbumName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlbumName= itemView.findViewById(R.id.tvAlbumName);

        }
        public void bind(final String album, final OnAlbumItemClickListener listener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAlbumItemClick(album);
                }
            });
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
            holder.bind(albumName,listener);
        }
    }
    @Override
    public int getItemCount() {
        return albumNames.size();
    }
}
