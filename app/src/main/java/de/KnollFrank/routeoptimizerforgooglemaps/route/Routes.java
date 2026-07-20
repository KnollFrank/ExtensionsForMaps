package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.List;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;

public class Routes {

    private Routes() {
    }

    public static Route addDummyStop(final Route route) {
        return RouteFactory.createRoute(addDummyStop(route.stops()));
    }

    private static List<Stop> addDummyStop(final List<Stop> stops) {
        return Lists.concat(
                stops,
                getSecondToLastElement(stops));
    }

    private static <T> T getSecondToLastElement(final List<T> ts) {
        return ts.get(ts.size() - 2);
    }
}
