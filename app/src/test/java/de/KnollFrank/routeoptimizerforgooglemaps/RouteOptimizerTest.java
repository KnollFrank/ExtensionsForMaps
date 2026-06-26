package de.KnollFrank.routeoptimizerforgooglemaps;

import static org.junit.Assert.assertEquals;

import static de.KnollFrank.routeoptimizerforgooglemaps.RouteOptimizationOrchestratorTest.getAddresses;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

@RunWith(RobolectricTestRunner.class)
public class RouteOptimizerTest {

    @Test
    public void testOptimizeStops_sortsByShortestDistance() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer = new RouteOptimizer(new OsrmRoutingMatricesProvider());
        final Geodetic berlin_start =
                Geodetic.fromLatitudeLongitude(
                        new Angle(52.5200, Unit.DEGREES),
                        new Angle(13.4050, Unit.DEGREES));
        final Stop munich_far =
                new Stop(
                        0,
                        "Munich",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.1351, Unit.DEGREES),
                                new Angle(11.5820, Unit.DEGREES)));
        final Stop potsdam_very_close =
                new Stop(
                        1,
                        "Potsdam",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.3906, Unit.DEGREES),
                                new Angle(13.0645, Unit.DEGREES)));
        final Stop leipzig_medium =
                new Stop(
                        2,
                        "Leipzig",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(51.3397, Unit.DEGREES),
                                new Angle(12.3731, Unit.DEGREES)));

        // When
        final List<Stop> optimized =
                routeOptimizer.optimizeStops(
                        berlin_start,
                        List.of(munich_far, potsdam_very_close, leipzig_medium),
                        RouteOptimizer.OptimizationStrategy.HAVERSINE);

        // Then
        assertEquals(
                getAddresses(List.of(potsdam_very_close, leipzig_medium, munich_far)),
                getAddresses(optimized));
    }

    @Test
    public void testOptimizeStops_userBugReproduction_Exact() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer = new RouteOptimizer(new OsrmRoutingMatricesProvider());
        final Geodetic start =
                Geodetic.fromLatitudeLongitude(
                        new Angle(48.4765345, Unit.DEGREES),
                        new Angle(8.9349008, Unit.DEGREES));
        final Stop hamburg =
                new Stop(
                        0,
                        "Hamburg",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(53.548828, Unit.DEGREES),
                                new Angle(9.987170, Unit.DEGREES)));
        final Stop unterhausen =
                new Stop(
                        1,
                        "Unterhausen",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.430628, Unit.DEGREES),
                                new Angle(9.2546378, Unit.DEGREES)));

        // When
        final List<Stop> optimized =
                routeOptimizer.optimizeStops(
                        start,
                        List.of(hamburg, unterhausen),
                        RouteOptimizer.OptimizationStrategy.HAVERSINE);

        // Then
        assertEquals(
                getAddresses(List.of(unterhausen, hamburg)),
                getAddresses(optimized));
    }

    @Test
    public void testOptimizeStops_osrmVsHaversine_LakeGarda() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer = new RouteOptimizer(new OsrmRoutingMatricesProvider());
        // Geografisches Hindernis: Der Gardasee (Lago di Garda) in Italien
        // Start: Limone sul Garda (am Westufer des Sees)
        final Geodetic start_LimoneSulGarda =
                Geodetic.fromLatitudeLongitude(
                        new Angle(45.8156, Unit.DEGREES),
                        new Angle(10.7904, Unit.DEGREES));
        // Stop A: Malcesine
        // (Liegt am Ostufer, genau gegenüber von Limone. Luftlinie: ~6 km. Auto: ~30 km, da man um den See fahren muss)
        final Stop malcesine =
                new Stop(
                        0,
                        "Malcesine",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(45.7656, Unit.DEGREES),
                                new Angle(10.8092, Unit.DEGREES)));
        // Stop B: Riva del Garda
        // (Liegt an der Nordspitze. Luftlinie: ~9 km. Auto: ~11 km, da auf derselben Uferseite über direkte Straße erreichbar)
        final Stop rivaDelGarda =
                new Stop(
                        1,
                        "Riva del Garda",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(45.8893, Unit.DEGREES),
                                new Angle(10.8430, Unit.DEGREES)));
        final List<Stop> stops = List.of(malcesine, rivaDelGarda);

        // ==========================================================
        // TEST 1: Haversine-Strategie (Luftlinie ignoriert den See)
        // Erwartung: Limone -> Malcesine (6km) -> Riva del Garda
        // ==========================================================
        // When
        final List<Stop> haversineRoute =
                routeOptimizer.optimizeStops(
                        start_LimoneSulGarda,
                        stops,
                        RouteOptimizer.OptimizationStrategy.HAVERSINE);

        // Then
        assertEquals(
                getAddresses(List.of(malcesine, rivaDelGarda)),
                getAddresses(haversineRoute));

        // ==========================================================
        // TEST 2: OSRM-Strategie (Echte Straßenführung)
        // Erwartung: Limone -> Riva del Garda (11km) -> Malcesine (weitere 19km)
        // ==========================================================
        // FK-TODO: für diesen Unittest bitte kein Internetzugriff, sondern hart codierte RoutingMatrices verwenden.
        // When
        final List<Stop> osrmRoute =
                routeOptimizer.optimizeStops(
                        start_LimoneSulGarda,
                        stops,
                        RouteOptimizer.OptimizationStrategy.OSRM);

        // Then
        assertEquals(
                getAddresses(List.of(rivaDelGarda, malcesine)),
                getAddresses(osrmRoute));
    }
}