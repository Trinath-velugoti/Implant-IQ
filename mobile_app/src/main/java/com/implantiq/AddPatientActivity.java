package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class AddPatientActivity extends BaseActivity {
    private ApiService apiService;
    private EditText etName, etAge, etPhone, etHistory;
    private AutoCompleteTextView spGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        apiService = ApiService.getInstance(this);

        etName = findViewById(R.id.et_patient_name);
        etAge = findViewById(R.id.et_age);
        etPhone = findViewById(R.id.et_phone);
        etHistory = findViewById(R.id.et_medical_history);
        spGender = findViewById(R.id.sp_gender);

        if (findViewById(R.id.btn_back) != null) {
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        }

        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add).setOnClickListener(v -> {
            applyGlowEffect(v);
            savePatient();
        });
    }

    private void savePatient() {
        String name = etName.getText().toString().trim();
        if(name.isEmpty()) { Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show(); return; }

        try {
            JSONObject body = new JSONObject();
            body.put("name", name);
            body.put("age", etAge.getText().toString());
            body.put("gender", spGender.getText().toString());
            body.put("phone", etPhone.getText().toString());
            body.put("medicalHistory", etHistory.getText().toString());

            apiService.post("/api/patients", body, new ApiService.ApiCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        String pid = response.getString("patientId");
                        Toast.makeText(AddPatientActivity.this, "Patient Created: " + pid, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(AddPatientActivity.this, PredictActivity.class);
                        intent.putExtra("patient_id", pid);
                        intent.putExtra("patient_name", name);
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) { e.printStackTrace(); }
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(AddPatientActivity.this, "Save Failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) { e.printStackTrace(); }
    }
}
