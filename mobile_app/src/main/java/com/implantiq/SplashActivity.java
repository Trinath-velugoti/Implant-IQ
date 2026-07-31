package com.implantiq;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        makeFullScreen();

        if (findViewById(R.id.btn_get_started) != null) {
            findViewById(R.id.btn_get_started).setOnClickListener(v -> {
                applyGlowEffect(v);
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                finish();
            });
        }
    }
}