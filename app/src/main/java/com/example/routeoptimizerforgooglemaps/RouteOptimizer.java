package com.example.routeoptimizerforgooglemaps;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RouteOptimizer {

    public static class Stop {
        public final String address;
        public final double lat;
        public final double lng;

        public Stop(final String address, final double lat, final double lng) {
            this.address = address;
            this.lat = lat;
            this.lng = lng;
        }
    }

    /**
     * Calculates the great-circle distance between two points on Earth using the Haversine formula.
     * Returns distance in meters.
     */
    private static double calculateHaversineDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
        final double R = 6371000.0; // Earth radius in meters
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static List<String> optimize(final double startLat, final double startLng, final List<Stop> stops) {
        final List<String> optimizedRoute = new ArrayList<>();
        if (stops.isEmpty()) {
            return optimizedRoute;
        }

        // 1. Define Vehicle
        final VehicleType type = VehicleTypeImpl.Builder.newInstance("car").build();
        final VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
                .setStartLocation(com.graphhopper.jsprit.core.problem.Location.newInstance(startLng, startLat))
                .setType(type)
                .build();

        // 2. Define Problem Builder
        final VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);

        // 3. Define Transport Costs using Haversine
        vrpBuilder.setRoutingCost(new VehicleRoutingTransportCosts() {
            @Override
            public double getTransportTime(com.graphhopper.jsprit.core.problem.Location from, com.graphhopper.jsprit.core.problem.Location to, double departureTime, com.graphhopper.jsprit.core.problem.driver.Driver driver, com.graphhopper.jsprit.core.problem.vehicle.Vehicle vehicle) {
                return getDistance(from, to, departureTime, vehicle);
            }

            @Override
            public double getBackwardTransportTime(com.graphhopper.jsprit.core.problem.Location from, com.graphhopper.jsprit.core.problem.Location to, double arrivalTime, com.graphhopper.jsprit.core.problem.driver.Driver driver, com.graphhopper.jsprit.core.problem.vehicle.Vehicle vehicle) {
                return getDistance(from, to, arrivalTime, vehicle);
            }

            @Override
            public double getBackwardTransportCost(com.graphhopper.jsprit.core.problem.Location from, com.graphhopper.jsprit.core.problem.Location to, double arrivalTime, com.graphhopper.jsprit.core.problem.driver.Driver driver, com.graphhopper.jsprit.core.problem.vehicle.Vehicle vehicle) {
                return getDistance(from, to, arrivalTime, vehicle);
            }

            @Override
            public double getDistance(com.graphhopper.jsprit.core.problem.Location from, com.graphhopper.jsprit.core.problem.Location to, double departureTime, com.graphhopper.jsprit.core.problem.vehicle.Vehicle vehicle) {
                if (from.getCoordinate() == null || to.getCoordinate() == null) {
                    return 0.0;
                }
                // Note jsprit Coordinate is (x, y) -> (lng, lat)
                return calculateHaversineDistance(
                        from.getCoordinate().getY(), from.getCoordinate().getX(),
                        to.getCoordinate().getY(), to.getCoordinate().getX()
                );
            }

            @Override
            public double getTransportCost(com.graphhopper.jsprit.core.problem.Location from, com.graphhopper.jsprit.core.problem.Location to, double departureTime, com.graphhopper.jsprit.core.problem.driver.Driver driver, com.graphhopper.jsprit.core.problem.vehicle.Vehicle vehicle) {
                return getDistance(from, to, departureTime, vehicle);
            }
        });

        // 4. Add Services (Stops)
        for (int i = 0; i < stops.size(); i++) {
            final Stop stop = stops.get(i);
            // We use address as ID. If duplicate addresses exist, we append index to make it unique
            final String jobId = stop.address + "___" + i; 
            
            final Service service = Service.Builder.newInstance(jobId)
                    // jsprit coordinate is (x=longitude, y=latitude)
                    .setLocation(com.graphhopper.jsprit.core.problem.Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(stop.lng, stop.lat))
                            .build())
                    .build();
            vrpBuilder.addJob(service);
        }

        // 5. Build Problem
        final VehicleRoutingProblem problem = vrpBuilder.build();

        // 6. Define and Run Algorithm
        final VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        final Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        // 7. Extract Best Solution
        final VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        if (bestSolution != null && !bestSolution.getRoutes().isEmpty()) {
            final VehicleRoute route = bestSolution.getRoutes().iterator().next();
            for (final TourActivity activity : route.getActivities()) {
                if (activity instanceof com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity.JobActivity) {
                    final String rawId = ((com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity.JobActivity) activity).getJob().getId();
                    // Split the ID to get the original address string
                    final String originalAddress = rawId.split("___")[0];
                    optimizedRoute.add(originalAddress);
                }
            }
        } else {
            // Fallback if optimization fails to find a valid route
            for (final Stop stop : stops) {
                optimizedRoute.add(stop.address);
            }
        }

        // Handle unassigned jobs (should not happen in this simple MVP, but good practice)
        for (final com.graphhopper.jsprit.core.problem.job.Job job : bestSolution.getUnassignedJobs()) {
             final String rawId = job.getId();
             final String originalAddress = rawId.split("___")[0];
             optimizedRoute.add(originalAddress);
        }

        return optimizedRoute;
    }
}
