package com.example.swapapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swapapp.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;

public class PostSkillActivity extends AppCompatActivity {

    private EditText editSkillTitle, editSkillDesc;
    private Spinner spinnerCategory, spinnerDuration, spinnerLevel, spinnerDelivery;
    private MaterialButton btnPublishSkill;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_skill);

        dbHelper = new DatabaseHelper(this);

        editSkillTitle = findViewById(R.id.editSkillTitle);
        editSkillDesc = findViewById(R.id.editSkillDesc);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDuration = findViewById(R.id.spinnerDuration);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        spinnerDelivery = findViewById(R.id.spinnerDelivery);
        btnPublishSkill = findViewById(R.id.btnPublishSkill);

        // Populate Dropdowns
        String[] categories = {"Technology", "Languages", "Arts & Design", "Fitness", "Business", "Music"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        String[] durations = {"30 Mins", "1 Hour", "1.5 Hours", "2 Hours"};
        spinnerDuration.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, durations));

        String[] levels = {"Beginner", "Intermediate", "Advanced"};
        spinnerLevel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, levels));

        String[] deliveryModes = {"Online (Video Call)", "In-Person"};
        spinnerDelivery.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, deliveryModes));

        btnPublishSkill.setOnClickListener(v -> {
            String title = editSkillTitle.getText().toString().trim();
            String desc = editSkillDesc.getText().toString().trim();
            String cat = spinnerCategory.getSelectedItem().toString();
            String durStr = spinnerDuration.getSelectedItem().toString();
            String lvl = spinnerLevel.getSelectedItem().toString();
            String del = spinnerDelivery.getSelectedItem().toString();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert string to actual minutes
            int durationMins = 60;
            if (durStr.equals("30 Mins")) durationMins = 30;
            else if (durStr.equals("1 Hour")) durationMins = 60;
            else if (durStr.equals("1.5 Hours")) durationMins = 90;
            else if (durStr.equals("2 Hours")) durationMins = 120;

            SharedPreferences prefs = getSharedPreferences("SwapAppPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("CURRENT_USER_ID", -1);

            if (dbHelper.addSkill(userId, title, cat, durationMins, desc, lvl, del)) {
                Toast.makeText(this, "Skill successfully posted!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error posting skill.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}