package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

public class OsrmVehicleRoutingTransportCostsProvider implements VehicleRoutingTransportCostsProvider {

    private final RoutingMatrixProvider routingMatrixProvider;

    public OsrmVehicleRoutingTransportCostsProvider(final RoutingMatrixProvider routingMatrixProvider) {
        this.routingMatrixProvider = routingMatrixProvider;
    }

    @Override
    public VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) throws Exception {
        return new OsrmTransportCosts(
                routingMatrixProvider.getRoutingMatrix(
                        route.origin(),
                        Lists.concat(route.waypoints(), route.destination())));
    }
}
