package com.example.greetingcard;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class moodtracker extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageView emoji;
    private final int[] emojiProgressValues = {10, 30, 50, 70, 100};
    private String userName;
    private String userUid;
    private int selectedEmojiIndex = -1;

    // Firebase Database reference
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_tracker);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Not logged in, redirect to login
            startActivity(new Intent(this, LoginForm.class));
            finish();
            return;
        }
        userUid = currentUser.getUid();

        // Initialize Firebase Database pointing to user's moodData
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userUid);

        // Get user's name from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullName")) {
                        userName = dataSnapshot.child("fullName").getValue(String.class);
                        TextView userNameTextView = findViewById(R.id.greetings);
                        if (userNameTextView != null) {
                            userNameTextView.setText("Hey, " + userName + "!");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(moodtracker.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });

        progressBar = findViewById(R.id.progress);
        Button nextButton = findViewById(R.id.nextbutton);

        // emoji click listeners
        setupEmojiSelection(R.id.terrible, 0);
        setupEmojiSelection(R.id.bad, 1);
        setupEmojiSelection(R.id.okay, 2);
        setupEmojiSelection(R.id.good, 3);
        setupEmojiSelection(R.id.great, 4);

        nextButton.setOnClickListener(v -> {
            if (emoji == null) {
                Toast.makeText(this, "Please select how you're feeling", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current hour (0-23) for the daily tracking
            int currentHour = Integer.parseInt(
                    new SimpleDateFormat("HH", Locale.getDefault()).format(new Date())
            );

            // Convert progress value (10-100) to your scale (1-10)
            int moodValue = emojiProgressValues[selectedEmojiIndex] / 10;

            // Create entry in the database using hour as key
            databaseReference.child("moodData").child("daily").child(String.valueOf(currentHour))
                    .setValue(moodValue)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(moodtracker.this, "Mood recorded for hour " + currentHour, Toast.LENGTH_SHORT).show();

                        // Proceed to next activity
                        Intent j = new Intent(moodtracker.this, moodtracker2.class);
                        j.putExtra("USER_NAME", userName);
                        j.putExtra("PFP_RES_ID", R.mipmap.pfp_foreground); // Default image if not set
                        startActivity(j);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(moodtracker.this, "Failed to record mood: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void setupEmojiSelection(int emojiId, int progressIndex) {
        ImageView this_emoji = findViewById(emojiId);
        this_emoji.setOnClickListener(v -> {
            if (emoji != null) {
                emoji.setBackgroundResource(0);
            }
            progressBar.setProgress(emojiProgressValues[progressIndex]);
            this_emoji.setBackgroundResource(R.drawable.selected_border);
            emoji = this_emoji;
            selectedEmojiIndex = progressIndex;
        });
    }

    private String getRadioGroupSelection(int radioGroupId) {
        RadioGroup radioGroup = findViewById(radioGroupId);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton radbutton = findViewById(selectedId);
            return radbutton.getText().toString();
        }
        return "";
    }
}