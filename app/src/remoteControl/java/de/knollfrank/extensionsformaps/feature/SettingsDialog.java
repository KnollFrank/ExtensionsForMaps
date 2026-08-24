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
import de.knollfrank.extensionsformaps.databinding.DialogApiKeyBinding;
import de.knollfrank.extensionsformaps.databinding.DialogSettingsBinding;
import de.knollfrank.extensionsformaps.optimize.OptimizationType;
import de.knollfrank.extensionsformaps.optimize.ors.OpenRouteServiceRoutingMatrixProvider;

public class SettingsDialog {

    private final AccessibilityService accessibilityService;
    private final ApiKeyRepository apiKeyRepository;

    public SettingsDialog(final AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
        this.apiKeyRepository = new ApiKeyRepository(accessibilityService);
    }

    public void show() {
        final Context themedContext = new ContextThemeWrapper(accessibilityService, R.style.Theme_ExtensionsForMaps_Dialog);
        final DialogSettingsBinding binding = DialogSettingsBinding.inflate(LayoutInflater.from(themedContext));

        binding.checkBoxShowPreview.setChecked(SortConfig.shouldShowRoutePreview(accessibilityService));

        List
                .of(
                        binding.checkBoxShowPreview,
                        binding.tvGeneralLabel)
                .forEach(view -> view.setVisibility(BuildConfig.FEATURE_ROUTE_PREVIEW_VISIBLE ? View.VISIBLE : View.GONE));

        binding.layoutHaversine.setOnClickListener(
                view -> {
                    // Manual RadioButton management to allow clicking the whole container/description
                    binding.rbHaversine.setChecked(true);
                    binding.rbOrs.setChecked(false);
                });
        binding.layoutOrs.setOnClickListener(
                view -> {
                    if (binding.rbOrs.isEnabled()) {
                        binding.rbOrs.setChecked(true);
                        binding.rbHaversine.setChecked(false);
                    }
                });

        binding.layoutFixedDest.setOnClickListener(
                view -> {
                    binding.rbFixedDest.setChecked(true);
                    binding.rbAnyDest.setChecked(false);
                });
        binding.layoutAnyDest.setOnClickListener(
                view -> {
                    binding.rbAnyDest.setChecked(true);
                    binding.rbFixedDest.setChecked(false);
                });

        final Runnable updateOrsState =
                () -> {
                    final boolean hasApiKey = apiKeyRepository.getApiKey().isPresent();
                    binding.layoutOrs.setEnabled(hasApiKey);
                    binding.rbOrs.setEnabled(hasApiKey);
                    binding.tvOrsDesc.setAlpha(hasApiKey ? 0.7f : 0.3f);
                    if (!hasApiKey && binding.rbOrs.isChecked()) {
                        binding.rbHaversine.setChecked(true);
                        binding.rbOrs.setChecked(false);
                    }
                };

        updateOrsState.run();

        final SharedPreferences.OnSharedPreferenceChangeListener prefsListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {

                    @Override
                    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, @Nullable final String key) {
                        ContextCompat.getMainExecutor(accessibilityService).execute(updateOrsState);
                    }
                };

        final SharedPreferences prefs = apiKeyRepository.getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        final AlertDialog settingsDialog =
                new MaterialAlertDialogBuilder(themedContext)
                        .setTitle(R.string.settings_title)
                        .setView(binding.getRoot())
                        .setCancelable(false)
                        .setPositiveButton(
                                R.string.ok,
                                (dialog, which) -> {
                                    SortConfig.setShouldShowRoutePreview(accessibilityService, binding.checkBoxShowPreview.isChecked());
                                    SortConfig.setOptimizationMethod(
                                            accessibilityService,
                                            binding.rbOrs.isChecked() ?
                                                    SortConfig.OptimizationMethod.OPEN_ROUTE_SERVICE :
                                                    SortConfig.OptimizationMethod.HAVERSINE);
                                    SortConfig.setOptimizationType(
                                            accessibilityService,
                                            binding.rbAnyDest.isChecked() ?
                                                    OptimizationType.ANY_DESTINATION :
                                                    OptimizationType.FIXED_DESTINATION);
                                })
                        .setNegativeButton(
                                R.string.cancel,
                                (dialog, which) -> dialog.dismiss())
                        .setOnDismissListener(dialog -> prefs.unregisterOnSharedPreferenceChangeListener(prefsListener))
                        .create();

