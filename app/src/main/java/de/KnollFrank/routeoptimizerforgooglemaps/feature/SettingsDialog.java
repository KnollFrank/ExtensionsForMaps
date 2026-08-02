package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;

import de.KnollFrank.routeoptimizerforgooglemaps.ApiKeyRepository;
import de.KnollFrank.routeoptimizerforgooglemaps.BuildConfig;
import de.KnollFrank.routeoptimizerforgooglemaps.R;
import de.KnollFrank.routeoptimizerforgooglemaps.SortConfig;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.OptimizationType;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;

public class SettingsDialog {

    private final AccessibilityService service;

    public SettingsDialog(final AccessibilityService service) {
        this.service = service;
    }

    public void show() {
        final Context themedContext = new ContextThemeWrapper(service, R.style.Theme_RouteoptimizerForGoogleMaps_Dialog);
        final View dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_settings, null);

        final CheckBox checkBox = dialogView.findViewById(R.id.checkBoxShowPreview);
        checkBox.setChecked(SortConfig.shouldShowRoutePreview(service));

        final View tvGeneralLabel = dialogView.findViewById(R.id.tvGeneralLabel);
        final int visibility = BuildConfig.FEATURE_ROUTE_PREVIEW_VISIBLE ? View.VISIBLE : View.GONE;
        checkBox.setVisibility(visibility);
        tvGeneralLabel.setVisibility(visibility);

        final View layoutHaversine = dialogView.findViewById(R.id.layoutHaversine);
        final RadioButton rbHaversine = dialogView.findViewById(R.id.rbHaversine);
        final View layoutOrs = dialogView.findViewById(R.id.layoutOrs);
        final RadioButton rbOrs = dialogView.findViewById(R.id.rbOrs);
        final TextView tvOrsDesc = dialogView.findViewById(R.id.tvOrsDesc);
        final View btnConfigureOrs = dialogView.findViewById(R.id.btnConfigureOrs);

        final View layoutFixedDest = dialogView.findViewById(R.id.layoutFixedDest);
        final RadioButton rbFixedDest = dialogView.findViewById(R.id.rbFixedDest);
        final View layoutAnyDest = dialogView.findViewById(R.id.layoutAnyDest);
        final RadioButton rbAnyDest = dialogView.findViewById(R.id.rbAnyDest);

        // Manual RadioButton management to allow clicking the whole container/description
        final View.OnClickListener haversineClick = v -> {
            rbHaversine.setChecked(true);
            rbOrs.setChecked(false);
        };
        final View.OnClickListener orsClick = v -> {
            if (rbOrs.isEnabled()) {
                rbOrs.setChecked(true);
                rbHaversine.setChecked(false);
            }
        };

        layoutHaversine.setOnClickListener(haversineClick);
        layoutOrs.setOnClickListener(orsClick);

        final View.OnClickListener fixedDestClick = v -> {
            rbFixedDest.setChecked(true);
            rbAnyDest.setChecked(false);
        };
        final View.OnClickListener anyDestClick = v -> {
            rbAnyDest.setChecked(true);
            rbFixedDest.setChecked(false);
        };

        layoutFixedDest.setOnClickListener(fixedDestClick);
        layoutAnyDest.setOnClickListener(anyDestClick);

        final Runnable updateOrsState = () -> {
            final boolean hasApiKey = ApiKeyRepository.getApiKey(service).isPresent();
            layoutOrs.setEnabled(hasApiKey);
            rbOrs.setEnabled(hasApiKey);
            tvOrsDesc.setAlpha(hasApiKey ? 0.7f : 0.3f);
            if (!hasApiKey && rbOrs.isChecked()) {
                rbHaversine.setChecked(true);
                rbOrs.setChecked(false);
            }
        };

        updateOrsState.run();

        final SharedPreferences.OnSharedPreferenceChangeListener prefsListener = (sharedPreferences, key) ->
                ContextCompat.getMainExecutor(service).execute(updateOrsState);

        final SharedPreferences prefs = ApiKeyRepository.getSharedPreferences(service);
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        final AlertDialog settingsDialog = new MaterialAlertDialogBuilder(themedContext)
                .setTitle(R.string.settings_title)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    SortConfig.setShouldShowRoutePreview(service, checkBox.isChecked());
                    SortConfig.setOptimizationMethod(
                            service,
                            rbOrs.isChecked() ?
                                    SortConfig.OptimizationMethod.OPEN_ROUTE_SERVICE :
                                    SortConfig.OptimizationMethod.HAVERSINE);
                    SortConfig.setOptimizationType(
                            service,
                            rbAnyDest.isChecked() ?
                                    OptimizationType.ANY_DESTINATION :
                                    OptimizationType.FIXED_DESTINATION);
                })
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .setOnDismissListener(d -> prefs.unregisterOnSharedPreferenceChangeListener(prefsListener))
                .create();

        btnConfigureOrs.setOnClickListener(v -> showApiKeyDialog(themedContext, settingsDialog));

        if (SortConfig.getOptimizationMethod(service) == SortConfig.OptimizationMethod.HAVERSINE) {
            rbHaversine.setChecked(true);
            rbOrs.setChecked(false);
        } else if (rbOrs.isEnabled()) {
            rbOrs.setChecked(true);
            rbHaversine.setChecked(false);
        } else {
            rbHaversine.setChecked(true);
            rbOrs.setChecked(false);
        }

        if (SortConfig.getOptimizationType(service) == OptimizationType.FIXED_DESTINATION) {
            rbFixedDest.setChecked(true);
            rbAnyDest.setChecked(false);
        } else {
            rbAnyDest.setChecked(true);
            rbFixedDest.setChecked(false);
        }

        if (settingsDialog.getWindow() != null) {
            settingsDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
        }
        settingsDialog.show();
    }

    private void showApiKeyDialog(final Context context, final AlertDialog parentDialog) {
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_api_key, null);
        final EditText etApiKey = dialogView.findViewById(R.id.etApiKey);
        final ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        final TextView tvLink = dialogView.findViewById(R.id.tvLink);

        ApiKeyRepository.getApiKey(service).ifPresent(etApiKey::setText);

        final AlertDialog apiKeyDialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.api_key_title)
                .setView(dialogView)
                .setPositiveButton(R.string.api_key_save, null)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .create();

        tvLink.setOnClickListener(v -> {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tvLink.getText().toString()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            service.startActivity(intent);
            // Close both dialogs so they don't cover the browser
            apiKeyDialog.dismiss();
            parentDialog.dismiss();
        });

        if (apiKeyDialog.getWindow() != null) {
            apiKeyDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
        }

        apiKeyDialog.show();

        apiKeyDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String apiKey = etApiKey.getText().toString().trim();
            if (apiKey.isEmpty()) {
                etApiKey.setError(service.getString(R.string.api_key_error_empty));
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            apiKeyDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            etApiKey.setEnabled(false);

            new Thread(() -> {
                try {
                    OpenRouteServiceRoutingMatrixProvider.validateApiKey(apiKey);
                    ContextCompat.getMainExecutor(service).execute(() -> {
                        ApiKeyRepository.saveApiKey(service, apiKey);
                        Toast.makeText(service, R.string.api_key_success, Toast.LENGTH_SHORT).show();
                        apiKeyDialog.dismiss();
                    });
                } catch (final IOException e) {
                    ContextCompat.getMainExecutor(service).execute(() -> {
                        progressBar.setVisibility(View.GONE);
                        apiKeyDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        etApiKey.setEnabled(true);
                        etApiKey.setError(service.getString(R.string.api_key_error_invalid, e.getMessage()));
                    });
                }
            }).start();
        });
    }
}
