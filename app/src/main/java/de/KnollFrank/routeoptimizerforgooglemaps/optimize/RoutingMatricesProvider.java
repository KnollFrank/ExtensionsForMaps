package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

@FunctionalInterface
public interface RoutingMatricesProvider {

    RoutingMatrices getRoutingMatrices(final Geodetic start, final List<Geodetic> stops) throws Exception;
}
