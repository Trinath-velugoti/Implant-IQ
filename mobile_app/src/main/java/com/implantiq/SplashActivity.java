package com.implantiq;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        makeFullScreen();

        ApiService apiService = ApiService.getInstance(this);

        if (apiService.getAuthToken() != null) {
            // Persistent Session - Go to Dashboard
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            // No session - Standard onboarding
            if (findViewById(R.id.btn_get_started) != null) {
                findViewById(R.id.btn_get_started).setOnClickListener(v -> {
                    applyGlowEffect(v);
                    startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                    finish();
                });
            }
        }
    }
}