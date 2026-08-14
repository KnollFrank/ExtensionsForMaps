package de.knollfrank.extensionsformaps.feature;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.databinding.DialogActivateLicenseBinding;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;

public class UpgradeDialog {

    private static final String GUMROAD_URL = "https://knollfrank.gumroad.com/l/yhszp";

    public static void show(Context context, Runnable onActivated) {
        final AlertDialog dialog =
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.upgrade_pro_title)
                        .setMessage(R.string.upgrade_pro_message)
                        .setPositiveButton(R.string.upgrade_pro_checkout_button, null) // Listener set later to prevent auto-dismiss
                        .setNeutralButton(R.string.license_enter_key, (d, which) -> showActivationDialog(context, onActivated, () -> show(context, onActivated)))
                        .setNegativeButton(R.string.cancel, (d, which) -> {
                            if (context instanceof final Activity activity) {
                                activity.finish();
                            }
                        })
                        .setCancelable(false)
                        .create();

        dialog.show();

        // Override positive button to prevent auto-dismiss when opening checkout
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> openGumroadCheckout(context));
    }

    public static void openGumroadCheckout(Context context) {
        CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
        intent.launchUrl(context, Uri.parse(GUMROAD_URL));
    }

    public static void showActivationDialog(Context context, Runnable onActivated) {
        showActivationDialog(context, onActivated, null);
    }

    public static void showActivationDialog(Context context, Runnable onActivated, Runnable onCancel) {
        final DialogActivateLicenseBinding binding = DialogActivateLicenseBinding.inflate(LayoutInflater.from(context));

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.license_activate, null) // Set listener later to prevent auto-dismiss
                .setNegativeButton(R.string.cancel, (d, which) -> {
                    if (onCancel != null) {
                        onCancel.run();
                    }
                })
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String key = binding.etLicenseKey.getText() != null ? binding.etLicenseKey.getText().toString().trim() : "";
            if (key.isEmpty()) {
                binding.etLicenseKey.setError(context.getString(R.string.license_key_hint));
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            binding.etLicenseKey.setEnabled(false);

            LicenseManagerProvider
                    .getInstance(context)
                    .activate(key)
                    .thenAccept(success ->
                            dialog
                                    .getButton(AlertDialog.BUTTON_POSITIVE)
                                    .post(() -> {
                                        if (success) {
                                            Toast
                                                    .makeText(context, R.string.license_success, Toast.LENGTH_LONG)
                                                    .show();
                                            dialog.dismiss();
                                            if (onActivated != null) {
                                                onActivated.run();
                                            }
                                        } else {
                                            binding.progressBar.setVisibility(View.GONE);
                                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                            binding.etLicenseKey.setEnabled(true);
                                            binding.etLicenseKey.setError(context.getString(R.string.license_error_invalid));
                                        }
                                    }))
                    .exceptionally(throwable -> {
                        dialog
                                .getButton(AlertDialog.BUTTON_POSITIVE)
                                .post(() -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                    binding.etLicenseKey.setEnabled(true);
                                    Toast
                                            .makeText(context, context.getString(R.string.error_with_message, throwable.getMessage()), Toast.LENGTH_LONG)
                                            .show();
                                });
                        return null;
                    });
        });
    }
}
