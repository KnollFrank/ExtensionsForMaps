package de.knollfrank.extensionsformaps.feature;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knollfrank.extensionsformaps.GoogleMapsNavigator;
import de.knollfrank.extensionsformaps.ProgressOverlay;
import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.SortConfig;
import de.knollfrank.extensionsformaps.databinding.DialogAddStopInstructionBinding;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteToUrlConverter;
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
            showLimitReachedDialog();
        } else if (stopCount >= 15 && !LicenseManagerProvider.getInstance(this).isPro()) {
            showUpgradeDialog(route);
        } else if (stopCount < 10) {
            showSuggestMapsDialog(route);
        } else {
            addStopAndFinish(route);
        }
    }

    private void showUpgradeDialog(Route route) {
        UpgradeDialog.showUpgradeDialog(this, () -> handleRoute(route));
    }

    private void showLimitReachedDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_stop_limit_reached_title)
                .setMessage(R.string.add_stop_limit_reached_message)
                .setPositiveButton(R.string.ok, (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .show();
    }

    private void showSuggestMapsDialog(Route route) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_stop_suggest_maps_title)
                .setMessage(R.string.add_stop_suggest_maps_message)
                .setPositiveButton(R.string.add_stop_yes, (dialog, which) -> addStopAndFinish(route))
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .setCancelable(false)
                .show();
    }

    private void addStopAndFinish(final Route route) {
        CompletableFuture
                .supplyAsync(() -> RouteToUrlConverter.getUrl(Routes.addDummyStop(route)))
                .handleAsync(
                        (expandedUrl, throwable) -> {
                            if (throwable != null) {
                                Log.e(TAG, "Error adding stop", throwable);
                                Toast
                                        .makeText(this, R.string.error_processing_route, Toast.LENGTH_LONG)
                                        .show();
                                finish();
                            } else {
                                if (SortConfig.shouldShowAddStopInstruction(this)) {
                                    showInstructionDialog(expandedUrl);
                                } else {
                                    GoogleMapsNavigator.launchUrl(expandedUrl, getApplicationContext());
                                    finish();
                                }
                            }
                            return null;
                        },
                        ContextCompat.getMainExecutor(this));
    }

    private void showInstructionDialog(URL url) {
        final DialogAddStopInstructionBinding binding = DialogAddStopInstructionBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_stop_instruction_title)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (binding.cbDontShowAgain.isChecked()) {
                        SortConfig.setShouldShowAddStopInstruction(this, false);
                    }
                    GoogleMapsNavigator.launchUrl(url, getApplicationContext());
                    finish();
                })
                .setCancelable(false)
                .show();
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
