package de.knollfrank.extensionsformaps.route;

import android.net.Uri;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.knollfrank.extensionsformaps.common.URLs;
import de.knollfrank.extensionsformaps.coordinate.Angle;

public class RouteToUrlConverter {

    // FK-TODO: refactor
    public static URL getUrl(final Route route) {
        if (route.stops().size() > 10) {
            return getComplexUrl(route);
        }
        final Uri.Builder builder = createUriBuilder();
        appendPoint(builder, "origin", route.origin());
        appendPoint(builder, "destination", route.destination());
        appendWaypoints(builder, route.waypoints());
        return URLs.createUrl(builder.build().toString());
    }

    private static URL getComplexUrl(final Route route) {
        final StringBuilder pathBuilder = new StringBuilder("https://www.google.com/maps/dir");
        final List<String> dataTokens = new ArrayList<>();

        for (final Stop stop : route.stops()) {
            pathBuilder.append("/").append(Uri.encode(stop.address()));
            // FK-TODO: use ifPresentOrElse()
            if (stop.officialPlaceId().isPresent()) {
                dataTokens.add("1m5");
                dataTokens.add("1m4");
                dataTokens.add("1s" + stop.officialPlaceId().get().toUndocumentedPlaceId().value());
                dataTokens.add("8m2");
                dataTokens.add("3d" + format(stop.geodetic().getLatitude()));
                dataTokens.add("4d" + format(stop.geodetic().getLongitude()));
            } else {
                dataTokens.add("1m3");
                dataTokens.add("2m2");
                dataTokens.add("1d" + format(stop.geodetic().getLongitude()));
                dataTokens.add("2d" + format(stop.geodetic().getLatitude()));
            }
        }

        final int innerCount = dataTokens.size();
        final int outerCount = innerCount + 1;

        final StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("!3m2!1e3!4b1");
        dataBuilder.append("!4m").append(outerCount);
        dataBuilder.append("!4m").append(innerCount);
        for (final String token : dataTokens) {
            dataBuilder.append("!").append(token);
        }

        pathBuilder.append("/data=").append(dataBuilder).append("?entry=ttu");
        return URLs.createUrl(pathBuilder.toString());
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
                .map(RouteToUrlConverter::formatPlaceId)
                .collect(Collectors.joining("|"));
    }

    private static String formatPlaceId(final Stop stop) {
        return stop
                .officialPlaceId()
                .map(OfficialPlaceId::value)
                .orElse("");
    }

    private static String formatStop(final Stop stop) {
        return stop.officialPlaceId().isPresent() ?
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

    public static String format(final Angle angle) {
        final DecimalFormat df = new DecimalFormat("#.#######", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(angle.toDegrees());
    }
}