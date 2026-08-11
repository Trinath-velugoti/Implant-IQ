package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DashboardActivity extends BaseActivity {
    private TextView tvPatients, tvSuccess, tvActiveToday, tvCriticalRisks;
    private LinearLayout recentContainer;
    private SwipeRefreshLayout swipeRefresh;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        apiService = ApiService.getInstance(this);

        String doctorName = getIntent().getStringExtra("doctor_name");
        if (doctorName != null) {
            ((TextView)findViewById(R.id.tv_doctor_name)).setText(doctorName);
        }

        tvPatients = findViewById(R.id.tv_total_patients);
        tvSuccess = findViewById(R.id.tv_success_rate);
        tvActiveToday = findViewById(R.id.tv_active_predictions);
        tvCriticalRisks = findViewById(R.id.tv_critical_risks);
        recentContainer = findViewById(R.id.recent_predictions_container);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        swipeRefresh.setOnRefreshListener(this::refreshDashboard);

        setupClickListeners();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    private void refreshDashboard() {
        swipeRefresh.setRefreshing(true);
        fetchLiveStats();
        fetchRecentPredictions();
    }

    private void fetchLiveStats() {
        int userId = getSharedPreferences("ImplantIQ", MODE_PRIVATE).getInt("doctor_id", 1);
        apiService.get(NetworkConfig.API_STATS + "?user_id=" + userId, new ApiService.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    int total = response.has("totalPatients") ? response.getInt("totalPatients") : response.optInt("totalPredictions", 0);
                    tvPatients.setText(String.valueOf(total));
                    tvSuccess.setText(response.optDouble("successRate", 0) + "%");
                    tvActiveToday.setText(String.valueOf(response.optInt("activePredictions", 0)));
                    tvCriticalRisks.setText(String.valueOf(response.optInt("criticalRisks", 0)));
                    
                    // Store server today for consistent filtering
                    String serverToday = response.optString("serverToday", "");
                    if (!serverToday.isEmpty()) {
                        getSharedPreferences("ImplantIQ", MODE_PRIVATE).edit().putString("server_today", serverToday).apply();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                Snackbar.make(findViewById(android.R.id.content), "Sync Error: " + message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void fetchRecentPredictions() {
        recentContainer.removeAllViews();
        apiService.getArray(NetworkConfig.API_RECENT, new ApiService.ApiCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    if (response.length() == 0) {
                        showEmptyState();
                    } else {
                        for (int i = 0; i < response.length(); i++) {
                            addRecentItem(response.getJSONObject(i));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                showEmptyState();
            }
        });
    }

    private void addRecentItem(JSONObject obj) throws JSONException {
        View view = LayoutInflater.from(this).inflate(R.layout.item_recent_prediction, recentContainer, false);
        ((TextView) view.findViewById(R.id.tv_patient_id)).setText(obj.getString("name"));
        ((TextView) view.findViewById(R.id.tv_date)).setText(obj.getString("date"));
        
        double survival = obj.optDouble("survival_years", 0);
        double rate = obj.optDouble("success_rate", 0);
        String result = obj.optString("result", "N/A");

        TextView tvResult = view.findViewById(R.id.tv_result);
        tvResult.setText(String.format(java.util.Locale.US, "%.1fy | %.0f%%", survival, rate));

        TextView tvGrade = view.findViewById(R.id.tv_grade);
        tvGrade.setText(result);
        
        if (result.contains("A") || result.contains("Excellent") || rate > 80) {
            tvGrade.setTextColor(getResources().getColor(R.color.success));
            tvGrade.setBackgroundResource(R.drawable.badge_passed);
        } else {
            tvGrade.setTextColor(getResources().getColor(R.color.warning));
            tvGrade.setBackgroundResource(R.drawable.badge_warning);
        }

        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientDetailActivity.class);
            try {
                intent.putExtra("patient_id", obj.getString("patient_id"));
                intent.putExtra("patient_name", obj.getString("name"));
            } catch (JSONException e) { e.printStackTrace(); }
            startActivity(intent);
        });
        recentContainer.addView(view);
    }

    private void showEmptyState() {
        TextView tv = new TextView(this);
        tv.setText("No analytics records available.");
        tv.setTextColor(getResources().getColor(R.color.secondary_text));
        tv.setPadding(0, 48, 0, 48);
        tv.setGravity(android.view.Gravity.CENTER);
        recentContainer.addView(tv);
    }

    private void setupClickListeners() {
        // High-Value Clinical Navigation
        findViewById(R.id.card_patients).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, PatientListActivity.class));
        });
        findViewById(R.id.card_success).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, ReportsActivity.class));
        });
        findViewById(R.id.card_active_preds).setOnClickListener(v -> {
            applyGlowEffect(v);
            Intent intent = new Intent(this, PatientListActivity.class);
            intent.putExtra("filter", "today");
            startActivity(intent);
        });
        findViewById(R.id.card_risks).setOnClickListener(v -> {
            applyGlowEffect(v);
            Intent intent = new Intent(this, PatientListActivity.class);
            intent.putExtra("filter", "risk");
            startActivity(intent);
        });

        findViewById(R.id.btn_start_quick).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, PredictActivity.class));
        });
        findViewById(R.id.fab_add).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, AddPatientActivity.class));
        });
        findViewById(R.id.iv_notifications).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, NotificationCenterActivity.class));
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) return true;
            if (id == R.id.nav_patients) {
                startActivity(new Intent(this, PatientListActivity.class));
                return true;
            }
            if (id == R.id.nav_predict) {
                startActivity(new Intent(this, PredictActivity.class));
                return true;
            }
            if (id == R.id.nav_reports) {
                startActivity(new Intent(this, ReportsActivity.class));
                return true;
            }
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}
