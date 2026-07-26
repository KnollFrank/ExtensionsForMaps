package de.KnollFrank.routeoptimizerforgooglemaps;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URL;
import java.util.Locale;

import de.KnollFrank.routeoptimizerforgooglemaps.optimize.NativeSuburbResolver;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.RouteOptimizer;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

public class RouteOptimizationWorkflow {

    private final RouteOptimizationOrchestrator routeOptimizationOrchestrator;
    private final ProgressOverlay progressOverlay;

    public RouteOptimizationWorkflow(final RouteOptimizer routeOptimizer, final Context context) {
        this.progressOverlay = new ProgressOverlay(context);
        this.routeOptimizationOrchestrator =
                new RouteOptimizationOrchestrator(
                        createCallback(context, progressOverlay),
                        routeOptimizer);
    }

    RouteOptimizationWorkflow(final RouteOptimizationOrchestrator routeOptimizationOrchestrator,
                              final ProgressOverlay progressOverlay) {
        this.routeOptimizationOrchestrator = routeOptimizationOrchestrator;
        this.progressOverlay = progressOverlay;
    }

    private RouteOptimizationOrchestrator.Callback createCallback(final Context context,
                                                                  final ProgressOverlay progressOverlay) {
        return new RouteOptimizationOrchestrator.Callback() {

            @Override
            public void onExtractRouteFromDirectionsUrlStarted() {
                progressOverlay.show();
                // FK-TODO: i18n
                progressOverlay.updateStatus("Lese Route...");
            }

            @Override
            public void onExtractRouteFromDirectionsUrlSuccess(final Route route) {
                final Route enrichedRoute = addSuburbsToAddresses(route, context);
                progressOverlay.hide();
                runOnUiThread(() -> showRoutePreviewDialog(enrichedRoute, context));
            }

            @Override
            public void onOptimizationStarted() {
                // FK-TODO: i18n
                progressOverlay.updateStatus("Optimiere Route...");
            }

            @Override
            public void onOptimizationSuccess(final Route optimizedRoute) {
                progressOverlay.hide();
                GoogleMapsNavigator.launchRouteOverview(optimizedRoute, context);
            }

            @Override
            public void onError(final String message) {
                progressOverlay.hide();
                runOnUiThread(
                        () -> Toast
                                .makeText(context, message, Toast.LENGTH_LONG)
                                .show());
            }

            private void showRoutePreviewDialog(final Route enrichedRoute, final Context context) {
                final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_route_preview, null);
                final StopsAdapter stopsAdapter = new StopsAdapter();
                final RecyclerView recyclerViewStops = dialogView.findViewById(R.id.recyclerViewStops);
                recyclerViewStops.setLayoutManager(new LinearLayoutManager(context));
                recyclerViewStops.setAdapter(stopsAdapter);

                stopsAdapter.setRoute(enrichedRoute);

                final AlertDialog dialog =
                        new AlertDialog
                                .Builder(context)
                                .setTitle("Route Preview")
                                .setView(dialogView)
                                .setPositiveButton(
                                        "OK",
                                        (d, which) -> {
                                            stopsAdapter.getRoute().ifPresent(configuredRoute -> {
                                                progressOverlay.show();
                                                progressOverlay.updateStatus("Optimiere Route...");
                                                routeOptimizationOrchestrator.optimizeRoute(configuredRoute);
                                            });
                                        })
                                .setNegativeButton(
                                        "Abbrechen",
                                        (d, which) -> d.dismiss())
                                .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
                }
                dialog.show();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setLayout(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
                }
            }

            private Route addSuburbsToAddresses(final Route route, final Context context) {
                final SuburbsToAddressesAdder suburbsToAddressesAdder =
                        new SuburbsToAddressesAdder(
                                new NativeSuburbResolver(
                                        new Geocoder(
                                                context,
                                                Locale.getDefault())));
                return suburbsToAddressesAdder.addSuburbsToAddresses(route);
            }
        };
    }

    public void optimizeThenShowRoute(final URL directionsUrl) {
        routeOptimizationOrchestrator.extractRouteFromDirectionsUrl(directionsUrl);
    }

    // FK-TODO: die Methode runOnUiThread() gibt es mehrfach im Projekt. In eine neue common-Klasse auslagern.
    private static void runOnUiThread(final Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
