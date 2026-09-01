package com.example.swapapp.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.swapapp.database.DatabaseHelper;

public class AuthManager {

    private DatabaseHelper dbHelper;

    public AuthManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Process: Register a new user
    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Storing basic credentials. (In a production app, password would be hashed here)
        values.put("name", "New User");
        values.put("email", email);
        values.put("password_hash", password);

        // insert() returns -1 if there is an error (like a duplicate email)
        long result = db.insert("USERS", null, values);
        db.close();

        return result != -1;
    }

    // Process: Validate login credentials and return User ID
    public int validateLogin(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int userId = -1; // Default to -1 (meaning login failed)

        Cursor cursor = db.rawQuery("SELECT user_id FROM USERS WHERE email=? AND password_hash=?",
                new String[]{email, password});

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
        }

        cursor.close();
        db.close();

        return userId;
    }
}