package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import org.json.JSONException;
import org.json.JSONObject;

public class OtpVerificationActivity extends BaseActivity {

    private ApiService apiService;
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        apiService = ApiService.getInstance(this);
        email = getIntent().getStringExtra("email");

        TextView tvSubtitle = findViewById(R.id.tv_subtitle);
        tvSubtitle.setText("Enter the 6-digit code sent to " + email);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);

        setupOtpInputs();

        MaterialButton btnVerify = findViewById(R.id.btn_verify);
        btnVerify.setOnClickListener(v -> {
            applyGlowEffect(v);
            verifyOtp();
        });

        findViewById(R.id.tv_resend).setOnClickListener(v -> {
            applyGlowEffect(v);
            resendOtp();
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void resendOtp() {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);

            apiService.post("/api/auth/resend-otp", body, new ApiService.ApiCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {
                    String demoOtp = response.optString("demo_otp", "123456");
                    Toast.makeText(OtpVerificationActivity.this, "New Code Sent! Demo: " + demoOtp, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(OtpVerificationActivity.this, "Resend failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void setupOtpInputs() {
        EditText[] inputs = {otp1, otp2, otp3, otp4, otp5, otp6};
        for (int i = 0; i < inputs.length; i++) {
            final int index = i;
            inputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < inputs.length - 1) {
                        inputs[index + 1].requestFocus();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void verifyOtp() {
        String code = otp1.getText().toString() + otp2.getText().toString() + 
                     otp3.getText().toString() + otp4.getText().toString() + 
                     otp5.getText().toString() + otp6.getText().toString();

        if (code.length() < 6) {
            Toast.makeText(this, "Complete the code", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("otp", code);

            apiService.post("/api/auth/verify-otp", body, new ApiService.ApiCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {
                    Toast.makeText(OtpVerificationActivity.this, "Clinical Access Activated", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) { e.printStackTrace(); }
    }
}
