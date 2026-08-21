package de.knollfrank.extensionsformaps.feature;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.atomic.AtomicReference;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.SortConfig;
import de.knollfrank.extensionsformaps.optimize.OptimizationType;

public class OptimizationTypeDialog {

    public interface Callback {

        void onOptimizationTypeSelected(OptimizationType selectedType);

        void onCancel();
    }

    public static void show(final Context context, final Callback callback) {
        final AtomicReference<OptimizationType> selectedType = new AtomicReference<>(SortConfig.getOptimizationType(context));
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.sort_dialog_title)
                .setSingleChoiceItems(
                        new String[]{
                                context.getString(R.string.settings_type_fixed_destination),
                                context.getString(R.string.settings_type_any_destination)
                        },
                        selectedType.get() == OptimizationType.FIXED_DESTINATION ? 0 : 1,
                        (dialog, which) ->
                                selectedType.set(
                                        which == 0 ?
                                                OptimizationType.FIXED_DESTINATION :
                                                OptimizationType.ANY_DESTINATION))
                .setCancelable(false)
                .setPositiveButton(
                        R.string.ok,
                        (dialog, which) -> {
                            SortConfig.setOptimizationType(context, selectedType.get());
                            callback.onOptimizationTypeSelected(selectedType.get());
                        })
                .setNegativeButton(
                        R.string.cancel,
                        (dialog, which) -> callback.onCancel())
                .setOnCancelListener(dialog -> callback.onCancel())
                .show();
    }
}
