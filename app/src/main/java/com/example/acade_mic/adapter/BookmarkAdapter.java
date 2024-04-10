package com.example.acade_mic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acade_mic.AppDatabase;
import com.example.acade_mic.OnItemClickListener;
import com.example.acade_mic.R;
import com.example.acade_mic.model.Bookmark;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private ArrayList<Bookmark> bookmarks;
    private OnItemClickListener listener;
    private Context bContext;
    private AppDatabase db;

    public BookmarkAdapter(ArrayList<Bookmark> list, OnItemClickListener listener, Context context){
        this.bookmarks = list;
        this.listener = listener;
        this.bContext = context;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        TextView index;
        TextView positionMark;
        ImageButton deleteBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            index = itemView.findViewById(R.id.textViewSerialNumber);
            positionMark = itemView.findViewById(R.id.textViewTime);
            deleteBtn = itemView.findViewById(R.id.buttonDelete);
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
        public void setDeleteButtonClickListener(View.OnClickListener listener) {
            deleteBtn.setOnClickListener(listener);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_itemview_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position != RecyclerView.NO_POSITION) {
            Bookmark bookmark = bookmarks.get(position);

            // Set data to views here
            int index = position + 1;
            holder.index.setText(String.valueOf(index));
            String pos = dateFormat(bookmark.getPosition());
            holder.positionMark.setText(pos);
            holder.itemView.setTooltipText(bookmark.getNote());
            holder.setDeleteButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        Bookmark del = bookmarks.get(adapterPosition);

                        deleteBookmark(del);
                    }
                }
            });
            // Set click listeners if needed
        }
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }
    private Executor executor = Executors.newSingleThreadExecutor();
    public void deleteBookmark(Bookmark delBookmark) {

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    AppDatabase db = AppDatabase.getInstance(bContext);

                    db.bookmarkDao().delete(delBookmark.getAudioId(), delBookmark.getPosition());
                }
            });

           bookmarks.remove(delBookmark);
           notifyDataSetChanged();


    }
    public String dateFormat(int duration) {
        int d = duration / 1000;
        int s = d % 60;
        int m = (d / 60) % 60;
        int h = (d - m * 60) / 360;

        NumberFormat f = new DecimalFormat("00");
        String str = m + ":" + f.format(s);
        if (h > 0) {
            str = h + ":" + str;
        }
        return str;
    }
}
