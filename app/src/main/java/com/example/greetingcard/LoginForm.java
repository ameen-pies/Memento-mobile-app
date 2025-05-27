package com.example.greetingcard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginForm extends AppCompatActivity {
    private MaterialButton loginbtn;
    private EditText email, password;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.momento2);

        loginbtn = findViewById(R.id.loginbtn);
        email = findViewById(R.id.emaillogin);
        password = findViewById(R.id.passlogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (TextUtils.isEmpty(mail)) {
                    email.setError("Email is required!");
                    email.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                    email.setError("Please enter a valid email!");
                    email.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(pass)) {
                    password.setError("Password is required!");
                    password.requestFocus();
                    return;
                }

                if (pass.length() < 6) {
                    password.setError("Minimum password length is 6 characters!");
                    password.requestFocus();
                    return;
                }

                // Show progress dialog or loading indicator here if needed

                mAuth.signInWithEmailAndPassword(mail, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        fetchUserData(user);
                                    }
                                } else {
                                    Toast.makeText(LoginForm.this, "Incorrect email or password.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        // Back Button
        MaterialButton backButton = findViewById(R.id.backopt);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginForm.this, MainPage.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        TextView sign = findViewById(R.id.signin);
        sign.setOnClickListener(v -> {
            startActivity(new Intent(LoginForm.this, sign.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void fetchUserData(FirebaseUser user) {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        Long pfpResId = documentSnapshot.getLong("avatarResId");

                        Intent intent = new Intent(LoginForm.this, MainActivity.class);
                        intent.putExtra("USER_NAME", fullName != null ? fullName : "User");
                        intent.putExtra("PFP_RES_ID", pfpResId != null ? pfpResId.intValue() : R.mipmap.pfp1_foreground);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } else {
                        Toast.makeText(LoginForm.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginForm.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    // Still proceed to MainActivity with default values if needed
                    Intent intent = new Intent(LoginForm.this, MainActivity.class);
                    intent.putExtra("USER_NAME", "User");
                    intent.putExtra("PFP_RES_ID", R.mipmap.pfp1_foreground);
                    startActivity(intent);
                    finish();
                });
    }
}