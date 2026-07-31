package com.implantiq;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

public class AdminControlPanelActivity extends BaseActivity {
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_control_panel);

        apiService = ApiService.getInstance(this);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        refreshHealthCheck();

        findViewById(R.id.btn_test_connection).setOnClickListener(v -> {
            applyGlowEffect(v);
            refreshHealthCheck();
        });
    }

    private void refreshHealthCheck() {
        apiService.get("/api/health", new ApiService.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String dbStatus = response.optString("database", "disconnected");
                    setupRow(R.id.status_api, "API Server", "ONLINE", R.color.success);
                    setupRow(R.id.status_db, "Database", dbStatus.toUpperCase(), 
                        dbStatus.equals("connected") ? R.color.success : R.color.error);
                    setupRow(R.id.status_ai, "AI Model", "ACTIVE", R.color.success);
                    
                    Toast.makeText(AdminControlPanelActivity.this, "System Health Synchronized", Toast.LENGTH_SHORT).show();
                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            public void onError(String message) {
                setupRow(R.id.status_api, "API Server", "OFFLINE", R.color.error);
                setupRow(R.id.status_db, "Database", "DISCONNECTED", R.color.error);
                Toast.makeText(AdminControlPanelActivity.this, "Check Backend Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRow(int id, String label, String value, int colorRes) {
        android.view.View v = findViewById(id);
        if (v == null) return;
        TextView tvLabel = v.findViewById(R.id.tvLabel);
        TextView tvValue = v.findViewById(R.id.tvValue);
        tvLabel.setText(label);
        tvValue.setText(value);
        tvValue.setTextColor(getResources().getColor(colorRes));
    }
}
