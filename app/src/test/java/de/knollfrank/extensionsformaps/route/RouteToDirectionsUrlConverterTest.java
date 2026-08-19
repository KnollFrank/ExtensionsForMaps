package de.knollfrank.extensionsformaps.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static de.knollfrank.extensionsformaps.coordinate.Unit.DEGREES;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.common.Lists;
import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;
import de.knollfrank.extensionsformaps.route.extract.GoogleMapsRouteExtractor;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;

@RunWith(RobolectricTestRunner.class)
public class RouteToDirectionsUrlConverterTest {

    @Test
    public void testGetDirectionsUrl_OriginAndDestination() {
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
        final DirectionsUrl directionsUrl = RouteToDirectionsUrlConverter.getDirectionsUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=48.5016154%2C8.9930185&destination=48.476549%2C8.9349058",
                directionsUrl.url().toString());
    }

    @Test
    public void testGetDirectionsUrl_WithWaypoints() {
        // Given
        final Route route = RouteTestFactory.createRouteWithTwoWaypoints();

        // When
        final DirectionsUrl directionsUrl = RouteToDirectionsUrlConverter.getDirectionsUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=10%2C20&destination=30%2C40&waypoints=15%2C25",
                directionsUrl.url().toString());
    }

    @Test
    public void testGetDirectionsUrl_WithMultipleWaypoints() {
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
        final DirectionsUrl directionsUrl = RouteToDirectionsUrlConverter.getDirectionsUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=10%2C20&destination=30%2C40&waypoints=15%2C25%7C20%2C30",
                directionsUrl.url().toString());
    }

    @Test
    public void testGetDirectionsUrl_WithPlaceIds() {
        // Given
        final Route route =
                new Route(
                        new Stop(
                                "1",
                                "Origin City",
                                Optional.of(new OfficialPlaceId("ChIJ1V1RE0v8mUcROpsR_6oBUjQ")),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES)),
                                Optional.empty()),
                        List.of(
                                new Stop(
                                        "2",
                                        "Waypoint Street",
                                        Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
                                        Geodetic.fromLatitudeLongitude(
                                                new Angle(15.0, DEGREES),
                                                new Angle(25.0, DEGREES)),
                                        Optional.empty())),
                        new Stop(
                                "3",
                                "Destination Landmark",
                                Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES)),
                                Optional.empty()));

        // When
        final DirectionsUrl directionsUrl = RouteToDirectionsUrlConverter.getDirectionsUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=Origin%20City&origin_place_id=ChIJ1V1RE0v8mUcROpsR_6oBUjQ&destination=Destination%20Landmark&destination_place_id=ChIJsYBbyF7zmUcREc3DW6XSMuQ&waypoints=Waypoint%20Street&waypoint_place_ids=ChIJsYBbyF7zmUcREc3DW6XSMuQ",
                directionsUrl.url().toString());
    }

    @Test
    public void testGetDirectionsUrl_WithMixedPlaceIds() {
        // Given
        final Route route =
                new Route(
                        new Stop(
                                "1",
                                "Origin",
                                Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(10.0, DEGREES),
                                        new Angle(20.0, DEGREES)),
                                Optional.empty()),
                        List.of(
                                new Stop(
                                        "2",
                                        "W1",
                                        Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
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
                                Optional.of(new OfficialPlaceId("ChIJsYBbyF7zmUcREc3DW6XSMuQ")),
                                Geodetic.fromLatitudeLongitude(
                                        new Angle(30.0, DEGREES),
                                        new Angle(40.0, DEGREES)),
                                Optional.empty()));

        // When
        final DirectionsUrl directionsUrl = RouteToDirectionsUrlConverter.getDirectionsUrl(route);

        // Then
        assertEquals(
                "https://www.google.com/maps/dir/?api=1&origin=Origin&origin_place_id=ChIJsYBbyF7zmUcREc3DW6XSMuQ&destination=Destination&destination_place_id=ChIJsYBbyF7zmUcREc3DW6XSMuQ&waypoints=W1%7C20%2C30&waypoint_place_ids=ChIJsYBbyF7zmUcREc3DW6XSMuQ%7C",
                directionsUrl.url().toString());
    }

    @Test
    public void testGetDirectionsUrl_WithManyStops() {
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
        final DirectionsUrl directionsUrl = RouteToDirectionsUrlConverter.getDirectionsUrl(route);

        // Then
        assertEquals(GoogleMapsRouteExtractor.extractRoute(directionsUrl), route);
    }

    @Test
    public void testGetDirectionsUrl_WithManyStopsAndPlaceId() {
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
                                        new Angle(9.0, DEGREES)),
                                Optional.empty()),
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
                                        new Angle(9.1, DEGREES)),
                                Optional.empty()));

        // When
        final DirectionsUrl directionsUrl = RouteToDirectionsUrlConverter.getDirectionsUrl(route);

        // Then
        // 1. Verifiziere, dass die Place-ID im data-Part vorkommt (als Hex kodiert)
        assertTrue(directionsUrl.url().toString().contains(officialPlaceId.toUndocumentedPlaceId().value()));

        // 2. Verifiziere den Round-Trip (lossless extraction)
        assertEquals(GoogleMapsRouteExtractor.extractRoute(directionsUrl), route);
    }

    private static Stop createWaypoint(final int i, final Optional<OfficialPlaceId> officialPlaceId) {
        return new Stop(
                String.valueOf(i),
                "W" + i,
                officialPlaceId,
                Geodetic.fromLatitudeLongitude(
                        new Angle(48.5 + i * 0.01, DEGREES),
                        new Angle(9.0 + i * 0.01, DEGREES)),
                Optional.empty());
    }
}
