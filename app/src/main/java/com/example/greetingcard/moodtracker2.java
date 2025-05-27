package com.example.greetingcard;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;



public class moodtracker2 extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_tracker2);


        String userName = getIntent().getStringExtra("USER_NAME");
        int pfpResId = getIntent().getIntExtra("PFP_RES_ID", R.mipmap.pfp_foreground);
        //get EditText fields
        EditText care = findViewById(R.id.care);
        EditText describe = findViewById(R.id.describe);
        EditText inspire = findViewById(R.id.inspire);
        EditText safe = findViewById(R.id.safe);
        EditText hard= findViewById(R.id.hard);
        EditText love = findViewById(R.id.loveletter);

        //next button "finished"
        Button finishButton = findViewById(R.id.nextbutton);

        //receive data from previous activity
        Intent intent = getIntent();
        int moodProgress = intent.getIntExtra("MOOD_PROGRESS", 50);


        finishButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // entries
                String careText = care.getText().toString();
                String describeText = describe.getText().toString();
                String inspireText = inspire.getText().toString();
                String safeText = safe.getText().toString();
                String hardText = hard.getText().toString();
                String loveLetterText = love.getText().toString();


                // intent to pass data tonext activity
                Intent affirm_intent = new Intent(moodtracker2.this, AffirmationActivity.class);
                affirm_intent.putExtra("USER_NAME", userName);
                affirm_intent.putExtra("PFP_RES_ID", pfpResId);
                startActivity(affirm_intent);
            }
        });
    }
}