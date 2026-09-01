package com.example.swapapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapapp.adapters.SkillAdapter;
import com.example.swapapp.database.DatabaseHelper;
import com.example.swapapp.models.Skill;

import java.util.ArrayList;
import java.util.List;

public class SkillFeedActivity extends AppCompatActivity {

    private RecyclerView rvSkills;
    private SkillAdapter adapter;
    private DatabaseHelper dbHelper;

    private final List<Skill> skillList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_feed);

        rvSkills = findViewById(R.id.rvSkills);

        rvSkills.setLayoutManager(
                new LinearLayoutManager(this)
        );

        dbHelper = new DatabaseHelper(this);

        adapter = new SkillAdapter(
                this,
                skillList
        );

        rvSkills.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadSkills();
    }

    private void loadSkills() {

        skillList.clear();

        Cursor cursor = null;

        try {

            cursor = dbHelper
                    .getReadableDatabase()
                    .rawQuery(
                            "SELECT * FROM SKILLS",
                            null
                    );

            if (cursor.moveToFirst()) {

                do {

                    int skillId = cursor.getInt(0);
                    int userId = cursor.getInt(1);
                    String title = cursor.getString(2);
                    String category = cursor.getString(3);
                    int duration = cursor.getInt(4);

                    Skill skill = new Skill(
                            skillId,
                            userId,
                            title,
                            category,
                            duration
                    );

                    skillList.add(skill);

                } while (cursor.moveToNext());
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Unable to load skills",
                    Toast.LENGTH_SHORT
            ).show();

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }
}