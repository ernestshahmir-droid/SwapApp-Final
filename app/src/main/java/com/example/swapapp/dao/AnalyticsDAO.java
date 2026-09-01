package com.example.swapapp.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.swapapp.database.DatabaseHelper;

public class AnalyticsDAO {
    private DatabaseHelper dbHelper;

    public AnalyticsDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 1. Calculate Total Hours Taught or Learned based on completed sessions
    public int getTotalHours(int userId, boolean isTeacher) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String role = isTeacher ? "teacher_id" : "learner_id";

        // Joins SESSIONS and SKILLS to find the total duration
        String query = "SELECT SUM(s.duration_hrs) FROM SESSIONS sess " +
                "JOIN SKILLS s ON sess.skill_id = s.skill_id " +
                "WHERE sess." + role + " = ? AND sess.status = 'completed'";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int hours = 0;

        if (cursor.moveToFirst() && cursor.getString(0) != null) {
            hours = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return hours;
    }

    // 2. Calculate the Average Rating out of 5.0
    public float getAverageRating(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(score) FROM RATINGS WHERE reviewee_id = ?",
                new String[]{String.valueOf(userId)});
        float avg = 0;

        if (cursor.moveToFirst() && cursor.getString(0) != null) {
            avg = cursor.getFloat(0);
        }
        cursor.close();
        db.close();

        // Round to 1 decimal place
        return Math.round(avg * 10.0f) / 10.0f;
    }

    // 3. Find their most actively learned/taught category
    public String getMostPopularCategory(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT s.category, COUNT(s.category) as cat_count FROM SESSIONS sess " +
                "JOIN SKILLS s ON sess.skill_id = s.skill_id " +
                "WHERE (sess.teacher_id = ? OR sess.learner_id = ?) AND sess.status = 'completed' " +
                "GROUP BY s.category ORDER BY cat_count DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(userId)});
        String category = "N/A";

        if (cursor.moveToFirst()) {
            category = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return category;
    }
}