package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

public class OsrmVehicleRoutingTransportCostsProvider implements VehicleRoutingTransportCostsProvider {

    private final RoutingMatricesProvider routingMatricesProvider;

    public OsrmVehicleRoutingTransportCostsProvider(final RoutingMatricesProvider routingMatricesProvider) {
        this.routingMatricesProvider = routingMatricesProvider;
    }

    @Override
    public VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) throws Exception {
        return new OsrmTransportCosts(
                routingMatricesProvider.getRoutingMatrices(
                        route.origin(),
                        Lists.concat(route.waypoints(), route.destination())));
    }
}
