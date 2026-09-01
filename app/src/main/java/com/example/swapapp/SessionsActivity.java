package com.example.swapapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapapp.adapters.SessionAdapter;
import com.example.swapapp.database.DatabaseHelper;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class SessionsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSessions;
    private TabLayout tabLayoutSessions;
    private SessionAdapter adapter;

    private List<Session> allSessionsList;
    private List<Session> displayList;

    private DatabaseHelper dbHelper;

    private int currentUserId;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        recyclerViewSessions = findViewById(R.id.recyclerViewSessions);
        tabLayoutSessions = findViewById(R.id.tabLayoutSessions);

        recyclerViewSessions.setLayoutManager(
                new LinearLayoutManager(this)
        );

        allSessionsList = new ArrayList<>();
        displayList = new ArrayList<>();

        dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs =
                getSharedPreferences(
                        "SwapAppPrefs",
                        Context.MODE_PRIVATE
                );

        currentUserId =
                prefs.getInt(
                        "CURRENT_USER_ID",
                        -1
                );

        // No logged-in user
        if (currentUserId == -1) {

            Intent intent = new Intent(
                    SessionsActivity.this,
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

        // Only add tabs if XML has not already added them
        if (tabLayoutSessions.getTabCount() == 0) {

            tabLayoutSessions.addTab(
                    tabLayoutSessions
                            .newTab()
                            .setText("Requests")
            );

            tabLayoutSessions.addTab(
                    tabLayoutSessions
                            .newTab()
                            .setText("Approved")
            );

            tabLayoutSessions.addTab(
                    tabLayoutSessions
                            .newTab()
                            .setText("Completed")
            );

            tabLayoutSessions.addTab(
                    tabLayoutSessions
                            .newTab()
                            .setText("All Sessions")
            );
        }

        adapter = new SessionAdapter(
                this,
                displayList
        );

        recyclerViewSessions.setAdapter(adapter);

        tabLayoutSessions.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {

                        currentTab = tab.getPosition();
                        refreshSessions();
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                        currentTab = tab.getPosition();
                        refreshSessions();
                    }
                }
        );

        refreshSessions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter != null) {
            refreshSessions();
        }
    }

    public void refreshSessions() {

        loadSessionsFromDatabase();
        filterSessions(currentTab);
    }

    private void filterSessions(int tabIndex) {

        displayList.clear();

        for (Session session : allSessionsList) {

            String status = session.getStatus();

            // ====================================
            // REQUESTS
            // ONLY PROVIDER/OWNER CAN SEE THESE
            // ====================================
            if (tabIndex == 0) {

                if ("Pending".equalsIgnoreCase(status)
                        && currentUserId == session.getProviderId()) {

                    displayList.add(session);
                }
            }

            // ====================================
            // APPROVED
            // BOTH REQUESTER + PROVIDER
            // ====================================
            else if (tabIndex == 1) {

                if ("Approved".equalsIgnoreCase(status)) {

                    displayList.add(session);
                }
            }

            // ====================================
            // COMPLETED
            // ====================================
            else if (tabIndex == 2) {

                if ("Completed".equalsIgnoreCase(status)
                        || "Cancelled".equalsIgnoreCase(status)
                        || "Disputed".equalsIgnoreCase(status)) {

                    displayList.add(session);
                }
            }

            // ====================================
            // ALL SESSIONS
            // ====================================
            else if (tabIndex == 3) {

                displayList.add(session);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadSessionsFromDatabase() {

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        String query =

                "SELECT " +

                        "t.transaction_id, " +
                        "t.requester_id, " +
                        "t.provider_id, " +
                        "s.title, " +

                        "(SELECT name FROM USERS " +
                        "WHERE user_id = " +
                        "CASE " +
                        "WHEN t.requester_id = ? " +
                        "THEN t.provider_id " +
                        "ELSE t.requester_id " +
                        "END) AS partner_name, " +

                        "t.status " +

                        "FROM TRANSACTIONS t " +

                        "JOIN SKILLS s " +
                        "ON t.skill_id = s.skill_id " +

                        "WHERE t.requester_id = ? " +
                        "OR t.provider_id = ? " +

                        "ORDER BY t.transaction_id DESC";

        Cursor cursor =
                db.rawQuery(
                        query,
                        new String[]{
                                String.valueOf(currentUserId),
                                String.valueOf(currentUserId),
                                String.valueOf(currentUserId)
                        }
                );

        allSessionsList.clear();

        int incomingPendingCount = 0;

        if (cursor.moveToFirst()) {

            do {

                int transactionId =
                        cursor.getInt(0);

                int requesterId =
                        cursor.getInt(1);

                int providerId =
                        cursor.getInt(2);

                String skillTitle =
                        cursor.getString(3);

                String partnerName =
                        cursor.getString(4);

                String status =
                        cursor.getString(5);

                if (partnerName == null ||
                        partnerName.trim().isEmpty()) {

                    partnerName = "User";
                }

                Session session =
                        new Session(
                                transactionId,
                                requesterId,
                                providerId,
                                skillTitle,
                                partnerName,
                                status
                        );

                allSessionsList.add(session);

                // Count only requests YOU received
                if ("Pending".equalsIgnoreCase(status)
                        && currentUserId == providerId) {

                    incomingPendingCount++;
                }

            } while (cursor.moveToNext());
        }

        cursor.close();

        if (currentTab == 0 &&
                incomingPendingCount > 0) {

            Toast.makeText(
                    this,
                    "🔔 You have " +
                            incomingPendingCount +
                            " request(s) to accept",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}