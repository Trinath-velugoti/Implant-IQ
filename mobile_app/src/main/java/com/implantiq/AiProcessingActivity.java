package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class AiProcessingActivity extends AppCompatActivity {

    private TextView tvStatus, tvPercent;
    private CircularProgressIndicator progressBar;
    private View stepData, stepStats, stepOutliers, stepAi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_processing);

        if (findViewById(R.id.btn_back) != null) {
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        }

        tvStatus = findViewById(R.id.tv_status_main);
        tvPercent = findViewById(R.id.tv_progress_percent);
        progressBar = findViewById(R.id.circular_progress);
        
        stepData = findViewById(R.id.step_data);
        stepStats = findViewById(R.id.step_stats);
        stepOutliers = findViewById(R.id.step_outliers);
        stepAi = findViewById(R.id.step_ai);

        startProcessingSimulation();
    }

    private void startProcessingSimulation() {
        Handler handler = new Handler();
        
        // Step 1: Data Extraction
        handler.postDelayed(() -> {
            updateStep(stepData, "Data Extraction", true);
            tvPercent.setText("25%");
            progressBar.setProgress(25);
        }, 1000);

        // Step 2: Stats
        handler.postDelayed(() -> {
            updateStep(stepStats, "Statistical Calculation", true);
            tvPercent.setText("50%");
            progressBar.setProgress(50);
            tvStatus.setText("Calculating");
        }, 2000);

        // Step 3: Outliers
        handler.postDelayed(() -> {
            updateStep(stepOutliers, "Outlier Detection", true);
            tvPercent.setText("75%");
            progressBar.setProgress(75);
            tvStatus.setText("Analyzing");
        }, 3000);

        // Step 4: AI Insights
        handler.postDelayed(() -> {
            updateStep(stepAi, "Generating AI Insights", true);
            tvPercent.setText("100%");
            progressBar.setProgress(100);
            tvStatus.setText("Complete");
        }, 4000);

        handler.postDelayed(() -> {
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            finish();
        }, 5000);
    }

    private void updateStep(View view, String text, boolean completed) {
        ((TextView) view.findViewById(R.id.tv_step_name)).setText(text);
        if (completed) {
            ((ImageView) view.findViewById(R.id.iv_status)).setImageResource(android.R.drawable.checkbox_on_background);
        }
    }
}
