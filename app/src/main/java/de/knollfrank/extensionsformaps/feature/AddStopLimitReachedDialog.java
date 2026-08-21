package de.knollfrank.extensionsformaps.feature;

import android.app.Activity;
import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.knollfrank.extensionsformaps.R;

public class AddStopLimitReachedDialog {

    public static void show(final Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.add_stop_limit_reached_title)
                .setMessage(R.string.add_stop_limit_reached_message)
                .setPositiveButton(
                        R.string.ok,
                        (dialog, which) -> finish(context))
                .setOnCancelListener(dialog -> finish(context))
                .show();
    }

    private static void finish(final Context context) {
        if (context instanceof final Activity activity) {
            activity.finish();
        }
    }
}
