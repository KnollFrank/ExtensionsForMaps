package de.KnollFrank.routeoptimizerforgooglemaps.route;

import com.google.common.collect.ImmutableList;

import java.util.List;

public record Route(Stop origin, List<Stop> waypoints, Stop destination) {

    public List<Stop> stops() {
        return ImmutableList
                .<Stop>builder()
                .add(origin)
                .addAll(waypoints)
                .add(destination)
                .build();
    }
}
