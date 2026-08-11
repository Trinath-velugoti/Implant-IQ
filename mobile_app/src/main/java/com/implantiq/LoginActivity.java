package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.tabs.TabLayout;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity {

    private ApiService apiService;
    private TextInputEditText etEmail, etPassword;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = ApiService.getInstance(this);
        
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        MaterialButton btnSignIn = findViewById(R.id.btn_sign_in);
        MaterialButton btnGoogle = findViewById(R.id.btn_google_sign_in);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        TabLayout tabs = findViewById(R.id.login_tabs);

        // Switch between Login and Signup via tabs
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) { // Signup
                    startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                    finish();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Real Google Integration
        btnGoogle.setOnClickListener(v -> {
            applyGlowEffect(v);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        btnSignIn.setOnClickListener(v -> {
            applyGlowEffect(v);
            performLogin();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            
            // Signed in successfully, show authenticated UI.
            String name = account.getDisplayName();
            String email = account.getEmail();
            
            // For Social Login, we map to a static ID or handle backend registration
            getSharedPreferences("ImplantIQ", MODE_PRIVATE).edit()
                    .putInt("doctor_id", 1) // Default for demo
                    .putString("doctor_name", name)
                    .apply();

            Toast.makeText(this, "Welcome " + name, Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("doctor_name", name);
            startActivity(intent);
            finish();
            
        } catch (ApiException e) {
            // Fallback for demo/testing if Google Cloud is not configured
            Log.w("GoogleLogin", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Google Sign-In requires Client ID. Launching Demo Mode...", Toast.LENGTH_SHORT).show();
            
            new android.os.Handler().postDelayed(() -> {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("doctor_name", "Dr. Trinath Velugoti");
                startActivity(intent);
                finish();
            }, 1000);
        }
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

            apiService.post("/api/login", body, new ApiService.ApiCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        JSONObject user = response.getJSONObject("user");
                        int id = user.getInt("id");
                        String name = user.getString("username");

                        getSharedPreferences("ImplantIQ", MODE_PRIVATE).edit()
                                .putInt("doctor_id", id)
                                .putString("doctor_name", name)
                                .apply();

                        Toast.makeText(LoginActivity.this, "Welcome " + name, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.putExtra("doctor_name", name);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(LoginActivity.this, "Authentication Failed: " + message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
