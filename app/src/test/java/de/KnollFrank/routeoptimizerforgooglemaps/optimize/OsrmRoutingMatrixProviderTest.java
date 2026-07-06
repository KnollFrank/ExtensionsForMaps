package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
public class OsrmRoutingMatrixProviderTest {

    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetRoutingMatrix_success() throws Exception {
        // Given
        final OsrmRoutingMatrixProvider provider = createProvider();
        final Stop stopA =
                createStop(
                        "A",
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.52, Unit.DEGREES),
                                new Angle(13.40, Unit.DEGREES)));
        final Stop stopB =
                createStop(
                        "B",
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.62, Unit.DEGREES),
                                new Angle(13.50, Unit.DEGREES)));
        final Set<Stop> stops = Set.of(stopA, stopB);

        // Order in List is how provider process it: stream().toList()
        final List<Stop> stopsList = stops.stream().toList();
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(
                                "{" +
                                        "  \"code\": \"Ok\"," +
                                        "  \"distances\": [[0, 1000.5], [2000.5, 0]]," +
                                        "  \"durations\": [[0, 60.2], [120.4, 0]]" +
                                        "}"));

        // When
        final RoutingMatrix matrix = provider.getRoutingMatrix(stops);

        // Then
        // FK-TODO: direkt die ganzen Tabellen auf Gleichheit überprüfen, nicht nur einzelne Einträge.
        final var table = matrix.getDistanceDurationByStopTable();
        assertEquals(1000.5, table.get(stopsList.get(0), stopsList.get(1)).distance(), 0.01);
        assertEquals(60.2, table.get(stopsList.get(0), stopsList.get(1)).duration(), 0.01);
        assertEquals(2000.5, table.get(stopsList.get(1), stopsList.get(0)).distance(), 0.01);
        assertEquals(120.4, table.get(stopsList.get(1), stopsList.get(0)).duration(), 0.01);
    }

    @Test
    public void testGetRoutingMatrix_apiError() {
        // Given
        final OsrmRoutingMatrixProvider provider = createProvider();
        final Stop stopA =
                createStop(
                        "A",
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.52, Unit.DEGREES),
                                new Angle(13.40, Unit.DEGREES)));
        final Set<Stop> stops = Set.of(stopA);
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(
                                "{" +
                                        "  \"code\": \"Error\"," +
                                        "  \"message\": \"Some error occurred\"" +
                                        "}"));

        // When / Then
        assertThrows(
                IOException.class,
                () -> provider.getRoutingMatrix(stops));
    }

    private OsrmRoutingMatrixProvider createProvider() {
        return new OsrmRoutingMatrixProvider(mockWebServer.url("/").url());
    }

    private static Stop createStop(final String id, final Geodetic geodetic) {
        return new Stop(
                id,
                "Addr " + id,
                Optional.empty(),
                geodetic,
                Optional.empty());
    }
}
