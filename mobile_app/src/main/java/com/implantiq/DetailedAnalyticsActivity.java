package com.implantiq;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class DetailedAnalyticsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_analytics);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupChartBar(R.id.age1, "20-35 Years", "45%", 45);
        setupChartBar(R.id.age2, "36-50 Years", "38%", 38);
        setupChartBar(R.id.age3, "51+ Years", "17%", 17);
    }

    private void setupChartBar(int id, String label, String percent, int progress) {
        View v = findViewById(id);
        ((TextView) v.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) v.findViewById(R.id.tvPercent)).setText(percent);
        ((LinearProgressIndicator) v.findViewById(R.id.indicator)).setProgress(progress);
    }
}
