package com.implantiq;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class FilterMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_menu);

        findViewById(R.id.btn_apply).setOnClickListener(v -> finish());
    }
}
