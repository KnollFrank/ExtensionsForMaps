package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

public class OsrmVehicleRoutingTransportCostsProvider implements VehicleRoutingTransportCostsProvider {

    private final RoutingMatrixProvider routingMatrixProvider;

    public OsrmVehicleRoutingTransportCostsProvider(final RoutingMatrixProvider routingMatrixProvider) {
        this.routingMatrixProvider = routingMatrixProvider;
    }

    @Override
    public VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) throws Exception {
        return new OsrmTransportCosts(getRoutingMatrix(route));
    }

    private RoutingMatrix getRoutingMatrix(final Route route) throws Exception {
        return routingMatrixProvider.getRoutingMatrix(toSet(route.stops()));
    }

    private static <T> Set<T> toSet(final List<T> ts) {
        return ts.stream().collect(Collectors.toUnmodifiableSet());
    }
}
