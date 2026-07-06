package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

// FK-TODO: refactor
public class RouteOptimizer {

    private final VehicleRoutingTransportCostsProvider vehicleRoutingTransportCostsProvider;

    public RouteOptimizer(final VehicleRoutingTransportCostsProvider vehicleRoutingTransportCostsProvider) {
        this.vehicleRoutingTransportCostsProvider = vehicleRoutingTransportCostsProvider;
    }

    public Route optimize(final Route route) throws Exception {
        final Map<String, Stop> stopById = getStopById(route.waypoints());
        // FK-TODO: use Optional.ofNullable()
        final VehicleRoutingProblemSolution solution =
                Solutions.bestOf(
                        this
                                .createVehicleRoutingAlgorithm(route, stopById)
                                .searchSolutions());
        final List<Stop> optimizedWaypoints = new ArrayList<>();
        if (solution != null) {
            // ANFORDERUNG 1: Check ob alle Jobs zugewiesen wurden
            if (!solution.getUnassignedJobs().isEmpty()) {
                throw new IllegalStateException("Unassigned jobs:" + solution.getUnassignedJobs());
            }
            optimizedWaypoints.addAll(getOptimizedWaypoints(solution, stopById));
        }
        if (optimizedWaypoints.isEmpty()) {
            optimizedWaypoints.addAll(route.waypoints());
        }
        return new Route(
                route.origin(),
                optimizedWaypoints,
                route.destination());
    }

    private static List<Stop> getOptimizedWaypoints(final VehicleRoutingProblemSolution solution,
                                                    final Map<String, Stop> stopById) {
        // FK-TODO: refactor
        final List<Stop> optimizedRoute = new ArrayList<>();
        for (final VehicleRoute vehicleRoute : solution.getRoutes()) {
            for (final TourActivity activity : vehicleRoute.getActivities()) {
                if (activity instanceof final TourActivity.JobActivity jobActivity) {
                    final String rawId = jobActivity.getJob().getId();
                    final Stop originalStop = stopById.get(rawId);
                    if (originalStop != null) {
                        optimizedRoute.add(originalStop);
                    }
                }
            }
        }
        return optimizedRoute;
    }

    private VehicleRoutingAlgorithm createVehicleRoutingAlgorithm(
            final Route route,
            final Map<String, Stop> stopById) throws Exception {
        final VehicleRoutingProblem vehicleRoutingProblem = createVehicleRoutingProblem(route, stopById);
        final StateManager stateManager = new StateManager(vehicleRoutingProblem);
        return Jsprit
                .Builder
                .newInstance(vehicleRoutingProblem)
                .setStateAndConstraintManager(
                        stateManager,
                        // ANFORDERUNG 4: Soft-Constraint für Zonen (Zeitfenster haben Vorrang durch native jsprit-Implementierung)
                        new ConstraintManager(
                                vehicleRoutingProblem,
                                stateManager,
                                List.of(new FlexibleGroupConstraint())))
                .buildAlgorithm();
    }

    private static Map<String, Stop> getStopById(final List<Stop> stops) {
        return stops
                .stream()
                .collect(
                        Collectors.toUnmodifiableMap(
                                Stop::id,
                                Function.identity()));
    }

    private VehicleRoutingProblem createVehicleRoutingProblem(
            final Route route,
            final Map<String, Stop> stopById) throws Exception {
        final VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        // ANFORDERUNG 1: Eiserne Zustellungs-Garantie wird am Ende durch unassignedJobs Check erzwungen.
        return vrpBuilder
                .addVehicle(createVehicle(route))
                .setRoutingCost(vehicleRoutingTransportCostsProvider.getVehicleRoutingTransportCosts(route))
                .addAllJobs(createServices(stopById))
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .build();
    }

    private static VehicleImpl createVehicle(final Route route) {
        return VehicleImpl
                .Builder
                .newInstance("vehicle")
                .setType(
                        VehicleTypeImpl
                                .Builder
                                .newInstance("car")
                                .setCostPerDistance(1.0)
                                .build())
                .setStartLocation(createLocation(route.origin()))
                .setEndLocation(createLocation(route.destination()))
                .setReturnToDepot(true)
                .build();
    }

    private static List<Service> createServices(final Map<String, Stop> stopById) {
        return stopById
                .entrySet()
                .stream()
                .map(jobIdWaypointEntry -> createService(jobIdWaypointEntry.getValue(), jobIdWaypointEntry.getKey()))
                .toList();
    }

    private static Service createService(final Stop stop, final String jobId) {
        return Service
                .Builder
                .newInstance(jobId)
                .setLocation(createLocation(stop))
                // ANFORDERUNG 3: DeliveryGroup in UserData speichern
                .setUserData(stop.deliveryGroup())
                .build();
    }

    private static Location createLocation(final Stop stop) {
        return Location
                .Builder
                .newInstance()
                .setId(stop.id())
                .setCoordinate(getCoordinate(stop.geodetic()))
                .build();
    }

    private static Coordinate getCoordinate(final Geodetic geodetic) {
        return Coordinate.newInstance(
                geodetic.getLongitude().toDegrees(),
                geodetic.getLatitude().toDegrees());
    }
}
