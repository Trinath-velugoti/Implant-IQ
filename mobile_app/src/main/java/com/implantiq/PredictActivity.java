package com.implantiq;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class PredictActivity extends BaseActivity {
    private ApiService apiService;
    private TextInputEditText etDensity, etHeight, etWidth;
    private TextView tvStatus;
    private Bitmap selectedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        apiService = ApiService.getInstance(this);

        etDensity = findViewById(R.id.etBoneDensity);
        etHeight = findViewById(R.id.etBoneHeight);
        etWidth = findViewById(R.id.etBoneWidth);
        tvStatus = findViewById(R.id.tv_upload_status);

        findViewById(R.id.card_upload).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        });

        findViewById(R.id.btnPredict).setOnClickListener(v -> {
            applyGlowEffect(v);
            Toast.makeText(this, "AI Prediction Generated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ResultActivity.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                validateAndExtract();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void validateAndExtract() {
        tvStatus.setText("Validating Dental X-Ray...");
        tvStatus.setTextColor(getResources().getColor(R.color.accent_cyan));

        apiService.uploadImage("/api/analyze-xray", selectedBitmap, "PAT-TEMP", new ApiService.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                if (response.optString("status").equals("success")) {
                    etDensity.setText(String.valueOf(response.optDouble("boneDensity")));
                    etHeight.setText(String.valueOf(response.optDouble("boneHeight")));
                    etWidth.setText(String.valueOf(response.optDouble("boneWidth")));
                    tvStatus.setText("✓ Valid Dental X-Ray Analyzed");
                    tvStatus.setTextColor(getResources().getColor(R.color.success));
                } else {
                    tvStatus.setText("❌ Invalid dental X-Ray. Please upload a clear panoramic image.");
                    tvStatus.setTextColor(getResources().getColor(R.color.error));
                }
            }

            @Override
            public void onError(String message) {
                tvStatus.setText("Clinical Connection Error");
            }
        });
    }
}
