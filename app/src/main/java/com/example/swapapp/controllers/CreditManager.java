package com.example.swapapp.controllers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.swapapp.database.DatabaseHelper;

public class CreditManager {

    private DatabaseHelper dbHelper;

    public CreditManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Atomic transaction: Deducts 1 hour from Learner, adds 1 hour to Teacher
    public boolean processCreditTransfer(int learnerId, int teacherId, int hours) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Begin ACID Transaction
        db.beginTransaction();
        try {
            // 1. Deduct from Learner
            db.execSQL("UPDATE USERS SET time_credits = time_credits - ? WHERE user_id = ?",
                    new Object[]{hours, learnerId});

            // 2. Add to Teacher
            db.execSQL("UPDATE USERS SET time_credits = time_credits + ? WHERE user_id = ?",
                    new Object[]{hours, teacherId});

            // Mark successful if both operations complete without error
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // Ends transaction (commits if successful, rolls back if failed)
            db.endTransaction();
            db.close();
        }
    }
}