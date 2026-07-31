package com.implantiq;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ImplantComparisonActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_implant_comparison);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
