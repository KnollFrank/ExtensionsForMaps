package de.knollfrank.extensionsformaps.feature;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.knollfrank.extensionsformaps.R;

public class SuggestMapsDialog {

    public interface Callback {

        void onYes();

        void onCancel();
    }

    public static void show(final Context context, final Callback callback) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.add_stop_suggest_maps_title)
                .setMessage(R.string.add_stop_suggest_maps_message)
                .setPositiveButton(
                        R.string.add_stop_yes,
                        (dialog, which) -> callback.onYes())
                .setNegativeButton(
                        R.string.cancel,
                        (dialog, which) -> callback.onCancel())
                .setOnCancelListener(dialog -> callback.onCancel())
                .setCancelable(false)
                .show();
    }
}
