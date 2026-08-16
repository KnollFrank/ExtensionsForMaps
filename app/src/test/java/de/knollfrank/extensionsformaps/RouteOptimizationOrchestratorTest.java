package de.knollfrank.extensionsformaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static de.knollfrank.extensionsformaps.route.Stops.getAddresses;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.collect.ImmutableTable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import de.knollfrank.extensionsformaps.optimize.DistanceDuration;
import de.knollfrank.extensionsformaps.optimize.OsrmVehicleRoutingTransportCostsProvider;
import de.knollfrank.extensionsformaps.optimize.RouteOptimizer;
import de.knollfrank.extensionsformaps.optimize.RoutingMatrix;
import de.knollfrank.extensionsformaps.optimize.RoutingMatrixProvider;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrl;
import de.knollfrank.extensionsformaps.route.url.DirectionsUrlFactory;

@RunWith(RobolectricTestRunner.class)
public class RouteOptimizationOrchestratorTest {

    @Test
    public void test_optimizeRouteOfDirectionsUrl() throws InterruptedException, MalformedURLException {
        // Given
        final DirectionsUrl directionsUrl =
                DirectionsUrlFactory
                        .createDirectionsUrl(new URL("https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0&g_ep=CAESCDI2LjE2LjEyGAAgkUEqiwEsOTQyNjc3MjcsOTQyOTIxOTUsOTQyOTk1MzIsMTAwNzk2NDk4LDEwMDc5Nzc2MSwxMDA3OTY1MzUsOTQyODA1NzYsMTAwODExOTYwLDk0MjA3Mzk0LDk0MjA3NTA2LDk0MjA4NTA2LDk0MjE4NjUzLDk0MjI5ODM5LDk0Mjc1MTY4LDk0Mjc5NjE5QgJVUw%3D%3D&skid=0a1f62d3-c01c-47b9-b4b6-ccadc456baa8"))
                        .orElseThrow();
        final RoutingMatrixProvider routingMatrixProviderForCentralApothekeHamburgUnterhausen = getRoutingMatrixProviderForCentralApothekeHamburgUnterhausen();

        // When
        final Optional<Route> optimizedRoute = optimizeRouteOfDirectionsUrl(directionsUrl, routingMatrixProviderForCentralApothekeHamburgUnterhausen);

        // Then
        assertTrue(optimizedRoute.isPresent());
        assertEquals(
                List.of("Central-Apotheke", "Hamburg", "Unterhausen"),
                getAddresses(optimizedRoute.orElseThrow().stops()));
    }

    private static Optional<Route> optimizeRouteOfDirectionsUrl(final DirectionsUrl directionsUrl,
                                                                final RoutingMatrixProvider routingMatrixProvider) throws InterruptedException {
        final AtomicReference<Optional<Route>> extractedRoute = new AtomicReference<>(Optional.empty());
        final AtomicReference<Optional<Route>> optimizedRoute = new AtomicReference<>(Optional.empty());
        final CountDownLatch extractionLatch = new CountDownLatch(1);
        final CountDownLatch optimizationLatch = new CountDownLatch(1);
        final RouteOptimizationOrchestrator orchestrator =
                new RouteOptimizationOrchestrator(
                        ApplicationProvider.getApplicationContext(),
                        createCallback(extractedRoute, extractionLatch, optimizedRoute, optimizationLatch),
                        new RouteOptimizer(new OsrmVehicleRoutingTransportCostsProvider(routingMatrixProvider)));
        orchestrator.extractRouteFromDirectionsUrl(directionsUrl);
        extractionLatch.await();
        if (extractedRoute.get().isPresent()) {
            orchestrator.optimizeRoute(extractedRoute.get().get());
            optimizationLatch.await();
        }
        return optimizedRoute.get();
    }

    private static RouteOptimizationOrchestrator.Callback createCallback(final AtomicReference<Optional<Route>> extractedRoute, final CountDownLatch extractionLatch, final AtomicReference<Optional<Route>> optimizedRoute, final CountDownLatch optimizationLatch) {
        return new RouteOptimizationOrchestrator.Callback() {

            @Override
            public void onExtractRouteFromDirectionsUrlStarted() {
            }

            @Override
            public void onExtractRouteFromDirectionsUrlSuccess(Route route) {
                extractedRoute.set(Optional.of(route));
                extractionLatch.countDown();
            }

            @Override
            public void onOptimizationStarted() {
            }

            @Override
            public void onOptimizationProgress(int progressPercentage) {
            }

            @Override
            public void onOptimizationSuccess(Route route) {
                optimizedRoute.set(Optional.of(route));
                optimizationLatch.countDown();
            }

            @Override
            public void onError(String message) {
                extractionLatch.countDown();
                optimizationLatch.countDown();
            }
        };
    }

    private static RoutingMatrixProvider getRoutingMatrixProviderForCentralApothekeHamburgUnterhausen() {
        return new RoutingMatrixProvider() {

            @Override
            public RoutingMatrix getRoutingMatrix(final Set<Stop> stops) {
                return getRoutingMatrix(List.copyOf(stops));
            }

            private RoutingMatrix getRoutingMatrix(final List<Stop> stops) {
                if (stops.size() != 3) {
                    throw new IllegalArgumentException("" + stops);
                }
                return getRoutingMatrix(stops.get(0), stops.get(1), stops.get(2));
            }

            private static RoutingMatrix getRoutingMatrix(final Stop stop0, final Stop stop1, final Stop stop2) {
                return new RoutingMatrix(
                        ImmutableTable
                                .<Stop, Stop, DistanceDuration>builder()
                                .put(stop0, stop0, new DistanceDuration(0.0, 0.0))
                                .put(stop0, stop1, new DistanceDuration(709743.3, 25712.7))
                                .put(stop0, stop2, new DistanceDuration(32104.7, 2338.5))

                                .put(stop1, stop0, new DistanceDuration(708177.6, 25609.3))
                                .put(stop1, stop1, new DistanceDuration(0.0, 0.0))
                                .put(stop1, stop2, new DistanceDuration(716507.1, 26124.7))

                                .put(stop2, stop0, new DistanceDuration(32159.8, 2373.0))
                                .put(stop2, stop1, new DistanceDuration(717982.8, 26136.4))
                                .put(stop2, stop2, new DistanceDuration(0.0, 0.0))
                                .build());
            }
        };
    }
}