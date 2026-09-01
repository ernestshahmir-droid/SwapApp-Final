package com.example.swapapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swapapp.database.DatabaseHelper;

public class SkillDetailActivity extends AppCompatActivity {

    private TextView tvDetailTitle, tvDetailCategory, tvDetailDuration, tvDetailDescription;
    private Button btnConfirmRequest;

    private DatabaseHelper dbHelper;
    private int currentUserId;
    private int targetSkillId = -1;
    private int providerUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_detail); // Make sure this matches your layout name!

        // Link your layout IDs (Check these match your XML file!)
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailDuration = findViewById(R.id.tvDetailDuration);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        btnConfirmRequest = findViewById(R.id.btnConfirmRequest);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("SwapAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("CURRENT_USER_ID", -1);

        // Get the skill ID that was passed from the Adapter
        targetSkillId = getIntent().getIntExtra("SKILL_ID", -1);

        if (targetSkillId != -1) {
            loadSkillDetails();
        } else {
            Toast.makeText(this, "Error loading skill", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnConfirmRequest.setOnClickListener(v -> {
            if (providerUserId == -1) {
                Toast.makeText(this, "Error finding skill owner.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (providerUserId == currentUserId) {
                Toast.makeText(this, "You cannot request your own skill!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt to deduct 2 credits and create the transaction
            boolean success = dbHelper.requestSession(currentUserId, providerUserId, targetSkillId);

            if (success) {
                Toast.makeText(this, "Request Sent! 2 Time Credits reserved.", Toast.LENGTH_LONG).show();
                finish(); // Sends you back to the feed
            } else {
                Toast.makeText(this, "Request failed. Do you have at least 2 Time Credits?", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSkillDetails() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT user_id, title, category, duration_hrs, description FROM SKILLS WHERE skill_id = ?", new String[]{String.valueOf(targetSkillId)});

        if (c.moveToFirst()) {
            providerUserId = c.getInt(0); // The person who owns the skill
            tvDetailTitle.setText(c.getString(1));
            tvDetailCategory.setText(c.getString(2));

            int d = c.getInt(3);
            if (d <= 12) d = d * 60; // Fixes legacy hour data
            tvDetailDuration.setText(d + " mins");

            tvDetailDescription.setText(c.getString(4));
        }
        c.close();
    }
}