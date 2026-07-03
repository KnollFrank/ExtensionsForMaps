package de.KnollFrank.routeoptimizerforgooglemaps.optimize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.Range;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;
import de.KnollFrank.routeoptimizerforgooglemaps.route.DeliveryGroup;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

@RunWith(RobolectricTestRunner.class)
public class RouteOptimizerTest {

    // FK-TODO: refactor
    @Test
    public void testOptimize_supportsManyGroups() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        // Dieser Test verifiziert, dass mehr als 10 Gruppen unterstützt werden (durch UserData)
        final int numGroups = 15;
        final List<Stop> waypoints = new ArrayList<>();
        // Wir erstellen 15 Stopps in umgekehrter Reihenfolge ihrer geografischen Nähe,
        // aber mit aufsteigender Gruppen-Sequenz.
        {
            final Geodetic berlin =
                    Geodetic
                            .fromLatitudeLongitude(
                                    new Angle(52.52, Unit.DEGREES),
                                    new Angle(13.40, Unit.DEGREES));
            for (int i = 1; i <= numGroups; i++) {
                // Je kleiner i, desto weiter weg, aber desto wichtiger die Gruppe
                final Angle _offset = new Angle((numGroups - i) * 0.01, Unit.DEGREES);
                final Geodetic offset = Geodetic.fromLatitudeLongitude(_offset, _offset);
                waypoints.add(
                        new Stop(
                                "s" + i,
                                "Addr" + i,
                                Optional.empty(),
                                berlin.add(offset),
                                Optional.of(new DeliveryGroup("id" + i, "Group " + i, i)),
                                Optional.empty()));
            }
        }
        // Route: Berlin -> [S15 (nah, Prio 15) ... S1 (fern, Prio 1)] -> Berlin
        // Erwartung: S1 zuerst, S15 zuletzt (wegen sequenceOrder 1 bis 15)
        final Stop berlin =
                new Stop("0",
                         "Berlin",
                         Optional.empty(),
                         Geodetic.fromLatitudeLongitude(
                                 new Angle(52.52, Unit.DEGREES),
                                 new Angle(13.40, Unit.DEGREES)),
                         Optional.empty(),
                         Optional.empty());
        final Route route = new Route(berlin, waypoints, berlin);

        // When
        final Route optimizedRoute = routeOptimizer.optimize(route);

