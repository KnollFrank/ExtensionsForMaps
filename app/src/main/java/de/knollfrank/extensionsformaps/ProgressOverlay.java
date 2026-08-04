package de.knollfrank.extensionsformaps;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProgressOverlay {

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AlertDialog dialog;
    private TextView statusTextView;

    public ProgressOverlay(final Context context) {
        this.context = context;
    }

    public void show() {
        mainHandler.post(() -> {
            if (dialog != null && dialog.isShowing()) {
                return;
            }

            final ContextThemeWrapper themeContext = new ContextThemeWrapper(context, R.style.Theme_ExtensionsForMaps_Dialog);
            final View view = LayoutInflater.from(themeContext).inflate(R.layout.dialog_progress, null);
            statusTextView = view.findViewById(R.id.tvStatus);

            dialog = new MaterialAlertDialogBuilder(themeContext)
                    .setView(view)
                    .setCancelable(false)
                    .create();

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
            if (statusTextView != null) {
                statusTextView.setText(message);
            }
        });
    }

    public void hide() {
        mainHandler.post(() -> {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
                statusTextView = null;
            }
        });
    }
}
