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
import com.example.acade_mic.model.Alarm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {
    private ArrayList<Alarm> alarms;
    private OnItemClickListener listener;

    public AlarmAdapter(ArrayList<Alarm> alarms, OnItemClickListener listener) {
        this.alarms = alarms;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvStartDateTime;
        TextView tvDuration;
        CheckBox checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStartDateTime = itemView.findViewById(R.id.tvStartDateTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
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
            return false;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alram_itemview_layout, parent, false);
        return new AlarmAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position != RecyclerView.NO_POSITION) {
            Alarm alarm = alarms.get(position);
            Date date = new Date(alarm.getStartTimeMillis());
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
            String dateFormatted = formatter.format(date);

            long secDuration = (alarm.getEndTimeMillis() - alarm.getStartTimeMillis()) / 1000L;

            holder.tvStartDateTime.setText(String.valueOf(dateFormatted));
            holder.tvDuration.setText(String.valueOf(secDuration));
        }
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }
}
