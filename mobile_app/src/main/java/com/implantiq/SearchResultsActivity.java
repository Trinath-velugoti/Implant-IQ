package com.implantiq;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rv_search_results);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        // Mock results
        PatientAdapter adapter = new PatientAdapter(new ArrayList<>(), patient -> {});
        rv.setAdapter(adapter);
    }
}