        // Then
        for (int i = 0; i < numGroups; i++) {
            assertEquals("s" + (i + 1), optimizedRoute.waypoints().get(i).id());
        }
    }

    @Test
    public void testOptimize_respectsTimeWindows() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());

        // Given: Berlin start.
        // Stop A (farther): early window. 
        // Stop B (closer): late window.
        final Stop berlin =
                new Stop("0",
                         "Berlin",
                         Optional.empty(),
                         Geodetic.fromLatitudeLongitude(
                                 new Angle(52.52, Unit.DEGREES),
                                 new Angle(13.40, Unit.DEGREES)),
                         Optional.empty(),
                         Optional.empty());

        final Stop far_early =
                new Stop(
                        "1",
                        "FarEarly",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.62, Unit.DEGREES),
                                new Angle(13.50, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.of(
                                Range.closed(
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(8, 0)),
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(10, 0)))));
        final Stop close_late =
                new Stop(
                        "2",
                        "CloseLate",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.55, Unit.DEGREES),
                                new Angle(13.45, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.of(
                                Range.closed(
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(14, 0)),
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(16, 0)))));
        final Route route =
                new Route(
                        berlin,
                        List.of(close_late, far_early),
                        berlin);

        // When
        final Route optimizedRoute = routeOptimizer.optimize(route);

        // Then: Should visit FarEarly FIRST to satisfy window, even if it's farther
        assertEquals(
                new Route(
                        berlin,
                        List.of(far_early, close_late),
                        berlin),
                optimizedRoute);
    }

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
                                new Angle(13.4050, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.empty());
        final Stop munich_far =
                new Stop(
                        "1",
                        "Munich",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.1351, Unit.DEGREES),
                                new Angle(11.5820, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.empty());
        final Stop potsdam_very_close =
                new Stop(
                        "2",
                        "Potsdam",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.3906, Unit.DEGREES),
                                new Angle(13.0645, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.empty());
        final Stop leipzig_medium =
                new Stop(
                        "3",
                        "Leipzig",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(51.3397, Unit.DEGREES),
                                new Angle(12.3731, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.empty());
        final Route route =
                new Route(
                        berlin_origin_destination,
                        List.of(munich_far, potsdam_very_close, leipzig_medium),
                        berlin_origin_destination);

        // When
        final Route optimizedRoute = routeOptimizer.optimize(route);

        // Then
        assertEquals(
                new Route(
                        berlin_origin_destination,
                        List.of(munich_far, leipzig_medium, potsdam_very_close),
                        berlin_origin_destination),
                optimizedRoute);
    }

    @Test
    public void testOptimize_respectsGroupSequence() throws Exception {
        // Given: Kernstadt (Group 1), Dörfer (Group 2)
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        // Kernstadt (Group 1), Dörfer (Group 2)
        final DeliveryGroup kernstadt =
                new DeliveryGroup(
                        "ks",
                        "Kernstadt",
                        1);
        final DeliveryGroup doerfer =
                new DeliveryGroup(
                        "df",
                        "Dörfer",
                        2);
        final Stop berlin =
                new Stop("0",
                         "Berlin",
                         Optional.empty(),
                         Geodetic.fromLatitudeLongitude(
                                 new Angle(52.52, Unit.DEGREES),
                                 new Angle(13.40, Unit.DEGREES)),
                         Optional.empty(),
                         Optional.empty());
        // Dorf is closer to Berlin than Stadt
        final Stop dorf_close =
                new Stop(
                        "1",
                        "Dorf",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.53, Unit.DEGREES),
                                new Angle(13.41, Unit.DEGREES)),
                        Optional.of(doerfer),
                        Optional.empty());
        final Stop stadt_far =
                new Stop(
                        "2",
                        "Stadt",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.60, Unit.DEGREES),
                                new Angle(13.50, Unit.DEGREES)),
                        Optional.of(kernstadt),
                        Optional.empty());
        final Route route =
                new Route(
                        berlin,
                        List.of(dorf_close, stadt_far),
                        berlin);

        // When
        final Route optimizedRoute = routeOptimizer.optimize(route);

        // Then: Should visit Stadt FIRST because it's in Group 1, even if Dorf is closer
        assertEquals(
                new Route(
                        berlin,
                        List.of(stadt_far, dorf_close),
                        berlin),
                optimizedRoute);
    }

    @Test
    public void testOptimize_breaksGroupSequenceForTimeWindows() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());

        // Requirement 4: Time Windows > Zones
        final DeliveryGroup kernstadt =
                new DeliveryGroup(
                        "ks",
                        "Kernstadt",
                        1);
        final DeliveryGroup doerfer =
                new DeliveryGroup(
                        "df",
                        "Dörfer",
                        2);
        final Stop berlin =
                new Stop("0",
                         "Berlin",
                         Optional.empty(),
                         Geodetic.fromLatitudeLongitude(
                                 new Angle(52.52, Unit.DEGREES),
                                 new Angle(13.40, Unit.DEGREES)),
                         Optional.empty(),
                         Optional.empty());
        // Kernstadt stop has LATE window
        final Stop stadt_late =
                new Stop(
                        "1",
                        "Stadt",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.60, Unit.DEGREES),
                                new Angle(13.50, Unit.DEGREES)),
                        Optional.of(kernstadt),
                        Optional.of(
                                Range.closed(
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(15, 0)),
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(17, 0)))));
        // Dorf stop has EARLY window
        final Stop dorf_early =
                new Stop(
                        "2",
                        "Dorf",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.53, Unit.DEGREES),
                                new Angle(13.41, Unit.DEGREES)),
                        Optional.of(doerfer),
                        Optional.of(
                                Range.closed(
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(8, 0)),
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(10, 0)))));
        final Route route =
                new Route(
                        berlin,
                        List.of(stadt_late, dorf_early),
                        berlin);

        // When
        final Route optimizedRoute = routeOptimizer.optimize(route);

        // Then: Should visit Dorf FIRST to satisfy time window, breaking group sequence
        assertEquals(
                new Route(
                        berlin,
                        List.of(dorf_early, stadt_late),
                        berlin),
                optimizedRoute);
    }

    @Test
    public void testOptimize_unassignedJobsThrowsException() {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        final Stop berlin =
                new Stop("0",
                         "Berlin",
                         Optional.empty(),
                         Geodetic.fromLatitudeLongitude(
                                 new Angle(52.52, Unit.DEGREES),
                                 new Angle(13.40, Unit.DEGREES)),
                         Optional.empty(),
                         Optional.empty());
        final Stop s1 =
                new Stop(
                        "1",
                        "S1",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.53, Unit.DEGREES),
                                new Angle(13.41, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.of(
                                Range.closed(
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(9, 0)),
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(9, 1))))); // 1 min window
        final Stop s2 =
                new Stop(
                        "2",
                        "S2",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(53.53, Unit.DEGREES),
                                new Angle(14.41, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.of(
                                Range.closed(
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(9, 0)),
                                        LocalDateTime.of(LocalDate.EPOCH, LocalTime.of(9, 1))))); // Same window, but far away
        final Route route =
                new Route(
                        berlin,
                        List.of(s1, s2),
                        berlin);

        // When / Then
        assertThrows(
                IllegalStateException.class,
                () -> routeOptimizer.optimize(route));
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
                                new OsrmRoutingMatrixProvider()));
        // Geografisches Hindernis: Der Gardasee (Lago di Garda) in Italien
        // Start: Limone sul Garda (am Westufer des Sees)
        final Stop start_LimoneSulGarda_west =
                new Stop(
                        "0",
                        "Limone sul Garda",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(45.8156, Unit.DEGREES),
                                new Angle(10.7904, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.empty());
        // Stop A: Malcesine
        // (Liegt am Ostufer, genau gegenüber von Limone. Luftlinie: ~6 km. Auto: ~30 km, da man um den See fahren muss)
        final Stop malcesine_east =
                new Stop(
                        "1",
                        "Malcesine",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(45.7656, Unit.DEGREES),
                                new Angle(10.8092, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.empty());
        // Stop B: Riva del Garda
        // (Liegt an der Nordspitze. Luftlinie: ~9 km. Auto: ~11 km, da auf derselben Uferseite über direkte Straße erreichbar)
        final Stop rivaDelGarda_north =
                new Stop(
                        "2",
                        "Riva del Garda",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(45.8893, Unit.DEGREES),
                                new Angle(10.8430, Unit.DEGREES)),
                        Optional.empty(),
                        Optional.empty());

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
                // FK-TODO: RouteOptimizer soll den doppelten Stop malcesine_east aus der optimierten Route (waypoints) entfernen, d.h. die Route normalisieren: wenn zwei direkt aufeinander folgende Stops gleich sind, dann einen der beiden entfernen.
                new Route(
                        start_LimoneSulGarda_west,
                        List.of(rivaDelGarda_north, malcesine_east),
                        malcesine_east),
                osrmRoute);
    }
}
