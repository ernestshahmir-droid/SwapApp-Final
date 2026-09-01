package com.example.swapapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapapp.database.DatabaseHelper;
import com.example.swapapp.models.Skill;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView textProfileAvatar, textProfileName, textProfileEmail;
    private TextView textStatCredits, textStatSkills, textStatSessions, textStatRating;
    private ImageView btnEditProfile;
    private Button btnLogout;
    private RecyclerView rvMySkills, rvMyReviews;

    private DatabaseHelper dbHelper;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textProfileAvatar = findViewById(R.id.textProfileAvatar);
        textProfileName = findViewById(R.id.textProfileName);
        textProfileEmail = findViewById(R.id.textProfileEmail);

        textStatCredits = findViewById(R.id.textStatCredits);
        textStatSkills = findViewById(R.id.textStatSkills);
        textStatRating = findViewById(R.id.textStatRating);
        textStatSessions = findViewById(R.id.textStatSessions);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        rvMySkills = findViewById(R.id.rvMySkills);
        rvMyReviews = findViewById(R.id.rvMyReviews);

        rvMySkills.setLayoutManager(new LinearLayoutManager(this));
        rvMyReviews.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs =
                getSharedPreferences("SwapAppPrefs", Context.MODE_PRIVATE);

        currentUserId = prefs.getInt("CURRENT_USER_ID", -1);

        if (currentUserId == -1) {

            Intent intent = new Intent(
                    ProfileActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
            return;
        }

        // EDIT PROFILE
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // SIGN OUT
        btnLogout.setOnClickListener(v -> {

            prefs.edit().clear().apply();

            Toast.makeText(
                    ProfileActivity.this,
                    "Signed out successfully",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    ProfileActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentUserId != -1) {
            loadProfileData();
        }
    }

    private void loadProfileData() {

        Cursor userCursor = dbHelper.getUserDetails(currentUserId);

        if (userCursor.moveToFirst()) {

            String name = userCursor.getString(0);

            textProfileName.setText(name);
            textProfileEmail.setText(userCursor.getString(1));

            textStatCredits.setText(
                    String.valueOf(userCursor.getInt(2))
            );

            if (name != null && !name.isEmpty()) {

                textProfileAvatar.setText(
                        String.valueOf(name.charAt(0)).toUpperCase()
                );
            }
        }

        userCursor.close();

        textStatSkills.setText(
                String.valueOf(
                        dbHelper.getUserSkillCount(currentUserId)
                )
        );

        textStatSessions.setText(
                String.valueOf(
                        dbHelper.getCompletedSessionCount(currentUserId)
                )
        );

        float avgRating =
                dbHelper.getAverageRating(currentUserId);

        textStatRating.setText(
                avgRating > 0
                        ? String.format("%.1f ★", avgRating)
                        : "New"
        );

        loadMySkills();
        loadMyReviews();
    }

    private void loadMySkills() {

        List<Skill> mySkills = new ArrayList<>();

        Cursor c = dbHelper.getUserSkills(currentUserId);

        if (c.moveToFirst()) {

            do {

                int skillId = c.getInt(0);
                int userId = c.getInt(1);
                String title = c.getString(2);
                String category = c.getString(3);
                int duration = c.getInt(4);

                mySkills.add(
                        new Skill(
                                skillId,
                                userId,
                                title,
                                category,
                                duration
                        )
                );

            } while (c.moveToNext());
        }

        c.close();

        rvMySkills.setAdapter(
                new MyProfileSkillAdapter(mySkills)
        );
    }

    private void loadMyReviews() {

        List<ReviewModel> reviews = new ArrayList<>();

        Cursor c = dbHelper.getUserReviews(currentUserId);

        if (c.moveToFirst()) {

            do {

                reviews.add(
                        new ReviewModel(
                                c.getInt(0),
                                c.getString(1),
                                c.getString(2)
                        )
                );

            } while (c.moveToNext());
        }

        c.close();

        rvMyReviews.setAdapter(
                new ReviewAdapter(reviews)
        );
    }

    private void showEditProfileDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText inputName =
                new EditText(this);

        inputName.setHint("Update Full Name");

        inputName.setText(
                textProfileName.getText().toString()
        );

        layout.addView(inputName);

        final EditText inputEmail =
                new EditText(this);

        inputEmail.setHint("Update Email");

        inputEmail.setText(
                textProfileEmail.getText().toString()
        );

        inputEmail.setPadding(
                inputEmail.getPaddingLeft(),
                30,
                inputEmail.getPaddingRight(),
                inputEmail.getPaddingBottom()
        );

        layout.addView(inputEmail);

        builder.setView(layout);

        builder.setPositiveButton(
                "Save",
                (dialog, which) -> {

                    String newName =
                            inputName
                                    .getText()
                                    .toString()
                                    .trim();

                    String newEmail =
                            inputEmail
                                    .getText()
                                    .toString()
                                    .trim();

                    if (!newName.isEmpty() &&
                            !newEmail.isEmpty()) {

                        if (dbHelper.updateUserProfile(
                                currentUserId,
                                newName,
                                newEmail)) {

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Profile Updated",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadProfileData();

                        } else {

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Update failed. Email may already be in use.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
        );

        builder.setNegativeButton(
                "Cancel",
                null
        );

        builder.show();
    }

    class MyProfileSkillAdapter
            extends RecyclerView.Adapter<
            MyProfileSkillAdapter.SkillViewHolder> {

        List<Skill> skills;

        MyProfileSkillAdapter(List<Skill> skills) {
            this.skills = skills;
        }

        @NonNull
        @Override
        public SkillViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType) {

            return new SkillViewHolder(
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(
                                    R.layout.item_skill,
                                    parent,
                                    false
                            )
            );
        }

        @Override
        public void onBindViewHolder(
                @NonNull SkillViewHolder holder,
                int position) {

            Skill skill = skills.get(position);

            holder.tvTitle.setText(skill.getTitle());
            holder.tvCategory.setText(skill.getCategory());

            int d = skill.getDurationHrs();

            if (d <= 12) {
                d = d * 60;
            }

            String timeText;

            if (d < 60) {

                timeText = d + " mins";

            } else if (d == 60) {

                timeText = "1 hr";

            } else if (d % 60 == 0) {

                timeText = (d / 60) + " hrs";

            } else {

                timeText =
                        String.format(
                                "%.1f hrs",
                                d / 60.0
                        );
            }

            holder.tvDuration.setText(timeText);

            if (holder.btnRequestTrade != null) {

                holder.btnRequestTrade.setVisibility(View.VISIBLE);

                holder.btnRequestTrade.setText("Delete Skill");

                holder.btnRequestTrade.setBackgroundColor(
                        Color.parseColor("#DC3545")
                );

                holder.btnRequestTrade.setOnClickListener(v -> {

                    new AlertDialog.Builder(ProfileActivity.this)

                            .setTitle("Delete Skill")

                            .setMessage(
                                    "Are you sure you want to delete this skill? This cannot be undone."
                            )

                            .setPositiveButton(
                                    "Delete",
                                    (dialog, which) -> {

                                        if (dbHelper.deleteSkill(
                                                skill.getSkillId()
                                        )) {

                                            Toast.makeText(
                                                    ProfileActivity.this,
                                                    "Skill deleted",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            loadProfileData();
                                        }
                                    }
                            )

                            .setNegativeButton(
                                    "Cancel",
                                    null
                            )

                            .show();
                });
            }
        }

        @Override
        public int getItemCount() {
            return skills.size();
        }

        class SkillViewHolder
                extends RecyclerView.ViewHolder {

            TextView tvTitle;
            TextView tvCategory;
            TextView tvDuration;

            Button btnRequestTrade;

            SkillViewHolder(@NonNull View itemView) {

                super(itemView);

                tvTitle =
                        itemView.findViewById(
                                R.id.tvItemTitle
                        );

                tvCategory =
                        itemView.findViewById(
                                R.id.tvItemCategory
                        );

                tvDuration =
                        itemView.findViewById(
                                R.id.tvItemDuration
                        );

                btnRequestTrade =
                        itemView.findViewById(
                                R.id.btnRequestTrade
                        );
            }
        }
    }

    class ReviewModel {

        int stars;
        String partnerName;
        String skillTitle;

        ReviewModel(
                int stars,
                String partnerName,
                String skillTitle) {

            this.stars = stars;
            this.partnerName = partnerName;
            this.skillTitle = skillTitle;
        }
    }

    class ReviewAdapter
            extends RecyclerView.Adapter<
            ReviewAdapter.ViewHolder> {

        List<ReviewModel> items;

        ReviewAdapter(List<ReviewModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType) {

            return new ViewHolder(
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(
                                    R.layout.item_review,
                                    parent,
                                    false
                            )
            );
        }

        @Override
        public void onBindViewHolder(
                @NonNull ViewHolder holder,
                int position) {

            ReviewModel r = items.get(position);

            holder.name.setText(
                    "From: " + r.partnerName
            );

            holder.skill.setText(
                    "Session: " + r.skillTitle
            );

            holder.stars.setRating(r.stars);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder
                extends RecyclerView.ViewHolder {

            TextView name;
            TextView skill;
            RatingBar stars;

            ViewHolder(@NonNull View v) {

                super(v);

                name =
                        v.findViewById(
                                R.id.textReviewerName
                        );

                skill =
                        v.findViewById(
                                R.id.textReviewSkill
                        );

                stars =
                        v.findViewById(
                                R.id.ratingBarReview
                        );
            }
        }
    }
}