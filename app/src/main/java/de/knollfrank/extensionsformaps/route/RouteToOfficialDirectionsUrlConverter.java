package de.knollfrank.extensionsformaps.route;

import android.net.Uri;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import de.knollfrank.extensionsformaps.common.URLs;
import de.knollfrank.extensionsformaps.coordinate.Angle;
import de.knollfrank.extensionsformaps.coordinate.Geodetic;
import de.knollfrank.extensionsformaps.route.url.OfficialDirectionsUrl;

class RouteToOfficialDirectionsUrlConverter {

    public static Optional<OfficialDirectionsUrl> getOfficialDirectionsUrl(final Route route) {
        return route.stops().size() <= 10 ?
                Optional.of(_getOfficialUrl(route)) :
                Optional.empty();
    }

    private static OfficialDirectionsUrl _getOfficialUrl(final Route route) {
        final Uri.Builder builder = createUriBuilder();
        appendPoint(builder, "origin", route.origin());
        appendPoint(builder, "destination", route.destination());
        appendWaypoints(builder, route.waypoints());
        return new OfficialDirectionsUrl(URLs.createUrl(builder.build().toString()));
    }

    private static Uri.Builder createUriBuilder() {
        return new Uri
                .Builder()
                .scheme("https")
                .authority("www.google.com")
                .path("/maps/dir/")
                .appendQueryParameter("api", "1");
    }

    private static void appendPoint(final Uri.Builder builder, final String key, final Stop stop) {
        builder.appendQueryParameter(key, formatStop(stop));
        stop.officialPlaceId().ifPresent(officialPlaceId -> builder.appendQueryParameter(key + "_place_id", officialPlaceId.value()));
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
        return stops.stream().anyMatch(stop -> stop.officialPlaceId().isPresent());
    }

    private static String formatPlaceIds(final List<Stop> stops) {
        return stops
                .stream()
                .map(RouteToOfficialDirectionsUrlConverter::formatPlaceId)
                .collect(Collectors.joining("|"));
    }

    private static String formatPlaceId(final Stop stop) {
        return stop
                .officialPlaceId()
                .map(OfficialPlaceId::value)
                .orElse("");
    }

    private static String formatStops(final List<Stop> stops) {
        return stops
                .stream()
                .map(RouteToOfficialDirectionsUrlConverter::formatStop)
                .collect(Collectors.joining("|"));
    }

    private static String formatStop(final Stop stop) {
        return stop.officialPlaceId().isPresent() ?
                stop.address() :
                format(stop.geodetic());
    }

    private static String format(final Geodetic geodetic) {
        return format(geodetic.getLatitude()) + "," + format(geodetic.getLongitude());
    }

    public static String format(final Angle angle) {
        final DecimalFormat df = new DecimalFormat("#.#######", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(angle.toDegrees());
    }
}
