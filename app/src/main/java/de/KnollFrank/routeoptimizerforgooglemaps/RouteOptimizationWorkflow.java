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
    private final SpinnerOverlay spinnerOverlay;

    public RouteOptimizationWorkflow(final RouteOptimizer routeOptimizer, final Context context) {
        this.spinnerOverlay = new SpinnerOverlay(context);
        this.routeOptimizationOrchestrator =
                new RouteOptimizationOrchestrator(
                        createCallback(context, spinnerOverlay),
                        routeOptimizer);
    }

    RouteOptimizationWorkflow(final RouteOptimizationOrchestrator routeOptimizationOrchestrator,
                              final SpinnerOverlay spinnerOverlay) {
        this.routeOptimizationOrchestrator = routeOptimizationOrchestrator;
        this.spinnerOverlay = spinnerOverlay;
    }

    private RouteOptimizationOrchestrator.Callback createCallback(final Context context,
                                                                  final SpinnerOverlay spinnerOverlay) {
        return new RouteOptimizationOrchestrator.Callback() {

            @Override
            public void onExtractRouteFromDirectionsUrlStarted() {
                spinnerOverlay.show();
            }

            @Override
            public void onExtractRouteFromDirectionsUrlSuccess(final Route route) {
                routeOptimizationOrchestrator.optimizeRoute(route);
            }

            @Override
            public void onOptimizationStarted() {
            }

            @Override
            public void onOptimizationSuccess(final Route optimizedRoute) {
                spinnerOverlay.hide();
                GoogleMapsNavigator.launchRouteOverview(optimizedRoute, context);
            }

            @Override
            public void onError(final String message) {
                spinnerOverlay.hide();
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
