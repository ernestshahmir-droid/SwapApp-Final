package com.example.swapapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.swapapp.database.DatabaseHelper;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import com.example.swapapp.models.Skill;

public class SkillDAO {
    private DatabaseHelper dbHelper;

    public SkillDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Method to insert a new skill into the database
    public boolean insertSkill(int userId, String title, String category, int durationHrs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("user_id", userId);
        values.put("title", title);
        values.put("category", category);
        values.put("duration_hrs", durationHrs);

        // insert() returns -1 if an error occurred
        long result = db.insert("SKILLS", null, values);
        db.close();

        return result != -1;
    }
    public List<Skill> getAllSkills() {
        List<Skill> skillList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get all skills
        Cursor cursor = db.rawQuery("SELECT * FROM SKILLS", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("skill_id"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration_hrs"));

                Skill skill = new Skill(id, userId, title, category, duration);
                skillList.add(skill);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return skillList;
    }
}
