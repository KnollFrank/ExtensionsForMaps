package de.knollfrank.extensionsformaps.route;

import java.util.ArrayList;
import java.util.List;

public class RouteSplitter {

    public static final int MAX_STOPS_PER_BLOCK = 10;

    private RouteSplitter() {
    }

    public static List<Route> splitInto10StopBlocks(final Route route) {
        if (route == null) {
            return List.of();
        }
        final List<Stop> stops = route.stops();
        final int totalStops = stops.size();
        if (totalStops <= MAX_STOPS_PER_BLOCK) {
            return List.of(route);
        }

        final List<Route> blocks = new ArrayList<>();
        int start = 0;
        final int step = MAX_STOPS_PER_BLOCK - 1; // 9
        while (start < totalStops - 1) {
            final int end = Math.min(start + step, totalStops - 1);
            final List<Stop> blockStops = stops.subList(start, end + 1);
            final Stop origin = blockStops.get(0);
            final List<Stop> waypoints = blockStops.subList(1, blockStops.size() - 1);
            final Stop destination = blockStops.get(blockStops.size() - 1);
            blocks.add(new Route(origin, waypoints, destination));
            start = end;
        }
        return blocks;
    }
}
