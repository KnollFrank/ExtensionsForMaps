package de.KnollFrank.routeoptimizerforgooglemaps;

import java.util.List;

@FunctionalInterface
public interface RoutingMatricesProvider {

    RouteOptimizer.RoutingMatrices getRoutingMatrices(final double startLat,
                                                      final double startLng,
                                                      final List<RouteOptimizer.Stop> stops) throws Exception;
}
