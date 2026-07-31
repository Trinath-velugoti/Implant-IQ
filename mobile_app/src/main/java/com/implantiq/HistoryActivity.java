package com.implantiq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (findViewById(R.id.btn_back) != null) {
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        }

        populateHistory();
    }

    private void populateHistory() {
        LinearLayout container = findViewById(R.id.history_container);
        if (container == null) return;

        String[][] items = {
            {"🔮", "New Prediction", "Patient PID-1024 analysis completed", "10:30 AM"},
            {"👤", "Patient Added", "Vikram Singh added to system", "09:15 AM"},
            {"🔑", "System Login", "Doctor session started", "08:00 AM"}
        };

        for (String[] item : items) {
            View view = LayoutInflater.from(this).inflate(R.layout.layout_summary_row, container, false);
            ((TextView) view.findViewById(R.id.tvLabel)).setText(item[0] + " " + item[1]);
            ((TextView) view.findViewById(R.id.tvValue)).setText(item[3]);
            
            TextView desc = new TextView(this);
            desc.setText(item[2]);
            desc.setTextColor(getResources().getColor(R.color.secondary_text));
            desc.setTextSize(12);
            desc.setPadding(0, 0, 0, 24);
            
            container.addView(view);
            container.addView(desc);
        }
    }
}