package de.knollfrank.extensionsformaps.optimize;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

public class OsrmVehicleRoutingTransportCostsProvider implements VehicleRoutingTransportCostsProvider {

    private final RoutingMatrixProvider routingMatrixProvider;

    public OsrmVehicleRoutingTransportCostsProvider(final RoutingMatrixProvider routingMatrixProvider) {
        this.routingMatrixProvider = routingMatrixProvider;
    }

    @Override
    public VehicleRoutingTransportCosts getVehicleRoutingTransportCosts(final Route route) throws Exception {
        final RoutingMatrix routingMatrix = getRoutingMatrix(route);
        final VehicleRoutingTransportCostsMatrix.Builder matrixBuilder =
                VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        for (final Stop from : route.stops()) {
            for (final Stop to : route.stops()) {
                final DistanceDuration distanceDuration =
                        routingMatrix
                                .getDistanceDurationByStopIdTable()
                                .get(from.id(), to.id());
                matrixBuilder.addTransportDistance(from.id(), to.id(), distanceDuration.distance());
                matrixBuilder.addTransportTime(from.id(), to.id(), distanceDuration.duration());
            }
        }
        return matrixBuilder.build();
    }

    private RoutingMatrix getRoutingMatrix(final Route route) throws Exception {
        return routingMatrixProvider.getRoutingMatrix(toSet(route.stops()));
    }

    private static <T> Set<T> toSet(final List<T> ts) {
        return ts.stream().collect(Collectors.toUnmodifiableSet());
    }
}
