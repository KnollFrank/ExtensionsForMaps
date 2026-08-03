package de.knollfrank.extensionsformaps.optimize;

import java.util.Set;

import de.knollfrank.extensionsformaps.route.Stop;

@FunctionalInterface
public interface RoutingMatrixProvider {

    RoutingMatrix getRoutingMatrix(final Set<Stop> stops) throws Exception;
}
