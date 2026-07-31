package com.implantiq;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AppointmentReminderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_reminder);

        findViewById(R.id.btn_view_details).setOnClickListener(v -> {
            // In a real app, this would open specific appointment detail
            finish();
        });

        findViewById(R.id.btn_dismiss).setOnClickListener(v -> finish());
    }
}
