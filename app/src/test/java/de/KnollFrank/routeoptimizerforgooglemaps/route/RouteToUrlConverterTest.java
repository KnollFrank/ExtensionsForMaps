package de.KnollFrank.routeoptimizerforgooglemaps.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit.DEGREES;

import org.junit.Test;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;

public class RouteToUrlConverterTest {

    @Test
    public void testGetUrl_OriginAndDestination() {
        // Given
        final Stop origin =
                new Stop(
                        1,
                        "Origin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.50161543647, DEGREES),
                                new Angle(8.993018526571907, DEGREES)));
        final Stop destination =
                new Stop(
                        2,
                        "Destination",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.476548982116846, DEGREES),
                                new Angle(8.934905787678979, DEGREES)));
        final Route route = new Route(List.of(origin, destination));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=48.5016154,8.9930185&destination=48.476549,8.9349058",
                result.toString());
    }

    @Test
    public void testGetUrl_WithWaypoints() {
        // Given
        final Stop origin =
                new Stop(
                        1,
                        "Origin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(10.0, DEGREES),
                                new Angle(20.0, DEGREES)));
        final Stop waypoint =
                new Stop(
                        2,
                        "Waypoint",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(15.0, DEGREES),
                                new Angle(25.0, DEGREES)));
        final Stop destination =
                new Stop(
                        3,
                        "Destination",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(30.0, DEGREES),
                                new Angle(40.0, DEGREES)));
        final Route route = new Route(List.of(origin, waypoint, destination));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=10,20&destination=30,40&waypoints=15,25",
                result.toString());
    }

    @Test
    public void testGetUrl_WithMultipleWaypoints() {
        // Given
        final Stop origin =
                new Stop(
                        1,
                        "Origin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(10.0, DEGREES),
                                new Angle(20.0, DEGREES)));
        final Stop waypoint1 =
                new Stop(
                        2,
                        "W1",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(15.0, DEGREES),
                                new Angle(25.0, DEGREES)));
        final Stop waypoint2 =
                new Stop(
                        3,
                        "W2",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(20.0, DEGREES),
                                new Angle(30.0, DEGREES)));
        final Stop destination =
                new Stop(
                        4,
                        "Destination",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(30.0, DEGREES),
                                new Angle(40.0, DEGREES)));
        final Route route = new Route(List.of(origin, waypoint1, waypoint2, destination));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=10,20&destination=30,40&waypoints=15,25|20,30",
                result.toString());
    }

    @Test
    public void testGetUrl_WithPlaceIds() {
        // Given
        final Stop origin =
                new Stop(
                        1,
                        "Origin City",
                        Optional.of("place1"),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(10.0, DEGREES),
                                new Angle(20.0, DEGREES)));
        final Stop waypoint =
                new Stop(
                        2,
                        "Waypoint Street",
                        Optional.of("place2"),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(15.0, DEGREES),
                                new Angle(25.0, DEGREES)));
        final Stop destination =
                new Stop(
                        3,
                        "Destination Landmark",
                        Optional.of("place3"),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(30.0, DEGREES),
                                new Angle(40.0, DEGREES)));
        final Route route = new Route(List.of(origin, waypoint, destination));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=Origin+City&origin_place_id=place1&destination=Destination+Landmark&destination_place_id=place3&waypoints=Waypoint+Street&waypoint_place_ids=place2",
                result.toString());
    }

    @Test
    public void testGetUrl_WithMixedPlaceIds() {
        // Given
        final Stop origin =
                new Stop(
                        1,
                        "Origin",
                        Optional.of("place1"),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(10.0, DEGREES),
                                new Angle(20.0, DEGREES)));
        final Stop waypoint1 =
                new Stop(
                        2,
                        "W1",
                        Optional.of("placeW1"),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(15.0, DEGREES),
                                new Angle(25.0, DEGREES)));
        final Stop waypoint2 =
                new Stop(
                        3,
                        "W2",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(20.0, DEGREES),
                                new Angle(30.0, DEGREES)));
        final Stop destination =
                new Stop(
                        4,
                        "Destination",
                        Optional.of("place3"),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(30.0, DEGREES),
                                new Angle(40.0, DEGREES)));
        final Route route = new Route(List.of(origin, waypoint1, waypoint2, destination));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=Origin&origin_place_id=place1&destination=Destination&destination_place_id=place3&waypoints=W1|20,30&waypoint_place_ids=placeW1|",
                result.toString());
    }

    @Test
    public void testGetUrl_TooFewStops_ThrowsException() {
        // Given
        final Stop origin =
                new Stop(
                        1,
                        "Origin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(10.0, DEGREES),
                                new Angle(20.0, DEGREES)));
        final Route route = new Route(List.of(origin));

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> RouteToUrlConverter.getUrl(route));
    }
}
