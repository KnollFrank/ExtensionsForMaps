package com.example.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

        final List<String> optimized = RouteOptimizer.optimize(startLat, startLng, stops);

        assertFalse(optimized.isEmpty());
        assertEquals(3, optimized.size());
        
        // Expected order: Potsdam -> Leipzig -> Munich
        assertEquals("Potsdam", optimized.get(0));
        assertEquals("Leipzig", optimized.get(1));
        assertEquals("Munich", optimized.get(2));
    }

    @Test
    public void testOptimize_emptyList() {
        final List<String> optimized = RouteOptimizer.optimize(52.0, 13.0, new ArrayList<>());
        assertTrue(optimized.isEmpty());
    }
}
