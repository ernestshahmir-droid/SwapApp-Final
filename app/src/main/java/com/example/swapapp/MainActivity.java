package com.example.swapapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnBrowseSkills;
    private MaterialButton btnPostSkill;
    private MaterialButton btnSessions;
    private MaterialButton btnWallet;
    private MaterialButton btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect buttons
        btnBrowseSkills = findViewById(R.id.btnBrowseSkills);
        btnPostSkill = findViewById(R.id.btnPostSkill);
        btnSessions = findViewById(R.id.btnSessions);
        btnWallet = findViewById(R.id.btnWallet);
        btnProfile = findViewById(R.id.btnProfile);

        // Browse Skills
        btnBrowseSkills.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SkillFeedActivity.class
            );
            startActivity(intent);
        });

        // Post Skill
        btnPostSkill.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    PostSkillActivity.class
            );
            startActivity(intent);
        });

        // Sessions
        btnSessions.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SessionsActivity.class
            );
            startActivity(intent);
        });

        // Wallet
        btnWallet.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    WalletActivity.class
            );
            startActivity(intent);
        });

        // Profile
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ProfileActivity.class
            );
            startActivity(intent);
        });
    }
}