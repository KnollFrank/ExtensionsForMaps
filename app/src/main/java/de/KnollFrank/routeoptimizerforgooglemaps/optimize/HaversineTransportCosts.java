package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

// =========================================================================================
// STRATEGIE 1: Haversine Implementierung (Luftlinie)
// =========================================================================================
// FK-TODO: braucht man für relativ kurze Strecken (z.B. innerhalb Deutschlands) eine so präzise Formel, die die Erdkrümmung berücksichtigt, oder geht das auch einfacher planar?
class HaversineTransportCosts implements VehicleRoutingTransportCosts {

    @Override
    public double getTransportTime(final Location from, final Location to, final double departureTime, final Driver driver, final Vehicle vehicle) {
        return getDistance(from, to, departureTime, vehicle);
    }

    @Override
    public double getBackwardTransportTime(final Location from, final Location to, double arrivalTime, final Driver driver, final Vehicle vehicle) {
        return getDistance(from, to, arrivalTime, vehicle);
    }

    @Override
    public double getBackwardTransportCost(final Location from, final Location to, final double arrivalTime, final Driver driver, final Vehicle vehicle) {
        return getDistance(from, to, arrivalTime, vehicle);
    }

    @Override
    public double getDistance(final Location from, final Location to, final double departureTime, final Vehicle vehicle) {
        if (from.getCoordinate() == null || to.getCoordinate() == null) {
            return 0.0;
        }
        return calculateHaversineDistance(
                from.getCoordinate().getY(),
                from.getCoordinate().getX(),
                to.getCoordinate().getY(),
                to.getCoordinate().getX());
    }

    @Override
    public double getTransportCost(final Location from, final Location to, final double departureTime, final Driver driver, final Vehicle vehicle) {
        return getDistance(from, to, departureTime, vehicle);
    }

    private double calculateHaversineDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
        final double R = 6371000.0;
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
