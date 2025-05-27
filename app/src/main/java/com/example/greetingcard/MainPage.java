package com.example.greetingcard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public class MainPage extends AppCompatActivity {

    private TextView quoteTextView;
    private Handler quoteHandler = new Handler();
    private RequestQueue requestQueue;
    private Runnable quoteRunnable;
    private static final String ZEN_QUOTES_URL = "https://zenquotes.io/api/quotes";
    private Random random = new Random();
    private JSONArray fetchedQuotes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.momento);

        MaterialButton startButton = findViewById(R.id.start);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainPage.this, LoginForm.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        quoteTextView = findViewById(R.id.textView4);
        requestQueue = Volley.newRequestQueue(this);

        fetchQuotesFromZenQuotes();
    }

    private void fetchQuotesFromZenQuotes() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, ZEN_QUOTES_URL, null,
                response -> {
                    fetchedQuotes = response;
                    startQuoteRotation();
                },
                error -> {
                    Log.e("QuoteError", "Failed to fetch ZenQuotes: " + error.toString());
                    fadeText(quoteTextView, "Keep calm and carry on.");
                }
        );

        requestQueue.add(jsonArrayRequest);
    }

    private void startQuoteRotation() {
        quoteRunnable = new Runnable() {
            @Override
            public void run() {
                showRandomQuote();
                quoteHandler.postDelayed(this, 10000); // Rotate every 15 seconds
            }
        };
        quoteHandler.post(quoteRunnable);
    }

    private void showRandomQuote() {
        try {
            if (fetchedQuotes != null && fetchedQuotes.length() > 0) {
                int index = random.nextInt(fetchedQuotes.length());
                JSONObject quoteObj = fetchedQuotes.getJSONObject(index);
                String content = quoteObj.getString("q");
                String author = quoteObj.getString("a");
                String fullQuote = "\"" + content + "\"\n– " + author;

                fadeText(quoteTextView, fullQuote);
            } else {
                fadeText(quoteTextView, "You are enough.");
            }
        } catch (Exception e) {
            Log.e("ParseError", "Failed to parse ZenQuote: " + e.getMessage());
        }
    }

    private void fadeText(TextView textView, String newText) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(500);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {
                textView.setText(newText);
                textView.startAnimation(fadeIn);
            }
        });

        textView.startAnimation(fadeOut);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        quoteHandler.removeCallbacks(quoteRunnable);
    }
}
