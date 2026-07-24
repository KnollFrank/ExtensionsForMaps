package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

@FunctionalInterface
public interface VehicleRoutingTransportCostsProvider {

    VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) throws Exception;
}
