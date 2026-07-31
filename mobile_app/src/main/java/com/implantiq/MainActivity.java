package com.implantiq;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton btnStart = findViewById(R.id.btnGetStarted);
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(this, PredictActivity.class);
            startActivity(intent);
        });
    }
}