package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends BaseActivity {

    private int step = 1;
    private TextView tvTitle, tvDesc;
    private MaterialButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        tvTitle = findViewById(R.id.tv_onboarding_title);
        tvDesc = findViewById(R.id.tv_onboarding_desc);
        btnNext = findViewById(R.id.btn_next);

        findViewById(R.id.btn_back).setOnClickListener(v -> {
            applyGlowEffect(v);
            if (step > 1) {
                step--;
                updateStepUI();
            } else {
                finish();
            }
        });

        findViewById(R.id.btn_skip).setOnClickListener(v -> {
            applyGlowEffect(v);
            finishOnboarding();
        });

        btnNext.setOnClickListener(v -> {
            applyGlowEffect(v);
            if (step < 3) {
                step++;
                updateStepUI();
            } else {
                finishOnboarding();
            }
        });
    }

    private void updateStepUI() {
        if (step == 1) {
            tvTitle.setText("AI-Powered Predictions");
            tvDesc.setText("High-accuracy machine learning models to predict dental implant survival years and success rates.");
            btnNext.setText("NEXT");
        } else if (step == 2) {
            tvTitle.setText("Secure & Reliable");
            tvDesc.setText("Clinical data is encrypted and protected following the highest security standards.");
            btnNext.setText("NEXT");
        } else if (step == 3) {
            tvTitle.setText("Detailed Analytics");
            tvDesc.setText("Visualize clinic performance and patient outcomes with interactive charts.");
            btnNext.setText("GET STARTED");
        }
    }

    private void finishOnboarding() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
