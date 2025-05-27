package com.example.greetingcard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String userName;
    private int pfpResId = R.mipmap.pfp_foreground;
    private LineChart lineChart;
    private Button btnMonthly, btnWeekly, btnDaily;
    private int[] dailyMoodData = new int[24];
    private int[] weeklyMoodData = new int[7];
    private int[] monthlyMoodData = new int[32];
    private int currentChartViewMode = 7; // Default to weekly view

    // Firebase variables
    private DatabaseReference userRef;
    private DatabaseReference userTodoRef;
    private FirebaseAuth mAuth;
    private String userUid;

    // UI components
    private RecyclerView dailyTasksRecyclerView;
    private SimpleItemAdapter dailyTasksAdapter;
    private ArrayList<String> todoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.momento4);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginForm.class));
            finish();
            return;
        }
        userUid = currentUser.getUid();

        // Initialize Firebase Database references
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);
        userTodoRef = FirebaseDatabase.getInstance().getReference("users").child(userUid).child("todoItems");

        // Initialize UI components
        initializeUI();
        initializeTodoList();
        setupButtonListeners();
        initializeChart();
        initializeMoodButtons();
        initializeOptionsButton();

        // Setup navigation
        setupBottomNavigation();

        // Load user data
        loadUserData();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_diary) {
                    startActivity(new Intent(MainActivity.this, JournalActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (itemId == R.id.nav_tasks) {
                    startActivity(new Intent(MainActivity.this, CheckList.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (itemId == R.id.nav_quiz) {
                    Intent intent2 = new Intent(MainActivity.this, moodtracker.class);
                    intent2.putExtra("USER_UID", userUid);
                    startActivity(intent2);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                return true;
            };

    private void initializeUI() {
        ImageView pfpImage = findViewById(R.id.pfp);
        TextView textName = findViewById(R.id.textName);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("USER_NAME")) {
                userName = intent.getStringExtra("USER_NAME");
                textName.setText(userName);
            }

            if (intent.hasExtra("PFP_RES_ID")) {
                pfpResId = intent.getIntExtra("PFP_RES_ID", R.mipmap.pfp_foreground);
                pfpImage.setImageResource(pfpResId);
            }
        }
    }

    private void initializeTodoList() {
        dailyTasksRecyclerView = findViewById(R.id.dailyTasksRecyclerView);
        dailyTasksAdapter = new SimpleItemAdapter(todoList);
        dailyTasksRecyclerView.setAdapter(dailyTasksAdapter);
        dailyTasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load todo items from Firebase
        userTodoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                todoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String item = snapshot.getValue(String.class);
                    if (item != null) {
                        todoList.add(item);
                    }
                }
                dailyTasksAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullName")) {
                        userName = dataSnapshot.child("fullName").getValue(String.class);
                        TextView textName = findViewById(R.id.textName);
                        textName.setText(userName);
                    }

                    if (dataSnapshot.hasChild("avatarResId")) {
                        pfpResId = dataSnapshot.child("avatarResId").getValue(Integer.class);
                        ImageView pfpImage = findViewById(R.id.pfp);
                        pfpImage.setImageResource(pfpResId);
                    }

                    if (dataSnapshot.hasChild("moodData")) {
                        loadMoodData(dataSnapshot.child("moodData"));
                    }
                } else {
                    initializeNewUserData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading user data", databaseError.toException());
                Toast.makeText(MainActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoodData(DataSnapshot moodDataSnapshot) {
        dailyMoodData = new int[24];
        weeklyMoodData = new int[7];
        monthlyMoodData = new int[32];

        if (moodDataSnapshot.hasChild("daily")) {
            DataSnapshot dailySnapshot = moodDataSnapshot.child("daily");
            for (DataSnapshot hourSnapshot : dailySnapshot.getChildren()) {
                int hour = Integer.parseInt(hourSnapshot.getKey());
                int mood = hourSnapshot.getValue(Integer.class);
                if (hour >= 0 && hour < 24) {
                    dailyMoodData[hour] = mood;
                }
            }
        }

        if (moodDataSnapshot.hasChild("weekly")) {
            DataSnapshot weeklySnapshot = moodDataSnapshot.child("weekly");
            for (DataSnapshot daySnapshot : weeklySnapshot.getChildren()) {
                int day = Integer.parseInt(daySnapshot.getKey());
                int mood = daySnapshot.getValue(Integer.class);
                if (day >= 0 && day < 7) {
                    weeklyMoodData[day] = mood;
                }
            }
        }

        if (moodDataSnapshot.hasChild("monthly")) {
            DataSnapshot monthlySnapshot = moodDataSnapshot.child("monthly");
            for (DataSnapshot daySnapshot : monthlySnapshot.getChildren()) {
                int day = Integer.parseInt(daySnapshot.getKey());
                int mood = daySnapshot.getValue(Integer.class);
                if (day >= 1 && day <= 31) {
                    monthlyMoodData[day] = mood;
                }
            }
        }

        updateChartData(currentChartViewMode); // Use current view mode
    }

    private void initializeNewUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        Map<String, Object> initialData = new HashMap<>();
        initialData.put("fullName", userName);
        initialData.put("avatarResId", pfpResId);

        // Sample mood data
        Map<String, Integer> daily = new HashMap<>();
        daily.put("9", 5);
        Map<String, Integer> weekly = new HashMap<>();
        weekly.put("0", 6);
        weekly.put("3", 4);
        Map<String, Integer> monthly = new HashMap<>();
        monthly.put("1", 6);
        monthly.put("10", 7);
        monthly.put("15", 5);

        Map<String, Object> moodData = new HashMap<>();
        moodData.put("daily", daily);
        moodData.put("weekly", weekly);
        moodData.put("monthly", monthly);
        initialData.put("moodData", moodData);

        userRef.setValue(initialData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "User data initialized"))
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to initialize user data", e));
    }

    private void setupButtonListeners() {
        Button quiz = findViewById(R.id.quizbtn);
        quiz.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, moodtracker.class);
            intent.putExtra("USER_NAME", userName);
            intent.putExtra("PFP_RES_ID", pfpResId);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        Button checkListButton = findViewById(R.id.clist);
        checkListButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CheckList.class);
            intent.putExtra("USER_UID", userUid);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        userRef.child("moodData").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loadMoodData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error refreshing mood data", databaseError.toException());
            }
        });
    }

    private void initializeChart() {
        lineChart = findViewById(R.id.dashboard_chart);
        btnMonthly = findViewById(R.id.monthly);
        btnWeekly = findViewById(R.id.weekly);
        btnDaily = findViewById(R.id.daily);

        btnMonthly.setOnClickListener(v -> {
            currentChartViewMode = 30;
            updateChartData(currentChartViewMode);
        });
        btnWeekly.setOnClickListener(v -> {
            currentChartViewMode = 7;
            updateChartData(currentChartViewMode);
        });
        btnDaily.setOnClickListener(v -> {
            currentChartViewMode = 24;
            updateChartData(currentChartViewMode);
        });
    }

    private void updateChartData(int dataPoints) {
        ArrayList<Entry> entries = new ArrayList<>();
        int[] dataSet;
        String[] xLabels = new String[dataPoints];

        switch (dataPoints) {
            case 24: // Daily view (hours)
                dataSet = dailyMoodData;
                for (int i = 0; i < 24; i++) {
                    xLabels[i] = String.format(Locale.getDefault(), "%02d:00", i);
                    if (dataSet[i] > 0) {
                        entries.add(new Entry(i, dataSet[i]));
                    }
                }
                break;

            case 7: // Weekly view (days)
                dataSet = weeklyMoodData;
                String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
                System.arraycopy(dayNames, 0, xLabels, 0, 7);
                for (int i = 0; i < 7; i++) {
                    if (dataSet[i] > 0) {
                        entries.add(new Entry(i, dataSet[i]));
                    }
                }
                break;

            case 30: // Monthly view (30 days for safety)
                dataSet = monthlyMoodData;
                for (int i = 1; i <= 30; i++) {
                    xLabels[i - 1] = String.valueOf(i);
                    if (dataSet[i] > 0) {
                        entries.add(new Entry(i - 1, dataSet[i]));
                    }
                }
                break;
        }

        if (entries.size() > 0) {
            LineDataSet lineDataSet = new LineDataSet(entries, "Mood Score");
            lineDataSet.setColor(Color.parseColor("#018786"));
            lineDataSet.setValueTextColor(Color.BLACK);
            lineDataSet.setLineWidth(2f);
            lineDataSet.setCircleRadius(4f);
            lineDataSet.setDrawValues(true);
            lineDataSet.setDrawCircles(true);
            lineDataSet.setDrawHorizontalHighlightIndicator(false);
            lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            lineDataSet.setDrawFilled(false);

            LineData lineData = new LineData(lineDataSet);
            lineChart.setData(lineData);

            // Configure X-axis
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

            if (dataPoints == 24) {
                xAxis.setLabelCount(8, false);
                xAxis.setLabelRotationAngle(-45);
                xAxis.setAxisMinimum(-0.5f);
                xAxis.setAxisMaximum(23.5f);
            } else if (dataPoints == 30) {
                xAxis.setLabelCount(6, false);
                xAxis.setLabelRotationAngle(-45);
                xAxis.setAxisMinimum(-0.5f);
                xAxis.setAxisMaximum(29.5f);
            } else {
                xAxis.setLabelCount(7, false);
                xAxis.setLabelRotationAngle(-30);
                xAxis.setAxisMinimum(-0.5f);
                xAxis.setAxisMaximum(6.5f);
            }

            xAxis.setAvoidFirstLastClipping(true);
            xAxis.setCenterAxisLabels(false);
            xAxis.setSpaceMin(0.5f);
            xAxis.setSpaceMax(0.5f);

            // Configure Y-axis
            lineChart.getAxisLeft().setAxisMinimum(1f);
            lineChart.getAxisLeft().setAxisMaximum(10f);
            lineChart.getAxisLeft().setGranularity(1f);
            lineChart.getAxisRight().setEnabled(false);

            // Other chart configurations
            lineChart.setDescription(null);
            lineChart.getLegend().setEnabled(false);
            lineChart.setTouchEnabled(true);
            lineChart.setDragEnabled(true);
            lineChart.setScaleEnabled(true);
            lineChart.setPinchZoom(true);
            lineChart.setHighlightPerDragEnabled(true);
            lineChart.invalidate();
        } else {
            lineChart.clear();
            lineChart.invalidate();
            Toast.makeText(this, "No data available for this time period", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeMoodButtons() {
        setMoodClickListener(R.id.smile_foreground, "Smile");
        setMoodClickListener(R.id.wow_foreground, "Wow");
        setMoodClickListener(R.id.haha_foreground, "Haha");
        setMoodClickListener(R.id.grr_foreground, "Angry");
        setMoodClickListener(R.id.sleepy_foreground, "Sleepy");
        setMoodClickListener(R.id.hearts_foreground, "Love");
        setMoodClickListener(R.id.confident_foreground, "Cool");
        setMoodClickListener(R.id.confidant_foreground, "Confident");
        setMoodClickListener(R.id.cry_foreground, "Cry");
    }

    private void setMoodClickListener(int imageViewId, String moodName) {
        findViewById(imageViewId).setOnClickListener(v -> {
            String message = getToastMessageForMood(moodName);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            recordMoodSelection(moodName);
        });
    }

    private String getToastMessageForMood(String moodName) {
        switch (moodName) {
            case "Smile": return "Spread the joy! 😊";
            case "Wow": return "Life is full of wonders! ✨";
            case "Haha": return "Laughter is the best medicine! 😂";
            case "Angry": return "Take a deep breath... you've got this! 💪";
            case "Sleepy": return "Rest is fuel for the soul... 💤";
            case "Love": return "Love makes the world go round! ❤️";
            case "Cool": return "You're looking cool today! 😎";
            case "Confident": return "You're shining bright today! 🌟";
            case "Cry": return "Tears water the garden of the soul... 🌱";
            default: return "Mood acknowledged!";
        }
    }

    private void recordMoodSelection(String moodName) {
        int moodValue = mapMoodNameToValue(moodName);
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        Map<String, Object> updates = new HashMap<>();
        updates.put("moodData/daily/" + currentHour, moodValue);
        updates.put("moodData/weekly/" + currentDayOfWeek, moodValue);
        updates.put("moodData/monthly/" + currentDayOfMonth, moodValue);
        updates.put("stats/moodEntries", ServerValue.increment(1));

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    dailyMoodData[currentHour] = moodValue;
                    weeklyMoodData[currentDayOfWeek] = moodValue;
                    monthlyMoodData[currentDayOfMonth] = moodValue;
                    updateChartData(currentChartViewMode); // Use current view mode
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to record mood", Toast.LENGTH_SHORT).show();
                });
    }

    private int mapMoodNameToValue(String moodName) {
        switch (moodName) {
            case "Smile": return 8;
            case "Wow": return 9;
            case "Haha": return 7;
            case "Angry": return 3;
            case "Sleepy": return 4;
            case "Love": return 10;
            case "Cool": return 8;
            case "Confident": return 7;
            case "Cry": return 2;
            default: return 5;
        }
    }

    private void initializeOptionsButton() {
        MaterialButton btn = findViewById(R.id.buttonOptions);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainPage.class);
            intent.putExtra("USER_UID", userUid);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        ImageView pfpImage = findViewById(R.id.pfp);
        pfpImage.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginForm.class));
            finish();
        });
    }

    public static class SimpleItemAdapter extends RecyclerView.Adapter<SimpleItemAdapter.ViewHolder> {
        private ArrayList<String> items;

        public SimpleItemAdapter(ArrayList<String> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.simple_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(items.get(position));
        }

        public void updateList(ArrayList<String> newList) {
            items = newList;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textViewItemSimple);
            }
        }
    }
}