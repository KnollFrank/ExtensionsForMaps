package de.knollfrank.extensionsformaps.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;
import de.knollfrank.extensionsformaps.coordinate.Unit;
import de.knollfrank.extensionsformaps.db.AppDatabase;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

@RunWith(RobolectricTestRunner.class)
public class AccumulatedStopsRepositoryTest {

    private AppDatabase db;
    private AccumulatedStopsRepository repository;

    @Before
    public void setUp() {
        db = Room
                .inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        repository = new AccumulatedStopsRepository(db.stopDao());
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testAddAndGetStops() {
        assertFalse(repository.hasAccumulatedStops());

        final Stop s1 = createStop("1", "Address 1", 10.0, 20.0);
        final Stop s2 = createStop("2", "Address 2", 15.0, 25.0);
        final Stop s3 = createStop("3", "Address 3", 20.0, 30.0);

        repository.addStops(List.of(s1, s2, s3));

        assertTrue(repository.hasAccumulatedStops());
        assertEquals(3, repository.getAccumulatedStopsCount());

        final List<Stop> stops = repository.getAccumulatedStops();
        assertEquals("Address 1", stops.get(0).address());
        assertEquals("Address 2", stops.get(1).address());
        assertEquals("Address 3", stops.get(2).address());

        final Optional<Route> routeOpt = repository.getAccumulatedRoute();
        assertTrue(routeOpt.isPresent());
        final Route route = routeOpt.get();
        assertEquals("Address 1", route.origin().address());
        assertEquals(1, route.waypoints().size());
        assertEquals("Address 2", route.waypoints().get(0).address());
        assertEquals("Address 3", route.destination().address());
    }

    @Test
    public void testAccumulateMultipleSharesAndDeduplicateConsecutiveBoundary() {
        final Stop s1 = createStop("1", "Address 1", 10.0, 20.0);
        final Stop s2 = createStop("2", "Address 2", 15.0, 25.0);

        repository.addStops(List.of(s1, s2));

        final Stop s2Dup = createStop("2", "Address 2", 15.0, 25.0);
        final Stop s3 = createStop("3", "Address 3", 20.0, 30.0);

        repository.addStops(List.of(s2Dup, s3));

        assertEquals(3, repository.getAccumulatedStopsCount());
        final List<Stop> stops = repository.getAccumulatedStops();
        assertEquals("Address 1", stops.get(0).address());
        assertEquals("Address 2", stops.get(1).address());
        assertEquals("Address 3", stops.get(2).address());
    }

    @Test
    public void testClearStops() {
        repository.addStops(List.of(createStop("1", "A1", 1.0, 1.0)));
        assertTrue(repository.hasAccumulatedStops());

        repository.clearAccumulatedStops();
        assertFalse(repository.hasAccumulatedStops());
        assertEquals(0, repository.getAccumulatedStopsCount());
    }

    @Test
    public void testAccumulating13StopsWithDuplicateIDsHasUniqueIDs() {
        final List<Stop> share1 = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            share1.add(createStop("" + i, "Address " + i, 10.0 + i, 20.0 + i));
        }
        repository.addStops(share1);

        final List<Stop> share2 = new java.util.ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            share2.add(createStop("" + i, "Address B " + i, 50.0 + i, 60.0 + i));
        }
        repository.addStops(share2);

        assertEquals(13, repository.getAccumulatedStopsCount());
        final Route route = repository.getAccumulatedRoute().orElseThrow();
        assertEquals(13, route.stops().size());

        final long uniqueIdCount = route.stops().stream().map(Stop::id).distinct().count();
        assertEquals(13, uniqueIdCount);
    }

    private Stop createStop(final String id, final String address, final double lat, final double lon) {
        return new Stop(
                id,
                address,
                Optional.empty(),
                Geodetic.fromLatitudeLongitude(
                        new Angle(lat, Unit.DEGREES),
                        new Angle(lon, Unit.DEGREES)),
                Optional.empty());
    }
}
