package de.KnollFrank.routeoptimizerforgooglemaps;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

public class DirectionsUrlTemplateFactory {

    public static URL createDirectionsUrlTemplate(final Geodetic base, int totalStops) {
        return RouteToUrlConverter.getUrl(
                createRoute(
                        base,
                        Geodetic.fromLatitudeLongitude(
                                new Angle(0.0005, Unit.DEGREES),
                                new Angle(0.0003, Unit.DEGREES)),
                        totalStops));
    }

    private static Route createRoute(final Geodetic base, final Geodetic shift, final int totalStops) {
        if (totalStops < 2) {
            throw new IllegalArgumentException("totalStops: " + totalStops);
        }
        final List<Stop> stops = createStops(base, shift, totalStops);
        return new Route(
                stops.get(0),
                stops.subList(1, stops.size() - 1),
                stops.get(stops.size() - 1));
    }

    private static List<Stop> createStops(final Geodetic base, final Geodetic shift, final int totalStops) {
        return IntStream
                .range(0, totalStops)
                .mapToObj(
                        i ->
                                new Stop(
                                        "" + (i + 1),
                                        String.format(Locale.US, "Wegpunkt %d", i + 1),
                                        base.add(shift.mul(i))))
                .toList();
    }
}
