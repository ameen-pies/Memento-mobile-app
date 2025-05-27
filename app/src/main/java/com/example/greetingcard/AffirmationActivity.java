package com.example.greetingcard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class AffirmationActivity extends AppCompatActivity{
    private ImageView progressBar1, progressBar2, progressBar3;
    private int progressCount = 0;
    private Button nextbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affirmation);

        String userName = getIntent().getStringExtra("USER_NAME");
        int pfpResId = getIntent().getIntExtra("PFP_RES_ID", R.mipmap.pfp_foreground);

        progressBar1 = findViewById(R.id.progress_bar_1);
        progressBar2 = findViewById(R.id.progress_bar_2);
        progressBar3 = findViewById(R.id.progress_bar_3);
        nextbutton = findViewById(R.id.nextbutton);

        Button loveButton = findViewById(R.id.lovebutton);
        Button worthyButton = findViewById(R.id.worthybutton);
        Button bestButton = findViewById(R.id.bestbutton);

        loveButton.setOnClickListener(v -> updateProgressBar());
        worthyButton.setOnClickListener(v -> updateProgressBar());
        bestButton.setOnClickListener(v -> updateProgressBar());

        nextbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AffirmationActivity.this, MainActivity.class);
                intent.putExtra("USER_NAME", userName);
                intent.putExtra("PFP_RES_ID", pfpResId);
                startActivity(intent);
            }
        });
    }



    private void updateProgressBar(){
        progressCount++;
        if (progressCount == 1) {
            progressBar1.setBackgroundColor(Color.YELLOW);
        } else if (progressCount == 2) {
            progressBar2.setBackgroundColor(Color.YELLOW);
        } else if (progressCount >= 3) {
            progressBar3.setBackgroundColor(Color.YELLOW);
        }
    }

}