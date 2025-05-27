package com.example.greetingcard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckList extends AppCompatActivity implements ItemAdapter.OnItemListener {

    private ArrayList<String> itemList;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private EditText editTextItem;
    private Button buttonAdd;
    private MaterialButton backButton;

    // Firebase variables
    private DatabaseReference userTodoRef;
    private FirebaseAuth mAuth;
    private String userUid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.to_do_list);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(new Intent(CheckList.this, LoginForm.class));
            finish();
            return;
        }
        userUid = currentUser.getUid();
        userTodoRef = FirebaseDatabase.getInstance().getReference("users").child(userUid).child("todoItems");

        // Initialize views
        backButton = findViewById(R.id.backtodo);
        editTextItem = findViewById(R.id.editTextItem);
        buttonAdd = findViewById(R.id.buttonAdd);
        recyclerView = findViewById(R.id.recyclerView);

        // Initialize empty list
        itemList = new ArrayList<>();

        // Setup adapter
        itemAdapter = new ItemAdapter(itemList, this);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load items from Firebase
        loadTodoItemsFromFirebase();

        // Set up navigation
        setupNavigation();

        // Add button click listener
        buttonAdd.setOnClickListener(v -> addItem());

        // Set up swipe to delete
        setupSwipeToDelete();
    }

    private void setupNavigation() {
        // Back button
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(CheckList.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_tasks);
    }

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(CheckList.this, MainActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (itemId == R.id.nav_diary) {
                    startActivity(new Intent(CheckList.this, JournalActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else if (itemId == R.id.nav_tasks) {
                    // Already in CheckList
                } else if (itemId == R.id.nav_quiz) {
                    startActivity(new Intent(CheckList.this, moodtracker.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
                return true;
            };

    private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deleteItem(viewHolder.getAdapterPosition());
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadTodoItemsFromFirebase() {
        userTodoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String item = snapshot.getValue(String.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CheckList.this, "Failed to load items", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error loading todo items", databaseError.toException());
            }
        });
    }

    private void addItem() {
        String item = editTextItem.getText().toString().trim();
        if (!item.isEmpty()) {
            // Add to local list
            itemList.add(item);
            itemAdapter.notifyItemInserted(itemList.size() - 1);
            editTextItem.setText("");
            recyclerView.smoothScrollToPosition(itemList.size() - 1);

            // Save to Firebase
            saveTodoListToFirebase();
        } else {
            Toast.makeText(this, "Please enter an item", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteItem(int position) {
        if (position >= 0 && position < itemList.size()) {
            // Remove from local list
            itemList.remove(position);
            itemAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();

            // Update Firebase
            saveTodoListToFirebase();
        }
    }

    private void saveTodoListToFirebase() {
        Map<String, Object> todoMap = new HashMap<>();
        for (int i = 0; i < itemList.size(); i++) {
            todoMap.put("item" + i, itemList.get(i));
        }

        userTodoRef.setValue(todoMap)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Todo list saved successfully"))
                .addOnFailureListener(e -> {
                    Toast.makeText(CheckList.this, "Failed to save items", Toast.LENGTH_SHORT).show();
                    Log.e("Firebase", "Error saving todo list", e);
                });
    }

    @Override
    public void onItemClick(int position) {
        // Handle item click if needed
    }

    @Override
    public void onItemLongClick(int position) {
        deleteItem(position);
    }

    @Override
    public void onCheckboxChanged(int position, boolean isChecked) {
        String item = itemList.get(position);
        String message = isChecked ? "Completed: " + item : "Unchecked: " + item;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}