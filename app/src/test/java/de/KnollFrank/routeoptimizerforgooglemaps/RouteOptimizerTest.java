package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(RobolectricTestRunner.class)
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

	@Test
	public void testOptimize_osrmVsHaversine_LakeGarda() {
		// Geografisches Hindernis: Der Gardasee (Lago di Garda) in Italien
		// Start: Limone sul Garda (am Westufer des Sees)
		final double startLat = 45.8156;
		final double startLng = 10.7904;

		final List<RouteOptimizer.Stop> stops = new ArrayList<>();

		// Stop A: Malcesine
		// (Liegt am Ostufer, genau gegenüber von Limone. Luftlinie: ~6 km. Auto: ~30 km, da man um den See fahren muss)
		stops.add(new RouteOptimizer.Stop("Malcesine", 45.7656, 10.8092));

		// Stop B: Riva del Garda
		// (Liegt an der Nordspitze. Luftlinie: ~9 km. Auto: ~11 km, da auf derselben Uferseite über direkte Straße erreichbar)
		stops.add(new RouteOptimizer.Stop("Riva del Garda", 45.8893, 10.8430));

		// ==========================================================
		// TEST 1: Haversine-Strategie (Luftlinie ignoriert den See)
		// Erwartung: Limone -> Malcesine (6km) -> Riva del Garda
		// ==========================================================
		final List<RouteOptimizer.Stop> haversineRoute =
				RouteOptimizer.optimize(startLat, startLng, stops, RouteOptimizer.OptimizationStrategy.HAVERSINE);

		assertEquals(2, haversineRoute.size());
		assertEquals("Malcesine", haversineRoute.get(0).address());
		assertEquals("Riva del Garda", haversineRoute.get(1).address());

		// ==========================================================
		// TEST 2: OSRM-Strategie (Echte Straßenführung)
		// Erwartung: Limone -> Riva del Garda (11km) -> Malcesine (weitere 19km)
		// ==========================================================
		final List<RouteOptimizer.Stop> osrmRoute =
				RouteOptimizer.optimize(startLat, startLng, stops, RouteOptimizer.OptimizationStrategy.OSRM);

		assertEquals(2, osrmRoute.size());
		assertEquals("Riva del Garda", osrmRoute.get(0).address()); // OSRM erkennt, dass Riva näher zu befahren ist!
		assertEquals("Malcesine", osrmRoute.get(1).address());
	}
}