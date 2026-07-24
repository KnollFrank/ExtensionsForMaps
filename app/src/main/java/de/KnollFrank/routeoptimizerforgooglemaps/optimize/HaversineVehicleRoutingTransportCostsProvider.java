package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

public class HaversineVehicleRoutingTransportCostsProvider implements VehicleRoutingTransportCostsProvider {

    @Override
    public VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) {
        return new HaversineTransportCosts();
    }
}
