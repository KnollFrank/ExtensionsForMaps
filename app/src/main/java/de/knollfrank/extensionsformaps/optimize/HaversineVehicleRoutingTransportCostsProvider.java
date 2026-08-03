package de.knollfrank.extensionsformaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

public class HaversineVehicleRoutingTransportCostsProvider implements VehicleRoutingTransportCostsProvider {

    @Override
    public VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) {
        final VehicleRoutingTransportCostsMatrix.Builder matrixBuilder =
                VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for (final Stop from : route.stops()) {
            for (final Stop to : route.stops()) {
                matrixBuilder.addTransportDistance(
                        from.id(),
                        to.id(),
                        HaversineDistanceCalculator.calculateDistance(from, to));
            }
        }
        return matrixBuilder.build();
    }
}
