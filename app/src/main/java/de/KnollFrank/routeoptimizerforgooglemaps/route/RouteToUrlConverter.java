package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;

public class RouteToUrlConverter {

    // FK-TODO: refactor
    public static URL getUrl(final Route route) {
        if (route.stops().size() < 2) {
            throw new IllegalArgumentException("Route must have at least an origin and a destination.");
        }
        final StringBuilder urlBuilder = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        appendPoint(urlBuilder, "origin", route.getOrigin());
        appendPoint(urlBuilder, "destination", route.getDestination());
        appendWaypoints(urlBuilder, route);
        return URLs.createUrl(urlBuilder.toString());
    }

    private static void appendPoint(final StringBuilder sb,
                                    final String prefix,
                                    final Stop stop) {
        sb
                .append("&")
                .append(prefix)
                .append("=")
                .append(formatStop(stop));
        stop
                .placeId()
                .ifPresent(
                        placeId ->
                                sb
                                        .append("&")
                                        .append(prefix)
                                        .append("_place_id=")
                                        .append(placeId));
    }

    // FK-TODO: refactor
    private static void appendWaypoints(final StringBuilder urlBuilder, final Route route) {
        if (route.stops().size() > 2) {
            final List<Stop> waypoints = route.getWaypoints();
            urlBuilder
                    .append("&waypoints=")
                    .append(formatStops(waypoints));
            if (waypoints.stream().anyMatch(waypoint -> waypoint.placeId().isPresent())) {
                final String waypointPlaceIds =
                        waypoints
                                .stream()
                                .map(stop -> stop.placeId().orElse(""))
                                .collect(Collectors.joining("|"));
                urlBuilder
                        .append("&waypoint_place_ids=")
                        .append(waypointPlaceIds);
            }
        }
    }

    private static String formatStop(final Stop stop) {
        return stop.placeId().isPresent() ?
                URLs.encode(stop.address()) :
                formatCoordinates(stop);
    }

    private static String formatStops(final List<Stop> stops) {
        return stops
                .stream()
                .map(RouteToUrlConverter::formatStop)
                .collect(Collectors.joining("|"));
    }

    private static String formatCoordinates(final Stop stop) {
        return format(stop.geodetic().getLatitude()) + "," + format(stop.geodetic().getLongitude());
    }

    private static String format(final Angle angle) {
        final DecimalFormat df = new DecimalFormat("#.#######", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(angle.toDegrees());
    }
}
