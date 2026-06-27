package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

@FunctionalInterface
public interface RoutingMatricesProvider {

    RoutingMatrices getRoutingMatrices(final Geodetic start, final List<Stop> stops) throws Exception;
}
