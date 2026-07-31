package com.implantiq;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SecurityVerificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_verification);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupItem(R.id.setting_change_pwd, "Change Password", "Update your clinical access key");
        setupItem(R.id.setting_history, "Login History", "Review recent clinical sessions");

        findViewById(R.id.setting_change_pwd).setOnClickListener(v -> {
            Toast.makeText(this, "Redirection to reset workflow...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupItem(int id, String title, String subtitle) {
        View v = findViewById(id);
        ((TextView) v.findViewById(R.id.tv_title)).setText(title);
        ((TextView) v.findViewById(R.id.tv_subtitle)).setText(subtitle);
    }
}
