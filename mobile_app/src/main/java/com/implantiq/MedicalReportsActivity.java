package com.implantiq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MedicalReportsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_reports);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        populateReports();
    }

    private void populateReports() {
        LinearLayout container = findViewById(R.id.reports_list_container);
        String[][] data = {
            {"Annual Survival Summary", "12 May 2026", "2.4 MB"},
            {"Monthly Clinical Audit", "01 May 2026", "1.1 MB"},
            {"Patient Demographics", "15 Apr 2026", "850 KB"}
        };

        for (String[] report : data) {
            View v = LayoutInflater.from(this).inflate(R.layout.layout_summary_row, container, false);
            ((TextView) v.findViewById(R.id.tvLabel)).setText(report[0]);
            ((TextView) v.findViewById(R.id.tvValue)).setText(report[2]);
            
            v.setOnClickListener(view -> {
                Toast.makeText(this, "Downloading " + report[0] + "...", Toast.LENGTH_LONG).show();
            });
            
            container.addView(v);
        }
    }
}
