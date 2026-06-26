package de.KnollFrank.routeoptimizerforgooglemaps;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class RouteOptimizer {

    private final RoutingMatricesProvider routingMatricesProvider;

    public RouteOptimizer(final RoutingMatricesProvider routingMatricesProvider) {
        this.routingMatricesProvider = routingMatricesProvider;
    }

    // FK-FEATURE: biete neben der HaversineDistance auch folgendes an:
    //  + Embedded GraphHopper + GitHub-Download
    //  - OpenRouteService-API (https://openrouteservice.org/)
    //  - https://locationiq.com/
    public enum OptimizationStrategy {
        HAVERSINE,
        OSRM
    }

    // FK-TODO: use Labyrinth:org.labyrinth.coordinate.Geodetic instead of lat/lng at all places in this app
    public record Stop(String address, double lat, double lng) {
    }

    // Interner Container für die OSRM Distanz- und Dauer-Matrizen
    public record RoutingMatrices(double[][] distances, double[][] durations) {

        @Override
        public String toString() {
            return new StringJoiner(", ", RoutingMatrices.class.getSimpleName() + "[", "]")
                    .add("distances=" + Arrays.deepToString(distances))
                    .add("durations=" + Arrays.deepToString(durations))
                    .toString();
        }
    }

    // FK-TODO: geschätzte Ersparnis in km und Zeit berechnen und anzeigen
    // FK-TODO: Routen optimieren für Auto, Fußgänger, Fahrrad und öffentliche Verkehrsmittel
    public List<Stop> optimize(final double startLat,
                               final double startLng,
                               final List<Stop> stops,
                               final OptimizationStrategy strategy) throws Exception {
        final List<Stop> optimizedRoute = new ArrayList<>();
        if (stops.isEmpty()) {
            return optimizedRoute;
        }

        final Map<String, Stop> stopMap = new HashMap<>();
        final VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        // Start-Location definieren (ID "0" für den OSRM Matrix-Index)
        final Location startLocation =
                Location
                        .Builder
                        .newInstance()
                        .setId("0")
                        .setCoordinate(Coordinate.newInstance(startLng, startLat))
                        .build();
        vrpBuilder.addVehicle(
                VehicleImpl
                        .Builder
                        .newInstance("vehicle")
                        .setStartLocation(startLocation)
                        .setType(
                                VehicleTypeImpl
                                        .Builder
                                        .newInstance("car")
                                        .setCostPerDistance(1.0)
                                        .build())
                        .setReturnToDepot(false)
                        .build());
        // ---------------------------------------------------------
        // STRATEGY PATTERN: Auswahl der korrekten Kosten-Implementierung
        // ---------------------------------------------------------
        final VehicleRoutingTransportCosts transportCosts =
                switch (strategy) {
                    case OSRM ->
                            new OsrmTransportCosts(routingMatricesProvider.getRoutingMatrices(startLat, startLng, stops));
                    case HAVERSINE -> new HaversineTransportCosts();
                };

        vrpBuilder.setRoutingCost(transportCosts);
        // ---------------------------------------------------------

        // Stopps definieren (IDs "1", "2", "3" usw. für die Matrix)
        for (int i = 0; i < stops.size(); i++) {
            final Stop stop = stops.get(i);
            final String jobId = stop.address + "___" + i;
            stopMap.put(jobId, stop);

            final Location stopLocation = Location.Builder.newInstance()
                    .setId(String.valueOf(i + 1))
                    .setCoordinate(Coordinate.newInstance(stop.lng, stop.lat))
                    .build();

            final Service service =
                    Service
                            .Builder
                            .newInstance(jobId)
                            .setLocation(stopLocation)
                            .build();
            vrpBuilder.addJob(service);
        }

        final VehicleRoutingProblem problem =
                vrpBuilder
                        .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                        .build();

        final VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        final Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        final VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        if (bestSolution != null) {
            for (final VehicleRoute route : bestSolution.getRoutes()) {
                for (final TourActivity activity : route.getActivities()) {
                    if (activity instanceof final TourActivity.JobActivity jobActivity) {
                        final String rawId = jobActivity.getJob().getId();
                        final Stop originalStop = stopMap.get(rawId);
                        if (originalStop != null) {
                            optimizedRoute.add(originalStop);
                        }
                    }
                }
            }
            for (final Job job : bestSolution.getUnassignedJobs()) {
                final Stop originalStop = stopMap.get(job.getId());
                if (originalStop != null && !optimizedRoute.contains(originalStop)) {
                    optimizedRoute.add(originalStop);
                }
            }
        }
        if (optimizedRoute.isEmpty()) {
            optimizedRoute.addAll(stops);
        }
        return optimizedRoute;
    }

    // =========================================================================================
    // STRATEGIE 1: Haversine Implementierung (Luftlinie)
    // =========================================================================================
    private static class HaversineTransportCosts implements VehicleRoutingTransportCosts {

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

    // =========================================================================================
    // STRATEGIE 2: OSRM Implementierung (Straßennetz-Matrix)
    // =========================================================================================
    private static class OsrmTransportCosts implements VehicleRoutingTransportCosts {

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
}