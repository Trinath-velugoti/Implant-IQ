package com.implantiq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PredictionHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_history);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        populateHistory();
    }

    private void populateHistory() {
        LinearLayout container = findViewById(R.id.prediction_list_container);
        String[][] data = {
            {"Aditi Rao", "12 May 2026", "11.4y", "90%", "Excellent"},
            {"Rajesh Kumar", "11 May 2026", "8.2y", "78%", "Good"},
            {"Sneha Kapoor", "10 May 2026", "4.5y", "52%", "Poor"}
        };

        for (String[] item : data) {
            View v = LayoutInflater.from(this).inflate(R.layout.item_recent_prediction, container, false);
            ((TextView) v.findViewById(R.id.tv_patient_id)).setText(item[0]);
            ((TextView) v.findViewById(R.id.tv_date)).setText(item[1]);
            ((TextView) v.findViewById(R.id.tv_result)).setText(item[2] + " | " + item[3]);
            TextView tvGrade = v.findViewById(R.id.tv_grade);
            tvGrade.setText(item[4]);
            
            if (item[4].equals("Excellent")) tvGrade.setTextColor(getResources().getColor(R.color.success));
            else if (item[4].equals("Good")) tvGrade.setTextColor(getResources().getColor(R.color.accent_cyan));
            else tvGrade.setTextColor(getResources().getColor(R.color.error));

            container.addView(v);
        }
    }
}
