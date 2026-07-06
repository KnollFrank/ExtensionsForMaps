package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import androidx.annotation.NonNull;

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
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(RobolectricTestRunner.class)
// FK-TODO: refactor
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
        final Stop stopA = createStop("A", 52.52, 13.40);
        final Stop stopB = createStop("B", 52.62, 13.50);
        final Set<Stop> stops = Set.of(stopA, stopB);

        // Die erwartete URL-Teile für den Dispatcher
        final Dispatcher dispatcher =
                new Dispatcher() {

                    @NonNull
                    @Override
                    public MockResponse dispatch(final RecordedRequest request) {
                        final String expectedCoordsA = "13.400000,52.520000";
                        final String expectedCoordsB = "13.500000,52.620000";
                        final String path = request.getPath();
                        if (path.contains(expectedCoordsA) && path.contains(expectedCoordsB) && path.contains("annotations=distance,duration")) {
                            return new MockResponse()
                                    .setResponseCode(200)
                                    .setBody("{" +
                                                     "  \"code\": \"Ok\"," +
                                                     "  \"distances\": [[0, 1000.5], [2000.5, 0]]," +
                                                     "  \"durations\": [[0, 60.2], [120.4, 0]]" +
                                                     "}");
                        }
                        return new MockResponse().setResponseCode(404);
                    }
                };
        mockWebServer.setDispatcher(dispatcher);

        // When
        final RoutingMatrix matrix = provider.getRoutingMatrix(stops);

        // Then
        final List<Stop> stopsList = stops.stream().toList();
        final var table = matrix.getDistanceDurationByStopTable();
        // FK-TODO: direkt die ganzen Tabellen auf Gleichheit überprüfen, nicht nur einzelne Einträge.
        assertEquals(1000.5, table.get(stopsList.get(0), stopsList.get(1)).distance(), 0.01);
        assertEquals(60.2, table.get(stopsList.get(0), stopsList.get(1)).duration(), 0.01);
        assertEquals(2000.5, table.get(stopsList.get(1), stopsList.get(0)).distance(), 0.01);
        assertEquals(120.4, table.get(stopsList.get(1), stopsList.get(0)).duration(), 0.01);
    }

    @Test
    public void testGetRoutingMatrix_apiError() {
        // Given
        final OsrmRoutingMatrixProvider provider = createProvider();
        final Stop stopA = createStop("A", 52.52, 13.40);
        final Set<Stop> stops = Set.of(stopA);
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody("{" +
                                         "  \"code\": \"Error\"," +
                                         "  \"message\": \"Some error occurred\"" +
                                         "}"));

        // When / Then
        assertThrows(IOException.class, () -> provider.getRoutingMatrix(stops));
    }

    private OsrmRoutingMatrixProvider createProvider() {
        return new OsrmRoutingMatrixProvider(mockWebServer.url("/").url());
    }

    private static Stop createStop(final String id, final double lat, final double lon) {
        return new Stop(
                id,
                "Addr " + id,
                Optional.empty(),
                Geodetic.fromLatitudeLongitude(new Angle(lat, Unit.DEGREES), new Angle(lon, Unit.DEGREES)),
                Optional.empty());
    }
}
