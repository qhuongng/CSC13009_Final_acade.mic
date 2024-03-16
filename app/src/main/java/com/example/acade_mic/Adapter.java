package com.example.acade_mic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private ArrayList<AudioRecord> records;

    public Adapter(ArrayList<AudioRecord> records) {
        this.records = records;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilename;
        TextView tvMeta;
        CheckBox checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFilename = itemView.findViewById(R.id.tvFilename);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            checkbox = itemView.findViewById(R.id.checkbox);
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
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
