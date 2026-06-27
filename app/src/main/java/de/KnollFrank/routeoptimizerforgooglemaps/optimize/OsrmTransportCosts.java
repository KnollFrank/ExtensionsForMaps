package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

// =========================================================================================
// STRATEGIE 2: OSRM Implementierung (Straßennetz-Matrix)
// =========================================================================================
class OsrmTransportCosts implements VehicleRoutingTransportCosts {

    private final RoutingMatrices matrices;
    // Eingebautes Fallback, falls bei einzelnen Koordinaten ein Fehler auftritt
    private final HaversineTransportCosts fallback = new HaversineTransportCosts();

    public OsrmTransportCosts(final RoutingMatrices matrices) {
        this.matrices = matrices;
    }

    @Override
    public double getTransportTime(final Location from, final Location to, final double departureTime, final Driver driver, final Vehicle vehicle) {
        try {
            final int fromIdx = Integer.parseInt(from.getId());
            final int toIdx = Integer.parseInt(to.getId());
            return matrices.durations()[fromIdx][toIdx];
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
            final int fromIdx = Integer.parseInt(from.getId());
            final int toIdx = Integer.parseInt(to.getId());
            return matrices.distances()[fromIdx][toIdx];
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
}
