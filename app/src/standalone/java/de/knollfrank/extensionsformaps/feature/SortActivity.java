package de.knollfrank.extensionsformaps.feature;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knollfrank.extensionsformaps.RouteOptimizationWorkflow;
import de.knollfrank.extensionsformaps.route.RouteOptimizerFactory;

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
                    startOptimizationWorkflow(url);
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

    private void startOptimizationWorkflow(URL url) {
        Toast.makeText(this, "Route wird geladen...", Toast.LENGTH_SHORT).show();
        RouteOptimizationWorkflow workflow = new RouteOptimizationWorkflow(RouteOptimizerFactory.createRouteOptimizer(this), this);
        workflow.setShowOptimizationTypeDialog(true);
        workflow.optimizeThenShowRoute(url);
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
}
