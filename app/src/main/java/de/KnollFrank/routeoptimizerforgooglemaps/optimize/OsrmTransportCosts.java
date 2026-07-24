package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

// =========================================================================================
// STRATEGIE 2: OSRM Implementierung (Straßennetz-Matrix)
// =========================================================================================
// FK-TODO: use com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix instead
class OsrmTransportCosts implements VehicleRoutingTransportCosts {

    private final RoutingMatrix routingMatrix;
    // FK-TODO: remove fallback
    // Eingebautes Fallback, falls bei einzelnen Koordinaten ein Fehler auftritt
    private final HaversineTransportCosts fallback = new HaversineTransportCosts();

    public OsrmTransportCosts(final RoutingMatrix routingMatrix) {
        this.routingMatrix = routingMatrix;
    }

    @Override
    public double getTransportTime(final Location from, final Location to, final double departureTime, final Driver driver, final Vehicle vehicle) {
        try {
            return getDistanceDuration(from, to).duration();
        } catch (final Exception e) {
            return fallback.getTransportTime(from, to, departureTime, driver, vehicle);
        }
    }

    @Override
    public double getBackwardTransportTime(final Location from, final Location to, double arrivalTime, final Driver driver, final Vehicle vehicle) {
        return getTransportTime(from, to, arrivalTime, driver, vehicle);
    }

    @Override
    public double getBackwardTransportCost(final Location from, final Location to, final double arrivalTime, final Driver driver, final Vehicle vehicle) {
        return getTransportCost(from, to, arrivalTime, driver, vehicle);
    }

    @Override
    public double getDistance(final Location from, final Location to, final double departureTime, final Vehicle vehicle) {
        try {
            return getDistanceDuration(from, to).distance();
        } catch (final Exception e) {
            return fallback.getDistance(from, to, departureTime, vehicle);
        }
    }

    @Override
    public double getTransportCost(final Location from, final Location to, final double departureTime, final Driver driver, final Vehicle vehicle) {
        // In Jsprit sind in diesem Fall die Transportkosten oft identisch mit der Distanz.
        // Du kannst hier bei Bedarf auch die Fahrzeit (getTransportTime) zurückgeben,
        // wenn Jsprit primär auf Zeit optimieren soll.
        return getDistance(from, to, departureTime, vehicle);
    }

    private DistanceDuration getDistanceDuration(final Location from, final Location to) {
        return routingMatrix
                .getDistanceDurationByStopIdTable()
                .get(from.getId(), to.getId());
    }
}
