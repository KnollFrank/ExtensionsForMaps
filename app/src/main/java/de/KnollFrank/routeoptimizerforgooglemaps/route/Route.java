package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;

public record Route(List<Stop> stops) {

    public Route {
        if (stops.size() < 2) {
            throw new IllegalArgumentException("Route must have at least an origin and a destination.");
        }
    }

    public Stop origin() {
        return Lists.getHead(stops).orElseThrow();
    }

    public Stop destination() {
        return Lists.getLastElement(stops).orElseThrow();
    }

    public List<Stop> waypoints() {
        return stops.subList(1, stops.size() - 1);
    }
}
