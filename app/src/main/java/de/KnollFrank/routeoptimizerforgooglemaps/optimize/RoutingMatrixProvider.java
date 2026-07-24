package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import java.util.Set;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

@FunctionalInterface
public interface RoutingMatrixProvider {

    RoutingMatrix getRoutingMatrix(final Set<Stop> stops) throws Exception;
}
