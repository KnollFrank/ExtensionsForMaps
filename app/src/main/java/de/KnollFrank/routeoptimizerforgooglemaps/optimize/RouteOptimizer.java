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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        final List<Stop> optimizedRoute = new ArrayList<>();
        final Map<String, Stop> stopMap = new HashMap<>();
        final VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        // ANFORDERUNG 1: Eiserne Zustellungs-Garantie wird am Ende durch unassignedJobs Check erzwungen.
        vrpBuilder.addVehicle(
                VehicleImpl
                        .Builder
                        .newInstance("vehicle")
                        .setStartLocation(createLocation(route.origin()))
                        .setEndLocation(createLocation(route.destination()))
                        .setType(
                                VehicleTypeImpl
                                        .Builder
                                        .newInstance("car")
                                        .setCostPerDistance(1.0)
                                        .build())
                        .setReturnToDepot(true)
                        .setEarliestStart(0.0)
                        .setLatestArrival(Double.MAX_VALUE)
                        .build());
        vrpBuilder.setRoutingCost(vehicleRoutingTransportCostsProvider.getVehicleRoutingTransportCosts(route));
        // FK-TODO: refactor using Streams
        for (final Stop waypoint : route.waypoints()) {
            final String jobId = waypoint.id();
            stopMap.put(jobId, waypoint);

            // ANFORDERUNG 2: Harte Zeitfenster
            vrpBuilder.addJob(
                    Service
                            .Builder
                            .newInstance(jobId)
                            .setLocation(createLocation(waypoint))
                            .addTimeWindow(
                                    JspritTimeUtils.toJspritWindow(
                                            waypoint.startWindow(),
                                            waypoint.endWindow()))
                            // ANFORDERUNG 3: Gruppen-ID im Priority-Feld
                            .setPriority(waypoint.deliveryGroup().sequenceOrder())
                            .build());
        }
        final VehicleRoutingProblem vehicleRoutingProblem =
                vrpBuilder
                        .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                        .build();
        final StateManager stateManager = new StateManager(vehicleRoutingProblem);
        // ANFORDERUNG 4: Soft-Constraint für Zonen (Zeitfenster haben Vorrang durch native jsprit-Implementierung)
        final ConstraintManager constraintManager =
                new ConstraintManager(
                        vehicleRoutingProblem,
                        stateManager,
                        List.of(new FlexibleGroupConstraint()));
        final VehicleRoutingAlgorithm algorithm =
                Jsprit
                        .Builder
                        .newInstance(vehicleRoutingProblem)
                        .setStateAndConstraintManager(stateManager, constraintManager)
                        .buildAlgorithm();
        final Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        // FK-TODO: use Optional.ofNullable()
        final VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        if (bestSolution != null) {
            // ANFORDERUNG 1: Check ob alle Jobs zugewiesen wurden
            if (!bestSolution.getUnassignedJobs().isEmpty()) {
                throw new IllegalStateException("Unassigned jobs:" + bestSolution.getUnassignedJobs());
            }

            for (final VehicleRoute vehicleRoute : bestSolution.getRoutes()) {
                for (final TourActivity activity : vehicleRoute.getActivities()) {
                    if (activity instanceof final TourActivity.JobActivity jobActivity) {
                        final String rawId = jobActivity.getJob().getId();
                        final Stop originalStop = stopMap.get(rawId);
                        if (originalStop != null) {
                            optimizedRoute.add(originalStop);
                        }
                    }
                }
            }
        }
        if (optimizedRoute.isEmpty()) {
            optimizedRoute.addAll(route.waypoints());
        }
        return new Route(
                route.origin(),
                optimizedRoute,
                route.destination());
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
