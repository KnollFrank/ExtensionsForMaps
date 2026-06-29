package de.KnollFrank.routeoptimizerforgooglemaps;

import java.io.IOException;
import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.optimize.RouteOptimizer;
import de.KnollFrank.routeoptimizerforgooglemaps.optimize.VehicleRoutingTransportCostsProvider;
import de.KnollFrank.routeoptimizerforgooglemaps.route.DirectionsUrlPredicate;
import de.KnollFrank.routeoptimizerforgooglemaps.route.GoogleMapsRouteExtractor;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

public class RouteOptimizationOrchestrator {

    public interface Callback {

        void onOptimizationStarted();

        void onOptimizationSuccess(Route optimizedRoute);

        void onError(String message);
    }

    private final Callback callback;
    private final RouteOptimizer routeOptimizer;
    private final VehicleRoutingTransportCostsProvider vehicleRoutingTransportCostsProvider;

    public RouteOptimizationOrchestrator(final Callback callback,
                                         final RouteOptimizer routeOptimizer,
                                         final VehicleRoutingTransportCostsProvider vehicleRoutingTransportCostsProvider) {
        this.callback = callback;
        this.routeOptimizer = routeOptimizer;
        this.vehicleRoutingTransportCostsProvider = vehicleRoutingTransportCostsProvider;
    }

    // FK-TODO: refactor
    public void optimizeRouteOfDirectionsUrl(final String directionsUrl) {
        callback.onOptimizationStarted();
        new Thread(() -> {
            try {
                callback.onOptimizationSuccess(
                        optimizeRoute(
                                GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(
                                        expandShortDirectionsUrl(
                                                new URL(directionsUrl)))));
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
        return routeOptimizer.optimizeRoute(route, vehicleRoutingTransportCostsProvider);
    }
}