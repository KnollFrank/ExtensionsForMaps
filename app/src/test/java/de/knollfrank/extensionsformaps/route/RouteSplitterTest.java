package de.knollfrank.extensionsformaps.route;

import static org.junit.Assert.assertEquals;
import static de.knollfrank.extensionsformaps.coordinate.Unit.DEGREES;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;

public class RouteSplitterTest {

    @Test
    public void testRouteWithLessThan10Stops() {
        final Route route = createTestRoute(5);
        final List<Route> blocks = RouteSplitter.splitInto10StopBlocks(route);

        assertEquals(1, blocks.size());
        assertEquals(5, blocks.get(0).stops().size());
        assertEquals("Stop 0", blocks.get(0).origin().address());
        assertEquals("Stop 4", blocks.get(0).destination().address());
    }

    @Test
    public void testRouteWithExactly10Stops() {
        final Route route = createTestRoute(10);
        final List<Route> blocks = RouteSplitter.splitInto10StopBlocks(route);

        assertEquals(1, blocks.size());
        assertEquals(10, blocks.get(0).stops().size());
        assertEquals("Stop 0", blocks.get(0).origin().address());
        assertEquals("Stop 9", blocks.get(0).destination().address());
    }

    @Test
    public void testRouteWith15Stops() {
        final Route route = createTestRoute(15);
        final List<Route> blocks = RouteSplitter.splitInto10StopBlocks(route);

        assertEquals(2, blocks.size());

        // Block 1: stops 0 to 9 (10 stops)
        final Route block1 = blocks.get(0);
        assertEquals(10, block1.stops().size());
        assertEquals("Stop 0", block1.origin().address());
        assertEquals("Stop 9", block1.destination().address());

        // Block 2: stops 9 to 14 (6 stops)
        final Route block2 = blocks.get(1);
        assertEquals(6, block2.stops().size());
        assertEquals("Stop 9", block2.origin().address());
        assertEquals("Stop 14", block2.destination().address());
    }

    @Test
    public void testRouteWith28Stops() {
        final Route route = createTestRoute(28);
        final List<Route> blocks = RouteSplitter.splitInto10StopBlocks(route);

        assertEquals(3, blocks.size());

        // Block 1: stops 0 to 9 (10 stops)
        assertEquals(10, blocks.get(0).stops().size());
        assertEquals("Stop 0", blocks.get(0).origin().address());
        assertEquals("Stop 9", blocks.get(0).destination().address());

        // Block 2: stops 9 to 18 (10 stops)
        assertEquals(10, blocks.get(1).stops().size());
        assertEquals("Stop 9", blocks.get(1).origin().address());
        assertEquals("Stop 18", blocks.get(1).destination().address());

        // Block 3: stops 18 to 27 (10 stops)
        assertEquals(10, blocks.get(2).stops().size());
        assertEquals("Stop 18", blocks.get(2).origin().address());
        assertEquals("Stop 27", blocks.get(2).destination().address());
    }

    private Route createTestRoute(final int numStops) {
        final List<Stop> stops = new ArrayList<>();
        for (int i = 0; i < numStops; i++) {
            stops.add(new Stop(
                    "" + i,
                    "Stop " + i,
                    Optional.empty(),
                    Geodetic.fromLatitudeLongitude(
                            new Angle(10.0 + i * 0.1, DEGREES),
                            new Angle(20.0 + i * 0.1, DEGREES)),
                    Optional.empty()));
        }
        final Stop origin = stops.get(0);
        final List<Stop> waypoints = stops.subList(1, stops.size() - 1);
        final Stop destination = stops.get(stops.size() - 1);
        return new Route(origin, waypoints, destination);
    }
}
