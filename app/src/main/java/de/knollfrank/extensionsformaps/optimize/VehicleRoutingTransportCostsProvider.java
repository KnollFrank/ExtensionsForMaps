package de.knollfrank.extensionsformaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;

import de.knollfrank.extensionsformaps.route.Route;

@FunctionalInterface
public interface VehicleRoutingTransportCostsProvider {

    VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) throws Exception;
}
