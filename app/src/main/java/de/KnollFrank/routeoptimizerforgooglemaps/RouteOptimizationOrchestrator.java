package de.KnollFrank.routeoptimizerforgooglemaps;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.route.DirectionsUrlPredicate;
import de.KnollFrank.routeoptimizerforgooglemaps.route.GoogleMapsRouteExtractor;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

public class RouteOptimizationOrchestrator {

    public interface Callback {

        void onOptimizationStarted();

        void onOptimizationSuccess(List<RouteOptimizer.Stop> finalRoute);

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
                final List<RouteOptimizer.Stop> stops =
                        convert(
                                GoogleMapsRouteExtractor
                                        .extractRouteFromDirectionsUrl(expandedDirectionsUrl)
                                        .stops());
                if (stops.isEmpty()) {
                    callback.onError("No stops found in URL.");
                    return;
                }
                final List<RouteOptimizer.Stop> finalRoute =
                        stops.size() < 2 ?
                                stops :
                                optimizeRoute(stops);
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

    private static List<RouteOptimizer.Stop> convert(final List<Stop> stops) {
        return stops
                .stream()
                .map(RouteOptimizationOrchestrator::convert)
                .toList();
    }

    private static RouteOptimizer.Stop convert(final Stop stop) {
        return new RouteOptimizer.Stop(
                stop.address(),
                stop.geodetic().getLatitude().toDegrees(),
                stop.geodetic().getLongitude().toDegrees());
    }

    private List<RouteOptimizer.Stop> optimizeRoute(final List<RouteOptimizer.Stop> stops) throws Exception {
        final RouteOptimizer.Stop start = stops.get(0);
        final List<RouteOptimizer.Stop> intermediate = stops.subList(1, stops.size());
        final List<RouteOptimizer.Stop> optimizedIntermediate =
                routeOptimizer.optimize(
                        start.lat(),
                        start.lng(),
                        intermediate,
                        RouteOptimizer.OptimizationStrategy.OSRM);
        // FK-TODO: use guava ImmutableList
        final List<RouteOptimizer.Stop> finalRoute = new ArrayList<>();
        finalRoute.add(start);
        finalRoute.addAll(optimizedIntermediate);
        return finalRoute;
    }
}