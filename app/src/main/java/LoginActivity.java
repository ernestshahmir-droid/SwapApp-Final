package com.example.swapapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swapapp.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    private Button btnLogin;
    private Button btnRegister;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        dbHelper = new DatabaseHelper(this);

        // LOGIN
        btnLogin.setOnClickListener(v -> loginUser());

        // REGISTER
        btnRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        // IMPORTANT:
        // Gets the REAL user ID from SQLite.
        int userId = dbHelper.loginUser(email, password);

        if (userId == -1) {

            Toast.makeText(
                    LoginActivity.this,
                    "Incorrect email or password",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Save REAL logged-in user ID.
        SharedPreferences prefs =
                getSharedPreferences(
                        "SwapAppPrefs",
                        Context.MODE_PRIVATE
                );

        prefs.edit()
                .putInt("CURRENT_USER_ID", userId)
                .putString("USER_EMAIL", email)
                .apply();

        Toast.makeText(
                LoginActivity.this,
                "Login successful",
                Toast.LENGTH_SHORT
        ).show();

        // LOGIN -> HOME
        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}