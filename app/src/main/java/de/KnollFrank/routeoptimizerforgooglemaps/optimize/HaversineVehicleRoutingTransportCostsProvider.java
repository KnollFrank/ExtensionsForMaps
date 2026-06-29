package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;

import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;

// FK-FEATURE: biete neben der HaversineDistance auch folgendes an:
//  - Heidelberger OpenRouteService-API (https://openrouteservice.org/)
//  - https://locationiq.com/
class HaversineVehicleRoutingTransportCostsProvider implements VehicleRoutingTransportCostsProvider {

    @Override
    public VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) {
        return new HaversineTransportCosts();
    }
}
