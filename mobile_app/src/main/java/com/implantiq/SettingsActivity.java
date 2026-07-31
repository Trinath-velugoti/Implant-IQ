package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends BaseActivity {
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        apiService = ApiService.getInstance(this);

        if (findViewById(R.id.btn_back) != null) {
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        }

        setupBottomNav();

        findViewById(R.id.setting_clinic_name).setOnClickListener(v -> {
            applyGlowEffect(v);
            showClinicEditDialog();
        });
        findViewById(R.id.setting_api_endpoint).setOnClickListener(v -> {
            applyGlowEffect(v);
            showApiEndpointDialog();
        });
        findViewById(R.id.btn_view_profile).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, ProfileActivity.class));
        });
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            applyGlowEffect(v);
            showLogoutDialog();
        });

        setupSettingItem(R.id.setting_admin, "Admin Control Panel", "System & API monitoring");
        findViewById(R.id.setting_admin).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, AdminControlPanelActivity.class));
        });

        setupSettingItem(R.id.setting_chat, "Live Doctor Chat", "Support & clinical assistance");
        findViewById(R.id.setting_chat).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, DoctorChatActivity.class));
        });
        
        setupSettingItem(R.id.setting_change_password, "Change Password", "Last changed 3 months ago");
        findViewById(R.id.setting_change_password).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, SecurityVerificationActivity.class));
        });
        
        setupSettingItem(R.id.setting_faq, "Clinical FAQ", "Common questions & answers");
        findViewById(R.id.setting_faq).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, FaqActivity.class));
        });

        setupSettingItem(R.id.setting_privacy, "Privacy Policy", "Data protection details");
        findViewById(R.id.setting_privacy).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
        });

        setupSettingItem(R.id.setting_terms, "Terms & Conditions", "App usage license");
        findViewById(R.id.setting_terms).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, TermsActivity.class));
        });

        setupSettingItem(R.id.setting_about, "About", "App version & developer info");
        findViewById(R.id.setting_about).setOnClickListener(v -> {
            applyGlowEffect(v);
            startActivity(new Intent(this, AboutActivity.class));
        });
    }

    private void showClinicEditDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter Clinic Name");
        new AlertDialog.Builder(this)
                .setTitle("Update Clinic Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString();
                    if (!name.isEmpty()) {
                        Toast.makeText(this, "Clinic Name Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showApiEndpointDialog() {
        EditText input = new EditText(this);
        input.setHint("http://10.0.2.2:5000");
        new AlertDialog.Builder(this)
                .setTitle("Update API Endpoint")
                .setView(input)
                .setPositiveButton("Test & Save", (dialog, which) -> {
                    String url = input.getText().toString();
                    testConnection(url);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void testConnection(String url) {
        // Temporarily change base URL
        String oldUrl = "http://10.0.2.2:5000"; // Mocking recovery
        apiService.setBaseUrl(url);
        apiService.get("/api/health", new ApiService.ApiCallback<org.json.JSONObject>() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                Toast.makeText(SettingsActivity.this, "✓ Connected successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SettingsActivity.this, "✗ Connection failed", Toast.LENGTH_SHORT).show();
                apiService.setBaseUrl(oldUrl);
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to end your session?")
                .setPositiveButton("Yes, Logout", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    finishAffinity();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupSettingItem(int viewId, String title, String subtitle) {
        View row = findViewById(viewId);
        if (row != null) {
            ((TextView) row.findViewById(R.id.tv_title)).setText(title);
            ((TextView) row.findViewById(R.id.tv_subtitle)).setText(subtitle);
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) { startActivity(new Intent(this, DashboardActivity.class)); finish(); return true; }
            if (id == R.id.nav_patients) { startActivity(new Intent(this, PatientListActivity.class)); finish(); return true; }
            if (id == R.id.nav_predict) { startActivity(new Intent(this, PredictActivity.class)); finish(); return true; }
            if (id == R.id.nav_reports) { startActivity(new Intent(this, ReportsActivity.class)); finish(); return true; }
            if (id == R.id.nav_settings) return true;
            return false;
        });
    }
}
