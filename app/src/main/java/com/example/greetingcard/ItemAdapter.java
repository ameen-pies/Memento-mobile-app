package com.example.greetingcard;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private ArrayList<String> itemList;
    private OnItemListener onItemListener;
    private HashMap<Integer, Integer> colorMap = new HashMap<>();
    private Random random = new Random();
    private int[] pastelColors = {
            Color.parseColor("#FFD1DC"),
            Color.parseColor("#FFECB8"),
            Color.parseColor("#B5EAD7"),
            Color.parseColor("#C7CEEA"),
            Color.parseColor("#E2F0CB"),
            Color.parseColor("#FFDAC1")
    };
    private HashMap<Integer, Boolean> checkedStates = new HashMap<>();

    public interface OnItemListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
        void onCheckboxChanged(int position, boolean isChecked);
    }

    public ItemAdapter(ArrayList<String> itemList, OnItemListener onItemListener) {
        this.itemList = itemList;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ItemViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Set item text
        holder.textViewItem.setText(itemList.get(position));

        // Set card color
        if (!colorMap.containsKey(position)) {
            colorMap.put(position, pastelColors[random.nextInt(pastelColors.length)]);
        }
        holder.cardView.setCardBackgroundColor(colorMap.get(position));

        // Set checkbox state without triggering listener
        holder.checkBox.setOnCheckedChangeListener(null);
        boolean isChecked = checkedStates.getOrDefault(position, false);
        holder.checkBox.setChecked(isChecked);

        // Apply strikethrough if checked
        if (isChecked) {
            holder.textViewItem.setPaintFlags(holder.textViewItem.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textViewItem.setPaintFlags(holder.textViewItem.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Set checkbox listener
        holder.checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
            checkedStates.put(position, checked);
            if (onItemListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemListener.onCheckboxChanged(adapterPosition, checked);

                    // Update visual immediately
                    if (checked) {
                        holder.textViewItem.setPaintFlags(holder.textViewItem.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        holder.textViewItem.setPaintFlags(holder.textViewItem.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                }
            }
        });

        // Set delete button listener
        holder.imageViewDelete.setOnClickListener(v -> {
            if (onItemListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemListener.onItemLongClick(adapterPosition);
                }
            }
        });

        // Set item click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemListener.onItemClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onViewRecycled(@NonNull ItemViewHolder holder) {
        super.onViewRecycled(holder);
        holder.checkBox.setOnCheckedChangeListener(null);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        CheckBox checkBox;
        TextView textViewItem;
        ImageView imageViewDelete;

        public ItemViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewItem);
            checkBox = itemView.findViewById(R.id.checkBoxItem);
            textViewItem = itemView.findViewById(R.id.textViewItem);
            imageViewDelete = itemView.findViewById(R.id.imageViewDelete);
        }
    }

    // Call this from your Activity when you get checkbox updates from Firebase
    public void updateCheckedState(int position, boolean isChecked) {
        checkedStates.put(position, isChecked);
        notifyItemChanged(position);
    }
}