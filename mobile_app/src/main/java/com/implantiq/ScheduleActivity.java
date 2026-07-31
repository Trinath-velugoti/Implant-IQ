package com.implantiq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ScheduleActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        if (findViewById(R.id.btn_back) != null) {
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        }

        populateSchedule();
    }

    private void populateSchedule() {
        LinearLayout container = findViewById(R.id.schedule_tasks_container);
        if (container == null) return;

        String[][] data = {
            {"Review X-Rays", "09:00 AM", "Surgery Prep", "Confirmed"},
            {"Patient Aditi Rao", "10:30 AM", "Surgery", "Confirmed"},
            {"Consultation", "12:00 PM", "New Patient", "Pending"}
        };

        for (String[] task : data) {
            View v = LayoutInflater.from(this).inflate(R.layout.layout_summary_row, container, false);
            ((TextView) v.findViewById(R.id.tvLabel)).setText(task[0]);
            ((TextView) v.findViewById(R.id.tvValue)).setText(task[1]);
            container.addView(v);
        }
    }
}
