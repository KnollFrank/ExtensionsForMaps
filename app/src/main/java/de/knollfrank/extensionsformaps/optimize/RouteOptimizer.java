package de.knollfrank.extensionsformaps.optimize;

import com.google.common.collect.ImmutableMap;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.knollfrank.extensionsformaps.coordinate.Geodetic;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

public class RouteOptimizer {

    private final VehicleRoutingTransportCostsProvider vehicleRoutingTransportCostsProvider;

    public RouteOptimizer(final VehicleRoutingTransportCostsProvider vehicleRoutingTransportCostsProvider) {
        this.vehicleRoutingTransportCostsProvider = vehicleRoutingTransportCostsProvider;
    }

    public Route optimize(final Route route) throws Exception {
        return optimize(route, OptimizationType.FIXED_DESTINATION);
    }

    public Route optimize(final Route route, final OptimizationType optimizationType) throws Exception {
        return optimize(route, optimizationType, null);
    }

    public Route optimize(final Route route,
                          final OptimizationType optimizationType,
                          final Consumer<Integer> progressListener) throws Exception {
        return optimize(route, optimizationType, progressListener, () -> false);
    }

    public Route optimize(final Route route,
                          final OptimizationType optimizationType,
                          final Consumer<Integer> progressListener,
                          final Supplier<Boolean> isCanceled) throws Exception {
        final List<Stop> allStops = new ArrayList<>(route.waypoints());
        if (optimizationType == OptimizationType.ANY_DESTINATION) {
            allStops.add(route.destination());
        }

        final ImmutableMap<String, Stop> stopById = getStopById(allStops);
        final VehicleRoutingAlgorithm algorithm = createVehicleRoutingAlgorithm(route, stopById, optimizationType);

        algorithm.addTerminationCriterion(discoveredSolution -> isCanceled.get());

        if (progressListener != null) {
            final int maxIterations = algorithm.getMaxIterations();
            algorithm.addListener(
                    new IterationEndsListener() {

                        private int lastReportedProgress = -1;

                        @Override
                        public void informIterationEnds(final int i,
                                                        final VehicleRoutingProblem problem,
                                                        final Collection<VehicleRoutingProblemSolution> solutions) {
                            int progress = (int) ((i / (float) maxIterations) * 100);
                            if (progress != lastReportedProgress) {
                                progressListener.accept(progress);
                                lastReportedProgress = progress;
                            }
                        }
                    });
        }

        final VehicleRoutingProblemSolution bestSolution =
                RouteOptimizer
                        .getBestSolution(algorithm.searchSolutions())
                        .orElseThrow(() -> new IllegalStateException("No solution found"));

        if (isCanceled.get()) {
            throw new InterruptedException("Optimization canceled");
        }

        final List<Stop> optimizedStops = getStops(bestSolution, stopById);

        if (optimizationType == OptimizationType.ANY_DESTINATION) {
            final Stop newDestination = optimizedStops.get(optimizedStops.size() - 1);
            final List<Stop> newWaypoints = new ArrayList<>(optimizedStops);
            newWaypoints.remove(newWaypoints.size() - 1);
            return new Route(route.origin(), newWaypoints, newDestination);
        } else {
            return new Route(route.origin(), optimizedStops, route.destination());
        }
    }

    private static ImmutableMap<String, Stop> getStopById(final List<Stop> stops) {
        return stops
                .stream()
                .collect(
                        ImmutableMap.toImmutableMap(
                                Stop::id,
                                Function.identity()));
    }

    private VehicleRoutingAlgorithm createVehicleRoutingAlgorithm(
            final Route route,
            final ImmutableMap<String, Stop> stopById,
            final OptimizationType optimizationType) throws Exception {
        final VehicleRoutingProblem vehicleRoutingProblem = createVehicleRoutingProblem(route, stopById, optimizationType);
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

    private VehicleRoutingProblem createVehicleRoutingProblem(
            final Route route,
            final ImmutableMap<String, Stop> stopById,
            final OptimizationType optimizationType) throws Exception {
        final VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        // ANFORDERUNG 1: Eiserne Zustellungs-Garantie wird am Ende durch unassignedJobs Check erzwungen.
        return vrpBuilder
                .addVehicle(createVehicle(route, optimizationType))
                .setRoutingCost(vehicleRoutingTransportCostsProvider.getVehicleRoutingTransportCosts(route))
                .addAllJobs(createServices(stopById))
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .build();
    }

    private static VehicleImpl createVehicle(final Route route, final OptimizationType optimizationType) {
        final VehicleImpl.Builder vehicleBuilder =
                VehicleImpl
                        .Builder
                        .newInstance("vehicle")
                        .setType(
                                VehicleTypeImpl
                                        .Builder
                                        .newInstance("car")
                                        .setCostPerDistance(1.0)
                                        .build())
                        .setStartLocation(createLocation(route.origin()));

        if (optimizationType == OptimizationType.FIXED_DESTINATION) {
            vehicleBuilder
                    .setEndLocation(createLocation(route.destination()))
                    .setReturnToDepot(true);
        } else {
            vehicleBuilder.setReturnToDepot(false);
        }

        return vehicleBuilder.build();
    }

    private static List<Service> createServices(final ImmutableMap<String, Stop> stopById) {
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

    private static Optional<VehicleRoutingProblemSolution> getBestSolution(final Collection<VehicleRoutingProblemSolution> solutions) {
        final var bestSolution = Optional.ofNullable(Solutions.bestOf(solutions));
        bestSolution.ifPresent(RouteOptimizer::assertHasNoUnassignedJobs);
        return bestSolution;
    }

    // ANFORDERUNG 1: Check ob alle Jobs zugewiesen wurden
    private static void assertHasNoUnassignedJobs(final VehicleRoutingProblemSolution solution) {
        if (!solution.getUnassignedJobs().isEmpty()) {
            throw new IllegalStateException("UNASSIGNED_JOBS:" + solution.getUnassignedJobs());
        }
    }

    private static List<Stop> getStops(final VehicleRoutingProblemSolution solution,
                                       final ImmutableMap<String, Stop> stopById) {
        // FK-TODO: refactor
        final List<Stop> waypoints = new ArrayList<>();
        for (final VehicleRoute vehicleRoute : solution.getRoutes()) {
            for (final TourActivity activity : vehicleRoute.getActivities()) {
                if (activity instanceof final TourActivity.JobActivity jobActivity) {
                    final String rawId = jobActivity.getJob().getId();
                    final Stop originalStop = stopById.get(rawId);
                    if (originalStop != null) {
                        waypoints.add(originalStop);
                    }
                }
            }
        }
        return waypoints;
    }
}
