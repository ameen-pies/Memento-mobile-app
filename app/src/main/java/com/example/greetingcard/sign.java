package com.example.greetingcard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class sign extends AppCompatActivity {
    private int selectedPfpResId = R.mipmap.pfp1_foreground; // Default avatar
    private EditText fullname, pass, email;
    private MaterialButton signUpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private TextView loginlink;

    private void highlightSelectedPfp(int selectedPfpId) {
        int[] allPfpIds = {R.id.pfp1, R.id.pfp2, R.id.pfp3, R.id.pfp4, R.id.pfp5, R.id.pfp6};

        for (int id : allPfpIds) {
            ImageView pfp = findViewById(id);
            if (id == selectedPfpId) {
                pfp.setBackgroundResource(R.drawable.selected_pfp_border);
            } else {
                pfp.setBackgroundResource(0); // Remove border
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        fullname = findViewById(R.id.fullNameEditText);
        pass = findViewById(R.id.passsignup);
        email = findViewById(R.id.emailsignup);
        signUpButton = findViewById(R.id.signupbtn);

        // Avatar selection
        int[] pfpIds = {R.id.pfp1, R.id.pfp2, R.id.pfp3, R.id.pfp4, R.id.pfp5, R.id.pfp6};
        int[] pfpResources = {
                R.mipmap.pfp1_foreground,
                R.mipmap.pfp2_foreground,
                R.mipmap.pfp3_foreground,
                R.mipmap.pfp4_foreground,
                R.mipmap.pfp5_foreground,
                R.mipmap.pfp6_foreground
        };
        for (int i = 0; i < pfpIds.length; i++) {
            ImageView pfp = findViewById(pfpIds[i]);
            int finalI = i;
            pfp.setOnClickListener(v -> {
                selectedPfpResId = pfpResources[finalI];
                highlightSelectedPfp(pfpIds[finalI]);
            });
        }

        // Back Button
        MaterialButton backButton = findViewById(R.id.backopt);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(sign.this, MainPage.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        loginlink = findViewById(R.id.login);
        loginlink.setOnClickListener(v -> {
            Intent intent = new Intent(sign.this, LoginForm.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Signup button click
        signUpButton.setOnClickListener(v -> {
            String fullName = fullname.getText().toString().trim();
            String password = pass.getText().toString().trim();
            String mail = email.getText().toString().trim();

            if (fullName.isEmpty() || password.isEmpty() || mail.isEmpty()) {
                Toast.makeText(sign.this, "Fields required!", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 5) {
                Toast.makeText(sign.this, "Password must be 5+ characters!", Toast.LENGTH_SHORT).show();
            } else if (!mail.matches("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")) {
                Toast.makeText(sign.this, "Invalid email!", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Create user data in Realtime Database
                            createUserDatabaseEntry(user.getUid(), fullName, selectedPfpResId);

                            // Proceed to MainActivity
                            Intent intent = new Intent(sign.this, MainActivity.class);
                            intent.putExtra("USER_NAME", fullName);
                            intent.putExtra("PFP_RES_ID", selectedPfpResId);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        }
                    } else {
                        Toast.makeText(sign.this, "Signup failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void createUserDatabaseEntry(String uid, String fullName, int avatarResId) {
        // Create the basic user data structure
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("avatarResId", avatarResId);

        // Initialize empty mood data
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("daily", new HashMap<>());
        moodData.put("weekly", new HashMap<>());
        moodData.put("monthly", new HashMap<>());
        userData.put("moodData", moodData);

        // Initialize stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("moodEntries", 0);
        stats.put("completedTasks", 0);
        stats.put("notesCreated", 0);
        userData.put("stats", stats);

        // Write to database
        databaseRef.child("users").child(uid).setValue(userData)
                .addOnFailureListener(e -> {
                    Log.e("Signup", "Failed to create user database entry", e);
                });
    }
}