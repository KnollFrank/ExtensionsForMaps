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

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.databinding.DialogActivateLicenseBinding;
import de.knollfrank.extensionsformaps.license.LicenseManagerProvider;

public class UpgradeDialog {

    private static final String GUMROAD_URL = "https://knollfrank.gumroad.com/l/yhszp";

    public static void showUpgradeDialog(final Context context, final Runnable onActivated) {
        final AlertDialog upgradeDialog =
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.upgrade_pro_title)
                        .setMessage(R.string.upgrade_pro_message)
                        .setPositiveButton(R.string.upgrade_pro_checkout_button, null) // Listener set later to prevent auto-dismiss
                        .setNeutralButton(
                                R.string.license_enter_key,
                                (dialog, which) ->
                                        showActivationDialog(
                                                context,
                                                onActivated,
                                                () -> showUpgradeDialog(context, onActivated)))
                        .setNegativeButton(
                                R.string.cancel,
                                (dialog, which) -> {
                                    if (context instanceof final Activity activity) {
                                        activity.finish();
                                    }
                                })
                        .setCancelable(false)
                        .create();

        upgradeDialog.show();

        // Override positive button to prevent auto-dismiss when opening checkout
        upgradeDialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> openGumroadCheckout(context));
    }

    public static void openGumroadCheckout(final Context context) {
        new CustomTabsIntent
                .Builder()
                .build()
                .launchUrl(context, Uri.parse(GUMROAD_URL));
    }

    public static void showActivationDialog(final Context context,
                                            final Runnable onActivated,
                                            final Runnable onCancel) {
        final DialogActivateLicenseBinding binding = DialogActivateLicenseBinding.inflate(LayoutInflater.from(context));
        final AlertDialog activationDialog =
                new MaterialAlertDialogBuilder(context)
                        .setView(binding.getRoot())
                        .setPositiveButton(R.string.license_activate, null) // Set listener later to prevent auto-dismiss
                        .setNegativeButton(
                                R.string.cancel,
                                (dialogInterface, which) -> onCancel.run())
                        .create();

        activationDialog.show();

        activationDialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(
                        new View.OnClickListener() {

                            @Override
                            public void onClick(final View view) {
                                final String licenseKey =
                                        Optional
                                                .ofNullable(binding.etLicenseKey.getText())
                                                .map(_licenseKey -> _licenseKey.toString().trim())
                                                .orElse("");
                                if (licenseKey.isEmpty()) {
                                    binding.etLicenseKey.setError(context.getString(R.string.license_key_hint));
                                    return;
                                }

                                binding.progressBar.setVisibility(View.VISIBLE);
                                setEnabled(false);

                                LicenseManagerProvider
                                        .getInstance(context)
                                        .activate(licenseKey)
                                        .thenAccept(success ->
                                                            activationDialog
                                                                    .getButton(AlertDialog.BUTTON_POSITIVE)
                                                                    .post(() -> {
                                                                        if (success) {
                                                                            Toast
                                                                                    .makeText(context, R.string.license_success, Toast.LENGTH_LONG)
                                                                                    .show();
                                                                            activationDialog.dismiss();
                                                                            onActivated.run();
                                                                        } else {
                                                                            binding.progressBar.setVisibility(View.GONE);
                                                                            setEnabled(true);
                                                                            binding.etLicenseKey.setError(context.getString(R.string.license_error_invalid));
                                                                        }
                                                                    }))
                                        .exceptionally(throwable -> {
                                            activationDialog
                                                    .getButton(AlertDialog.BUTTON_POSITIVE)
                                                    .post(() -> {
                                                        binding.progressBar.setVisibility(View.GONE);
                                                        setEnabled(true);
                                                        Toast
                                                                .makeText(context, context.getString(R.string.error_with_message, throwable.getMessage()), Toast.LENGTH_LONG)
                                                                .show();
                                                    });
                                            return null;
                                        });
                            }

                            private void setEnabled(final boolean enabled) {
                                List
                                        .of(
                                                activationDialog.getButton(AlertDialog.BUTTON_POSITIVE),
                                                binding.etLicenseKey)
                                        .forEach(view -> view.setEnabled(enabled));
                            }
                        });
    }
}
