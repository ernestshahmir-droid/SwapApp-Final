package com.example.swapapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapapp.R;
import com.example.swapapp.database.DatabaseHelper;
import com.example.swapapp.models.Skill;

import java.util.List;

public class SkillAdapter
        extends RecyclerView.Adapter<SkillAdapter.SkillViewHolder> {

    private final Context context;
    private final List<Skill> skillList;

    private final DatabaseHelper dbHelper;
    private final int currentUserId;

    public SkillAdapter(
            Context context,
            List<Skill> skillList) {

        this.context = context;
        this.skillList = skillList;

        dbHelper = new DatabaseHelper(context);

        SharedPreferences prefs =
                context.getSharedPreferences(
                        "SwapAppPrefs",
                        Context.MODE_PRIVATE
                );

        currentUserId =
                prefs.getInt(
                        "CURRENT_USER_ID",
                        -1
                );
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_skill,
                                parent,
                                false
                        );

        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull SkillViewHolder holder,
            int position) {

        Skill skill =
                skillList.get(position);

        // ======================================
        // BASIC SKILL INFORMATION
        // ======================================

        holder.tvTitle.setText(
                skill.getTitle()
        );

        holder.tvCategory.setText(
                skill.getCategory()
        );

        // ======================================
        // DURATION
        // ======================================

        int duration =
                skill.getDurationHrs();

        String durationText;

        /*
         * Your database field is duration_hrs,
         * but your addSkill method currently
         * stores the duration value provided
         * by the Post Skill screen.
         */

        if (duration < 60) {

            durationText =
                    duration + " mins";

        } else if (duration == 60) {

            durationText =
                    "1 hr";

        } else if (duration % 60 == 0) {

            durationText =
                    (duration / 60)
                            + " hrs";

        } else {

            int hours =
                    duration / 60;

            int mins =
                    duration % 60;

            durationText =
                    hours + " hr "
                            + mins + " mins";
        }

        holder.tvDuration.setText(
                durationText
        );


        // ======================================
        // FIND REAL OWNER FROM DATABASE
        // ======================================

        int ownerId =
                dbHelper.getSkillOwnerId(
                        skill.getSkillId()
                );

        String ownerName =
                dbHelper.getSkillOwnerName(
                        skill.getSkillId()
                );


        // ======================================
        // SHOW POSTED BY
        // ======================================

        holder.tvPostedBy.setVisibility(
                View.VISIBLE
        );


        // ======================================
        // MY OWN SKILL
        // ======================================

        if (ownerId == currentUserId) {

            holder.tvPostedBy.setText(
                    "Posted by: You ("
                            + ownerName
                            + ")"
            );

            holder.btnRequestTrade.setText(
                    "YOUR SKILL"
            );

            holder.btnRequestTrade.setEnabled(
                    false
            );

            holder.btnRequestTrade.setAlpha(
                    0.55f
            );

            holder.btnRequestTrade
                    .setOnClickListener(null);

            return;
        }


        // ======================================
        // SOMEONE ELSE'S SKILL
        // ======================================

        holder.tvPostedBy.setText(
                "Posted by: "
                        + ownerName
        );


        // ======================================
        // ALREADY REQUESTED
        // ======================================

        if (dbHelper.hasActiveRequest(
                currentUserId,
                skill.getSkillId())) {

            holder.btnRequestTrade.setText(
                    "REQUESTED"
            );

            holder.btnRequestTrade.setEnabled(
                    false
            );

            holder.btnRequestTrade.setAlpha(
                    0.55f
            );

            holder.btnRequestTrade
                    .setOnClickListener(null);

            return;
        }


        // ======================================
        // AVAILABLE TO REQUEST
        // ======================================

        holder.btnRequestTrade.setText(
                "REQUEST SESSION"
        );

        holder.btnRequestTrade.setEnabled(
                true
        );

        holder.btnRequestTrade.setAlpha(
                1.0f
        );


        // ======================================
        // REQUEST BUTTON
        // ======================================

        holder.btnRequestTrade
                .setOnClickListener(v -> {

                    if (currentUserId == -1) {

                        Toast.makeText(
                                context,
                                "Please login again",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    // =================================
                    // EXTRA OWN-SKILL PROTECTION
                    // =================================

                    int actualOwnerId =
                            dbHelper.getSkillOwnerId(
                                    skill.getSkillId()
                            );


                    if (actualOwnerId ==
                            currentUserId) {

                        Toast.makeText(
                                context,
                                "You cannot request your own skill",
                                Toast.LENGTH_LONG
                        ).show();

                        holder.btnRequestTrade
                                .setText(
                                        "YOUR SKILL"
                                );

                        holder.btnRequestTrade
                                .setEnabled(false);

                        return;
                    }


                    // =================================
                    // DUPLICATE REQUEST PROTECTION
                    // =================================

                    if (dbHelper.hasActiveRequest(
                            currentUserId,
                            skill.getSkillId())) {

                        Toast.makeText(
                                context,
                                "You already requested this skill",
                                Toast.LENGTH_SHORT
                        ).show();

                        holder.btnRequestTrade
                                .setText(
                                        "REQUESTED"
                                );

                        holder.btnRequestTrade
                                .setEnabled(false);

                        return;
                    }


                    // =================================
                    // CREATE SESSION REQUEST
                    // =================================

                    boolean success =
                            dbHelper.requestSession(
                                    currentUserId,
                                    actualOwnerId,
                                    skill.getSkillId()
                            );


                    if (success) {

                        Toast.makeText(
                                context,
                                "Session request sent to "
                                        + ownerName,
                                Toast.LENGTH_LONG
                        ).show();


                        holder.btnRequestTrade
                                .setText(
                                        "REQUESTED"
                                );

                        holder.btnRequestTrade
                                .setEnabled(false);

                        holder.btnRequestTrade
                                .setAlpha(
                                        0.55f
                                );

                    } else {

                        Toast.makeText(
                                context,
                                "Unable to request session. "
                                        + "You may already have a request "
                                        + "or not enough credits.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    @Override
    public int getItemCount() {

        return skillList.size();
    }


    // ==========================================
    // VIEW HOLDER
    // ==========================================

    static class SkillViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvCategory;
        TextView tvDuration;

        TextView tvPostedBy;

        Button btnRequestTrade;

        SkillViewHolder(
                @NonNull View itemView) {

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


            tvPostedBy =
                    itemView.findViewById(
                            R.id.tvPostedBy
                    );


            btnRequestTrade =
                    itemView.findViewById(
                            R.id.btnRequestTrade
                    );
        }
    }
}