package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.tabs.TabLayout;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity {

    private ApiService apiService;
    private TextInputEditText etEmail, etPassword;
    private boolean isSignupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = ApiService.getInstance(this);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        MaterialButton btnAction = findViewById(R.id.btn_sign_in);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        TabLayout tabs = findViewById(R.id.login_tabs);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isSignupMode = (tab.getPosition() == 1);
                btnAction.setText(isSignupMode ? "REGISTER CLINIC" : "AUTHENTICATE & ENTER");
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnAction.setOnClickListener(v -> {
            applyGlowEffect(v);
            if (isSignupMode) performSignup();
            else performLogin();
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);

            apiService.post("/api/auth/login", body, new ApiService.ApiCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        String token = response.getString("token");
                        String name = response.getJSONObject("user").getString("username");
                        
                        apiService.setAuthToken(token);
                        
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.putExtra("doctor_name", name);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) { e.printStackTrace(); }
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void performSignup() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.length() < 8) {
            Toast.makeText(this, "Valid email and 8+ char password required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("username", "Dr. " + email.split("@")[0]); // Temporary Name
            body.put("email", email);
            body.put("password", password);

            apiService.post("/api/auth/signup", body, new ApiService.ApiCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {
                    // Start OTP Activity
                    Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) { e.printStackTrace(); }
    }
}
