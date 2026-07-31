package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

public class PatientDetailActivity extends BaseActivity {
    private ApiService apiService;
    private TextView tvHeaderName, tvName, tvInfo;
    private LinearLayout historyContainer;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        apiService = ApiService.getInstance(this);
        patientId = getIntent().getStringExtra("patient_id");

        tvHeaderName = findViewById(R.id.tv_patient_name_header);
        tvName = findViewById(R.id.tv_patient_name);
        tvInfo = findViewById(R.id.tv_patient_info);
        historyContainer = findViewById(R.id.history_container);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_new_prediction).setOnClickListener(v -> {
            applyGlowEffect(v);
            Intent intent = new Intent(this, PredictActivity.class);
            intent.putExtra("patient_id", patientId);
            intent.putExtra("patient_name", tvHeaderName.getText().toString());
            startActivity(intent);
        });

        findViewById(R.id.btn_view_timeline).setOnClickListener(v -> {
            applyGlowEffect(v);
            Intent intent = new Intent(this, PatientTimelineActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        fetchPatientDetails();
    }

    private void fetchPatientDetails() {
        if (patientId == null) return;
        
        apiService.get("/api/patients/" + patientId, new ApiService.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String name = response.getString("name");
                    tvHeaderName.setText(name);
                    tvName.setText("Name: " + name);
                    
                    String grade = response.optString("grade", "N/A");
                    double survival = response.optDouble("survival_years", 0);
                    double rate = response.optDouble("success_rate", 0);
                    tvInfo.setText("Result: " + grade + " | Survival: " + survival + "y");
                    
                    addHistoryItem(response.optString("prediction_date", "Today"), grade, survival, rate);
                    
                } catch (JSONException e) {
                    Log.e("PatientDetail", "Parse error", e);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(PatientDetailActivity.this, "Failed to load record from server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addHistoryItem(String date, String grade, double survival, double rate) {
        TextView tv = new TextView(this);
        tv.setText(date + ": AI Analysis - Grade " + grade + " (" + rate + "% Success)");
        tv.setTextColor(getResources().getColor(R.color.white));
        tv.setPadding(0, 8, 0, 8);
        historyContainer.addView(tv);
    }
}
