package de.knollfrank.extensionsformaps;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.knollfrank.extensionsformaps.databinding.DialogProgressBinding;

public class ProgressOverlay {

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AlertDialog dialog;
    private DialogProgressBinding binding;
    private Runnable onCancelListener;

    public ProgressOverlay(final Context context) {
        this.context = context;
    }

    public void setOnCancelListener(Runnable onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    public void show() {
        mainHandler.post(() -> {
            if (dialog != null && dialog.isShowing()) {
                return;
            }

            final ContextThemeWrapper themeContext = new ContextThemeWrapper(context, R.style.Theme_ExtensionsForMaps_Dialog);
            binding = DialogProgressBinding.inflate(LayoutInflater.from(themeContext));

            MaterialAlertDialogBuilder builder =
                    new MaterialAlertDialogBuilder(themeContext)
                            .setView(binding.getRoot())
                            .setCancelable(false);

            if (onCancelListener != null) {
                builder.setNegativeButton(R.string.cancel, (d, which) -> onCancelListener.run());
            }

            dialog = builder.create();

            if (dialog.getWindow() != null) {
                Context baseContext = context;
                while (baseContext instanceof android.view.ContextThemeWrapper) {
                    baseContext = ((android.view.ContextThemeWrapper) baseContext).getBaseContext();
                }
                int windowType = (baseContext instanceof android.accessibilityservice.AccessibilityService)
                        ? WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                        : WindowManager.LayoutParams.TYPE_APPLICATION;
                dialog.getWindow().setType(windowType);
            }

            dialog.show();
        });
    }

    public void updateStatus(final String message) {
        mainHandler.post(() -> {
            if (binding != null) {
                binding.tvStatus.setText(message);
            }
        });
    }

    public void updateProgress(final int percentage) {
        mainHandler.post(() -> {
            if (binding != null) {
                binding.tvPercentage.setText(context.getString(R.string.percentage_format, percentage));
                binding.progressBar.setIndeterminate(false);
                binding.progressBar.setProgress(percentage);
            }
        });
    }

    public void hide() {
        mainHandler.post(() -> {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
                binding = null;
            }
        });
    }
}
