package de.knollfrank.extensionsformaps.feature;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import de.knollfrank.extensionsformaps.UrlExpander;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;
import de.knollfrank.extensionsformaps.route.GoogleMapsRouteExtractor;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteToUrlConverter;
import de.knollfrank.extensionsformaps.route.Routes;

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

    private void processUrl(URL url) {
        progressOverlay.show();
        progressOverlay.updateStatus(getString(R.string.status_reading_route));
        CompletableFuture
                .supplyAsync(
                        () -> {
                            try {
                                URL expandedUrl = UrlExpander.expandUrl(url);
                                return GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(expandedUrl);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                .thenAccept(
                        route -> runOnUiThread(() -> {
                            progressOverlay.hide();
                            handleRoute(route);
                        }))
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, "Error processing route", throwable);
                            runOnUiThread(() -> {
                                progressOverlay.hide();
                                Toast
                                        .makeText(this, R.string.error_processing_route, Toast.LENGTH_LONG)
                                        .show();
                                finish();
                            });
                            return null;
                        });
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
        UpgradeDialog.show(this, () -> handleRoute(route));
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
                .show();
    }

    private void addStopAndFinish(Route route) {
        CompletableFuture.supplyAsync(() -> RouteToUrlConverter.getUrl(Routes.addDummyStop(route)))
                .thenAccept(expandedUrl -> runOnUiThread(() -> {
                    if (SortConfig.shouldShowAddStopInstruction(this)) {
                        showInstructionDialog(expandedUrl);
                    } else {
                        GoogleMapsNavigator.launchUrl(expandedUrl, getApplicationContext());
                        finish();
                    }
                }))
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error adding stop", throwable);
                    runOnUiThread(() -> {
                        Toast
                                .makeText(this, R.string.error_processing_route, Toast.LENGTH_LONG)
                                .show();
                        finish();
                    });
                    return null;
                });
    }

    private void showInstructionDialog(URL url) {
        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_add_stop_instruction, null);
        android.widget.CheckBox cbDontShowAgain = view.findViewById(R.id.cbDontShowAgain);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_stop_instruction_title)
                .setView(view)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (cbDontShowAgain.isChecked()) {
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
