package com.implantiq;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

public class DoctorChatActivity extends BaseActivity {
    private LinearLayout chatContainer;
    private EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_chat);

        chatContainer = findViewById(R.id.chat_container);
        etMessage = findViewById(R.id.et_message);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_send).setOnClickListener(v -> {
            applyGlowEffect(v);
            sendMessage();
        });

        addMessage("Support", "Hello Dr. Trinath, I am the ImplantIQ Core AI. How can I assist with your clinical workflow today?");
    }

    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();
        if (msg.isEmpty()) return;

        addMessage("Me", msg);
        etMessage.setText("");

        // Enhanced AI Response Logic
        etMessage.postDelayed(() -> {
            String q = msg.toLowerCase(Locale.ROOT);
            String r = "I'm currently processing your request. For immediate dashboard navigation, please use the bottom navigation bar.";
            
            if (q.contains("dashboard") || q.contains("go through")) {
                r = "You can access clinical metrics on the dashboard. Click 'Patients' for registry, or 'Predict' to start an AI analysis.";
            } else if (q.contains("what are you doing") || q.contains("who are you")) {
                r = "I am the ImplantIQ Core AI, designed to assist with dental implant survival predictions and clinical data management.";
            } else if (q.contains("predict") || q.contains("analysis")) {
                r = "To run a prediction, upload an X-ray in the Predict tab. I will auto-extract bone metrics for you.";
            } else if (q.contains("risk")) {
                r = "The 'Critical Risks' filter shows patients with Grade C or lower, which might require your immediate attention.";
            } else if (q.contains("hi") || q.contains("hello")) {
                r = "Hello Dr. Trinath! System status is nominal. Ready for analysis.";
            }

            addMessage("AI Assistant", r);
        }, 1200);
    }

    private void addMessage(String sender, String text) {
        TextView tv = new TextView(this);
        tv.setText(sender + ": " + text);
        tv.setTextColor(getResources().getColor(R.color.white));
        tv.setBackgroundResource(sender.equals("Me") ? R.color.card_inner : R.color.surface);
        tv.setPadding(32, 24, 32, 24);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        params.gravity = sender.equals("Me") ? Gravity.END : Gravity.START;
        tv.setLayoutParams(params);
        
        chatContainer.addView(tv);
    }
}
