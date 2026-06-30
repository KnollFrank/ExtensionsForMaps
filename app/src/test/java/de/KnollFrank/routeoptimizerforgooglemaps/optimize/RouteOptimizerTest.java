package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

@RunWith(RobolectricTestRunner.class)
public class RouteOptimizerTest {

    // FK-TOD: ergänze einen Test, der verlangt, dass origin und destination einer zu optimierenden Route immer erhalten bleiben und nur die waypoints der Route in ihrer Reihenfolge geändert werden.
    @Test
    public void testOptimize_sortsByShortestDistance() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        final Stop berlin_origin_destination =
                new Stop(
                        "0",
                        "Berlin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.5200, Unit.DEGREES),
                                new Angle(13.4050, Unit.DEGREES)));
        final Stop munich_far =
                new Stop(
                        "1",
                        "Munich",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.1351, Unit.DEGREES),
                                new Angle(11.5820, Unit.DEGREES)));
        final Stop potsdam_very_close =
                new Stop(
                        "2",
                        "Potsdam",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.3906, Unit.DEGREES),
                                new Angle(13.0645, Unit.DEGREES)));
        final Stop leipzig_medium =
                new Stop(
                        "3",
                        "Leipzig",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(51.3397, Unit.DEGREES),
                                new Angle(12.3731, Unit.DEGREES)));
        final Route route =
                new Route(
                        berlin_origin_destination,
                        List.of(munich_far, potsdam_very_close, leipzig_medium),
                        berlin_origin_destination);

        // When
        final Route optimized = routeOptimizer.optimize(route);

        // Then
        assertEquals(
                new Route(
                        berlin_origin_destination,
                        List.of(munich_far, leipzig_medium, potsdam_very_close),
                        berlin_origin_destination),
                optimized);
    }

    @Test
    public void testOptimize_userBugReproduction_Exact() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        final Stop rottenburg_CentralApotheke =
                new Stop(
                        "0",
                        "Central Apotheke",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.4765345, Unit.DEGREES),
                                new Angle(8.9349008, Unit.DEGREES)));
        final Stop hamburg =
                new Stop(
                        "1",
                        "Hamburg",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(53.548828, Unit.DEGREES),
                                new Angle(9.987170, Unit.DEGREES)));
        final Stop unterhausen =
                new Stop(
                        "2",
                        "Unterhausen",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.430628, Unit.DEGREES),
                                new Angle(9.2546378, Unit.DEGREES)));

        // When
        final Route optimized =
                routeOptimizer.optimize(
                        new Route(
                                rottenburg_CentralApotheke,
                                List.of(hamburg, unterhausen),
                                hamburg));

        // Then
        assertEquals(
                new Route(
                        rottenburg_CentralApotheke,
                        List.of(unterhausen, hamburg),
                        hamburg),
                optimized);
    }

    @Test
    public void testOptimize_osrmVsHaversine_LakeGarda() throws Exception {
        // Given
        final RouteOptimizer haversineRouteOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        final RouteOptimizer osrmRouteOptimizer =
                new RouteOptimizer(
                        new OsrmVehicleRoutingTransportCostsProvider(
                                new OsrmRoutingMatricesProvider()));
        // Geografisches Hindernis: Der Gardasee (Lago di Garda) in Italien
        // Start: Limone sul Garda (am Westufer des Sees)
        final Stop start_LimoneSulGarda_west =
                new Stop(
                        "0",
                        "Limone sul Garda",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(45.8156, Unit.DEGREES),
                                new Angle(10.7904, Unit.DEGREES)));
        // Stop A: Malcesine
        // (Liegt am Ostufer, genau gegenüber von Limone. Luftlinie: ~6 km. Auto: ~30 km, da man um den See fahren muss)
        final Stop malcesine_east =
                new Stop(
                        "1",
                        "Malcesine",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(45.7656, Unit.DEGREES),
                                new Angle(10.8092, Unit.DEGREES)));
        // Stop B: Riva del Garda
        // (Liegt an der Nordspitze. Luftlinie: ~9 km. Auto: ~11 km, da auf derselben Uferseite über direkte Straße erreichbar)
        final Stop rivaDelGarda_north =
                new Stop(
                        "2",
                        "Riva del Garda",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(45.8893, Unit.DEGREES),
                                new Angle(10.8430, Unit.DEGREES)));

        // ==========================================================
        // TEST 1: Haversine-Strategie (Luftlinie ignoriert den See)
        // Erwartung: Limone -> Malcesine (6km) -> Riva del Garda
        // ==========================================================
        // When
        final Route west_east_north_west =
                new Route(
                        start_LimoneSulGarda_west,
                        List.of(malcesine_east, rivaDelGarda_north),
                        start_LimoneSulGarda_west);
        final Route haversineRoute = haversineRouteOptimizer.optimize(west_east_north_west);

        // Then
        assertEquals(west_east_north_west, haversineRoute);

        // ==========================================================
        // TEST 2: OSRM-Strategie (Echte Straßenführung)
        // Erwartung: Limone -> Riva del Garda (11km) -> Malcesine (weitere 19km)
        // ==========================================================
        // FK-TODO: für diesen Unittest bitte kein Internetzugriff, sondern hart codierte RoutingMatrices verwenden.
        // When
        final Route osrmRoute =
                osrmRouteOptimizer.optimize(
                        new Route(
                                start_LimoneSulGarda_west,
                                List.of(malcesine_east, rivaDelGarda_north),
                                malcesine_east));

        // Then
        assertEquals(
                // FK-TODO: RouteOptimizer soll den doppelten Stop malcesine_east aus der optimierten Route (waypoints) entfernen, d.h. die Route Normalisieren: wenn zwei direkt aufeinander folgende Stops gleich sind, dann einen der beiden entfernen.
                new Route(
                        start_LimoneSulGarda_west,
                        List.of(rivaDelGarda_north, malcesine_east),
                        malcesine_east),
                osrmRoute);
    }
}