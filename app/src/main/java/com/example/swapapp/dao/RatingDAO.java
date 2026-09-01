package com.example.swapapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.swapapp.database.DatabaseHelper;

public class RatingDAO {
    private DatabaseHelper dbHelper;

    public RatingDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean submitRating(int sessionId, int reviewerId, int revieweeId, float score, String comment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("session_id", sessionId);
        values.put("reviewer_id", reviewerId);
        values.put("reviewee_id", revieweeId);
        values.put("score", score);
        values.put("comment", comment);

        long result = db.insert("RATINGS", null, values);
        db.close();

        return result != -1;
    }
}