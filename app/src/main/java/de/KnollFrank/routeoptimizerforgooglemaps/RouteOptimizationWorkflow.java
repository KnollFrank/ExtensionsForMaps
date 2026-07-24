package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;

import java.net.URL;

import de.KnollFrank.routeoptimizerforgooglemaps.optimize.RouteOptimizer;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

public class RouteOptimizationWorkflow {

    private final RouteOptimizationOrchestrator routeOptimizationOrchestrator;

    public RouteOptimizationWorkflow(final RouteOptimizer routeOptimizer, final Context context) {
        routeOptimizationOrchestrator =
                new RouteOptimizationOrchestrator(
                        new RouteOptimizationOrchestrator.Callback() {

                            @Override
                            public void onExtractRouteFromDirectionsUrlStarted() {
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
                                GoogleMapsNavigator.launchRouteOverview(optimizedRoute, context);
                            }

                            @Override
                            public void onError(final String message) {
                                throw new RuntimeException(message);
                            }
                        },
                        routeOptimizer);
    }

    public void optimizeThenShowRoute(final URL directionsUrl) {
        routeOptimizationOrchestrator.extractRouteFromDirectionsUrl(directionsUrl);
    }
}
