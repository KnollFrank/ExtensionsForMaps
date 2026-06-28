package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.List;

public class RouteFactory {

    public static Route createRoute(final List<Stop> stops) {
        if (stops.size() < 2) {
            throw new IllegalArgumentException("Route must have at least an origin and a destination.");
        }
        return new Route(
                stops.get(0),
                stops.subList(1, stops.size() - 1),
                stops.get(stops.size() - 1));
    }
}
