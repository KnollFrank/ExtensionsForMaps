package de.knollfrank.extensionsformaps.feature;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.databinding.DialogRouteStagesBinding;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteSplitter;

public class RouteStagesDialog {

    public static void show(final Context context,
                            final Route fullRoute,
                            final Runnable onDismiss) {
        final List<Route> stages = RouteSplitter.splitInto10StopBlocks(fullRoute);
        final ContextThemeWrapper themeContext = new ContextThemeWrapper(context, R.style.Theme_ExtensionsForMaps_Dialog);
        final DialogRouteStagesBinding binding = DialogRouteStagesBinding.inflate(LayoutInflater.from(themeContext));

        binding.tvStagesTitle.setText(
                context.getString(R.string.route_stages_title, fullRoute.stops().size()));

        final RouteStagesAdapter adapter = new RouteStagesAdapter(stages);
        binding.recyclerViewStages.setLayoutManager(new LinearLayoutManager(themeContext));
        binding.recyclerViewStages.setAdapter(adapter);

        final AlertDialog dialog = new MaterialAlertDialogBuilder(themeContext)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (d, which) -> {
                    d.dismiss();
                    if (onDismiss != null) {
                        onDismiss.run();
                    }
                })
                .setOnDismissListener(d -> {
                    if (onDismiss != null) {
                        onDismiss.run();
                    }
                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        }
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
    }
}
