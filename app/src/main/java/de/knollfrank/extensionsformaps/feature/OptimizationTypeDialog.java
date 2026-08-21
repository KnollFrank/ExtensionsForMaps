package de.knollfrank.extensionsformaps.feature;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.collect.ImmutableBiMap;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.SortConfig;
import de.knollfrank.extensionsformaps.optimize.OptimizationType;

public class OptimizationTypeDialog {

    public interface Callback {

        void onOptimizationTypeSelected(OptimizationType selectedType);

        void onCancel();
    }

    private static final ImmutableBiMap<OptimizationType, Integer> optimizationTypeToStringRes =
            ImmutableBiMap.of(
                    OptimizationType.FIXED_DESTINATION, R.string.settings_type_fixed_destination,
                    OptimizationType.ANY_DESTINATION, R.string.settings_type_any_destination);

    private static final List<OptimizationType> optimizationTypes = optimizationTypeToStringRes.keySet().asList();

    public static void show(final Context context, final Callback callback) {
        final AtomicReference<OptimizationType> selectedOptimizationType = new AtomicReference<>(SortConfig.getOptimizationType(context));
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.sort_dialog_title)
                .setSingleChoiceItems(
                        getSingleChoiceItems(context),
                        optimizationTypes.indexOf(selectedOptimizationType.get()),
                        (dialog, which) -> selectedOptimizationType.set(optimizationTypes.get(which)))
                .setCancelable(false)
                .setPositiveButton(
                        R.string.ok,
                        (dialog, which) -> {
                            SortConfig.setOptimizationType(context, selectedOptimizationType.get());
                            callback.onOptimizationTypeSelected(selectedOptimizationType.get());
                        })
                .setNegativeButton(
                        R.string.cancel,
                        (dialog, which) -> callback.onCancel())
                .setOnCancelListener(dialog -> callback.onCancel())
                .show();
    }

    private static String[] getSingleChoiceItems(final Context context) {
        return optimizationTypes
                .stream()
                .map(optimizationType -> context.getString(Objects.requireNonNull(optimizationTypeToStringRes.get(optimizationType))))
                .toArray(String[]::new);
    }
}
