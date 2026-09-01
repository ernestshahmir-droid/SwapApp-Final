package com.example.swapapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapapp.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WalletActivity extends AppCompatActivity {

    private TextView textWalletBalance, textTotalEarned, textTotalSpent;
    private LinearLayout graphContainer;
    private RecyclerView rvTransactions;

    private DatabaseHelper dbHelper;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        textWalletBalance = findViewById(R.id.textWalletBalance);
        textTotalEarned = findViewById(R.id.textTotalEarned);
        textTotalSpent = findViewById(R.id.textTotalSpent);
        graphContainer = findViewById(R.id.graphContainer);
        rvTransactions = findViewById(R.id.rvTransactions);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("SwapAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("CURRENT_USER_ID", -1);

        if (currentUserId == -1) {
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWalletData();
    }

    private void loadWalletData() {
        Cursor userCursor = dbHelper.getUserDetails(currentUserId);
        if (userCursor.moveToFirst()) {
            textWalletBalance.setText(String.valueOf(userCursor.getInt(2)));
        }
        userCursor.close();

        List<TxModel> txList = new ArrayList<>();
        int sumEarned = 0;
        int sumSpent = 0;

        Cursor c = dbHelper.getWalletTransactions(currentUserId);
        if (c.moveToFirst()) {
            do {
                int providerId = c.getInt(2);
                String skillTitle = c.getString(3);
                String partnerName = c.getString(4);
                int stars = c.getInt(5);

                // Calculate the star-based reward for the provider
                int starCredits = (stars == 1) ? 2 : (stars == 2) ? 3 : (stars == 3) ? 5 : (stars == 4) ? 7 : 10;

                boolean isEarned = (currentUserId == providerId);
                int transactionAmount = 0;

                // FIXED: If earned, they get star credits. If spent, they paid a flat 2 credits.
                if (isEarned) {
                    sumEarned += starCredits;
                    transactionAmount = starCredits;
                } else {
                    sumSpent += 2; // Flat request fee
                    transactionAmount = 2;
                }

                txList.add(new TxModel(isEarned, partnerName, skillTitle, transactionAmount));
            } while (c.moveToNext());
        }
        c.close();

        textTotalEarned.setText(String.valueOf(sumEarned));
        textTotalSpent.setText(String.valueOf(sumSpent));

        drawNativeGraph(txList);

        Collections.reverse(txList);
        rvTransactions.setAdapter(new TxAdapter(txList));
    }

    private void drawNativeGraph(List<TxModel> transactions) {
        graphContainer.removeAllViews();

        if (transactions.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Complete a session to see your graph.");
            emptyText.setTextColor(Color.parseColor("#8A92A6"));
            graphContainer.addView(emptyText);
            return;
        }

        int maxCredits = 10;

        for (TxModel t : transactions) {
            LinearLayout barLayout = new LinearLayout(this);
            barLayout.setOrientation(LinearLayout.VERTICAL);
            barLayout.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(60, ViewGroup.LayoutParams.MATCH_PARENT);
            barParams.setMargins(16, 0, 16, 0);
            barLayout.setLayoutParams(barParams);

            View space = new View(this);
            LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, maxCredits - t.credits);
            space.setLayoutParams(spaceParams);
            barLayout.addView(space);

            View bar = new View(this);
            LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, t.credits);
            bar.setLayoutParams(colorParams);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadii(new float[]{8, 8, 8, 8, 0, 0, 0, 0});
            drawable.setColor(Color.parseColor(t.isEarned ? "#1D9E75" : "#DC3545"));
            bar.setBackground(drawable);

            barLayout.addView(bar);
            graphContainer.addView(barLayout);
        }
    }

    class TxModel {
        boolean isEarned;
        String partnerName, skillTitle;
        int credits;
        TxModel(boolean isEarned, String partnerName, String skillTitle, int credits) {
            this.isEarned = isEarned;
            this.partnerName = partnerName;
            this.skillTitle = skillTitle;
            this.credits = credits;
        }
    }

    class TxAdapter extends RecyclerView.Adapter<TxAdapter.ViewHolder> {
        List<TxModel> items;
        TxAdapter(List<TxModel> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TxModel t = items.get(position);

            holder.title.setText(t.isEarned ? "Taught " + t.skillTitle : "Learned " + t.skillTitle);
            holder.partner.setText(t.isEarned ? "To: " + t.partnerName : "From: " + t.partnerName);

            if (t.isEarned) {
                holder.iconBg.setCardBackgroundColor(Color.parseColor("#E1F5EE"));
                holder.iconTxt.setText("+");
                holder.iconTxt.setTextColor(Color.parseColor("#0F6E56"));
                holder.amount.setText("+" + t.credits + " TC");
                holder.amount.setTextColor(Color.parseColor("#1D9E75"));
            } else {
                holder.iconBg.setCardBackgroundColor(Color.parseColor("#FAECE7"));
                holder.iconTxt.setText("-");
                holder.iconTxt.setTextColor(Color.parseColor("#993C1D"));
                holder.amount.setText("-" + t.credits + " TC");
                holder.amount.setTextColor(Color.parseColor("#DC3545"));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, partner, amount, iconTxt;
            CardView iconBg;
            ViewHolder(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.textTxTitle);
                partner = v.findViewById(R.id.textTxPartner);
                amount = v.findViewById(R.id.textTxAmount);
                iconTxt = v.findViewById(R.id.textTxIcon);
                iconBg = v.findViewById(R.id.cardIconBg);
            }
        }
    }
}