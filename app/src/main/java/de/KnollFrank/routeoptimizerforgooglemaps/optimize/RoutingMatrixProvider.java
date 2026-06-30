package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

@FunctionalInterface
public interface RoutingMatrixProvider {

    // FK-TODO: Parameter start und stops kürzen auf einen einzigen Parameter "final List<Stop> stops"
    RoutingMatrix getRoutingMatrix(final Stop start, final List<Stop> stops) throws Exception;
}
