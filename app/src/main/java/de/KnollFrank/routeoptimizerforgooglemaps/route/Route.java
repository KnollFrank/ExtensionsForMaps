package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;

public record Route(List<Stop> stops) {

    public Stop getOrigin() {
        return Lists.getHead(stops).orElseThrow();
    }

    public Stop getDestination() {
        return Lists.getLastElement(stops).orElseThrow();
    }

    public List<Stop> getWaypoints() {
        return stops.subList(1, stops.size() - 1);
    }
}
