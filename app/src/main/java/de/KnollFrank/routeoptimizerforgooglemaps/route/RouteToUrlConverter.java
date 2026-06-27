package de.KnollFrank.routeoptimizerforgooglemaps.route;

import android.net.Uri;

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
        final Uri.Builder builder = creaetUriBuilder();
        appendPoint(builder, "origin", route.getOrigin());
        appendPoint(builder, "destination", route.getDestination());
        appendWaypoints(builder, route.getWaypoints());
        return URLs.createUrl(builder.build().toString());
    }

    private static Uri.Builder creaetUriBuilder() {
        return new Uri
                .Builder()
                .scheme("https")
                .authority("www.google.com")
                .path("/maps/dir/")
                .appendQueryParameter("api", "1");
    }

    private static void appendPoint(final Uri.Builder builder, final String key, final Stop stop) {
        builder.appendQueryParameter(key, formatStop(stop));
        stop.placeId().ifPresent(placeId -> builder.appendQueryParameter(key + "_place_id", placeId));
    }

    private static void appendWaypoints(final Uri.Builder builder, final List<Stop> waypoints) {
        if (waypoints.isEmpty()) {
            return;
        }
        builder.appendQueryParameter("waypoints", formatStops(waypoints));
        if (hasAnyPlaceId(waypoints)) {
            builder.appendQueryParameter("waypoint_place_ids", formatPlaceIds(waypoints));
        }
    }

    private static boolean hasAnyPlaceId(final List<Stop> stops) {
        return stops.stream().anyMatch(stop -> stop.placeId().isPresent());
    }

    private static String formatPlaceIds(final List<Stop> stops) {
        return stops
                .stream()
                .map(stop -> stop.placeId().orElse(""))
                .collect(Collectors.joining("|"));
    }

    private static String formatStop(final Stop stop) {
        return stop.placeId().isPresent() ?
                stop.address() :
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
