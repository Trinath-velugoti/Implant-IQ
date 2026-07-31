package com.implantiq;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class RiskAssessmentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risk_assessment);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupRiskBar(R.id.factor1, "Bone Quality", "10%", 15);
        setupRiskBar(R.id.factor2, "Patient Age", "5%", 10);
    }

    private void setupRiskBar(int id, String label, String percent, int progress) {
        View v = findViewById(id);
        ((TextView) v.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) v.findViewById(R.id.tvPercent)).setText(percent);
        ((LinearProgressIndicator) v.findViewById(R.id.indicator)).setProgress(progress);
    }
}
