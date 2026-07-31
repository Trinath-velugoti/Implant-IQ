package com.implantiq;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FaqActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        populateFaq();
    }

    private void populateFaq() {
        LinearLayout container = findViewById(R.id.faq_container);
        String[][] faqs = {
            {"How accurate are AI predictions?", "The system uses a validated RandomForest model trained on 10,000+ historical cases with 94% accuracy."},
            {"What data is required?", "Bone density (g/cm³), height, width, and intended implant dimensions are required for prediction."},
            {"Is the data HIPAA compliant?", "Yes, all patient clinical data is stored in your private local MySQL instance."}
        };

        for (String[] faq : faqs) {
            View v = getLayoutInflater().inflate(R.layout.layout_summary_row, container, false);
            ((TextView) v.findViewById(R.id.tvLabel)).setText(faq[0]);
            ((TextView) v.findViewById(R.id.tvValue)).setText("Show");
            
            TextView ans = new TextView(this);
            ans.setText(faq[1]);
            ans.setTextColor(getResources().getColor(R.color.secondary_text));
            ans.setPadding(0, 0, 0, 32);
            ans.setVisibility(View.GONE);
            
            v.setOnClickListener(view -> {
                if (ans.getVisibility() == View.GONE) {
                    ans.setVisibility(View.VISIBLE);
                    ((TextView) v.findViewById(R.id.tvValue)).setText("Hide");
                } else {
                    ans.setVisibility(View.GONE);
                    ((TextView) v.findViewById(R.id.tvValue)).setText("Show");
                }
            });
            
            container.addView(v);
            container.addView(ans);
        }
    }
}
