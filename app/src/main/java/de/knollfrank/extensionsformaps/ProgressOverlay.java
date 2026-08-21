package de.knollfrank.extensionsformaps;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Optional;

import de.knollfrank.extensionsformaps.databinding.DialogProgressBinding;

public class ProgressOverlay {

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AlertDialog dialog;
    private DialogProgressBinding binding;
    private Optional<Runnable> onCancelListener = Optional.empty();

    public ProgressOverlay(final Context context) {
        this.context = context;
    }

    public void setOnCancelListener(final Runnable onCancelListener) {
        this.onCancelListener = Optional.ofNullable(onCancelListener);
    }

    public void show() {
        mainHandler.post(() -> {
            if (dialog != null && dialog.isShowing()) {
                return;
            }
            final ContextThemeWrapper themeContext = new ContextThemeWrapper(context, R.style.Theme_ExtensionsForMaps_Dialog);
            binding = DialogProgressBinding.inflate(LayoutInflater.from(themeContext));
            dialog = createAlertDialog(themeContext, onCancelListener, binding.getRoot());
            if (dialog.getWindow() != null) {
                dialog.getWindow().setType(getWindowType(context));
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

    private static AlertDialog createAlertDialog(final Context context,
                                                 final Optional<Runnable> onCancelListener,
                                                 final View root) {
        final MaterialAlertDialogBuilder dialogBuilder =
                new MaterialAlertDialogBuilder(context)
                        .setView(root)
                        .setCancelable(false);
        onCancelListener.ifPresent(_onCancelListener -> configureNegativeButton(dialogBuilder, _onCancelListener));
        return dialogBuilder.create();
    }

    private static void configureNegativeButton(final MaterialAlertDialogBuilder dialogBuilder,
                                                final Runnable onCancelListener) {
        dialogBuilder.setNegativeButton(
                R.string.cancel,
                (dialog, which) -> onCancelListener.run());
    }

    private static int getWindowType(final Context context) {
        return getBaseContext(context) instanceof AccessibilityService ?
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY :
                WindowManager.LayoutParams.TYPE_APPLICATION;
    }

    private static Context getBaseContext(final Context context) {
        Context baseContext = context;
        while (baseContext instanceof final ContextThemeWrapper contextThemeWrapper) {
            baseContext = contextThemeWrapper.getBaseContext();
        }
        return baseContext;
    }
}
