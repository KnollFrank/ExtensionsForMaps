package de.knollfrank.extensionsformaps;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

import de.knollfrank.extensionsformaps.optimize.RouteOptimizer;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.RouteDirectionsUrlConverter;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

public class RouteOptimizationOrchestrator {

    public interface Callback {

        void onExtractRouteFromDirectionsUrlStarted();

        void onExtractRouteFromDirectionsUrlSuccess(Route route);

        void onOptimizationStarted();

        void onOptimizationProgress(int progressPercentage);

        void onOptimizationSuccess(Route optimizedRoute);

        void onError(String message);
    }

    private final Context context;
    private final Callback callback;
    private final RouteOptimizer routeOptimizer;
    private final AtomicBoolean isOptimizationCanceled = new AtomicBoolean(false);

    public RouteOptimizationOrchestrator(final Context context,
                                         final Callback callback,
                                         final RouteOptimizer routeOptimizer) {
        this.context = context;
        this.callback = callback;
        this.routeOptimizer = routeOptimizer;
    }

    // FK-TODO: spezialisierten callback mit den Methoden onExtractRouteFromDirectionsUrlStarted(), onExtractRouteFromDirectionsUrlSuccess() und onError() direkt als Parameter übergeben oder als Continuation zurückgeben.
    public void extractRouteFromDirectionsUrl(final DirectionsUrl directionsUrl) {
        callback.onExtractRouteFromDirectionsUrlStarted();
        new Thread(() -> {
            try {
                callback.onExtractRouteFromDirectionsUrlSuccess(RouteDirectionsUrlConverter.getRoute(directionsUrl));
            } catch (final Exception e) {
                callback.onError(context.getString(R.string.error_general, e.getMessage()));
            }
        }).start();
    }

    // FK-TODO: spezialisierten callback mit den Methoden onOptimizationStarted(), onOptimizationSuccess() und onError() direkt als Parameter übergeben oder als Continuation zurückgeben.
    public void optimizeRoute(final Route route) {
        isOptimizationCanceled.set(false);
        callback.onOptimizationStarted();
        new Thread(() -> {
            try {
                callback.onOptimizationSuccess(
                        routeOptimizer.optimize(
                                route,
                                SortConfig.getOptimizationType(context),
                                callback::onOptimizationProgress,
                                isOptimizationCanceled::get));
            } catch (final InterruptedException e) {
                // Optimization was canceled, no error message needed
            } catch (final Exception e) {
                final String msg = e.getMessage();
                if (msg != null && msg.startsWith("UNASSIGNED_JOBS:")) {
                    callback.onError(context.getString(R.string.error_unassigned_jobs, msg.substring(16)));
                } else {
                    callback.onError(context.getString(R.string.error_optimization, e.getMessage()));
                }
            }
        }).start();
    }

    public void cancelOptimization() {
        isOptimizationCanceled.set(true);
    }
}
