package com.implantiq;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class ClinicStatisticsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_statistics);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupStat(R.id.stat1, "Patient Retention", "87%", 87);
        setupStat(R.id.stat2, "Treatment Success", "94%", 94);
        setupStat(R.id.stat3, "Appointment Rate", "92%", 92);
    }

    private void setupStat(int id, String label, String percent, int progress) {
        View v = findViewById(id);
        ((TextView) v.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) v.findViewById(R.id.tvPercent)).setText(percent);
        ((LinearProgressIndicator) v.findViewById(R.id.indicator)).setProgress(progress);
    }
}
