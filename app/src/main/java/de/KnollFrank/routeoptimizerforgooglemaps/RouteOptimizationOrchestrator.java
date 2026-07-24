package de.KnollFrank.routeoptimizerforgooglemaps;

import java.io.IOException;
import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.optimize.RouteOptimizer;
import de.KnollFrank.routeoptimizerforgooglemaps.route.DirectionsUrlPredicate;
import de.KnollFrank.routeoptimizerforgooglemaps.route.GoogleMapsRouteExtractor;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

public class RouteOptimizationOrchestrator {

    public interface Callback {

        void onExtractRouteFromDirectionsUrlStarted();

        void onExtractRouteFromDirectionsUrlSuccess(Route route);

        void onOptimizationStarted();

        void onOptimizationSuccess(Route optimizedRoute);

        void onError(String message);
    }

    private final Callback callback;
    private final RouteOptimizer routeOptimizer;

    public RouteOptimizationOrchestrator(final Callback callback, final RouteOptimizer routeOptimizer) {
        this.callback = callback;
        this.routeOptimizer = routeOptimizer;
    }

    // FK-TODO: spezialisierten callback mit den Methoden onExtractRouteFromDirectionsUrlStarted(), onExtractRouteFromDirectionsUrlSuccess() und onError() direkt als Parameter übergeben oder als Continuation zurückgeben.
    public void extractRouteFromDirectionsUrl(final URL directionsUrl) {
        callback.onExtractRouteFromDirectionsUrlStarted();
        new Thread(() -> {
            try {
                callback.onExtractRouteFromDirectionsUrlSuccess(
                        GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(
                                expandShortDirectionsUrl(
                                        directionsUrl)));
            } catch (final IOException e) {
                callback.onError("Network error: " + e.getMessage());
            } catch (final Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    // FK-TODO: spezialisierten callback mit den Methoden onOptimizationStarted(), onOptimizationSuccess() und onError() direkt als Parameter übergeben oder als Continuation zurückgeben.
    public void optimizeRoute(final Route route) {
        callback.onOptimizationStarted();
        new Thread(() -> {
            try {
                callback.onOptimizationSuccess(routeOptimizer.optimize(route));
            } catch (final Exception e) {
                callback.onError("Optimization error: " + e.getMessage());
            }
        }).start();
    }

    private static URL expandShortDirectionsUrl(final URL directionsUrl) throws IOException {
        return DirectionsUrlPredicate.isShortDirectionsUrl(directionsUrl) ?
                UrlExpander.expandUrl(directionsUrl) :
                directionsUrl;
    }
}