package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

// FK-TODO: refactor
public class RouteOptimizer {

    private final RoutingMatricesProvider routingMatricesProvider;

    public RouteOptimizer(final RoutingMatricesProvider routingMatricesProvider) {
        this.routingMatricesProvider = routingMatricesProvider;
    }

    // FK-TODO: geschätzte Ersparnis in km und Zeit berechnen und anzeigen
    // FK-TODO: Routen optimieren für Auto, Fußgänger, Fahrrad und öffentliche Verkehrsmittel
    public List<Stop> optimizeStops(final Geodetic start,
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
                        .setCoordinate(getCoordinate(start))
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
                            new OsrmTransportCosts(routingMatricesProvider.getRoutingMatrices(start, stops));
                    case HAVERSINE -> new HaversineTransportCosts();
                };

        vrpBuilder.setRoutingCost(transportCosts);
        // ---------------------------------------------------------

        // Stopps definieren (IDs "1", "2", "3" usw. für die Matrix)
        for (int i = 0; i < stops.size(); i++) {
            final Stop stop = stops.get(i);
            final String jobId = stop.address() + "___" + i;
            stopMap.put(jobId, stop);
            final Location stopLocation =
                    Location
                            .Builder
                            .newInstance()
                            .setId(String.valueOf(i + 1))
                            .setCoordinate(getCoordinate(stop.geodetic()))
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

    private static Coordinate getCoordinate(final Geodetic geodetic) {
        return Coordinate.newInstance(
                geodetic.getLongitude().toDegrees(),
                geodetic.getLatitude().toDegrees());
    }
}