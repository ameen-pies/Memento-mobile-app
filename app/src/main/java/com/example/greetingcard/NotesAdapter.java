package com.example.greetingcard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    public interface OnNoteListener {
        void onNoteClick(int position);
        void onNoteDelete(int position);
    }

    private List<Note> notesList;
    private OnNoteListener noteListener;
    private long selectedDateMillis = -1;

    public NotesAdapter(List<Note> notesList, OnNoteListener listener) {
        this.notesList = notesList;
        this.noteListener = listener;
    }

    public void setSelectedDate(long dateMillis) {
        this.selectedDateMillis = dateMillis;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view, noteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notesList.get(position);

        // Set note content
        holder.tvNoteTitle.setText(note.getTitle());
        holder.tvNoteDate.setText(note.getDate());

        // Limit content preview
        String content = note.getContent();
        holder.tvNoteContent.setText(content.length() > 100 ?
                content.substring(0, 100) + "..." : content);

        // Check if note matches selected date
        boolean isSelectedDate = isSameDay(note.getTimestamp(), selectedDateMillis);

        // Set background with highlight if needed
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(32f);
        drawable.setColor(isSelectedDate ?
                Color.argb(255, 230, 245, 255) : // Light blue for selected
                note.getBackgroundColor()); // Normal color otherwise
        drawable.setStroke(isSelectedDate ? 4 : 0, Color.parseColor("#FF4081")); // Pink border if selected

        holder.itemView.setBackground(drawable);

        // Style date text for selected notes
        holder.tvNoteDate.setTypeface(null, isSelectedDate ? Typeface.BOLD : Typeface.NORMAL);
        holder.tvNoteDate.setTextColor(isSelectedDate ?
                Color.parseColor("#FF4081") : // Pink for selected
                Color.BLACK);
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public void updateNotes(List<Note> newNotes) {
        notesList = newNotes;
        notifyDataSetChanged();
    }

    private boolean isSameDay(long time1, long time2) {
        if (time2 == -1) return false; // No date selected

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        cal2.setTimeInMillis(time2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoteTitle, tvNoteDate, tvNoteContent;
        ImageView btnDeleteNote;

        public NoteViewHolder(@NonNull View itemView, OnNoteListener listener) {
            super(itemView);
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            btnDeleteNote = itemView.findViewById(R.id.btnDeleteNote);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onNoteClick(position);
                }
            });

            btnDeleteNote.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onNoteDelete(position);
                }
            });
        }
    }
}