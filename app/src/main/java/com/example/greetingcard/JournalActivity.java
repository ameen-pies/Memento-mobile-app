package com.example.greetingcard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JournalActivity extends AppCompatActivity implements NotesAdapter.OnNoteListener {
    private static final String TAG = "JournalActivity";
    private static final int ADD_NOTE_REQUEST = 1;

    private List<Note> notesList = new ArrayList<>();
    private NotesAdapter adapter;
    private DatabaseReference notesRef;
    private String userUid;
    private CalendarView calendarView;
    private long selectedDateMillis = System.currentTimeMillis();
    private boolean shouldHighlightSelectedDate = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary);

        // Initialize Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        userUid = currentUser.getUid();
        notesRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userUid)
                .child("journalNotes");

        // Initialize views
        MaterialButton backButton = findViewById(R.id.backopt1);
        FloatingActionButton addButton = findViewById(R.id.buttonact);
        RecyclerView recyclerView = findViewById(R.id.notesRecyclerView);
        calendarView = findViewById(R.id.calendarView);

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Setup Calendar
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDateMillis = calendar.getTimeInMillis();
            shouldHighlightSelectedDate = true;
            adapter.setSelectedDate(selectedDateMillis);
            adapter.notifyDataSetChanged(); // Refresh to update highlighting
        });

        // Setup RecyclerView
        adapter = new NotesAdapter(notesList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load notes
        loadNotes();

        backButton.setOnClickListener(v -> finish());

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(JournalActivity.this, NoteActivity.class);
            intent.putExtra("SELECTED_DATE", selectedDateMillis);
            startActivityForResult(intent, ADD_NOTE_REQUEST);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_diary); // Set the current active item
    }

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(JournalActivity.this, MainActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (itemId == R.id.nav_diary) {
                    return true; // Already here
                } else if (itemId == R.id.nav_tasks) {
                    startActivity(new Intent(JournalActivity.this, CheckList.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (itemId == R.id.nav_quiz) {
                    startActivity(new Intent(JournalActivity.this, moodtracker.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                return true;
            };

    // ... [rest of your existing methods remain unchanged]
    private void loadNotes() {
        notesRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notesList.clear();
                for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);
                    if (note != null) {
                        note.setId(noteSnapshot.getKey());
                        notesList.add(0, note); // Newest first
                    }
                }
                adapter.setSelectedDate(selectedDateMillis);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JournalActivity.this, "Failed to load notes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNotesForSelectedDate() {
        // Calculate start and end of selected day
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDateMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfDay = calendar.getTimeInMillis();

        notesRef.orderByChild("timestamp")
                .startAt(startOfDay)
                .endAt(endOfDay)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notesList.clear();
                        for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                            Note note = noteSnapshot.getValue(Note.class);
                            if (note != null) {
                                note.setId(noteSnapshot.getKey());
                                notesList.add(note);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(JournalActivity.this, "Failed to load notes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onNoteClick(int position) {
        Note selectedNote = notesList.get(position);
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra("NOTE", selectedNote);
        intent.putExtra("SELECTED_DATE", selectedNote.getTimestamp());
        startActivityForResult(intent, ADD_NOTE_REQUEST);
    }

    @Override
    public void onNoteDelete(int position) {
        if (position >= 0 && position < notesList.size()) {
            String noteId = notesList.get(position).getId();
            notesRef.child(noteId).removeValue()
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK && data != null) {
            Note note = (Note) data.getSerializableExtra("NOTE");
            if (note != null) {
                if (note.getId() == null) {
                    // New note
                    String key = notesRef.push().getKey();
                    note.setTimestamp(selectedDateMillis);
                    notesRef.child(key).setValue(note);
                } else {
                    // Updated note
                    notesRef.child(note.getId()).setValue(note);
                }
            }
        }
    }
}