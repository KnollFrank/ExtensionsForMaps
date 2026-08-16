package de.knollfrank.extensionsformaps.feature;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.RouteOptimizationWorkflow;
import de.knollfrank.extensionsformaps.route.RouteOptimizerFactory;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrlFactory;

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
                final URL url = extractUrl(sharedText);
                if (url != null) {
                    startOptimizationWorkflow(url);
                } else {
                    Toast
                            .makeText(this, R.string.error_no_route_found, Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void startOptimizationWorkflow(final URL url) {
        final RouteOptimizationWorkflow workflow = new RouteOptimizationWorkflow(RouteOptimizerFactory.createRouteOptimizer(this), this);
        workflow.setShowOptimizationTypeDialog(true);
        DirectionsUrlFactory
                .createDirectionsUrl(url)
                .thenAcceptAsync(
                        optionalDirectionsUrl -> {
                            final DirectionsUrl directionsUrl =
                                    optionalDirectionsUrl.orElseThrow(() -> new IllegalArgumentException("Invalid URL: " + url));
                            workflow.optimizeThenShowRoute(directionsUrl);
                        },
                        ContextCompat.getMainExecutor(this))
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, "Error creating DirectionsUrl", throwable);
                            runOnUiThread(() ->
                                                  Toast
                                                          .makeText(this, R.string.error_general, Toast.LENGTH_LONG)
                                                          .show());
                            return null;
                        });
    }

    // FK-TODO: use Optional<URL>
    @Nullable
    private URL extractUrl(final String text) {
        final Pattern pattern = Pattern.compile("https?://\\S+");
        final Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return new URL(matcher.group());
            } catch (final MalformedURLException e) {
                Log.e(TAG, "Malformed URL: " + matcher.group(), e);
            }
        }
        return null;
    }
}
