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
import java.util.Collection;
import java.util.List;

public class RouteOptimizer {

	public record Stop(String address, double lat, double lng) {
	}

	public static List<String> optimize(final double startLat, final double startLng, final List<Stop> stops) {
		final List<String> optimizedRoute = new ArrayList<>();
		if (stops.isEmpty()) {
			return optimizedRoute;
		}

		// Define Problem Builder
		final VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(
				VehicleImpl
						.Builder
						.newInstance("vehicle")
						.setStartLocation(Location.newInstance(startLng, startLat))
						.setType(
								VehicleTypeImpl
										.Builder
										.newInstance("car")
										.setCostPerDistance(1.0)
										.build())
						.setReturnToDepot(false)
						.build());

		// Define Transport Costs using Haversine
		vrpBuilder.setRoutingCost(
				new VehicleRoutingTransportCosts() {

					@Override
					public double getTransportTime(final Location from,
					                               final Location to,
					                               final double departureTime,
					                               final Driver driver,
					                               final Vehicle vehicle) {
						return getDistance(from, to, departureTime, vehicle);
					}

					@Override
					public double getBackwardTransportTime(final Location from,
					                                       final Location to,
					                                       double arrivalTime,
					                                       final Driver driver,
					                                       final Vehicle vehicle) {
						return getDistance(from, to, arrivalTime, vehicle);
					}

					@Override
					public double getBackwardTransportCost(final Location from,
					                                       final Location to,
					                                       final double arrivalTime,
					                                       final Driver driver,
					                                       final Vehicle vehicle) {
						return getDistance(from, to, arrivalTime, vehicle);
					}

					@Override
					public double getDistance(final Location from,
					                          final Location to,
					                          final double departureTime,
					                          final Vehicle vehicle) {
						if (from.getCoordinate() == null || to.getCoordinate() == null) {
							return 0.0;
						}
						// Note jsprit Coordinate is (x, y) -> (lng, lat)
						return calculateHaversineDistance(
								from.getCoordinate().getY(),
								from.getCoordinate().getX(),
								to.getCoordinate().getY(),
								to.getCoordinate().getX());
					}

					@Override
					public double getTransportCost(final Location from,
					                               final Location to,
					                               final double departureTime,
					                               final Driver driver,
					                               final Vehicle vehicle) {
						return getDistance(from, to, departureTime, vehicle);
					}

					/**
					 * Calculates the great-circle distance between two points on Earth using the Haversine formula.
					 * Returns distance in meters.
					 */
					private double calculateHaversineDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
						final double R = 6371000.0; // Earth radius in meters
						final double dLat = Math.toRadians(lat2 - lat1);
						final double dLon = Math.toRadians(lon2 - lon1);
						final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
								Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
										Math.sin(dLon / 2) * Math.sin(dLon / 2);
						final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
						return R * c;
					}
				});

		// Add Services (Stops)
		for (int i = 0; i < stops.size(); i++) {
			final Stop stop = stops.get(i);
			// We use address as ID. If duplicate addresses exist, we append index to make it unique
			final String jobId = stop.address + "___" + i;

			final Service service =
					Service
							.Builder
							.newInstance(jobId)
							// jsprit coordinate is (x=longitude, y=latitude)
							.setLocation(
									Location
											.Builder
											.newInstance()
											.setCoordinate(Coordinate.newInstance(stop.lng, stop.lat))
											.build())
							.build();
			vrpBuilder.addJob(service);
		}

		// Build Problem - Force finite fleet size to use only the vehicle we added
		final VehicleRoutingProblem problem = vrpBuilder
				.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
				.build();

		// Define and Run Algorithm
		final VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
		final Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

		// Extract Best Solution
		final VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

		if (bestSolution != null) {
			// Iterate over all routes (though we only expect one with finite fleet size)
			for (final VehicleRoute route : bestSolution.getRoutes()) {
				for (final TourActivity activity : route.getActivities()) {
					if (activity instanceof final TourActivity.JobActivity jobActivity) {
						final String rawId = jobActivity.getJob().getId();
						final String originalAddress = rawId.split("___")[0];
						optimizedRoute.add(originalAddress);
					}
				}
			}

			// Add unassigned jobs to ensure no stops are ever lost
			for (final Job job : bestSolution.getUnassignedJobs()) {
				final String rawId = job.getId();
				final String originalAddress = rawId.split("___")[0];
				optimizedRoute.add(originalAddress);
			}
		}

		// Final fallback: if result is still empty, return input order
		if (optimizedRoute.isEmpty()) {
			for (final Stop stop : stops) {
				optimizedRoute.add(stop.address);
			}
		}

		return optimizedRoute;
	}
}
