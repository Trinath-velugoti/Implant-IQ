package com.implantiq;

import android.os.Bundle;

public class PatientTimelineActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_timeline);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
