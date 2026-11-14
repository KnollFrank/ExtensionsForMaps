package de.knollfrank.extensionsformaps.repository;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.knollfrank.extensionsformaps.db.AppDatabase;
import de.knollfrank.extensionsformaps.db.RouteMetadataEntity;
import de.knollfrank.extensionsformaps.db.StopDao;
import de.knollfrank.extensionsformaps.db.StopEntity;
import de.knollfrank.extensionsformaps.route.Route;
import de.knollfrank.extensionsformaps.route.Stop;

public class AccumulatedStopsRepository {

    private final StopDao stopDao;

    public AccumulatedStopsRepository(final Context context) {
        this(AppDatabase.getInstance(context).stopDao());
    }

    public AccumulatedStopsRepository(final StopDao stopDao) {
        this.stopDao = stopDao;
    }

    public synchronized void addStops(final List<Stop> newStops) {
        if (newStops == null || newStops.isEmpty()) {
            return;
        }

        final List<StopEntity> existingEntities = stopDao.getAllStopsOrdered();
        final List<Stop> existingStops = existingEntities.stream().map(StopEntity::toStop).toList();

        final List<Stop> stopsToAdd = new ArrayList<>();
        for (final Stop newStop : newStops) {
            final Stop lastStop = !stopsToAdd.isEmpty() ?
                    stopsToAdd.get(stopsToAdd.size() - 1) :
                    (!existingStops.isEmpty() ? existingStops.get(existingStops.size() - 1) : null);

            if (lastStop == null || !isSameStop(lastStop, newStop)) {
                stopsToAdd.add(newStop);
            }
        }

        if (stopsToAdd.isEmpty()) {
            return;
        }

        int currentOrder = existingEntities.size();
        final List<StopEntity> entitiesToInsert = new ArrayList<>();
        for (final Stop stop : stopsToAdd) {
            entitiesToInsert.add(StopEntity.fromStop(stop, currentOrder++));
        }

        stopDao.insertAll(entitiesToInsert);
        setSorted(false);
    }

    public synchronized void saveSortedRoute(final List<Stop> sortedStops) {
        if (sortedStops == null || sortedStops.isEmpty()) {
            return;
        }
        stopDao.deleteAll();
        int currentOrder = 0;
        final List<StopEntity> entitiesToInsert = new ArrayList<>();
        for (final Stop stop : sortedStops) {
            entitiesToInsert.add(StopEntity.fromStop(stop, currentOrder++));
        }
        stopDao.insertAll(entitiesToInsert);
        setSorted(true);
    }

    public synchronized List<Stop> getAccumulatedStops() {
        return stopDao
                .getAllStopsOrdered()
                .stream()
                .map(StopEntity::toStop)
                .toList();
    }

    public synchronized Optional<Route> getAccumulatedRoute() {
        final List<Stop> stops = getAccumulatedStops();
        if (stops.size() < 2) {
            return Optional.empty();
        }
        final Stop origin = stops.get(0);
        final List<Stop> waypoints = stops.subList(1, stops.size() - 1);
        final Stop destination = stops.get(stops.size() - 1);
        return Optional.of(new Route(origin, waypoints, destination));
    }

    public synchronized void clearAccumulatedStops() {
        stopDao.deleteAll();
        stopDao.deleteMetadata();
    }

    public synchronized boolean hasAccumulatedStops() {
        return getAccumulatedStopsCount() > 0;
    }

    public synchronized boolean isSorted() {
        final Boolean sorted = stopDao.isSorted();
        return Boolean.TRUE.equals(sorted);
    }

    public synchronized int getAccumulatedStopsCount() {
        return stopDao.getCount();
    }

    private void setSorted(final boolean isSorted) {
        stopDao.setMetadata(new RouteMetadataEntity(isSorted));
    }

    private boolean isSameStop(final Stop s1, final Stop s2) {
        if (s1.address() != null && s1.address().equalsIgnoreCase(s2.address())) {
            return true;
        }
        return s1.geodetic().equals(s2.geodetic());
    }
}
