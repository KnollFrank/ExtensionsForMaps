package de.knollfrank.extensionsformaps.feature;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.List;

import de.knollfrank.extensionsformaps.ApiKeyRepository;
import de.knollfrank.extensionsformaps.BuildConfig;
import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.SortConfig;
import de.knollfrank.extensionsformaps.optimize.OptimizationType;
import de.knollfrank.extensionsformaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;

public class SettingsDialog {

    private final AccessibilityService service;

    public SettingsDialog(final AccessibilityService service) {
        this.service = service;
    }

    public void show() {
        final Context themedContext = new ContextThemeWrapper(service, R.style.Theme_ExtensionsForMaps_Dialog);
        final View dialogView =
                LayoutInflater
                        .from(themedContext)
                        .inflate(R.layout.dialog_settings, null);

        final CheckBox checkBoxShowPreview = dialogView.findViewById(R.id.checkBoxShowPreview);
        checkBoxShowPreview.setChecked(SortConfig.shouldShowRoutePreview(service));

        List
                .of(
                        checkBoxShowPreview,
                        dialogView.findViewById(R.id.tvGeneralLabel))
                .forEach(view -> view.setVisibility(BuildConfig.FEATURE_ROUTE_PREVIEW_VISIBLE ? View.VISIBLE : View.GONE));

        final RadioButton rbHaversine = dialogView.findViewById(R.id.rbHaversine);
        final View layoutOrs = dialogView.findViewById(R.id.layoutOrs);
        final RadioButton rbOrs = dialogView.findViewById(R.id.rbOrs);
        final TextView tvOrsDesc = dialogView.findViewById(R.id.tvOrsDesc);

        final RadioButton rbFixedDest = dialogView.findViewById(R.id.rbFixedDest);
        final RadioButton rbAnyDest = dialogView.findViewById(R.id.rbAnyDest);

        dialogView
                .findViewById(R.id.layoutHaversine)
                .setOnClickListener(
                        view -> {
                            // Manual RadioButton management to allow clicking the whole container/description
                            rbHaversine.setChecked(true);
                            rbOrs.setChecked(false);
                        });
        layoutOrs.setOnClickListener(
                view -> {
                    if (rbOrs.isEnabled()) {
                        rbOrs.setChecked(true);
                        rbHaversine.setChecked(false);
                    }
                });

        dialogView
                .findViewById(R.id.layoutFixedDest)
                .setOnClickListener(
                        view -> {
                            rbFixedDest.setChecked(true);
                            rbAnyDest.setChecked(false);
                        });
        dialogView
                .findViewById(R.id.layoutAnyDest)
                .setOnClickListener(
                        view -> {
                            rbAnyDest.setChecked(true);
                            rbFixedDest.setChecked(false);
                        });

        final Runnable updateOrsState =
                () -> {
                    final boolean hasApiKey =
                            ApiKeyRepository
                                    .getApiKey(service)
                                    .isPresent();
                    layoutOrs.setEnabled(hasApiKey);
                    rbOrs.setEnabled(hasApiKey);
                    tvOrsDesc.setAlpha(hasApiKey ? 0.7f : 0.3f);
                    if (!hasApiKey && rbOrs.isChecked()) {
                        rbHaversine.setChecked(true);
                        rbOrs.setChecked(false);
                    }
                };

        updateOrsState.run();

        final SharedPreferences.OnSharedPreferenceChangeListener prefsListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {

                    @Override
                    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, @Nullable final String key) {
                        ContextCompat.getMainExecutor(service).execute(updateOrsState);
                    }
                };

        final SharedPreferences prefs = ApiKeyRepository.getSharedPreferences(service);
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        final AlertDialog settingsDialog =
                new MaterialAlertDialogBuilder(themedContext)
                        .setTitle(R.string.settings_title)
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton(
                                R.string.ok,
                                (dialog, which) -> {
                                    SortConfig.setShouldShowRoutePreview(service, checkBoxShowPreview.isChecked());
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
                        .setNegativeButton(
                                R.string.cancel,
                                (dialog, which) -> dialog.dismiss())
                        .setOnDismissListener(dialog -> prefs.unregisterOnSharedPreferenceChangeListener(prefsListener))
                        .create();

        dialogView
                .findViewById(R.id.btnConfigureOrs)
                .setOnClickListener(view -> showApiKeyDialog(themedContext, settingsDialog));

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
        final View dialogView =
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.dialog_api_key, null);
        final EditText etApiKey = dialogView.findViewById(R.id.etApiKey);
        final ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        final TextView tvLink = dialogView.findViewById(R.id.tvLink);

        ApiKeyRepository.getApiKey(service).ifPresent(etApiKey::setText);

        final AlertDialog apiKeyDialog =
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.api_key_title)
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton(R.string.api_key_save, null)
                        .setNegativeButton(
                                R.string.cancel,
                                (dialog, which) -> dialog.dismiss())
                        .create();

        tvLink.setOnClickListener(v -> {
            final Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(tvLink.getText().toString()));
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

        apiKeyDialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(
                        view -> {
                            final String apiKey = etApiKey.getText().toString().trim();
                            if (apiKey.isEmpty()) {
                                etApiKey.setError(service.getString(R.string.api_key_error_empty));
                                return;
                            }

                            progressBar.setVisibility(View.VISIBLE);
                            apiKeyDialog
                                    .getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setEnabled(false);
                            etApiKey.setEnabled(false);

                            new Thread(() -> {
                                try {
                                    OpenRouteServiceRoutingMatrixProvider.validateApiKey(apiKey);
                                    ContextCompat
                                            .getMainExecutor(service)
                                            .execute(() -> {
                                                ApiKeyRepository.saveApiKey(service, apiKey);
                                                Toast
                                                        .makeText(service, R.string.api_key_success, Toast.LENGTH_SHORT)
                                                        .show();
                                                apiKeyDialog.dismiss();
                                            });
                                } catch (final IOException e) {
                                    ContextCompat
                                            .getMainExecutor(service)
                                            .execute(() -> {
                                                progressBar.setVisibility(View.GONE);
                                                apiKeyDialog
                                                        .getButton(AlertDialog.BUTTON_POSITIVE)
                                                        .setEnabled(true);
                                                etApiKey.setEnabled(true);
                                                etApiKey.setError(service.getString(R.string.api_key_error_invalid, e.getMessage()));
                                            });
                                }
                            }).start();
                        });
    }
}
