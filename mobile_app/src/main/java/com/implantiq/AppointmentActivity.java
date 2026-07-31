package com.implantiq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AppointmentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        populateAppointments();
    }

    private void populateAppointments() {
        LinearLayout container = findViewById(R.id.appointment_container);
        String[][] data = {
            {"Aditi Rao", "10:30 AM", "Surgery", "Confirmed"},
            {"Vikram Singh", "12:00 PM", "Consultation", "Pending"},
            {"Sneha Kapoor", "03:15 PM", "Follow-up", "Confirmed"}
        };

        for (String[] appt : data) {
            View v = LayoutInflater.from(this).inflate(R.layout.layout_summary_row, container, false);
            ((TextView) v.findViewById(R.id.tvLabel)).setText(appt[0] + " (" + appt[2] + ")");
            ((TextView) v.findViewById(R.id.tvValue)).setText(appt[1]);
            
            TextView status = new TextView(this);
            status.setText("Status: " + appt[3]);
            status.setTextColor(appt[3].equals("Confirmed") ? getResources().getColor(R.color.success) : getResources().getColor(R.color.warning));
            status.setPadding(0, 0, 0, 48);
            
            container.addView(v);
            container.addView(status);
        }
    }
}
