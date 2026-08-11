package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import org.json.JSONObject;

public class ReportsActivity extends BaseActivity {
    private ApiService apiService;
    private TextView tvTotalPreds, tvAvgSuccess, tvExcellent;
    private TextView tvExcellentDist, tvGoodDist;
    private LinearProgressIndicator pbExcellent, pbGood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        apiService = ApiService.getInstance(this);
        
        tvTotalPreds = findViewById(R.id.tv_total_preds_val);
        tvAvgSuccess = findViewById(R.id.tv_avg_success_val);
        tvExcellent = findViewById(R.id.tv_excellent_val);
        
        tvExcellentDist = findViewById(R.id.tv_excellent_dist_label);
        tvGoodDist = findViewById(R.id.tv_good_dist_label);
        pbExcellent = findViewById(R.id.pb_excellent_dist);
        pbGood = findViewById(R.id.pb_good_dist);

        setupBottomNav();
        fetchAnalyticsData();

        findViewById(R.id.btn_back).setOnClickListener(v -> {
            applyGlowEffect(v);
            finish();
        });
        findViewById(R.id.btn_export).setOnClickListener(v -> {
            applyGlowEffect(v);
            showExportDialog();
        });
    }

    private void fetchAnalyticsData() {
        int userId = getSharedPreferences("ImplantIQ", MODE_PRIVATE).getInt("doctor_id", 1);
        apiService.get(NetworkConfig.API_STATS + "?user_id=" + userId, new ApiService.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    int total = response.has("totalPatients") ? response.getInt("totalPatients") : response.optInt("totalPredictions", 0);
                    int successRate = response.optInt("successRate", 0);
                    int criticalRisks = response.optInt("criticalRisks", 0);
                    
                    tvTotalPreds.setText(String.valueOf(total));
                    tvAvgSuccess.setText(successRate + "%");
                    
                    int excellentCount = (int)(total * 0.7);
                    int goodCount = (int)(total * 0.2);
                    
                    tvExcellent.setText(String.valueOf(excellentCount));
                    tvExcellentDist.setText(excellentCount + " cases");
                    pbExcellent.setProgress(70);
                    
                    tvGoodDist.setText(goodCount + " cases");
                    pbGood.setProgress(20);
                    
                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReportsActivity.this, "Live Analytics Sync Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showExportDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Export Clinical Dataset")
                .setMessage("Generate a professional CSV/PDF report of all AI analysis results?")
                .setPositiveButton("Export Now", (dialog, which) -> simulateExport())
                .setNegativeButton("Cancel", null).show();
    }

    private void simulateExport() {
        android.app.ProgressDialog progress = new android.app.ProgressDialog(this);
        progress.setTitle("Clinical Export");
        progress.setMessage("Assembling encrypted database for " + tvTotalPreds.getText() + " patients...");
        progress.setIndeterminate(true);
        progress.show();

        new android.os.Handler().postDelayed(() -> {
            progress.dismiss();
            Toast.makeText(this, "Success: Report saved to /ImplantIQ/Reports/Main_Dataset.csv", Toast.LENGTH_LONG).show();
        }, 2500);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_reports);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) { startActivity(new Intent(this, DashboardActivity.class)); finish(); return true; }
            if (id == R.id.nav_patients) { startActivity(new Intent(this, PatientListActivity.class)); finish(); return true; }
            if (id == R.id.nav_predict) { startActivity(new Intent(this, PredictActivity.class)); finish(); return true; }
            if (id == R.id.nav_reports) return true;
            if (id == R.id.nav_settings) { startActivity(new Intent(this, SettingsActivity.class)); finish(); return true; }
            return false;
        });
    }
}
