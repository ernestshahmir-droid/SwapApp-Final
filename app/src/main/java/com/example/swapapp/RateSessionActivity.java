package com.example.swapapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swapapp.dao.RatingDAO;

public class RateSessionActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmitRating;
    private RatingDAO ratingDAO;

    private int sessionId;
    private int revieweeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_session);

        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmitRating = findViewById(R.id.btnSubmitRating);

        ratingDAO = new RatingDAO(this);

        // Fetch the Session ID and the ID of the person being reviewed from the Intent
        sessionId = getIntent().getIntExtra("SESSION_ID", -1);
        revieweeId = getIntent().getIntExtra("REVIEWEE_ID", -1);

        btnSubmitRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitUserRating();
            }
        });
    }

    private void submitUserRating() {
        float score = ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (score == 0) {
            Toast.makeText(this, "Please select a star rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current logged-in user (the reviewer)
        SharedPreferences prefs = getSharedPreferences("SwapAppPrefs", MODE_PRIVATE);
        int reviewerId = prefs.getInt("CURRENT_USER_ID", -1);

        boolean isSubmitted = ratingDAO.submitRating(sessionId, reviewerId, revieweeId, score, comment);

        if (isSubmitted) {
            Toast.makeText(this, "Rating submitted successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close the rating screen and return to the previous screen
        } else {
            Toast.makeText(this, "Error submitting rating", Toast.LENGTH_SHORT).show();
        }
    }
}