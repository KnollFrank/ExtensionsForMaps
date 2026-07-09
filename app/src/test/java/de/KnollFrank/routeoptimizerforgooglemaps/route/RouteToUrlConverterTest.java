package de.KnollFrank.routeoptimizerforgooglemaps.route;

import static org.junit.Assert.assertEquals;
import static de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit.DEGREES;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.URL;
import java.util.List;
import java.util.Optional;

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
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.50161543647, DEGREES),
                                        new Angle(8.993018526571907, DEGREES)),
                                Optional.empty()),
                        List.of(),
                        new Stop(
                                "2",
                                "Destination",
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.476548982116846, DEGREES),
                                        new Angle(8.934905787678979, DEGREES)),
                                Optional.empty()));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=48.5016154%2C8.9930185&destination=48.476549%2C8.9349058",
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
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES)),
                                Optional.empty()),
                        List.of(
                                new Stop(
                                        "2",
                                        "Waypoint",
                                        Optional.empty(),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES)),
                                        Optional.empty())),
                        new Stop(
                                "3",
                                "Destination",
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES)),
                                Optional.empty()));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=10%2C20&destination=30%2C40&waypoints=15%2C25",
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
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES)),
                                Optional.empty()),
                        List.of(
                                new Stop(
                                        "2",
                                        "W1",
                                        Optional.empty(),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES)),
                                        Optional.empty()),
                                new Stop(
                                        "3",
                                        "W2",
                                        Optional.empty(),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(20.0, DEGREES),
                                                new Angle(30.0, DEGREES)),
                                        Optional.empty())),
                        new Stop(
                                "4",
                                "Destination",
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES)),
                                Optional.empty()));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=10%2C20&destination=30%2C40&waypoints=15%2C25%7C20%2C30",
                result.toString());
    }

    @Test
    public void testGetUrl_WithPlaceIds() {
        // Given
        final Route route =
                new Route(
                        new Stop(
                                "1",
                                "Origin City",
                                Optional.of("place1"),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES)),
                                Optional.empty()),
                        List.of(
                                new Stop(
                                        "2",
                                        "Waypoint Street",
                                        Optional.of("place2"),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES)),
                                        Optional.empty())),
                        new Stop(
                                "3",
                                "Destination Landmark",
                                Optional.of("place3"),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES)),
                                Optional.empty()));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=Origin%20City&origin_place_id=place1&destination=Destination%20Landmark&destination_place_id=place3&waypoints=Waypoint%20Street&waypoint_place_ids=place2",
                result.toString());
    }

    @Test
    public void testGetUrl_WithMixedPlaceIds() {
        // Given
        final Route route =
                new Route(
                        new Stop(
                                "1",
                                "Origin",
                                Optional.of("place1"),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES)),
                                Optional.empty()),
                        List.of(
                                new Stop(
                                        "2",
                                        "W1",
                                        Optional.of("placeW1"),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES)),
                                        Optional.empty()),
                                new Stop(
                                        "3",
                                        "W2",
                                        Optional.empty(),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(20.0, DEGREES),
                                                new Angle(30.0, DEGREES)),
                                        Optional.empty())),
                        new Stop(
                                "4",
                                "Destination",
                                Optional.of("place3"),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES)),
                                Optional.empty()));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=Origin&origin_place_id=place1&destination=Destination&destination_place_id=place3&waypoints=W1%7C20%2C30&waypoint_place_ids=placeW1%7C",
                result.toString());
    }

    @Test
    public void testGetUrl_WithManyStops() {
        // Given: Route with 12 stops (origin, destination, 10 waypoints)
        final Stop origin =
                new Stop(
                        "0",
                        "Origin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(10.0, DEGREES),
                                new Angle(20.0, DEGREES)),
                        Optional.empty());
        final Stop destination =
                new Stop(
                        "11",
                        "Destination",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(30.0, DEGREES),
                                new Angle(40.0, DEGREES)),
                        Optional.empty());
        final List<Stop> waypoints = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            waypoints.add(
                    new Stop(
                            String.valueOf(i),
                            "W" + i,
                            Optional.empty(),
                            Geodetic.fromLatitudeLongitude(
                                    new Angle(10.0 + i, DEGREES),
                                    new Angle(20.0 + i, DEGREES)),
                            Optional.empty()));
        }
        final Route route = new Route(origin, waypoints, destination);

        // When
        final URL url = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url), route);
    }
}
