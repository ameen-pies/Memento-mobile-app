package com.example.greetingcard;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {
    private EditText etNoteTitle, etNoteContent;
    private long selectedDateMillis; // Stores the selected date from calendar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteContent = findViewById(R.id.etNoteContent);
        Button btnSaveNote = findViewById(R.id.btnSaveNote);

        // Get the selected date from intent (default to current time if not provided)
        selectedDateMillis = getIntent().getLongExtra("SELECTED_DATE", System.currentTimeMillis());

        // Check if we're editing an existing note
        Note existingNote = (Note) getIntent().getSerializableExtra("NOTE");
        if (existingNote != null) {
            etNoteTitle.setText(existingNote.getTitle());
            etNoteContent.setText(existingNote.getContent());
            selectedDateMillis = existingNote.getTimestamp(); // Use the note's existing date
        }

        btnSaveNote.setOnClickListener(v -> saveNote(existingNote));
    }

    private void saveNote(Note existingNote) {
        String title = etNoteTitle.getText().toString().trim();
        String content = etNoteContent.getText().toString().trim();

        // Format the selected date
        String formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(new Date(selectedDateMillis));

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note;
        if (existingNote != null) {
            // Update existing note
            note = existingNote;
            note.setTitle(title);
            note.setContent(content);
            note.setDate(formattedDate);
            note.setTimestamp(selectedDateMillis); // Keep the original or updated date
        } else {
            // Create new note with the selected date
            note = new Note(title, content, formattedDate);
            note.setTimestamp(selectedDateMillis);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("NOTE", note);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}