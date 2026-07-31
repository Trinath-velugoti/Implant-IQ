package com.implantiq;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONObject;

public class UploadXrayActivity extends AppCompatActivity {
    private ApiService apiService;
    private View loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_xray);

        apiService = ApiService.getInstance(this);
        loadingOverlay = findViewById(R.id.loading_overlay); // Assuming I add this to XML

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_take_photo).setOnClickListener(v -> {
            cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
        });

        findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        });
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    analyzeXray();
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    analyzeXray();
                }
            }
    );

    private void analyzeXray() {
        loadingOverlay.setVisibility(View.VISIBLE);
        
        // Simulation of multipart upload
        apiService.post("/api/analyze-xray", new JSONObject(), new ApiService.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {
                loadingOverlay.setVisibility(View.GONE);
                Intent result = new Intent();
                try {
                    result.putExtra("boneDensity", response.getDouble("boneDensity"));
                    result.putExtra("boneHeight", response.getDouble("boneHeight"));
                    result.putExtra("boneWidth", response.getDouble("boneWidth"));
                    result.putExtra("implantLength", response.getDouble("implantLength"));
                    result.putExtra("implantDiameter", response.getDouble("implantDiameter"));
                    
                    setResult(RESULT_OK, result);
                    finish();
                } catch (Exception e) {
                    showError("Analysis failed");
                }
            }

            @Override
            public void onError(String message) {
                loadingOverlay.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
