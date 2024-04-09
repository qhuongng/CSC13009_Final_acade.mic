package com.example.acade_mic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acade_mic.R;

import java.util.List;

public class SelectedItemsAdapter extends RecyclerView.Adapter<SelectedItemsAdapter.ViewHolder> {

    private List<String> selectedItems;

    public SelectedItemsAdapter(List<String> selectedItems) {
        this.selectedItems = selectedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = selectedItems.get(position);
        holder.selectedItemName.setText(item);
        holder.removeIcon.setOnClickListener(v -> {
            selectedItems.remove(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return selectedItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView selectedItemName;
        ImageButton removeIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            selectedItemName = itemView.findViewById(R.id.selectedItemName);
            removeIcon = itemView.findViewById(R.id.removeIcon);
        }
    }
}
