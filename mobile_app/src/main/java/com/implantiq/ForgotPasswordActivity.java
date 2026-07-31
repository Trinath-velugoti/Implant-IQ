package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        TextInputEditText etEmail = findViewById(R.id.et_reset_email);
        MaterialButton btnReset = findViewById(R.id.btn_reset_password);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                Toast.makeText(this, "Security Verification Link Sent to " + email, Toast.LENGTH_LONG).show();
                // Navigate to OTP verification for a real-world workflow
                startActivity(new Intent(this, OtpVerificationActivity.class));
            } else {
                Toast.makeText(this, "Please enter your clinical email", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
