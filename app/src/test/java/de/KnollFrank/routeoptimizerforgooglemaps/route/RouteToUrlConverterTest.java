package de.KnollFrank.routeoptimizerforgooglemaps.route;

import static org.junit.Assert.assertEquals;
import static de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit.DEGREES;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.URL;
import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

@RunWith(RobolectricTestRunner.class)
public class RouteToUrlConverterTest {

    @Test
    public void testGetUrl_OriginAndDestination() {
        // Given
        final Route route =
                new Route(
                        new Stop(
                                "1",
                                "Origin",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.50161543647, DEGREES),
                                        new Angle(8.993018526571907, DEGREES))),
                        List.of(),
                        new Stop(
                                "2",
                                "Destination",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.476548982116846, DEGREES),
                                        new Angle(8.934905787678979, DEGREES))));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/Origin/Destination/data=!3m2!1e3!4b1!4m9!4m8!1m3!2m2!1d8.9930185!2d48.5016154!1m3!2m2!1d8.9349058!2d48.476549?entry=ttu",
                result.toString());
    }

    @Test
    public void testGetUrl_WithWaypoints() {
        // Given
        final Route route =
                new Route(
                        new Stop(
                                "1",
                                "Origin",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES))),
                        List.of(
                                new Stop(
                                        "2",
                                        "Waypoint",
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES)))),
                        new Stop(
                                "3",
                                "Destination",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES))));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/Origin/Waypoint/Destination/data=!3m2!1e3!4b1!4m13!4m12!1m3!2m2!1d20!2d10!1m3!2m2!1d25!2d15!1m3!2m2!1d40!2d30?entry=ttu",
                result.toString());
    }

    @Test
    public void testGetUrl_WithMultipleWaypoints() {
        // Given
        final Route route =
                new Route(
                        new Stop(
                                "1",
                                "Origin",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES))),
                        List.of(
                                new Stop(
                                        "2",
                                        "W1",
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES))),
                                new Stop(
                                        "3",
                                        "W2",
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(20.0, DEGREES),
                                                new Angle(30.0, DEGREES)))),
                        new Stop(
                                "4",
                                "Destination",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES))));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/Origin/W1/W2/Destination/data=!3m2!1e3!4b1!4m17!4m16!1m3!2m2!1d20!2d10!1m3!2m2!1d25!2d15!1m3!2m2!1d30!2d20!1m3!2m2!1d40!2d30?entry=ttu",
                result.toString());
    }

    @Test
    public void testGetUrl_WithManyStops() {
        // Given: Route with 12 stops (origin, destination, 10 waypoints)
        // One waypoint has a Place ID.
        final Route route =
                new Route(
                        new Stop(
                                "0",
                                "Origin",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.5, DEGREES),
                                        new Angle(9.0, DEGREES))),
                        Lists
                                .createRange(1, 10)
                                .stream()
                                .map(RouteToUrlConverterTest::createWaypoint)
                                .toList(),
                        new Stop(
                                "11",
                                "Destination",
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.6, DEGREES),
                                        new Angle(9.1, DEGREES))));

        // When
        final URL url = RouteToUrlConverter.getUrl(route);

        // Then
        // Verifiziere den Round-Trip (lossless extraction)
        assertEquals(route, GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url));
    }

    private static Stop createWaypoint(final int i) {
        return new Stop(
                String.valueOf(i),
                "W" + i,
                Geodetic.fromLatitudeLongitude(
                        new Angle(48.5 + i * 0.01, DEGREES),
                        new Angle(9.0 + i * 0.01, DEGREES)));
    }
}
