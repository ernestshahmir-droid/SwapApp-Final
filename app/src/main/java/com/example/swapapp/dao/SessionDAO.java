package com.example.swapapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.swapapp.database.DatabaseHelper;
import com.example.swapapp.models.Session;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {
    private DatabaseHelper dbHelper;

    public SessionDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // THIS IS THE ONLY REQUEST SESSION METHOD
    public boolean requestSession(int skillId, int learnerId, int teacherId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("skill_id", skillId);
        values.put("learner_id", learnerId);
        values.put("teacher_id", teacherId);
        values.put("status", "pending");

        long result = db.insert("SESSIONS", null, values);
        db.close();

        return result != -1;
    }

    public boolean updateSessionStatus(int sessionId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        int rows = db.update("SESSIONS", values, "session_id = ?", new String[]{String.valueOf(sessionId)});
        db.close();
        return rows > 0;
    }

    public List<Session> getSessionsForUser(int userId) {
        List<Session> sessionList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get sessions where user is either the teacher or the learner
        Cursor cursor = db.rawQuery("SELECT * FROM SESSIONS WHERE teacher_id = ? OR learner_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("session_id"));
                int skillId = cursor.getInt(cursor.getColumnIndexOrThrow("skill_id"));
                int learnerId = cursor.getInt(cursor.getColumnIndexOrThrow("learner_id"));
                int teacherId = cursor.getInt(cursor.getColumnIndexOrThrow("teacher_id"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                sessionList.add(new Session(id, skillId, learnerId, teacherId, status, "N/A"));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sessionList;
    }
}