        binding.btnConfigureOrs.setOnClickListener(view -> showApiKeyDialog(themedContext, settingsDialog));

        if (SortConfig.getOptimizationMethod(accessibilityService) == SortConfig.OptimizationMethod.HAVERSINE) {
            binding.rbHaversine.setChecked(true);
            binding.rbOrs.setChecked(false);
        } else if (binding.rbOrs.isEnabled()) {
            binding.rbOrs.setChecked(true);
            binding.rbHaversine.setChecked(false);
        } else {
            binding.rbHaversine.setChecked(true);
            binding.rbOrs.setChecked(false);
        }

        if (SortConfig.getOptimizationType(accessibilityService) == OptimizationType.FIXED_DESTINATION) {
            binding.rbFixedDest.setChecked(true);
            binding.rbAnyDest.setChecked(false);
        } else {
            binding.rbAnyDest.setChecked(true);
            binding.rbFixedDest.setChecked(false);
        }

        if (settingsDialog.getWindow() != null) {
            settingsDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
        }
        settingsDialog.show();
    }

    private void showApiKeyDialog(final Context context, final AlertDialog parentDialog) {
        final DialogApiKeyBinding binding = DialogApiKeyBinding.inflate(LayoutInflater.from(context));

        apiKeyRepository.getApiKey().ifPresent(binding.etApiKey::setText);

        final AlertDialog apiKeyDialog =
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.api_key_title)
                        .setView(binding.getRoot())
                        .setCancelable(false)
                        .setPositiveButton(R.string.api_key_save, null)
                        .setNegativeButton(
                                R.string.cancel,
                                (dialog, which) -> dialog.dismiss())
                        .create();

        binding.tvLink.setOnClickListener(v -> {
            final Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(binding.tvLink.getText().toString()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            accessibilityService.startActivity(intent);
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
                            final String apiKey =
                                    binding.etApiKey.getText() != null ?
                                            binding.etApiKey.getText().toString().trim() :
                                            "";
                            if (apiKey.isEmpty()) {
                                binding.etApiKey.setError(accessibilityService.getString(R.string.api_key_error_empty));
                                return;
                            }

                            binding.progressBar.setVisibility(View.VISIBLE);
                            apiKeyDialog
                                    .getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setEnabled(false);
                            binding.etApiKey.setEnabled(false);

                            new Thread(() -> {
                                try {
                                    OpenRouteServiceRoutingMatrixProvider.validateApiKey(apiKey);
                                    ContextCompat
                                            .getMainExecutor(accessibilityService)
                                            .execute(() -> {
                                                apiKeyRepository.saveApiKey(apiKey);
                                                Toast
                                                        .makeText(accessibilityService, R.string.api_key_success, Toast.LENGTH_SHORT)
                                                        .show();
                                                apiKeyDialog.dismiss();
                                            });
                                } catch (final IOException e) {
                                    ContextCompat
                                            .getMainExecutor(accessibilityService)
                                            .execute(() -> {
                                                binding.progressBar.setVisibility(View.GONE);
                                                apiKeyDialog
                                                        .getButton(AlertDialog.BUTTON_POSITIVE)
                                                        .setEnabled(true);
                                                binding.etApiKey.setEnabled(true);
                                                binding.etApiKey.setError(accessibilityService.getString(R.string.api_key_error_invalid, e.getMessage()));
                                            });
                                }
                            }).start();
                        });
    }
}
