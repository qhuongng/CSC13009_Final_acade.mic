package com.example.acade_mic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acade_mic.OnItemClickListener;
import com.example.acade_mic.R;
import com.example.acade_mic.model.AudioRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private ArrayList<AudioRecord> records;
    private OnItemClickListener listener;
    private boolean editMode = false;

    public Adapter(ArrayList<AudioRecord> records, OnItemClickListener listener) {
        this.records = records;
        this.listener = listener;
    }

    public boolean isEditMode() {return editMode;}
    public void setEditMode(boolean mode){
        if(editMode != mode){
            editMode = mode;
            notifyDataSetChanged();

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvFilename;
        TextView tvMeta;
        CheckBox checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFilename = itemView.findViewById(R.id.tvFilename);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            checkbox = itemView.findViewById(R.id.checkbox);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position != RecyclerView.NO_POSITION) {
            AudioRecord record = records.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date(record.getTimestamp());
            String strDate = sdf.format(date);

            holder.tvFilename.setText(record.getFilename());
            holder.tvMeta.setText(record.getDuration() + " " + strDate);

            if(editMode){
                holder.checkbox.setVisibility(View.VISIBLE);
                holder.checkbox.setChecked(record.isChecked());
            } else {
                holder.checkbox.setVisibility(View.GONE);
                holder.checkbox.setChecked(false);
            }
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
