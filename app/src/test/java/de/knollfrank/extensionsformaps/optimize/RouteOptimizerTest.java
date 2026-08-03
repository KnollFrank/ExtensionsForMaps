package de.knollfrank.extensionsformaps.optimize;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;
import de.knollfrank.extensionsformaps.coordinate.Unit;
import de.knollfrank.extensionsformaps.optimize.osrm.OsrmRoutingMatrixProvider;
import de.knollfrank.extensionsformaps.route.DeliveryGroup;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

@RunWith(RobolectricTestRunner.class)
public class RouteOptimizerTest {

    @Test
    public void testOptimize_reproduceUserScenario_HechingenBeforeNoGroupWurmlingen() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        // Scenario: Hechingen is Group 1 (Stadt), Wurmlingen has NO GROUP (Optional.empty)
        // Tübingen -> Wurmlingen (No Group) -> Hechingen (Group 1) -> Tübingen
        final Stop tuebingen =
                new Stop(
                        "0",
                        "Tuebingen",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.5015, Unit.DEGREES),
                                new Angle(8.9932, Unit.DEGREES)),
                        Optional.empty());
        final DeliveryGroup kernstadt = new DeliveryGroup("ks", 1);
        // Hechingen is further away (~16km)
        final Stop hechingen_stadt =
                new Stop(
                        "1",
                        "Hechingen",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.3539, Unit.DEGREES),
                                new Angle(8.9614, Unit.DEGREES)),
                        Optional.of(kernstadt));
        // Wurmlingen has NO group
        final Stop wurmlingen_no_group =
                new Stop(
                        "2",
                        "Wurmlingen",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.5030, Unit.DEGREES),
                                new Angle(8.9625, Unit.DEGREES)),
                        Optional.empty());
        final Route route =
                new Route(
                        tuebingen,
                        List.of(wurmlingen_no_group, hechingen_stadt),
                        tuebingen);

        // When
        final Route optimizedRoute = routeOptimizer.optimize(route);

        // Then: Even if Wurmlingen has no group, Hechingen should be FIRST because it HAS a group with low sequence order.
        assertEquals(
                new Route(
                        tuebingen,
                        List.of(hechingen_stadt, wurmlingen_no_group),
                        tuebingen),
                optimizedRoute);
    }

    @Test
    public void testOptimize_reproduceUserScenario_HechingenBeforeWurmlingen() throws Exception {
        // Given: Tübingen start/end, Hechingen (Group 1 - Kernstadt), Wurmlingen (Group 2 - Dorf)
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        final Stop tuebingen =
                new Stop(
                        "0",
                        "Tuebingen",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.5015, Unit.DEGREES),
                                new Angle(8.9932, Unit.DEGREES)),
                        Optional.empty());
        final DeliveryGroup kernstadt = new DeliveryGroup("ks", 1);
        final DeliveryGroup dorf = new DeliveryGroup("df", 2);
        // Hechingen is further away (~16km)
        final Stop hechingen_stadt =
                new Stop(
                        "1",
                        "Hechingen",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.3539, Unit.DEGREES),
                                new Angle(8.9614, Unit.DEGREES)),
                        Optional.of(kernstadt));
        // Wurmlingen is very close (~2.5km)
        final Stop wurmlingen_dorf =
                new Stop(
                        "2",
                        "Wurmlingen",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.5030, Unit.DEGREES),
                                new Angle(8.9625, Unit.DEGREES)),
                        Optional.of(dorf));
        final Route route =
                new Route(
                        tuebingen,
                        List.of(wurmlingen_dorf, hechingen_stadt),
                        tuebingen);

        // When
        final Route optimizedRoute = routeOptimizer.optimize(route);

        // Then: Should visit Hechingen FIRST because it's in Group 1, even if Wurmlingen is much closer
        assertEquals(
                new Route(
                        tuebingen,
                        List.of(hechingen_stadt, wurmlingen_dorf),
                        tuebingen),
                optimizedRoute);
    }

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
                                Optional.of(
                                        new DeliveryGroup(
                                                "id" + i,
                                                i))));
            }
        }
        // Route: Berlin -> [S15 (nah, Prio 15) ... S1 (fern, Prio 1)] -> Berlin
        // Erwartung: S1 zuerst, S15 zuletzt (wegen sequenceOrder 1 bis 15)
        final Stop berlin =
                new Stop(
                        "0",
                        "Berlin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.52, Unit.DEGREES),
                                new Angle(13.40, Unit.DEGREES)),
                        Optional.empty());
        final Route route = new Route(berlin, waypoints, berlin);

        // When
        final Route optimizedRoute = routeOptimizer.optimize(route);

        // Then
        for (int i = 0; i < numGroups; i++) {
            assertEquals("s" + (i + 1), optimizedRoute.waypoints().get(i).id());
        }
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
                        Optional.empty());
        final Stop munich_far =
                new Stop(
                        "1",
                        "Munich",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.1351, Unit.DEGREES),
                                new Angle(11.5820, Unit.DEGREES)),
                        Optional.empty());
        final Stop potsdam_very_close =
                new Stop(
                        "2",
                        "Potsdam",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.3906, Unit.DEGREES),
                                new Angle(13.0645, Unit.DEGREES)),
                        Optional.empty());
        final Stop leipzig_medium =
                new Stop(
                        "3",
                        "Leipzig",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(51.3397, Unit.DEGREES),
                                new Angle(12.3731, Unit.DEGREES)),
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
    public void testOptimize_anyDestination() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        // Start: Berlin
        // Waypoint: Potsdam (Close)
        // Destination: Munich (Far)
        final Stop berlin =
                new Stop(
                        "0",
                        "Berlin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.5200, Unit.DEGREES),
                                new Angle(13.4050, Unit.DEGREES)),
                        Optional.empty());
        final Stop potsdam =
                new Stop(
                        "1",
                        "Potsdam",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.3906, Unit.DEGREES),
                                new Angle(13.0645, Unit.DEGREES)),
                        Optional.empty());
        final Stop munich =
                new Stop(
                        "2",
                        "Munich",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.1351, Unit.DEGREES),
                                new Angle(11.5820, Unit.DEGREES)),
                        Optional.empty());
        final Route route = new Route(berlin, List.of(potsdam), munich);

        // When: ANY_DESTINATION
        final Route optimizedAny = routeOptimizer.optimize(route, OptimizationType.ANY_DESTINATION);

        // Then: Should end at Munich (it's the furthest from Berlin)
        // Order: Berlin -> Potsdam -> Munich
        assertEquals(munich, optimizedAny.destination());
        assertEquals(potsdam, optimizedAny.waypoints().get(0));

        // When: Swap Munich and Potsdam in input, but still ANY_DESTINATION
        final Route routeSwapped = new Route(berlin, List.of(munich), potsdam);
        final Route optimizedAnySwapped = routeOptimizer.optimize(routeSwapped, OptimizationType.ANY_DESTINATION);

        // Then: Should still end at Munich (the furthest)
        // Order: Berlin -> Potsdam -> Munich
        assertEquals(munich, optimizedAnySwapped.destination());
        assertEquals(potsdam, optimizedAnySwapped.waypoints().get(0));
    }

    @Test
    public void testOptimize_fixedDestination() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        // Start: Berlin
        // Waypoint: Munich (Far)
        // Destination: Potsdam (Close)
        final Stop berlin =
                new Stop(
                        "0",
                        "Berlin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.5200, Unit.DEGREES),
                                new Angle(13.4050, Unit.DEGREES)),
                        Optional.empty());
        final Stop potsdam =
                new Stop(
                        "1",
                        "Potsdam",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.3906, Unit.DEGREES),
                                new Angle(13.0645, Unit.DEGREES)),
                        Optional.empty());
        final Stop munich =
                new Stop(
                        "2",
                        "Munich",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(48.1351, Unit.DEGREES),
                                new Angle(11.5820, Unit.DEGREES)),
                        Optional.empty());
        final Route route = new Route(berlin, List.of(munich), potsdam);

        // When: FIXED_DESTINATION (Default)
        final Route optimizedFixed = routeOptimizer.optimize(route, OptimizationType.FIXED_DESTINATION);

        // Then: Destination MUST remain Potsdam
        assertEquals(potsdam, optimizedFixed.destination());
        assertEquals(munich, optimizedFixed.waypoints().get(0));
    }

    @Test
    public void testOptimize_respectsGroupSequence() throws Exception {
        // Given
        final RouteOptimizer routeOptimizer =
                new RouteOptimizer(
                        new HaversineVehicleRoutingTransportCostsProvider());
        final DeliveryGroup first = new DeliveryGroup("TOWN", 1);
        final DeliveryGroup second = new DeliveryGroup("VILLAGE", 2);
        final Stop berlin =
                new Stop(
                        "0",
                        "Berlin",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.52, Unit.DEGREES),
                                new Angle(13.40, Unit.DEGREES)),
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
                        Optional.of(second));
        final Stop stadt_far =
                new Stop(
                        "2",
                        "Stadt",
                        Optional.empty(),
                        Geodetic.fromLatitudeLongitude(
                                new Angle(52.60, Unit.DEGREES),
                                new Angle(13.50, Unit.DEGREES)),
                        Optional.of(first));
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
