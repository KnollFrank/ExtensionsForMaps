package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

public class OsrmVehicleRoutingTransportCostsProvider implements VehicleRoutingTransportCostsProvider {

    private final RoutingMatricesProvider routingMatricesProvider;

    public OsrmVehicleRoutingTransportCostsProvider(final RoutingMatricesProvider routingMatricesProvider) {
        this.routingMatricesProvider = routingMatricesProvider;
    }

    @Override
    public VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) throws Exception {
        return new OsrmTransportCosts(
                routingMatricesProvider.getRoutingMatrices(
                        route.origin().geodetic(),
                        getGeodetics(Lists.concat(route.waypoints(), route.destination()))));
    }

    private static List<Geodetic> getGeodetics(final List<Stop> stops) {
        return stops
                .stream()
                .map(Stop::geodetic)
                .toList();
    }
}
