package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.net.URL;

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
                routeOptimizationOrchestrator.optimizeRoute(route);
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
