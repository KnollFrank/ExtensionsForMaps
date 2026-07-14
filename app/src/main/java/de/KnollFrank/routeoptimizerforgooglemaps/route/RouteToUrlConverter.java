package de.KnollFrank.routeoptimizerforgooglemaps.route;

import android.net.Uri;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.KnollFrank.routeoptimizerforgooglemaps.common.URLs;
import de.KnollFrank.routeoptimizerforgooglemaps.coordinate.Angle;

public class RouteToUrlConverter {

    // FK-TODO: refactor
    public static URL getUrl(final Route route) {
        final StringBuilder pathBuilder = new StringBuilder("https://www.google.com/maps/dir");
        final List<String> dataTokens = new ArrayList<>();

        for (final Stop stop : route.stops()) {
            pathBuilder.append("/").append(Uri.encode(stop.address()));
            dataTokens.add("1m3");
            dataTokens.add("2m2");
            dataTokens.add("1d" + format(stop.geodetic().getLongitude()));
            dataTokens.add("2d" + format(stop.geodetic().getLatitude()));
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

    public static String format(final Angle angle) {
        final DecimalFormat df = new DecimalFormat("#.#######", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(angle.toDegrees());
    }
}