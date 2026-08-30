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

import de.knollfrank.extensionsformaps.GoogleMapsNavigator;
import de.knollfrank.extensionsformaps.ProgressOverlay;
import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.SortConfig;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteDirectionsUrlConverter;
import de.knollfrank.extensionsformaps.route.Routes;
import de.knollfrank.extensionsformaps.route.extract.GoogleMapsRouteExtractor;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrlFactory;

public class AddStopActivity extends AppCompatActivity {

    private static final String TAG = AddStopActivity.class.getSimpleName();
    private ProgressOverlay progressOverlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressOverlay = new ProgressOverlay(this);
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
                    processUrl(url);
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

    private void processUrl(final URL url) {
        progressOverlay.show();
        progressOverlay.updateStatus(getString(R.string.status_reading_route));
        DirectionsUrlFactory
                .createDirectionsUrl(url)
                .thenApply(
                        optionalDirectionsUrl ->
                                optionalDirectionsUrl
                                        .map(GoogleMapsRouteExtractor::extractRoute)
                                        .orElseThrow(() -> new IllegalArgumentException("Invalid URL: " + url)))
                .handleAsync(
                        (route, throwable) -> {
                            progressOverlay.hide();
                            if (throwable != null) {
                                Log.e(TAG, "Error processing route", throwable);
                                Toast
                                        .makeText(this, R.string.error_processing_route, Toast.LENGTH_LONG)
                                        .show();
                                finish();
                            } else {
                                handleRoute(route);
                            }
                            return null;
                        },
                        ContextCompat.getMainExecutor(this));
    }

    private void handleRoute(Route route) {
        int stopCount = route.stops().size();
        if (stopCount >= 27) {
            AddStopLimitReachedDialog.show(this);
        } else if (stopCount >= 15 && !LicenseManagerProvider.getInstance(this).isPro()) {
            showUpgradeDialog(route);
        } else if (stopCount < 10) {
            SuggestMapsDialog.show(
                    this,
                    new SuggestMapsDialog.Callback() {

                        @Override
                        public void onYes() {
                            addStopAndFinish(route);
                        }

                        @Override
                        public void onCancel() {
                            finish();
                        }
                    });
        } else {
            addStopAndFinish(route);
        }
    }

    private void showUpgradeDialog(Route route) {
        UpgradeDialog.showUpgradeDialog(this, () -> handleRoute(route));
    }

    private void addStopAndFinish(final Route route) {
        CompletableFuture
                .supplyAsync(() -> RouteDirectionsUrlConverter.getDirectionsUrl(Routes.addDummyStop(route)))
                .handleAsync(
                        (directionsUrl, throwable) -> {
                            if (throwable != null) {
                                Log.e(TAG, "Error adding stop", throwable);
                                Toast
                                        .makeText(this, R.string.error_processing_route, Toast.LENGTH_LONG)
                                        .show();
                                finish();
                            } else {
                                if (SortConfig.shouldShowAddStopInstruction(this)) {
                                    AddStopInstructionDialog.show(
                                            this,
                                            getLayoutInflater(),
                                            () -> {
                                                GoogleMapsNavigator.launchDirectionsUrl(directionsUrl, this.getApplicationContext());
                                                finish();
                                            });
                                } else {
                                    GoogleMapsNavigator.launchDirectionsUrl(directionsUrl, getApplicationContext());
                                    finish();
                                }
                            }
                            return null;
                        },
                        ContextCompat.getMainExecutor(this));
    }

    @Nullable
    private URL extractUrl(String text) {
        Pattern pattern = Pattern.compile("https?://\\S+");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return new URL(matcher.group());
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed URL extracted: " + matcher.group(), e);
            }
        }
        return null;
    }
}
