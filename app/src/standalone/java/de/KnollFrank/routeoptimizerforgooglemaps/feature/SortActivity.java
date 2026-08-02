package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.KnollFrank.routeoptimizerforgooglemaps.R;
import de.KnollFrank.routeoptimizerforgooglemaps.RouteOptimizationWorkflow;
import de.KnollFrank.routeoptimizerforgooglemaps.SortConfig;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.OptimizationType;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteOptimizerFactory;

public class SortActivity extends AppCompatActivity {

    private static final String TAG = SortActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }

        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                URL url = extractUrl(sharedText);
                if (url != null) {
                    showOptimizationTypeDialog(url);
                } else {
                    Toast.makeText(this, "Keine Google Maps Route gefunden.", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void showOptimizationTypeDialog(URL url) {
        String[] options = {
                getString(R.string.settings_type_fixed_destination),
                getString(R.string.settings_type_any_destination)
        };
        final OptimizationType[] selectedType = {SortConfig.getOptimizationType(this)};
        int checkedItem = (selectedType[0] == OptimizationType.FIXED_DESTINATION) ? 0 : 1;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sort_dialog_title)
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    selectedType[0] = (which == 0) ? OptimizationType.FIXED_DESTINATION : OptimizationType.ANY_DESTINATION;
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    SortConfig.setOptimizationType(this, selectedType[0]);
                    startOptimization(url);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .show();
    }

    private void startOptimization(URL url) {
        Toast.makeText(this, "Route wird optimiert...", Toast.LENGTH_SHORT).show();
        // Pass 'this' instead of applicationContext so the UI elements can use the activity's window token.
        new RouteOptimizationWorkflow(RouteOptimizerFactory.createRouteOptimizer(this), this)
                .optimizeThenShowRoute(url);
    }

    @Nullable
    private URL extractUrl(String text) {
        Pattern pattern = Pattern.compile("https?://\\S+");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return new URL(matcher.group());
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed URL: " + matcher.group(), e);
            }
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Once the workflow launches Google Maps, this activity goes to background (onStop).
        // We finish it then to keep the task list clean.
        if (!isFinishing()) {
            finish();
        }
    }
}
