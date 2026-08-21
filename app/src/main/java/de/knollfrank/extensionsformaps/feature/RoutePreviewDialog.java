package de.knollfrank.extensionsformaps.feature;

import android.app.Activity;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.Consumer;

import de.knollfrank.extensionsformaps.ProgressOverlay;
import de.knollfrank.extensionsformaps.R;
import de.knollfrank.extensionsformaps.StopsAdapter;
import de.knollfrank.extensionsformaps.databinding.DialogRoutePreviewBinding;
import de.knollfrank.extensionsformaps.route.Route;

public class RoutePreviewDialog {

    public static void show(final Context context, final Route route, final Consumer<Route> onOk) {
        show(route, new ContextThemeWrapper(context, R.style.Theme_ExtensionsForMaps_Dialog), onOk);
    }

    private static void show(final Route route, final ContextThemeWrapper themeContext, final Consumer<Route> onOk) {
        final DialogRoutePreviewBinding binding = DialogRoutePreviewBinding.inflate(LayoutInflater.from(themeContext));
        final StopsAdapter stopsAdapter = new StopsAdapter();
        stopsAdapter.setRoute(route);
        binding.recyclerViewStops.setLayoutManager(new LinearLayoutManager(themeContext));
        binding.recyclerViewStops.setAdapter(stopsAdapter);
        final AlertDialog routePreviewDialog =
                new MaterialAlertDialogBuilder(themeContext)
                        .setTitle(R.string.route_preview_title)
                        .setCancelable(false)
                        .setView(binding.getRoot())
                        .setPositiveButton(
                                R.string.ok,
                                (dialog, which) -> stopsAdapter.getRoute().ifPresent(onOk))
                        .setNegativeButton(
                                R.string.cancel,
                                (dialog, which) -> {
                                    dialog.dismiss();
                                    if (themeContext.getBaseContext() instanceof final Activity activity) {
                                        activity.finish();
                                    }
                                })
                        .create();
        if (routePreviewDialog.getWindow() != null) {
            routePreviewDialog.getWindow().setType(ProgressOverlay.getWindowType(themeContext));
        }
        routePreviewDialog.show();
        if (routePreviewDialog.getWindow() != null) {
            routePreviewDialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
    }
}
