package de.KnollFrank.routeoptimizerforgooglemaps;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.common.HeadAndTail;
import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.route.DirectionsUrlPredicate;
import de.KnollFrank.routeoptimizerforgooglemaps.route.GoogleMapsRouteExtractor;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

public class RouteOptimizationOrchestrator {

    public interface Callback {

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

    // FK-TODO: refactor
    public void optimizeRouteOfDirectionsUrl(final String directionsUrl) {
        callback.onOptimizationStarted();
        new Thread(() -> {
            try {
                final URL expandedDirectionsUrl = expandShortDirectionsUrl(new URL(directionsUrl));
                final Route route = GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(expandedDirectionsUrl);
                if (route.stops().isEmpty()) {
                    callback.onError("No stops found in URL.");
                    return;
                }
                final Route finalRoute = optimizeRoute(route);
                callback.onOptimizationSuccess(finalRoute);
            } catch (final IOException e) {
                callback.onError("Network error: " + e.getMessage());
            } catch (final Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private static URL expandShortDirectionsUrl(final URL directionsUrl) throws IOException {
        return DirectionsUrlPredicate.isShortDirectionsUrl(directionsUrl) ?
                UrlExpander.expandUrl(directionsUrl) :
                directionsUrl;
    }

    private Route optimizeRoute(final Route route) throws Exception {
        final HeadAndTail<Stop> startAndIntermediate =
                Lists
                        .asHeadAndTail(route.stops())
                        .orElseThrow();
        return new Route(
                ImmutableList
                        .<Stop>builder()
                        .add(startAndIntermediate.head())
                        .addAll(
                                routeOptimizer.optimizeStops(
                                        startAndIntermediate.head().geodetic(),
                                        startAndIntermediate.tail(),
                                        RouteOptimizer.OptimizationStrategy.OSRM))
                        .build());
    }
}