package com.example.swapapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapapp.R;
import com.example.swapapp.Session;
import com.example.swapapp.SessionsActivity;
import com.example.swapapp.VideoCallActivity;
import com.example.swapapp.database.DatabaseHelper;

import java.util.List;

public class SessionAdapter
        extends RecyclerView.Adapter<
        SessionAdapter.SessionViewHolder> {

    private final Context context;
    private final List<Session> sessions;

    private final DatabaseHelper dbHelper;

    private final int currentUserId;


    public SessionAdapter(
            Context context,
            List<Session> sessions) {

        this.context = context;
        this.sessions = sessions;

        dbHelper =
                new DatabaseHelper(context);

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
    public SessionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_session_card,
                                parent,
                                false
                        );

        return new SessionViewHolder(
                view
        );
    }


    @Override
    public void onBindViewHolder(
            @NonNull SessionViewHolder holder,
            int position) {

        Session session =
                sessions.get(position);


        // =====================================
        // SESSION INFORMATION
        // =====================================

        holder.tvSessionTitle.setText(
                session.getSkillTitle()
        );

        holder.tvSessionPartner.setText(
                "With: " +
                        session.getPartnerName()
        );

        holder.tvSessionStatus.setText(
                session.getStatus()
        );


        // Reset controls
        holder.btnAcceptSession
                .setVisibility(
                        View.GONE
                );

        holder.btnCancelRequest
                .setVisibility(
                        View.GONE
                );

        holder.btnJoinVideo
                .setVisibility(
                        View.GONE
                );

        holder.btnMarkCompleted
                .setVisibility(
                        View.GONE
                );

        holder.layoutRating
                .setVisibility(
                        View.GONE
                );

        holder.btnReportIssue
                .setVisibility(
                        View.GONE
                );


        String status =
                session.getStatus();


        // =====================================
        // PENDING
        // =====================================

        if ("Pending"
                .equalsIgnoreCase(status)) {

            holder.tvSessionStatus
                    .setTextColor(
                            Color.parseColor(
                                    "#BA7517"
                            )
                    );


            /*
             * SKILL OWNER / PROVIDER
             */

            if (currentUserId ==
                    session.getProviderId()) {

                holder.btnAcceptSession
                        .setVisibility(
                                View.VISIBLE
                        );

                holder.btnCancelRequest
                        .setVisibility(
                                View.VISIBLE
                        );
            }


            /*
             * REQUESTER
             */

            else if (currentUserId ==
                    session.getRequesterId()) {

                holder.btnCancelRequest
                        .setVisibility(
                                View.VISIBLE
                        );
            }
        }


        // =====================================
        // APPROVED
        // =====================================

        else if ("Approved"
                .equalsIgnoreCase(status)) {

            holder.tvSessionStatus
                    .setTextColor(
                            Color.parseColor(
                                    "#1D9E75"
                            )
                    );


            // BOTH USERS CAN JOIN VIDEO

            holder.btnJoinVideo
                    .setVisibility(
                            View.VISIBLE
                    );


            // REQUESTER ENDS/RATES

            if (currentUserId ==
                    session.getRequesterId()) {

                holder.btnMarkCompleted
                        .setVisibility(
                                View.VISIBLE
                        );
            }
        }


        // =====================================
        // COMPLETED
        // =====================================

        else if ("Completed"
                .equalsIgnoreCase(status)) {

            holder.tvSessionStatus
                    .setTextColor(
                            Color.parseColor(
                                    "#0F6E56"
                            )
                    );

            holder.btnReportIssue
                    .setVisibility(
                            View.VISIBLE
                    );
        }


        // =====================================
        // CANCELLED / DISPUTED
        // =====================================

        else {

            holder.tvSessionStatus
                    .setTextColor(
                            Color.parseColor(
                                    "#6B7280"
                            )
                    );
        }


        // =====================================
        // ACCEPT REQUEST
        // =====================================

        holder.btnAcceptSession
                .setOnClickListener(v -> {

                    if (currentUserId !=
                            session.getProviderId()) {

                        Toast.makeText(
                                context,
                                "Only the skill owner can accept this request",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    boolean success =
                            dbHelper.acceptSession(
                                    session.getTransactionId()
                            );


                    if (success) {

                        Toast.makeText(
                                context,
                                "Request accepted",
                                Toast.LENGTH_SHORT
                        ).show();

                        refreshSessions();

                    } else {

                        Toast.makeText(
                                context,
                                "Could not accept request",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });


        // =====================================
        // CANCEL REQUEST
        // =====================================

        holder.btnCancelRequest
                .setOnClickListener(v -> {

                    boolean success =
                            dbHelper.cancelSession(
                                    session.getTransactionId()
                            );


                    if (success) {

                        Toast.makeText(
                                context,
                                "Session cancelled",
                                Toast.LENGTH_SHORT
                        ).show();

                        refreshSessions();

                    } else {

                        Toast.makeText(
                                context,
                                "Unable to cancel session",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });


        // =====================================
        // LIVE VIDEO INSIDE SWAP APP
        // =====================================

        holder.btnJoinVideo
                .setOnClickListener(v -> {

                    /*
                     * Same transaction ID =
                     * same room for both users.
                     */

                    String roomName =
                            "SwapAppSession" +
                                    session.getTransactionId();


                    Intent intent =
                            new Intent(
                                    context,
                                    VideoCallActivity.class
                            );


                    intent.putExtra(
                            "ROOM_NAME",
                            roomName
                    );


                    context.startActivity(
                            intent
                    );
                });


        // =====================================
        // END SESSION
        // =====================================

        holder.btnMarkCompleted
                .setOnClickListener(v -> {

                    holder.btnMarkCompleted
                            .setVisibility(
                                    View.GONE
                            );

                    holder.layoutRating
                            .setVisibility(
                                    View.VISIBLE
                            );


                    Toast.makeText(
                            context,
                            "Please rate your session",
                            Toast.LENGTH_SHORT
                    ).show();
                });


        // =====================================
        // SUBMIT RATING
        // =====================================

        holder.btnSubmitRating
                .setOnClickListener(v -> {

                    /*
                     * Only requester / student rates.
                     */

                    if (currentUserId !=
                            session.getRequesterId()) {

                        Toast.makeText(
                                context,
                                "Only the learner can rate this session",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    int stars =
                            Math.round(
                                    holder.ratingBar
                                            .getRating()
                            );


                    if (stars < 1) {

                        Toast.makeText(
                                context,
                                "Please select a rating",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    int providerId =
                            session.getProviderId();


                    boolean success =
                            dbHelper
                                    .completeSessionTransaction(
                                            session.getTransactionId(),
                                            providerId,
                                            stars
                                    );


                    if (success) {

                        Toast.makeText(
                                context,
                                stars +
                                        " star rating submitted",
                                Toast.LENGTH_LONG
                        ).show();


                        refreshSessions();

                    } else {

                        Toast.makeText(
                                context,
                                "Could not submit rating",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });


        // =====================================
        // REPORT ISSUE
        // =====================================

        holder.btnReportIssue
                .setOnClickListener(v -> {

                    boolean success =
                            dbHelper.disputeSession(
                                    session.getTransactionId()
                            );


                    if (success) {

                        Toast.makeText(
                                context,
                                "Issue reported",
                                Toast.LENGTH_SHORT
                        ).show();


                        refreshSessions();
                    }
                });
    }


    // =========================================
    // REFRESH
    // =========================================

    private void refreshSessions() {

        if (context
                instanceof SessionsActivity) {

            ((SessionsActivity) context)
                    .refreshSessions();
        }
    }


    @Override
    public int getItemCount() {

        return sessions.size();
    }


    // =========================================
    // VIEW HOLDER
    // =========================================

    static class SessionViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvSessionTitle;
        TextView tvSessionPartner;
        TextView tvSessionStatus;

        Button btnAcceptSession;
        Button btnCancelRequest;

        Button btnJoinVideo;

        Button btnMarkCompleted;

        Button btnSubmitRating;

        Button btnReportIssue;

        LinearLayout layoutRating;

        RatingBar ratingBar;


        public SessionViewHolder(
                @NonNull View itemView) {

            super(itemView);


            tvSessionTitle =
                    itemView.findViewById(
                            R.id.tvSessionTitle
                    );


            tvSessionPartner =
                    itemView.findViewById(
                            R.id.tvSessionPartner
                    );


            tvSessionStatus =
                    itemView.findViewById(
                            R.id.tvSessionStatus
                    );


            btnAcceptSession =
                    itemView.findViewById(
                            R.id.btnAcceptSession
                    );


            btnCancelRequest =
                    itemView.findViewById(
                            R.id.btnCancelRequest
                    );


            btnJoinVideo =
                    itemView.findViewById(
                            R.id.btnJoinVideo
                    );


            btnMarkCompleted =
                    itemView.findViewById(
                            R.id.btnMarkCompleted
                    );


            layoutRating =
                    itemView.findViewById(
                            R.id.layoutRating
                    );


            ratingBar =
                    itemView.findViewById(
                            R.id.ratingBar
                    );


            btnSubmitRating =
                    itemView.findViewById(
                            R.id.btnSubmitRating
                    );


            btnReportIssue =
                    itemView.findViewById(
                            R.id.btnReportIssue
                    );
        }
    }
}