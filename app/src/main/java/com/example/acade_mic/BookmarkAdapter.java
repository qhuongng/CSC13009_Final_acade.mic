package com.example.acade_mic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private ArrayList<Bookmark> bookmarks;
    private OnItemClickListener listener;

    public BookmarkAdapter(ArrayList<Bookmark> list, OnItemClickListener listener){
        this.bookmarks = list;
        this.listener = listener;
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
            holder.positionMark.setText(bookmark.getPosition());
            holder.setDeleteButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        deleteBookmark(adapterPosition);
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
    public void deleteBookmark(int position) {
        if (position >= 0 && position < bookmarks.size()) {
            bookmarks.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, bookmarks.size());
        }
    }
}
