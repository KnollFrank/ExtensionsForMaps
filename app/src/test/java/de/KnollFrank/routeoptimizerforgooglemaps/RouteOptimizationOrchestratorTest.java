package de.KnollFrank.routeoptimizerforgooglemaps;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

@RunWith(RobolectricTestRunner.class)
public class RouteOptimizationOrchestratorTest {

    // FK-TODO: brauchen Test, bei dem die routeUrl keine URL ist.
    @Test
    public void optimizeRouteOfDirectionsUrl() throws InterruptedException {
        // Given
        final String routeUrl = "https://www.google.com/maps/dir/Central-Apotheke/Hamburg/Unterhausen/data=!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0?utm_source=mstt_0&g_ep=CAESCDI2LjE2LjEyGAAgkUEqiwEsOTQyNjc3MjcsOTQyOTIxOTUsOTQyOTk1MzIsMTAwNzk2NDk4LDEwMDc5Nzc2MSwxMDA3OTY1MzUsOTQyODA1NzYsMTAwODExOTYwLDk0MjA3Mzk0LDk0MjA3NTA2LDk0MjA4NTA2LDk0MjE4NjUzLDk0MjI5ODM5LDk0Mjc1MTY4LDk0Mjc5NjE5QgJVUw%3D%3D&skid=0a1f62d3-c01c-47b9-b4b6-ccadc456baa8";
        final RoutingMatricesProvider routingMatricesProvider =
                new RoutingMatricesProvider() {

                    @Override
                    public RouteOptimizer.RoutingMatrices getRoutingMatrices(final Geodetic start,
                                                                             final List<Stop> stops) {
                        return new RouteOptimizer.RoutingMatrices(
                                new double[][]{
                                        new double[]{0.0, 709743.3, 32104.7},
                                        new double[]{708177.6, 0.0, 716507.1},
                                        new double[]{32159.8, 717982.8, 0.0}
                                },
                                new double[][]{
                                        new double[]{0.0, 25712.7, 2338.5},
                                        new double[]{25609.3, 0.0, 26124.7},
                                        new double[]{2373.0, 26136.4, 0.0}
                                });
                    }
                };

        // When
        final List<Stop> route = getRoute(routeUrl, routingMatricesProvider);

        // Then
        Assert.assertEquals(
                List.of("Central-Apotheke", "Unterhausen", "Hamburg"),
                getAddresses(route));
    }

    private static List<Stop> getRoute(final String directionsUrl,
                                       final RoutingMatricesProvider routingMatricesProvider) throws InterruptedException {
        final AtomicReference<Optional<List<Stop>>> route = new AtomicReference<>(Optional.empty());
        final CountDownLatch latch = new CountDownLatch(1);
        final RouteOptimizationOrchestrator orchestrator =
                new RouteOptimizationOrchestrator(
                        createCallback(route, latch),
                        new RouteOptimizer(routingMatricesProvider));
        orchestrator.optimizeRouteOfDirectionsUrl(directionsUrl);
        latch.await();
        return route.get().orElseThrow();
    }

    private static RouteOptimizationOrchestrator.Callback createCallback(
            final AtomicReference<Optional<List<Stop>>> route,
            final CountDownLatch latch) {
        return new RouteOptimizationOrchestrator.Callback() {

            @Override
            public void onOptimizationStarted() {
            }

            @Override
            public void onOptimizationSuccess(final List<Stop> finalRoute) {
                route.set(Optional.of(finalRoute));
                latch.countDown();
            }

            @Override
            public void onError(final String message) {
                route.set(Optional.empty());
                latch.countDown();
            }
        };
    }

    // FK-TODO: move method to Stops class
    public static List<String> getAddresses(final List<Stop> stops) {
        return stops
                .stream()
                .map(Stop::address)
                .toList();
    }
}