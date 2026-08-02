package de.KnollFrank.routeoptimizerforgooglemaps.feature;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.KnollFrank.routeoptimizerforgooglemaps.R;
import de.KnollFrank.routeoptimizerforgooglemaps.license.LicenseManagerProvider;

public class UpgradeDialog {

    public static void show(Context context, Runnable onActivated) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.upgrade_pro_title)
                .setMessage(R.string.upgrade_pro_message)
                .setPositiveButton(R.string.license_enter_key, (dialog, which) -> showActivationDialog(context, onActivated))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void showActivationDialog(Context context, Runnable onActivated) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_activate_license, null);
        EditText etKey = view.findViewById(R.id.etLicenseKey);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setPositiveButton(R.string.license_activate, null) // Set listener later to prevent auto-dismiss
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String key = etKey.getText().toString().trim();
            if (LicenseManagerProvider.getInstance(context).activate(key)) {
                Toast.makeText(context, R.string.license_success, Toast.LENGTH_LONG).show();
                dialog.dismiss();
                if (onActivated != null) {
                    onActivated.run();
                }
            } else {
                etKey.setError(context.getString(R.string.license_error_invalid));
            }
        });
    }
}
