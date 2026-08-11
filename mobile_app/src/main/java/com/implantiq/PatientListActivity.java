package com.implantiq;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PatientListActivity extends BaseActivity {
    private List<Patient> patientList = new ArrayList<>();
    private List<Patient> filteredList = new ArrayList<>();
    private PatientAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        apiService = ApiService.getInstance(this);
        RecyclerView rvPatients = findViewById(R.id.rv_patients);
        rvPatients.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PatientAdapter(filteredList, patient -> {
            Intent intent = new Intent(this, PatientDetailActivity.class);
            intent.putExtra("patient_id", patient.getId());
            intent.putExtra("patient_name", patient.getName());
            startActivity(intent);
        });
        rvPatients.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(this::fetchPatients);

        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterList(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        String filter = getIntent().getStringExtra("filter");
        fetchPatients(filter);
        setupBottomNav();

        findViewById(R.id.btn_add_patient).setOnClickListener(v -> startActivity(new Intent(this, AddPatientActivity.class)));
        findViewById(R.id.fab_add).setOnClickListener(v -> startActivity(new Intent(this, AddPatientActivity.class)));
    }

    private void fetchPatients(String filter) {
        swipeRefresh.setRefreshing(true);
        int userId = getSharedPreferences("ImplantIQ", MODE_PRIVATE).getInt("doctor_id", 1);
        apiService.getArray(NetworkConfig.API_PATIENTS + "?user_id=" + userId, new ApiService.ApiCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray response) {
                patientList.clear();
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.getJSONObject(i);
                        String grade = obj.optString("grade", "N/A");
                        String date = obj.optString("prediction_date", "N/A");
                        
                        // Apply Clinical Filter if requested
                        if ("risk".equals(filter) && !grade.contains("C")) continue;
                        
                        if ("today".equals(filter)) {
                            String today = getSharedPreferences("ImplantIQ", MODE_PRIVATE).getString("server_today", "");
                            if (today.isEmpty()) {
                                today = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
                            }
                            if (!date.equals(today)) continue;
                        }

                        patientList.add(new Patient(
                                obj.getString("patient_id"),
                                obj.getString("name"),
                                date,
                                grade
                        ));
                    }
                    filterList(((EditText)findViewById(R.id.et_search)).getText().toString());
                } catch (JSONException e) {
                    Snackbar.make(findViewById(android.R.id.content), "Parse Error", Snackbar.LENGTH_SHORT).show();
                }
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                Snackbar.make(findViewById(android.R.id.content), "Offline: " + message, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", v -> fetchPatients(filter)).show();
            }
        });
    }

    private void fetchPatients() {
        fetchPatients(null);
    }

    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(patientList);
        } else {
            for (Patient p : patientList) {
                if (p.getName().toLowerCase().contains(query.toLowerCase()) || p.getId().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_patients);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) { startActivity(new Intent(this, DashboardActivity.class)); finish(); return true; }
            if (id == R.id.nav_patients) return true;
            if (id == R.id.nav_predict) { startActivity(new Intent(this, PredictActivity.class)); finish(); return true; }
            if (id == R.id.nav_reports) { startActivity(new Intent(this, ReportsActivity.class)); finish(); return true; }
            if (id == R.id.nav_settings) { startActivity(new Intent(this, SettingsActivity.class)); finish(); return true; }
            return false;
        });
    }
}
