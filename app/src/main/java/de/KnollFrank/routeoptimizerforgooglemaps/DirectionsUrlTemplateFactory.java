package de.KnollFrank.routeoptimizerforgooglemaps;

import android.content.Context;

import java.net.URL;
import java.util.List;
import java.util.stream.IntStream;

import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Geodetic;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Unit;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Route;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteFactory;
import de.KnollFrank.routeoptimizerforgooglemaps.route.RouteToUrlConverter;
import de.KnollFrank.routeoptimizerforgooglemaps.route.Stop;

public class DirectionsUrlTemplateFactory {

    // FK-TODO: make context an instance variable
    public static URL createDirectionsUrlTemplate(final Context context, final Geodetic base, int totalStops) {
        return RouteToUrlConverter.getUrl(
                createRoute(
                        context,
                        base,
                        Geodetic.fromLatitudeLongitude(
                                new Angle(0.0005, Unit.DEGREES),
                                new Angle(0.0003, Unit.DEGREES)),
                        totalStops));
    }

    private static Route createRoute(final Context context, final Geodetic base, final Geodetic shift, final int totalStops) {
        if (totalStops < 2) {
            throw new IllegalArgumentException("totalStops: " + totalStops);
        }
        return RouteFactory.createRoute(createStops(context, base, shift, totalStops));
    }

    private static List<Stop> createStops(final Context context, final Geodetic base, final Geodetic shift, final int totalStops) {
        return IntStream
                .range(0, totalStops)
                .mapToObj(
                        i ->
                                new Stop(
                                        "" + (i + 1),
                                        context.getString(R.string.waypoint_name_template, i + 1),
                                        base.add(shift.mul(i))))
                .toList();
    }
}
