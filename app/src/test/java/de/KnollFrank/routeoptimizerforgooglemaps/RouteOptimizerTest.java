package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RouteOptimizerTest {

	@Test
	public void testOptimize_sortsByShortestDistance() {
		// Start: Berlin (52.5200, 13.4050)
		final double startLat = 52.5200;
		final double startLng = 13.4050;

		final List<RouteOptimizer.Stop> stops = new ArrayList<>();
		// Stop A: Munich (far)
		stops.add(new RouteOptimizer.Stop("Munich", 48.1351, 11.5820));
		// Stop B: Potsdam (very close)
		stops.add(new RouteOptimizer.Stop("Potsdam", 52.3906, 13.0645));
		// Stop C: Leipzig (medium)
		stops.add(new RouteOptimizer.Stop("Leipzig", 51.3397, 12.3731));

		final List<RouteOptimizer.Stop> optimized = RouteOptimizer.optimize(startLat, startLng, stops);

		assertFalse(optimized.isEmpty());
		assertEquals(3, optimized.size());

		// Expected order: Potsdam -> Leipzig -> Munich
		List<String> addresses = optimized.stream().map(RouteOptimizer.Stop::address).collect(Collectors.toList());
		assertEquals(List.of("Potsdam", "Leipzig", "Munich"), addresses);
	}

	@Test
	public void testOptimize_userBugReproduction_Exact() {
		// Exact coordinates from user URL
		double startLat = 48.4765345;
		double startLng = 8.9349008;

		List<RouteOptimizer.Stop> stops = new ArrayList<>();
		// Hamburg
		stops.add(new RouteOptimizer.Stop("Hamburg", 53.548828, 9.987170));
		// Unterhausen
		stops.add(new RouteOptimizer.Stop("Unterhausen", 48.430628, 9.2546378));

		List<RouteOptimizer.Stop> optimized = RouteOptimizer.optimize(startLat, startLng, stops);

		// Expect both intermediate stops to be present
		assertEquals(2, optimized.size());
		// Verify names
		List<String> addresses = optimized.stream().map(RouteOptimizer.Stop::address).collect(Collectors.toList());
		assertTrue(addresses.contains("Hamburg"));
		assertTrue(addresses.contains("Unterhausen"));
	}
}
