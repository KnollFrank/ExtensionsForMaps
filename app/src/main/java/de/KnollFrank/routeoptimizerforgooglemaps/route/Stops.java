package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.List;

public class Stops {

    private Stops() {
    }

    public static List<String> getAddresses(final List<Stop> stops) {
        return stops
                .stream()
                .map(Stop::address)
                .toList();
    }
}
