package com.implantiq;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class BoneAnalysisActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bone_analysis);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        String density = getIntent().getStringExtra("bone_density");
        String height = getIntent().getStringExtra("bone_height");
        String width = getIntent().getStringExtra("bone_width");

        ((TextView) findViewById(R.id.tv_density_val)).setText(density + " g/cm³");
        
        setupRow(R.id.row_height, "Bone Height", height + " mm");
        setupRow(R.id.row_width, "Bone Width", width + " mm");
    }

    private void setupRow(int id, String label, String val) {
        View v = findViewById(id);
        ((TextView) v.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) v.findViewById(R.id.tvValue)).setText(val);
    }
}
