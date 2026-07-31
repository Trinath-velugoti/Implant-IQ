package com.implantiq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationCenterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        populateNotifications();
    }

    private void populateNotifications() {
        LinearLayout container = findViewById(R.id.notification_container);
        
        String[][] items = {
            {"Success", "AI Analysis Complete", "Prediction for Patient PID-1024 is ready for review.", "10 mins ago"},
            {"Info", "App Update", "Version 2.0 with improved bone analysis is now live.", "2 hours ago"},
            {"Warning", "Sync Required", "Database connection lost. Please check internet.", "1 day ago"}
        };

        for (String[] item : items) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_recent_prediction, container, false);
            ((TextView) view.findViewById(R.id.tv_patient_id)).setText(item[1]);
            ((TextView) view.findViewById(R.id.tv_date)).setText(item[3]);
            ((TextView) view.findViewById(R.id.tv_result)).setText(item[2]);
            TextView tvGrade = view.findViewById(R.id.tv_grade);
            tvGrade.setText(item[0]);
            
            if (item[0].equals("Success")) tvGrade.setTextColor(getResources().getColor(R.color.success));
            else if (item[0].equals("Warning")) tvGrade.setTextColor(getResources().getColor(R.color.error));
            else tvGrade.setTextColor(getResources().getColor(R.color.accent_cyan));

            container.addView(view);
        }
    }
}
