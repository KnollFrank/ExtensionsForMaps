package de.KnollFrank.routeoptimizerforgooglemaps.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit.DEGREES;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.URL;
import java.util.List;
import java.util.Optional;

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
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.50161543647, DEGREES),
                                        new Angle(8.993018526571907, DEGREES))),
                        List.of(),
                        new Stop(
                                "2",
                                "Destination",
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.476548982116846, DEGREES),
                                        new Angle(8.934905787678979, DEGREES))));

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
        final Route route = RouteTestFactory.createRouteWithTwoWaypoints();

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
                                        new Angle(20.0, DEGREES))),
                        List.of(
                                new Stop(
                                        "2",
                                        "W1",
                                        Optional.empty(),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES))),
                                new Stop(
                                        "3",
                                        "W2",
                                        Optional.empty(),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(20.0, DEGREES),
                                                new Angle(30.0, DEGREES)))),
                        new Stop(
                                "4",
                                "Destination",
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES))));

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
                                Optional.of(new OfficialPlaceId("ChIJ1V1RE0v8mUcROpsR_6oBUjQ")),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES))),
                        List.of(
                                new Stop(
                                        "2",
                                        "Waypoint Street",
                                        Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES)))),
                        new Stop(
                                "3",
                                "Destination Landmark",
                                Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES))));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=Origin%20City&origin_place_id=ChIJ1V1RE0v8mUcROpsR_6oBUjQ&destination=Destination%20Landmark&destination_place_id=ChIJsYBbyF7zmUcREc3DW6XSMuQ&waypoints=Waypoint%20Street&waypoint_place_ids=ChIJsYBbyF7zmUcREc3DW6XSMuQ",
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
                                Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES))),
                        List.of(
                                new Stop(
                                        "2",
                                        "W1",
                                        Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES))),
                                new Stop(
                                        "3",
                                        "W2",
                                        Optional.empty(),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(20.0, DEGREES),
                                                new Angle(30.0, DEGREES)))),
                        new Stop(
                                "4",
                                "Destination",
                                Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES))));

        // When
        final URL result = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=Origin&origin_place_id=ChIJsYBbyF7zmUcREc3DW6XSMuQ&destination=Destination&destination_place_id=ChIJsYBbyF7zmUcREc3DW6XSMuQ&waypoints=W1%7C20%2C30&waypoint_place_ids=ChIJsYBbyF7zmUcREc3DW6XSMuQ%7C",
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
                                new Angle(20.0, DEGREES)));
        final Stop destination =
                new Stop(
                        "11",
                        "Destination",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(30.0, DEGREES),
                                new Angle(40.0, DEGREES)));
        final List<Stop> waypoints = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            waypoints.add(
                    new Stop(
                            String.valueOf(i),
                            "W" + i,
                            Optional.empty(),
                            Geodetic.fromLatitudeLongitude(
                                    new Angle(10.0 + i, DEGREES),
                                    new Angle(20.0 + i, DEGREES))));
        }
        final Route route = new Route(origin, waypoints, destination);

        // When
        final URL url = RouteToUrlConverter.getUrl(route);

        // Then
        assertEquals(GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url), route);
    }

    @Test
    public void testGetUrl_WithManyStopsAndPlaceId() {
        // Given: Route with 12 stops (origin, destination, 10 waypoints)
        // One waypoint has a Place ID.
        final OfficialPlaceId officialPlaceId = new OfficialPlaceId("ChIJgUbEo8cfqokR5lP9_Wh_DaM");
        final Route route =
                new Route(
                        new Stop(
                                "0",
                                "Origin",
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.5, DEGREES),
                                        new Angle(9.0, DEGREES))),
                        Lists
                                .createRange(1, 10)
                                .stream()
                                .map(i ->
                                             createWaypoint(
                                                     i,
                                                     i == 5 ? Optional.of(officialPlaceId) : Optional.empty()))
                                .toList(),
                        new Stop(
                                "11",
                                "Destination",
                                Optional.empty(),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(48.6, DEGREES),
                                        new Angle(9.1, DEGREES))));

        // When
        final URL url = RouteToUrlConverter.getUrl(route);

        // Then
        // 1. Verifiziere, dass die Place-ID im data-Part vorkommt (als Hex kodiert)
        assertTrue(url.toString().contains(officialPlaceId.toUndocumentedPlaceId().value()));

        // 2. Verifiziere den Round-Trip (lossless extraction)
        assertEquals(route, GoogleMapsRouteExtractor.extractRouteFromDirectionsUrl(url));
    }

    private static Stop createWaypoint(final int i, final Optional<OfficialPlaceId> officialPlaceId) {
        return new Stop(
                String.valueOf(i),
                "W" + i,
                officialPlaceId,
                Geodetic.fromLatitudeLongitude(
                        new Angle(48.5 + i * 0.01, DEGREES),
                        new Angle(9.0 + i * 0.01, DEGREES)));
    }
}
