package de.knollfrank.extensionsformaps.feature;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.databinding.DialogStopsAccumulationBinding;

public class StopsAccumulationDialog {

    public interface Callback {

        void onSortAll();

        void onCollectMore();

        void onClear();
    }

    public static void show(final Context context,
                            final int totalStopsCount,
                            final int newlyAddedCount,
                            final Callback callback) {
        final ContextThemeWrapper themeContext = new ContextThemeWrapper(context, R.style.Theme_ExtensionsForMaps_Dialog);
        final DialogStopsAccumulationBinding binding =
                DialogStopsAccumulationBinding.inflate(LayoutInflater.from(themeContext));

        binding.tvMessage.setText(
                context.getString(R.string.stops_accumulated_message_format, totalStopsCount, newlyAddedCount));
        binding.btnSortAll.setText(
                context.getString(R.string.btn_sort_all_stops, totalStopsCount));

        final AlertDialog dialog = new MaterialAlertDialogBuilder(themeContext)
                .setView(binding.getRoot())
                .setCancelable(false)
                .create();

        binding.btnSortAll.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onSortAll();
            }
        });

        binding.btnCollectMore.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onCollectMore();
            }
        });

        binding.btnClear.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) {
                callback.onClear();
            }
        });

        dialog.show();
    }
}
