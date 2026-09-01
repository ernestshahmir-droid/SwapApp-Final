package com.example.swapapp.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.swapapp.database.DatabaseHelper;
import com.example.swapapp.models.User;

public class UserDAO {
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Fetch a single user by their ID
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE user_id = ?",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            int credits = cursor.getInt(cursor.getColumnIndexOrThrow("time_credits"));

            user = new User(userId, name, email, credits);
        }

        cursor.close();
        db.close();

        return user;
    }
}