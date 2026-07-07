package de.KnollFrank.routeoptimizerforgooglemaps;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;

public class ApiKeyActivity extends AppCompatActivity {

    private EditText etApiKey;
    private Button btnSave;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_key);

        etApiKey = findViewById(R.id.etApiKey);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        final String apiKey = etApiKey.getText().toString().trim();
        if (apiKey.isEmpty()) {
            etApiKey.setError("Bitte gib einen API Key ein");
            return;
        }

        setLoading(true);
        new Thread(() -> {
            try {
                OpenRouteServiceRoutingMatrixProvider.validateApiKey(apiKey);
                runOnUiThread(() -> {
                    ApiKeyRepository.saveApiKey(this, apiKey);
                    Toast.makeText(this, "API Key erfolgreich gespeichert!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (final IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    etApiKey.setError("API Key ungültig oder Netzwerkfehler: " + e.getMessage());
                });
            }
        }).start();
    }

    private void setLoading(final boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
        etApiKey.setEnabled(!isLoading);
    }
}
