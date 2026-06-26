package de.KnollFrank.routeoptimizerforgooglemaps;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface RoutingMatricesProvider {

    RouteOptimizer.RoutingMatrices getRoutingMatrices(final double startLat,
                                                      final double startLng,
                                                      final List<RouteOptimizer.Stop> stops) throws JSONException, IOException;
}
