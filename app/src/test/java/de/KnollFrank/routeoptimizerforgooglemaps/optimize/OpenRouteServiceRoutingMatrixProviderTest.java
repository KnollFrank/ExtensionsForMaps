package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

// FK-TODO: refactor
@RunWith(RobolectricTestRunner.class)
public class OpenRouteServiceRoutingMatrixProviderTest {

    private MockWebServer mockWebServer;
    private final String testApiKey = "test-api-key";

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
        final Stop stopA = createStop("A", 48.477473, 9.70093);
        final Stop stopB = createStop("B", 49.153868, 9.207916);
        final Set<Stop> stops = Set.of(stopA, stopB);
        final List<Stop> stopsList = stops.stream().toList();

        final Dispatcher dispatcher =
                new Dispatcher() {

                    @NonNull
                    @Override
                    public MockResponse dispatch(final @NonNull RecordedRequest request) {
                        if (!"POST".equals(request.getMethod())) {
                            return new MockResponse().setResponseCode(405);
                        }
                        if (!request.getPath().equals("/v2/matrix/driving-car")) {
                            return new MockResponse().setResponseCode(404);
                        }
                        if (!testApiKey.equals(request.getHeader("Authorization"))) {
                            return new MockResponse().setResponseCode(401);
                        }
                        // Verifiziere den Body (Koordinaten)
                        final String body = request.getBody().readUtf8();
                        if (!body.contains("9.70093,48.477473") || !body.contains("9.207916,49.153868")) {
                            return new MockResponse().setResponseCode(400).setBody("Invalid coordinates in body");
                        }
                        return new MockResponse()
                                .setResponseCode(200)
                                .setBody("{\"durations\":[[0,5670.16],[5494.1,0]],\"distances\":[[0,140859.05],[139760.8,0]]}");
                    }
                };
        mockWebServer.setDispatcher(dispatcher);

        final OpenRouteServiceRoutingMatrixProvider provider =
                new OpenRouteServiceRoutingMatrixProvider(
                        testApiKey,
                        mockWebServer.url("/").url());

        // When
        final RoutingMatrix matrix = provider.getRoutingMatrix(stops);

        // Then
        assertNotNull(matrix);
        final var table = matrix.getDistanceDurationByStopTable();
        // Index mapping: stopsList(0) -> JSON row 0, stopsList(1) -> JSON row 1
        assertEquals(140859.05, table.get(stopsList.get(0), stopsList.get(1)).distance(), 0.01);
        assertEquals(5670.16, table.get(stopsList.get(0), stopsList.get(1)).duration(), 0.01);
        assertEquals(139760.8, table.get(stopsList.get(1), stopsList.get(0)).distance(), 0.01);
        assertEquals(5494.1, table.get(stopsList.get(1), stopsList.get(0)).duration(), 0.01);
    }

    @Test
    public void testGetRoutingMatrix_error() {
        // Given
        final Set<Stop> stops = Set.of(createStop("A", 0, 0));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        final OpenRouteServiceRoutingMatrixProvider provider = new OpenRouteServiceRoutingMatrixProvider(testApiKey, mockWebServer.url("/").url());

        // When / Then
        assertThrows(IOException.class, () -> provider.getRoutingMatrix(stops));
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
